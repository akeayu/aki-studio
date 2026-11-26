package com.akidigitalstudio.aki

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test
import java.util.TimeZone

class ExportFileNamingTest {
    @Test
    fun expandsUserPatternTokensDeterministically() {
        val previousZone = TimeZone.getDefault()
        try {
            TimeZone.setDefault(TimeZone.getTimeZone("UTC"))
            assertEquals(
                "Studio_photo_20261126_053530",
                ExportFileNaming.expand("Studio_{type}_{date}", "photo", 1795671330000L)
            )
        } finally {
            TimeZone.setDefault(previousZone)
        }
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
    fun replacesAnyKnownExtensionWithSelectedExportExtension() {
        assertEquals("portrait.jpg", ExportFileNaming.safeBaseName("portrait.PNG", "jpg"))
    }

    @Test
    fun rejectsBlankNames() {
        assertThrows(IllegalArgumentException::class.java) {
            ExportFileNaming.safeBaseName(" .. ", "jpg")
        }
    }
}
