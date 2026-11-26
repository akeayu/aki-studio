# A3 implementation record — editable inspector and adaptive workspace shell

**Record:** AKIDS-A3-INSPECTOR-ADAPTIVE-FOLD-001  
**Version:** 0.1.0  
**Stage:** A3 implementation  
**Runtime boundary:** Local-only Android; no account, cloud, remote inference, telemetry, or upload.

## Delivered

### 1. Editable layer inspector and property editor
- The inspector exposes direct actions for editing the selected layer and managing layers.
- Selected-layer editing now supports normalized center X/Y, width, height, rotation (-180° to 180°), opacity (0–100%), visibility and lock state.
- A locked layer can still be unlocked through the property editor; direct canvas manipulation remains blocked while locked.
- Changes are validated through the existing `DesignLayer.validated()` contract before replacing the layer.
- Text-layer controls include font size, weight, italic, alignment, font family, line spacing, text effect, effect width and effect color.
- Vector-layer controls include primitive type, stroke width, fill and stroke colors, and editable custom path data.
- Existing schema-v3 serialization carries the updated properties; no schema bump was required.

### 2. Color picker and reusable style presets
- Added a platform-only color picker with a built-in palette and custom hex input.
- Fill/text, vector stroke and text-effect colors can be selected independently.
- Added built-in Aki Mint, Editorial, Bold Poster and Minimal styles.
- Users can save up to 12 named custom style presets in app-private SharedPreferences.
- Presets apply appearance properties while preserving layer identity, text/path content, geometry and rotation.
- Presets are device-local and are not synchronized or backed up.
- To save a custom style, save the desired layer properties first, reopen its editor, then choose **Save style preset**. The saved preset captures the layer's currently persisted style.

### 3. Responsive navigation and compact controls
- Expanded windows use a persistent vertical workspace navigation rail.
- Compact and medium windows retain horizontal workspace navigation.
- Expanded windows retain the contextual command strip and canvas/inspector split.
- Compact windows add floating canvas actions (Select, Compare, Layers) and a fixed bottom command bar (Undo, Redo, Compare, Export).
- All controls invoke existing application actions; no mock controls were introduced.
- Workspace switching remains in-place and does not replace the Activity or discard the current editing session.

### 4. Foldable posture and hinge-aware layout
- Added AndroidX WindowManager's Java adapter to observe window layout information and folding features.
- Separating vertical hinges are treated as a pane boundary: the navigation rail is sized against the leading pane and a hinge-width spacer is reserved before the content pane.
- Separating horizontal hinges reserve the upper hinge region before scrollable workspace content.
- Layout changes trigger a screen rebuild while retaining the in-memory editing session.
- If fold information is unavailable, the app falls back to the existing compact/medium/expanded window-size profile.
- The dependency is used only for local window-layout observation; it does not add a network runtime or require an Internet permission.

## Verification

| Check | Result |
|---|---|
| Source structure and single-package integrity | PASS (static inspection) |
| Editable transform fields and bounded validation | PASS (static inspection) |
| Layer-specific text/vector controls | PASS (static inspection) |
| Local color picker and preset storage paths | PASS (static inspection) |
| Responsive rail / compact floating controls / bottom bar | PASS (static inspection) |
| Fold feature observation and separating-hinge branches | PASS (static inspection) |
| Preset application regression test | AUTHORED; NOT RUN |
| Android Gradle compile and unit tests | NOT RUN |
| Emulator rendering and API-level compatibility | NOT RUN |
| Physical foldable posture / hinge validation | NOT RUN |
| Touch, accessibility, font scaling and rotation QA | NOT RUN |
| Airplane-mode runtime verification | NOT RUN |

## Known limitations and follow-up gates

- The color picker provides a curated palette and hex entry; it is not yet a full hue/saturation/value or alpha-channel picker.
- Custom presets store visual style, not position, dimensions, layer names or text/path content.
- The preset editor captures the last saved layer style; save layer properties before creating a preset.
- Vertical-hinge sizing is based on the reported folding-feature bounds and current window density. Real device-specific posture testing is required.
- Horizontal-hinge handling reserves the reported upper hinge region; it does not yet create independently scrollable upper/lower editing workspaces.
- No multi-window drag-across-hinge or tabletop-specific control relocation has been verified.
- AndroidX WindowManager dependency resolution and binary compatibility are not yet verified by a successful build.
- A4 verification remains separate. This record does not claim A4 acceptance or production readiness.

## Scope and governance

This increment is within the Owner-authorized local-only A3 Android MVP. No merge, PR, deployment, release, purchase, paid service, or expenditure was performed.
