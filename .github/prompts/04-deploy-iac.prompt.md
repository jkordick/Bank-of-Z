---
agent: 'deployment-engineer'
description: Phase 4 — Emit infrastructure-as-code for the target platform chosen in phase 3a.
---

Emit infrastructure-as-code for the platform chosen in `docs/modernization/03a-architecture/target-architecture.md`. Use the IaC flavor phase 3a specified (Bicep / Terraform / …). If phase 3a did not pick one, **stop and ask**.

Produce, under `infra/`:

- Network and identity foundations — including connectivity back to the mainframe (z/OS Connect endpoint, DB2 / MQ access) if phase 3a requires coexistence.
- Compute per phase 3a.
- Data stores per phase 3a's data-model decision, plus any replication / CDC component it names.
- Observability wiring (logs, metrics, traces, health checks).
- Parameter files for `dev`, `staging`, `production` — differences only in scale and secret refs.

Also write `docs/modernization/04-deploy/observability.md`: the signals the platform emits and how they map back to business rules and to legacy CICS / IMS transaction IDs.

Rules:

- Secrets are referenced from a vault by name. No inline values.
- Do **not** run `apply`, `az deployment`, `terraform apply`, `pulumi up` or any equivalent. Emit files only.

Print the files added and stop.
