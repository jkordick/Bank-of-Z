---
agent: 'module-rewriter'
description: Phase 3b — Derive behavior-preserving acceptance tests for ONE module before any rewrite.
---

Derive acceptance tests for **one** module — the same module you intend to rewrite next. Ask which one if it is not obvious.

Sources:

- `docs/modernization/01-rediscovery/business-rules.md` (the module's section, incl. its *Decision coverage* table).
- `docs/modernization/01-rediscovery/data-model.md` for input / output shapes.
- `docs/modernization/00-source-of-truth/programs/<PROGRAM>.md` — decision points and 88-level values.
- `docs/modernization/03b-rewrite/<module>/interface.md` if it exists.

Produce `docs/modernization/03b-rewrite/<module>/acceptance-tests.md`:

- One test per row: `test id | business rule ref | decision point (file:LINE) | input | expected output | notes`.
- Cover every business rule. If a rule has multiple branches (including edge cases marked "possibly dead"), test each branch.
- Include the unhappy paths the legacy code handles: not-found, `SQLCODE` failures, deadlock retry exhaustion, rollback.
- Use exact decimal values for money — no rounding the legacy code doesn't do.
- If a rule is ambiguous, **stop** and add the question to `docs/modernization/01-rediscovery/open-questions.md`. Never invent expected outputs.

Do **not** write test code yet and do **not** touch the new implementation. Print the test count, decision points covered vs. total, and any questions raised. Stop.
