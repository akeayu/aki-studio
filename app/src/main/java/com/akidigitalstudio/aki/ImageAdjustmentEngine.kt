package com.akidigitalstudio.aki

import android.graphics.Bitmap
import kotlin.math.roundToInt

/**
 * Deterministic, CPU-only tonal adjustment pass.
 *
 * The input is never modified. The caller owns and must recycle the returned bitmap.
 * Processing uses one scanline buffer so temporary memory is bounded by image width.
 */
object ImageAdjustmentEngine {
    fun apply(
        source: Bitmap,
        brightness: Int,
        contrast: Int,
        saturation: Int,
        warmth: Int
    ): Bitmap {
        require(brightness in -100..100) { "Brightness must be between -100 and 100" }
        require(contrast in -100..100) { "Contrast must be between -100 and 100" }
        require(saturation in 0..200) { "Saturation must be between 0 and 200" }
        require(warmth in -100..100) { "Warmth must be between -100 and 100" }

        val output = source.copy(Bitmap.Config.ARGB_8888, true)
            ?: throw IllegalStateException("Could not allocate adjustment output")
        val width = source.width
        val height = source.height
        val row = IntArray(width)

        try {
            for (y in 0 until height) {
                source.getPixels(row, 0, width, 0, y, width, 1)
                for (x in row.indices) {
                    row[x] = adjustPixel(row[x], brightness, contrast, saturation, warmth)
                }
                output.setPixels(row, 0, width, 0, y, width, 1)
            }
            return output
        } catch (error: Exception) {
            output.recycle()
            throw error
        }
    }

    /** Pure pixel transform kept separate so its colour math can be JVM unit-tested. */
    internal fun adjustPixel(
        pixel: Int,
        brightness: Int,
        contrast: Int,
        saturation: Int,
        warmth: Int
    ): Int {
        val alpha = pixel ushr 24
        var red = (pixel shr 16) and 0xFF
        var green = (pixel shr 8) and 0xFF
        var blue = pixel and 0xFF

        val brightnessOffset = brightness * 1.28f
        val contrastFactor = 1f + contrast / 100f
        val saturationFactor = saturation / 100f
        val warmthOffset = warmth * 0.42f

        red = ((red - 127.5f) * contrastFactor + 127.5f + brightnessOffset + warmthOffset)
            .roundToInt().coerceIn(0, 255)
        green = ((green - 127.5f) * contrastFactor + 127.5f + brightnessOffset)
            .roundToInt().coerceIn(0, 255)
        blue = ((blue - 127.5f) * contrastFactor + 127.5f + brightnessOffset - warmthOffset)
            .roundToInt().coerceIn(0, 255)

        val luma = 0.213f * red + 0.715f * green + 0.072f * blue
        red = (luma + (red - luma) * saturationFactor).roundToInt().coerceIn(0, 255)
        green = (luma + (green - luma) * saturationFactor).roundToInt().coerceIn(0, 255)
        blue = (luma + (blue - luma) * saturationFactor).roundToInt().coerceIn(0, 255)

        return (alpha shl 24) or (red shl 16) or (green shl 8) or blue
    }
}
