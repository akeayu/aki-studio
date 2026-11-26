package com.akidigitalstudio.aki

import org.junit.Assert.assertEquals
import org.junit.Test

class DesignStylePresetTest {
    @Test
    fun presetChangesAppearanceWithoutChangingLayerGeometryOrContent() {
        val original = DesignLayer(
            name = "Headline",
            kind = "TEXT",
            text = "Aki Studio",
            x = 0.32f,
            y = 0.41f,
            width = 0.62f,
            height = 0.22f,
            rotationDegrees = 27f,
            opacity = 0.8f
        )
        val preset = DesignStylePreset(
            name = "Test style",
            fillColor = 0xFFFFCC00.toInt(),
            strokeColor = 0xFF112233.toInt(),
            strokeWidth = 0.01f,
            fontScale = 0.08f,
            bold = true,
            italic = true,
            opacity = 0.65f,
            textAlignment = "LEFT",
            fontFamily = "SERIF",
            lineSpacingMultiplier = 1.4f,
            textEffect = "OUTLINE",
            effectColor = 0xFF000000.toInt(),
            effectWidth = 0.01f
        )

        val styled = preset.applyTo(original)

        assertEquals(original.text, styled.text)
        assertEquals(original.x, styled.x)
        assertEquals(original.y, styled.y)
        assertEquals(original.width, styled.width)
        assertEquals(original.height, styled.height)
        assertEquals(original.rotationDegrees, styled.rotationDegrees)
        assertEquals(0xFFFFCC00.toInt(), styled.fillColor)
        assertEquals("LEFT", styled.textAlignment)
        assertEquals("SERIF", styled.fontFamily)
        assertEquals(0.65f, styled.opacity)
    }
}

class AdaptiveUiProfileBreakpointTest {
    @Test
    fun compactClassCoversWidthsBelowSixHundredDp() {
        assertEquals(WindowWidthClass.COMPACT, AdaptiveUiProfile.classify(320))
        assertEquals(WindowWidthClass.COMPACT, AdaptiveUiProfile.classify(599))
    }

    @Test
    fun mediumClassIncludesSixHundredAndExcludesEightHundredFortyDp() {
        assertEquals(WindowWidthClass.MEDIUM, AdaptiveUiProfile.classify(600))
        assertEquals(WindowWidthClass.MEDIUM, AdaptiveUiProfile.classify(839))
    }

    @Test
    fun expandedClassStartsAtEightHundredFortyDp() {
        assertEquals(WindowWidthClass.EXPANDED, AdaptiveUiProfile.classify(840))
        assertEquals(WindowWidthClass.EXPANDED, AdaptiveUiProfile.classify(1200))
    }
}
