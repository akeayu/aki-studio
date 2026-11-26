# Aki Studio — IROKA G01 Native Authentication and Private Task-Service Reconciliation

**Document ID:** AKIDS-A2-IROKA-RECON-001  
**Version:** 0.1.0  
**Status:** A2 EVIDENCE RECONCILIATION — PARTIAL / NOT CLOSED  
**Owner:** Akefa Ayele Daka  
**Prepared:** 26 November 2026  
**Parent:** AKIDS-A2-TECH-PROP-002 v0.1.0  
**Governing product decision:** AKIDS-A2-ADR-001 — APPROVED  
**Repository:** akeayu/aki-studio  
**Branch:** docs/a2-technical-reconciliation-v0.1.0

## 1. Determination

The repository evidence supports a concrete IROKA G01 credential-verification contract, but it does not establish a production native sign-in issuer or a production private task-service integration.

The controlling distinction is:

- G01 verifies a signed credential and creates an opaque verified principal.
- G01 tenant resolution derives a tenant context from Owner-governed server-side membership.
- G02 resolves organizational authority and capability scope.
- A native Android OAuth/PKCE flow is documented as a proposal, not implemented.
- The private task-service candidate uses an injected workload authenticator. It does not invoke G01 or G02 and is a TALORA-specific development prototype.

**Aki integration disposition:** retain Aki's own public gateway boundary. Do not connect the Android application directly to IROKA, reuse the TALORA audience, or treat the task-service prototype as an approved production API.

## 2. Exact repository and branch evidence

### 2.1 IROKA canonical main

- Repository: akefadaka-git/iroka — https://github.com/akefadaka-git/iroka
- Repository ID: 1445234713
- Current main SHA observed: 6b7004c897907f5ca40de58fee7222732fd7cde0
- Main commit date: 21 November 2026
- Main workspace contains crates/iroka-kernel; it does not contain the TALORA task API crate.
- G01 credential-verifier source blob: crates/iroka-kernel/src/identity_crypto.rs, blob 1ae42e7439129af0d27148ccc9f8f0b11cd28575.
- Verified-principal source blob: crates/iroka-kernel/src/identity.rs, blob 36b4230be95e4f72f79e1d4b6b1b29e9062de051.
- Main G01 contract: https://github.com/akefadaka-git/iroka/blob/main/docs/architecture/G01-IDENTITY-CONTRACT.md
- Main credential lifecycle: https://github.com/akefadaka-git/iroka/blob/main/docs/architecture/G01-CREDENTIAL-VERIFICATION.md

### 2.2 G01 tenant-reconciliation candidate

- Branch: feat/a3-8-g01-tenant-reconciliation
- Current branch ref and PR metadata head observed: c46f2b5876c0294481ee22dc0786fded1cdad9a6
- PR: https://github.com/akefadaka-git/iroka/pull/50
- PR state: OPEN / DRAFT / UNMERGED
- PR base branch: feat/a3-8-g02-owner-organization-authority, base SHA 31dd1cff9ab48c7ecedb941e920281f5e1924051
- Tenant resolver source blob: crates/iroka-kernel/src/tenant.rs, blob 8037d94932196f9c9a664b81d4d21c40843789e3

**Lineage discrepancy:** the PR description separately states a latest head of aa9b1d8bea049cdcb244c08a7f6e7d17673d2f79, while the live branch ref and PR metadata both resolve to c46f2b5876c0294481ee22dc0786fded1cdad9a6. The stated later SHA is not accepted as the current branch head without further exact-ref evidence. The PR description's CI claims are not treated as proof for the live branch ref until this discrepancy is reconciled.

The branch is a candidate, not a merged or production-activated G01/G02 service.

### 2.3 Native Android A2 evidence

- Branch: architecture/a2-android-integration-reconciliation
- Current branch head observed: d560b50c5dd6dbe1296f569f8fe8cbe9155820fe
- Document: docs/mobile/IROKA-ANDROID-A2-READONLY-INTEGRATION-RECONCILIATION-v0.1.0.md
- Document blob: ca79904946097c56aed9babcc5096d221ded3915
- Direct reference: https://github.com/akefadaka-git/iroka/blob/architecture/a2-android-integration-reconciliation/docs/mobile/IROKA-ANDROID-A2-READONLY-INTEGRATION-RECONCILIATION-v0.1.0.md

This document proposes Authorization Code + PKCE (S256) through the system browser/Custom Tabs, a public native client without a client secret, and a short-lived G01 credential. It explicitly records that the issuer, client registration, token lifecycle and native login implementation have not been established.

### 2.4 Private task-service candidate

- PR: https://github.com/akefadaka-git/iroka/pull/129
- Branch: integration/talora-a3-private-api
- Current branch head: 00b323750fb120f80d56191069d10a825e66dc41
- Verified code head stated in the PR: 8056cea136f42436d896078046796eba2129092b
- Task-service source blob: crates/iroka-talora-api/src/lib.rs, blob 5f3e52871be433ee796915aad5e4cb55e71e6b2b
- PR state: OPEN / DRAFT / UNMERGED
- PR base: main at 1aea1447eadf1d827d702fd6c4034fa058aac21e
- Comparison against current main 6b7004c897907f5ca40de58fee7222732fd7cde0: 35 commits ahead, 1 behind.
- The PR records passing exact-code-head CI run 44457815902 and current-branch-head CI run 44458023955. These are isolated branch checks, not integrated service or production-host verification.

## 3. Actual G01 credential contract

The inspected G01 verifier accepts a compact JWS with exactly three base64url segments.

| Element | Actual verifier behavior |
|---|---|
| Algorithm | Ed25519 / EdDSA only |
| Protected type | IROKA-CREDENTIAL |
| Key selection | Non-empty kid, matched to a trusted issuer/key record |
| Required signed claims | iss, sub, exact string aud, exp, iat, jti |
| Optional claim | nbf |
| Unknown fields | Rejected |
| Maximum token size | 16 KiB |
| Maximum credential lifetime | 3,600 seconds |
| Clock skew | 30 seconds |
| Revocation | Issuer-scoped credential and key checks; unavailable revocation state fails closed |
| Trust | Root-pinned, signed, versioned trust bundles with anti-rollback and controlled key rotation |

Production verifier construction is tied to a verified signed trust bundle and a RevocationChecker. The repository does not contain a live private issuer key or production user-credential issuance service.

### 3.1 Verified principal is not an entitlement token

VerifiedPrincipal exposes the verified subject, issuer and credential ID. It does not itself contain an authoritative tenant membership, organization role, workspace permission or Aki feature entitlement.

The G01 tenant resolver in the candidate branch accepts a verified principal and a tenant selector, then checks the Owner-governed membership record. It returns an immutable TenantContext with tenant ID, membership version and resolution time. Inactive, missing, stale or unavailable membership fails closed. The inspected implementation uses SQLite and explicitly limits that adapter to single-node use.

G02 organization authority remains a separate authority source. A valid G01 credential or active tenant membership does not by itself grant an organizational designation or capability.

### 3.2 Reviewer proof is a different mechanism

The G01 reviewer-proof candidate is a short-lived, one-time cryptographic proof bound to a verified issuer/subject/credential tuple and an exact reviewer, approval, tenant, organization, record, purpose, classification, capability and resource tuple. The PR description specifies a maximum 300-second proof lifetime and 30-second key-binding freshness.

This is not the native sign-in flow and must not be substituted for an access credential. Its issuer-side authoritative key-binding adapter and downstream G05 consumption remain outstanding in the PR description.

## 4. Native Android authentication — reconciled proposal

The IROKA Android A2 document proposes:

1. Android is a public client; no embedded client secret.
2. Authorization Code + PKCE using S256 through the system browser or Custom Tabs, not an embedded WebView.
3. A registered and verified redirect URI, with state validation and OIDC nonce validation where OIDC is used.
4. The trusted issuer, not the Android app, issues the short-lived compact JWS credential.
5. Refresh credentials are opaque, rotated on use, stored server-side as hashes, and revocable by device/session family.
6. Android stores refresh material only through an Android Keystore-backed protection design and excludes it from backups, logs and analytics.
7. Offline drafts are not authorization and require online re-authentication before protected work.

**Status:** proposed only. No issuer endpoint, PKCE exchange, client registration, refresh endpoint, token-family revocation service, device-loss process or native login implementation was verified.

The IROKA Android document's proposed audience iroka-android-api belongs to that separate IROKA Android proposal. It is not an Aki Studio audience and must not be copied into Aki's public contract.

## 5. Actual private task-service contract

PR #129 adds iroka-talora-api as a separate workspace crate on an unmerged branch. Its implementation is a transport-neutral route handler around a bounded TaskService.

### 5.1 Authentication boundary

The task crate defines an injected WorkloadAuthenticator interface. Its authenticate method receives a bearer credential and time, then returns an AuthenticatedCaller or an authentication error.

The service checks the exact audience iroka-talora-api and checks tenant, workspace, project and scope against the returned caller context. It invokes the existing Kernel policy evaluator before capability execution; that policy seam does not replace G01 credential verification, G01 tenant resolution, G02 organization authority or tenant-specific policy partitioning. The PR tests use a test authenticator. The code does not call Ed25519CredentialVerifier, TenantMembershipResolver or the G02 organization-authority resolver.

Consequently, this is an adapter seam, not evidence of G01-integrated user or workload authentication. The caller context must not be populated from untrusted request fields in a production adapter.

### 5.2 Route surface

| Method | Path | Actual candidate behavior |
|---|---|---|
| GET | /api/v1/health/live | Liveness response |
| GET | /api/v1/health/ready | Development-only readiness response |
| GET | /api/v1/capabilities | Authenticated, scope-checked capability catalogue |
| POST | /api/v1/tasks | Authenticated task submission |
| GET | /api/v1/tasks/{task_id} | Authenticated, project-scoped task read |
| GET | /api/v1/tasks/{task_id}/events | Event response; current prototype emits a terminal completed event |
| POST | /api/v1/tasks/{task_id}/cancel | Cancellation response; an existing synchronous task returns conflict rather than being cancelled |

### 5.3 Implemented behavior and hard limitations

- One advertised capability: talora.dev.fake.v1, version 1.0.0.
- Provider class: FAKE_LOCAL; development-only.
- Supported locale tags: am-ET and en; this does not establish language-generation quality.
- The fake adapter reports inference_performed: false; it is not generative inference.
- Remote providers and persistent memory are denied.
- Synthetic-only input classification is enforced.
- The request is text/reference-oriented; there is no Aki image upload session, object store, derivative lifecycle or image-processing contract in this slice.
- Idempotency replay and conflicting-payload behavior are tested.
- Task metadata is held in a bounded process-local in-memory store.
- Execution is synchronous; there is no durable worker queue, durable event replay or effective cancellation of in-flight work.
- The route handler does not own a network listener. TLS, trusted proxy, connection limits, deadlines and host deployment remain unimplemented.

The task API is TALORA-specific. Its audience, capability IDs, request fields, development policy and fake adapter are not approved Aki contracts.

## 6. Required Aki integration boundary

The approved Aki architecture remains:

**Aki Android client → Aki-owned gateway → private IROKA service boundary.**

IROKA must not be directly reachable from the Aki mobile application. Aki retains ownership of customer consent, image references, asset transfer, task presentation, result validation, edit preview, rendering and export.

### 6.1 Public native-client access

Proposed Aki public-client profile:

- Authorization Code + PKCE (S256) through the system browser/Custom Tabs.
- No embedded client secret.
- Aki-specific registered client and redirect URI.
- Short-lived access credential with an exact Aki gateway audience, provisionally akids-intelligence-gateway.
- Issuer, client registration, audience, access lifetime, refresh and device-revocation semantics remain OPEN.
- Authentication is not consent. Each remote image task still requires separate task-scoped consent.

The proposed audience is a design placeholder, not an existing issuer registration. The IROKA verifier's exact-audience check means a token for iroka-android-api or iroka-talora-api is not interchangeable with an Aki gateway token.

### 6.2 Gateway-to-IROKA service authentication

The gateway must use a separate service identity for private IROKA calls. The user credential must not be forwarded to a different audience without an explicitly supported token-exchange or delegation mechanism.

The private request must carry a verifiable, bounded actor-delegation context so IROKA can independently resolve the user principal, tenant membership, current G02 authority and permitted capability. A client-authored JSON subject, tenant, role, scope or capability field is not proof of authority.

**Missing actual interface:** no production Aki-to-IROKA service credential, token exchange, signed actor-delegation envelope, IROKA private network host or G01/G02-integrated task adapter was found in the inspected evidence.

### 6.3 Aki task contract

Aki may retain the proposed Aki-owned task lifecycle from AKIDS-A2-TECH-PROP-002, subject to A2 reconciliation:

- Aki capability identifiers and schemas, never TALORA capability IDs.
- Aki gateway-issued asset IDs and digests, never local Android paths or raw image bytes in task JSON.
- Server-derived principal, tenant, consent and policy context.
- Idempotent task submission, authorized status reads, durable progress/replay, effective cancellation and explicit terminal states.
- Result provenance and structured proposals only; Aki validates and previews before applying an edit.
- No remote task or upload when consent, authority, retention or downstream deletion cannot be proven.

## 7. A2 decision register

| ID | Reconciliation decision / evidence required | State |
|---|---|---|
| AUTH-01 | Select authoritative issuer owner and confirm PKCE/OIDC support | OPEN |
| AUTH-02 | Register Aki public client, redirect URI and exact Aki audience | OPEN |
| AUTH-03 | Define access expiry, refresh rotation, reuse detection, logout and per-device revocation | OPEN |
| AUTH-04 | Decide where G01 verification executes without copying proprietary IROKA source into Aki | OPEN |
| AUTH-05 | Define trusted Aki-gateway-to-IROKA service identity and actor-delegation/token-exchange contract | OPEN |
| AUTH-06 | Bind G01 verified principal to live G01 tenant membership and G02 capability resolution | OPEN |
| TASK-01 | Define first-party IROKA private task host and Aki-specific service contract | OPEN |
| TASK-02 | Replace test workload authenticator with a reviewed G01/G02-aware trusted adapter | OPEN |
| TASK-03 | Establish durable task state, event replay, worker recovery and effective cancellation | OPEN |
| TASK-04 | Establish integrated contract tests on one immutable compatible IROKA commit graph | OPEN |
| TASK-05 | Verify exact branch/PR lineage discrepancies, including PR #50 head evidence | OPEN |

## 8. Security and governance constraints

1. Do not copy IROKA proprietary source into Aki Studio. Public visibility is not a source-use or redistribution grant.
2. Do not adopt the CIW browser-cookie session as Aki native authentication. The IROKA Android A2 report identifies its browser orientation and missing native issuer flow; the separate IROKA governance record also says CIW PR #23 was not adopted and records authorization/lineage defects.
3. Do not treat PR #129's test authenticator as a production verifier.
4. Do not trust caller-supplied tenant, workspace, project, role, scope, consent or capability values without current server-side resolution.
5. Do not treat a G01 authentication result or Kernel ALLOW decision as a G09 execution grant.
6. Do not enable remote Aki image tasks until authentication, consent, downstream processing, retention and deletion are verified.
7. No Aki implementation, IROKA merge, production key provisioning, deployment or expenditure is authorized by this document.

## 9. Disposition

- G01 compact-JWS verifier: **IMPLEMENTED IN IROKA KERNEL; production issuer/activation NOT VERIFIED**.
- G01 tenant membership resolver: **CANDIDATE IMPLEMENTATION ON UNMERGED BRANCH; production authority store NOT VERIFIED**.
- Native Android PKCE sign-in: **PROPOSED; NOT IMPLEMENTED**.
- Private TALORA task route handler: **IMPLEMENTED DEVELOPMENT SLICE; NOT G01-INTEGRATED OR PRODUCTION-HOSTED**.
- Aki-specific native authentication and private task integration: **OPEN**.
- A2 technical reconciliation: **PARTIAL / NOT CLOSED**.
- A3 implementation: **NOT AUTHORIZED**.
- A4: **NOT STARTED**.
- A5 deployment/release: **NOT AUTHORIZED**.

**Immediate A2 priority:** obtain an authoritative issuer and mobile-client registration contract, then define the Aki gateway's user-token verification and separate gateway-to-IROKA delegated service-auth contract. Do not implement against the TALORA test authenticator.

---

**End of AKIDS-A2-IROKA-RECON-001 v0.1.0.**