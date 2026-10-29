---
agent: 'cobol-archaeologist'
description: Phase 1 — Bootstrap rediscovery. Produce a high-level map of Bank-of-Z and a curated AGENTS.md, consistent with the source of truth.
---

Bootstrap the rediscovery of this mainframe application. No business rules, data model or integrations yet — just orientation.

Prerequisite: `docs/modernization/00-source-of-truth/index.md` exists. If it does not, stop and tell me to run `/01-rediscovery-source-of-truth` first.

Do the following and stop:

1. Produce a **high-level map** at `docs/modernization/01-rediscovery/repo-map.md`:
   - COBOL programs grouped by subsystem (CICS / IMS / batch), one line each with its role. **Take the program list and counts from `00-source-of-truth/index.md`** — your map must not contain more or fewer programs.
   - Copybooks, BMS maps, JCL, PL/I, IMS DBD/PSB, Java, z/OS Connect API definitions, DB2 DDL (`.setup/jcl/*/Db2-create.j2`), build/deploy setup (`.setup/`, `.github/workflows/`).
   - Anything unusual (generated code under `src/api/**/gen`, duplicate copybook names, vendor drops, dead directories).
   - The entry points a user hits: CICS transaction IDs (from `call-graph.md`), IMS transactions, REST operations in `src/api/src/main/operations/`.
2. Draft `AGENTS.md` at the repo root with the minimum an agent needs to work here:
   - What this code base is, in 3–5 lines.
   - Directory conventions and naming rules you inferred (e.g. `BNK1*` = 3270 front-ends, `IB*` = IMS).
   - Files / folders the agent must **not** modify during rediscovery.
   - How the source of truth is (re)built.
3. Add *Suggested reading order* at the bottom of `repo-map.md`: the subsystems to tackle first, worst, and last, one-line reason each. Factor in parser gaps — a ❌ program is harder to verify.

**Do not** extract business rules, propose modernization, or edit legacy code. Cite `path/to/file:LINE` for anything specific.
