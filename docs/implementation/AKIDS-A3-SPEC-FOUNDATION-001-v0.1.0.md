# A3 Increment — Jurisdiction-aware photo specification foundation

**Record:** AKIDS-A3-SPEC-FOUNDATION-001 v0.1.0  
**Stage:** A3 implementation  
**Branch:** feat/a3-local-android-mvp  
**Status:** IMPLEMENTED IN SOURCE — BUILD/RUNTIME NOT VERIFIED

## Purpose

Separate user-configurable photo templates from requirements verified against an issuing authority. The app must not imply official compliance merely because dimensions or guide percentages have been entered.

## Implemented

- Added `OfficialPhotoSpecEngine` with explicit `AUTHORITY_VERIFIED` and `USER_DEFINED` provenance.
- Validation requires a name, bounded physical dimensions, head-height and eye-line ranges.
- Authority-verified provenance additionally requires an issuing authority, HTTPS primary source, effective date and verification date.
- The validation result permits an official-compliance claim only when all validation rules pass and provenance is authority-verified.
- Added an Amharic/English UI workflow for user-defined templates.
- User-defined templates apply crop dimensions and head/eye guide values but are visibly marked `USER-DEFINED / UNVERIFIED`.
- Nominal pixel dimensions are calculated at 300 DPI; this is a pixel-count calculation and does not assert embedded JPEG DPI metadata.

## Not implemented / not claimed

- No country or issuing-authority profile is bundled as verified.
- No official passport, national ID, visa or residence-permit requirements are asserted.
- No web access, remote standards lookup or cloud dependency.
- No automated face detection, head measurement, eye-line detection or compliance certification.
- Authority verification still requires a primary source review and controlled profile record with version/effective date.

## Acceptance checks performed

- Source-level inspection confirms explicit provenance distinction.
- Source-level inspection confirms authority provenance requires issuer, HTTPS source, effective date and verification date.
- Source-level inspection confirms user-defined UI labels are unverified.
- Build, unit tests, device behavior and output metadata: NOT RUN / UNKNOWN.
