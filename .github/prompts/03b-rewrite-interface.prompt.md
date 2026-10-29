---
agent: 'module-rewriter'
description: Phase 3b — Define a legacy-identical interface contract for ONE module (strangler-fig switch point).
---

Define the interface contract for the module being rewritten. The contract must let the new implementation replace the legacy one **behind the same interface**, so traffic can be shifted gradually.

Write `docs/modernization/03b-rewrite/<module>/interface.md` with:

- **Entry points** — the exact program / transaction / REST operation names callers use today (from `00-source-of-truth/call-graph.md` and `src/api/`).
- **Input shape** — COMMAREA / copybook layout field-by-field, PICTURE clauses preserved. Start from the *LINKAGE SECTION* table in `00-source-of-truth/programs/<PROGRAM>.md`.
- **Output shape** — same treatment.
- **Error / return-code semantics** — every success flag, fail code and abend code the legacy code can produce, and what it means.
- **Side effects** — DB2 rows, IMS segments, `PROCTRAN` audit records, files, messages — in the order they happen, and which happen inside the same `SYNCPOINT`.
- **Switch point** — where the new implementation is selected (z/OS Connect / API gateway route, CICS program router, feature flag). Reference the migration strategy from phase 3a.

If any of the above cannot be answered from the rediscovery artifacts, stop and add the gap to `docs/modernization/01-rediscovery/open-questions.md`. Do not paper over unknowns. Stop when the file is complete.
