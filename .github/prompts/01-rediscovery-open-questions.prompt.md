---
agent: 'cobol-archaeologist'
description: Phase 1 — Sweep the rediscovery artifacts for gaps, guesses, unresolved behavior and contradictions with the source of truth.
---

Sweep everything under `docs/modernization/01-rediscovery/` for things that need a human answer. This is an audit of the artifacts you produced, checked against the generated `docs/modernization/00-source-of-truth/`. Do **not** re-read whole programs; you may open a cited legacy line (± a few lines) when the digest alone cannot confirm a claim.

Look for:

- Rules stated in hedging language ("appears to", "seems to", "probably").
- Code values (88-levels, fail codes, abend codes) without a documented meaning.
- Integrations without an owner or purpose; `still unresolved` targets.
- Branches noted as "possibly dead" without confirmation.
- `UNCOVERED` decision points in the *Decision coverage* tables.
- Contradictions between `business-rules.md`, `data-model.md` and `integrations.md`.
- **Contradictions with the source of truth** — spot-check at least 10 citations: does the cited line exist in the digest's decision points / touchpoints / data items, and does the source line say what the artifact claims? Log every mismatch as `CONTRADICTION`.
- Claims about ❌ programs that are not flagged as unverified.

Consolidate every finding into `docs/modernization/01-rediscovery/open-questions.md` with:

- The specific artifact reference (and source-of-truth reference where relevant).
- The question, phrased so a domain expert can answer yes/no or fill a blank.
- The blast radius if the answer is wrong (which downstream phase depends on it).

Print the count of open questions grouped by blast radius, and the number of contradictions found, then stop.
