# Bank of Z

IBM Z sample banking application with CICS/BMS screens and DB2-backed COBOL (`src/base/cics/bms/BNK1MAI.bms:14`, `src/base/cics/cobol/INQACC.cbl:10`).
IMS includes COBOL message programs, PL/I and Java (`src/base/ims/cobol/IBACSUM.cbl:202`, `src/base/ims/pli/IBLOGIN.pli:14`, `src/base/ims/java/src/main/java/nazare/jmp/controller/IMSBankHistory.java:29`).
Batch generates monthly statements; z/OS Connect exposes REST operations (`src/base/batch/cobol/BNKSTMTC.cbl:8`, `src/api/src/main/api/openapi.yaml:1`).
Start with `docs/modernization/01-rediscovery/repo-map.md`; the authoritative COBOL inventory is `docs/modernization/00-source-of-truth/index.md:16`.

## Directory and naming conventions

- `src/base/cics/cobol/`: `BNK1*` BMS front-ends, `BNKMENU` menu; separate operation back-ends such as `INQACC` (`src/base/cics/cobol/BNK1CAC.cbl:10`, `src/base/cics/cobol/BNKMENU.cbl:11`, `src/base/cics/cobol/INQACC.cbl:10`).
- `src/base/ims/cobol/`: `IB*` message programs and `LOAD*` data loaders; `DBD/` and `PSB/` are assembler database/view definitions (`src/base/ims/cobol/IBGCUDAT.cbl:179`, `src/base/ims/cobol/LOADACCT.cbl:159`, `src/base/ims/DBD/ACCOUNT.asm:13`, `src/base/ims/PSB/IBACSUM.asm:10`).
- `src/base/*/copy/`: `.cpy` members; base members take precedence over API copies. BMS `.bms` sources are separate from generated symbolic copybooks (`docs/modernization/00-source-of-truth/index.md:67`, `docs/modernization/00-source-of-truth/index.md:88`).
- `src/base/batch/`: COBOL/PL/I statement programs and JCL; `src/base/mq/java/`: MQ clients (`src/base/batch/cobol/BNKSTMTC.cbl:8`, `src/base/batch/pli/BNKSTMTP.pli:12`, `src/base/batch/jcl/BNKSTMTC.jcl:8`, `src/base/mq/java/src/main/java/com/ibm/boq/TransferMoneyToBOZViaCICS.java:32`).
- `src/api/src/main/`: `api/openapi.yaml`, URL-encoded `operations/` directories and `zosAssets/` providers. `providerFiles/gen/` is generated material (`src/api/src/main/api/openapi.yaml:1`, `src/api/src/main/operations/%2Faccounts/get/operation.yaml:3`, `docs/modernization/00-source-of-truth/index.md:96`).
- `.setup/`: DBB build, deployment and runtime definitions; `.github/workflows/` invokes remote setup (`.setup/build/dbb-build.yaml:39`, `.setup/deploy/Development.yml:11`, `.github/workflows/Setup.yml:34`).

## Rediscovery boundaries

Do not modify `src/`, `.setup/`, `docs/docs/`, workflows, or vendored/generated application files during rediscovery. Write findings only under `docs/modernization/01-rediscovery/`; root `AGENTS.md` is a bootstrap-only exception. Never hand-edit `docs/modernization/00-source-of-truth/` (generator contract: `.github/skills/cobol-rekt/run-cobol-rekt.sh:10`).

No mainframe is attached. Do not compile, deploy, invoke remote setup, or run CICS/IMS/DB2 commands. Describe the existing system; do not propose modernization during rediscovery.

## Evidence and source-of-truth rebuild

Read `00-source-of-truth/index.md`, then `programs/<PROGRAM>.md` before describing a program; check cross-program claims against `call-graph.md` and `data-access.md`. Cite specific claims as `path/to/file:LINE`. Prefer original source over parsed facts, then scanned/configuration facts; log contradictions and unknowns in `01-rediscovery/open-questions.md` instead of guessing (`docs/modernization/00-source-of-truth/index.md:5`).

**BNK1CCS, BNK1DCS, CRECUST, IBTRAN and BNKSTMTC have control flow unverified by tooling.** Repeat this qualification in every artifact relying on them; non-COBOL artifacts require direct source citations (`docs/modernization/00-source-of-truth/index.md:16`, `docs/modernization/00-source-of-truth/index.md:106`).

Rebuild from the repository root using the `cobol-rekt` skill:

```bash
.github/skills/cobol-rekt/run-cobol-rekt.sh
# Selected programs:
.github/skills/cobol-rekt/run-cobol-rekt.sh XFRFUN INQCUST
```

Requires Java 21+, Python 3.9+ and curl. The pinned, checksum-verified parser is downloaded into `.tools/` on first use; analysis uses scratch copies and writes generated documentation, not legacy sources (`.github/skills/cobol-rekt/SKILL.md:21`, `.github/skills/cobol-rekt/run-cobol-rekt.sh:21`, `.github/skills/cobol-rekt/run-cobol-rekt.sh:49`, `.github/skills/cobol-rekt/run-cobol-rekt.sh:90`).
