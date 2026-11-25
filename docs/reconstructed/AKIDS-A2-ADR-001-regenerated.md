# AKIDS-A2-ADR-001 — Invisible IROKA Intelligence Backbone Integration
## Regenerated archival reconstruction

| Field | Value |
|---|---|
| Decision ID | AKIDS-A2-ADR-001 |
| Decision version | 1.0 |
| Record type | Regenerated archival reconstruction |
| Owner | Akefa Ayele Daka |
| Original approval | APPROVED — 26 November 2026 |
| Overall A2 status | INCOMPLETE / NOT CLOSED |
| A3 implementation | NOT AUTHORIZED |
| Deployment / expenditure | NOT AUTHORIZED |

> **Provenance notice:** The complete original assistant-authored ADR message body was not recoverable from the available chat-history retrieval. This record is regenerated from surviving approval metadata, project context, and the existing archival transcription. It is not a verbatim export, signed original, or independently authenticated source. The historical approval remains attached to the A2 architecture decision; this reconstruction does not create a new approval or close the wider A2 stage.

## 1. Decision statement

**APPROVED:** Aki Studio will use IROKA as its private intelligence backbone through a dedicated AkiDS Intelligence Gateway.

Governing principle:

> AkiDS owns the experience. IROKA powers the intelligence. The customer sees only AkiDS.

Aki Studio owns the product experience, customer relationship, image-data boundaries, editing workflow, rendering, print generation, and authorization of changes. IROKA provides private intelligence and workflow coordination behind the product boundary.

## 2. Context and problem

Aki Studio requires intelligent assistance for image-quality assessment, framing, ID/passport photo checks, background workflows, portrait enhancement recommendations, and other image-related tasks.

Directly exposing IROKA as the customer product would fragment the Aki Studio brand and blur responsibility for customer data and editing. Making every image operation dependent on remote AI would also undermine offline-first operation, privacy, affordability, and reliability on constrained Android devices.

The architecture therefore separates:
- the customer-facing Aki Studio product;
- the gateway that controls access to intelligence;
- IROKA's orchestration and reasoning role;
- specialized image-processing and rendering engines;
- optional remote models/services.

## 3. Brand and system boundaries

| Layer | Responsibility / identity |
|---|---|
| Parent studio | Aki Digital Studio |
| Public product | Aki Studio |
| Historical A1 product name | AkiDS Studio |
| Private adapter | AkiDS Intelligence Gateway |
| Private intelligence backbone | IROKA |
| Image-processing subsystem | AkiDS Imaging Engine |
| Workflow coordination | AkiDS Intelligence Orchestrator |

IROKA must not appear in ordinary customer-facing:
- product name or logo;
- splash screen;
- navigation;
- settings or customer-facing help;
- marketing;
- exported image or document metadata.

IROKA identifiers may exist in engineering documentation, internal administration, and operational logs where appropriate.

**Important distinction:** Keeping IROKA out of customer branding does not permit hiding external processing. If image data is sent to a remote service, Aki Studio must clearly disclose the processing and obtain informed consent where required.

## 4. Separation of responsibilities

### 4.1 Aki Studio responsibilities
Aki Studio remains authoritative for:
- customer-facing UI and experience;
- capture/import and project lifecycle;
- image-data access and privacy boundaries;
- editing controls and edit history;
- image rendering and export;
- print layout and generation;
- user consent and authorization;
- deciding whether an AI recommendation is applied.

### 4.2 IROKA responsibilities
IROKA may:
- interpret an intelligence task;
- coordinate multi-step workflows;
- plan and recommend actions;
- assess image quality or task outcomes;
- select or invoke approved intelligence capabilities;
- return structured recommendations and explanations.

IROKA is not the pixel-level editor and must not independently overwrite source images or apply unbounded edits.

### 4.3 Specialized processing responsibilities
Actual pixel operations belong to dedicated, validated components:
- image-processing libraries and deterministic transforms;
- computer-vision components;
- segmentation and enhancement models;
- GPU or CPU rendering engine;
- export and print-generation engine.

The orchestrator may request these operations through approved contracts, but the Aki Studio application validates the result and retains user authorization.

## 5. Logical architecture

Proposed logical flow:

1. **Aki Studio Android UI** — user interaction, consent, project state, editing controls.
2. **AkiDS Intelligence Gateway** — authenticated, validated, versioned boundary for intelligence requests.
3. **IROKA Intelligence Core** — orchestration, planning, assessment, and recommendations.
4. **Model and capability adapters** — local or optional remote models, computer vision, and quality assessment.
5. **AkiDS Imaging Engine** — deterministic image operations, masks, transforms, rendering, and export.
6. **Local project/data layer** — source images, edit instructions, preferences, and project metadata.

This is a logical design. It does not assert that a production gateway, API, model, or integration already exists.

## 6. Gateway requirements

The AkiDS Intelligence Gateway is the controlled boundary between Aki Studio and intelligence capabilities.

It should provide:
- versioned request and response schemas;
- authentication and authorization;
- strict validation of task names, image references, template identifiers, and parameters;
- bounded payload sizes and resource limits;
- timeouts and cancellation;
- structured errors;
- retry rules that do not duplicate unsafe operations;
- audit events without unnecessary image or personal data;
- provider/model abstraction;
- explicit privacy mode and data-processing classification;
- capability discovery and compatibility checks;
- fail-closed behavior for unauthorized or invalid requests.

The gateway must not become a route for arbitrary code execution or unrestricted model actions.

## 7. Hybrid and offline-first operating model

### 7.1 On-device capability direction
Core local functions should include, where supported by target devices:
- crop, resize, rotate, and basic transforms;
- color and exposure adjustments;
- print layout and local export;
- local project storage;
- basic image-quality checks;
- supported face detection and segmentation;
- supported local AI assessment.

### 7.2 Optional remote capability direction
Remote intelligence may be considered for tasks requiring larger models or more advanced orchestration, only where:
- the user is informed;
- the user explicitly consents before image transmission;
- the task cannot reasonably be completed locally or remote use is an optional enhancement;
- data sent is minimized;
- provider, retention, and deletion terms are assessed;
- failure does not destroy local work.

### 7.3 Offline behavior
- Core editing and export must not require a network connection.
- Remote intelligence must be optional.
- Failed or unavailable remote services must return a clear recoverable state.
- Pending remote work must not block access to local projects.
- No silent background upload or deferred upload without consent.

## 8. Privacy and data governance

The integration must follow these rules:
1. Local processing by default.
2. No automatic upload of source photographs to IROKA or other remote services.
3. Explicit, informed, task-specific consent before optional remote image processing.
4. Clear disclosure of the provider, purpose, categories of data, and retention where known.
5. Data minimization and purpose limitation.
6. No default sharing with unrelated products or services.
7. No unnecessary face-recognition or identity database.
8. Secure transport and access controls.
9. Defined temporary-file handling and deletion behavior.
10. Logs must avoid raw image content and unnecessary personal information.
11. The user must retain control of source images and local projects.

Brand invisibility is not a privacy exemption. External processing must be disclosed.

## 9. Model and provider independence

The gateway must decouple Aki Studio from a single model provider, model family, or inference runtime.

Design direction:
- provider adapters behind stable internal contracts;
- capability-based routing rather than provider-specific UI logic;
- versioned model metadata and compatibility declarations;
- explicit fallback behavior;
- local model option where technically feasible;
- replaceable model/runtime components;
- license, provenance, security, and performance review before adoption.

Provider independence is an architectural requirement, not a claim that all providers are interchangeable without testing.

## 10. Controlled intelligence execution

The system must use constrained task contracts rather than open-ended model control.

Requirements:
- allowlisted task types;
- typed and versioned schemas;
- validated image references and template identifiers;
- strict parameter ranges;
- bounded execution time and resource use;
- explicit user authorization for consequential edits;
- preview and review before applying recommendations;
- preservation of original images;
- non-destructive operations where feasible;
- structured results, warnings, confidence/uncertainty, and error codes;
- cancellation and recovery.

### 10.1 Illustrative contract direction

A contract name discussed for assessment was:

- Contract family: `akids.intelligence.v1`
- Example task: `passport_photo_assessment`
- Example inputs: image reference, selected template identifier, processing/privacy mode.
- Example constraints: privacy-first, preserve identity, preserve original, non-destructive.

This is illustrative only. It is not evidence of an existing endpoint, implemented schema, deployed service, or verified IROKA API. The exact contract must be reconciled against the actual IROKA repository and Aki Studio Android design during A2.

## 11. Failure modes and recovery

| Failure | Required behavior |
|---|---|
| Network unavailable | Continue core local workflows; report remote capability unavailable |
| Gateway timeout | Cancel or return bounded timeout; preserve local project |
| Invalid request | Reject with structured validation error |
| Unauthorized task | Fail closed |
| Model unavailable | Use an approved local/fallback capability if validated; otherwise return recoverable failure |
| Low device memory | Reduce processing load, use tiled processing, or offer a clear retry path |
| Partial image processing | Do not overwrite source; discard or isolate incomplete output |
| Provider failure | Do not silently transmit to another provider |
| User revokes consent | Stop optional remote processing and prevent new transmission |
| Incompatible model/version | Reject safely and report compatibility issue |

Retries must be bounded and safe. A remote failure must never cause loss of local edits or source images.

## 12. Security and trust requirements

Full A2 reconciliation must specify:
- application-to-gateway authentication;
- token/key lifecycle and secure storage;
- authorization scopes and least privilege;
- transport security;
- input validation and payload limits;
- rate and resource limits;
- replay/idempotency strategy where relevant;
- provider isolation;
- secrets management;
- dependency and model supply-chain controls;
- audit-event policy;
- privacy threat model;
- incident and data-deletion procedures.

No production credentials, secrets, or remote processing commitments are authorized by this ADR.

## 13. Alternatives and decision rationale

The recoverable approval history supports the selected private gateway and hybrid architecture. The principal rejected directions are:

### Expose IROKA directly as the consumer product
Not selected because Aki Studio must own its public brand and customer experience, and because the intelligence backbone is an internal capability.

### Make all editing dependent on remote AI
Not selected because it conflicts with offline-first operation, privacy-first defaults, affordability, constrained connectivity, and resilience.

### Let IROKA directly perform unrestricted pixel editing
Not selected because deterministic image processing, source preservation, user review, and edit authorization must remain under Aki Studio control.

### Bind the product permanently to one model provider
Not selected because the architecture requires replaceable models/providers and independent product evolution.

The complete original alternatives matrix was not recoverable; this section records the rationale supported by surviving context and does not claim to reproduce a missing original matrix.

## 14. Required A2 technical reconciliation before A2 closure

The approved ADR is one decision within A2. Full A2 remains open until the following are evidenced and reviewed:

1. Inspect actual IROKA repository, APIs, contracts, authentication, and runtime capabilities. Do not assume an API exists.
2. Define Android-to-gateway topology, transport, authentication, authorization, versioning, and failure behavior.
3. Validate local models, segmentation, face detection, enhancement, inference runtimes, and GPU compatibility.
4. Specify full-resolution rendering, memory budgets, and non-destructive editing.
5. Define official document-photo preset provenance and compliance boundaries.
6. Establish minimum Android/API/device requirements and lower-memory performance targets.
7. Review licenses for source code, dependencies, models, and weights.
8. Complete privacy, security, threat-model, and supply-chain assessments.
9. Define freemium entitlement architecture without committing to payment providers.
10. Complete screen-by-screen UI/UX specifications.
11. Define test strategy and measurable acceptance criteria.
12. Estimate effort, phases, risks, and implementation dependencies.
13. Specify exact integration contracts, offline behavior, recovery, and observability.
14. Produce a consolidated A2 closure package for Owner review.

## 15. Approval and authority boundary

Surviving approval metadata:
- Owner: Akefa Ayele Daka.
- Decision ID: AKIDS-A2-ADR-001.
- Decision: APPROVED.
- Approval date: 26 November 2026.
- Scope: private IROKA backbone integration through the AkiDS Intelligence Gateway.

Stage status:
| Item | Status |
|---|---|
| A1 product baseline | APPROVED / VERIFIED-CLOSED |
| A2-ADR-001 architecture direction | APPROVED |
| Full A2 reconciliation | INCOMPLETE / NOT CLOSED |
| A3 implementation | NOT AUTHORIZED |
| A4 verification | NOT STARTED |
| A5 deployment/release | NOT AUTHORIZED |
| Expenditure / paid services | NOT AUTHORIZED |

Owner instruction associated with the decision:

> None. Preserve the approved A1 baseline. Do not authorize A3 implementation, deployment or expenditure.

This decision does not amend A1, authorize code changes, authorize deployment, or create financial commitments.

## 16. Regeneration and provenance statement

This document is a regenerated archival reconstruction of the approved A2 architecture direction, prepared because the original assistant-authored message body could not be retrieved. It consolidates the surviving A2 archival transcription and recoverable project context. Exact original wording, original message IDs, and original source-file bytes are unavailable. No checksum is claimed for the missing original.

**End of regenerated archival reconstruction — AKIDS-A2-ADR-001.**
