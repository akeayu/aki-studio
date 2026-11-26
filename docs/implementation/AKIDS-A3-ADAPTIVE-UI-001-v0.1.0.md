# Aki Studio adaptive UI foundation — A3

## Purpose
Introduce a reusable Android View-based responsive system that adapts to the current application window rather than assuming one phone size or orientation.

## Adaptive inputs
- Current window width and height in dp, including resizable and split-screen windows.
- Smallest-width qualifier as a tablet-like form-factor hint.
- Portrait/landscape orientation.
- Display density and user font scale.
- Window classes: compact (<600dp), medium (600–839dp), expanded (>=840dp).

## Implemented layout policy
- Action grids use 2, 3 or 4 columns by width class; large font scale reduces density.
- Tool sections become two columns on sufficiently wide, tall windows.
- Preview height responds to orientation and width class.
- Page gutters scale from compact to expanded.
- Content width is capped on very wide displays to preserve readable line lengths.
- Orientation, screen-size, screen-layout and smallest-screen-size changes rebuild the view tree while preserving the in-memory editing session.
- System window insets are applied to the scroll workspace.
- Shared visual tokens and cards remain platform-View based; no third-party UI dependency.

## Reusable primitives
- AdaptiveUiProfile: window/device policy snapshot.
- AkiStudioUi.page: adaptive page shell.
- AkiStudioUi.header: shared studio header and language switch.
- AkiStudioUi.panel: section card with responsive action grid.
- AkiStudioUi.actionTile: reusable action widget/card.
- AkiStudioUi.previewContainer: layered preview surface.
- AkiStudioUi.statusText and badge: shared status and metadata partials.

## Deliberate boundaries
- Fold posture is not inferred from screen dimensions alone; a future fold-feature integration can supply posture explicitly.
- No hardware model allow-list or device-name detection. The layout responds to actual window constraints instead.
- This is a native Android Views implementation; no Compose or external UI dependency was added.
- Accessibility, touch target, Amharic shaping, foldable hinge avoidance, keyboard navigation and real-device visual behavior still require runtime verification.

## Verification
- Pure adaptive classification tests are authored with this increment.
- Android build and unit tests are to be verified by the repository workflow.
- Physical-device and foldable posture checks remain NOT RUN until suitable hardware is available.
