# A3 Technical Assessment — Offline Portrait Segmentation Model Candidates

**Record:** AKIDS-A3-SEG-MODEL-REVIEW-001 v0.1.0  
**Stage:** A3 implementation research / design assessment  
**Runtime boundary:** Android API 29+, bundled model, CPU inference, no network/cloud  
**Status:** ASSESSMENT RECORDED — NO MODEL ADOPTED

## 1. Decision summary

The current `OnDeviceSegmentationModel` interface is an appropriate integration seam. The current default remains the CPU border-colour heuristic and must continue to be labelled as a heuristic, not AI.

**Recommended next experiment:** evaluate the MediaPipe Selfie Segmenter 256×256 model as the smallest practical CPU inference baseline, while retaining MMNet as the portrait-matting research comparator. MODNet is a quality-oriented comparator, not the first low-resource integration choice.

This is a recommendation for a bounded prototype only. It is not approval to ship a model, make official-photo compliance claims, or remove manual mask editing.

No model file, runtime dependency, remote service, or model download was added in this increment.

## 2. Candidate comparison

| Candidate | Source and license evidence | Runtime / footprint evidence | Assessment |
|---|---|---|---|
| MediaPipe Selfie Segmenter | MediaPipe project is Apache-2.0. The published LiteRT community square model card declares Apache-2.0. | General model uses 256×256 RGB input and returns a 256×256 mask. A community FP16 model card reports about 250 KB. MediaPipe documents smartphone use for prominent people/selfies, normally within 2 m. | **First prototype candidate.** Very small and relevant to foreground/background replacement. Low resolution and selfie/video-call training/use case may limit hair, fine edges, small accessories and formal portrait framing. The 250 KB figure is for the community-published artifact, not a verified pinned Google binary. |
| MMNet | Upstream `hyperconnect/MMNet` repository declares Apache License 2.0. | Research targets real-time mobile portrait matting. Upstream repository describes 128×128 and 256×256 experiments and a reported accelerated 30 FPS result on a Xiaomi Mi 5 / Snapdragon 820. The repository's setup is TensorFlow 1.6-era; a supported, pinned Android LiteRT artifact and its exact size were not established. | **Second comparator.** Strong mobile/portrait relevance, but requires more conversion, provenance and Android compatibility work before adoption. Published research speed is not an Aki device benchmark. |
| MODNet | Official `ZHKKKe/MODNet` repository states that code, models and demos (excluding its GIF directory) are Apache-2.0. | RGB-only, trimap-free portrait matting. Upstream research code and community conversions exist. A separate community conversion reports a roughly 25–26 MB TFLite model, but that is not the canonical upstream Android artifact. | **Quality comparator / later option.** Better aligned with soft portrait alpha mattes and hair edges in principle, but higher model/runtime cost and no verified canonical LiteRT package for this project. Do not treat third-party conversion claims as upstream guarantees. |

## 3. Evidence and provenance caveats

### MediaPipe
- Official MediaPipe documentation describes the general model as 256×256×3 input and output as a 256×256×1 segmentation mask.
- Official documentation describes the intended use as prominent-person selfie effects and video conferencing; it is not a passport-photo compliance model.
- The legacy documentation shows both CPU and GPU graphs, so CPU operation is technically represented in the project. That does not prove acceptable performance on Aki's minimum target handset.
- The 250 KB FP16 size is from a community LiteRT model card. Before bundling, pin the exact artifact, publisher, immutable revision, SHA-256 digest, metadata and license/notice files. Do not fetch a mutable `latest` asset at runtime.

### MMNet
- The upstream repository's Apache-2.0 license is visible in the repository.
- The repository describes a mobile matting architecture and reports a historical accelerated benchmark on Snapdragon 820. It does not establish performance on current entry-level Android devices or provide an already-integrated Aki-compatible runtime contract.
- Verify any model weights separately from the source repository license, including the git submodule/revision and binary weight provenance.

### MODNet
- The upstream repository explicitly states Apache-2.0 for code, models and demos, subject to its stated GIF exclusion.
- The upstream README identifies community conversion paths, not an official Aki-ready LiteRT Android artifact.
- Any converted binary must be traced to its exact source checkpoint, conversion scripts, patches, quantization settings, operator set and checksum. A model's architecture license alone is not a substitute for binary provenance.

## 4. Provisional integration direction

For the first Android CPU spike, use the MediaPipe Selfie Segmenter candidate only after its exact model artifact and metadata are pinned and reviewed.

The spike must:
1. Load the model only from the APK's bundled assets; no runtime URL, download, update check or network fallback.
2. Run on CPU without requiring GPU, NNAPI, DSP or NPU acceleration.
3. Accept a bounded software bitmap, perform explicit documented RGB conversion and normalization, and return a source-aligned foreground mask.
4. Keep source pixels immutable and release interpreter, tensor and bitmap resources deterministically.
5. Surface method, model version, confidence/uncertainty and limitations to the editor.
6. Fall back to the existing local heuristic on model initialization/inference failure, and preserve manual paint/erase correction.
7. Keep all inference local; no image, mask, diagnostic or telemetry upload.

The current Gradle project has no third-party runtime dependencies. Introducing LiteRT or MediaPipe Tasks would be a deliberate dependency change and must be documented, version-pinned and checked for API 29 compatibility, supported ABIs, transitive dependencies and license notices before coding it into the app.

## 5. Required gates before model adoption

| Gate | Required evidence | Current status |
|---|---|---|
| Artifact provenance | Immutable upstream URL/revision, SHA-256, model card, license and redistribution rights | OPEN |
| Runtime compatibility | Verified Android API 29+, CPU-only interpreter, supported ABI and operator set | NOT RUN |
| Resource profile | Model bytes, peak native/Java memory, inference latency and thermal behavior on low-end target devices | NOT RUN |
| Mask quality | Reproducible test corpus with IoU, precision/recall, boundary quality and failure-case review | NOT RUN |
| Portrait suitability | Hair, dark/light clothing, similar-colour background, textured background, glasses, head coverings and edge-touching subjects | NOT RUN |
| Privacy/offline | Airplane-mode runtime test; prove no network permission or network traffic | NOT RUN |
| Recovery | Model load/inference failure, low-confidence warning, manual correction and original preservation | NOT RUN |
| Android build | Successful Gradle build, unit/instrumentation tests and APK inspection | NOT RUN |

Latency and memory acceptance thresholds must be set after the actual target handset class is identified and measured. Do not invent a performance pass based on desktop inference, a paper benchmark, or a different phone.

## 6. Evaluation protocol

- Keep the existing synthetic harness as a deterministic regression suite for the border-colour heuristic. It does not evaluate the neural model and does not execute Android Kotlin.
- Add model-specific synthetic positive controls and hard negatives only after the exact artifact is pinned.
- Evaluate model outputs against ground-truth masks; report per-case metrics and aggregate distributions, not a single averaged score alone.
- Include edge-focused assessment for hair, ears, glasses, shoulders, clothing and semi-transparent detail.
- Do not collect real portraits or identity documents under this assessment. Any later real-image corpus requires a separate explicit data-governance and consent decision.
- Do not make biometric identity, face recognition or official-photo compliance claims.

## 7. Scope and authorization

This record is within the already-authorized A3 local-only Android MVP work. It does not authorize:
- cloud inference, remote model downloads or network runtime;
- paid models, services, licenses or subscriptions;
- collecting real personal photographs;
- merge to `main`, opening a PR, release or deployment;
- removing the current heuristic or manual-mask fallback.

## 8. References

1. MediaPipe Selfie Segmentation overview and model input shapes: https://github.com/google-ai-edge/mediapipe/blob/master/docs/solutions/selfie_segmentation.md
2. MediaPipe model catalogue: https://github.com/google-ai-edge/mediapipe/blob/master/docs/solutions/models.md
3. MediaPipe project license: https://github.com/google-ai-edge/mediapipe
4. LiteRT Community square selfie model card (community artifact; not an immutable upstream pin): https://huggingface.co/litert-community/MediaPipe-ImageSegmenter-Selfie-Square
5. MMNet upstream repository and Apache-2.0 declaration: https://github.com/hyperconnect/MMNet
6. MODNet upstream repository and Apache-2.0 declaration: https://github.com/ZHKKKe/MODNet
7. MODNet upstream license text: https://github.com/ZHKKKe/MODNet/blob/master/LICENSE

## 9. Verification record

- Repository source inspection: **PASS** — existing replaceable model interface and local fallback confirmed.
- Manifest network permission inspection: **PASS** — no `INTERNET` permission declared.
- Candidate source/license review: **PARTIAL** — upstream project licenses and use cases reviewed; exact distributable model artifacts and checksums not yet pinned.
- Model integration: **NOT RUN**.
- Gradle build / Android runtime / physical-device benchmark: **NOT RUN**.
- No model binary or runtime dependency added.
