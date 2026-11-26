# Aki Studio — Intelligence Gateway, Image Governance and Runtime Placement Proposal

**Document ID:** AKIDS-A2-TECH-PROP-002  
**Version:** 0.1.0  
**Status:** PROPOSED — FOR A2 RECONCILIATION / OWNER REVIEW  
**Parent assessment:** AKIDS-A2-TECH-RECON-001 v0.1.0  
**Governing architecture decision:** AKIDS-A2-ADR-001 — APPROVED  
**Product baseline:** AKIDS-A1-INIT-001 v1.0 — APPROVED / VERIFIED-CLOSED  
**Owner:** Akefa Ayele Daka  
**Prepared:** 26 November 2026  
**Repository:** akeayu/aki-studio  
**Branch:** docs/a2-technical-reconciliation-v0.1.0

## 1. Executive proposal

This document proposes a first-party Aki Studio Intelligence Gateway contract and a reconciled image-data, imaging-engine, execution-placement and acceptance model.

The design keeps the approved responsibility boundary intact:

> Aki Studio owns the customer experience and image lifecycle. IROKA provides governed intelligence behind an Aki-controlled boundary. The imaging engine performs pixel operations.

The proposal separates three planes:

1. **Aki client and image plane:** Android application, local image assets, edit document, deterministic image operations, preview, rendering, print and export.
2. **Aki Intelligence Gateway plane:** first-party Aki API, task admission, consent enforcement, asset-transfer control, capability translation, result validation, audit/provenance and lifecycle.
3. **IROKA intelligence plane:** private, authenticated and policy-governed reasoning/orchestration services. IROKA internals are not exposed as customer-facing Aki API contracts.

**Recommended direction:** local-first deterministic editing, with optional, explicit, task-scoped remote intelligence. A remote task must never be required for capture, crop, standard adjustments, print layout or export. No image is uploaded merely because the user opened an AI-capable screen.

This is a proposal, not an approved implementation baseline. All endpoint names, schemas, limits and numeric thresholds below are proposed contract material. No endpoint or service is asserted to exist.

## 2. Design principles and non-goals

### 2.1 Binding principles

- Preserve the approved A1 and A2-ADR-001 records without editing them.
- Aki owns its public API, user consent, local assets, edit history and customer-visible status.
- IROKA is a private backend dependency, not a mobile SDK embedded into the app and not a public Aki API namespace.
- Pixel transforms are deterministic imaging-engine operations. Intelligence may assess, recommend or produce bounded parameters; it does not silently mutate the source.
- Original image bytes remain immutable. Every edit is represented as a non-destructive operation or project revision.
- Local operations remain available without network access.
- Remote processing is opt-in, task-specific, disclosed and revocable before dispatch.
- A task may receive only the minimum image derivative and metadata required for that task.
- No face recognition, identity matching, face database or biometric identification feature is part of this proposal.
- No external provider, model, cloud storage, payment service or paid API is selected or activated here.

### 2.2 Non-goals

This document does not authorize:
- Android implementation, an API host, a database, an object store, model deployment or cloud account;
- copying IROKA proprietary source into Aki Studio;
- inferring an IROKA API contract from TALORA or another candidate branch;
- cloud image processing by default;
- final selection of model weights, provider, SDK versions, Android minimum version or payment provider;
- claims of official passport/visa photo compliance without jurisdiction-specific authoritative sources.

## 3. Logical architecture

~~~text
Aki Studio Android app
  ├─ Capture / Android Photo Picker / document import
  ├─ Local Asset Vault + immutable originals
  ├─ Non-destructive Edit Project
  ├─ Preview / Tile Renderer / Export / Print
  ├─ Local CV and optional local inference adapters
  └─ Aki Gateway Client
       │ authenticated request + task-specific consent
       ▼
Aki Intelligence Gateway (Aki-owned API boundary)
  ├─ Native-client authentication adapter
  ├─ Consent and data-classification enforcement
  ├─ Asset upload-session and derivative control
  ├─ Capability catalogue and policy admission
  ├─ Task lifecycle / idempotency / quotas
  ├─ IROKA adapter (private, versioned contract)
  ├─ Result schema validation / provenance
  └─ Retention / deletion / audit metadata
       │ private service identity; no direct mobile access
       ▼
IROKA governed intelligence services
  ├─ Identity / policy / authority (G01/G02 and approved controls)
  ├─ Capability orchestration / reasoning
  └─ Explicitly admitted intelligence adapters

Gateway results return as structured proposals.
Aki validates, previews and applies them only through Aki's edit engine.
~~~

The gateway is an Aki-owned logical boundary. The physical host, cloud account, network topology, data store, queue and service owner still require A2 evidence and approval. IROKA must not be reachable directly from the mobile app.

## 4. Proposed first-party API surface

### 4.1 Contract namespace and compatibility

- Public base path proposal: /api/akids/v1.
- Contract identifier: akids.intelligence.gateway.v1.
- Content type: JSON for control-plane requests and responses.
- Binary image data is transferred only through a separately authorized asset-transfer mechanism; never embed base64 image bytes in task JSON.
- Unknown major versions must fail closed with a version error.
- Additive optional fields may be introduced within v1 only if old clients can safely ignore them.
- Breaking changes require a new major API version and an explicit migration window.
- Every request/response carries a trace identifier; identifiers must not encode personal or image information.
- All time values are UTC RFC 3339 timestamps. Client wall-clock time is informational, not an authority source.

### 4.2 Proposed endpoints

| Method | Proposed path | Purpose |
|---|---|---|
| GET | /capabilities | List capabilities enabled for this authenticated principal and client version |
| POST | /asset-upload-sessions | Request a narrowly scoped upload session after consent and policy checks |
| POST | /asset-upload-sessions/{id}/complete | Finalize upload and verify byte count, checksum and media metadata |
| DELETE | /assets/{asset_id} | Request deletion of a remote asset and linked derivatives |
| POST | /tasks | Submit one capability task against authorized asset references |
| GET | /tasks/{task_id} | Retrieve task state and result metadata |
| GET | /tasks/{task_id}/events | Poll or stream lifecycle events, depending on verified host transport |
| POST | /tasks/{task_id}/cancel | Request cancellation |
| DELETE | /tasks/{task_id} | Request task/result deletion under retention policy |
| GET | /health | Liveness only; no sensitive internals |
| GET | /ready | Readiness for the advertised contract/capability set |

These are proposed paths only. No corresponding Aki service currently exists in the inspected repository.

### 4.3 Capability catalogue

A capability response should disclose availability and constraints, not just a name.

Proposed fields:
- capability_id: Aki-owned stable identifier, e.g. akids.photo.assess.v1 (illustrative only).
- capability_version.
- availability: available, temporarily_unavailable, unsupported_client, requires_consent, requires_upload.
- execution_modes: local, remote, or hybrid.
- accepted_media: MIME types, maximum dimensions and maximum bytes.
- input_data_classes: required image derivative and permitted metadata.
- locale_support: locale and quality status, not merely locale tags.
- output_schema_version.
- limits: size, concurrency and rate limits.
- retention_class: policy identifier and expiry.
- model_or_engine_provenance: opaque, validated engine/model identifier and version where disclosable.
- reason_unavailable: safe machine-readable reason.

The catalogue must not expose internal IROKA service names, secrets, branch names or infrastructure topology.

### 4.4 Task submission — illustrative JSON

~~~json
{
  "contract": "akids.intelligence.gateway.v1",
  "capability_id": "akids.photo.assess.v1",
  "capability_version": "1",
  "client": {
    "app_version": "0.0.0",
    "platform": "android",
    "locale": "am-ET"
  },
  "asset": {
    "asset_id": "opaque-gateway-issued-asset-id",
    "sha256": "hex-digest",
    "mime_type": "image/jpeg",
    "width": 3024,
    "height": 4032,
    "orientation": "normalized"
  },
  "task_options": {
    "template_id": "user-selected-template-id",
    "assessment_profile": "document-photo-framing"
  },
  "consent_grant_id": "single-task-consent-grant",
  "idempotency_key": "client-generated-random-value"
}
~~~

All identifiers above are placeholders. The app must not send a local file path, raw image bytes, Android content URI, EXIF GPS, full contact identity, tenant/role assertion, policy decision or Owner approval in this payload.

The gateway must derive the authenticated principal, tenant/workspace, permitted scopes and policy context from trusted authentication and server-side authority. Client-supplied values cannot grant authority.

### 4.5 Task state machine

Proposed states:
- accepted: schema and admission checks passed; task identifier issued.
- queued: admitted and waiting for bounded execution.
- running: work has started.
- succeeded: a validated result is available.
- failed: terminal failure with safe error code.
- cancel_requested: cancellation has been requested but not confirmed.
- cancelled: execution stopped and no further work will be performed.
- expired: task/result retention window ended.

Rules:
- Every task has one immutable task ID, creation time, capability version and idempotency key.
- Repeated submission with the same principal, capability and idempotency key returns the same task outcome; it must not duplicate work.
- A cancellation acknowledgement is not the same as confirmed cancellation.
- A task cannot transition from a terminal state back to running.
- Result delivery is idempotent.
- Timeouts and retry limits are bounded. Automatic retries must not create duplicate provider charges or duplicate processing.
- The client must display unavailable, pending, failed and cancelled states distinctly; never present a synthetic test response as completed intelligence.

### 4.6 Result envelope

A successful result must include:
- task_id, capability_id, capability_version, result_schema_version;
- status, created_at, completed_at;
- summary in a supported locale, where available;
- findings[] with machine-readable code, severity, confidence/uncertainty and explanation;
- proposed_operations[] using an Aki-owned, allowlisted edit-operation schema;
- provenance: engine/model ID and version, execution mode, processing timestamp, source asset digest and policy/version references;
- limitations[] and warnings[].

A result is a recommendation, not an instruction to overwrite an image. Aki must validate the schema and supported operation bounds, show a preview, and require the user to apply the change. Unknown operations must be rejected, not guessed.

### 4.7 Error contract

Stable machine-readable error codes should include:
- AUTH_REQUIRED, AUTH_EXPIRED, AUTH_REVOKED;
- CONSENT_REQUIRED, CONSENT_EXPIRED, CONSENT_REVOKED;
- CAPABILITY_UNAVAILABLE, CAPABILITY_UNSUPPORTED;
- ASSET_NOT_FOUND, ASSET_EXPIRED, ASSET_CHECKSUM_MISMATCH, ASSET_TYPE_UNSUPPORTED;
- INPUT_TOO_LARGE, INPUT_INVALID;
- RATE_LIMITED, QUOTA_EXCEEDED;
- TASK_NOT_FOUND, TASK_TERMINAL, CANCELLATION_UNAVAILABLE;
- POLICY_DENIED, UPSTREAM_UNAVAILABLE, UPSTREAM_TIMEOUT;
- CONTRACT_VERSION_UNSUPPORTED, INTERNAL_ERROR.

Responses must include trace_id, retryability and a safe user-facing message. Do not return stack traces, provider secrets, raw model prompts, internal policy text or image content in error payloads.

### 4.8 Authentication and trust

The native authentication flow remains OPEN pending reconciliation with the actual IROKA G01 authority implementation.

Proposed security requirements:
- Public native app; no client-embedded shared secret.
- Short-lived access credentials, audience-bound to the Aki Gateway, with narrow scopes.
- Token refresh, rotation, revocation and lost-device response must be defined by the authoritative identity service.
- Use an established native public-client flow (for example, authorization-code with PKCE if the selected identity provider supports it); this is a candidate pattern, not a selected provider or approved G01 contract.
- Gateway verifies token signature, issuer, audience, expiry, scopes and revocation/status using the approved authority interface.
- Gateway-to-IROKA uses separate service identity and least privilege.
- User consent is a separate task-scoped authorization record; authentication is not consent.
- Consent cannot override a server-side policy denial.
- Do not trust client-supplied tenant, role, entitlement, capability, consent or Owner-authority claims.

No concrete token issuer, endpoint, signing key, database or production trust configuration has been verified.

## 5. Image-data governance and transfer model

### 5.1 Data classes

| Class | Examples | Default handling |
|---|---|---|
| Original image | Camera JPEG/HEIC, imported original | Local immutable asset; never upload by default |
| Working derivative | Downscaled, orientation-normalized image | Local only unless task-specific remote consent is granted |
| Remote task derivative | Minimum-resolution crop or normalized copy for one approved task | Upload only after explicit consent and policy admission |
| Edit project | Crop, layers, masks, parameters, history | Local project storage; remote sync not assumed |
| AI task metadata | Capability, dimensions, locale, template ID | Minimize; omit identifying metadata |
| Result/provenance | Findings, suggested operations, engine/version | Local project association; remote retention bounded |
| Diagnostics | Crash/error/latency metadata | No image bytes, face crops, filenames, GPS or raw prompts |

### 5.2 Local asset model

- Use an app-controlled asset identifier, not a file path, as the stable project reference.
- Preserve original bytes read-only; compute a cryptographic digest when importing.
- Normalize orientation for processing without rewriting the original.
- Keep original EXIF in the original asset only. Remove GPS/location and unnecessary device metadata from remote derivatives.
- Generate derivatives explicitly per task; record dimensions, MIME type, digest and transformation lineage.
- Use Android Photo Picker / Storage Access Framework to obtain user-selected media access instead of broad media-library permissions where possible. Android documents the Photo Picker as granting access to selected items rather than the entire library: https://developer.android.com/training/data-storage/shared/photo-picker?hl=en
- A selected remote/cloud-backed photo must be materialized locally and checked before processing; selection from a picker is not permission to send it to an AI provider.
- Use app-private storage for intermediate files and encrypted-at-rest storage for sensitive project data where platform/device support permits. Key custody and backup policy require a separate threat-model decision.
- Temporary working files must be cleaned after success, failure, cancellation and crash recovery.

### 5.3 Consent sequence

For every remote image task:
1. User chooses a specific AI function.
2. Aki explains the exact purpose, whether the image or a derivative leaves the device, the processing party/category, retention period, and whether the feature can work locally.
3. User explicitly confirms this task.
4. Aki creates a consent record bound to principal, asset digest, capability, purpose, disclosure version and expiry.
5. Gateway validates the grant and policy before issuing an upload session.
6. Only the approved derivative is transferred.
7. Gateway verifies checksum, type, dimensions and policy before dispatch.
8. Aki presents result and provenance.
9. Asset/task data expires or is deleted under the declared retention contract; user may request deletion earlier.

No consent checkbox may be preselected. Consent for one task must not authorize unrelated tasks, model training, product analytics or future uploads.

### 5.4 Remote transfer and retention

Proposed controls:
- TLS-protected upload to a gateway-authorized destination; no public object URL.
- Short-lived, single-purpose upload authorization bound to one asset, task, checksum and size ceiling.
- No arbitrary user-controlled URL fetch (SSRF prevention).
- Verify actual MIME type and decode safety; do not trust filename or client declaration.
- Maximum upload size and image dimensions are capability-specific and disclosed before transfer.
- No provider training or secondary use of user images unless a separate explicit policy and consent decision is made; default is prohibited.
- Raw image bytes must not appear in application logs, tracing, crash reports or support diagnostics.
- Proposed default remote retention: delete task image derivatives as soon as processing is complete and no later than 24 hours after terminal task state; task metadata/result may be retained only under a separately disclosed, minimal retention schedule. This is a target for contractual verification, not a claim about any current IROKA/provider behavior.
- User deletion request should cascade to the gateway asset, derivatives, task result and any downstream processor within a documented deadline. Backups and legal holds must be disclosed and bounded.
- If the gateway cannot prove deletion/retention behavior for all downstream processors, the remote capability must remain disabled for personal images.

### 5.5 Image-data threat model

| Threat | Required mitigation |
|---|---|
| Accidental upload | Local-by-default architecture; explicit task consent; network tests proving no upload in local workflows |
| Wrong asset sent | Asset digest bound to consent and task; preview of selected asset; server checksum verification |
| Metadata leakage | Strip GPS and unnecessary EXIF from derivative; no filenames in logs |
| Public link leakage | Short-lived scoped upload authorization; no public bucket/object access |
| Cross-user asset access | Principal-bound asset ownership and authorization on every read/delete |
| Replayed task | Idempotency key, nonce/expiry and task-principal binding |
| Provider retention/training | Contractual processor terms and verifiable deletion/zero-training policy before enabling |
| Log/trace leakage | Structured allowlisted logs; redact tokens, image content, prompts and identifiers |
| Malicious image file | MIME sniffing, safe decode limits, pixel-count limits, parser patching and sandboxed processing |
| Device loss | App-private storage, key-protected encryption, no cloud backup by default until separately assessed |

## 6. Imaging-engine selection proposal

No single library should own every pixel operation. Use a replaceable adapter architecture and select engines by workload.

### 6.1 Candidate stack

| Layer | Candidate | Proposed role | Assessment |
|---|---|---|---|
| Android UI | Kotlin + Jetpack Compose | Customer-facing workflows and controls | Retain as A1 candidate; verify supported Android/device matrix in A2 |
| Camera | AndroidX CameraX | Preview, capture and image analysis | Preferred Android capture candidate; official docs provide Preview, ImageCapture and ImageAnalysis use cases: https://developer.android.com/media/camera/camerax/architecture |
| Import | Android Photo Picker / Storage Access Framework | User-selected image access | Preferred privacy boundary; avoid broad library permissions where possible: https://developer.android.com/training/data-storage/shared/photo-picker?hl=en |
| Deterministic transforms | Android Bitmap/Canvas and a dedicated Kotlin imaging module | Crop, rotate, color matrices, overlays and export | Preferred first implementation candidate; keep operations non-destructive and test color/EXIF behavior |
| Computer vision | OpenCV 4.5+ | Geometry, image processing, masks and selected classical CV | Strong candidate, not yet selected. OpenCV states 4.5.0+ uses Apache 2.0; older versions use 3-clause BSD. Pin and review exact artifact: https://opencv.org/license/ |
| Face detection | ML Kit Face Detection | On-device face box/landmarks for framing assistance | Candidate for assisted framing only; it detects faces and features but does not recognize identity. Model delivery differs between bundled and Play Services options: https://developers.google.com/ml-kit/vision/face-detection/android |
| Segmentation | ML Kit Selfie Segmentation or a separately licensed custom model | Portrait/background mask generation | Candidate requiring image-quality and device tests; ML Kit installation guidance indicates selfie segmentation is bundled-model only: https://developers.google.com/ml-kit/tips/installation-paths |
| Custom inference | ONNX Runtime Mobile | Run approved custom models on-device | Candidate runtime only after model/artifact and device profiling. Android support and model fit are documented; model must fit storage and memory: https://onnxruntime.ai/docs/tutorials/mobile/ |
| Print/PDF | Android platform PDF facilities and Aki-owned layout module | Page layout, physical dimensions and PDF export | Prefer platform APIs plus deterministic layout; validate pixel-to-point conversions and printer output |
| Optional native acceleration | NDK/C++ kernels | Only proven hotspots | Defer until profiling justifies complexity and ABI/support cost |

### 6.2 Candidate disposition

**Preferred baseline for further A2 validation:**
- CameraX for camera lifecycle/capture.
- Android platform image APIs + Kotlin-owned non-destructive operation model for core edits.
- OpenCV as an optional, isolated CV module after exact version/license review.
- ML Kit Face Detection for optional on-device face-framing assistance, subject to dependency terms, device availability and offline-model behavior.
- Evaluate segmentation separately; do not assume selfie segmentation is equivalent to studio-quality hair/edge matting.
- Keep ONNX Runtime Mobile as a replaceable custom-inference candidate, not a mandatory app dependency until one exact model has been tested and licensed.

This is a candidate architecture, not final technology selection.

### 6.3 Important caveats

- ML Kit's face detector is a detector, not identity recognition. The product must not build face identity profiles or use tracking IDs as persistent user identity.
- ML Kit's bundled/unbundled choices affect app size, first-run readiness and dependency on Google Play Services. An unbundled model may require download before first use; therefore it cannot be the sole path for an offline-first feature.
- ONNX Runtime's MIT license applies to the runtime code, not automatically to model weights, training data, model outputs or third-party operators. Each artifact needs its own license/provenance review.
- OpenCV's license changed by version: 4.5.0+ Apache 2.0; 4.4.0 and earlier use 3-clause BSD. Pin the exact version and include notices.
- CameraX and ML Kit official docs establish API capabilities, not performance guarantees on Aki's target devices.
- No candidate model may alter genuine facial characteristics in official-document workflows.
- Do not use generative fill or portrait beautification for official ID/passport output unless the operation is explicitly allowed by the relevant authority; default official-photo workflow should preserve the source subject.

### 6.4 Imaging pipeline and edit document

Recommended pipeline:
1. Import original and calculate digest.
2. Decode safely; normalize orientation into a processing view.
3. Generate preview proxy; retain full-resolution source.
4. Store edits as a versioned operation graph.
5. Render preview using the same operation semantics as export.
6. Render full-resolution output in bounded tiles/strips where required.
7. Validate output dimensions, color profile, metadata policy and digest.
8. Export to a user-selected destination; never overwrite the original by default.

Each operation should carry:
- stable operation type and schema version;
- parameters in normalized units where possible;
- source coordinate space and transform order;
- mask/selection reference if applicable;
- engine implementation/version where output depends on engine behavior;
- creation timestamp and undo/redo linkage.

A preview proxy must not become the final export source. Export must re-evaluate the edit graph against the original-resolution asset.

## 7. Local, remote and hybrid placement policy

### 7.1 Placement matrix

| Capability | Required mode | Network requirement | Remote image allowed? |
|---|---|---|---|
| Camera capture/import | Local | None | No |
| Crop, rotate, resize, perspective | Local | None | No |
| Basic exposure/color adjustments | Local | None | No |
| Layers, masks, text, frames | Local | None | No |
| Standard print layout/PDF/JPEG/PNG export | Local | None | No |
| Face framing/eye-line guidance | Local preferred | None for bundled/available local model | No |
| Basic quality checks (blur, exposure, dimensions) | Local | None | No |
| Background removal | Local preferred; remote optional only after separate consent | Local model availability or explicit online task | Only for explicit remote task |
| Advanced portrait enhancement | Local where approved model exists; otherwise opt-in remote or unavailable | Optional | Only for explicit remote task |
| AI recommendations / task planning | Local deterministic rules first; remote IROKA optional | Optional | Only minimum task derivative if image-dependent |
| Official template dimensions/rules | Local versioned catalogue | Updates optional | No |
| Amharic UI and core help | Local | None | No |
| Cloud backup/sync | Not in initial baseline | Network | Separate future decision and consent |

### 7.2 Runtime selection rules

1. Prefer a deterministic local algorithm where quality is sufficient.
2. If a supported local model is installed and its quality/resource profile passes, offer local execution.
3. If only remote execution is available, disclose that fact and request task-specific consent before creating an upload session.
4. If the user declines, the network is unavailable, the model is unavailable or the policy denies the task, provide a safe local alternative or mark the feature unavailable.
5. Never silently switch from local to remote.
6. Never queue a remote image upload for later without the user being told and explicitly authorizing deferred upload. A queued request must expire and re-check consent, identity and policy before execution.
7. Remote failure must not block local editing, project saving or export.
8. The gateway is authoritative for remote capability admission; the client may not self-grant a capability based on a cached catalogue.

### 7.3 Offline behavior

Offline core must include:
- open/create local project;
- capture/import user-selected image;
- crop/rotate/resize/basic adjustments;
- frames/text/masks where supported;
- preview and full-resolution export;
- print sheet composition and PDF/JPEG/PNG export;
- save, reopen, duplicate and delete local project.

Offline mode may omit:
- remote IROKA reasoning;
- remote segmentation/enhancement;
- cloud sync or backup;
- capability catalogue refresh;
- online official-template updates.

The app must show a clear offline state and must not represent an unavailable remote capability as running.

## 8. Proposed measurable acceptance criteria

All thresholds below are **provisional A2 targets for validation**, not achieved results or final approved release gates. The test device set, labelled image corpus, measurement procedure and Owner acceptance must be frozen before A2 closure.

### 8.1 Functional and contract tests

| ID | Proposed acceptance target | Verification |
|---|---|---|
| API-01 | 100% of malformed, unsupported-version and unauthorized contract fixtures rejected safely | Automated contract tests |
| API-02 | Repeated identical idempotency key produces one logical task and no duplicate execution | Replay test |
| API-03 | Every accepted task reaches one terminal state or is explicitly expired; no terminal-to-running transition | State-machine/property tests |
| API-04 | Unknown result operation is rejected and cannot mutate the edit project | Schema/fuzz tests |
| API-05 | Client cannot elevate tenant, role, capability, consent or Owner authority through request fields | Negative authorization tests |
| API-06 | All API requests/results include trace ID and schema version; logs contain no image bytes, tokens or GPS | Contract/log inspection |
| API-07 | Cancellation is reported as complete only after worker termination is confirmed | Race and cancellation tests |

### 8.2 Image integrity and editing

| ID | Proposed acceptance target | Verification |
|---|---|---|
| IMG-01 | Original asset digest remains unchanged after all edit/export workflows | SHA-256 before/after |
| IMG-02 | Preview and full-resolution export use identical operation ordering and geometry semantics | Golden-image comparison |
| IMG-03 | Crop/rotate/resize output dimensions match requested values exactly, subject only to explicitly documented rounding | Pixel-dimension fixtures |
| IMG-04 | No unintended aspect-ratio distortion; geometry error no greater than 1 pixel on deterministic synthetic fixtures | Geometry test |
| IMG-05 | JPEG/PNG export opens in independent decoders; metadata follows the declared privacy policy | Interoperability test |
| IMG-06 | 300 DPI export has correct physical-size metadata and expected pixel dimensions; no claim that DPI metadata adds detail | File inspection and printed ruler test |
| IMG-07 | Undo/redo and project reopen reproduce the same output within defined deterministic tolerance | Round-trip tests |

### 8.3 CV and model quality

| ID | Provisional target | Verification |
|---|---|---|
| CV-01 | Face-framing detector recall ≥98% on the defined frontal/near-frontal validation subset; false-positive rate ≤1% on no-face subset | Human-labelled representative corpus |
| CV-02 | Assisted eye-line/face-box placement error ≤2% of face height on the supported pose subset | Annotated landmark comparison |
| CV-03 | Background-mask median IoU ≥0.90 on the defined portrait validation set; 10th percentile ≥0.80 | Pixel-labelled segmentation corpus |
| CV-04 | Hair, glasses, ears and shoulder edge quality separately scored; no aggregate IoU alone can conceal systematic edge failure | Stratified human review |
| CV-05 | Model uncertainty/low-confidence cases produce a user review or manual fallback, never silent acceptance | Adversarial/low-quality image tests |
| CV-06 | Official-document workflows preserve genuine facial features; no generative identity alteration | Side-by-side review and operation allowlist |

These targets are aspirational until a lawful, representative, consented test corpus and ground-truth annotation protocol exist. The model must be evaluated across lighting, skin tones, hairstyles, head coverings, glasses, camera quality and background types. No demographic performance claim may be made without stratified evidence.

### 8.4 Performance and device compatibility

| ID | Provisional target | Verification |
|---|---|---|
| PERF-01 | On the agreed entry-tier reference device, 12 MP import-to-editable-preview p95 ≤3 seconds | Repeated benchmark, cold/warm |
| PERF-02 | Common preview adjustment response p95 ≤150 ms while editing | Frame/interaction profiling |
| PERF-03 | 12 MP JPEG export p95 ≤8 seconds on the agreed reference device | Repeated benchmark |
| PERF-04 | Peak app memory during 12 MP edit/export ≤35% of physical RAM and no out-of-memory termination in 100 repeated runs | Memory profiler/stress test |
| PERF-05 | 24 MP source either processes successfully with bounded tiling or fails early with a clear safe limit; no device freeze | Stress test |
| PERF-06 | Core local workflows pass airplane-mode tests | Network-disabled device tests |
| PERF-07 | Battery/thermal throttling is measured during 20 consecutive exports; no unbounded background work | Long-run thermal test |

The entry-tier device model, RAM, Android API, CPU/GPU and storage must be selected from actual target-market evidence. Do not freeze a minimum Android version or RAM floor from these provisional thresholds alone.

### 8.5 Privacy, security and offline tests

| ID | Proposed acceptance target | Verification |
|---|---|---|
| PRIV-01 | Zero image or metadata network egress during capture, crop, basic edit, save and export when no remote feature is requested | Packet capture/proxy test |
| PRIV-02 | Remote image transfer occurs only after an explicit, task-specific consent event | UI-to-network audit |
| PRIV-03 | Consent binds to one asset digest, capability, purpose and disclosure version; reuse for another task is denied | Negative consent tests |
| PRIV-04 | Remote derivative contains no GPS and no unnecessary EXIF | Metadata inspection |
| PRIV-05 | Remote assets/derivatives meet the proposed ≤24-hour terminal-state deletion target, with earlier user deletion supported | End-to-end retention/deletion evidence |
| PRIV-06 | No image bytes, face crops, access tokens, local paths or GPS appear in logs/crash reports | Log and crash-report inspection |
| PRIV-07 | Cross-principal asset read/delete attempts are denied in 100% of authorization fixtures | Tenant isolation tests |
| PRIV-08 | If remote deletion or provider retention cannot be evidenced, image-dependent remote capability remains disabled | Release gate inspection |

### 8.6 UX, localization and accessibility

| ID | Proposed acceptance target | Verification |
|---|---|---|
| UX-01 | Amharic is available for all critical capture, consent, editing, error and export workflows at initial release | String and workflow audit |
| UX-02 | No clipped or missing Ethiopic glyphs in supported device/font matrix | Screenshot/glyph review |
| UX-03 | Critical actions meet 48 dp minimum touch-target guidance unless platform accessibility guidance requires larger | UI inspection |
| UX-04 | Contrast, focus order, TalkBack labels and scalable text meet WCAG 2.2 AA target | Accessibility audit |
| UX-05 | Remote/local processing mode and consent status are visible before and during task execution | Usability test |
| UX-06 | No user is told an official preset is compliant unless the preset has verified jurisdiction, authority, source and effective date | Content/preset audit |

## 9. Licensing and supply-chain admission gates

Before selecting any dependency, SDK, model or model dataset:

1. Record exact package/model name, version, source URL, checksum and maintainer.
2. Record license identifier and all bundled/transitive license obligations.
3. Separately review code license, model-weight license, training-data provenance and commercial/redistribution terms.
4. Record telemetry/network behavior and any external service dependency.
5. Generate and retain an SBOM for the exact release artifact.
6. Scan for known vulnerabilities and define update/patch ownership.
7. Verify reproducible or otherwise auditable build provenance.
8. Do not copy IROKA proprietary code or internal artifacts into the Aki app or public repository.
9. Do not treat a permissive runtime license as permission to use arbitrary model weights.

Candidate source references:
- OpenCV license: https://opencv.org/license/
- ONNX Runtime Android build: https://onnxruntime.ai/docs/build/android.html
- ONNX Runtime mobile deployment: https://onnxruntime.ai/docs/tutorials/mobile/
- ML Kit model installation paths: https://developers.google.com/ml-kit/tips/installation-paths

## 10. Decisions still required for A2 closure

| ID | Question | Current recommendation | State |
|---|---|---|---|
| D01 | Who owns and hosts the Aki Gateway? | Aki-owned logical API; host/vendor not selected | OPEN |
| D02 | Which G01 native auth contract is authoritative? | Reconcile against exact IROKA authority implementation; no custom embedded secret | OPEN |
| D03 | What is the private IROKA service contract? | Define a narrow Aki adapter; do not inherit TALORA routes/capabilities | OPEN |
| D04 | Which remote processor/storage, if any, is permitted? | None selected; remote image features disabled until privacy/retention terms are evidenced | OPEN |
| D05 | What are the final image size and upload limits? | Capability-specific limits based on device tests and cost/security review | OPEN |
| D06 | Which imaging libraries are selected? | CameraX + platform imaging baseline; evaluate OpenCV/ML Kit/ORT separately | OPEN |
| D07 | Which model(s) and licenses are approved? | None selected | OPEN |
| D08 | What is the supported Android/API/RAM/device floor? | Determine from target-market device evidence and benchmarks | OPEN |
| D09 | What is the final offline feature matrix? | Preserve local core; no silent remote fallback | OPEN |
| D10 | What retention/deletion promise can be contractually verified? | Target ≤24 hours after terminal state for remote image derivatives; verify downstream | OPEN |
| D11 | What image corpus and evaluation protocol are lawful and representative? | Establish consented, stratified, annotated validation set | OPEN |
| D12 | What are final performance and quality gates? | Validate provisional targets on named devices/corpus | OPEN |
| D13 | What official photo presets can be shipped? | Only source-verified, jurisdiction-specific, dated presets | OPEN |
| D14 | What are release signing, SBOM, monitoring and incident-response controls? | Define before A3/A4 handoff | OPEN |

## 11. A2 closure checklist

A2 cannot close until evidence demonstrates all of the following:

- [ ] Aki-owned gateway API contract, schemas, errors, lifecycle and compatibility policy are reconciled.
- [ ] Native authentication is mapped to the actual approved G01 authority contract.
- [ ] A private IROKA service interface is identified and its exact immutable refs/build evidence are verified.
- [ ] Image consent, derivative creation, upload authorization, retention, deletion and provider boundaries are specified.
- [ ] Local/cloud capability matrix and offline behavior are frozen.
- [ ] Imaging engine candidates are compared using exact versions, licenses, model rights and device benchmarks.
- [ ] Non-destructive edit document and full-resolution renderer architecture are specified.
- [ ] Print-size calculations, DPI semantics and export fixtures are validated.
- [ ] Official-photo preset provenance policy is defined.
- [ ] Amharic-first UX, accessibility and supported-device matrix are reconciled.
- [ ] Numeric acceptance criteria are validated against a named device set and lawful test corpus.
- [ ] Privacy/security threat model, dependency inventory and SBOM plan are reviewed.
- [ ] Cost/effort/risk and operational ownership are documented without committing expenditure.
- [ ] Consolidated A2 package is presented for explicit Owner review and decision.

A2 closure is not A3 authorization. A3 must be separately approved after the A2 package is complete.

## 12. Source references and evidence boundary

Official technical references consulted for candidate capabilities:

1. Android CameraX architecture — Preview, ImageCapture and ImageAnalysis use cases: https://developer.android.com/media/camera/camerax/architecture
2. Android Photo Picker — selected-item access and fallback behavior: https://developer.android.com/training/data-storage/shared/photo-picker?hl=en
3. Android CameraX image analysis: https://developer.android.com/media/camera/camerax/analyze
4. Google ML Kit Face Detection for Android — model delivery and API usage: https://developers.google.com/ml-kit/vision/face-detection/android
5. Google ML Kit face detection — face detection does not identify individuals: https://developers.google.com/ml-kit/vision/face-detection/
6. Google ML Kit model installation paths — bundled/unbundled support: https://developers.google.com/ml-kit/tips/installation-paths
7. OpenCV licensing by version: https://opencv.org/license/
8. ONNX Runtime mobile deployment: https://onnxruntime.ai/docs/tutorials/mobile/
9. ONNX Runtime Android build: https://onnxruntime.ai/docs/build/android.html

These references establish documented product/API behavior and project license statements only. They do not prove Aki integration, production suitability, model accuracy, performance on target devices, or legal approval for every transitive artifact.

No Aki application code was built or tested. No model was downloaded or evaluated. No image was uploaded. No remote service, credential, provider, database, object store or payment system was activated.

## 13. Status and authorization

| Stage / item | Disposition |
|---|---|
| A1 v1.0 | APPROVED / VERIFIED-CLOSED — preserved |
| A2-ADR-001 | APPROVED — preserved |
| A2 first repository evidence pass | PARTIAL — recorded in AKIDS-A2-TECH-RECON-001 |
| A2 gateway/image/runtime proposal | PROPOSED v0.1.0 — this document |
| A2 overall | PARTIAL / NOT CLOSED |
| A3 implementation | NOT AUTHORIZED |
| A4 verification | NOT STARTED |
| A5 deployment/release | NOT AUTHORIZED |
| Expenditure / paid services | NOT AUTHORIZED |

**Immediate next A2 action:** obtain evidence for the authoritative IROKA G01 native-client authentication and a compatible private service-host/task contract; in parallel, prepare the target-device and image-corpus evaluation protocol. Keep remote image capabilities disabled until consent, retention and downstream processing can be proven.

---

**End of AKIDS-A2-TECH-PROP-002 v0.1.0 — proposal only, pending A2 reconciliation and Owner review.**
