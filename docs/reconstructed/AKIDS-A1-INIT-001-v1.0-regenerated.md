# AKIDS-A1-INIT-001 v1.0 — Product Inception & Architecture Specification
## Regenerated archival reconstruction

| Field | Value |
|---|---|
| Record ID | AKIDS-A1-INIT-001 |
| Baseline version | 1.0 |
| Record type | Regenerated archival reconstruction |
| Original approval | APPROVED — 26 November 2026 |
| Owner | Akefa Ayele Daka |
| A1 baseline disposition | VERIFIED-CLOSED |
| Reconstruction date | 26 November 2026 |
| A3 / deployment / expenditure authority | NOT GRANTED |

> **Provenance notice:** The original assistant-authored message body could not be recovered from available chat-history retrieval. This document is therefore regenerated from surviving conversation summaries, approval metadata, and the previously committed A1 archival transcription. It is a fuller reconstruction of the approved A1 baseline, not a verbatim export, signed original, or proof that every sentence below appeared in the original message. The historical approval remains the approval of A1 v1.0; this reconstruction does not create a new approval or reopen the closed baseline.

## 1. Executive summary

Aki Studio is an Android-first professional mobile photography and image-editing studio. It enables users, small photo studios, and print operators to prepare identity photographs, passport photographs, portraits, edited images, and print-ready layouts from an Android device.

The product is intended to be:
- Amharic-first, with English support;
- offline-first and privacy-first;
- usable on lower-spec Android devices;
- affordable through a freemium product model;
- capable of professional, print-ready output;
- non-destructive, preserving original photographs;
- designed to preserve genuine identity, especially in official-document photographs.

**Product descriptor:** ID • Passport • Portrait • Edit • Print

**Tagline:** A New Way to Create.

**Supporting line:** Rooted in Maale. Designed for the world.

## 2. Product and brand identity

### 2.1 Brand hierarchy
- Parent studio: **Aki Digital Studio**.
- Product: **Aki Studio**.
- Historical product concept/name in the A1 baseline: **AkiDS Studio**.
- Earlier ID-photo concept: **AkiDS ID Photo**, retained as a feature/module concept rather than the name of the complete product.
- Private intelligence backbone: **IROKA**, not a customer-facing product brand.

The word **Aki** means “new” in Maale. The later clarification of Aki Digital Studio as the parent studio and Aki Studio as the public product must be preserved as controlled brand history; it must not erase the historical A1 naming.

### 2.2 Product positioning
A professional, accessible mobile photo studio that combines identity-photo preparation, portrait editing, design, and print layout in one Android application.

### 2.3 Intended users
- Individuals preparing passport, visa, ID, school, employee, and other formal photographs.
- Local photographers and small photo studios.
- Users who need portrait correction, creative editing, frames, or print layouts.
- Small businesses needing consistent headshots, branded photographs, or repeatable photo output.

These are target-user categories, not evidence of validated market research.

## 3. Product objectives

1. Make common identity-photo and print-preparation workflows accessible from Android.
2. Provide accurate framing, crop, size, and layout tools.
3. Provide useful portrait and image editing without destroying the original.
4. Support background removal and replacement with manual correction.
5. Produce predictable physical print layouts and export files.
6. Work offline for core functions and avoid mandatory accounts/cloud services.
7. Provide a usable Amharic-first experience and English localization.
8. Scale from individual use to small photo-studio workflows.
9. Keep AI recommendations bounded by user authorization.
10. Maintain a replaceable, privacy-conscious intelligence architecture.

## 4. Functional scope — ten product domains

### P01 — ID & Passport Studio
**Purpose:** Prepare identity photographs using selected physical dimensions and framing guidance.

Capabilities:
- Passport, visa, national ID, driving licence, employee ID, student ID, and configurable custom formats.
- Face detection and positioning guides.
- Head-size and eye-line guidance where technically supported.
- Centering, crop, aspect ratio, and background checks.
- Multiple copies on a print sheet.
- Preset selection and custom dimensions.
- Quality warnings for blur, exposure, pose, framing, or image resolution.

Controls:
- Official presets must carry jurisdiction, issuing authority/source, dimensions, effective date, last verification date, and verification status.
- A preset with no authoritative evidence must be marked unverified.
- The application must not claim legal or official compliance merely because a preset exists.
- The user must be able to inspect and adjust the crop.
- Official-photo workflows must not apply identity-altering beauty filters or facial reshaping.

### P02 — AI Background Studio
**Purpose:** Remove, replace, and refine image backgrounds.

Capabilities:
- Subject/person segmentation.
- Background removal and replacement.
- Solid-color, custom-image, and transparent backgrounds.
- Edge refinement, hair-detail refinement where supported, feathering, and mask editing.
- Manual erase and restore tools.
- Shadow and foreground/background blending options.
- Transparent PNG export where supported.

Controls:
- Show preview before committing.
- Preserve the original.
- Provide manual correction when automatic segmentation is imperfect.
- Do not imply that segmentation is perfect across all hair, clothing, lighting, or complex backgrounds.

### P03 — Portrait Enhancement
**Purpose:** Improve portrait appearance while retaining a natural result.

Capabilities:
- Exposure and lighting correction.
- Blemish and spot correction.
- Red-eye correction.
- Teeth whitening and eye clarity as optional creative adjustments.
- Sharpening and noise reduction.
- Natural skin correction and controlled retouching.

Controls:
- User-adjustable intensity and reversible edits.
- Preserve identity and natural facial characteristics.
- Clearly separate creative retouching from official ID/passport processing.
- No automatic facial identity recognition or identity database.

### P04 — Advanced Photo Editor
**Purpose:** Provide general-purpose editing controls.

Capabilities:
- Crop, rotate, flip, straighten, and perspective correction.
- Exposure, brightness, contrast, highlights, shadows, whites, and blacks.
- Saturation, vibrance, hue, temperature, tint, and color balance.
- RGB controls, curves, and levels.
- Sharpening, blur, and noise reduction.
- Layers, masks, blending, text, shapes, and overlays.
- Undo/redo and edit history.
- Non-destructive edit instructions and project state.

Controls:
- Preserve the source image.
- Separate low-resolution preview from full-resolution output.
- Provide predictable export dimensions and format handling.
- Prevent destructive edits from silently replacing the original.

### P05 — Frames & Design Studio
**Purpose:** Create designed photographs and composed visual outputs.

Capabilities:
- Formal, passport, graduation, wedding, family, and business frame categories.
- Borders, backgrounds, overlays, text, typography, logos, and watermarks.
- Reusable templates and custom designs.
- Position, size, rotation, color, and opacity controls.

Controls:
- Templates must be editable.
- User-supplied logos and assets remain user-controlled.
- Any bundled third-party assets require license review before distribution.

### P06 — Print Studio
**Purpose:** Prepare photographs for physical printing.

Capabilities:
- Physical dimensions and custom sizes.
- 300-DPI output option where source resolution supports it.
- Automatic arrangement of repeated photographs.
- Copy counts, spacing, margins, orientation, and cut guides.
- Print preview.
- A4 and custom paper/layout sizes.
- JPEG, PNG, and PDF export.

Initial size catalogue:
- 3 × 4 cm
- 3.5 × 4.5 cm
- 4 × 5 cm
- 5 × 7 cm
- 10 × 15 cm
- 15 × 20 cm
- 20 × 25 cm
- 20 × 30 cm
- 30 × 40 cm
- Custom dimensions

Reference pixel dimensions at 300 DPI (rounded):
| Print size | Pixel dimensions |
|---|---:|
| 3 × 4 cm | 354 × 472 |
| 3.5 × 4.5 cm | 413 × 531 |
| 4 × 5 cm | 472 × 591 |
| 5 × 7 cm | 591 × 827 |
| 10 × 15 cm | 1181 × 1772 |
| 15 × 20 cm | 1772 × 2362 |
| 20 × 25 cm | 2362 × 2953 |
| 20 × 30 cm | 2362 × 3543 |
| 30 × 40 cm | 3543 × 4724 |

Print rules:
- Never stretch faces or distort aspect ratio.
- Distinguish crop-to-fill from fit-with-padding.
- DPI metadata does not add image detail.
- Warn when source resolution is insufficient for the requested print size.
- Validate physical dimensions, rounding, PDF page sizing, and printer scaling during later verification.

### P07 — Portrait Studio
**Purpose:** Support portrait-oriented creation workflows.

Capabilities:
- Business and professional headshots.
- Graduation portraits.
- Family and group portraits.
- Formal portraits.
- Creative portrait looks and compositions.
- Reusable portrait presets and output profiles.

### P08 — Project Management
**Purpose:** Manage source images and editable work.

Capabilities:
- Local projects and editable sessions.
- Original-image preservation.
- Edit history and undo/redo.
- Duplicate project and save-as workflows.
- Batch processing where device resources permit.
- User-controlled storage, export, and deletion.

### P09 — AI Intelligence
**Purpose:** Provide intelligent assistance across photo workflows.

Capabilities:
- Image-quality assessment.
- Framing and crop recommendations.
- Background and edge-quality assessment.
- Suggested correction workflows.
- Editing recommendations and guided actions.
- Passport/ID photo assessment against selected, sourced presets.

Controls:
- Recommendations are advisory until the user accepts them.
- The application remains responsible for validating inputs and applying edits.
- AI assessment must disclose uncertainty and must not claim official acceptance.
- IROKA is a private backend capability, not a customer-facing brand.

### P10 — Localization & Preferences
**Purpose:** Make the product adaptable and accessible.

Capabilities:
- Amharic-first interface and English language option.
- Metric and imperial units.
- User preferences for editing and export.
- Accessibility settings and readable controls.
- Persistent preferences with user control.

## 5. User workflow direction

### 5.1 Identity-photo workflow
1. Open ID & Passport Studio.
2. Select a sourced preset or custom dimensions.
3. Capture or import an image.
4. Receive framing and quality guidance.
5. Adjust crop, background, and permitted corrections.
6. Review the result.
7. Arrange one or more copies in Print Studio.
8. Preview and export the print file.

### 5.2 General editing workflow
1. Import or capture a photograph.
2. Preserve the original as the source asset.
3. Create an editable project.
4. Apply reversible edits and optional AI recommendations.
5. Preview at working resolution.
6. Render at export resolution.
7. Save or share the chosen output.

### 5.3 Studio/batch workflow
1. Select or create a reusable preset.
2. Import multiple images.
3. Apply controlled operations.
4. Review exceptions and quality warnings.
5. Generate print sheets or individual exports.
6. Save projects locally.

Batch automation must not bypass review where a workflow can materially alter identity photographs.

## 6. Technical direction recorded at A1

The A1 direction is a native Android application, not a web wrapper.

Candidate technologies discussed:
- Kotlin and Android SDK.
- Jetpack Compose for UI.
- CameraX for camera workflows.
- OpenCV or equivalent validated image-processing libraries.
- On-device ML capabilities for supported detection/segmentation tasks.
- A replaceable inference runtime and GPU acceleration where available.
- Room/SQLite for local project metadata.
- Android storage/document APIs.
- PDF generation and image export facilities.

Architecture principles:
- Separate UI, project/edit model, image-processing engine, intelligence integration, and export.
- Keep preview rendering responsive and separate from full-resolution export.
- Represent edits as non-destructive instructions where feasible.
- Use tiled or memory-conscious processing for large images.
- Support lower-spec Android devices with capability-aware processing.
- Make AI models and providers replaceable.
- Keep core workflows functional offline.

These are candidate directions, not a final technology selection or evidence of implementation. A2 must validate SDK compatibility, device requirements, dependency/model licenses, performance, security, and integration details.

## 7. Freemium product direction

### AkiDS Free
- Essential ID/passport formats.
- Standard backgrounds.
- Crop and resize.
- Basic image adjustments.
- Basic print sheets.
- JPEG export.

### AkiDS Pro
- Advanced background removal.
- Professional portrait retouching.
- Advanced color controls, curves, and levels.
- Layers and masks.
- Premium frames and effects.
- High-quality export options.
- Batch processing.
- Advanced print layouts.

### AkiDS Studio Business
- Batch production workflows.
- Reusable studio presets.
- Client/project organization.
- Custom print templates.
- Studio branding and watermarks.
- Commercial studio workflows.

Commercial details:
- Pricing and payment provider are undecided.
- No paid service, payment integration, subscription, or financial commitment is approved by A1.
- Core basic workflows should not require internet access or a subscription.
- Entitlement boundaries must be designed without compromising local data ownership.

## 8. Privacy, security, and trust principles

- Local processing by default.
- No mandatory user account for core editing.
- No automatic cloud upload.
- Explicit informed consent before optional remote processing.
- Disclose what image data is sent, why, to which provider, and for how long.
- Minimize transmitted data.
- Secure temporary files and user-controlled deletion.
- Preserve original images.
- No unnecessary facial recognition or identity database.
- No default cross-product data sharing.
- Keep logs free of image content and sensitive personal information unless strictly necessary and consented.
- Keep customer experience and authorization under Aki Studio control.

## 9. Quality attributes and constraints

The following are product requirements to be quantified in A2/A4, not claims that they have already been tested:
- Offline availability for core editing and export.
- Responsive preview and editing on supported lower-spec devices.
- Memory-safe handling of high-resolution photographs.
- Export dimensions and physical print sizing that are deterministic.
- Reversible editing and source preservation.
- Clear error and recovery behavior.
- Amharic and English localization.
- Accessible UI controls.
- No silent image upload or autonomous edit application.
- Replaceable AI models and processing engines.

## 10. Acceptance direction

A future implementation should demonstrate, at minimum:
1. User can import/capture an image and preserve the original.
2. User can create and reopen a local editable project.
3. Crop, rotate, resize, and core adjustments work without distorting the source.
4. ID-photo framing and dimension presets are traceable to documented sources or marked unverified.
5. Background removal includes manual correction.
6. Official-photo workflow does not apply identity-altering retouching.
7. Print layouts preserve aspect ratio and physical sizing.
8. Export works offline for core workflows.
9. Remote intelligence is optional, consent-gated, and failure-tolerant.
10. Amharic and English interfaces are usable.
11. Low-memory behavior and large-image export are tested on declared target devices.
12. User can delete projects and temporary assets under documented retention rules.

Numerical performance thresholds, device matrix, and test evidence remain to be defined in A2/A4.

## 11. Risks and dependencies

- Incorrect or outdated official photo requirements.
- Inaccurate segmentation and face-framing recommendations.
- Low-memory crashes or slow full-resolution rendering.
- Model/dependency licensing restrictions.
- Privacy exposure from remote processing.
- Printer/PDF scaling inconsistencies.
- Inadequate localization or accessibility.
- Overly aggressive retouching that alters identity.
- Freemium design that makes essential workflows dependent on paid services.

## 12. Governance, approval, and stage boundary

The surviving approval record identifies:
- Owner: Akefa Ayele Daka.
- Record: AKIDS-A1-INIT-001 v1.0.
- Decision: APPROVED.
- Date: 26 November 2026.
- Baseline status: VERIFIED-CLOSED.

A1 approval applies to the product inception baseline. It does not authorize:
- A3 implementation;
- A4 verification work as a project stage;
- A5 deployment or release;
- purchases, paid services, subscriptions, or expenditure.

A2 is a separate technical reconciliation stage. Any material change to the approved baseline must be controlled through an addendum or new version, with the original preserved.

## 13. Regeneration and provenance statement

This reconstruction consolidates the surviving A1 archival transcription and recoverable project context into a fuller specification. It is prepared to avoid making the Owner recreate assistant-authored work. Exact original wording, original message IDs, and original source-file bytes were not retrievable. No SHA-256 checksum is claimed for the missing original.

**End of regenerated archival reconstruction — AKIDS-A1-INIT-001 v1.0.**
