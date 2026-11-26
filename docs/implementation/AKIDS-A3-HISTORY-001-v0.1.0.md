# AKIDS-A3-HISTORY-001 — bounded edit undo/redo

- Version: 0.1.0
- Stage: A3 local-only Android MVP
- Status: IMPLEMENTED — static inspection only; Android/JUnit execution pending
- Scope: recipe-level undo/redo for non-destructive image edits

## Implemented

- Added `EditHistory`, a platform-independent history manager.
- Stores immutable copies of scalar edit-recipe snapshots, not image bitmaps.
- Supports reset, record, undo and redo.
- Duplicate consecutive snapshots are ignored.
- A new edit after undo discards the redo branch.
- Default history capacity is 40 states; supported capacity is bounded to 2–100.
- When capacity is exceeded, the oldest state is dropped.
- Added Amharic/English Undo edit and Redo edit controls.
- Connected rotation, monochrome, brightness, contrast, saturation, warmth, crop ratio/rectangle, crop position and resize recipe changes to history.
- Undo/redo re-renders from the original source image; it does not repeatedly transform a prior preview.
- Opening a new source or restoring a saved project starts a fresh in-session history baseline.

## Deliberate boundaries

- This increment is session history only. History is not yet serialized into saved project manifests; reopening a project restores its current edit recipe but not earlier undo steps.
- Background mask painting retains its dedicated stroke-level undo/redo. Mask pixels and mask stroke history are excluded from recipe snapshots to avoid falsely claiming that a scalar recipe can restore bitmap state.
- Brush/feather settings, guide visibility, guide percentages, and background-mask state are not part of this history.
- The history manager stores only small parameter maps, not full bitmap snapshots; it therefore does not multiply image memory usage.
- A failed render does not establish a new history state because recording occurs only after preview rendering succeeds.

## Verification

- Added seven JUnit tests for snapshot recording, undo/redo, redo-branch invalidation, capacity bounds, defensive copying, capacity validation and empty history.
- Static source inspection: PASS (10/10 repository checks).
- Standalone Kotlin compilation of the actual `EditHistory.kt` source: PASS.
- Standalone behavioral harness: PASS for record/undo, redo-branch invalidation, capacity bounds and defensive snapshot copying.
- JUnit/Gradle execution: NOT RUN (Android SDK/Gradle toolchain unavailable in the current execution environment).
- Android device interaction, memory profiling and project reopen behavior: NOT RUN.

## Next

Persist bounded recipe history inside a versioned project manifest, retaining schema-v1 compatibility and keeping the flattened background mask lifecycle separate.
