# Aki Studio — A3 Local Android MVP Implementation Record

**Record ID:** AKIDS-A3-IMPL-LOCAL-001  
**Version:** 0.1.0  
**Status:** IMPLEMENTATION IN PROGRESS — NOT VERIFIED  
**Owner:** Akefa Ayele Daka  
**Authorization:** Owner instruction, 26 November 2026  
**Repository:** akeayu/aki-studio  
**Branch:** feat/a3-local-android-mvp

## 1. Scope authorization

The Owner directed: “No need of network or no need for cloud computing just local processor and start implementation.”

This record interprets that instruction as authorization to begin A3 for a local-only Android MVP. The implementation shall not depend on an internet connection, remote API, cloud inference, remote storage, user account, analytics or telemetry. All image processing in this scope executes on the Android device using CPU software bitmap operations.

This does not authorize merging, release, deployment, purchasing hardware/services, paid dependencies or other expenditure.

## 2. Implemented vertical slice

The branch introduces:

- Native Android application module using Kotlin and Android platform APIs.
- Android 10 / API 29 minimum and API 36 target configuration.
- No third-party runtime libraries.
- No INTERNET permission in the manifest.
- System document picker for explicit image selection.
- Bounded software bitmap decode for preview.
- 90-degree rotation, monochrome conversion and reset operations.
- Original source remains unchanged; edits are applied to in-memory derivatives.
- Save-copy operation to Pictures/Aki Studio using MediaStore.
- Amharic-first visible controls with an English language toggle.
- Bounded memory-oriented preview/export decode dimensions.

## 3. Explicit limitations

This is not the complete Aki Studio product. It does not yet implement full-resolution non-destructive edit documents, crop/resize controls, ID/passport presets, camera capture, background segmentation, portrait retouching, layers, frames, print-sheet composition, PDF export, project persistence, undo history, accessibility audit or image-quality evaluation.

Preview decode is bounded to 1,800 pixels on the longest edge. Saved output is bounded to 4,096 pixels on the longest edge. The output is a new JPEG copy; the original is not overwritten. The current implementation has not been built or tested on a physical Android device in this work record.

## 4. Runtime privacy boundary

- No INTERNET permission.
- No network client or cloud endpoint.
- No authentication/account requirement.
- No automatic upload or synchronization.
- No analytics or telemetry.
- User explicitly chooses an image through Android's document picker.
- Export writes a local media item.
- Android OS/provider behavior is outside the app's direct control; no app-initiated network transfer is implemented.

## 5. Verification plan (not yet executed)

1. Build with a local Android SDK and JDK 17.
2. Inspect merged manifest and verify no INTERNET permission.
3. Install on API 29 and API 36 devices/emulators.
4. Test image selection, rotation, monochrome, reset, and save-copy.
5. Test large image and low-memory behavior on a 3 GB physical-RAM device.
6. Verify original source digest remains unchanged.
7. Verify exported JPEG dimensions, orientation, decoding and metadata behavior.
8. Run core workflow in airplane mode and inspect app-initiated network traffic.
9. Test Amharic glyph rendering and screen-reader labels.
10. Record exact build toolchain, APK hash, device SKU, OS build and test evidence.

## 6. Stage status

- A1 v1.0: APPROVED / VERIFIED-CLOSED.
- A2-ADR-001: APPROVED.
- A2 technical reconciliation: PARTIAL / IN PROGRESS.
- A3 local Android MVP: IMPLEMENTATION IN PROGRESS.
- A4: NOT STARTED; no build or device evidence yet.
- A5: NOT AUTHORIZED.
- Merge to main: NOT AUTHORIZED.
- Expenditure: NOT AUTHORIZED.

**End of AKIDS-A3-IMPL-LOCAL-001 v0.1.0.**
