# Aki Studio — Android Device Matrix and Image-Quality Evaluation Corpus

**Document ID:** AKIDS-A2-DEVICE-IMAGE-EVAL-001  
**Version:** 0.1.0  
**Status:** A2 PROPOSAL — FOR OWNER REVIEW; NO TESTS EXECUTED  
**Owner:** Akefa Ayele Daka  
**Prepared:** 26 November 2026  
**Parent:** AKIDS-A2-TECH-PROP-002 v0.1.0  
**Repository:** akeayu/aki-studio  
**Branch:** docs/a2-technical-reconciliation-v0.1.0

## 1. Purpose and disposition

This document establishes a provisional Ethiopia-focused Android compatibility matrix and a consent-based image-quality evaluation corpus for Aki Studio.

The matrix is a selection plan for acquiring or borrowing test devices. It is not a claim that the named devices have been tested. The corpus is a proposed collection and annotation specification. No participant images have been collected, no model has been trained, and no image-quality benchmark has been run.

The product remains Android-first, Amharic-first, offline-first and privacy-first. Core editing and export must remain usable on lower-spec devices without remote inference.

## 2. Market and platform evidence

The device shortlist uses the AppBrain Ethiopia device ranking updated 25 November 2026. Its listed leading Android models include Samsung Galaxy A06, TECNO SPARK 40, Samsung Galaxy M13, TECNO SPARK Go 2/2S, Infinix SMART, TECNO SPARK 30C and Samsung Galaxy A16. AppBrain describes its data as an anonymized sample from applications using its SDK, with the displayed popularity calculated over the preceding seven days; this is a useful shortlist signal, not a census of Ethiopian handset ownership.

- AppBrain Ethiopia device list: https://www.appbrain.com/stats/top-android-phones-tablets-by-country?country=ET
- StatCounter reported Android at 94.82% of Ethiopia mobile/tablet OS usage in September 2026. This is browser-usage share, not a handset sales or installed-base census: https://gs.statcounter.com/os-market-share/mobile-tab-/ethiopia
- Google Play requires new apps and app updates to target Android 16 / API 36 from 31 August 2026: https://support.google.com/googleplay/android-developer/answer/11926878
- Android's low-cost-device guidance recommends adapting memory use to device capacity and using lower-resolution working images when required: https://developer.android.com/docs/quality-guidelines/build-for-billions/device-capacity

## 3. Provisional Android device matrix

Physical RAM, not the vendor's combined physical-plus-extended-memory marketing figure, determines the test tier. Extended/virtual RAM must not be counted as physical RAM.

| Tier | Representative device / configuration | Evidence-backed profile | Required test scope |
|---|---|---|---|
| L0 — entry / Android Go | TECNO SPARK Go 2, 3 GB physical RAM variant | Android 15 Go; T7250; 720×1600 display; vendor lists 3 GB + 3 GB extended RAM in the 6 GB configuration | Import, crop, rotate, basic adjustments, save/reopen, offline export, low-memory behavior, process death and 12 MP image handling |
| L1 — entry | Infinix SMART 10, 3 GB physical RAM variant | Android 15 / XOS 15.1; T7250; 720×1600 display; vendor lists 3 GB + 3 GB extended RAM | Same as L0; verify camera/import variation and low-storage recovery |
| M0 — mainstream entry | Samsung Galaxy A06, 4 GB / 64 GB | Helio G85; 720×1600 display; 50 MP rear camera; official Samsung Africa listing confirms 4 GB physical RAM | Full deterministic editing, print layout, 12 MP and 24 MP tiled processing, local CV candidate benchmark |
| M1 — mainstream entry | TECNO SPARK 40, 4 GB physical RAM variant | Android 15; Helio G81; 720×1600; 50 MP rear camera; vendor's 8 GB combined listing includes 4 GB extended RAM | Same as M0; thermal and repeated-export tests |
| M2 — mainstream | Samsung Galaxy A16, 4 GB / 128 GB | Helio G99 LTE variant; FHD+ 1080×2340 display; 50 MP rear and 13 MP front cameras | Full deterministic editing, higher-resolution UI, 24 MP tiled workflow, local CV candidate benchmark |
| H0 — upper mainstream | Samsung Galaxy A16, 6 GB physical RAM variant, or SPARK 40 6 GB physical variant | Vendor listings distinguish physical RAM from extended RAM; exact Ethiopia SKU must be confirmed | 24 MP tiled workflow, batch operations, segmentation benchmark, thermal/battery soak |
| LEGACY — compatibility | Samsung Galaxy M13, 4 GB / Android 12 launch profile | Samsung launch material lists 4 GB / 64 GB variant and Android 12 | API compatibility, storage permission migration, import/export, offline recovery and reduced-feature behavior |

Manufacturer references:

- Samsung Galaxy A06: https://www.samsung.com/africa_en/business/smartphones/galaxy-a/galaxy-a06-black-64gb-sm-a065fzkdafb/
- Samsung Galaxy A16: https://www.samsung.com/africa_en/smartphones/galaxy-a/galaxy-a16-black-128gb-sm-a165fzkdafb/
- TECNO SPARK 40: https://www.tecno-mobile.com/et-en/phones/tech-specs/techspecs/spark-40/
- TECNO SPARK Go 2: https://www.tecno-mobile.com/phones/tech-specs/techspecs/spark-go-2/
- Infinix SMART 10: https://infinixmobiles.in/pages/smart-10-specs
- Samsung Galaxy M13 launch specification: https://news.samsung.com/in/samsung-india-launches-galaxy-m13-series-with-ram-plus-auto-data-switching

### 3.1 Platform floor and API levels

- Proposed minimum supported Android version: Android 10 / API 29. This is a provisional security/compatibility floor, not a verified Ethiopian OS-distribution conclusion.
- Proposed Play target: Android 16 / API 36, consistent with the requirement effective 31 August 2026 for new apps and updates.
- Test OS/API coverage: API 29, 31/32, 34, 35 and 36, using physical devices where available and emulators for supplemental compatibility checks.
- Reassess the minimum version against an actual Ethiopia field sample before A2 closure. Do not lower the security floor solely to accommodate an unmeasured legacy tail.

### 3.2 Device record required for every benchmark

Record exact model code/SKU and country variant, physical RAM, storage capacity and free space, chipset, GPU, Android version/API, OEM skin/build fingerprint, security patch level, display resolution/density, battery health, thermal state, camera source/resolution, and whether the device uses Android Go or memory extension.

A model name alone is insufficient: variants can differ in RAM, SoC, modem, firmware and camera sensor.

### 3.3 Feature admission by tier

- All tiers: import, crop, rotate, resize, standard color/exposure adjustments, text/frames, save/reopen, print layout, export and offline operation.
- 3 GB tier: tiled/downsampled working images; bounded undo history; no requirement for local segmentation or local model inference. If a local CV operation exceeds the memory/latency gate, show a clear unavailable state or use a safe deterministic fallback.
- 4 GB tier: benchmark optional local face framing and segmentation; enable only when the exact engine/model passes quality, license and resource gates.
- 6 GB+ tier: benchmark larger images and batch workflows; this tier does not automatically authorize an on-device model.
- No tier may silently upload an image to compensate for local memory or model failure.

## 4. Performance and reliability test profile

The existing A2 technical proposal's targets remain provisional:

| Operation | Provisional target |
|---|---:|
| 12 MP import to usable preview | p95 ≤ 3 seconds |
| Common preview adjustment | p95 ≤ 150 ms |
| 12 MP JPEG export | p95 ≤ 8 seconds |
| Peak app memory | ≤ 35% of physical device RAM |
| Repeated export stability | 100 consecutive exports without OOM |
| 24 MP processing | Tiled success or explicit safe failure |
| Core local workflow | Complete in airplane mode |
| Thermal/battery | 20-export sustained test, record temperature, throttling and battery delta |

These are acceptance targets, not achieved results. Measure cold and warm runs, median/p90/p95, peak PSS/RSS, GC pauses, dropped frames, OOMs, storage growth, CPU/GPU utilization, thermal throttling and battery delta. Repeat each test on at least three physical units per tier where possible; record firmware and environment.

A 3 GB physical-RAM device has a tighter absolute budget than a 6 GB device. The application must use Android low-memory signals, bounded decode sizes, tiled rendering, explicit bitmap lifecycle management and reduced-resolution previews. Extended RAM is not a substitute for a real memory budget.

## 5. Aki Image Quality Evaluation Corpus (AIQEC) — pilot specification

### 5.1 Corpus purpose and scope

The corpus evaluates face framing, background segmentation, image-quality assessment and deterministic image-edit integrity. It is not an identity-recognition dataset and must not be used to identify people, create face embeddings, build a face database or train a biometric identification system.

The corpus does not establish official passport or visa compliance. Each official photo preset still requires its own jurisdiction, authoritative source, dimensions, effective date and verification status.

### 5.2 Pilot size and capture plan

Proposed pilot minimum:

- 500 consenting adult volunteers, age 18 or older.
- 50 participants in each of ten Monk Skin Tone (MST) visual bands, recorded only for the approved fairness-evaluation purpose.
- Five images per participant, approximately 2,500 portrait images.
- 500 hard-negative images without a single usable frontal face.
- Total pilot target: approximately 3,000 images.

The five portrait captures should cover:
1. Neutral, frontal, evenly lit head-and-shoulders capture on a plain background.
2. Frontal capture with a different background and realistic background clutter.
3. Mild pose variation and natural expression variation.
4. Glasses, facial hair, hair styles/textures, or head covering where voluntarily represented.
5. Challenging but realistic lighting, shadow, exposure, blur or camera-quality condition.

Capture with multiple physical devices and camera classes, retaining original resolution and recording camera/device metadata that is necessary for reproducibility. Avoid deliberately degrading the source beyond the defined quality-condition capture.

The 500 hard negatives should cover no-face scenes, multiple faces, partial faces, printed photographs/screens, mannequins or face-like objects, reflections/shadows and other false-positive conditions.

### 5.3 Representation and annotation

Use voluntary, purpose-specific participation. MST is a visual skin-tone evaluation label only; do not infer or record ethnicity from an image. Record other representation attributes only when necessary, voluntarily supplied or reviewed under the consent protocol, and never use them to infer identity.

For each image, annotate:

- Face bounding box and face count.
- Eye centers and approved facial landmarks needed for framing.
- Pose (yaw/pitch/roll), occlusion and glasses/head-covering flags.
- Lighting, exposure, blur, noise, compression and resolution condition.
- Hair, ear, glasses, shoulder and background boundaries for segmentation.
- Background category and edge difficulty.
- Capture device/SKU, camera, image dimensions, orientation and source digest.

Use two independent annotators for the gold-standard test subset. Resolve disagreements through a documented adjudication process. Store annotation version, annotator IDs (pseudonymous), tool version and adjudication status. Do not store participant names in image filenames or EXIF.

### 5.4 Dataset separation

Split by participant, never by image, to prevent the same person's face appearing in development and locked evaluation partitions.

Provisional pilot split:
- 60% development: 300 participants.
- 20% validation: 100 participants.
- 20% locked test: 100 participants.

The locked pilot subset is too small to support strong claims for every skin-tone subgroup. Use it to discover failure modes and establish measurement repeatability only. Before a release-grade subgroup claim, perform a statistical power calculation and enlarge the locked set so that each reported subgroup has enough independent participants and a sufficiently narrow 95% confidence interval. Repeated images of one person do not count as independent people.

Keep test labels and gold annotations access-controlled. Any model tuning, threshold selection or prompt/model changes must use development/validation data; the locked test is opened only for a recorded evaluation.

### 5.5 Privacy, consent and source controls

- Collect only from adults who provide explicit informed consent in a language they understand, including Amharic and English.
- Explain purpose, capture conditions, access, storage, retention, withdrawal and whether any external processor is involved.
- Do not collect passport scans, identity cards, real application documents, names or unnecessary contact information.
- Store participant contact/consent records separately from pseudonymous image IDs.
- Encrypt corpus storage and backups; apply least-privilege access, access logging and a deletion/withdrawal process.
- No public dataset upload, external model-provider upload, training, fine-tuning or secondary use without a separate explicit approval and consent basis.
- No web-scraped faces or datasets with unclear consent, license or provenance.
- Do not collect images of minors in this pilot. Any future child-image evaluation requires a separate safeguarding, legal and consent review.
- Delete withdrawn participants' images, annotations and derived copies under a documented deletion workflow, subject to reviewed legal/records obligations.

### 5.6 Proposed image-quality acceptance metrics

| Capability | Metric / provisional threshold | Reporting requirement |
|---|---|---|
| Face detection | Recall ≥ 98%; false-positive rate ≤ 1% on hard negatives | Overall and by lighting, pose, device and MST band, with 95% confidence intervals |
| Framing landmarks | Eye-line / placement error ≤ 2% of face height on supported frontal/near-frontal subset | Report normalized error distribution and unsupported-pose rate |
| Background segmentation | Median IoU ≥ 0.90; 10th percentile IoU ≥ 0.80 | Overall and by hair, glasses, ears, shoulders, head covering and background class |
| Boundary quality | Boundary F-score target ≥ 0.85 on annotated difficult edges | Separate hair/ear/glasses/shoulder results |
| Confidence behavior | Low-confidence or out-of-domain inputs must trigger user review or safe fallback | Measure calibration and false confidence, not just average score |
| Official-document integrity | No identity-altering generative edits; preserve source and user-approved geometry | Adversarial and human review; no official-compliance claim without preset provenance |
| Image integrity | Original SHA-256 unchanged; exact output dimensions; no unintended aspect distortion | Deterministic fixtures and independent decoder checks |
| Print integrity | Correct physical dimensions at declared DPI and stable PDF/JPEG output | Pixel rounding, page placement, margin and cut-guide fixtures |

All thresholds are provisional. Report confidence intervals, sample counts, failures and exclusions. A high aggregate score cannot conceal a weak subgroup or difficult-condition result. If a subgroup sample is too small, report it as insufficient evidence rather than PASS.

## 6. Corpus and device acceptance gates

Before the corpus or matrix is accepted as a release baseline:

1. Confirm the Ethiopia device shortlist using a small local field sample, including exact SKUs and firmware.
2. Acquire or borrow the physical devices; record the device inventory before benchmarking.
3. Confirm the Android minimum API and Play target policy.
4. Approve the adult participant consent, privacy, retention and withdrawal protocol before collecting images.
5. Create annotation guidelines and inter-annotator agreement checks.
6. Freeze a versioned dataset manifest with image digests, labels, split membership, provenance and rights status.
7. Pre-register metric definitions, confidence intervals, subgroup reporting and stop conditions.
8. Run benchmarks only against exact engine/model versions and exact app commits; preserve raw test output.
9. Keep any model/provider or dataset license decision separate from this evaluation plan.

## 7. Current status

| Item | Status |
|---|---|
| Ethiopia device shortlist | PROPOSED from current third-party usage sample |
| Physical RAM tier definitions | ESTABLISHED PROPOSAL; SKU verification required |
| Android minimum API 29 | PROPOSED; field validation required |
| Play target API 36 | POLICY REQUIREMENT for new apps/updates from 31 August 2026 |
| Physical device acquisition | NOT DONE |
| Device benchmarks | NOT RUN |
| Adult volunteer corpus | NOT COLLECTED |
| Gold annotations | NOT CREATED |
| Image-quality metrics | PROVISIONAL TARGETS |
| A4 verification | NOT STARTED |
| A3 implementation | NOT AUTHORIZED |
| A5 deployment/release | NOT AUTHORIZED |
| Expenditure | NOT AUTHORIZED |

**Immediate next A2 priority:** validate the shortlist against real Ethiopian device SKUs and complete the privacy/consent and sample-size plan before any participant image collection. No device purchase or paid dataset service is implied or authorized.

---

**End of AKIDS-A2-DEVICE-IMAGE-EVAL-001 v0.1.0.**