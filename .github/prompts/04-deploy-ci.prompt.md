---
agent: 'deployment-engineer'
description: Phase 4 — Emit the CI pipeline for the modernized modules. Files only, no live runs.
---

Emit a CI pipeline that builds, tests and scans the modernized code. Read `docs/modernization/03a-architecture/target-architecture.md` for the runtime and platform.

Produce:

- `.github/workflows/modernized-ci.yml` with stages: checkout, setup, build, unit tests, **phase-3b acceptance tests** (gating), security scan, IaC lint (if `infra/` exists — if it does not yet, say so and remind me to re-run this prompt after `/04-deploy-iac`), package. Trigger only on paths of the modernized code so the existing mainframe workflows are unaffected.
- `docs/modernization/04-deploy/pipeline.md` explaining each stage, its inputs, and its failure semantics.

Rules:

- Reference secrets by name only. Never hard-code values.
- Acceptance tests from every rewritten module must run and gate the workflow.
- The workflow must run on GitHub-hosted runners from a fresh clone with only public dependencies.
- Do **not** modify `.github/workflows/PR.yml` / `Setup.yml` (they target z/OS on self-hosted runners).
- Do **not** execute the workflow, do **not** publish anything. Emit files only.

Print the files added and stop.
