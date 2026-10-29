---
agent: 'deployment-engineer'
description: Phase 4 — Document the strangler-fig cutover, promotion and rollback path for one module. No live traffic changes.
---

Document how a rewritten module goes from "green in CI" to "serving production traffic" — and how it comes back safely if it doesn't — while the mainframe stays live.

Produce:

- `docs/modernization/04-deploy/promotion.md` — dev → staging → production, with the gates at each hop (tests, approvals, health signals).
- `docs/modernization/04-deploy/cutover.md` — the traffic-shifting mechanism for one rewritten module: where the switch lives (z/OS Connect / API gateway route, CICS program router, feature flag), the shift schedule (e.g. shadow → 1% → 10% → 50% → 100%), acceptance criteria per step, and how parity is compared. Live shadowing (sending the same request to legacy and new) is only allowed for **read-only / idempotent** operations. For anything that writes (DB2 updates, `PROCTRAN` entries, transfers, creates, deletes), each live request goes to **exactly one** implementation; compare parity by replaying captured traffic against an isolated copy of the data instead.
- `docs/modernization/04-deploy/rollback.md` — how to send traffic back to the CICS / IMS path at each step, and what state needs reconciliation (DB2 rows, `PROCTRAN` audit entries, in-flight units of work).

Rules:

- Cite the interface contract `docs/modernization/03b-rewrite/<module>/interface.md` — the switch only works if the contract holds.
- Cite the phase-3a migration strategy.
- Never propose a design where one business transaction is executed by both implementations against shared data.
- Do **not** perform any live traffic change, deployment, or feature-flag toggle. Documents only.

Print the files added and stop.
