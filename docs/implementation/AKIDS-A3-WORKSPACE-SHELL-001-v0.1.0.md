# Aki Studio unified workspace shell — A3

## Scope
Add a persistent workspace navigation and contextual command layer around the existing native Android editing surface. Preserve the local-only runtime and existing editing-session state.

## Delivered
- Horizontally scrollable workspace navigation: All, Workspace, Image Lab, Portrait & ID, Background Lab, Print Studio, Design Studio.
- Selecting a workspace filters visible tool panels in place. It does not recreate the Activity or discard the current image, masks, layers, selection, or edit history.
- Persistent contextual command strip exposes Undo, Redo, Before/After, Export, and (on expanded windows) Layers.
- Reusable toolbar buttons and selected-state navigation chips in AkiStudioUi.
- Collapsible Inspector panel displays current selection identity, layer count and local-processing context.
- Inspector defaults expanded on sufficiently large expanded windows and collapsed on smaller windows; users can toggle it on every window class.
- Inspector summary refreshes when canvas selection or preview state changes.
- Existing responsive window profile continues to determine action-grid density, preview sizing, panel columns and gutters.

## Architecture notes
- Native Android Views only; no new runtime dependency.
- Workspace filtering is view visibility state, not route/activity navigation.
- The toolbar uses existing application actions and introduces no placeholder actions.
- Inspector is informational in this increment; editable per-layer property controls remain part of later inspector evolution.
- Navigation uses a horizontal scroll strip across form factors; a dedicated vertical navigation rail remains a future enhancement.
- Fold posture/hinge geometry is not inferred from window size.

## Privacy and runtime
No network, cloud, account, telemetry, remote inference or IROKA runtime dependency was added. All commands continue to use existing local application behavior.

## Verification status
Source and documentation are committed. Android Gradle build, unit tests, emulator rendering, touch behavior, accessibility and physical-device checks must be verified by CI/device evidence before A4 acceptance.
