# A3 Increment — manual mask and solid background replacement foundation

**Record:** AKIDS-A3-BG-MANUAL-001 v0.1.0  
**Stage:** A3 implementation  
**Runtime:** On-device Android only  
**Status:** SOURCE COMMITTED — BUILD/RUNTIME NOT VERIFIED

## Implemented foundation

- Manual brush strokes are recorded in normalized image coordinates, so the mask remains aligned when rendered at export dimensions.
- Eraser strokes remove previously painted protection regions.
- The mask renderer creates a bounded bitmap at the requested output dimensions.
- Solid background replacement consumes a foreground-protection mask and a chosen solid color.
- Original source remains unchanged; replacement returns a new bitmap.
- UI explicitly describes the feature as manual masking, not automatic segmentation.

## Important limitations

- No AI segmentation, subject detection, hair refinement, edge decontamination, feathering, or automatic mask generation.
- The user must paint the foreground protection mask accurately; unpainted areas are replaced.
- The initial implementation is a foundation and requires visual/runtime verification before being treated as production-ready.
- This increment does not add an automatic export flow until mask alignment and bitmap compositing are device-tested.

## Verification

Static source inspection only. Gradle build, unit tests, image corpus tests, low-memory profiling, and physical-device verification: NOT RUN.
