package com.akidigitalstudio.aki

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.view.MotionEvent
import android.view.View
import kotlin.math.min

/**
 * Manual foreground-protection mask editor. Strokes are normalized to the displayed image.
 * This is not automatic segmentation; users must paint carefully and inspect edges.
 */
class ManualMaskView(context: Context) : View(context) {
    private data class Stroke(val points: MutableList<Pair<Float, Float>>, val erase: Boolean)
    private val strokes = mutableListOf<Stroke>()
    private var imageWidth = 0
    private var imageHeight = 0
    private var editing = false
    private var eraseMode = false
    private val displayPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.argb(110, 0, 170, 255)
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
        strokeWidth = 22f
    }

    fun configure(bitmap: Bitmap?, enabled: Boolean, erase: Boolean = false) {
        imageWidth = bitmap?.width ?: 0
        imageHeight = bitmap?.height ?: 0
        editing = enabled
        eraseMode = erase
        visibility = if (enabled && imageWidth > 0) VISIBLE else GONE
        invalidate()
    }

    fun clearMask() {
        strokes.clear()
        invalidate()
    }

    fun renderMask(width: Int, height: Int): Bitmap {
        require(width > 0 && height > 0 && width.toLong() * height <= 16_000_000L)
        val mask = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(mask)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            style = Paint.Style.STROKE
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
            strokeWidth = min(width, height) * 0.035f
        }
        for (stroke in strokes) {
            paint.color = if (stroke.erase) Color.BLACK else Color.WHITE
            if (stroke.points.size == 1) {
                val p = stroke.points[0]
                canvas.drawCircle(p.first * width, p.second * height, paint.strokeWidth / 2f, paint)
            } else {
                val path = Path()
                stroke.points.forEachIndexed { i, point ->
                    val x = point.first * width
                    val y = point.second * height
                    if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                }
                canvas.drawPath(path, paint)
            }
        }
        return mask
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!editing || imageWidth <= 0 || imageHeight <= 0) return false
        val image = imageBounds()
        if (event.action == MotionEvent.ACTION_DOWN && !image.contains(event.x, event.y)) return false
        val x = ((event.x - image.left) / image.width()).coerceIn(0f, 1f)
        val y = ((event.y - image.top) / image.height()).coerceIn(0f, 1f)
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                strokes += Stroke(mutableListOf(x to y), eraseMode)
                parent?.requestDisallowInterceptTouchEvent(true)
                invalidate()
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                strokes.lastOrNull()?.points?.add(x to y)
                invalidate()
                return true
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                parent?.requestDisallowInterceptTouchEvent(false)
                invalidate()
                return true
            }
        }
        return true
    }

    private fun imageBounds(): RectF {
        val scale = min(width.toFloat() / imageWidth, height.toFloat() / imageHeight)
        val w = imageWidth * scale
        val h = imageHeight * scale
        return RectF((width-w)/2f, (height-h)/2f, (width+w)/2f, (height+h)/2f)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (imageWidth <= 0 || imageHeight <= 0) return
        val bounds = imageBounds()
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(125, 0, 170, 255)
            style = Paint.Style.STROKE
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
            strokeWidth = 22f * resources.displayMetrics.density
        }
        for (stroke in strokes) {
            paint.color = if (stroke.erase) Color.argb(100, 255, 90, 70) else Color.argb(125, 0, 170, 255)
            val path = Path()
            stroke.points.forEachIndexed { i, p ->
                val x = bounds.left + p.first * bounds.width()
                val y = bounds.top + p.second * bounds.height()
                if (i == 0) path.moveTo(x,y) else path.lineTo(x,y)
            }
            if (stroke.points.size == 1) {
                val p = stroke.points[0]
                canvas.drawCircle(bounds.left + p.first*bounds.width(), bounds.top + p.second*bounds.height(), paint.strokeWidth/2f, paint)
            } else canvas.drawPath(path, paint)
        }
    }
}
