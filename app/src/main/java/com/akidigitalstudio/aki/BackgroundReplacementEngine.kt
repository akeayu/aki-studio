package com.akidigitalstudio.aki

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint

/** Applies a user-painted foreground mask and solid background locally, retaining soft mask edges. */
object BackgroundReplacementEngine {
    fun replaceSolid(source: Bitmap, mask: Bitmap, color: Int): Bitmap {
        require(mask.width == source.width && mask.height == source.height) { "Mask dimensions must match source" }
        val output = source.copy(Bitmap.Config.ARGB_8888, true)
            ?: throw IllegalStateException("Could not copy source image")
        val red = Color.red(color).toFloat()
        val green = Color.green(color).toFloat()
        val blue = Color.blue(color).toFloat()
        // Mask is black for replaceable background and white for protected foreground.
        // The inverted luminance becomes overlay alpha; gray antialiased pixels blend softly.
        val matrix = ColorMatrix(floatArrayOf(
            0f, 0f, 0f, 0f, red,
            0f, 0f, 0f, 0f, green,
            0f, 0f, 0f, 0f, blue,
            -0.299f, -0.587f, -0.114f, 0f, 255f
        ))
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            colorFilter = ColorMatrixColorFilter(matrix)
        }
        Canvas(output).drawBitmap(mask, 0f, 0f, paint)
        return output
    }
}
