---
agent: 'cobol-archaeologist'
description: Phase 1 — Build the deterministic source of truth with cobol-rekt before any LLM rediscovery.
---

Build the deterministic source of truth for this code base. You are running a parser, not interpreting code.

1. Run the `cobol-rekt` skill from the repository root:

   ```bash
   .github/skills/cobol-rekt/run-cobol-rekt.sh
   ```

   If Java 21+ or Python 3 is missing, stop and tell me what to install. Do not try to work around it.

2. Read `docs/modernization/00-source-of-truth/index.md` and report, in a short table:
   - programs parsed vs. parser gaps, per subsystem (CICS / IMS / batch),
   - stubbed copybooks grouped by reason,
   - artifact types that cobol-rekt does **not** cover.

3. Create `docs/modernization/01-rediscovery/open-questions.md` if it does not exist and add a section **Tooling gaps** with one entry per ❌ program: program, cobol-rekt message, and the consequence ("control flow and decisions of X are unverified — rediscovery claims about X need manual review").

4. Show me the `call-graph.md` mermaid diagram and point out one fact the tool could **not** resolve (an `?` target). Do not resolve it yourself yet — just name it.

Do **not** edit the generated files, do **not** extract business rules, do **not** touch legacy code. Stop after the summary.
