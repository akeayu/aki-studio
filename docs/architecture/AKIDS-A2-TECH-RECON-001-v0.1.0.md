# Aki Studio — A2 Technical Reconciliation (Read-only Assessment)

**Document ID:** AKIDS-A2-TECH-RECON-001  
**Version:** 0.1.0  
**Status:** PARTIAL — first evidence pass; A2 NOT CLOSED  
**Product:** Aki Studio  
**Owner:** Akefa Ayele Daka  
**A1 baseline:** AKIDS-A1-INIT-001 v1.0 — APPROVED / VERIFIED-CLOSED  
**Architecture decision:** AKIDS-A2-ADR-001 — APPROVED  
**Assessment mode:** Read-only repository and architecture reconciliation  
**Prepared:** 26 November 2026  
**Working branch:** `docs/a2-technical-reconciliation-v0.1.0`

## 1. Purpose and authority

This record begins the technical reconciliation required after approval of Aki Studio's A1 product baseline and A2 IROKA integration decision. It compares the approved Aki Studio direction with the repository evidence available in the Aki Studio and IROKA destination repositories.

This is an evidence assessment, not an implementation specification freeze. It creates no new Owner decision and does not amend A1 or A2-ADR-001.

**Authority boundary:** no application code, service, API, dependency, model, provider, credential, payment service or deployment is authorized by this assessment. A3 remains NOT AUTHORIZED. No expenditure is authorized.

## 2. Executive determination

The approved architecture remains coherent at the responsibility-boundary level:

> AkiDS owns the experience. IROKA powers the intelligence. The customer sees only AkiDS.

However, the repositories do not yet evidence an implementable, integrated Aki Studio product stack.

1. The Aki Studio repository is currently a documentation home. The inspected material does not establish an Android application, image-processing engine, print renderer, AkiDS Intelligence Gateway, or executable Aki-to-IROKA integration.
2. IROKA's canonical `main` Rust workspace declares only `crates/iroka-kernel`. The inspected main-branch source establishes a governed authorization kernel, not a customer-facing task API, production HTTP host, image-processing service, model-serving runtime, or Aki-specific gateway.
3. IROKA has separate candidate branches and draft pull requests for cognitive, conversation, task API, memory, orchestration and neural capabilities. Existing IROKA A2 reports conclude that these components do not yet form one verified production integration graph. Their candidate contracts are not automatically approved Aki Studio contracts.
4. No approved Android-compatible local inference runtime/model artifact, image segmentation model, enhancement model, or device support floor was verified.
5. Aki's image editing, non-destructive rendering, full-resolution processing, print generation, official-photo preset provenance, UI/UX, and device-performance requirements remain to be converted into testable A2 contracts.
6. No dependency/model license and supply-chain decision has been made for Aki Studio. IROKA's public repository declares `LicenseRef-Proprietary`; public visibility is not permission to copy or redistribute its source.

**Disposition:** A2 technical reconciliation is **PARTIAL / NOT CLOSED**. The approved architecture direction is preserved, but the integration and product implementation baseline is not frozen.

## 3. Repositories and inspected evidence

### 3.1 Aki Studio

- Repository: `akeayu/aki-studio`
- Default branch: `main`
- Visibility: public
- Repository purpose: Aki Studio product documentation and controlled project records.
- Inspected root README: [README.md](https://github.com/akeayu/aki-studio/blob/main/README.md)
- Inspected architecture placeholder: [docs/architecture/README.md](https://github.com/akeayu/aki-studio/blob/main/docs/architecture/README.md)
- Approved architecture record: [AKIDS-A2-ADR-001 regenerated archival reconstruction](https://github.com/akeayu/aki-studio/blob/main/docs/reconstructed/AKIDS-A2-ADR-001-regenerated.md)

The README explicitly describes product capabilities as planned rather than implemented. The architecture folder is reserved for A2 technical design and reconciliation. No Android source tree, build manifest, gateway implementation or imaging implementation was evidenced in the inspected files.

### 3.2 IROKA canonical repository

- Repository: `akefadaka-git/iroka`
- Repository ID: `1445234713`
- Default branch: `main`
- Visibility: public
- Declared repository license: `LicenseRef-Proprietary`
- Latest main-branch commit observed in the commit search: `6b7004c897907f5ca40de58fee7222732fd7cde0` (IROKA A1 master architecture closure documentation).
- Main Cargo workspace manifest blob: `d64d3143b762589346fb19ee77e6f40d4af8c77c`
- Main workspace members: `crates/iroka-kernel` only.
- Workspace Rust edition: 2021; declared MSRV: 1.75.

Inspected source:
- [Cargo.toml](https://github.com/akefadaka-git/iroka/blob/main/Cargo.toml)
- [iroka-kernel Cargo.toml](https://github.com/akefadaka-git/iroka/blob/main/crates/iroka-kernel/Cargo.toml)
- [iroka-kernel lib.rs](https://github.com/akefadaka-git/iroka/blob/main/crates/iroka-kernel/src/lib.rs)
- [iroka-kernel service.rs](https://github.com/akefadaka-git/iroka/blob/main/crates/iroka-kernel/src/service.rs)

The kernel manifest includes pinned cryptographic and persistence dependencies such as `ed25519-dalek`, `serde_jcs`, `sha2`, and `rusqlite`. This is not a complete dependency/license/SBOM review and does not approve these dependencies for Aki Studio.

The inspected kernel service boundary performs governed authorization. Its source documentation explicitly distinguishes an authorization decision from an execution grant: an `ALLOW` decision is not itself permission to perform a protected side effect. Aki Studio must therefore integrate through a separately defined and authorized service contract, not treat a direct kernel call or model response as an editing authorization.

## 4. Relevant IROKA A2 evidence — reuse as evidence, not as Aki approval

Two existing IROKA assessments materially inform this Aki Studio reconciliation:

### 4.1 IROKA Android A2 read-only reconciliation

- Document: `IROKA-ANDROID-A2-RIR-001 v0.1.0`
- Branch: `architecture/a2-android-integration-reconciliation`
- File blob: `ca79904946097c56aed9babcc5096d221ded3915`
- [Assessment document](https://github.com/akefadaka-git/iroka/blob/architecture/a2-android-integration-reconciliation/docs/mobile/IROKA-ANDROID-A2-READONLY-INTEGRATION-RECONCILIATION-v0.1.0.md)
- Related draft PR: [#132](https://github.com/akefadaka-git/iroka/pull/132)

Its assessed candidates included BRAIN PR #27, CIW PR #23 and the TALORA-specific private task API PR #129. It determined that they were individually bounded candidate components, not one compatible production integration graph. It also recorded missing native Android authentication, a runnable first-party service host, governed mobile-facing memory integration, durable task lifecycle, approved on-device runtime/model artifact, and Android end-to-end integration evidence.

**Applicability to Aki:** these findings are relevant evidence about IROKA's integration maturity. The report is for the IROKA Android client, not Aki Studio. It does not define or approve Aki's gateway, API, image-data flow, task schemas or product entitlements.

### 4.2 INNIA A2 neural infrastructure reconciliation

- Document: `INNIA-A2-RECON-001 v1.0.0`
- Branch: `architecture/a2-innia-neural-reconciliation`
- File blob: `f11b91cc0102154f2a15a292d2e6aa21b4741b66`
- [Assessment document](https://github.com/akefadaka-git/iroka/blob/architecture/a2-innia-neural-reconciliation/docs/architecture/INNIA-A2-NEURAL-INFRASTRUCTURE-RECONCILIATION-v1.0.0.md)
- Related draft PR: [#135](https://github.com/akefadaka-git/iroka/pull/135)

Its principal determination is that the IROKA main workspace contains only the Kernel while neural/cognitive candidates remain on separate unmerged branches. It records unresolved integration among authority, cognition, memory, orchestration and model infrastructure; an unverified INGA exact-head run; a G08 trusted-time gap; and no established production model registry, model-serving runtime or governed training/promotion pipeline.

**Applicability to Aki:** IROKA should be treated as an approved architectural dependency direction, not as a presently verified production AI service. No Aki feature may claim availability merely because a candidate IROKA branch or isolated test exists.

These IROKA reports are evidence inputs only. Their product-specific decisions, branches, contracts and implementation permissions are not inherited by Aki Studio.

## 5. Aki-to-IROKA responsibility and integration boundary

The approved A2-ADR-001 responsibility split remains the governing direction:

| Responsibility | Aki Studio | IROKA / approved service |
|---|---|---|
| Customer-facing brand, UI and workflow | Authoritative | Not customer-facing |
| Capture/import and local project lifecycle | Authoritative | No ownership |
| Source image and privacy boundary | Authoritative | Receives only task-scoped authorized inputs |
| Edit history, preview and user review | Authoritative | May return structured proposals |
| Pixel transforms, segmentation, masks and rendering | Aki-controlled imaging subsystem | May coordinate approved capabilities; not the pixel editor |
| Print layout and export | Authoritative | No ownership |
| Reasoning, task decomposition and recommendations | Validates and presents results | Intelligence/orchestration role, subject to actual verified capabilities |
| Identity, policy, consent and protected execution | Aki enforces product consent and client safeguards | Canonical IROKA authority must verify server-side identity/policy where connected |
| Applying a proposed edit | Requires Aki-controlled user/product authorization | Must not independently overwrite source images |

The illustrative `akids.intelligence.v1` contract and `passport_photo_assessment` task in A2-ADR-001 remain examples only. No existing endpoint, schema, authentication mechanism or production gateway was verified.

## 6. A2 reconciliation matrix

| Area | Evidence observed | Status | Required A2 closure evidence |
|---|---|---|---|
| Android product foundation | Aki repository documentation only; no app source/build evidence inspected | MISSING | Native Android app architecture, supported API/device floor and screen inventory |
| AkiDS Intelligence Gateway | Approved as an architecture boundary; no implementation/API exists in inspected evidence | MISSING | Aki-owned versioned API contract, trust model, auth, schema, errors, timeouts and lifecycle |
| IROKA service integration | Kernel on main; candidate services on separate unmerged branches; IROKA A2 reports identify composition gaps | PARTIAL | Exact compatible refs, host composition, server auth, capability discovery and contract tests |
| On-device AI | No approved Android runtime/model artifact verified | MISSING | Runtime/model candidates, license/provenance, ABI/device/RAM floor, artifact integrity and update policy |
| Image segmentation/background removal | Product requirement exists; no selected or verified implementation | MISSING | Candidate evaluation, quality thresholds, edge/mask acceptance and low-memory behavior |
| Portrait enhancement | Product requirement exists; no selected or verified model/algorithm | MISSING | Identity-preservation criteria, artifact/license review and test corpus |
| Deterministic image editing | Product requirements defined; no Aki implementation inspected | MISSING | Non-destructive edit document, renderer architecture, color/geometry semantics and round-trip tests |
| Full-resolution processing | Requirement identified; no memory/performance benchmark | MISSING | Tile strategy, peak memory budget, image-size limits and target-device benchmark |
| Print generation | Physical sizes and 300 DPI direction recorded in A1; no renderer verified | MISSING | Pixel rounding, physical dimensions, page layout, PDF/export and printer validation |
| Official ID/passport presets | A1 requires provenance and verification status; no authoritative jurisdictional source register inspected | UNRESOLVED | Per-template jurisdiction, authority/source, effective date, dimensions and verification status |
| Privacy and consent | Local-first/no automatic upload principles approved | PARTIAL | Image data-flow map, task-specific consent, provider disclosure, retention/deletion and threat model |
| Offline and recovery | Offline-first product direction approved; no Aki queue/project implementation | PARTIAL | Offline capability matrix, no-upload guarantee, cancellation, crash recovery and safe local export |
| Localization/accessibility | Amharic-first/English and accessibility requirements recorded | PARTIAL | Screen-level Amharic/English strings, Ethiopic font/rendering checks and WCAG 2.2 AA acceptance |
| Licensing/supply chain | IROKA proprietary declaration observed; Aki dependency/model inventory absent | UNRESOLVED | Exact dependency/model versions, licenses, provenance, SBOM and redistribution rights |
| Freemium entitlements | Tier direction recorded; no payment provider selected | PARTIAL | Local entitlement model and feature matrix; payment provider remains a separate decision |
| Verification strategy | Product acceptance direction exists; no Aki executable test suite | MISSING | Measurable tests, fixtures, thresholds, device matrix and evidence format |

## 7. Binding constraints for the remaining A2 work

1. Preserve A1 v1.0 and A2-ADR-001 unchanged. Any new product or architecture decision must be a separately identified, versioned record.
2. Do not copy IROKA proprietary source code into Aki Studio. Any future integration must use an explicitly approved interface and separately reviewed rights.
3. Do not treat draft PRs, candidate branches, synthetic adapters or isolated CI runs as production capability evidence.
4. Do not send source images to a remote provider without explicit, informed, task-specific consent and a reviewed data-flow/retention contract.
5. Do not make core editing, print or export depend on network availability.
6. Do not claim an ID/passport preset is officially compliant without jurisdiction-specific authoritative provenance and current verification.
7. Preserve genuine facial characteristics in official-document workflows; distinguish document processing from creative portrait retouching.
8. Keep payment-provider selection, paid model activation, subscriptions, purchases and cloud expenditure outside this assessment.
9. No implementation, merge, production credentials, provider activation or deployment is authorized.

## 8. Remaining A2 decision and evidence register

| ID | Required decision/evidence | State |
|---|---|---|
| AK-A2-01 | Aki-owned gateway boundary, host ownership and versioned request/response contracts | OPEN |
| AK-A2-02 | Native app-to-gateway authentication, token lifecycle and authorization mapping | OPEN |
| AK-A2-03 | Aki image reference model, local/remote data flow, consent and deletion semantics | OPEN |
| AK-A2-04 | Capability catalogue and safe task lifecycle: admission, status, progress, cancellation, idempotency and recovery | OPEN |
| AK-A2-05 | Local/cloud placement rules and offline capability matrix | OPEN |
| AK-A2-06 | Candidate imaging engines, segmentation/enhancement models, licenses and device compatibility | OPEN |
| AK-A2-07 | Non-destructive full-resolution renderer and memory/performance budgets | OPEN |
| AK-A2-08 | Print layout/export contract and physical-size acceptance fixtures | OPEN |
| AK-A2-09 | Official-photo template provenance and jurisdictional verification policy | OPEN |
| AK-A2-10 | Screen-by-screen UX, Amharic-first content, accessibility and lower-spec device behavior | OPEN |
| AK-A2-11 | Privacy/security threat model, dependency/model supply-chain review and SBOM approach | OPEN |
| AK-A2-12 | Test strategy, measurable acceptance thresholds, effort/dependency plan and risk register | OPEN |
| AK-A2-13 | Freemium entitlement model without selecting a payment provider or creating financial commitments | OPEN |

A2 closure requires these items to be reconciled into one coherent, versioned Aki Studio technical baseline with traceable evidence and explicit Owner review. A2 closure must not be represented as A3 authorization.

## 9. Assessment limitations

- This is a bounded first evidence pass, not a full audit of every IROKA branch, PR, artifact, workflow or historical repository.
- The IROKA Android and INNIA A2 reports are dated 19–21 November 2026 and describe their inspected refs. Their findings are not silently promoted to current runtime verification.
- No build, test, device benchmark, image-quality evaluation, license scan, SBOM generation, legal review or external security review was run for Aki Studio.
- The Aki repository's documentation records include regenerated archival reconstructions. They preserve known decisions but are not verbatim original chat exports or authenticated source artifacts.
- Absence claims are limited to the files and repository refs actually inspected.

## 10. Current stage disposition

| Gate | Status |
|---|---|
| A1 product inception v1.0 | APPROVED / VERIFIED-CLOSED |
| A2-ADR-001 IROKA integration direction | APPROVED |
| A2 technical reconciliation | PARTIAL / NOT CLOSED |
| A3 implementation | NOT AUTHORIZED |
| A4 verification | NOT STARTED |
| A5 deployment/release | NOT AUTHORIZED |
| Expenditure / paid services | NOT AUTHORIZED |

**Immediate next A2 task:** resolve the Aki-specific gateway, image-data, imaging-engine and local/cloud placement contracts against exact, approved IROKA service interfaces and candidate technology evidence. Do not start A3 until the complete A2 package is presented for Owner review and separately authorized.

---

**End of AKIDS-A2-TECH-RECON-001 v0.1.0 — first read-only evidence pass.**
