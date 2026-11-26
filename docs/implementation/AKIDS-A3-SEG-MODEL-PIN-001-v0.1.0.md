# Model Artifact Pin — MediaPipe Selfie Segmenter Square FP16

**Record:** AKIDS-A3-SEG-MODEL-PIN-001 v0.1.0  
**Purpose:** Reproducible artifact pin for the A3 local Android CPU integration spike  
**Status:** CANDIDATE PINNED — BINARY NOT YET VENDORED OR EXECUTED

## 1. Selected artifact

| Field | Pinned value |
|---|---|
| Model | MediaPipe Selfie Segmenter — square |
| File | `selfie_segmenter.tflite` |
| Precision | FP16 weights; model input/output tensors are float32 |
| File size | 249,537 bytes |
| SHA-256 | `191ac9529ae506ee0beefa6b2c945a172dab9d07d1e802a290a4e4038226658b` |
| Input tensor | `[1, 256, 256, 3]`, float32 RGB |
| Input normalization | divide channel values by 255; mean 0, std 255 |
| Output tensor | `[1, 256, 256, 1]`, float32 person confidence mask |
| Model metadata | ImageSegmenter, version 1, author MediaPipe |
| License | Apache License 2.0, as stated by the bundled upstream Google model card |
| Upstream GCS generation | `1683436453600523` |
| Upstream pinned bucket version | `/1/` |
| Upstream source | `https://storage.googleapis.com/mediapipe-models/image_segmenter/selfie_segmenter/float16/latest/selfie_segmenter.tflite` |
| Provenance mirror | `https://huggingface.co/litert-community/MediaPipe-ImageSegmenter-Selfie-Square` |

The mirror documents that its model binary is byte-identical to Google's official MediaPipe artifact and records the Google Cloud Storage generation and pinned bucket version. The SHA-256 above is the mirror's published digest. This is strong provenance evidence, but the Aki build pipeline must independently hash the acquired bytes before accepting them.

## 2. Runtime pin for the prototype

- Runtime: standalone LiteRT Android.
- Version: `com.google.ai.edge.litert:litert:2.4.0`.
- API: CPU-only `Interpreter` API for the first integration spike; do not use GPU, NNAPI, Play services, or a remote runtime.
- Android minimum: LiteRT 2.4.0 documents CPU Interpreter support from API 24; Aki's minSdk 29 is above that platform floor.
- Model bytes must be packaged inside the application. No runtime model download, network initialization, remote update, Play services dependency or network fallback.
- Runtime version pin is provisional until Maven resolution and an Android build pass. No dependency is added by this record.

## 3. Required acquisition and integrity procedure

1. Acquire the model from the pinned Google artifact generation / version, or the provenance mirror at an immutable repository revision.
2. Calculate SHA-256 over the downloaded binary.
3. Require exact match with `191ac9529ae506ee0beefa6b2c945a172dab9d07d1e802a290a4e4038226658b`.
4. Confirm exact file size 249,537 bytes.
5. Inspect FlatBuffer metadata and tensor shapes with a local model-inspection tool.
6. Preserve the model card, Apache-2.0 license text and attribution/notice in the repository.
7. Record the acquisition URL, immutable source revision or GCS generation, acquisition date, SHA-256 and tool version in the model supply-chain manifest.
8. Reject the artifact on any mismatch; never silently accept a refreshed `latest` binary.

## 4. Integration contract

- Convert the input bitmap to RGB, center-crop/letterbox policy must be explicit, resize to 256×256, and normalize to float32 [0,1].
- Run CPU inference off the UI thread.
- Read the output confidence mask and resample it to source dimensions using a documented interpolation method.
- Preserve soft probabilities for feathered compositing; do not threshold to a hard binary mask by default.
- Treat the model output as a foreground estimate, not calibrated certainty or official-photo validation.
- Expose low-confidence/unsupported cases and require user review.
- Keep manual paint/erase correction and the existing CPU heuristic fallback.
- Close the interpreter and release all buffers/bitmaps deterministically.
- Never mutate or replace the original source photo.

## 5. Model limitations and use restrictions

The model is intended for prominent-person human/background segmentation in video-call and mobile-AR settings. It is not a portrait-matting guarantee, passport-photo standard, biometric identification model, surveillance system or proof of identity. Its 256×256 mask may be insufficient for fine hair, translucent edges, jewellery and high-resolution print boundaries. Published performance from Pixel 6 or other devices is not an Aki low-end handset benchmark.

## 6. Current gate status

| Gate | Status |
|---|---|
| Public model card and license statement inspected | PASS |
| Model architecture/input/output/size/digest documented | PASS (source-reported) |
| Upstream Google GCS generation and pinned version documented | PASS (mirror provenance record) |
| Binary acquired into Aki repository | NOT RUN |
| Independent SHA-256 of acquired bytes | NOT RUN |
| License/notice copied into Aki repository | NOT RUN |
| LiteRT 2.4.0 Maven resolution | NOT RUN |
| Android adapter implementation | NOT RUN |
| Gradle build and APK inspection | NOT RUN |
| CPU inference / API 29 device tests | NOT RUN |
| Memory and latency benchmark on target devices | NOT RUN |
| Portrait mask quality evaluation | NOT RUN |

## 7. Authorization boundary

This pin authorizes no expenditure and no cloud/network runtime. It does not authorize collection of real portraits, merge to main, PR, deployment or release. It does not replace the current heuristic or manual correction path. The candidate is not approved as the production default.
