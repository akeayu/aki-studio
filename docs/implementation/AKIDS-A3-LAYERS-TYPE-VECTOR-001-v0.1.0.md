# AKIDS-A3-LAYERS-TYPE-VECTOR-001 v0.1.0

**Stage:** A3 local-only implementation  
**Status:** Implemented in source; runtime verification pending  
**Scope:** Layer document foundation, typography and vector primitives

## Architecture
- One versioned `DesignLayerDocument` stores up to 30 editable objects in normalized canvas coordinates.
- Supported objects: text, rectangle, ellipse and line. Each stores identity, name, z-order (list order), geometry, fill/stroke colors and width, font scale, bold/italic styling, visibility, lock and opacity.
- `DesignLayerRenderer` composites visible objects into a fresh ARGB bitmap on the CPU. It does not mutate the source image.
- Layer data is serialized as a bounded, versioned Base64 binary payload in the existing private project manifest. Existing schema-v1 projects without the optional field load with an empty design.
- Layer mutations participate in the existing recipe undo/redo snapshots.

## Editing capabilities
- Add text with editable content, multiline layout, font scale, color, bold and italic.
- Add vector rectangle, ellipse and line objects with editable fill/stroke colors and stroke width.
- Manage layers: select, edit text, toggle visibility, lock/unlock, raise/lower z-order, and delete.
- Adjust object position and size using normalized coordinates and bounded controls.
- Export compositing uses the same renderer as the preview.

## Limits
- Vector primitives are resolution-independent in the document, but export rasterizes them into the output bitmap.
- No Bézier paths, SVG import, gradients, text shaping controls, font library, kerning, rotation handles, multi-select, snapping, or arbitrary path editing in this increment.
- Font rendering uses Android system sans-serif; Amharic glyph support depends on device fonts.
- Maximum 30 objects, 500 characters per text object, 8 rendered lines.
- The renderer is CPU-based and synchronous with the existing preview/export pipeline.

## Privacy and compatibility
- No network, cloud, account, telemetry, remote model, or IROKA runtime dependency.
- Existing photo originals remain unchanged.
- Existing local projects without design-layer data remain compatible.
- Project schema version is not bumped; the new field is optional and bounded.

## Verification
- Unit tests authored for text/shape round-trip serialization, legacy empty documents, layer-count limit, coordinate validation, duplicate IDs, and undo/redo restoration of layer recipes.
- Static source checks may be performed through repository readback.
- Gradle/JUnit, Android device rendering, Amharic font coverage, memory/latency, export and airplane-mode runtime tests remain pending until executed.
