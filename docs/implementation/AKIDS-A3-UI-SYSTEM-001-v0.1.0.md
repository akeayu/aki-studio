# AKIDS-A3-UI-SYSTEM-001 v0.1.0 — Reusable creative studio interface

## Status

Implemented in the A3 local-only Android branch. This record describes source-level UI implementation. Android compilation, accessibility inspection, layout testing and device rendering remain NOT RUN.

## Design intent

Aki Studio is presented as one coherent creative workspace rather than a long list of unrelated utility buttons. The interface uses a dark, high-contrast studio shell with restrained mint/cyan accents, rounded layered surfaces, compact status badges and grouped task panels.

The visual system is intended to feel contemporary and technically capable without requiring GPU effects, remote assets, third-party UI libraries or a high-end device. The implementation uses Android platform Views and drawable primitives.

## Reusable component system

AkiStudioUi.kt defines:

- page(context) — shared studio page surface, spacing and base colour.
- header(context, language, action) — product identity, tagline, language action and local/private workspace indicators.
- badge(context, label, colour) — compact semantic status/metadata label.
- panel(context, title, subtitle, actions, language) — reusable workspace section with a tool count.
- actionGrid(context, actions) — responsive two-column action layout.
- actionTile(context, action) — accessible, focusable, ripple-enabled tool tile with symbol, title and explanatory subtitle.
- previewContainer(context) — rounded preview-stage surface.
- statusText(context) — reusable session status surface.
- spacer(context, height) — shared layout rhythm.

The action model carries a symbol, localized title, localized explanatory subtitle and a real callback. Tool tiles are not inert illustrations.

## Current screen composition

The main screen groups existing capabilities into:

1. Workspace — open photo, save/open project, export, undo and redo.
2. Image Lab — rotate, crop, fine crop, resize, tonal adjustments, monochrome, before/after and reset.
3. Portrait & ID — ID/passport presets, photo guides, specification validation and quality assessment.
4. Background Lab — local estimate, manual mask and mask controls.
5. Print Studio — A4 print-sheet PDF.

The image preview remains central. Existing editing operations and local-only processing boundaries are preserved.

## Interaction and accessibility principles

- Touch targets are 96 dp high for tool tiles, above the Android 48 dp minimum target guideline.
- Tiles are keyboard-focusable and expose a content description combining title and purpose.
- Ripple feedback communicates activation.
- Labels and explanatory subtitles reduce reliance on symbols alone.
- Amharic remains the default language; English remains selectable.
- The UI does not claim that heuristic background estimates or photo-quality scores are authoritative AI or official compliance results.
- Use of colour is paired with text labels; colour is not the only carrier of state.
- The layout uses scrollable content to accommodate small screens. Tool tiles use a vertical icon/title hierarchy to retain readable text on narrow phones; the language switch has a 48 dp minimum height.

## Runtime and dependency constraints

- No new runtime dependency.
- No network, cloud, account, telemetry or IROKA runtime integration.
- Uses Android platform widgets and drawables, compatible with the declared API 29 floor in principle; compilation and API 29 runtime compatibility are NOT RUN.
- Visual effects are drawable-based; no continuous animation or GPU-only rendering is required.

## Unified creative-workspace direction

The long-term product architecture should converge on shared primitives rather than bolt together isolated feature screens:

- One non-destructive document/project model for source assets, raster layers, vector objects, text, masks, effects and export profiles.
- One compositing/render pipeline with preview-quality and export-quality modes.
- One selection, transform, crop and alignment interaction model.
- One reusable typography, colour, spacing, grid, snapping and alignment system.
- One history/command model with bounded memory and recoverable project persistence.
- One asset library for frames, templates, backgrounds, shapes, stickers, logos and reusable brand kits.
- One export pipeline for social graphics, portraits, identity-photo layouts and print/PDF.
- Local intelligence adapters that can recommend actions or rank quality, but cannot silently overwrite user work.
- Progressive capability: simple guided flows first, advanced controls on demand.

## Product capability map — future work, not delivered by this UI increment

The following areas are strategic targets and must not be represented as implemented until their own code, acceptance tests and verification records exist:

| Capability family | Target strengths to integrate | Weaknesses to avoid |
|---|---|---|
| Photo editor | Non-destructive adjustments, precise crop/transform, masks, layers, colour tools | Destructive-only workflows, hidden state, slow full-resolution previews |
| Portrait studio | Natural retouching, background tools, pose/framing guidance | Plastic skin, identity distortion, unsupported compliance claims |
| Graphic design | Vector shapes, editable typography, grids, alignment, templates, brand kits | Flattened-only projects, poor text control, template lock-in |
| Smart studio | Guided workflows, explainable suggestions, quality checks, batch presets | Opaque AI, false confidence, unreviewable automatic changes |
| Print studio | Physical units, bleed/safe areas, multipage layouts, calibrated export | Wrong dimensions, unverified DPI, printer-specific assumptions |
| Asset management | Reusable local assets, project versions, recovery, predictable naming | Cloud lock-in, data loss, unclear original/derivative boundaries |

## Verification boundary

| Check | Status |
|---|---|
| Source integration and action mapping review | PASS (manual source review) |
| New third-party runtime dependency | NONE |
| Kotlin/Android Gradle compilation | NOT RUN |
| Unit/instrumentation tests | NOT RUN |
| Screen reader and keyboard accessibility | NOT RUN |
| Small-screen and large-font layout | NOT RUN |
| API 29 and current-device rendering | NOT RUN |
| Memory/scroll performance | NOT RUN |
| Airplane-mode runtime test | NOT RUN |

No merge, PR, deployment, release or expenditure is authorized by this implementation record.
