# Aki Studio — Decision and Approval Register

This register indexes controlled Owner decisions, reconstructed archival records, and current authority boundaries.

## Approved decisions

| ID | Version | Decision date | Decision | Current status | Controlled record |
|---|---|---|---|---|---|
| AKIDS-A1-INIT-001 | 1.0 | 26 November 2026 | Aki Studio product inception baseline | APPROVED / VERIFIED-CLOSED | [A1 baseline transcription](../decisions/AKIDS-A1-INIT-001-v1.0.md) |
| AKIDS-A2-ADR-001 | 1.0 | 26 November 2026 | Invisible IROKA Intelligence Backbone Integration | APPROVED; full A2 reconciliation INCOMPLETE | [A2 decision transcription](../decisions/AKIDS-A2-ADR-001.md) |

## Regenerated archival reconstructions

| Record | Purpose | Status |
|---|---|---|
| [AKIDS-A1-INIT-001 v1.0 regenerated](../reconstructed/AKIDS-A1-INIT-001-v1.0-regenerated.md) | Fuller reconstruction of the approved A1 product inception specification | RECONSTRUCTED; not verbatim |
| [AKIDS-A2-ADR-001 regenerated](../reconstructed/AKIDS-A2-ADR-001-regenerated.md) | Fuller reconstruction of the approved A2 intelligence-backbone architecture decision | RECONSTRUCTED; not verbatim |
| [AKIDS-REC-001](../archive/AKIDS-REC-001-chat-authored-record-recovery.md) | Recovery method, accountability, provenance, and source limitations | RECORDED |

The reconstructed records do not replace the historical decision files, create new approvals, or alter the approved decision dates. They consolidate recoverable content because the complete original assistant-authored chat message bodies could not be retrieved.

## Approval history

### 26 November 2026 — A1 baseline

- Owner: Akefa Ayele Daka.
- Record: AKIDS-A1-INIT-001 v1.0.
- Decision: APPROVED.
- Baseline disposition: VERIFIED-CLOSED.
- Authority granted: approval of the product inception baseline.
- Authority not granted: A3 implementation, deployment, or expenditure.

### 26 November 2026 — A2 architecture decision

- Owner: Akefa Ayele Daka.
- Record: AKIDS-A2-ADR-001.
- Decision: APPROVED.
- Scope: invisible IROKA intelligence backbone integration through a dedicated gateway, with hybrid/offline-first operation, privacy boundaries, model-provider independence, and Aki Studio-controlled execution.
- Authority not granted: A3 implementation, deployment, or expenditure.
- Full A2 technical reconciliation remains INCOMPLETE / NOT CLOSED.

## Stage-gate status

| Stage | Status | Authority |
|---|---|---|
| A1 — Assessment and specification | APPROVED / VERIFIED-CLOSED | Baseline approval only |
| A2 — Architecture and technical reconciliation | Individual ADR approved; overall A2 INCOMPLETE | No implementation authority |
| A3 — Implementation | NOT AUTHORIZED | None |
| A4 — Verification | NOT STARTED | None |
| A5 — Deployment/release | NOT AUTHORIZED | None |

## Change-control rules

1. Preserve approved historical versions unchanged.
2. Use a new version or controlled addendum for changes to an approved baseline.
3. Record the Owner, date, scope, rationale, decision, and authority boundary for every decision.
4. Distinguish an approved architecture direction from a technically verified implementation.
5. Do not mark a stage closed without its required evidence and explicit closure decision.
6. No implementation, deployment, or expenditure may be inferred from A1/A2 approval.
7. Keep archival transcriptions and regenerated reconstructions clearly labelled; never represent either as verbatim originals unless exact source text is recovered and verified.

## Record provenance

The original assistant-authored A1/A2 chat message bodies were not retrievable through the available history interfaces during the recovery attempt. The repository therefore retains the initial archival transcriptions and adds fuller regenerated reconstructions based on surviving project context. The Owner's recorded approvals and dates are preserved, but no missing wording, original message identifiers, signatures, or original-file checksums are invented.

If a complete conversation export or exact source artifact becomes accessible later, preserve it separately and byte-for-byte, record source and SHA-256 metadata, and compare it against these records without erasing the audit trail.
