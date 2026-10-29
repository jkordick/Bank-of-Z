---
agent: 'substitution-auditor'
description: Phase 2 — Walk one category or subsystem of the rediscovery and produce audit rows.
---

Do one pass of the substitution audit. Ask which **category** or **subsystem** to cover if it is not obvious from the conversation. Suggested categories:

- Home-grown code with a modern equivalent (deadlock retry loops, named-counter IDs, `ENQ`/`DEQ`, date handling via `CEE*`, abend handling).
- Presentation: 3270 BMS screens and pseudo-conversational flows.
- Dated integrations (COMMAREA `LINK`s, DL/I message processing, MQ, JNI, batch file drops).
- Data stores (DB2 tables, IMS hierarchical databases).
- Existing seams (z/OS Connect APIs).
- Operational assumptions (JCL scheduling, CICS/IMS region config, RACF, `SYNCPOINT` units of work).

Append rows to `docs/modernization/02-substitution-audit/audit.md`:

| legacy element | rediscovery ref | verdict | reason | trade-off |
|---|---|---|---|---|

Rules:

- `verdict` is exactly one of: `keep-as-is` | `replace-with-library` | `replace-with-platform` | `retire` | `blocked`.
- Every row cites a specific entry in `docs/modernization/01-rediscovery/`.
- Every row needs an explicit trade-off — including `keep-as-is` (what staying costs) and `retire` (what is lost). A row without one is not accepted.
- An element that depends on an open question is `blocked` and cites the question.
- Do **not** name specific target frameworks, propose service boundaries, or write code. That is phase 3a.

Stop after the pass, print the rows added, and wait for review.
