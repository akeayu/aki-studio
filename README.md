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
| A3 local Android MVP | In progress on `feat/a3-local-android-mvp`; not merged |
| A4 verification | Not started; no Android build/device test evidence yet |
| A5 deployment/release | Not authorized |

The Owner subsequently authorized A3 implementation of the local-only Android MVP on 26 November 2026. This authorization excludes network/cloud runtime services, merge to main, deployment/release, purchases, subscriptions, and other expenditure.

Approved baselines must be preserved. Subsequent decisions and changes are to be recorded as controlled addenda or revisions rather than silently overwriting earlier approvals.

## Platform and technology

The initial target is **Android**. The A3 local MVP uses native Kotlin and Android platform APIs, with an optional LiteRT CPU interpreter adapter for a future bundled model. The app has no INTERNET permission and does not require an account, network service, cloud inference, or remote storage. The expected model asset is not yet present, so the active background-estimation path remains a deterministic CPU heuristic. Image import uses the Android document picker; editing, quality heuristics, mask estimation and export run on-device.

This is an initial implementation slice, not the complete product. Current controls are photo import, 90-degree rotation, center-crop aspect ratios, aspect-ratio-preserving bounded resize, 35 × 45 mm and 2 × 2 inch ID/passport dimension presets, monochrome preview, reset, and save-copy. Crop positioning is center-anchored. The ID presets calculate 300-DPI pixel dimensions but do not yet embed verified 300-DPI metadata or certify compliance with a national issuing authority. The preview is bounded to 1,800 pixels on its longest edge and export decoding to 4,096 pixels. Manual masks, basic A4 PDF layout, local quality heuristics, and a CPU border-colour background estimate are now present in source. The optional LiteRT model adapter is not active because its binary asset is absent. Full-resolution non-destructive editing, camera capture, advanced portrait tools and validated ML/CV remain future A3 work. CPU software bitmap processing is the guaranteed path; GPU acceleration is not required.

## Documentation

Architecture specifications, decision records, requirements, threat assessments, and verification criteria will be added as they are prepared and approved. No implementation status should be inferred from planned documentation.

## Project ownership

**Owner:** Akefa Ayele Daka  
**Studio:** Aki Digital Studio

---

*Rooted in Maale. Designed for the world.*
