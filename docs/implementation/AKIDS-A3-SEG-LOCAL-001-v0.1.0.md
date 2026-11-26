# A3 Increment — local foreground estimation foundation

**Record:** AKIDS-A3-SEG-LOCAL-001 v0.1.0  
**Stage:** A3 implementation  
**Runtime boundary:** Local Android CPU only; no network or cloud dependency  
**Status:** SOURCE COMMITTED — BUILD/RUNTIME NOT VERIFIED

## Architecture

- `OnDeviceSegmentationModel` defines a replaceable contract for a future bundled on-device segmentation model. The contract has no network API.
- `LocalForegroundSegmentationEngine` prefers an injected local model if one is supplied and falls back locally if that model fails.
- `CpuBorderColorSegmentationProvider` is the current default. It is a deterministic border-colour heuristic, not a trained AI model.
- Analysis is downsampled to a maximum 512-pixel edge. It samples border colours, computes RGB distance per analysis pixel, creates a soft grayscale foreground-protection mask and scales the mask back to source dimensions.
- The heuristic returns low confidence (0.25) and explicit limitations.

## UI and workflow

- Added **Quick local background estimate** to the editor.
- The estimated mask is applied to a local solid background preview and is retained for export.
- Users can open manual mask editing to paint/erase corrections over the estimated mask.
- White mask areas protect the foreground; black areas are replaced; gray areas blend.
- Geometry-changing edits invalidate the estimate rather than silently reusing a misaligned mask.
- Source image remains unchanged. Mask and derived preview resources are released when replaced or when the activity closes.

## Known limitations

- This increment does **not** include a neural network or ML segmentation model.
- Border-colour heuristics fail when the background is textured, multi-coloured, similar to the subject, or when the subject touches the frame edge.
- Hair, translucent objects, fine jewellery, and similar-coloured clothing/skin may be classified incorrectly.
- The 512-pixel analysis resolution limits edge precision.
- The low confidence score is a caution indicator, not a statistically calibrated probability.
- Manual review and correction are required; no official-photo compliance claim is made.
- A future bundled model must be separately selected, licensed, measured on the approved image corpus, and tested on target devices before becoming the default.

## Resource and privacy constraints

- CPU-only bounded analysis; no GPU or network requirement.
- No remote model download, account, telemetry, cloud upload, or IROKA runtime.
- Analysis bitmap edge capped at 512 px; result mask is scaled to source size.
- Original input bitmap is not recycled or mutated by the segmentation provider.

## Verification

Static source inspection only. Gradle build, unit tests, mask alignment tests, image-corpus evaluation, low-memory profiling, airplane-mode runtime verification and physical-device tests: **NOT RUN**.
