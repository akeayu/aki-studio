# AKIDS-A3-CROP-NAMING-001 v0.1.0 — Interactive crop and user-controlled export naming

## Status

Implemented on the Owner-authorized A3 local-only Android branch. Source-level checks are recorded; Android build and device verification remain NOT RUN.

## Interactive crop

- Adds a dedicated crop-selection viewport with a dimmed outside region, rule-of-thirds guides and corner handles.
- Drag inside the frame to reposition it; drag a corner to resize while preserving the selected aspect ratio.
- Supports the existing ratio presets and original-image ratio.
- Crop geometry is stored as normalized image-space coordinates, independent of preview scale.
- Crop is applied non-destructively during rendering from the original source.
- Project manifests persist all four crop bounds and the aspect ratio.
- Project loading validates that crop bounds are complete, finite, normalized, and have positive area.
- No face detection or automatic subject placement is implied.

## User-controlled filenames

Both photo exports (JPEG/PNG) and A4 print-sheet PDFs now ask for a filename before saving.

- User can enter a one-time literal filename.
- User can enter a reusable naming pattern.
- Supported tokens: {type} and {date} (yyyyMMdd_HHmmss).
- Example: Studio_{type}_{date}.
- A checkbox lets the user save the entered pattern as the default for future exports.
- Pattern is stored locally in app SharedPreferences; it is not synchronized or uploaded.
- Unsafe path characters and control characters are replaced; whitespace is normalized; names are bounded to 80 characters; the correct extension is applied exactly once.
- Photo outputs remain in Pictures/Aki Studio; PDF outputs remain in Documents/Aki Studio.
- The original image is never overwritten.

## Verification

| Check | Status |
|---|---|
| Interactive crop view and touch handlers present | PASS (source inspection) |
| Crop rectangle normalized and applied from original | PASS (source inspection) |
| Crop bounds persisted and validated in projects | PASS (source inspection) |
| User filename prompt wired to JPEG and PNG | PASS (source inspection) |
| User filename prompt wired to print-sheet PDF | PASS (source inspection) |
| Reusable local pattern and tokens implemented | PASS (source inspection) |
| Filename sanitization and extension handling | PASS (source inspection) |
| Filename helper JVM tests authored | PASS (tests authored; not executed) |
| Android Gradle compilation | NOT RUN |
| JVM test execution | NOT RUN |
| Touch interaction / aspect-ratio device test | NOT RUN |
| MediaStore save and collision behavior on device | NOT RUN |
| Airplane-mode runtime test | NOT RUN |

## Known limitations

- Crop selector uses a fixed aspect ratio and corner resizing; freeform independent edge resizing, pinch zoom, and keyboard/accessibility fine adjustment are not included.
- Crop selector currently uses a bounded decoded image preview; final export is re-rendered from the full source, subject to the existing export edge cap.
- Naming pattern is shared across photo and PDF exports; {type} differentiates output class. No per-folder/per-format pattern registry or collision suffix policy has been device-validated.
- Android SharedPreferences storage is local to the app installation and is lost when app data is cleared or the app is uninstalled.
