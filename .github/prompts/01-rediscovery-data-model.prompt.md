---
agent: 'cobol-archaeologist'
description: Phase 1 — Reverse-engineer the data model from DB2 DDL, copybooks, IMS DBDs and defensive code.
---

Reverse-engineer the data model. Sources, in order of trust:

1. DB2 DDL (`.setup/jcl/*/Db2-create.j2`) and IMS database definitions (`src/base/ims/DBD/*.asm`, views in `PSB/`).
2. Copybooks and record layouts (`src/base/*/copy/`), incl. DB2 host-variable layouts (`ACCDB2`, `PROCDB2`, …).
3. Invariants enforced only by defensive `IF` / `EVALUATE` in program code.

Cross-check with `docs/modernization/00-source-of-truth/data-access.md`: every table and DL/I-accessed program listed there must appear in your model — or be listed under *Not modelled* with a reason.

Append to `docs/modernization/01-rediscovery/data-model.md`:

- Entities with fields, types and PICTURE clauses (keep `COMP-3` / signed / decimal places explicit).
- Relationships (foreign keys, composite keys like sort code + account number, "join by convention").
- Where the same entity exists twice (DB2 for CICS and IMS DB for IMS) — map one to the other.
- Invariants — including those only enforced in code. Cite file:LINE.
- Value domains for encoded fields (88-levels from the digests, e.g. `PROC-TY-*` transaction types) with the meaning of each code.

If an entity is ambiguous or a code's meaning cannot be inferred, log it to `docs/modernization/01-rediscovery/open-questions.md`.

Add a mermaid ER diagram at the top of the file. Stop when the pass is done and print what was added.
