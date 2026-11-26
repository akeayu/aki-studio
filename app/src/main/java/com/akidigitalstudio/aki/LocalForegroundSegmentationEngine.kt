package com.akidigitalstudio.aki

import android.graphics.Bitmap
import android.graphics.Color
import kotlin.math.max
import kotlin.math.sqrt

data class SegmentationResult(
    val foregroundMask: Bitmap,
    val method: String,
    val confidence: Float,
    val limitations: List<String>
)

/** Contract for a future bundled, fully on-device segmentation model. Implementations must not use network I/O. */
interface OnDeviceSegmentationModel {
    fun segment(source: Bitmap): SegmentationResult
}

/** Small replaceable provider boundary. Model errors fall back to the deterministic CPU heuristic. */
class LocalForegroundSegmentationEngine(
    private val model: OnDeviceSegmentationModel? = null,
    private val fallback: CpuBorderColorSegmentationProvider = CpuBorderColorSegmentationProvider()
) {
    fun segment(source: Bitmap): SegmentationResult {
        require(!source.isRecycled && source.width > 0 && source.height > 0)
        if (model != null) {
            try {
                val result = model.segment(source)
                require(result.foregroundMask.width == source.width && result.foregroundMask.height == source.height)
                return result
            } catch (_: Exception) {
                // Fail closed to a deterministic local estimate; do not attempt a remote service.
            }
        }
        return fallback.segment(source)
    }
}

/**
 * CPU-only border-colour estimate. It is deliberately described as a heuristic, not AI segmentation.
 * Work is bounded to a 512px analysis edge; the output mask is scaled back to source dimensions.
 */
class CpuBorderColorSegmentationProvider(
    private val distanceThreshold: Float = 58f,
    private val transitionWidth: Float = 30f
) {
    fun segment(source: Bitmap): SegmentationResult {
        require(!source.isRecycled && source.width > 0 && source.height > 0)
        val scale = minOf(1f, 512f / max(source.width, source.height).toFloat())
        val width = max(1, (source.width * scale).toInt())
        val height = max(1, (source.height * scale).toInt())
        val small = if (width == source.width && height == source.height) source.copy(Bitmap.Config.ARGB_8888, false) else Bitmap.createScaledBitmap(source, width, height, true)
        try {
            val pixels = IntArray(width * height)
            small.getPixels(pixels, 0, width, 0, 0, width, height)
            val border = ArrayList<Int>()
            val stepX = max(1, width / 64)
            val stepY = max(1, height / 64)
            for (x in 0 until width step stepX) {
                border += pixels[x]
                if (height > 1) border += pixels[(height - 1) * width + x]
            }
            for (y in stepY until height - 1 step stepY) {
                border += pixels[y * width]
                if (width > 1) border += pixels[y * width + width - 1]
            }
            val bgR = median(border.map { Color.red(it) })
            val bgG = median(border.map { Color.green(it) })
            val bgB = median(border.map { Color.blue(it) })
            val maskPixels = IntArray(pixels.size)
            val denominator = transitionWidth.coerceAtLeast(1f)
            for (i in pixels.indices) {
                val pixel = pixels[i]
                val dr = Color.red(pixel) - bgR
                val dg = Color.green(pixel) - bgG
                val db = Color.blue(pixel) - bgB
                val distance = sqrt((dr * dr + dg * dg + db * db).toFloat())
                // White protects likely foreground; black replaces likely border-colour background.
                val foreground = ((distance - distanceThreshold) / denominator).coerceIn(0f, 1f)
                val alpha = (foreground * 255f).toInt()
                maskPixels[i] = Color.argb(255, alpha, alpha, alpha)
            }
            val smallMask = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            smallMask.setPixels(maskPixels, 0, width, 0, 0, width, height)
            val output = if (width == source.width && height == source.height) smallMask
                else Bitmap.createScaledBitmap(smallMask, source.width, source.height, true).also { smallMask.recycle() }
            return SegmentationResult(
                foregroundMask = output,
                method = "CPU border-colour heuristic",
                confidence = 0.25f,
                limitations = listOf(
                    "Not a trained segmentation model; low-confidence estimate.",
                    "Assumes the background resembles colours along the image border.",
                    "Can remove similarly coloured hair, clothing or skin, and can retain clutter.",
                    "Review and correct the mask manually before export."
                )
            )
        } finally {
            if (!small.isRecycled) small.recycle()
        }
    }

    private fun median(values: List<Int>): Int {
        if (values.isEmpty()) return 0
        val sorted = values.sorted()
        return sorted[sorted.size / 2]
    }
}
