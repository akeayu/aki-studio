# Aki Studio — Decision and Approval Register

This register is the index of controlled Owner decisions and their current authority boundaries.

## Approved records

| ID | Version | Decision date | Decision | Current status | Record |
|---|---|---|---|---|---|
| AKIDS-A1-INIT-001 | 1.0 | 26 November 2026 | Aki Studio product inception baseline | APPROVED / VERIFIED-CLOSED | [A1 baseline](../decisions/AKIDS-A1-INIT-001-v1.0.md) |
| AKIDS-A2-ADR-001 | 1.0 archival decision record | 26 November 2026 | Invisible IROKA Intelligence Backbone Integration | APPROVED; full A2 reconciliation INCOMPLETE | [A2 decision](../decisions/AKIDS-A2-ADR-001.md) |

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

## Record provenance

The repository copies of A1 and A2 are explicitly marked archival transcriptions because the complete original document artifacts were not available when the archive was created. Approval dates and decisions are transcribed from the surviving project conversation history. They are not represented as digitally signed or independently authenticated approvals.

When original artifacts are recovered, preserve them byte-for-byte and add source metadata, including origin, recovery date, filename, and SHA-256 checksum. Do not replace these transcriptions without retaining their history.
