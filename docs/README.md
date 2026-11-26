# Aki Studio Documentation

This directory contains controlled product, architecture, governance, and verification documentation for Aki Studio.

## Document structure

- `decisions/` — Owner decisions and architecture decision records.
- `reconstructed/` — Regenerated archival reconstructions of assistant-authored records when original chat message bodies cannot be retrieved.
- `archive/` — Provenance, recovery notes, and historical archive records.
- `governance/` — decision register, approval history, change control, and stage-gate status.
- `product/` — product requirements, capabilities, UX, and product specifications.
- `architecture/` — technical architecture, interfaces, privacy boundaries, and platform assessments.
- `security/` — threat models, data handling, dependency and supply-chain assessments.
- `verification/` — test strategy, acceptance criteria, and evidence.
- `implementation/` — stage-gated implementation records and scope boundaries.
- `implementation/AKIDS-A3-SEG-MODEL-REVIEW-001-v0.1.0.md` — offline portrait-segmentation model candidate assessment; no model adopted.
- `implementation/AKIDS-A3-SEG-MODEL-PIN-001-v0.1.0.md` — exact MediaPipe Selfie Segmenter candidate artifact, SHA-256, GCS generation, LiteRT runtime floor, and acquisition/integration gates.
- `implementation/AKIDS-A3-SEG-LITERT-001-v0.1.0.md` — optional LiteRT CPU adapter source integration, missing binary asset, fallback behavior, and verification gates.
- `implementation/AKIDS-A3-SEG-ASYNC-001-v0.1.0.md` — asynchronous local segmentation, request invalidation, bitmap ownership, stale-result disposal, and lifecycle safeguards.
- `implementation/AKIDS-A3-SPEC-VALIDATION-001-v0.1.0.md` — fail-closed official-photo provenance, HTTPS source, ISO-date, effective-date validation, and authored JVM tests.
- `implementation/AKIDS-A3-EXPORT-FORMATS-001-v0.1.0.md` — JPEG/PNG local export, MediaStore publication safety, and metadata limitations.
- `implementation/AKIDS-A3-PROJECTS-001-v0.1.0.md` — private offline editable projects, original-image preservation, edit recipes, masks, and project management operations.
- `implementation/AKIDS-A3-ADJUST-001-v0.1.0.md` — local CPU-only brightness, contrast, saturation, and warmth controls with persisted non-destructive edit settings.
- `implementation/AKIDS-A3-CROP-NAMING-001-v0.1.0.md` — interactive crop selection, project crop persistence, user-supplied export names, and reusable local filename patterns.
- `implementation/AKIDS-A3-HISTORY-001-v0.1.0.md` — bounded in-session undo/redo for non-destructive edit recipes; bitmap-mask stroke history remains separate.
- `implementation/AKIDS-A3-UI-SYSTEM-001-v0.1.0.md` — reusable futuristic studio UI components, categorized workspace shell, interaction/accessibility principles, and unified creative-workspace direction.
- `implementation/AKIDS-A3-LAYERS-TYPE-VECTOR-001-v0.1.0.md` — versioned editable layers, local CPU compositing, typography, vector primitives, project persistence and verification limits.

## Controlled baseline records

| Record | Version | Approval | Status |
|---|---|---|---|
| [AKIDS-A1-INIT-001](decisions/AKIDS-A1-INIT-001-v1.0.md) | 1.0 | Owner-approved, 26 November 2026 | VERIFIED-CLOSED |
| [AKIDS-A2-ADR-001](decisions/AKIDS-A2-ADR-001.md) | 1.0 decision record | Owner-approved, 26 November 2026 | APPROVED; full A2 reconciliation incomplete |

## Regenerated archival reconstructions

Because the complete assistant-authored A1/A2 message bodies were not retrievable from chat history, the specifications have been regenerated from the surviving project context and archival transcriptions:

- [AKIDS-A1-INIT-001 v1.0 — regenerated full product specification](reconstructed/AKIDS-A1-INIT-001-v1.0-regenerated.md)
- [AKIDS-A2-ADR-001 — regenerated architecture decision](reconstructed/AKIDS-A2-ADR-001-regenerated.md)
- [AKIDS-REC-001 — chat-authored record recovery and provenance note](archive/AKIDS-REC-001-chat-authored-record-recovery.md)

The regenerated documents preserve the known approved direction and approval metadata. They are reconstructions, not verbatim message exports, signed originals, or cryptographically authenticated copies. They do not create new approvals, silently amend the approved baselines, or close full A2.

## Archival integrity and provenance

Distinguish original approved source artifacts, archival transcriptions, and regenerated reconstructions. Never label a reconstruction as an original. If exact original artifacts are later recovered, preserve them unchanged with source and integrity metadata and retain all existing records as audit history.

## Change control

- Never silently overwrite an approved baseline.
- Record subsequent changes as controlled addenda or new versions.
- Preserve prior versions and approval records.
- Distinguish Owner approval from technical verification.
- A3 implementation is authorized only for the local-only Android MVP recorded in AKIDS-A3-IMPL-LOCAL-001.
- This A3 authorization excludes network/cloud runtime services, merge to main, deployment/release, purchases, subscriptions, and other expenditure.
- Keep A4 verification and A5 deployment/release separately gated.

## Current stage boundary

A1 is approved and closed. The IROKA integration direction in A2-ADR-001 is approved. Full A2 technical reconciliation remains incomplete. A3 is authorized only for the local-only Android MVP. A4 verification has not started; A5 deployment/release and expenditure remain unauthorized.
