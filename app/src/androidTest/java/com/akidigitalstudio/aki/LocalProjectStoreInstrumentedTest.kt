package com.akidigitalstudio.aki

import android.graphics.Bitmap
import android.test.AndroidTestCase
import android.test.ActivityInstrumentationTestCase2
import android.view.ViewGroup
import androidx.core.util.Consumer
import androidx.window.java.layout.WindowInfoTrackerCallbackAdapter
import androidx.window.layout.FoldingFeature
import androidx.window.layout.WindowInfoTracker
import androidx.window.layout.WindowLayoutInfo
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executor
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference
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


/** Device/emulator smoke coverage for compact, unfolded and half-open foldable windows. */
class MainActivityWindowInstrumentedTest :
    ActivityInstrumentationTestCase2<MainActivity>(MainActivity::class.java) {

    fun testWorkspaceStartsAndMatchesRequestedWindowClass() {
        val host = getActivity()
        getInstrumentation().waitForIdleSync()
        val content = host.findViewById<ViewGroup>(android.R.id.content)
        assertNotNull("Activity content root was not created", content)
        assertTrue("Workspace content is empty", content.childCount > 0)

        val expected = getInstrumentation().arguments.getString("expectedWidthClass")
        if (!expected.isNullOrBlank()) {
            val actual = AdaptiveUiProfile.from(host).widthClass.name.lowercase()
            assertEquals("Unexpected adaptive width class", expected.lowercase(), actual)
        }
    }

    fun testHalfOpenFoldIsDeliveredAsSeparatingFeatureWhenRequested() {
        if (getInstrumentation().arguments.getString("expectFoldingFeature") != "true") return

        val host = getActivity()
        val tracker = WindowInfoTrackerCallbackAdapter(WindowInfoTracker.getOrCreate(host))
        val received = CountDownLatch(1)
        val foldingFeature = AtomicReference<FoldingFeature?>()
        val listener = Consumer<WindowLayoutInfo> { layout ->
            foldingFeature.set(layout.displayFeatures.filterIsInstance<FoldingFeature>().firstOrNull())
            received.countDown()
        }
        tracker.addWindowLayoutInfoListener(host, Executor { command -> command.run() }, listener)
        try {
            assertTrue("WindowManager did not publish layout information", received.await(10, TimeUnit.SECONDS))
            val feature = foldingFeature.get()
            assertNotNull("No folding feature was reported in half-open posture", feature)
            assertTrue("Half-open hinge must separate the window", feature!!.isSeparating)
        } finally {
            tracker.removeWindowLayoutInfoListener(listener)
        }
    }
}
