# LiteRT CPU Integration Spike — A3

**Record:** AKIDS-A3-SEG-LITERT-001 v0.1.0  
**Status:** ADAPTER IMPLEMENTED — MODEL ASSET ABSENT — BUILD/RUNTIME NOT VERIFIED

## Scope

This increment adds an optional LiteRT CPU Interpreter adapter for the pinned MediaPipe Selfie Segmenter candidate. It does not claim that the model is currently packaged or active in the application.

## Changes

- Pinned `com.google.ai.edge.litert:litert:2.4.0` in the Android app module.
- Added `LiteRtSelfieSegmenter`, implementing the existing `OnDeviceSegmentationModel` contract.
- Uses the CPU Interpreter with two worker threads; no GPU, NNAPI, Play Services, remote runtime or network path is configured.
- Validates exact input/output tensor shapes and FLOAT32 types before accepting the model.
- Uses 256×256 RGB input normalized to [0,1] and retains the soft output mask.
- Runs inference synchronously at the call site; the UI integration must be moved to a background executor before production use.
- Closes the Interpreter after each estimate.
- If the asset is absent or initialization fails, the existing CPU border-colour heuristic remains the fallback.
- Pixels outside the center-square model field of view are marked foreground/protected rather than being removed by the background replacement.
- Manual mask correction remains available.

## Critical asset status

The required binary is **not present** at `app/src/main/assets/selfie_segmenter.tflite`. The GitHub text-file connector used for this implementation cannot upload the binary artifact. No placeholder file was created.

Consequently:

- `createOrNull()` returns null in the current checkout.
- The app continues to use the heuristic fallback.
- The LiteRT adapter is source-level integration only, not an active model integration.
- The dependency has not been resolved by Gradle.
- The model SHA-256 has not been independently verified against bytes in this repository.
- The upstream model license/notice has not yet been copied into the repository.

## Known technical limitations

1. The model path currently uses a center-square crop. Content outside that crop is protected from replacement, but remains unsegmented.
2. The current invocation is synchronous. A production UI must dispatch inference off the main thread and handle cancellation/lifecycle.
3. The adapter's result confidence field is a compatibility placeholder, not a calibrated score. UI must not present it as measured certainty.
4. The adapter is not yet tested against a real model binary, Android build, emulator, or physical device.
5. The 256×256 model is not a portrait-matting guarantee and cannot certify official ID/passport compliance.
7. Tensor allocations and mask resizing need profiling on low-memory Android devices.

## Verification status

| Check | Status |
|---|---|
| Kotlin adapter source added | PASS — source present |
| LiteRT dependency pinned | PASS — declaration present |
| CPU-only Interpreter configured | PASS — source inspection |
| Tensor shape/type guards | PASS — source inspection |
| Heuristic fallback retained | PASS — source inspection |
| Manual correction retained | PASS — existing workflow |
| Model binary vendored | NOT RUN — binary transfer unavailable |
| SHA-256 verified in repository | NOT RUN |
| License/NOTICE vendored | NOT RUN |
| Gradle dependency resolution | NOT RUN |
| Gradle build / APK | NOT RUN |
| Android inference | NOT RUN |
| Background-thread dispatch | NOT RUN |
| API 29 device test | NOT RUN |
| Latency / memory benchmark | NOT RUN |

## Gate decision

This is a bounded A3 integration spike. Do not call the MediaPipe model integrated, operational, or production-ready until the exact binary and license notices are vendored, the checksum is independently verified, Gradle builds, and CPU inference is tested on API 29+ devices. Keep the heuristic and manual editing paths enabled.


## Follow-up source correction

The adapter now maps the full source frame to and from the square tensor instead of center-cropping. This avoids leaving unsegmented border regions, at the cost of aspect-ratio distortion during model inference. Interpreter instances that fail tensor validation are now explicitly closed. These are source-level corrections only; they have not been built or runtime-tested.
