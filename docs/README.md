# Aki Studio Documentation

This directory contains controlled product, architecture, governance, and verification documentation for Aki Studio.

## Document structure

- `decisions/` — Owner decisions and architecture decision records.
- `governance/` — decision register, approval history, change control, and stage-gate status.
- `product/` — product requirements, capabilities, UX, and product specifications.
- `architecture/` — technical architecture, interfaces, privacy boundaries, and platform assessments.
- `security/` — threat models, data handling, dependency and supply-chain assessments.
- `verification/` — test strategy, acceptance criteria, and evidence.

## Controlled baseline records

| Record | Version | Approval | Status |
|---|---|---|---|
| [AKIDS-A1-INIT-001](decisions/AKIDS-A1-INIT-001-v1.0.md) | 1.0 | Owner-approved, 26 November 2026 | VERIFIED-CLOSED |
| [AKIDS-A2-ADR-001](decisions/AKIDS-A2-ADR-001.md) | 1.0 decision record | Owner-approved, 26 November 2026 | APPROVED; full A2 reconciliation incomplete |

## Archival integrity and provenance

The records in this repository must distinguish original approved source artifacts from archival transcriptions prepared from surviving approval history.

At the time this documentation structure was established, the complete original A1 and A2 document files were not available in the repository or in the retrievable project history. The records linked above therefore identify their provenance and limitations explicitly. They must not be represented as verbatim copies, signed originals, or cryptographically authenticated source documents.

If the original approved artifacts are later recovered, preserve them unchanged, record their source and integrity metadata, and add them alongside—not silently in place of—the archival transcriptions.

## Change control

- Never silently overwrite an approved baseline.
- Record subsequent changes as controlled addenda or new versions.
- Preserve prior versions and approval records.
- Distinguish Owner approval from technical verification.
- Approval of A1 or an individual A2 architecture decision does not authorize implementation, deployment, or expenditure.
- Keep A3 implementation, A4 verification, and A5 deployment/release separately gated.

## Current stage boundary

A1 is approved and closed. The IROKA integration direction in A2-ADR-001 is approved. Full A2 technical reconciliation remains incomplete. A3 implementation, A4 verification, A5 deployment, and expenditure are not authorized by these records.
