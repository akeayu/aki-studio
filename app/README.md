# Aki Studio Android application

## Runtime boundary

Aki Studio is a native Kotlin Android application. Its manifest does not request the INTERNET permission. Photo selection, editing, quality heuristics, mask estimation, rendering and export are designed to run on the device. There is no login, cloud sync, remote inference, telemetry, or IROKA runtime dependency.

The app uses Android platform APIs and an optional LiteRT CPU interpreter dependency for a future bundled segmentation model. The expected model asset is currently absent, so the active automatic-estimation path is the deterministic CPU border-colour heuristic. The app falls back locally; it does not fetch a model or contact a service.

## Toolchain

- JDK 17
- Android SDK Platform 36
- Gradle 8.11.1
- Android Gradle Plugin 8.9.1
- Kotlin Android plugin 2.1.20
- Minimum supported Android: API 29
- LiteRT CPU interpreter dependency: 2.4.0

Open the repository root in Android Studio, sync the local Gradle/Android SDK toolchain, and run the `:app` configuration on an emulator or physical Android device. Build tooling may need locally available Gradle/SDK/plugin artifacts. The app runtime itself does not require a network connection.

## Implemented A3 source features

### Photo editing and export
- Save/reopen private offline editable projects with a private original-image copy and edit recipe.
- Duplicate, rename and delete saved projects; deletion requires confirmation and never removes the external original.
- Project source files are capped at 100 MB; persisted dimensions, crop settings and mask state are validated when opening.
- Current flattened foreground mask is retained; per-stroke mask history is not yet persisted.
- Choose an image using Android's system document picker.
- Bounded software-bitmap decoding for preview and export.
- Rotate by 90-degree increments.
- Crop to original, 1:1, 3:4, 4:5, 5:4, or 16:9 aspect ratios.
- Choose crop alignment from a nine-position grid.
- Fine crop with a draggable frame and corner handles; crop bounds are normalized, non-destructive and saved in editable projects.
- Resize within maximum width/height pixel bounds while preserving aspect ratio and avoiding upscaling.
- Apply or remove monochrome effect.
- Adjust brightness, contrast, saturation and warmth with bounded CPU-only processing; values are stored in the project edit recipe and re-rendered from the original.
- Reset edits while preserving the source.
- Save a new JPEG copy (quality 94) or lossless PNG under `Pictures/Aki Studio`; PNG retains alpha when present in the edited bitmap.
- Name each JPEG/PNG export and A4 PDF print sheet yourself, or save a reusable local filename pattern using the `{type}` and `{date}` tokens.

### ID/passport preparation
- Dimension presets: 35 × 45 mm (413 × 531 px) and 2 × 2 inch (600 × 600 px), calculated for 300 DPI.
- Configurable manual head-height and eye-line overlays.
- Manual visual alignment only; no face detection or biometric measurement.
- Jurisdiction-aware specification validation distinguishes authority-verified records from user-defined/unverified templates; authority claims require HTTPS provenance and valid effective/verification dates, and future-dated records fail closed.
- JVM tests are authored for provenance, date, and dimension rules but have not yet been executed.
- No government authority source has yet been independently verified and added; the validator does not itself certify a template's legal authenticity.

### Photo quality
- Bounded CPU sampling for dimensions, aspect ratio, sharpness proxy, exposure extremes, luminance variation and border variation.
- Explainable heuristic warnings.
- Thresholds are not calibrated against a representative photo corpus and must not be treated as a pass/fail certification.

### Background editing
- Manual foreground-protection painting and erasing.
- Adjustable brush width and edge softness.
- Undo/redo for mask strokes.
- Solid background replacement choices and before/after preview.
- Optional quick local mask estimate.
- Asynchronous single-worker execution; result IDs prevent stale analysis from being applied after image changes.
- Active automatic provider is a deterministic CPU border-colour heuristic. It is not trained AI and can fail on textured, multicolour or subject-similar backgrounds.
- Optional LiteRT adapter source exists, but `app/src/main/assets/selfie_segmenter.tflite` is absent. The neural model is therefore not active or included in the app.

### Print output
- Local A4 PDF generation.
- Configurable 1–5 columns and 1–7 rows.
- Repeats the current edited image with basic borders and cut marks.
- PDF is saved under `Documents/Aki Studio`.

### Direct manipulation
- Select the topmost visible layer on the preview.
- Drag inside the selection to move it; drag any corner handle to resize; use the top handle to rotate.
- Position snaps to nearby visible object centers/edges, with canvas-center fallback; rotation snaps to 15-degree increments.
- Rotated-layer corner resizing uses inverse-rotated local deltas and anchors the opposite corner.
- A completed gesture creates one edit-history step.
- Layer schema v3 persists typography, path geometry and transforms; decodes schema v1 and v2 payloads.

### Layer-based Design Studio
- A versioned, non-destructive design document supports up to 30 editable layers.
- Typography supports editable Amharic/English mixed-script text, left/center/right alignment, line spacing, bold/italic, system sans/serif/monospace/condensed/medium faces, and shadow/outline effects.
- Ethiopic text uses Android's system sans-serif and Unicode fallback; this is not a bundled or independently verified Amharic font family. Device font coverage and shaping still require physical-device testing.
- Paragraph rendering uses Android StaticLayout for mixed-script shaping, line breaking, alignment and bidi text direction.
- Vector primitives include rectangles, ellipses and lines; custom paths support M/L/Q/C/Z commands with bounded normalized coordinates.
- Custom path geometry is editable through layer properties and persisted in private projects.
- Multi-selection supports group translation, uniform scaling and rotation around the selected group's visual bounds; locked layers are protected.
- Layer management supports visibility, locking, normalized position/size, drag-and-drop z-order, and deletion.
- Object-to-object snapping aligns moving layers to visible peer centers and edges.
- Design layers are stored with private local projects and participate in the existing 100-step recipe history.
- A shared CPU renderer composites layers into the preview/export bitmap without mutating the original source.
- Text/vector objects are rasterized at export; SVG import/export, gradients, bundled font libraries, kerning controls, on-canvas Bézier node editing, configurable snapping guides and multi-touch group gestures are not implemented yet.

### Studio interface
- Reusable native-view design system with a dark cinematic workspace shell, restrained mint/cyan accents, rounded surfaces, status badges and ripple-enabled action tiles.
- Main screen groups actions into Workspace, Image Lab, Portrait & ID, Background Lab and Print Studio rather than presenting a long flat button list.
- Shared page, header, badge, panel, two-column action-grid, action-tile, preview-stage and status components are defined in AkiStudioUi.kt.
- Amharic-first labels and English toggle are preserved; tool tiles include descriptive text and accessibility content descriptions.
- No third-party UI dependency or network asset is introduced.

### Localization
- Amharic-first UI with English toggle.

## Known limitations

- No actual Gradle build, APK, emulator installation, or physical-device test has been recorded.
- No bundled Ethiopic font file or license record has been added; Ethiopic glyph fallback depends on the Android device's installed fonts.
- Per-stroke mask history, autosave/recovery journal, camera capture, batch processing, and advanced portrait retouching are not implemented yet.
- Tonal adjustment controls are global RGB approximations; no calibrated white balance, colour management, curves, selective adjustment, sharpening, or noise reduction yet.
- Background segmentation currently depends on a low-confidence border-colour heuristic; manual inspection and correction are required.
- The optional LiteRT model binary is not present and has not been checksum-verified or licensed into this repository.
- Current LiteRT adapter stretches non-square images to its square tensor input; aspect-ratio-aware preprocessing requires further evaluation.
- JPEG/PNG exports do not yet embed independently verified 300-DPI metadata or implement ICC/EXIF preservation and metadata-scrubbing policy.
- A4 print output is single-page, has no printer calibration, bleed configuration, or jurisdiction-specific cut specifications.
- No national ID/passport standard has been independently verified and marked authority-verified.
- Photo-quality thresholds and segmentation quality have not been validated on a representative, consented image corpus.

## Continuous integration

- `.github/workflows/android-verify.yml` runs JVM unit tests and assembles a debug APK on pushes to the A3 feature branch.
- A separate API 29 Android emulator job runs the app-private project save/reopen instrumentation test.
- The workflow uploads the debug APK as a build artifact.
- This CI is build/test infrastructure only; the installed app remains local-only and does not add a network runtime dependency.
- Workflow execution status must be checked from GitHub Actions; source creation alone is not proof that a run completed.

## Verification status

| Verification | Status |
|---|---|
| Static source review | Partial; individual source checks only |
| Synthetic segmentation harness | Reference algorithm checks passed; not Kotlin runtime evidence |
| Gradle dependency resolution/build | NOT RUN |
| Unit tests | Authored for photo-spec, tonal pixel math, design-layer schema, typography, path parsing, group transforms, snapping, ordering and project-recipe history; NOT RUN |
| Instrumentation tests | Project save/reopen regression authored; NOT RUN |
| Emulator/device test | NOT RUN |
| Airplane-mode runtime test | NOT RUN |
| Memory and latency profiling | NOT RUN |
| A4 verification gate | Not opened |
| Deployment/release | Not authorized |

Record implementation changes under `docs/implementation/` and verification evidence under `docs/verification/`. Do not describe a feature as verified until the relevant build and runtime tests have actually run.
