# AKIDS-A1-INIT-001 — Aki Studio Product Inception Baseline

| Field | Value |
|---|---|
| Document ID | AKIDS-A1-INIT-001 |
| Version | 1.0 |
| Stage | A1 — Assessment, inception, and product specification |
| Owner | Akefa Ayele Daka |
| Approval date | 26 November 2026 |
| Decision | APPROVED |
| Baseline status | VERIFIED-CLOSED |
| Implementation authority | NOT GRANTED |
| Expenditure authority | NOT GRANTED |

## 1. Record provenance and archival status

**Archival transcription — not a verbatim original artifact.**

This file records the approved A1 baseline and the product scope recoverable from the surviving project approval history. The complete original A1 document file, including its exact original wording and full approval block, was not available when this repository archive was prepared.

Accordingly:
- This record preserves the known document identifier, version, Owner, approval date, decision, closure status, and approved product direction.
- Product capabilities below are a structured archival summary of the approved A1 scope, not a claim that every sentence reproduces the original document verbatim.
- This file is not a digital signature, signed original, or cryptographically authenticated copy.
- If the original A1 v1.0 artifact is recovered, it must be added unchanged with provenance and integrity metadata. This transcription must remain available as an audit trail.

## 2. Owner decision and baseline disposition

The Owner approved the Aki Studio product inception baseline as **AKIDS-A1-INIT-001 v1.0** on **26 November 2026**.

The A1 baseline is **VERIFIED-CLOSED** as the approved product inception baseline. The Owner clarified the studio identity as **Aki Digital Studio**. That clarification is associated with the baseline and its subsequent brand hierarchy; it does not silently rewrite or replace the historical A1 v1.0 record.

A1 approval authorizes the product direction and baseline only. It does not authorize:
- A3 software implementation;
- deployment or release;
- purchases, subscriptions, paid services, or other expenditure;
- external cloud commitments or financial obligations.

## 3. Product identity

- **Parent studio:** Aki Digital Studio.
- **Public product:** AkiDS Studio, subsequently refined as the public product identity **Aki Studio** through a controlled brand clarification.
- **Original concept name:** AkiDS ID Photo, retained as an ID/passport module concept rather than the name of the complete product.
- **Meaning:** Aki is a Maale word meaning “new”.
- **Product descriptor:** ID • Passport • Portrait • Edit • Print.
- **Product category:** Professional mobile photography and image-editing studio.
- **Initial platform:** Android.
- **Business model direction:** Freemium.
- **Languages:** Amharic and English.

The brand clarification is not a reason to erase the original product naming history.

## 4. Product vision

Enable people to create professional-quality ID/passport photographs, portraits, edited images, and print-ready designs using an Android phone.

The product direction emphasizes:
- accessible professional-quality results;
- privacy-first and offline-first operation;
- affordability and usefulness on lower-spec Android devices;
- reversible, non-destructive editing;
- preservation of genuine personal identity;
- Amharic-first product experience with English support.

## 5. Approved functional scope

The A1 product scope is organized into ten functional domains.

### P01 — ID & Passport Studio

Support passport, visa, national ID, driving licence, employee/student ID, and configurable custom photo formats. Intended capabilities include framing guides, head-size measurement, eye-line alignment, face centering, quality checks, and multiple copies per print sheet.

Official-document presets must identify jurisdiction, source, dimensions, effective date, and verification status. Unverified presets must not be represented as officially compliant.

### P02 — AI Background Studio

Support person segmentation, background removal and replacement, solid colors, custom backgrounds, edge refinement, masks, manual erase/restore, shadows, and transparent PNG output.

### P03 — Portrait Enhancement

Support natural-looking skin and image corrections, including blemish correction, lighting, red-eye correction, teeth whitening, eye clarity, sharpening, and noise reduction. Preserve natural identity. Creative retouching must be distinguished from official-document processing.

### P04 — Advanced Photo Editor

The intended editing scope includes crop, rotate, flip, perspective correction, exposure, brightness, contrast, highlights/shadows, saturation, vibrance, hue, color balance, temperature/tint, RGB controls, curves, levels, sharpening, blur, noise reduction, layers, masks, blending, text/overlays, and undo/redo/history.

Editing should be non-destructive and preserve the original image.

### P05 — Frames & Design Studio

Support formal, passport, graduation, wedding/family, and business frames; borders, overlays, text, typography, logos, watermarks, and reusable templates.

### P06 — Print Studio

Support physical photo dimensions, 300-DPI export options, automatic photo arrangement, copy counts, margins, spacing, cut guides, orientation, print preview, JPEG/PNG/PDF, A4, and custom layouts.

### P07 — Portrait Studio

Support business headshots, graduation, family, formal, and creative portrait workflows.

### P08 — Project Management

Support original-image preservation, editable projects, history, duplication, batch processing, and local project storage.

### P09 — AI Intelligence

Support image-quality scoring, framing assistance, automatic-correction proposals, and editing recommendations. Recommendations must remain under Aki Studio-controlled user authorization.

### P10 — Localization & Preferences

Support Amharic and English, metric/imperial units, accessibility, and user preferences.

## 6. Initial print-size catalogue

The initial proposed catalogue includes:
- 3 × 4 cm;
- 3.5 × 4.5 cm;
- 4 × 5 cm;
- 5 × 7 cm;
- 10 × 15 cm;
- 15 × 20 cm;
- 20 × 25 cm;
- 20 × 30 cm;
- 30 × 40 cm;
- custom dimensions.

At 300 DPI, the previously documented example pixel dimensions are:

| Physical size | Approximate pixels |
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

Crop and resize must be distinguished; faces must never be stretched. A DPI label does not create image detail, and source resolution must be sufficient. Physical-size calculations and rounding require future formal testing.

## 7. Technical direction recorded at A1

The initial direction was a native Android application rather than a web wrapper. Candidate technologies included Kotlin, Android SDK, Jetpack Compose, CameraX, OpenCV, supported on-device ML capabilities, replaceable inference runtimes, GPU-accelerated rendering, Room/SQLite, Android local storage and document APIs, and PDF generation facilities.

The architecture direction called for:
- separation of interactive preview rendering from full-resolution export;
- non-destructive edit instructions and project documents;
- tile- and memory-efficient processing for large images and lower-spec devices;
- replaceable AI models and processing components.

These were A1 candidate directions, not a finalized or implemented technology stack. Detailed compatibility, licensing, security, and performance validation belongs to A2.

## 8. Freemium business-model direction

- **AkiDS Free:** essential ID/passport formats, standard backgrounds, crop/resize, basic adjustments, basic print sheets, and JPEG export.
- **AkiDS Pro:** advanced background removal, professional retouching, advanced color controls, curves/levels, layers/masks, premium frames/effects, high-quality export, batch processing, and advanced print layouts.
- **AkiDS Studio Business:** batch production, reusable studio presets, client project management, custom print templates, business branding/watermarks, and commercial workflows.

Pricing, payment providers, and payment implementation were left undecided and require future approval. Core basic workflows should not require internet access or a subscription.

## 9. Privacy and security principles

- Local processing by default.
- No mandatory account.
- No automatic cloud uploads.
- Explicit informed consent for optional online processing.
- Secure handling of temporary images and user-controlled deletion.
- Preserve original photos.
- No unnecessary facial identification or recognition databases.
- No default cross-product data sharing.
- Clear disclosure when images are transmitted to external providers.
- Preserve genuine facial characteristics in official ID photographs.

## 10. Stage-gate and change-control record

| Stage | A1 baseline disposition |
|---|---|
| A1 — Assessment/specification | APPROVED; VERIFIED-CLOSED |
| A2 — Technical reconciliation | Separate stage; not closed by A1 approval |
| A3 — Implementation | NOT AUTHORIZED |
| A4 — Verification | NOT STARTED |
| A5 — Deployment/release | NOT AUTHORIZED |

Preserve A1 v1.0 as the historical inception baseline. Later changes must be recorded as controlled addenda or revisions with traceable decisions and approvals. Do not silently replace this baseline.

## 11. Approval history

| Date | Actor | Event | Result |
|---|---|---|---|
| 26 November 2026 | Akefa Ayele Daka, Owner | Approved AKIDS-A1-INIT-001 v1.0 | APPROVED |
| 26 November 2026 | Owner | Closed A1 product baseline | VERIFIED-CLOSED |
| Subsequent clarification recorded in project history | Owner | Clarified Aki Digital Studio as parent studio identity; retained product naming history | Associated clarification; no silent overwrite |

**Approval evidence limitation:** the approval is recorded in the surviving ChatGPT project conversation history. A signed source artifact, signature file, or independent approval-system record was not available for this archival transcription.

---

**End of archival transcription — AKIDS-A1-INIT-001 v1.0**

*This document records the approved baseline; it does not grant implementation, deployment, or expenditure authority.*
