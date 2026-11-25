# Aki Studio

**A New Way to Create.**

Aki Studio is a planned Amharic-first, privacy-first Android creative photography studio. It is intended to help people create professional ID and passport photos, portraits, edited images, and print-ready designs using an Android phone.

The name **Aki** comes from the Maale language and means **“new”**—a foundation for a brand centered on fresh beginnings, creativity, confidence, and possibility.

## Product vision

Make professional-quality photography and image creation accessible, practical, and trustworthy, including on lower-spec Android devices and in offline settings.

## Planned product capabilities

- **ID & Passport Studio** — guided framing, photo dimensions, and configurable document-photo templates.
- **AI Background Studio** — background removal, replacement, and refinement.
- **Portrait Enhancement** — natural-looking image corrections that preserve personal identity.
- **Advanced Photo Editor** — crop, color, adjustments, layers, masks, and non-destructive editing.
- **Frames & Design Studio** — layouts, typography, overlays, and reusable designs.
- **Print Studio** — physical-size layouts, copy arrangements, and print-ready exports.
- **Portrait Studio** — personal, family, graduation, and professional portraits.
- **Project Management** — locally managed projects, originals, and editing history.
- **AI-Assisted Tools** — optional quality assessment and editing recommendations.
- **Localization & Accessibility** — Amharic and English, accessible controls, and configurable units.

These are planned capabilities, not claims of currently implemented functionality.

## Product principles

1. **Privacy first:** local processing by default; no mandatory account or automatic cloud upload.
2. **Offline first:** core photography, editing, and print workflows should remain useful without an internet connection.
3. **Identity preservation:** official-document workflows must preserve genuine facial characteristics.
4. **Non-destructive editing:** preserve the original image and keep edits reversible.
5. **Honest compliance:** document-photo presets must identify their jurisdiction, source, effective date, and verification status. Unverified presets must not be represented as officially compliant.
6. **Accessible performance:** design for a range of Android device capabilities, including constrained memory and processing resources.
7. **Transparent intelligence:** disclose optional external processing and obtain informed consent before transmitting images.

## Brand and architecture

- **Parent studio:** Aki Digital Studio
- **Public product:** Aki Studio
- **Private AI backbone:** IROKA
- **Internal integration boundary:** AkiDS Intelligence Gateway

Aki Studio owns the customer experience, image data boundaries, editing workflow, and rendering decisions. IROKA is a private intelligence backbone and is not a customer-facing product brand. Specialized image-processing and rendering components perform pixel-level operations; AI recommendations do not apply changes without Aki Studio-controlled authorization.

## Project governance and current status

This repository is the public project home and documentation starting point.

| Workstream | Status |
|---|---|
| A1 product inception baseline (AKIDS-A1-INIT-001 v1.0) | Owner-approved; baseline closed |
| A2 IROKA integration architecture decision (AKIDS-A2-ADR-001) | Owner-approved |
| Full A2 technical assessment and reconciliation | In progress; not closed |
| A3 implementation | Not authorized |
| A4 verification | Not started |
| A5 deployment/release | Not authorized |

Approval of the A1 baseline and A2 architecture decision does not authorize implementation, deployment, purchases, subscriptions, or other expenditure. Any such work requires separate explicit Owner authorization.

Approved baselines must be preserved. Subsequent decisions and changes are to be recorded as controlled addenda or revisions rather than silently overwriting earlier approvals.

## Platform and technology

The initial target is **Android**. Candidate technologies and dependencies—including native Android/Kotlin, Jetpack Compose, CameraX, computer-vision libraries, on-device model runtimes, and GPU-accelerated rendering—remain subject to A2 technical, compatibility, licensing, security, and performance assessment. This README does not represent a finalized implementation stack.

## Documentation

Architecture specifications, decision records, requirements, threat assessments, and verification criteria will be added as they are prepared and approved. No implementation status should be inferred from planned documentation.

## Project ownership

**Owner:** Akefa Ayele Daka  
**Studio:** Aki Digital Studio

---

*Rooted in Maale. Designed for the world.*
