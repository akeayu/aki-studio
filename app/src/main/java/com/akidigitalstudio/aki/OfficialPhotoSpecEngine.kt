package com.akidigitalstudio.aki

import java.net.URI
import java.time.LocalDate

/**
 * Provenance model for official-photo specifications.
 *
 * No authority-verified jurisdiction profiles are shipped by the initial MVP.
 * User-authored templates must never be represented as official requirements.
 */
object OfficialPhotoSpecEngine {
    enum class Provenance { AUTHORITY_VERIFIED, USER_DEFINED }

    data class PhotoSpec(
        val id: String,
        val displayName: String,
        val widthMm: Int,
        val heightMm: Int,
        val headHeightPercent: Int,
        val eyeLineFromTopPercent: Int,
        val provenance: Provenance,
        val issuingAuthority: String? = null,
        val sourceUrl: String? = null,
        val effectiveDateIso: String? = null,
        val verifiedDateIso: String? = null
    )

    data class Validation(
        val valid: Boolean,
        val issues: List<String>,
        val officialComplianceMayBeClaimed: Boolean
    )

    /**
     * Validates both structure and provenance. The asOf parameter makes effective-date
     * behavior deterministic in tests and prevents future-dated standards being presented
     * as currently effective.
     */
    fun validate(spec: PhotoSpec, asOf: LocalDate = LocalDate.now()): Validation {
        val issues = mutableListOf<String>()
        if (spec.id.isBlank()) issues += "Specification id is required"
        if (spec.displayName.isBlank()) issues += "Specification name is required"
        if (spec.widthMm !in 10..100) issues += "Width must be 10–100 mm"
        if (spec.heightMm !in 10..150) issues += "Height must be 10–150 mm"
        if (spec.headHeightPercent !in 40..90) issues += "Head height must be 40–90%"
        if (spec.eyeLineFromTopPercent !in 20..70) issues += "Eye-line must be 20–70%"

        var effectiveDate: LocalDate? = null
        if (spec.provenance == Provenance.AUTHORITY_VERIFIED) {
            if (spec.issuingAuthority.isNullOrBlank()) {
                issues += "Issuing authority is required"
            }
            val sourceIsPrimaryHttps = runCatching {
                val uri = URI(spec.sourceUrl ?: "")
                uri.scheme.equals("https", ignoreCase = true) &&
                    !uri.host.isNullOrBlank() &&
                    uri.userInfo == null
            }.getOrDefault(false)
            if (!sourceIsPrimaryHttps) {
                issues += "A valid HTTPS primary source is required"
            }

            effectiveDate = parseIsoDate(spec.effectiveDateIso)
            if (effectiveDate == null) issues += "A valid ISO effective date (YYYY-MM-DD) is required"
            val verifiedDate = parseIsoDate(spec.verifiedDateIso)
            if (verifiedDate == null) {
                issues += "A valid ISO verification date (YYYY-MM-DD) is required"
            } else if (verifiedDate.isAfter(asOf)) {
                issues += "Verification date cannot be in the future"
            }
            if (effectiveDate != null && effectiveDate.isAfter(asOf)) {
                issues += "Specification is not yet effective"
            }
        }

        val valid = issues.isEmpty()
        return Validation(
            valid = valid,
            issues = issues,
            officialComplianceMayBeClaimed = valid &&
                spec.provenance == Provenance.AUTHORITY_VERIFIED &&
                effectiveDate != null && !effectiveDate.isAfter(asOf)
        )
    }

    fun pixelsAt300Dpi(millimetres: Int): Int {
        require(millimetres in 10..150) { "Dimension must be 10–150 mm" }
        return (millimetres / 25.4f * 300f).toInt().coerceAtLeast(1)
    }

    private fun parseIsoDate(value: String?): LocalDate? =
        value?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
}
