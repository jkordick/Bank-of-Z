# INQACC route-back and state reconciliation

**Status: document-only, conditional recovery runbook.** No route change,
deployment, flag toggle, mainframe command or data recovery is performed.
ADR-007 requires explicit legacy route-back for subsequent requests; ADR-004
separates it from data rollback
(`docs/modernization/03a-architecture/target-architecture.md:227`,
`docs/modernization/03a-architecture/target-architecture.md:138`).
Use the accepted module contract, not an invented generic failure response:
`docs/modernization/03b-rewrite/INQACC/interface.md:131`,
`docs/modernization/03b-rewrite/INQACC/interface.md:277`,
`docs/modernization/03b-rewrite/INQACC/interface.md:345`.

## Trigger and response authority

Before activation, the release record in `promotion.md` must name the incident
commander, seam operator, business/security acceptors, mainframe/data/recovery
owner and Azure operator. It must hold the verified legacy route configuration,
product-specific restore/read-back commands, private destinations, approved
error/latency/capacity triggers, maximum route convergence and drain times, and
emergency approval procedure. These inputs are presently unknown; this is not
an executable product-specific restore command or cutover approval
(`docs/modernization/03a-architecture/target-architecture.md:227`).

Stop expansion for any required gate failure. Invoke the separately approved
incident procedure immediately for parity, authorization/attribution, duplicate
dispatch, unexpected state effects or telemetry loss; use the operational
sheet's numerical triggers for latency/error/availability/capacity breaches.
Do not wait for an observation window to finish after a safety failure.
Azure probe settings alone do not define the banking recovery objective
(`docs/modernization/04-deploy/observability.md:31`,
`docs/modernization/04-deploy/observability.md:142`).

| Current step | Route-back scope | State obligation |
|---|---|---|
| Isolated replay / dev / staging | Production remains legacy-only. Stop the candidate/replay; reject promotion. Rehearsal traffic returns to its isolated legacy destination only. | Preserve evidence; reset only isolated test data under its approved procedure. No production data restoration. |
| Production 0% | Keep the verified legacy-only configuration. Do not activate a replacement cohort to recover failed readiness. | Investigate candidate health/configuration; live legacy work continues unchanged. |
| 1%, 10%, 50% | Set both exact GET cohort routes to zero replacement through the seam's approved versioned restore procedure. Leave all non-wave routes unchanged. | Pin already dispatched requests to their original destination; drain and reconcile them without replay. |
| 100%, during/after soak | Restore the same two legacy GET mappings and zero replacement cohort; retain enough legacy capacity for the full returning load. | Same drain/reconciliation rules. Full-cohort routing did not transfer write authority or retire the provider. |
| Seam propagation/control failure | Restore verified legacy-only routing across affected instances; block replacement admission where route state cannot be verified. | Count executions per instance/version; do not duplicate requests to work around mixed configuration. If no verified safe destination exists, use the accepted explicit failure procedure. |

Both fallback operations use the original **CICS INQACC** provider mappings;
there is no equivalent IMS fallback for this module
(`docs/modernization/03b-rewrite/INQACC/interface.md:37`).
IMS operations and all other methods/paths keep their own retained mappings;
do not redirect CICS inquiries into `/ims`
(`docs/modernization/03a-architecture/target-architecture.md:83`).

## Ordered future route-back procedure

1. **Declare and freeze.** Incident commander records trigger, release digest,
   route version, affected window and approval. Freeze cohort expansion and
   other release/configuration changes. Secure current logs/traces, route
   read-back, active request inventory and diagnostics without exposing banking
   data. Do not overwrite the evidence by redeploying first.
2. **Verify the retained destination.** Seam/mainframe operators confirm
   accepted legacy connectivity, readiness and failback capacity using approved
   non-business checks and existing execution evidence. If legacy is also
   unavailable, do not silently retry there or choose an IMS substitute.
   Preserve the accepted explicit failure behavior, stop unsafe admission and
   escalate under the service incident procedure.
3. **Restore admission, not executions.** Authorized seam operator applies the
   saved legacy-only configuration for exactly the two GET routes, removing all
   replacement cohorts. Read back the active version across every routing
   instance and confirm zero new replacement dispatches within the approved
   convergence limit. Verify other routes are unchanged. Route changes require
   their separate human approval; this document is not that approval.
4. **Drain pinned work.** Allow already dispatched replacement and legacy
   requests to finish on their original destinations within the approved drain
   limit. Track queued-but-undispatched requests separately and assign them
   once under the verified restored configuration. A timeout, connection close
   or process cancellation does not prove backend termination/rollback. Mark
   ambiguous executions unresolved, investigate, and never resubmit them to
   either destination automatically.
5. **Reconcile effects.** Mainframe/data/recovery owner accounts for requests,
   caller-unit-of-work completion and diagnostics using the matrix below.
   Investigate any unexpected business mutation; do not issue compensating
   transfers, delete history or restore snapshots as a route-back shortcut.
6. **Verify recovery and preserve the candidate.** Confirm subsequent single
   requests use the legacy mapping, both GETs meet the approved recovery window
   and service limits, telemetry remains available and every in-flight outcome
   has a recorded disposition or explicit unresolved incident. Retain the
   failed image/configuration and evidence privately; disable or scale down
   candidate compute only under separate approval after drain/recovery review.
   Do not mistake application deletion for route-back.
7. **Require new admission evidence.** Keep replacement routing at zero until
   root cause is fixed and affected build, contract, integration, security,
   observability and staging route-back gates pass again. A new image repeats
   candidate promotion; no automatic return to the old cohort percentage.

This procedure implements the contract's single-dispatch/no-silent-fallback
rule. It does not establish end-to-end exactly-once behavior for independent
caller retries or shrink the caller's rollback scope
(`docs/modernization/03b-rewrite/INQACC/interface.md:191`,
`docs/modernization/03b-rewrite/INQACC/interface.md:345`).

## State reconciliation matrix

INQACC's scanned SQL reads ACCOUNT; it creates no PROCTRAN entry or other
business-row mutation. Its diagnostic dependency and rollback effects remain
part of recovery
(`docs/modernization/00-source-of-truth/programs/INQACC.md:118`,
`docs/modernization/03b-rewrite/INQACC/interface.md:166`).

| State | Required owner-led reconciliation | Forbidden shortcut |
|---|---|---|
| Request admission and in-flight work | Join opaque request ID, route version, actual destination/revision and observed backend execution. Account for dispatched, completed, timed-out and unresolved work; demonstrate no router-caused second dispatch. Use observed CICS task/APPLID/transaction IDs when available, not metadata-derived IDs (`docs/modernization/04-deploy/observability.md:107`, `docs/modernization/04-deploy/observability.md:115`). | Replaying an uncertain request or treating HTTP success/timeout as proof of unit-of-work completion. |
| DB2 ACCOUNT/CUSTOMER/CONTROL rows | Expect no replacement-origin business mutation. Use owner-approved execution/audit evidence and consistent-boundary checks to distinguish legitimate concurrent legacy writes from unexpected replacement effects. Escalate unexpected mutations as a data incident, with affected scope and recovery authority recorded. | Restoring pre-cutover rows or comparing balances at different times as if no legacy writers were active. Legacy write authority remains unchanged (`docs/modernization/03a-architecture/target-architecture.md:103`, `docs/modernization/03a-architecture/target-architecture.md:131`). |
| PROCTRAN business history | Expect zero entries attributable to this inquiry's business behavior, not zero entries globally. Retain history generated by concurrent legacy writes. Investigate any unexpected inquiry-attributed history together with originating execution evidence. | Deleting history to make counts match, using a diagnostic record as banking history, or assuming a locator proves uniqueness/outcome (`docs/modernization/03b-rewrite/INQACC/interface.md:166`, `docs/modernization/03a-architecture/target-architecture.md:250`). |
| ABNDFILE / replacement diagnostic effects | Compare attempted versus persisted diagnostics, stage/order, exact code/text/context and observed write failures. Preserve records even after route-back; a diagnostic LINK does not guarantee persistence. Inventory unexpected duplicate attempts as an incident (`docs/modernization/03b-rewrite/INQACC/interface.md:186`). | Deleting duplicate records or promising diagnostic writes roll back atomically with banking data; checked-in recovery metadata is not proof of deployed recovery (`docs/modernization/03b-rewrite/INQACC/interface.md:201`). |
| Participating caller unit of work | Mainframe/recovery owner establishes actual completion/rollback outcome for affected tasks. AFCR/AFCS/AFCT require caller-scope rollback; NORMAL yields retained-fields N, non-NORMAL produces HROL without normal return. Preserve caller work scope and investigate unresolved rollback independently of routing (`docs/modernization/03b-rewrite/INQACC/interface.md:137`, `docs/modernization/03b-rewrite/INQACC/interface.md:191`). | Creating an inquiry-only rollback, reissuing the handler to force completion, or claiming gateway drain/cancellation performs CICS recovery. |
| IMS stores and IMS/DB2 history | No INQACC authority or data move is selected. Verify their routing remained untouched; an unrelated IMS incident follows its owner-approved recovery contract (`docs/modernization/03a-architecture/target-architecture.md:103`). | Using IMS as an inquiry failover database, merging identities, or claiming this runbook resolves cross-store durability. |

If evidence is missing, record **unknown/unresolved** and escalate; do not
manufacture a successful reconciliation. Banking values remain exact
fixed-point, and restricted case evidence must distinguish NULL, zero, spaces
and low-values without logging sensitive values
(`docs/modernization/03b-rewrite/INQACC/interface.md:94`,
`docs/modernization/04-deploy/observability.md:71`).

## Boundary for any later write cutover

This runbook rolls back routing for an inquiry-only wave with unchanged write
authority. It is not a data-rollback procedure after a future writer migration.
ADR-004/005 require a separate accepted authority-transfer/recovery decision:
fence new writers, drain and reconcile committed and uncertain units of work,
reconcile DB2 rows and PROCTRAN multiplicity at an accepted consistent boundary,
then execute approved reverse-data or forward recovery before reopening legacy
writes. A routing switch alone cannot restore committed balances/history.
Retain whole legacy write operations meanwhile; no shared-data shadow writes,
dual execution, split debit/credit/history commits or ambiguous replay
(`docs/modernization/03a-architecture/target-architecture.md:136`,
`docs/modernization/03a-architecture/target-architecture.md:138`,
`docs/modernization/03a-architecture/target-architecture.md:228`).

**Verification boundary:** INQACC's rollback-response IF remains **unverified
by tooling**; its accepted source-derived tests do not establish deployed
transaction-manager equivalence
(`docs/modernization/03b-rewrite/INQACC/interface.md:24`,
`docs/modernization/03b-rewrite/INQACC/interface.md:512`).
BNK1CCS, BNK1DCS, CRECUST, IBTRAN and BNKSTMTC have control flow **unverified
by tooling**; this runbook preserves rather than certifies their retained paths
(`docs/modernization/00-source-of-truth/index.md:16`).
