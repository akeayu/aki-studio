# LiteRT CPU Integration Spike — A3

**Record:** AKIDS-A3-SEG-LITERT-001 v0.1.1  
**Status:** ADAPTER SOURCE IMPLEMENTED — MODEL ASSET ABSENT — BUILD/RUNTIME NOT VERIFIED

## Scope

This increment adds an optional LiteRT CPU Interpreter adapter for the pinned MediaPipe Selfie Segmenter candidate. It does not claim that the model is currently packaged or active in the application.

## Changes

- Pinned `com.google.ai.edge.litert:litert:2.4.0` in the Android app module.
- Added `LiteRtSelfieSegmenter`, implementing the existing `OnDeviceSegmentationModel` contract.
- Uses the CPU Interpreter with two worker threads; no GPU, NNAPI, Play Services, remote runtime or network path is configured.
- Validates input/output tensor shapes and FLOAT32 types before accepting the model.
- Uses 256×256 RGB input normalized to [0,1] and retains the soft output mask.
- Resizes the complete source frame into the square model tensor and maps the output mask across the complete source frame. This avoids unsegmented borders but geometrically distorts non-square images during inference.
- Closes the Interpreter after each estimate and closes it if tensor validation fails.
- If the asset is absent or initialization fails, the existing CPU border-colour heuristic remains the fallback.
- Manual mask correction remains available.
- The current invocation is synchronous at the call site. It must be moved to a background executor with lifecycle/cancellation handling before production use.

## Model contract evidence

The selected square MediaPipe Selfie Segmenter is documented as 256×256 input, float16 quantized weights, and a person-confidence mask. The published community mirror reports FLOAT32 input/output tensors of [1,256,256,3] and [1,256,256,1], respectively, and records the artifact SHA-256 in the model pin record. The upstream Google model card describes the 256×256 RGB input normalized to [0,1], person mask values [0,1], and Apache-2.0 license.

These published specifications are reference evidence only. They do not replace validation against the exact vendored binary.

## Critical asset status

The required binary is **not present** at `app/src/main/assets/selfie_segmenter.tflite`. The available repository write connector accepts text content and cannot upload this binary artifact. No placeholder file was created.

Consequently:

- `createOrNull()` returns null in the current checkout.
- The app continues to use the heuristic fallback.
- The LiteRT adapter is source-level integration only, not an active model integration.
- The dependency has not been resolved by Gradle.
- The model SHA-256 has not been independently verified against bytes in this repository.
- The upstream model license/NOTICE has not yet been copied into the repository.

## Known technical limitations

1. Full-frame resize to a square tensor distorts non-square image geometry; evaluate letterboxing only if it matches model preprocessing expectations and improves measured mask quality.
2. Synchronous inference can block the UI. Dispatch it off the main thread and handle cancellation/lifecycle before production use.
3. The result confidence field is a compatibility placeholder, not a calibrated score. The UI must not present it as measured certainty.
4. The adapter has not been tested against a real model binary, Android build, emulator, or physical device.
5. The model is intended for prominent-person segmentation; it is not a portrait-matting guarantee and cannot certify official ID/passport compliance.
6. Tensor allocations, inference latency and mask resizing need profiling on low-memory Android devices.

## Verification status

| Check | Status |
|---|---|
| Kotlin adapter source added | PASS — source present |
| LiteRT dependency pinned | PASS — declaration present |
| CPU-only Interpreter configured | PASS — source inspection |
| Tensor shape/type guards | PASS — source inspection |
| Full-frame mask mapping | PASS — source inspection |
| Interpreter cleanup on invalid tensor contract | PASS — source inspection |
| Heuristic fallback retained | PASS — source inspection |
| Manual correction retained | PASS — existing workflow |
| Model binary vendored | MISSING |
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
