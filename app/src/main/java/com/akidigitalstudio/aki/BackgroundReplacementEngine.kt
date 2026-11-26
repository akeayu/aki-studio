package com.akidigitalstudio.aki

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint

/** Applies a user-painted foreground mask and solid background locally. */
object BackgroundReplacementEngine {
    fun replaceSolid(source: Bitmap, mask: Bitmap, color: Int): Bitmap {
        require(mask.width == source.width && mask.height == source.height) { "Mask dimensions must match source" }
        val output = source.copy(Bitmap.Config.ARGB_8888, true)
            ?: throw IllegalStateException("Could not copy source image")
        val canvas = Canvas(output)
        val background = Paint(Paint.ANTI_ALIAS_FLAG).apply { this.color = color }
        // White mask pixels protect the original foreground; black pixels receive replacement.
        val inverse = Bitmap.createBitmap(mask.width, mask.height, Bitmap.Config.ARGB_8888)
        try {
            val pixels = IntArray(mask.width * mask.height)
            mask.getPixels(pixels, 0, mask.width, 0, 0, mask.width, mask.height)
            for (i in pixels.indices) {
                val alpha = Color.alpha(pixels[i])
                val luminance = (Color.red(pixels[i]) + Color.green(pixels[i]) + Color.blue(pixels[i])) / 3
                pixels[i] = if (alpha == 0 || luminance < 128) Color.WHITE else Color.TRANSPARENT
            }
            inverse.setPixels(pixels, 0, mask.width, 0, 0, mask.width, mask.height)
            canvas.drawBitmap(inverse, 0f, 0f, Paint(Paint.ANTI_ALIAS_FLAG).apply {
                colorFilter = android.graphics.PorterDuffColorFilter(color, android.graphics.PorterDuff.Mode.SRC_IN)
            })
        } finally {
            inverse.recycle()
        }
        return output
    }
}
