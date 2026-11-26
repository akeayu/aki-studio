# Aki Studio Android application

## Local-only implementation

The app is a native Kotlin Android application. It has no third-party runtime dependencies and its manifest deliberately omits the INTERNET permission.

### Toolchain

- JDK 17
- Android SDK Platform 36
- Gradle 8.11.1
- Android Gradle Plugin 8.9.1 (minimum supported for API 36)
- Kotlin Android plugin 2.1.20
- Minimum supported Android: API 29

Open the repository root in a current Android Studio installation, allow the local Gradle/Android SDK toolchain to sync, and run the `:app` configuration on an emulator or physical Android device. No backend, account, cloud project, API key, or runtime network connection is required.

### Current MVP

- Choose a photo through Android's system document picker.
- Rotate by 90 degrees.
- Center-crop to original, square, 3:4, 4:5, 5:4, or 16:9 proportions.
- Resize within maximum width/height pixel bounds while preserving the current aspect ratio; no upscaling.
- Apply ID/passport crop presets: 35 × 45 mm (413 × 531 px) and 2 × 2 inch (600 × 600 px), with pixel dimensions calculated for 300 DPI.
- Apply or remove a monochrome effect.
- Reset edits.
- Save a new JPEG copy under Pictures/Aki Studio.

Cropping is currently center-anchored; draggable crop positioning and custom dimensions are not implemented. The presets set output pixel dimensions, but the JPEG does not yet receive verified 300-DPI resolution metadata. They are dimension presets only, not confirmation that a particular country's issuing authority accepts the crop. The original selected file is not overwritten. Preview decoding is bounded to 1,800 px on the longest edge; export decoding is bounded to 4,096 px before preset scaling. This first vertical slice is not yet a full-resolution editor.

### Local processing guarantees and limitations

The current operations use Android software bitmaps and run on the device CPU. There is no cloud fallback. If a file cannot be decoded within the memory budget, the app must report failure rather than upload it. The app does not currently implement full-resolution project history, interactive crop positioning, custom ID templates, camera capture, background segmentation, print-sheet composition, or PDF export.

### Verification

No successful Gradle build, APK, emulator install, or physical-device test is claimed yet. Record those results under the A4 verification gate when the local Android toolchain and devices are available.
