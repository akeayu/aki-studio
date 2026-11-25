# AKIDS-A2-ADR-001 — Invisible IROKA Intelligence Backbone Integration

| Field | Value |
|---|---|
| Decision ID | AKIDS-A2-ADR-001 |
| Record version | 1.0 archival decision record |
| Stage | A2 — Architecture decision |
| Owner | Akefa Ayele Daka |
| Approval date | 26 November 2026 |
| Decision | APPROVED |
| Full A2 reconciliation | INCOMPLETE / NOT CLOSED |
| A3 implementation | NOT AUTHORIZED |
| Deployment and expenditure | NOT AUTHORIZED |

## 1. Record provenance and archival status

**Archival transcription — not a verbatim original artifact.**

This file records the approved A2 architecture direction recoverable from the surviving project approval history. The complete original ADR document file and its exact original wording were not available when this repository archive was prepared.

This transcription preserves the known decision identifier, Owner, approval date, scope, key constraints, and architecture direction. It is not a signed original, verbatim source artifact, or cryptographically authenticated copy. If the original approved ADR is recovered, add it unchanged with provenance and integrity metadata; retain this transcription as an audit trail.

## 2. Context

Aki Studio requires intelligent assistance for image quality assessment, framing, background processing workflows, portrait enhancement recommendations, document-photo checks, and other creative tasks.

The Owner approved an architecture direction in which IROKA acts as a private AI and intelligence backbone while remaining invisible to Aki Studio customers. Aki Studio must retain ownership of the customer experience, image workflow, data boundaries, rendering, and authorization of changes.

## 3. Owner decision

**APPROVED:** Integrate IROKA as the private intelligence backbone through a dedicated Aki Studio intelligence gateway, subject to the boundaries and constraints in this decision.

Governing principle:

> AkiDS owns the experience. IROKA powers the intelligence. The customer sees only AkiDS.

IROKA is not to appear as a customer-facing product brand or customer interface.

## 4. Approved architecture direction

### 4.1 Brand and system boundaries

- **Parent/public studio identity:** Aki Digital Studio.
- **Public Android product:** Aki Studio (historically named AkiDS Studio in the A1 baseline).
- **Private integration adapter:** AkiDS Intelligence Gateway.
- **Private intelligence backbone:** IROKA.
- **Internal intelligence contract:** IROKA Intelligence API.
- **Image processing and rendering:** AkiDS Imaging Engine.
- **Workflow coordination:** AkiDS Intelligence Orchestrator.

IROKA must not appear in ordinary customer-facing navigation, logo, splash screen, settings, marketing, or export metadata. Internal identifiers may be used in engineering, administration, and operational logs.

Brand separation does not remove the obligation to disclose external image processing and obtain informed consent where applicable.

### 4.2 Separation of responsibilities

Aki Studio retains responsibility for:
- customer-facing UI and user experience;
- image capture, editing, and project management;
- customer image data and privacy boundaries;
- image rendering and export;
- print generation;
- deciding whether proposed changes are applied.

IROKA may coordinate, plan, recommend, assess, and validate intelligence workflows. It is not the pixel-level image editor. Pixel operations are performed by dedicated image-processing engines, computer-vision models, segmentation/enhancement models, and renderers.

AI recommendations must not autonomously apply uncontrolled changes. Aki Studio retains authorization and control.

### 4.3 Logical architecture

1. Aki Studio Android user interface.
2. Private AkiDS Intelligence Gateway.
3. IROKA Intelligence Core.
4. AI models, computer vision, workflow orchestration, and quality evaluation.
5. Specialized image-processing components: OpenCV or other validated engines, GPU renderer, local AI models, and export engine.

This is a logical architecture direction, not evidence that these components or interfaces have been implemented.

### 4.4 Hybrid and offline-first operation

The architecture is to support a hybrid model:
- On-device processing for core crop/resize, color adjustments, print layout, supported face detection/segmentation, basic assessment, and export.
- Optional IROKA or remote intelligence for tasks that genuinely require more capable orchestration or models.
- No mandatory cloud dependency for core product usefulness.
- Lower-spec devices should be supported through smaller local models, efficient processing, or optional explicitly disclosed remote processing.
- Offline capability and recovery behavior must be designed as first-class requirements.

### 4.5 Privacy and data boundaries

- Local processing by default.
- No automatic image upload to IROKA or other remote services.
- Explicit informed consent before optional remote image processing.
- Data minimization and purpose limitation.
- No default cross-product sharing of Aki Studio customer images or data.
- No unnecessary facial identification database.
- Secure transport, access controls, retention limits, deletion, and failure handling must be specified during A2 technical reconciliation.
- IROKA must not become a single point of failure for core offline workflows.

### 4.6 Model and provider independence

The gateway and internal contracts should avoid hard-coupling the product to one model provider or inference runtime. Model providers and runtimes should be replaceable subject to A2 validation, without exposing those substitutions to customers or changing Aki Studio's ownership of the workflow.

### 4.7 Controlled intelligence execution

The gateway and contracts must provide versioned schemas, authentication, strict input/output validation, timeouts, recovery behavior, and bounded execution. Aki Studio must validate recommendations and retain control over applying edits.

An illustrative internal contract discussed for assessment was `akids.intelligence.v1`, with a `passport_photo_assessment` task, image reference, template identifier, privacy-first processing mode, and requirements such as preserving identity, preserving the original, and non-destructive operation. This is an example contract direction only; it is not a verified existing API.

## 5. Alternatives and rationale

The approved direction establishes a private intelligence gateway and hybrid architecture rather than exposing IROKA directly to customers or making all image processing dependent on a remote AI service.

The rationale recorded in the project history is:
- preserve one coherent Aki Studio customer brand;
- keep customer experience and data governance under Aki Studio control;
- support offline use and constrained Android devices;
- allow optional advanced intelligence without mandatory cloud processing;
- separate orchestration and recommendations from deterministic image rendering;
- preserve provider and model replaceability;
- prevent uncontrolled autonomous editing.

A complete contemporaneous alternatives analysis was not available in the surviving approval record. The rationale above is a summary, not a claim that a verbatim alternatives matrix has been recovered.

## 6. Consequences and obligations for full A2 reconciliation

Approval of this architecture direction does not close the full A2 assessment. The following work remains required before A2 can be presented for closure:

1. Inspect the actual IROKA repository, existing APIs, contracts, authentication, and runtime capabilities. Do not assume an API exists.
2. Define Android-to-gateway communication, versioned contracts, authentication, authorization, and failure behavior.
3. Validate local models, segmentation, face detection, enhancement, inference runtimes, and GPU compatibility.
4. Specify full-resolution rendering and non-destructive editing.
5. Define document-photo preset provenance and compliance-validation boundaries.
6. Establish minimum Android/API/device requirements and low-memory performance budgets.
7. Review licenses for code, dependencies, models, and weights.
8. Complete privacy, security, threat-model, and supply-chain assessments.
9. Define freemium entitlement boundaries without committing to a payment provider.
10. Complete screen-by-screen UI/UX specifications.
11. Define test strategy and measurable acceptance criteria.
12. Estimate effort, phases, risks, and implementation dependencies.
13. Specify exact integration contracts and offline/recovery behavior.
14. Prepare a consolidated A2 closure package for Owner review.

## 7. Stage-gate and authority boundary

| Stage / authority | Status |
|---|---|
| A1 product baseline | APPROVED / VERIFIED-CLOSED |
| This A2 architecture decision | APPROVED |
| Full A2 technical reconciliation | INCOMPLETE / NOT CLOSED |
| A3 implementation | NOT AUTHORIZED |
| A4 verification | NOT STARTED |
| A5 deployment/release | NOT AUTHORIZED |
| Financial expenditure or paid services | NOT AUTHORIZED |

The Owner's instruction associated with this decision was:

> None. Preserve the approved A1 baseline. Do not authorize A3 implementation, deployment or expenditure.

This ADR is an architecture direction only. It does not amend the A1 baseline, authorize code changes, authorize deployment, or authorize financial commitments.

## 8. Approval history

| Date | Actor | Event | Result |
|---|---|---|---|
| 26 November 2026 | Akefa Ayele Daka, Owner | Approved AKIDS-A2-ADR-001 — Invisible IROKA Intelligence Backbone Integration | APPROVED |
| 26 November 2026 | Owner | Directed preservation of A1 and withheld A3, deployment, and expenditure authority | Binding constraint recorded |

**Approval evidence limitation:** the approval is recorded in the surviving ChatGPT project conversation history. A signed source artifact, signature file, or independent approval-system record was not available for this archival transcription.

---

**End of archival transcription — AKIDS-A2-ADR-001**

*The decision is approved. Full A2 technical reconciliation remains incomplete and must not be represented as closed.*
