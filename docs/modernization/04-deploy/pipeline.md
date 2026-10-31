# Modernized CI pipeline

**Artifact-only delivery:** `.github/workflows/modernized-ci.yml` defines
verification and packaging; it has not been executed or published by this task.
No deployment, registry push, Azure login, provisioning or traffic shift occurs.

The inherited target is Java/Spring Boot in a Linux container on Azure
Container Apps with ACR, Key Vault, Azure Monitor and Bicep
(`docs/modernization/03a-architecture/target-architecture.md:5`). The current
INQACC implementation is a framework-independent core, not a running Spring
service; Java 21 is the existing local compilation baseline, not a newly
approved production runtime
(`docs/modernization/03b-rewrite/INQACC/interface.md:360`,
`modern/inqacc/run-tests.sh:13`). Accordingly, CI emits a **core-library JAR**,
not a runnable JAR or container. Building a Spring HTTP wrapper, adapters,
instrumented image and release pipeline remains separate work.

## Triggers and isolation

Push and pull-request path filters cover `modern/**`, phase-3b module
contracts/tests, `infra/**` and this workflow itself. These are the modernized
implementation and its verification inputs; legacy-only changes do not trigger
this workflow (`.github/workflows/modernized-ci.yml:3`). Every matching run
checks every rewritten module, not only the changed one. No manual-dispatch
trigger or deployment job is provided.

The workflow uses one GitHub-hosted `ubuntu-24.04` job, read-only repository
permissions and checkout without persisted credentials. It requires no custom
runner, mainframe, private Maven repository, cloud resource or user-provided
secret. Existing `.github/workflows/PR.yml`, `Setup.yml` and `.setup/` are
unchanged. Their independent trigger behavior is not overridden.

Temurin 21 and Python 3.11 are installed through public setup actions.
Semgrep 1.90.0 and Checkov 3.2.257 use separate virtual environments and public
PyPI dependencies; Bicep 0.31.92 comes from the public Azure/bicep release.
Semgrep's public `p/java` and `p/secrets` rule packs are fetched at scan time,
with metrics disabled. Tool versions are pinned; transitive packages, action
major tags, runner image and registry rule packs are not immutable lockfiles.
Dependency/rule download failures fail the run rather than bypassing a gate.

## Stages, inputs and failure semantics

Stages run sequentially in one job. Shell steps use GitHub's explicit Bash
default (`--noprofile --norc -e -o pipefail`); a failing command, including a
test piped through `tee`, fails its stage and prevents subsequent normal stages.
There is no soft-fail scanner, acceptance waiver or success-shaped fallback.

| Stage | Inputs and concrete tools | Gate / output |
|---|---|---|
| Checkout | Matching commit; `actions/checkout@v4`. | Checkout failure stops the job. Untrusted PR code receives no cloud credentials. |
| Setup | `actions/setup-java@v4` (Temurin 21), `actions/setup-python@v5` (3.11). | Missing/download-failed runtimes stop the job. No application dependencies are required by the current core. |
| Inventory | Every directory under `docs/modernization/03b-rewrite/`; Python checks matching lowercase `modern/<module>/`, contract, acceptance matrix, runner and Java source roots. | Empty inventory, missing inputs or an implementation without a contract fails. The generated module list drives all build/test/package loops, so newly added modules cannot silently miss acceptance. |
| Build | Production and test Java sources in every module; `javac --release 21 -Xlint:all -Werror`. | Empty source sets, compilation errors and compiler warnings fail. Production/test classes have separate output directories; test classes never enter library JARs. |
| Unit tests | Compiled `*Test.java` executable fixture drivers, run with `java`. | No drivers or any nonzero driver exit fails. Currently this is the same INQACC fake-port harness used for acceptance, not an invented separate JUnit suite (`modern/inqacc/src/test/java/com/bankofz/inqacc/InqaccAcceptanceTest.java:27`). |
| Phase-3b acceptance | `bash modern/<module>/run-tests.sh` for **every** module, followed by Python reconciliation of acceptance-matrix IDs with logged `PASS <MODULE>-Tnn` IDs. | Runner failure, empty/duplicate matrix IDs, missing, duplicate or unexpected PASS IDs fails. INQACC currently requires all 48 T01-T48 cases exactly once (`modern/inqacc/src/test/java/com/bankofz/inqacc/InqaccAcceptanceTest.java:28`, `docs/modernization/03b-rewrite/INQACC/acceptance-tests.md:128`). This independently recompiles/runs the canonical phase-3b suite rather than relying on the unit-stage result. |
| Security scan | Semgrep Java and secret rules over `modern/`, `infra/` and this workflow; `--strict --error`. | Any reported blocking finding, scan/configuration error or download failure blocks packaging. JSON report is retained when available. This is static analysis/secret detection, not dependency-vulnerability or live penetration testing. |
| IaC lint/security | Every `infra/**/*.bicep` compiled with Bicep's built-in lint; every `.bicepparam` compiled; Checkov scans compiled ARM templates, including independently compiled modules. | Compiler errors and any failing Checkov check block packaging. Bicep linter warnings are visible but are not promoted to errors by its default configuration. No scanner exclusions or severity waivers are supplied. |
| Package | Only verified production classes, original module sources, phase-3b evidence, original IaC, scan/test reports and commit SHA; `jar`, `sha256sum`. | Any packaging error fails. Per-module non-executable library JARs and SHA256SUMS form an inspectable evidence bundle. Synthetic compiled parameter files are deliberately excluded. |
| Retain package | `actions/upload-artifact@v4`, commit-addressed artifact, 14-day retention. | Runs only after every preceding gate passes; missing files or upload errors fail. This retains a CI artifact, not a release or registry publication. |
| Failure diagnostics | Available test logs and scanner JSON; always-run artifact step after the report path is initialized, 7-day retention. | Retains available evidence even after failed gates; skips upload if setup failed before path initialization. Absent reports only warn and do not erase the original failure. A diagnostic-upload error still fails the job. |

There is intentional overlap between the unit and acceptance stages because
the current project has one dependency-free executable core test harness, not
two separately maintained suites. Its failure accumulator throws on any
failed case (`modern/inqacc/src/test/java/com/bankofz/inqacc/InqaccAcceptanceTest.java:76`);
per-case PASS records come from the harness
(`modern/inqacc/src/test/java/com/bankofz/inqacc/InqaccAcceptanceTest.java:590`).
Running it twice preserves an explicit unit stage and an independent,
matrix-reconciled phase-3b gate without changing the rewrite.

New modules must follow the current uppercase-contract/lowercase-Java-directory
convention, provide `run-tests.sh`, and emit one PASS ID for each matrix row.
If later modules introduce Maven/Gradle, JUnit or Spring dependencies, update
setup/build/unit/package commands to their public build before merging them;
the current bare-JDK build intentionally fails rather than skipping unsupported
modules. Keep the canonical acceptance runner and matrix reconciliation gating.

## Existing infrastructure and synthetic inputs

`infra/` already exists, so the IaC stage is included now. Its checked-in
parameter files require site variables, not cloud discovery
(`infra/dev.bicepparam:3`, `infra/dev.bicepparam:18`). CI supplies clearly named
`ci-example-*` identities, dummy subscription UUIDs and synthetic private CIDRs
only to compile all environment parameter files without private dependencies.
They are **not secrets, discovered infrastructure, deployment inputs or
approved allocations**. No Azure authentication or resource validation occurs.
Real environment parameters remain owner-supplied as described in
`infra/README.md:77`; credential/URL/telemetry values remain in named Key Vault
secrets, never in CI or its package (`infra/README.md:102`).

Checkov consumes the compiled ARM JSON rather than assuming that scanning raw
Bicep covers its resources. Both the top-level template and standalone module
templates are scanned; module parameters and runtime conditions still limit
what static checks can establish. A failed check remains a failed gate until
its cause is reviewed and fixed; this task does not modify IaC to silence
pre-existing findings. Template compilation with synthetic inputs proves no
site connectivity, tenant permission, capacity or runtime health.

## Acceptance and verification boundary

The emitted workflow was statically checked against the inspected source,
runner, acceptance matrix and IaC input declarations. No workflow, local test
command, scanner, Bicep compilation or publication was executed in this task;
successful CI execution remains unverified. An emitted gate is not evidence
that the existing IaC already passes compilation or Checkov.

INQACC's rollback-response IF remains **unverified by tooling**, covered through
direct-source acceptance fixtures
(`docs/modernization/03b-rewrite/INQACC/interface.md:24`). Module tests cover
logical core behavior with fake ports, not deployed HTTP projections, live
CICS/DB2 integration, account authorization, concurrent legacy updates or
cutover readiness (`docs/modernization/03b-rewrite/INQACC/interface.md:3`,
`modern/inqacc/src/test/java/com/bankofz/inqacc/InqaccAcceptanceTest.java:600`).
A green core workflow does not approve production runtime versions, deploy
the application, reroute traffic or resolve the architecture's service gates.
