package com.akidigitalstudio.aki

import org.junit.Assert.assertEquals
import org.junit.Test

class AdaptiveUiProfileTest {
    @Test fun compactWindowBelow600Dp() {
        assertEquals(WindowWidthClass.COMPACT, AdaptiveUiProfile.classify(599))
    }
    @Test fun mediumWindowStartsAt600Dp() {
        assertEquals(WindowWidthClass.MEDIUM, AdaptiveUiProfile.classify(600))
    }
    @Test fun expandedWindowStartsAt840Dp() {
        assertEquals(WindowWidthClass.EXPANDED, AdaptiveUiProfile.classify(840))
    }
}
