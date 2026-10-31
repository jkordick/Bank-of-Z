# INQACC observability

**Status: emitted infrastructure and required application telemetry contract,
not observed runtime signals.** The human-selected platform is Azure Container
Apps with ACR, Key Vault and Log Analytics/Application Insights
(`docs/modernization/03a-architecture/target-architecture.md:5`).
The application remains disabled by default (`infra/main.bicep:69`), and the
rewrite is a Java core rather than a running REST service
(`docs/modernization/03b-rewrite/INQACC/interface.md:360`).
No resources, agent, alerts or mainframe collectors have been deployed.

## Wired platform signals

| Source | Emitted wiring and operational interpretation |
|---|---|
| Container Apps environment | Azure Monitor destination, console/system log diagnostic categories and AllMetrics to the workspace (`infra/modules/environment.bicep:16`, `infra/modules/environment.bicep:38`). Inspect revision activation, image/secret resolution, replica restarts, scheduling and scaling failures. Console records become structured application logs only if the image emits the contract below. |
| Container App | Startup, liveness and readiness HTTP probes on local port 8080; replica bounds and HTTP-concurrency scaling (`infra/modules/application.bicep:144`, `infra/modules/application.bicep:181`). Azure platform metrics cover resource/replica pressure and ingress; verify available metric names in the deployed region/API before binding alerts. Scaling concurrency 20 is a starting setting, not an accepted service SLO. |
| Application Insights | Workspace-backed resource, local authentication disabled and private ingestion/query only (`infra/modules/monitoring.bicep:24`). Instrumented Java image must emit requests, dependencies, traces, exceptions and custom events using the vault connection-string reference and UAMI configuration (`infra/modules/application.bicep:112`). No agent is installed by Bicep. |
| Key Vault | All diagnostic logs/metrics to the workspace; review secret-read denials, unexpected principals and rotation failures (`infra/modules/services.bicep:105`). A missing credential or failed secret fetch must fail activation/readiness, never silently enable anonymous access. |
| ACR | All diagnostic logs/metrics to the workspace; review pull/publish denials and unexpected principals (`infra/modules/services.bicep:125`). Workload access is AcrPull only; image digest and scan evidence belong in the release record. |
| Azure Monitor Private Link | Shared AMPLS receives additive workspace/Insights links, and private endpoint/DNS wiring supports private application ingestion and operator queries (`infra/modules/monitor-connection.bicep:10`, `infra/main.bicep:209`). Verify DNS/ingestion from the spoke and operator network before accepting visibility. |

Log Analytics retention is 30 days (`infra/modules/monitoring.bicep:14`).
This is an operational default, not an approved financial/audit retention
policy. Retention, access and any immutable archive need owner acceptance
before live traffic. Telemetry readers must use Entra/RBAC and private network
access. Audit file persistence is not replaced by cloud telemetry.

## Health semantics

Startup allows up to 30 failed checks at 10-second intervals; liveness/readiness
allow three at 10 seconds, each with a five-second timeout
(`infra/modules/application.bicep:144`). Treat these as initial probe settings,
not a recovery-time guarantee.

The future Spring service must implement the declared endpoints. Liveness
checks process health only: do not restart every replica because the mainframe
is unavailable. Readiness must represent local initialization, accepted
authorization configuration, secrets and adapter readiness; fail closed when
these are absent. Dependency health must use an owner-approved non-business
connectivity check, never invoke INQACC, a transfer or a rollback-capable
operation just to probe. Keep backend TLS/DNS/connection failures separately
visible from process liveness. Probe endpoints reveal no secrets or detailed
dependency data; all other Actuator endpoints are unexposed by the template
(`infra/modules/application.bicep:100`).

## Required application and seam telemetry

Emit structured JSON to stdout and request/dependency spans through the
supported Azure Monitor Java agent. Use W3C `traceparent`/`tracestate` across
the future gateway and Java HTTP client after validating incoming context.
Generate a bounded opaque request ID if absent. Agent sampling is initially
100% (`infra/modules/application.bicep:120`); approval of cost/retention and
redaction is required. Never let automatic HTTP instrumentation export the
account-bearing raw URL: normalize paths to the templates below and redact
URLs in request/dependency telemetry **before ingestion**. Verify with
synthetic account markers and token markers in logs/traces.

Required fields (an application contract, not already implemented):

| Field | Meaning and cardinality |
|---|---|
| `request_id`, `trace_id`, `span_id` | Opaque per-execution correlation; logs/traces only, not metric labels. |
| `operation` | `getAccount` or `getAccountBalances`; normalized paths `/api/accounts/{accountId}` and `/api/accounts/{accountId}/balances`. |
| `route`, `module`, `revision`, `environment` | `legacy`/`replacement`, `INQACC`, deployed revision/image digest and environment. Do not claim a route from a request header. |
| `rule_id`, `lookup_mode`, `outcome` | Bounded accepted rule, ordinary/highest, and branch outcome. Distinguish unchanged-fields success, populated success, rollback-return and abend; HTTP 2xx alone is not parity. |
| `sql_operation`, `sqlcode`, `abend_code`, `resp`, `resp2` | Exact signed SQLCODE and observed technical results when available. Restrict labels; unbounded/reissued abend codes belong in logs. No guessed values for HTTP-only callers. |
| `diagnostic_stage`, `rollback_outcome` | Ordered attempt/success/failure evidence for accepted diagnostic/rollback effects; distinguish attempted recording from persistence. |
| `legacy_program`, `legacy_transid`, `legacy_applid`, `legacy_task_id`, `zos_request_id` | Backend-observed correlation when available, not invented from provider metadata. Missing values are `unknown` with a reason in logs. |

Do not log account/customer identifiers, balances, COMMAREAs, native pointers,
authorization headers, credential secret values, bodies, SQL bind values or
unredacted URLs. Subject attribution must use a security-owner-approved opaque
reference, not raw tokens or new public identity mappings. Trace correlation
does not prove authorization, atomic commit or idempotency.

Measure per-operation request count, duration distribution (including p95/p99),
outcome counts, dependency failures/latency, diagnostic failures, rollback
outcomes, and parity mismatches. Metric dimensions are bounded
operation/route/environment/revision/outcome; never account IDs, task IDs or
trace IDs. Preserve branch counters separately from technical request-success
counters. Reconciliation mismatches must be explicit, not success-shaped
fallbacks.

## Mapping to accepted business rules

These classifications supplement, never normalize, accepted behavior:

| Rule | Required evidence and alert-review meaning |
|---|---|
| INQACC-R01 | Ordinary mode uses configured sort code/account selector only; record mode and result classification without identifiers. No additional type/customer predicate inferred (`docs/modernization/01-rediscovery/business-rules.md:94`). |
| INQACC-R02 | Highest mode is distinct; nonzero SELECT including +100 records HNCS and has no normal result. Do not count it as an ordinary missing account (`docs/modernization/01-rediscovery/business-rules.md:96`). |
| INQACC-R03/R04 | Count ordinary missing and suppressed blank/low-value type as unchanged-fields Y outcomes, not zero balances/not-found responses (`docs/modernization/01-rediscovery/business-rules.md:98`, `docs/modernization/01-rediscovery/business-rules.md:100`). |
| INQACC-R05 | Populated result and exact fixed-point/date projection parity; emit only match/mismatch categories, never financial values. No floating-point comparisons (`docs/modernization/01-rediscovery/business-rules.md:102`). |
| INQACC-R06 | OPEN/FETCH/CLOSE failure stage, exact SQLCODE, diagnostic attempt and HRAC abend; successful FETCH followed by failed CLOSE is still failure. No retry (`docs/modernization/01-rediscovery/business-rules.md:104`). |
| INQACC-R07 | Positive 923 storm diagnostic distinguished from negative -923; neither invents rollback or normal N outcome (`docs/modernization/01-rediscovery/business-rules.md:106`). |
| INQACC-R08 | AFCR/AFCS/AFCT rollback NORMAL returns N with retained fields; failed rollback produces HROL. AD2Z/other captured abends retain reissue semantics. Record diagnostic ordering and rollback outcome (`docs/modernization/01-rediscovery/business-rules.md:108`). |

**Verification boundary:** INQACC parsed successfully but the rollback-response
IF remains **unverified by tooling**; its observability mapping uses the accepted
original-source evidence (`docs/modernization/03b-rewrite/INQACC/interface.md:24`,
`src/base/cics/cobol/INQACC.cbl:687`). No parser rerun or runtime trace verifies
that branch here.

## CICS / IMS correlation

The two REST operations share INQACC. The provider's `OMEN` is `EIB_ONLY`
metadata, not a newly assigned terminal transaction or a BNKMENU dispatch
instruction (`docs/modernization/03b-rewrite/INQACC/interface.md:32`,
`docs/modernization/03b-rewrite/INQACC/interface.md:39`).
Unchanged screen callers are BNK1DAC/ODAC and BNK1UAC/OUAC, with existing LINK
paths; their IDs must not be attached to REST executions
(`docs/modernization/03b-rewrite/INQACC/interface.md:30`).

On the legacy path, join gateway request/time/destination evidence to an actual
z/OS Connect request ID and then the observed CICS APPLID/task/transaction ID
through an owner-approved instrumentation bridge. Whether the deployed server
propagates W3C context or exposes such a bridge is **unknown**. Do not assume an
HTTP trace ID appears in CICS/IMS records. The mainframe owner must demonstrate
the mapping with controlled requests and document its access/retention and
clock alignment before accepting cross-system traces.

IMS operations remain outside the INQACC wave and retain separate connection/
transaction identity; do not label them OMEN/INQACC or invent an IMS transaction
ID. The method/path seam retains `/ims` and its existing mappings
(`docs/modernization/03a-architecture/target-architecture.md:83`).
Capture an actual IMS transaction/message correlation only when supplied and
accepted by the mainframe owner. These artifacts install no CICS/IMS collector
or change any legacy program. No claims about parser-gap programs' execution
are required by this inquiry-only monitoring contract.

## Operational acceptance

Before traffic activation, demonstrate console/system logs, authenticated
private trace ingestion, request-to-dependency correlation, exact technical
error classification, secret/URL redaction, image/secret failure visibility,
probe behavior and backend-loss behavior. Compare accepted fixtures and
owner-approved live evidence; do not duplicate inquiry executions for
shadowing because diagnostics and rollback effects are part of the contract
(`docs/modernization/03b-rewrite/INQACC/interface.md:344`).

The operating owner must approve numeric latency/error/capacity thresholds,
evaluation windows, minimum traffic volumes, missing-telemetry detection,
retention, escalation destinations and route-back criteria before enabling
alerts or moving traffic. These values remain unselected, not silently
defaulted (`docs/modernization/03b-rewrite/INQACC/interface.md:344`).
Therefore no arbitrary paging rule or action-group recipient is provisioned.
Any detected parity or attribution failure blocks promotion; route-back changes
subsequent requests only, with no cross-destination replay.

Observe backend errors explicitly during an outage; never return fabricated
success or silently rerun a request elsewhere. Telemetry loss blocks acceptance
of a traffic shift. Compilation, security scan and live verification status are
recorded in `infra/README.md`; emitted wiring is not evidence that any of these
checks passed.
