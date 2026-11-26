package com.akidigitalstudio.aki

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.view.View
import kotlin.math.min

/**
 * Transparent, manually interpreted composition guides.
 * The overlay does not detect a face or certify a photo.
 */
class GuideOverlayView(context: Context) : View(context) {
    private var imageWidth = 0
    private var imageHeight = 0
    private var headHeightPercent = 70
    private var eyeLinePercent = 42
    private var guidesEnabled = false

    private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.rgb(40, 220, 150)
        style = Paint.Style.STROKE
        strokeWidth = resources.displayMetrics.density * 1.5f
    }
    private val centerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.argb(170, 255, 255, 255)
        style = Paint.Style.STROKE
        strokeWidth = resources.displayMetrics.density
        pathEffect = android.graphics.DashPathEffect(floatArrayOf(7f, 6f), 0f)
    }
    private val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textSize = resources.displayMetrics.scaledDensity * 11f
        setShadowLayer(3f, 1f, 1f, Color.BLACK)
    }

    fun configure(bitmap: android.graphics.Bitmap?, headPercent: Int, eyePercent: Int, visible: Boolean) {
        imageWidth = bitmap?.width ?: 0
        imageHeight = bitmap?.height ?: 0
        headHeightPercent = headPercent.coerceIn(40, 90)
        eyeLinePercent = eyePercent.coerceIn(20, 70)
        guidesEnabled = visible && imageWidth > 0 && imageHeight > 0
        visibility = if (guidesEnabled) VISIBLE else GONE
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (!guidesEnabled || width <= 0 || height <= 0) return
        val scale = min(width.toFloat() / imageWidth, height.toFloat() / imageHeight)
        val drawWidth = imageWidth * scale
        val drawHeight = imageHeight * scale
        val image = RectF(
            (width - drawWidth) / 2f,
            (height - drawHeight) / 2f,
            (width + drawWidth) / 2f,
            (height + drawHeight) / 2f
        )
        val faceWidth = image.width() * 0.54f
        val faceHeight = image.height() * headHeightPercent / 100f
        val faceTop = image.top + image.height() * 0.06f
        val face = RectF(
            image.centerX() - faceWidth / 2f,
            faceTop,
            image.centerX() + faceWidth / 2f,
            faceTop + faceHeight
        )
        canvas.drawRect(image, centerPaint)
        canvas.drawLine(image.centerX(), image.top, image.centerX(), image.bottom, centerPaint)
        canvas.drawOval(face, linePaint)
        val eyeY = image.top + image.height() * eyeLinePercent / 100f
        canvas.drawLine(face.left - image.width() * 0.08f, eyeY, face.right + image.width() * 0.08f, eyeY, linePaint)
        canvas.drawText("HEAD", face.left, (face.top - 6f).coerceAtLeast(image.top + 14f), labelPaint)
        canvas.drawText("EYE LINE", face.right + 5f, eyeY - 5f, labelPaint)
    }
}
