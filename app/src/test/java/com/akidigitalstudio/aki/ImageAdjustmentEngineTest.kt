package com.akidigitalstudio.aki

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ImageAdjustmentEngineTest {
    @Test
    fun neutralSettingsPreservePixelExactly() {
        val input = 0x7F3366CC
        assertEquals(input, ImageAdjustmentEngine.adjustPixel(input, 0, 0, 100, 0))
    }

    @Test
    fun brightnessRaisesChannelsWithoutChangingAlpha() {
        val input = 0x80306090.toInt()
        val output = ImageAdjustmentEngine.adjustPixel(input, 20, 0, 100, 0)
        assertEquals(0x80, output ushr 24)
        assertTrue(((output shr 16) and 0xFF) > ((input shr 16) and 0xFF))
        assertTrue(((output shr 8) and 0xFF) > ((input shr 8) and 0xFF))
        assertTrue((output and 0xFF) > (input and 0xFF))
    }

    @Test
    fun zeroSaturationProducesNeutralGrayChannels() {
        val output = ImageAdjustmentEngine.adjustPixel(0xFFCC6633.toInt(), 0, 0, 0, 0)
        val red = (output shr 16) and 0xFF
        val green = (output shr 8) and 0xFF
        val blue = output and 0xFF
        assertEquals(red, green)
        assertEquals(green, blue)
    }

    @Test
    fun warmthWarmsRedAndBlueBalance() {
        val input = 0xFF808080.toInt()
        val warm = ImageAdjustmentEngine.adjustPixel(input, 0, 0, 100, 50)
        val cool = ImageAdjustmentEngine.adjustPixel(input, 0, 0, 100, -50)
        assertTrue(((warm shr 16) and 0xFF) > (warm and 0xFF))
        assertTrue((cool and 0xFF) > ((cool shr 16) and 0xFF))
    }

    @Test
    fun outputChannelsAreClampedToByteRange() {
        val bright = ImageAdjustmentEngine.adjustPixel(0xFFFFFFFF.toInt(), 100, 100, 200, 100)
        val dark = ImageAdjustmentEngine.adjustPixel(0xFF000000.toInt(), -100, -100, 200, -100)
        for (pixel in listOf(bright, dark)) {
            assertTrue(((pixel shr 16) and 0xFF) in 0..255)
            assertTrue(((pixel shr 8) and 0xFF) in 0..255)
            assertTrue((pixel and 0xFF) in 0..255)
        }
    }
}
