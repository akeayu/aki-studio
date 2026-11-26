# AKIDS-A3-DIRECT-MANIPULATION-001 v0.1.0

**Stage:** A3 local-only implementation  
**Status:** Implemented in source; Android runtime verification pending  
**Scope:** Touch selection, move, resize, rotate, center snapping and layer schema compatibility

## Delivered
- Added `DesignLayerOverlayView`, a touch overlay aligned to the FIT_CENTER image bounds.
- Selects visible layers by topmost-first hit testing; locked layers can be selected but not modified.
- Drag inside a layer to reposition it in normalized source-image coordinates.
- Four corner handles resize the layer while preserving the opposite side's approximate anchor.
- A top-center handle rotates the layer; angles snap to 15-degree increments near the increment.
- Center alignment snapping is provided for horizontal and vertical canvas centerlines, with visual guides.
- A completed gesture commits one recipe-history snapshot, not one snapshot per move event.
- Selection and transform handles are drawn over the current composite preview. The existing export renderer applies the same transform to output.
- Rotation is stored in the layer document and rendered by the CPU compositor.

## Data compatibility
- Layer document schema advanced from v1 to v2 to persist rotation.
- Decoder continues to read v1 payloads, assigning zero degrees to legacy layers.
- Project manifest schema remains unchanged; the optional layer payload remains backward compatible with projects without layers.
- Tests authored for v2 rotation round-trip and v1 payload compatibility.

## Known limits
- Resize is corner-based and does not yet provide independent edge handles, aspect-ratio lock, or multi-touch pinch.
- Rotation is single-pointer; snapping currently covers canvas center axes and 15-degree angle increments.
- No multi-selection, group transforms, smart object-to-object snapping, keyboard fine adjustment or accessibility alternative for gestures yet.
- Selection hit-testing and visual handles need physical-device validation at varied densities and aspect ratios.
- This overlay is a first interaction foundation, not a replacement for full professional transform tooling.

## Verification
- Source readback/static checks are recorded separately.
- Gradle build, JUnit execution, instrumentation, emulator and physical-device gesture tests, accessibility, low-memory and performance tests have not been run.
- No network, cloud, telemetry, account or remote runtime is introduced.
