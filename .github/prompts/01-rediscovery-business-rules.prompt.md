---
agent: 'cobol-archaeologist'
description: Phase 1 — Extract business rules for ONE program/module and cross-check them against its decision points.
---

Extract business rules for **one** program or module. Ask which one if it is not obvious from the conversation. Use `docs/modernization/01-rediscovery/repo-map.md` for orientation.

1. Read `docs/modernization/00-source-of-truth/programs/<PROGRAM>.md` first. Its *Decision points* section lists every `IF` / `EVALUATE` the parser found — that is where rules live.
2. Read only the files that belong to the module: the program, its copybooks (listed in the digest) and directly-linked programs. Do not sweep the whole repo.
3. Append findings to `docs/modernization/01-rediscovery/business-rules.md` under a heading for the program:
   - One rule per bullet, in plain English, with an ID (`XFRFUN-R01`, …).
   - Cite `path/to/file:LINE` of the decision point (and of any 88-level / literal it uses).
   - Give a worked example (input → output) for anything non-trivial, using COMMAREA field names.
   - Preserve edge cases explicitly — including branches that look dead but might not be.
4. **Coverage check.** At the end of the section add a table *Decision coverage*: every decision point from the digest → the rule ID(s) covering it, or `technical` (pure RESP / SQLCODE plumbing), or `UNCOVERED`. If the program is a parser gap, write "decision points unverified by tooling" instead.
5. If anything cannot be resolved from the code alone — or contradicts the digest — **stop and add it to** `docs/modernization/01-rediscovery/open-questions.md` with both references. Never guess.

When done, print: program name, rule count, decision points covered / technical / uncovered, open questions raised. Then stop.
