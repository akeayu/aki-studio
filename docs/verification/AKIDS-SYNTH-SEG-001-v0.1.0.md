# Synthetic Foreground Segmentation Evaluation

**Suite:** AKIDS-SYNTH-SEG-001 v0.1.0  
**Implementation stage:** A3  
**Status:** SYNTHETIC REFERENCE EVALUATION PASS — ANDROID RUNTIME NOT VERIFIED

## Purpose and scope

This suite is a deterministic, standard-library-only Python reference harness for the current border-colour heuristic. It generates artificial 128 × 128 RGB images and ground-truth masks, emulates the app heuristic's RGB Euclidean-distance threshold and soft transition, and reports IoU, precision and recall.

It does not invoke Android Bitmap APIs or execute the Kotlin implementation. Results are therefore algorithm-reference evidence only, not an Android build, device, performance, or real-photo evaluation.

## Cases and observed results

| Synthetic case | IoU | Precision | Recall | Interpretation |
|---|---:|---:|---:|---|
| Flat high-contrast background | 1.0000 | 1.0000 | 1.0000 | Positive control only |
| Subject similar to background | 0.0000 | 0.0000 | 0.0000 | Expected failure exposed |
| Strongly varying horizontal background | 0.3195 | 0.3195 | 1.0000 | Significant false foreground exposed |

The positive control is intentionally simple: a uniform RGB background and a solid-colour elliptical subject. It demonstrates that the threshold arithmetic can separate a clearly contrasting synthetic subject. It does not establish performance on hair, skin, shadows, real camera noise, gradients, patterned backgrounds, motion blur or compression artifacts.

The similar-colour and varying-background cases are negative controls. Their poor scores are important findings, not passing segmentation outcomes. They demonstrate why the current algorithm must remain labelled low-confidence and require manual correction.

## Executable checks

Run from the repository root:

```bash
python3 tools/segmentation_eval.py
```

The harness writes PPM source images, PGM truth/prediction masks and `build/segmentation-eval/report.json`. It exits non-zero if the high-contrast control falls below IoU 0.90 or if either known failure case is no longer exposed by the current heuristic. This guards against silently weakening the synthetic checks; it is not a general model-quality benchmark.

## Acceptance and release gates

- Synthetic positive-control IoU ≥ 0.90: required for this narrow reference test.
- Known similar-colour and varying-background failure cases remain visible: required.
- No production segmentation acceptance threshold is established by these three synthetic fixtures.
- Before selecting a trained model: compare license and redistribution terms, model size, CPU latency, peak memory, Android API compatibility, mask quality, and offline packaging.
- Before A4: execute Kotlin/Android tests, validate export dimensions and alignment, profile low-memory target devices, and evaluate a consent-governed, representative dataset. Do not collect or upload real portraits under this synthetic test authorization.

## Privacy and runtime

The harness uses generated geometric shapes and synthetic RGB values only. It requires no network, cloud service, external image corpus, account, telemetry, or third-party Python package. No real portrait or identity document is included.
