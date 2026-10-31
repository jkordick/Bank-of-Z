# INQACC first-wave cutover

**Status: document-only, conditional procedure; all replacement routing stays
disabled.** ADR-007's strangler strategy and ADR-003's existing HTTP boundary
are controlling
(`docs/modernization/03a-architecture/target-architecture.md:203`,
`docs/modernization/03a-architecture/target-architecture.md:83`).
The routing product, deployed endpoints, supported replacement read path,
identity/recovery integration and numerical service limits remain acceptance
inputs, not facts supplied by this runbook
(`docs/modernization/03a-architecture/target-architecture.md:225`,
`docs/modernization/03a-architecture/target-architecture.md:227`,
`docs/modernization/03b-rewrite/INQACC/interface.md:345`).
No live traffic, deployment or flag change is performed.

## Switch location and exact scope

Place the controlled method/path/cohort switch **in front of z/OS Connect**,
at the existing `/api` seam, not in a CICS program router. Its accepted
configuration must implement the following route table
(`docs/modernization/03b-rewrite/INQACC/interface.md:334`):

| Method and path | Approved replacement cohort | Default / explicit route-back |
|---|---|---|
| GET `/api/accounts/{accountId}` | Future accepted private Java/Spring service, `getAccount` projection. | Existing z/OS Connect INQACC mapping (`src/api/src/main/operations/%2Faccounts%2F%7BaccountId%7D/get/operation.yaml:3`). |
| GET `/api/accounts/{accountId}/balances` | Same accepted service/cohort, distinct `getAccountBalances` projection. | Existing z/OS Connect INQACC mapping (`src/api/src/main/operations/%2Faccounts%2F%7BaccountId%7D%2Fbalances/get/operation.yaml:3`). |
| Every other method/path, including writes and `/api/ims/**` | None. | Its unchanged existing z/OS Connect mapping; CICS and IMS destinations remain distinct (`docs/modernization/03a-architecture/target-architecture.md:83`). |

Use exact route-template matching: GET-only, one account path segment and only
the named balances suffix. No broad `/accounts/*` replacement rule. Preserve
the accepted path assignment and undefined input disposition; routing must
not introduce identifier regex validation or prefix stripping
(`docs/modernization/03b-rewrite/INQACC/interface.md:217`,
`docs/modernization/03b-rewrite/INQACC/interface.md:308`).
Unmatched or ineligible requests remain legacy. Do not change shared CICS LINK
callers or transaction bindings, or retire the provider
(`docs/modernization/03b-rewrite/INQACC/interface.md:30`,
`docs/modernization/03b-rewrite/INQACC/interface.md:334`).

Select a stable cohort from an authenticated, security-owner-approved caller
identity, never a client-supplied route header or an account identifier. The
future router must use one versioned, nested cohort assignment for both GETs;
record the denominator as eligible caller cohorts, not a promised percentage
of total request volume. Verify actual request share separately. Route once
before dispatch and pin that request's destination through completion.
Disable router/service retry, hedging, mirroring and cross-destination
fallback for these executions. Route-control failure must never expand the
cohort; retain a verified legacy-only configuration or explicitly fail under
the accepted error contract if safe routing cannot be established.

Internal Container Apps revision weights are **not** the business switch:
they affect only traffic already addressed to the private app. Compute
activation and `BANKZ_REPLACEMENT_ROUTES_ENABLED=false` do not implement or
approve the external cohort router
(`infra/README.md:137`, `infra/README.md:151`).
The seam operator must complete the route execution sheet in `promotion.md`
with product-specific change, read-back and restore procedures before use.

## Baseline and isolated parity, not live shadowing

The switch works only if the full interface holds:
`docs/modernization/03b-rewrite/INQACC/interface.md:1`. Compare both exact
projections, literal status selectors/placeholders, exact decimal values and
observable ordered error/recovery effects, not just HTTP success.
The interface accepts missing-400 and invalid-ID conversion gaps, not invented
runtime outcomes
(`docs/modernization/03b-rewrite/INQACC/interface.md:208`,
`docs/modernization/03b-rewrite/INQACC/interface.md:277`,
`docs/modernization/03b-rewrite/INQACC/interface.md:308`).

**No live shadow stage for INQACC.** Its business SQL only reads ACCOUNT, but
diagnostic LINKs and caller-unit-of-work rollback are side effects. Duplicating
one request against shared live data could duplicate those effects
(`docs/modernization/00-source-of-truth/programs/INQACC.md:118`,
`docs/modernization/03b-rewrite/INQACC/interface.md:166`,
`docs/modernization/03b-rewrite/INQACC/interface.md:191`,
`docs/modernization/03b-rewrite/INQACC/interface.md:345`).
Live shadowing is permissible only for separately approved read-only/idempotent
operations whose entire effect contract supports duplication; none is selected
here. Writes always execute through exactly one implementation.

1. **Capture once.** With data/security-owner approval, observe ordinary
   single-destination executions and collect a bounded replay corpus and
   latency/outcome baseline for each GET. Record operation, input initialization,
   configuration, route/release, actual result and correlation, plus an
   owner-supported consistent data boundary. Capture no extra business call
   to obtain the baseline. Store sensitive evidence encrypted in an approved
   restricted location with access/retention controls, never in logs, Git,
   public artifacts or a third-party service.
2. **Establish reproducible inputs.** Use synthetic fixtures where possible.
   For approved captured cases, obtain an isolated consistent data copy with
   the schema, sort-code configuration and relevant row state. Replay retained
   update sequences in isolation for concurrency tests. A timestamp alone is
   not a snapshot; if data versions cannot be aligned, label the comparison
   inconclusive and do not count it as parity evidence.
3. **Replay in isolation.** Run legacy and replacement separately against
   independently reset copies of the same inputs/data and controlled context.
   Isolate diagnostic files, transaction state and dependencies as well as
   business tables; block network access to live systems. Replaying captured
   writes, if needed to construct concurrency scenarios, is allowed only
   inside this isolated harness, never against shared production data.
4. **Compare the contract.** Require all T01-T48 and both HTTP projections,
   then compare complete logical output, field presence/types/widths, fixed-point
   numeric values without tolerance, NULL versus zero/spaces, status selectors,
   exact error bodies, and diagnostic/rollback ordering, attempts and outcomes.
   Ignore JSON property order only; do not trim strings, remove fields or
   normalize errors to obtain matches. Context/time differences must follow a
   reviewed deterministic mapping, not a blanket diagnostic waiver. Cover
   sentinel `99999999`, ordinary missing results, suppressed blank/low-value
   type, signed limits and failure paths
   (`docs/modernization/03b-rewrite/INQACC/acceptance-tests.md:139`,
   `docs/modernization/03b-rewrite/INQACC/interface.md:131`,
   `docs/modernization/03b-rewrite/INQACC/interface.md:164`).
5. **Make discrepancies explicit.** Store case IDs, data boundary, release
   versions, expected/observed outcome and reviewer disposition privately.
   Any unexplained mismatch or inconclusive required case blocks expansion.
   Accepted undefined behavior remains explicitly outside executable parity;
   it is not silently scored PASS. Real adapters and HTTP/concurrency/security
   evidence are additional gates to fake-port core tests
   (`docs/modernization/03b-rewrite/INQACC/interface.md:326`,
   `docs/modernization/03b-rewrite/INQACC/interface.md:423`).

## Proposed shift schedule and acceptance at each step

This is a **candidate schedule requiring owner approval**, not selected live
percentages. Use isolated replay -> 0% -> 1% -> 10% -> 50% -> 100% of approved
eligible caller cohorts for both GETs together. If cohort cardinality cannot
support these fractions, approve exact nested cohort counts before activation;
do not round silently. No step advances merely because time passed.

| Step | Admission and acceptance | Failed gate |
|---|---|---|
| Isolated replay; no live shadow | Successful real release gates, all required reproducible parity cases with zero unexplained mismatches, accepted read/identity/recovery contracts, staging failure and route-back rehearsal. | Remain at legacy-only. |
| 0% production baseline | Approved release/operational/route sheets; legacy baseline and full failback capacity; private service healthy and observable; saved verified legacy route. | Keep 0%; do not create a canary to diagnose missing prerequisites. |
| 1% canary | Explicit production approval. Both GETs meet approved per-operation windows, minimum samples, p95/p99, technical-error/availability and dependency/capacity limits. Controlled parity cases pass; no account-authorization/attribution failure, duplicate dispatch, unexplained state effect or missing telemetry. | Stop expansion and explicitly route subsequent requests to legacy. |
| 10% expanded canary | Reapprove after every 1% gate passes. Same gates at the higher observed load; include owner-approved concurrent-update scenarios and route-back convergence evidence. | Route-back to 0%, not automatic fallback on a failed request. |
| 50% mixed operation | Reapprove after 10% passes. Same gates; both live paths remain within limits and data consistency evidence covers retained legacy writers. Retain verified 100% legacy capacity. | Route-back to 0%; reconcile in-flight effects. |
| 100% first wave and soak | Reapprove after 50% passes. Same gates through approved full-load soak and minimum samples for each GET. Record stable image/configuration, exact route read-back and ongoing replay evidence. | Route-back to 0%, including during or after soak. |

Numerical error/latency limits, sample counts, windows and soak duration are
**unselected**; they must be completed in the operational acceptance sheet,
not supplied as arbitrary banking SLOs here
(`docs/modernization/03a-architecture/target-architecture.md:227`,
`docs/modernization/04-deploy/observability.md:142`).
Measure technical branch failures separately from mapped HTTP statuses.
Stop immediately for behavioral/authorization/attribution failures, duplicate
dispatch, unapproved state changes or visibility loss; numerical-limit breaches
use the sheet's approved triggers. Each expansion requires a recorded human
decision and route read-back across all router instances.

## Data consistency and completion

ADR-004/005/007 retain legacy data and write authority; this wave performs no
data relocation, new business write, cache/replica activation or IMS/CICS merge
(`docs/modernization/03a-architecture/target-architecture.md:131`,
`docs/modernization/03a-architecture/target-architecture.md:170`,
`docs/modernization/03a-architecture/target-architecture.md:228`).
The future replacement must demonstrate its separately accepted read path and
consistency/isolation behavior while legacy updates continue. Do not choose
direct DB2 access or replication here, or claim that forwarding unchanged
INQACC is the rewrite.

No target-origin ACCOUNT/CUSTOMER/CONTROL/PROCTRAN mutation is expected from
this inquiry wave. Reconciliation checks execution provenance and approved
read-consistency evidence, not equality of live balances at different times.
Concurrent legacy writes remain legitimate; do not restore pre-cutover DB2
rows to force a match. Diagnostics and in-flight caller work are reconciled
as specified in `rollback.md`; a route change cannot undo them
(`docs/modernization/03b-rewrite/INQACC/interface.md:166`,
`docs/modernization/03b-rewrite/INQACC/interface.md:191`,
`docs/modernization/03a-architecture/target-architecture.md:138`).

Completion means the two accepted REST operations passed full-cohort gates,
not that the whole application migrated. Keep the existing CICS/IMS paths,
shared provider and non-HTTP dependencies available. Retirement requires a
separate dependency and recovery review
(`docs/modernization/03a-architecture/target-architecture.md:229`).

**Verification boundary:** INQACC's rollback-response IF is **unverified by
tooling** and has direct-source acceptance evidence, not proven live recovery
equivalence (`docs/modernization/03b-rewrite/INQACC/interface.md:24`).
BNK1CCS, BNK1DCS, CRECUST, IBTRAN and BNKSTMTC have control flow **unverified
by tooling**; unchanged/deferred flows are not certified or safe to retire on
this basis (`docs/modernization/00-source-of-truth/index.md:16`).
