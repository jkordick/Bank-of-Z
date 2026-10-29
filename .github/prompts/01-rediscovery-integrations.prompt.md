---
agent: 'cobol-archaeologist'
description: Phase 1 — Inventory every external touchpoint of Bank-of-Z, reconciled with the call graph and data-access map.
---

Inventory every place this system talks to something outside a single program. Append to `docs/modernization/01-rediscovery/integrations.md`:

- CICS transactions and program-to-program `LINK` / `XCTL` / `RETURN TRANSID` / `RUN TRANSID` hand-offs.
- DB2 tables, IMS databases (via DL/I) and files — with the programs that touch them.
- REST APIs exposed through z/OS Connect (`src/api/`) — operation → program mapping.
- MQ (`src/base/mq/`), Java / JNI bridges, PL/I programs.
- Batch: JCL jobs, datasets, schedules (`src/base/batch/jcl/`).
- Third-party / partner systems referenced by name, hard-coded hostnames, ports, abend codes that operations depend on.

For each entry: cite `path/to/file:LINE`, note direction (in / out / both), and whether it is *load-bearing* (production) or *legacy-only* (data load, test, setup).

**Reconcile** at the end of the file with a section *Source-of-truth check*:

- every edge in `00-source-of-truth/call-graph.md` and every row in `data-access.md` → the integrations entry that covers it, or `missing`;
- every `?` (unresolved) target → what you resolved it to **with the line that proves it**, or `still unresolved`;
- every integration you list that is **not** in the source of truth (BMS, JCL, APIs, MQ…) → marked `outside tool scope`.

If you find a system whose purpose is unclear, add it to `docs/modernization/01-rediscovery/open-questions.md`. Do not guess.

Stop when the pass is done and print what was added.
