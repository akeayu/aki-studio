package com.akidigitalstudio.aki

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BlurMaskFilter
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.RectF
import android.view.MotionEvent
import android.view.View
import kotlin.math.min

/** Manual foreground mask. No automatic segmentation or face detection is performed. */
class ManualMaskView(context: Context) : View(context) {
    private data class Stroke(
        val points: MutableList<Pair<Float, Float>>,
        val erase: Boolean,
        val brushPercent: Float,
        val featherPercent: Float
    )
    private val strokes = mutableListOf<Stroke>()
    private var baseMask: Bitmap? = null
    private var cursor = 0
    private var imageWidth = 0
    private var imageHeight = 0
    private var editing = false
    private var eraseMode = false
    private var brushPercent = 3.5f
    private var featherPercent = 0.8f

    fun configure(bitmap: Bitmap?, enabled: Boolean, erase: Boolean = false) {
        imageWidth = bitmap?.width ?: 0
        imageHeight = bitmap?.height ?: 0
        editing = enabled
        eraseMode = erase
        visibility = if (enabled && imageWidth > 0) VISIBLE else GONE
        invalidate()
    }

    fun setBrushPercent(value: Float) { brushPercent = value.coerceIn(0.8f, 12f) }
    fun setFeatherPercent(value: Float) { featherPercent = value.coerceIn(0f, 4f) }
    fun canUndo() = cursor > 0
    fun canRedo() = cursor < strokes.size

    fun undo() {
        if (canUndo()) { cursor--; invalidate() }
    }

    fun redo() {
        if (canRedo()) { cursor++; invalidate() }
    }

    fun setBaseMask(mask: Bitmap?) {
        baseMask?.recycle()
        baseMask = mask?.copy(Bitmap.Config.ARGB_8888, false)
        invalidate()
    }

    fun clearMask() {
        baseMask?.recycle()
        baseMask = null
        strokes.clear()
        cursor = 0
        invalidate()
    }

    fun renderMask(width: Int, height: Int): Bitmap {
        require(width > 0 && height > 0 && width.toLong() * height <= 16_000_000L)
        val mask = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(mask)
        canvas.drawColor(Color.BLACK)
        baseMask?.let { canvas.drawBitmap(it, null, RectF(0f, 0f, width.toFloat(), height.toFloat()), Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)) }
        drawStrokes(canvas, width.toFloat(), height.toFloat(), export = true)
        return mask
    }

    private fun drawStrokes(canvas: Canvas, width: Float, height: Float, export: Boolean) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
        }
        for (i in 0 until cursor) {
            val stroke = strokes[i]
            val base = min(width, height)
            paint.strokeWidth = base * stroke.brushPercent / 100f
            paint.maskFilter = if (stroke.featherPercent > 0f)
                BlurMaskFilter(base * stroke.featherPercent / 100f, BlurMaskFilter.Blur.NORMAL) else null
            paint.xfermode = if (stroke.erase) PorterDuffXfermode(PorterDuff.Mode.CLEAR) else null
            paint.color = if (stroke.erase) Color.TRANSPARENT else Color.WHITE
            val path = Path()
            stroke.points.forEachIndexed { index, point ->
                val x = point.first * width
                val y = point.second * height
                if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }
            if (stroke.points.size == 1) {
                val p = stroke.points[0]
                canvas.drawCircle(p.first * width, p.second * height, paint.strokeWidth / 2f, paint)
            } else canvas.drawPath(path, paint)
        }
        paint.maskFilter = null
        paint.xfermode = null
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!editing || imageWidth <= 0 || imageHeight <= 0) return false
        val image = imageBounds()
        if (event.action == MotionEvent.ACTION_DOWN && !image.contains(event.x, event.y)) return false
        val x = ((event.x - image.left) / image.width()).coerceIn(0f, 1f)
        val y = ((event.y - image.top) / image.height()).coerceIn(0f, 1f)
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                while (strokes.size > cursor) strokes.removeAt(strokes.lastIndex)
                strokes += Stroke(mutableListOf(x to y), eraseMode, brushPercent, featherPercent)
                cursor = strokes.size
                parent?.requestDisallowInterceptTouchEvent(true)
                invalidate()
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                strokes.getOrNull(cursor - 1)?.points?.add(x to y)
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
        val layer = canvas.saveLayer(bounds, null)
        baseMask?.let { mask ->
            val tint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG).apply {
                colorFilter = android.graphics.PorterDuffColorFilter(Color.argb(115, 0, 160, 255), PorterDuff.Mode.SRC_IN)
            }
            canvas.drawBitmap(mask, null, bounds, tint)
        }
        // Preview uses the same normalized geometry and brush widths as export.
        canvas.translate(bounds.left, bounds.top)
        drawStrokes(canvas, bounds.width(), bounds.height(), export = false)
        // Draw a subtle outline so the user can see the image area while painting.
        canvas.restoreToCount(layer)
    }
}
