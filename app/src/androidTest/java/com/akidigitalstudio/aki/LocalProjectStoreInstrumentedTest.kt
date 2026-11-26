package com.akidigitalstudio.aki

import android.graphics.Bitmap
import android.test.AndroidTestCase
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

/** Device-side regression: private project save/reopen must preserve styled text and vector paths. */
class LocalProjectStoreInstrumentedTest : AndroidTestCase() {
    fun testProjectSaveAndReopenPreservesDesignLayers() {
        val bitmap = Bitmap.createBitmap(24, 24, Bitmap.Config.ARGB_8888)
        val encoded = ByteArrayOutputStream().use { output ->
            assertTrue(bitmap.compress(Bitmap.CompressFormat.PNG, 100, output))
            output.toByteArray()
        }
        bitmap.recycle()

        val expected = listOf(
            DesignLayer(
                id = "ethiopic-title", name = "Maale title", kind = "TEXT", text = "ማሌ / Maale",
                fontFamily = "ETHIOPIC_SYSTEM", textAlignment = "LEFT",
                lineSpacingMultiplier = 1.35f, textEffect = "SHADOW"
            ),
            DesignLayer(
                id = "bezier-path", name = "Curve", kind = "SHAPE", shape = "CUSTOM_PATH",
                pathData = "M 0.1 0.8 C 0.1 0.2 0.9 0.2 0.9 0.8 Z"
            )
        )
        val settings = mapOf(
            "rotation" to "0",
            "backgroundReplaceEnabled" to "false",
            "designLayers" to DesignLayerDocument.encode(expected)
        )
        val summary = LocalProjectStore.save(
            context.filesDir, "design-regression", ByteArrayInputStream(encoded), settings, null
        )
        try {
            val reopened = LocalProjectStore.load(summary)
            try {
                assertEquals(expected, DesignLayerDocument.decode(reopened.values["designLayers"]))
            } finally {
                reopened.mask?.recycle()
            }
        } finally {
            LocalProjectStore.delete(summary)
        }
    }
}
