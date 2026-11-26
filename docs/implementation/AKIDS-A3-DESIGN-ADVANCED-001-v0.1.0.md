# AKIDS-A3-DESIGN-ADVANCED-001 v0.1.0

**Status:** IMPLEMENTED IN SOURCE — VERIFICATION PENDING  
**Stage:** A3 local-only Android MVP  
**Owner boundary:** No network, cloud, telemetry, account dependency, merge, PR, deployment, release, or expenditure.

## Scope delivered in this increment

### Advanced typography

The editable layer schema now supports:
- Text alignment: left, center, right.
- System font faces: sans-serif, serif, monospace, condensed sans, medium sans.
- An explicit Ethiopic system fallback choice for mixed Amharic/English text.
- Line spacing multiplier from 0.8 to 2.0.
- Text effects: none, shadow, outline.
- Effect color and bounded effect width.
- Existing bold/italic and multiline text editing remain available.
- All typography attributes are serialized with the design layer document and therefore travel with the existing private project recipe and edit-history snapshot.

**Ethiopic font qualification:** The Ethiopic option uses Android's system sans-serif typeface and platform Unicode fallback. This allows a mixed-script text run to resolve glyphs through the device font fallback chain. It is not a bundled, licensed, independently verified Amharic font family. Glyph coverage, shaping, kerning, baseline and line wrapping can vary by Android version and device. A curated bundled font pack and physical-device visual verification remain outstanding.

### Editable vector paths

- Added bounded path grammar using normalized layer-local coordinates:
  - `M x y` — move.
  - `L x y` — line.
  - `Q cx cy x y` — quadratic Bézier.
  - `C c1x c1y c2x c2y x y` — cubic Bézier.
  - `Z` — close.
- Path input is bounded to 2,048 characters and 128 parsed commands.
- Coordinates must be finite and between 0 and 1.
- Custom path data is stored in the editable layer and rendered by Android Path on the CPU.
- Custom path geometry can be edited through layer properties.
- Existing rectangle, ellipse and line primitives remain available.
- Export remains rasterized through the existing bitmap compositor; this increment does not add SVG import/export or a node-handle visual path editor.

### Multi-selection and group transforms

- Added multi-select layer picker.
- Added bounded group translation, uniform scaling and rotation around the selected group's center.
- Locked layers reject group transformations.
- Transform validation rejects out-of-canvas results and invalid scale/rotation values.
- Group edits flow through the existing edit recipe/history mechanism.

### Object-to-object snapping

- Direct manipulation now snaps a moving layer center to nearby visible object centers and corresponding object edges.
- Existing canvas-center snapping remains as a fallback.
- Snap guides now render at the actual snap coordinate rather than always at the canvas center.

### Layer panel

- Added a layer panel with tap-to-edit and long-press drag-and-drop reorder.
- Reordering changes z-order and triggers preview/history refresh.
- Existing visibility, lock, delete and stepwise move actions remain available.

### Persistence and regression coverage

- Design layer schema advanced from v2 to v3.
- Decoder remains backward-compatible with schema v1 and v2; legacy records receive defaults for new typography/path properties.
- Existing project manifest stores the serialized design-layer document in the private local project recipe.
- Existing edit history snapshots store the same serialized layer document.
- Added regression tests for typography and custom path round-trip, path command parsing/validation, reorder, group transforms, lock protection, object snapping, and design-layer project-recipe undo/redo restoration.

## Technical limits and unresolved verification

- No bundled Amharic font binary or font licensing record has been added.
- No visual font specimen/corpus has been tested on actual Ethiopian Android devices.
- Text shaping and complex script layout still depend on Android's platform text renderer.
- Path input is command-string based; there is no on-canvas Bézier node editor, SVG import/export, gradients, boolean path operations or path boolean editing.
- Group transforms are numeric dialog operations, not multi-touch group gestures.
- Object snapping uses center/edge candidates and a fixed normalized tolerance; no configurable grid, baseline, equal-spacing or guide manager.
- Drag-and-drop layer reorder requires Android runtime interaction testing.
- Project persistence is covered at the serialized recipe/history level by authored regression tests; a full app-private file save/reopen instrumentation test remains outstanding.
- Gradle build, JUnit execution, APK generation, emulator/device tests, API 29 compatibility, offline runtime, accessibility and memory/performance tests are NOT RUN in this record.

## Acceptance evidence

| Check | Result |
|---|---|
| Schema v3 source and v1/v2 decode branches | SOURCE IMPLEMENTED |
| Typography controls and renderer wiring | SOURCE IMPLEMENTED |
| Ethiopic fallback path | SOURCE IMPLEMENTED; DEVICE COVERAGE UNKNOWN |
| Custom path parser and renderer | SOURCE IMPLEMENTED |
| Group transform and object snapping operations | SOURCE IMPLEMENTED |
| Multi-select and drag-reorder UI wiring | SOURCE IMPLEMENTED |
| Project/history regression tests | AUTHORED — NOT RUN |
| Gradle build | NOT RUN |
| JUnit suite | NOT RUN |
| Physical-device verification | NOT RUN |

## Next verification sequence

1. Run the complete Gradle unit-test suite and resolve compilation/test failures.
2. Build debug APK with the documented Android SDK/JDK toolchain.
3. Test Amharic-only, English-only and mixed-script strings across system font choices and Android API 29+ devices.
4. Exercise text effects, long lines, multiline wrapping and export rasterization.
5. Test custom path parser edge cases and visual path output.
6. Test group transforms, object snapping, drag reorder, locked-layer handling and history.
7. Save, close, reopen and re-edit projects containing styled text and custom paths.
8. Verify airplane-mode operation and bounded memory/performance on physical devices.

This record documents implementation source, not a verified build or production-ready release.
