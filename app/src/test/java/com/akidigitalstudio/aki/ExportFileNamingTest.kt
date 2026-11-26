package com.akidigitalstudio.aki

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class ExportFileNamingTest {
    @Test
    fun expandsUserPatternTokensDeterministically() {
        assertEquals(
            "Studio_photo_20261126_053530",
            ExportFileNaming.expand("Studio_{type}_{date}", "photo", 1795671330000L)
        )
    }

    @Test
    fun sanitizesPathSeparatorsAndWhitespace() {
        assertEquals("passport_front.jpg", ExportFileNaming.safeBaseName("../passport/front", "jpg"))
    }

    @Test
    fun doesNotDuplicateKnownExtension() {
        assertEquals("portrait.png", ExportFileNaming.safeBaseName("portrait.PNG", "png"))
    }

    @Test
    fun rejectsBlankNames() {
        assertThrows(IllegalArgumentException::class.java) {
            ExportFileNaming.safeBaseName(" .. ", "jpg")
        }
    }
}
