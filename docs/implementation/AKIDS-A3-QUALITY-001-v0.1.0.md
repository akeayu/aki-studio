# A3 Increment — local photo-quality assessment

**Record:** AKIDS-A3-QUALITY-001 v0.1.0  
**Stage:** A3 implementation  
**Runtime boundary:** Local Android CPU; no network, cloud, telemetry or remote model calls  
**Status:** SOURCE COMMITTED — BUILD/RUNTIME NOT VERIFIED

## Implemented indicators

- Image pixel dimensions and aspect ratio.
- Sampled Laplacian variance as a sharpness/possible-blur indicator.
- Near-black/near-white sample percentage as a clipped-shadow/highlight indicator.
- Global luminance standard deviation as a contrast indicator.
- Border grayscale variation as a weak background-uniformity screening indicator.
- Explicit warnings and caveats in the Amharic/English UI.

## Method and safety boundaries

- Analysis uses a bounded grid no larger than 128 × 128 samples from the current edited preview bitmap.
- No source image is uploaded or modified; the temporary assessment bitmap is recycled after use.
- Thresholds are heuristic and not calibrated against a camera, printer, or government specification.
- Border variation is not background segmentation and cannot identify foreground subjects.
- No face detection, biometric processing, automatic background removal, official-photo compliance check, or pass/fail certification is performed.
- Users must inspect the photo and consult the relevant issuing authority's current rules.

## Verification

- Static source review: indicator calculations and bounded sampling present.
- Build, unit tests, memory profiling, physical-device behavior and image-corpus validation: NOT RUN.
- CI status evidence: not established.
