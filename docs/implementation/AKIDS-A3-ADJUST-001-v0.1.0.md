# AKIDS-A3-ADJUST-001 v0.1.0 — Local tonal adjustments

## Status

Implemented in the A3 local-only Android branch. Source-level checks are recorded below; Android compilation and runtime verification have not been performed.

## Scope

Adds deterministic, CPU-only brightness, contrast, saturation and warmth controls. The UI accepts bounded integer values:

| Control | Range | Neutral |
|---|---:|---:|
| Brightness | −100 to 100 | 0 |
| Contrast | −100 to 100 | 0 |
| Saturation | 0 to 200 | 100 |
| Warmth | −100 to 100 | 0 |

The values are stored as an edit recipe in the existing private project manifest and restored when a project is reopened. The renderer starts from the original image for every preview/export, so repeated slider-dialog changes do not accumulate destructive pixel edits.

## Processing characteristics

- Uses Android software bitmaps and a single scanline buffer.
- Preserves the source alpha channel.
- Returns a new bitmap; never mutates the input bitmap.
- Applies contrast and brightness, then a simple luma-based saturation adjustment and red/blue warmth offset.
- Runs locally; no network, cloud, account, telemetry, or IROKA dependency.
- No third-party runtime dependency is introduced.

## Limitations

- These are basic global tonal controls, not a colour-managed RAW workflow.
- Warmth is an approximate RGB channel shift, not calibrated white-balance temperature.
- No exposure compensation in EV, curves, levels, selective masks, HSL channels, sharpening, or noise reduction is included in this increment.
- No histogram, clipping warning, or ICC-profile-aware transform is provided.
- UI input is numeric rather than continuous slider interaction.
- The project schema remains version 1; new values are optional and older projects default to neutral settings.
- Large-image allocation, visual quality, alpha handling and responsiveness require device validation.

## Source-level acceptance checks

- [x] All four controls have explicit input bounds.
- [x] Engine validates parameters independently of the UI.
- [x] Engine returns a new bitmap and does not write to the source.
- [x] Pixel processing uses a per-row buffer rather than a full-frame temporary array.
- [x] Source alpha is preserved.
- [x] Adjustment recipe is saved and restored with local projects.
- [x] Reset returns all four values to neutral.
- [x] The existing local-only runtime boundary is unchanged.

## Added pixel-math unit coverage

The colour transform is factored into an internal pure pixel function so its arithmetic can be tested without Android bitmap runtime calls. `ImageAdjustmentEngineTest.kt` covers neutral identity, brightness direction, grayscale saturation, warm/cool channel balance, alpha preservation, and byte-channel clamping. These tests are authored but have not been executed; they do not verify Bitmap allocation, row processing, Android rendering, or device performance.

## Verification boundary

| Verification | Status |
|---|---|
| Source integration and static inspection | PASS (manual source review) |
| Kotlin/Android Gradle compilation | NOT RUN |
| Tonal pixel-math unit tests | AUTHORED; NOT RUN |
| Emulator / physical-device rendering | NOT RUN |
| Large-image memory and latency profiling | NOT RUN |
| Airplane-mode runtime test | NOT RUN |
