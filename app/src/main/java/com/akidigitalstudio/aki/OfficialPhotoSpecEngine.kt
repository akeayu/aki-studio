package com.akidigitalstudio.aki

/**
 * Provenance model for official-photo specifications.
 *
 * The initial local MVP ships with no authority-verified jurisdiction profiles.
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

    fun validate(spec: PhotoSpec): Validation {
        val issues = mutableListOf<String>()
        if (spec.id.isBlank()) issues += "Specification id is required"
        if (spec.displayName.isBlank()) issues += "Specification name is required"
        if (spec.widthMm !in 10..100) issues += "Width must be 10–100 mm"
        if (spec.heightMm !in 10..150) issues += "Height must be 10–150 mm"
        if (spec.headHeightPercent !in 40..90) issues += "Head height must be 40–90%"
        if (spec.eyeLineFromTopPercent !in 20..70) issues += "Eye-line must be 20–70%"
        if (spec.provenance == Provenance.AUTHORITY_VERIFIED) {
            if (spec.issuingAuthority.isNullOrBlank()) issues += "Issuing authority is required"
            if (spec.sourceUrl.isNullOrBlank() || !spec.sourceUrl.startsWith("https://")) {
                issues += "An HTTPS primary source is required"
            }
            if (spec.effectiveDateIso.isNullOrBlank()) issues += "Effective date is required"
            if (spec.verifiedDateIso.isNullOrBlank()) issues += "Verification date is required"
        }
        val valid = issues.isEmpty()
        return Validation(
            valid = valid,
            issues = issues,
            officialComplianceMayBeClaimed = valid && spec.provenance == Provenance.AUTHORITY_VERIFIED
        )
    }

    fun pixelsAt300Dpi(millimetres: Int): Int {
        require(millimetres in 10..150) { "Dimension must be 10–150 mm" }
        return (millimetres / 25.4f * 300f).toInt().coerceAtLeast(1)
    }
}
