package com.akidigitalstudio.aki

import android.graphics.Bitmap
import kotlin.math.abs
import kotlin.math.sqrt

/**
 * Explainable, low-resolution heuristic checks performed entirely on-device.
 * Scores are indicators, not calibrated camera measurements or official-photo certification.
 */
object PhotoQualityAssessment {
    data class Report(
        val width: Int,
        val height: Int,
        val aspectRatio: Float,
        val sharpnessScore: Double,
        val clippedPercent: Double,
        val contrastScore: Double,
        val borderVariation: Double,
        val warnings: List<String>
    )

    fun assess(source: Bitmap): Report {
        require(source.width > 0 && source.height > 0) { "Image must not be empty" }
        val sampleWidth = minOf(128, source.width).coerceAtLeast(3)
        val sampleHeight = minOf(128, source.height).coerceAtLeast(3)
        val pixels = IntArray(sampleWidth * sampleHeight)
        val luma = DoubleArray(pixels.size)
        var clipped = 0
        var sum = 0.0
        var sumSquared = 0.0
        var index = 0
        for (y in 0 until sampleHeight) {
            val sy = ((y.toLong() * source.height) / sampleHeight).toInt().coerceAtMost(source.height - 1)
            for (x in 0 until sampleWidth) {
                val sx = ((x.toLong() * source.width) / sampleWidth).toInt().coerceAtMost(source.width - 1)
                val color = source.getPixel(sx, sy)
                pixels[index] = color
                val value = 0.2126 * ((color shr 16) and 0xff) +
                    0.7152 * ((color shr 8) and 0xff) + 0.0722 * (color and 0xff)
                luma[index] = value
                if (value <= 5.0 || value >= 250.0) clipped++
                sum += value
                sumSquared += value * value
                index++
            }
        }
        val count = pixels.size.toDouble()
        val mean = sum / count
        val contrast = sqrt((sumSquared / count - mean * mean).coerceAtLeast(0.0))

        var laplaceSum = 0.0
        var laplaceSquared = 0.0
        var laplaceCount = 0
        for (y in 1 until sampleHeight - 1) for (x in 1 until sampleWidth - 1) {
            val i = y * sampleWidth + x
            val laplace = luma[i - 1] + luma[i + 1] + luma[i - sampleWidth] +
                luma[i + sampleWidth] - 4.0 * luma[i]
            laplaceSum += laplace
            laplaceSquared += laplace * laplace
            laplaceCount++
        }
        val laplaceMean = if (laplaceCount == 0) 0.0 else laplaceSum / laplaceCount
        val sharpness = if (laplaceCount == 0) 0.0 else
            (laplaceSquared / laplaceCount - laplaceMean * laplaceMean).coerceAtLeast(0.0)

        var borderSum = 0.0
        var borderSquared = 0.0
        var borderCount = 0
        for (y in 0 until sampleHeight) for (x in 0 until sampleWidth) {
            if (x == 0 || y == 0 || x == sampleWidth - 1 || y == sampleHeight - 1) {
                val color = pixels[y * sampleWidth + x]
                val r = ((color shr 16) and 0xff).toDouble()
                val g = ((color shr 8) and 0xff).toDouble()
                val b = (color and 0xff).toDouble()
                val gray = (r + g + b) / 3.0
                borderSum += gray
                borderSquared += gray * gray
                borderCount++
            }
        }
        val borderMean = borderSum / borderCount.coerceAtLeast(1)
        val borderVariation = sqrt((borderSquared / borderCount.coerceAtLeast(1) -
            borderMean * borderMean).coerceAtLeast(0.0))

        val clippedPercent = clipped * 100.0 / count
        val warnings = mutableListOf<String>()
        if (minOf(source.width, source.height) < 600) warnings += "Low pixel dimensions for detailed printing; check the receiving authority/printer's required resolution."
        if (sharpness < 35.0) warnings += "Possible softness or blur: low sampled Laplacian variance. Inspect the image at full size."
        if (clippedPercent > 8.0) warnings += "Potentially clipped shadows/highlights: more than 8% of sampled pixels are near black or white."
        if (contrast < 28.0) warnings += "Low global luminance contrast indicator; this may be intentional or caused by lighting."
        if (contrast > 95.0) warnings += "High global luminance contrast indicator; inspect harsh shadows and highlights."
        if (borderVariation > 48.0) warnings += "Image borders vary substantially; this does not prove the background is non-uniform."
        return Report(
            width = source.width,
            height = source.height,
            aspectRatio = source.width.toFloat() / source.height.toFloat(),
            sharpnessScore = sharpness,
            clippedPercent = clippedPercent,
            contrastScore = contrast,
            borderVariation = borderVariation,
            warnings = warnings
        )
    }
}
