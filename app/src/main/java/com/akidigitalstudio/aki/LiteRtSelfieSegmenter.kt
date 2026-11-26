package com.akidigitalstudio.aki

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.channels.FileChannel

/**
 * Optional bundled MediaPipe Selfie Segmenter adapter.
 *
 * Place the independently verified selfie_segmenter.tflite in app/src/main/assets.
 * If the asset is absent or incompatible, createOrNull returns null and the existing
 * deterministic CPU heuristic remains available. No network access is performed.
 */
class LiteRtSelfieSegmenter private constructor(
    private val interpreter: Interpreter
) : OnDeviceSegmentationModel, AutoCloseable {

    override fun segment(source: Bitmap): SegmentationResult {
        require(!source.isRecycled && source.width > 0 && source.height > 0)

        val input = Array(1) { Array(INPUT_SIZE) { Array(INPUT_SIZE) { FloatArray(3) } } }
        val square = centerSquare(source)
        try {
            val pixels = IntArray(INPUT_SIZE * INPUT_SIZE)
            square.getPixels(pixels, 0, INPUT_SIZE, 0, 0, INPUT_SIZE, INPUT_SIZE)
            var p = 0
            for (y in 0 until INPUT_SIZE) {
                for (x in 0 until INPUT_SIZE) {
                    val color = pixels[p++]
                    input[0][y][x][0] = Color.red(color) / 255f
                    input[0][y][x][1] = Color.green(color) / 255f
                    input[0][y][x][2] = Color.blue(color) / 255f
                }
            }
        } finally {
            square.recycle()
        }

        val output = Array(1) {
            Array(INPUT_SIZE) { Array(INPUT_SIZE) { FloatArray(1) } }
        }
        interpreter.run(input, output)

        val maskPixels = IntArray(INPUT_SIZE * INPUT_SIZE)
        var index = 0
        for (y in 0 until INPUT_SIZE) {
            for (x in 0 until INPUT_SIZE) {
                val confidence = output[0][y][x][0].coerceIn(0f, 1f)
                val gray = (confidence * 255f).toInt()
                maskPixels[index++] = Color.argb(255, gray, gray, gray)
            }
        }

        val squareMask = Bitmap.createBitmap(INPUT_SIZE, INPUT_SIZE, Bitmap.Config.ARGB_8888)
        squareMask.setPixels(maskPixels, 0, INPUT_SIZE, 0, 0, INPUT_SIZE, INPUT_SIZE)

        // Resize the complete source field into model coordinates and map the mask back.
        // This can distort aspect ratio; it avoids silently excluding border regions.
        val sourceMask = Bitmap.createBitmap(source.width, source.height, Bitmap.Config.ARGB_8888)
        android.graphics.Canvas(sourceMask).drawBitmap(
            squareMask,
            null,
            android.graphics.Rect(0, 0, source.width, source.height),
            android.graphics.Paint(android.graphics.Paint.FILTER_BITMAP_FLAG)
        )
        squareMask.recycle()

        return SegmentationResult(
            foregroundMask = sourceMask,
            method = "Bundled LiteRT Selfie Segmenter (CPU)",
            confidence = 0.5f,
            limitations = listOf(
                "Model confidence values are not calibrated probabilities of correctness.",
                "The model was designed for prominent-person segmentation, not official-photo compliance.",
                "Fine hair, translucent edges, multiple people and unusual backgrounds may be inaccurate.",
                "Center-square inference excludes image regions outside the central square; inspect and correct manually."
            )
        )
    }

    override fun close() {
        interpreter.close()
    }

    private fun centerSquare(source: Bitmap): Bitmap {
        // Full-frame resize matches the model's fixed square tensor contract.
        return Bitmap.createBitmap(INPUT_SIZE, INPUT_SIZE, Bitmap.Config.ARGB_8888).also { resized ->
            android.graphics.Canvas(resized).drawBitmap(
                source,
                null,
                android.graphics.Rect(0, 0, INPUT_SIZE, INPUT_SIZE),
                android.graphics.Paint(android.graphics.Paint.FILTER_BITMAP_FLAG)
            )
        }
    }

    companion object {
        const val ASSET_NAME = "selfie_segmenter.tflite"
        private const val INPUT_SIZE = 256

        /** Returns null when the verified model asset is not packaged. */
        fun createOrNull(context: Context): LiteRtSelfieSegmenter? {
            val descriptor = try {
                context.assets.openFd(ASSET_NAME)
            } catch (_: Exception) {
                return null
            }
            return try {
                val model = FileInputStream(descriptor.fileDescriptor).channel.map(
                    FileChannel.MapMode.READ_ONLY,
                    descriptor.startOffset,
                    descriptor.declaredLength
                )
                val options = Interpreter.Options().apply { setNumThreads(2) }
                val interpreter = Interpreter(model, options)
                try {
                    validateTensorShapes(interpreter)
                    LiteRtSelfieSegmenter(interpreter)
                } catch (error: Exception) {
                    interpreter.close()
                    throw error
                }
            } catch (_: Exception) {
                null
            } finally {
                descriptor.close()
            }
        }

        private fun validateTensorShapes(interpreter: Interpreter) {
            val inputShape = interpreter.getInputTensor(0).shape()
            val outputShape = interpreter.getOutputTensor(0).shape()
            require(inputShape.contentEquals(intArrayOf(1, INPUT_SIZE, INPUT_SIZE, 3))) {
                "Unexpected model input tensor shape"
            }
            require(outputShape.contentEquals(intArrayOf(1, INPUT_SIZE, INPUT_SIZE, 1))) {
                "Unexpected model output tensor shape"
            }
            require(interpreter.getInputTensor(0).dataType() == org.tensorflow.lite.DataType.FLOAT32)
            require(interpreter.getOutputTensor(0).dataType() == org.tensorflow.lite.DataType.FLOAT32)
        }
    }
}
