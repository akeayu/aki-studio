# AKIDS-A3-SEG-ASYNC-001 v0.1.0 — Asynchronous local foreground estimation

- Stage: A3 implementation
- Status: IMPLEMENTED IN SOURCE; runtime verification NOT RUN
- Runtime boundary: on-device CPU only; no network, cloud, account, telemetry, or IROKA dependency
- Branch: `feat/a3-local-android-mvp`

## Change

Local foreground estimation is dispatched to a dedicated single-thread executor. The UI thread prepares an immutable ARGB bitmap snapshot, reports that analysis is running, and returns immediately. The worker creates the optional bundled LiteRT adapter, invokes the existing model-or-CPU-fallback segmentation engine, and composes the replacement preview. The result is delivered to the main thread.

## Lifecycle and consistency safeguards

1. A monotonically increasing request ID identifies the active estimate.
2. Starting a newer estimate cancels the prior Future and invalidates its request ID.
3. Changing the working image through the preview refresh/reset path invalidates in-flight estimates.
4. The worker receives a private bitmap copy; it does not read or mutate the Activity's preview bitmap.
5. The selected background colour is snapshotted before dispatch, making the output deterministic for that request.
6. Stale results and results delivered after Activity destruction are discarded; their temporary bitmaps are recycled.
7. The LiteRT interpreter is closed in a `finally` block, and the worker bitmap snapshot is recycled.
8. Activity destruction invalidates the request, cancels the Future, and shuts down the executor.
9. A worker exception is surfaced as a local failure message; the original selected image remains unchanged.

Cancellation is best-effort: an already-running native inference call may finish before interruption is observed. The request ID check prevents its output from being applied after the request is stale.

## Segmentation model availability

The adapter remains optional. The expected `app/src/main/assets/selfie_segmenter.tflite` file is not present in this repository snapshot. Therefore `createOrNull()` returns null and the deterministic border-colour CPU heuristic remains the active provider. No model binary, checksum, model activation, or neural-segmentation quality is claimed by this record.

The current adapter resizes the complete source frame to the model's square input tensor. This preserves coverage of the full image but distorts non-square aspect ratios during inference. Model preprocessing parity and mask quality must be evaluated before treating the adapter as production-ready. Non-finite model mask values are rejected.

## Verification

- Source inspection: request invalidation, worker isolation, result delivery, stale-result disposal, interpreter closure, and Activity shutdown are present.
- Gradle build: NOT RUN.
- Unit/instrumentation tests: NOT RUN.
- Emulator/device test: NOT RUN.
- Airplane-mode runtime test: NOT RUN.
- Memory/latency benchmark: NOT RUN.
- Model binary checksum verification: NOT RUN; binary absent.

This is an A3 implementation record, not A4 verification or a release approval.
