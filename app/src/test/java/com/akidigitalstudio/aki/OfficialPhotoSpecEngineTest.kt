package com.akidigitalstudio.aki

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class OfficialPhotoSpecEngineTest {
    private val today = LocalDate.of(2026, 11, 26)

    private fun authoritySpec(
        sourceUrl: String = "https://authority.example.gov/photo-standard",
        effectiveDate: String = "2025-01-01",
        verifiedDate: String = "2026-11-20"
    ) = OfficialPhotoSpecEngine.PhotoSpec(
        id = "test-standard",
        displayName = "Test authority standard",
        widthMm = 35,
        heightMm = 45,
        headHeightPercent = 70,
        eyeLineFromTopPercent = 42,
        provenance = OfficialPhotoSpecEngine.Provenance.AUTHORITY_VERIFIED,
        issuingAuthority = "Example issuing authority",
        sourceUrl = sourceUrl,
        effectiveDateIso = effectiveDate,
        verifiedDateIso = verifiedDate
    )

    @Test
    fun validCurrentAuthorityRecordAllowsComplianceClaim() {
        val result = OfficialPhotoSpecEngine.validate(authoritySpec(), today)
        assertTrue(result.valid)
        assertTrue(result.officialComplianceMayBeClaimed)
        assertTrue(result.issues.isEmpty())
    }

    @Test
    fun userDefinedTemplateNeverAllowsComplianceClaim() {
        val result = OfficialPhotoSpecEngine.validate(
            authoritySpec().copy(
                provenance = OfficialPhotoSpecEngine.Provenance.USER_DEFINED,
                issuingAuthority = null,
                sourceUrl = null,
                effectiveDateIso = null,
                verifiedDateIso = null
            ),
            today
        )
        assertTrue(result.valid)
        assertFalse(result.officialComplianceMayBeClaimed)
    }

    @Test
    fun httpSourceIsRejected() {
        val result = OfficialPhotoSpecEngine.validate(
            authoritySpec(sourceUrl = "http://authority.example.gov/standard"),
            today
        )
        assertFalse(result.valid)
        assertFalse(result.officialComplianceMayBeClaimed)
        assertTrue(result.issues.any { it.contains("HTTPS") })
    }

    @Test
    fun malformedDatesAreRejected() {
        val result = OfficialPhotoSpecEngine.validate(
            authoritySpec(effectiveDate = "01/01/2025", verifiedDate = "not-a-date"),
            today
        )
        assertFalse(result.valid)
        assertTrue(result.issues.any { it.contains("effective date") })
        assertTrue(result.issues.any { it.contains("verification date") })
    }

    @Test
    fun futureEffectiveStandardCannotBeClaimedAsCurrent() {
        val result = OfficialPhotoSpecEngine.validate(
            authoritySpec(effectiveDate = "2027-01-01"),
            today
        )
        assertFalse(result.valid)
        assertFalse(result.officialComplianceMayBeClaimed)
        assertTrue(result.issues.any { it.contains("not yet effective") })
    }

    @Test
    fun futureVerificationDateIsRejected() {
        val result = OfficialPhotoSpecEngine.validate(
            authoritySpec(verifiedDate = "2027-01-01"),
            today
        )
        assertFalse(result.valid)
        assertTrue(result.issues.any { it.contains("future") })
    }

    @Test
    fun thirtyFiveByFortyFiveMillimetresConvertsToExpected300DpiPixels() {
        assertEquals(413, OfficialPhotoSpecEngine.pixelsAt300Dpi(35))
        assertEquals(531, OfficialPhotoSpecEngine.pixelsAt300Dpi(45))
    }

    @Test(expected = IllegalArgumentException::class)
    fun invalidMillimetreDimensionIsRejected() {
        OfficialPhotoSpecEngine.pixelsAt300Dpi(9)
    }
}
