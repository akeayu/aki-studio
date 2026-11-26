# AKIDS-A3-EXPORT-FORMATS-001 v0.1.0 — Local JPEG and PNG export

- Stage: A3 implementation
- Status: IMPLEMENTED IN SOURCE; Android runtime verification NOT RUN
- Runtime boundary: Android MediaStore local export; no network/cloud path

## Change

The Save Copy workflow now offers:
- JPEG at quality 94 for photographic output;
- PNG lossless encoding, preserving alpha where present in the edited bitmap.

Both formats use explicit file extensions and MIME types, save under `Pictures/Aki Studio`, use MediaStore's pending-publication flow, and delete the incomplete destination if encoding or writing fails. The selected source image is not overwritten. The export is still bounded by the existing 4,096-pixel maximum decode edge.

## Limitations

- No embedded, independently verified 300-DPI metadata is written.
- No ICC profile, EXIF preservation policy, or metadata scrub policy is implemented in this increment.
- PNG transparency is preserved only where the current edited bitmap contains alpha; current solid-background replacement produces an opaque result.
- Build and device export verification have not been run.
