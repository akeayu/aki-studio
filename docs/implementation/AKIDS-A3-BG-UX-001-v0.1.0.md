# A3 Increment — manual mask refinement and export review

**Record:** AKIDS-A3-BG-UX-001 v0.1.0  
**Stage:** A3 implementation  
**Runtime:** Android local-only  
**Status:** SOURCE COMMITTED — BUILD/RUNTIME NOT VERIFIED

## Added

- Adjustable manual brush width (1–12% of the shorter image edge).
- Adjustable edge softness (0–4% of the shorter image edge), rendered with a bounded bitmap blur mask.
- Undo and redo for individual mask strokes.
- Redo history is discarded when a new stroke is drawn after undo.
- Mask strokes retain the brush/softness settings that were active when each stroke was created.
- Soft grayscale mask edges are composited as partial background opacity rather than thresholded to a hard edge.
- Before/after preview toggle; original comparison preview is separately decoded at a smaller 1,200 px maximum edge to reduce memory pressure.
- JPEG export now requires explicit confirmation and states that the original remains unchanged.

## Mask semantics

- White mask regions protect the source foreground.
- Black and cleared regions are replaced by the selected solid background.
- Gray edge pixels blend the replacement color with the original image.
- The mask remains user-painted; this is not automatic segmentation or a guarantee of accurate hair/edge extraction.

## Safety and resource boundaries

- No network, cloud, remote inference, account, telemetry or IROKA runtime dependency.
- The original source file is not overwritten.
- Export and mask rendering retain existing pixel bounds.
- Comparison preview uses a lower maximum edge than the working preview to limit additional memory use.

## Verification

Static source inspection only. Gradle build, unit tests, image-corpus evaluation, low-memory profiling, export validation and physical-device testing: NOT RUN. The soft-edge behavior and drawing blend modes require runtime visual verification.
