package com.akidigitalstudio.aki

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.view.MotionEvent
import android.view.View
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

/**
 * Local interactive crop selector. Coordinates are kept in source-image pixels;
 * the caller persists normalized coordinates so edits survive preview scaling.
 */
class InteractiveCropView(
    context: Context,
    private val bitmap: Bitmap,
    aspectRatio: Float,
    initial: FloatArray?
) : View(context) {
    private val ratio = aspectRatio
    private val imageBounds = RectF()
    private val crop = RectF()
    private val imagePaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
    private val shadePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0x99000000.toInt() }
    private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFFFFFFFF.toInt(); style = Paint.Style.STROKE; strokeWidth = 2.5f
    }
    private val gridPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0x99FFFFFF.toInt(); strokeWidth = 1f
    }
    private val handlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xFFFFFFFF.toInt() }
    private val minCrop = min(bitmap.width, bitmap.height) * 0.08f
    private var mode = Mode.NONE
    private var activeCorner = -1
    private var downX = 0f
    private var downY = 0f
    private var startCrop = RectF()

    private enum class Mode { NONE, MOVE, RESIZE }

    init {
        require(aspectRatio.isFinite() && aspectRatio > 0f)
        val initialRect = if (initial != null && initial.size == 4) {
            RectF(initial[0] * bitmap.width, initial[1] * bitmap.height,
                initial[2] * bitmap.width, initial[3] * bitmap.height)
        } else defaultCrop()
        crop.set(clampRect(initialRect))
        contentDescription = "Drag the crop area to move it. Drag a corner to resize it."
    }

    private fun defaultCrop(): RectF {
        val imageRatio = bitmap.width.toFloat() / bitmap.height
        val w: Float
        val h: Float
        if (imageRatio > ratio) { h = bitmap.height * 0.82f; w = h * ratio }
        else { w = bitmap.width * 0.82f; h = w / ratio }
        return RectF((bitmap.width - w) / 2f, (bitmap.height - h) / 2f,
            (bitmap.width + w) / 2f, (bitmap.height + h) / 2f)
    }

    fun normalizedRect(): FloatArray = floatArrayOf(
        crop.left / bitmap.width, crop.top / bitmap.height,
        crop.right / bitmap.width, crop.bottom / bitmap.height
    )

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val scale = min(width.toFloat() / bitmap.width, height.toFloat() / bitmap.height)
        val drawW = bitmap.width * scale
        val drawH = bitmap.height * scale
        imageBounds.set((width - drawW) / 2f, (height - drawH) / 2f,
            (width + drawW) / 2f, (height + drawH) / 2f)
        canvas.drawBitmap(bitmap, null, imageBounds, imagePaint)
        val shown = RectF(
            imageBounds.left + crop.left / bitmap.width * imageBounds.width(),
            imageBounds.top + crop.top / bitmap.height * imageBounds.height(),
            imageBounds.left + crop.right / bitmap.width * imageBounds.width(),
            imageBounds.top + crop.bottom / bitmap.height * imageBounds.height()
        )
        canvas.drawRect(imageBounds.left, imageBounds.top, imageBounds.right, shown.top, shadePaint)
        canvas.drawRect(imageBounds.left, shown.bottom, imageBounds.right, imageBounds.bottom, shadePaint)
        canvas.drawRect(imageBounds.left, shown.top, shown.left, shown.bottom, shadePaint)
        canvas.drawRect(shown.right, shown.top, imageBounds.right, shown.bottom, shadePaint)
        canvas.drawRect(shown, borderPaint)
        for (i in 1..2) {
            val x = shown.left + shown.width() * i / 3f
            val y = shown.top + shown.height() * i / 3f
            canvas.drawLine(x, shown.top, x, shown.bottom, gridPaint)
            canvas.drawLine(shown.left, y, shown.right, y, gridPaint)
        }
        val radius = 8f * resources.displayMetrics.density
        for (x in floatArrayOf(shown.left, shown.right)) for (y in floatArrayOf(shown.top, shown.bottom)) {
            canvas.drawCircle(x, y, radius, handlePaint)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                if (!imageBounds.contains(event.x, event.y)) return false
                downX = toImageX(event.x); downY = toImageY(event.y)
                startCrop.set(crop)
                activeCorner = nearestCorner(downX, downY)
                mode = if (activeCorner >= 0) Mode.RESIZE else if (crop.contains(downX, downY)) Mode.MOVE else Mode.NONE
                if (mode == Mode.NONE) return false
                parent?.requestDisallowInterceptTouchEvent(true)
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                val dx = toImageX(event.x) - downX
                val dy = toImageY(event.y) - downY
                when (mode) {
                    Mode.MOVE -> {
                        val left = (startCrop.left + dx).coerceIn(0f, bitmap.width - startCrop.width())
                        val top = (startCrop.top + dy).coerceIn(0f, bitmap.height - startCrop.height())
                        crop.set(left, top, left + startCrop.width(), top + startCrop.height())
                    }
                    Mode.RESIZE -> resizeFromCorner(dx, dy)
                    Mode.NONE -> Unit
                }
                invalidate()
                return true
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                val handled = mode != Mode.NONE
                mode = Mode.NONE
                parent?.requestDisallowInterceptTouchEvent(false)
                performClick()
                return handled
            }
        }
        return true
    }

    private fun resizeFromCorner(dx: Float, dy: Float) {
        val leftSide = activeCorner == 0 || activeCorner == 2
        val topSide = activeCorner == 0 || activeCorner == 1
        val anchorX = if (leftSide) startCrop.right else startCrop.left
        val anchorY = if (topSide) startCrop.bottom else startCrop.top
        val rawW = abs((if (leftSide) startCrop.left else startCrop.right) + dx - anchorX)
        val rawH = abs((if (topSide) startCrop.top else startCrop.bottom) + dy - anchorY)
        var w = max(rawW, rawH * ratio)
        var h = w / ratio
        val maxW = if (leftSide) anchorX else bitmap.width - anchorX
        val maxH = if (topSide) anchorY else bitmap.height - anchorY
        w = min(w, min(maxW, maxH * ratio))
        h = w / ratio
        if (w < minCrop || h < minCrop) return
        val movingX = if (leftSide) anchorX - w else anchorX + w
        val movingY = if (topSide) anchorY - h else anchorY + h
        crop.set(min(anchorX, movingX), min(anchorY, movingY),
            max(anchorX, movingX), max(anchorY, movingY))
    }

    private fun nearestCorner(x: Float, y: Float): Int {
        val points = arrayOf(
            crop.left to crop.top, crop.right to crop.top,
            crop.left to crop.bottom, crop.right to crop.bottom
        )
        val threshold = min(bitmap.width, bitmap.height) * 0.07f
        return points.indices.minByOrNull { i ->
            val dx = points[i].first - x
            val dy = points[i].second - y
            dx * dx + dy * dy
        }?.takeIf { i ->
            val p = points[i]
            val dx = p.first - x; val dy = p.second - y
            dx * dx + dy * dy <= threshold * threshold
        } ?: -1
    }

    private fun clampRect(value: RectF): RectF {
        val w = value.width().coerceIn(minCrop, bitmap.width.toFloat())
        val h = (w / ratio).coerceAtMost(bitmap.height.toFloat())
        val actualW = h * ratio
        val left = value.left.coerceIn(0f, bitmap.width - actualW)
        val top = value.top.coerceIn(0f, bitmap.height - h)
        return RectF(left, top, left + actualW, top + h)
    }

    private fun toImageX(x: Float) = ((x - imageBounds.left) / imageBounds.width() * bitmap.width).coerceIn(0f, bitmap.width.toFloat())
    private fun toImageY(y: Float) = ((y - imageBounds.top) / imageBounds.height() * bitmap.height).coerceIn(0f, bitmap.height.toFloat())

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }
}
