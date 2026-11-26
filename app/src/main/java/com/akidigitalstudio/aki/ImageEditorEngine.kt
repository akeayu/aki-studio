package com.akidigitalstudio.aki

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.graphics.Matrix

/**
 * Deterministic, on-device image operations.
 * The source asset is never mutated; each operation returns a new bitmap.
 */
object ImageEditorEngine {
    fun rotate(source: Bitmap, degrees: Float): Bitmap {
        val matrix = Matrix().apply { postRotate(degrees) }
        return Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true)
    }

    fun monochrome(source: Bitmap): Bitmap {
        val output = Bitmap.createBitmap(source.width, source.height, Bitmap.Config.ARGB_8888)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            colorFilter = ColorMatrixColorFilter(ColorMatrix().apply { setSaturation(0f) })
        }
        Canvas(output).drawBitmap(source, 0f, 0f, paint)
        return output
    }

    fun cropCenter(source: Bitmap, aspectRatio: Float): Bitmap {
        require(aspectRatio.isFinite() && aspectRatio > 0f) { "Aspect ratio must be positive" }
        val sourceRatio = source.width.toFloat() / source.height.toFloat()
        val cropWidth: Int
        val cropHeight: Int
        if (sourceRatio > aspectRatio) {
            cropHeight = source.height
            cropWidth = (cropHeight * aspectRatio).toInt().coerceIn(1, source.width)
        } else {
            cropWidth = source.width
            cropHeight = (cropWidth / aspectRatio).toInt().coerceIn(1, source.height)
        }
        val left = (source.width - cropWidth) / 2
        val top = (source.height - cropHeight) / 2
        if (cropWidth == source.width && cropHeight == source.height) {
            return source.copy(Bitmap.Config.ARGB_8888, false)
                ?: throw IllegalStateException("Could not copy crop bitmap")
        }
        return Bitmap.createBitmap(source, left, top, cropWidth, cropHeight)
    }

    fun resizeWithin(source: Bitmap, maxWidth: Int, maxHeight: Int): Bitmap {
        require(maxWidth > 0 && maxHeight > 0) { "Maximum dimensions must be positive" }
        require(maxWidth.toLong() * maxHeight.toLong() <= 4_000_000L) {
            "Maximum dimensions exceed the safe pixel limit"
        }
        val scale = minOf(
            1.0,
            maxWidth.toDouble() / source.width.toDouble(),
            maxHeight.toDouble() / source.height.toDouble()
        )
        val width = (source.width * scale).toInt().coerceAtLeast(1)
        val height = (source.height * scale).toInt().coerceAtLeast(1)
        return resize(source, width, height)
    }

    fun resize(source: Bitmap, width: Int, height: Int): Bitmap {
        require(width > 0 && height > 0) { "Target dimensions must be positive" }
        require(width.toLong() * height.toLong() <= 4_000_000L) { "Target image exceeds the safe pixel limit" }
        if (source.width == width && source.height == height) {
            return source.copy(Bitmap.Config.ARGB_8888, false)
                ?: throw IllegalStateException("Could not copy resize bitmap")
        }
        return Bitmap.createScaledBitmap(source, width, height, true)
    }

    fun copy(source: Bitmap): Bitmap =
        source.copy(Bitmap.Config.ARGB_8888, false)
}
