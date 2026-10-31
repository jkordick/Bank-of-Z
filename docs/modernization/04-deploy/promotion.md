# INQACC promotion: dev -> staging -> production

**Status: document-only, conditional runbook; production promotion is blocked.**
No deployment, traffic change or feature-flag toggle is performed or authorized
by this document. ADR-007 selects controlled-cohort strangler migration, not
live cutover approval
(`docs/modernization/03a-architecture/target-architecture.md:203`,
`docs/modernization/03a-architecture/target-architecture.md:225`).
The inherited platform is Java/Spring Boot in a Linux container on Azure
Container Apps, with ACR, Key Vault, Azure Monitor and Bicep
(`docs/modernization/03a-architecture/target-architecture.md:5`).

The binding contract is
`docs/modernization/03b-rewrite/INQACC/interface.md:1`, especially its two REST
entries, projections, errors and switch conditions at lines 37, 208, 277 and
334. The existing implementation has only fake adapters, no REST projection
or live transaction integration; the CI package is a non-executable core
library, not a deployable service
(`docs/modernization/03b-rewrite/INQACC/interface.md:413`,
`docs/modernization/04-deploy/pipeline.md:14`).
Green core CI is necessary, not sufficient. No current successful workflow run,
image, deployed environment or accepted routing product is established here.

## Release record and blocking inputs

Before the first environment promotion, create an access-controlled release
record outside source control. Carry its evidence and approvals through every
hop; never put credentials, banking payloads or captured traffic in CI artifacts.

| Required field | Gate |
|---|---|
| Release identity | Commit SHA, successful Modernized CI run/attempt, verified package checksums, interface/matrix revision, immutable image digest and build provenance. A rebuilt image is a new candidate and repeats the gates. |
| Accountable reviewers | Named business/domain acceptor, security/identity owner, mainframe/data/recovery owner, Azure/network operator, seam operator and incident commander; approval timestamps and escalation contacts. Missing ownership blocks promotion. |
| Service and data contract | Approved production runtime versions, complete REST implementation, supported read path and consistency/isolation contract under concurrent legacy updates, deployed schemas/value domains, diagnostic and full caller-unit-of-work recovery equivalence. Forwarding unchanged INQACC is not a migrated inquiry. |
| Identity contract | Accepted account-level allow/deny policy, backend identity/attribution, credential format, TLS trust and fail-closed behavior on both routes. Do not silently choose between ADR-006's open alternatives. |
| Operational acceptance sheet | Per-operation p95/p99 limits, technical-error and availability limits, mainframe/target capacity limits, readiness and telemetry-loss triggers, evaluation windows, minimum samples, full-rollout soak, maximum route convergence and drain times. Every numerical value and measurement query must be approved before traffic moves. |
| Route execution sheet | Accepted router/product, operator access, exact method/path/cohort rules, active configuration/version, change and restore procedures, private destinations, convergence/read-back procedure, emergency approver and rehearsal evidence. No generic product command is treated as executable. |
| Recovery evidence | Staging route-back, partial route propagation, in-flight timeout, secret/backend/telemetry loss, and data/diagnostic reconciliation results; retained legacy capacity must cover 100% of these GETs. |

These are unresolved acceptance inputs, not optional follow-up work. They
implement the architecture gates for ownership, identity, data consistency,
numerical limits and explicit route-back
(`docs/modernization/03a-architecture/target-architecture.md:225`,
`docs/modernization/03a-architecture/target-architecture.md:227`,
`docs/modernization/03b-rewrite/INQACC/interface.md:345`).
Until the read-path and caller-unit-of-work contracts are demonstrated, there
is no safe production replacement destination.

## Promotion gates

All steps below are instructions for separately approved future execution.
Environment creation, application activation and business routing are distinct
approvals; none implies the next.

| Hop | Required execution and evidence | Approval / stop condition |
|---|---|---|
| Commit -> verified candidate | Require the existing Modernized CI build, unit drivers, all phase-3b acceptance cases reconciled against the matrix, Semgrep and Bicep/Checkov gates to succeed for the exact SHA. Retain reports before their CI retention expires. A future service build must also gate HTTP/adapters, dependency and container vulnerability scans, image provenance and digest packaging; these service-release stages are not present today. | Release engineer verifies a real green run and checksums, not just the workflow definition. Missing/failed/skipped gate means no promotion. Current core cases are INQACC-T01 through T48, not live parity (`docs/modernization/04-deploy/pipeline.md:50`, `docs/modernization/03b-rewrite/INQACC/acceptance-tests.md:191`). |
| Candidate -> dev | Compile/lint/scan IaC with dev inputs; review a site-specific resource change plan separately. Create only approved foundations first. Populate named vault references privately; publish the tested service image by digest. Activate private compute only after the service/adapters exist, with external replacement routing still at zero. Run the canonical core suite, both real HTTP projections, controlled allow/deny cases and isolated-data replay. | Development and domain reviewers accept behavior and evidence. No invented 400 mapping, missing-account 404, NULL default or floating-point money. No live banking traffic or shared-data replay. |
| Dev -> staging | Promote the same image digest, using staging configuration and separate resources/data. Validate private DNS/TLS, hub/site connectivity, image pull, secrets/rotation, probes, authenticated private telemetry and redaction. Exercise concurrency, backend loss, diagnostics/rollback failures, routing propagation and rollback against isolated data; prove non-wave routes never enter replacement. Run load at approved production-representative levels and the complete acceptance set. | Security, mainframe/data/recovery, network and operating owners accept the release record. Any behavioral mismatch, unauthorized access, unresolved recovery scope or absent telemetry blocks production. |
| Staging -> production, zero cohort | Review production-specific resource plan, identity, capacity and vault references; promote the exact tested digest. Validate compute/readiness and non-business connectivity without sending duplicate banking requests. Preserve the legacy route; verify the saved route-back configuration and operator access. Collect the legacy baseline from ordinary single executions. | Explicit production approval from business, security, mainframe/recovery and operating owners. Baseline, numerical acceptance sheet and rehearsed route-back must be complete before any cohort is enabled. |
| Production, zero -> cohorts -> full first wave | Follow `cutover.md`; approve each cohort expansion independently after its window, samples, parity and operational gates pass. Keep the same accepted configuration/image pair; changing either repeats affected gates. | Incident commander/seam operator records actual read-back, sample counts and reviewer decision. A failed gate invokes `rollback.md`, not automatic per-request fallback. Full first-wave routing does not authorize provider retirement. |

Use `infra/dev.bicepparam`, `infra/staging.bicepparam` and
`infra/production.bicepparam` with their environment-specific RG/CIDRs and
owner-supplied inputs. Existing foundation, DNS, private operator-network and
secret-name requirements are in `infra/README.md:44`, `infra/README.md:77` and
`infra/README.md:102`. The template defaults `deployApplication=false` and
requires an immutable image when activated
(`infra/main.bicep:69`, `infra/main.bicep:71`, `infra/README.md:128`).
An approved future application-activation parameter set must explicitly record
these inputs; the supplied environment files alone create no application.
Do not reuse synthetic CI parameters for deployment
(`docs/modernization/04-deploy/pipeline.md:80`).

## Health and contract evidence

Use the measurement/redaction contract in
`docs/modernization/04-deploy/observability.md:47` and its operational acceptance
at line 134. Require startup/readiness, image and secret resolution, normalized
operation/route/revision metrics, p95/p99, technical branch outcomes and
request-to-backend correlation. HTTP 200 alone is not health or parity.
Dependency probes must be accepted non-business checks, not repeated INQACC
calls or writes (`docs/modernization/04-deploy/observability.md:40`).
Cross-system attribution must use observed IDs, not assign OMEN from metadata
(`docs/modernization/04-deploy/observability.md:107`,
`docs/modernization/04-deploy/observability.md:115`).

For this module, do not use live shadowing even though it reads business rows:
diagnostics and caller rollback effects are contractual. Each live request has
one destination; parity comes from controlled fixtures and isolated replay
(`docs/modernization/03b-rewrite/INQACC/interface.md:166`,
`docs/modernization/03b-rewrite/INQACC/interface.md:191`,
`docs/modernization/03b-rewrite/INQACC/interface.md:345`).
Existing mainframe workflows, sources, stores and callers remain unchanged;
this document introduces no CI/deployment automation or live action.

**Verification boundary:** INQACC's rollback-response IF remains **unverified
by tooling**, with source-derived acceptance cases rather than demonstrated
live transaction equivalence
(`docs/modernization/03b-rewrite/INQACC/interface.md:24`,
`docs/modernization/03b-rewrite/INQACC/interface.md:512`).
BNK1CCS, BNK1DCS, CRECUST, IBTRAN and BNKSTMTC also have control flow
**unverified by tooling**; retained/deferred flows are not certified by this
runbook (`docs/modernization/00-source-of-truth/index.md:16`).
