# Target architecture

## ADR-001 - Target runtime and platform

**Status: open; neither option selected.** The target language, framework and deployment platform remain undecided pending an accepted hosting and service contract.

### Justification

The existing REST mappings and configurable CICS/IMS connections provide a candidate boundary for evaluating a new runtime, not evidence of deployed use or approved contract preservation (A01/A02: `docs/modernization/01-rediscovery/integrations.md:334`, `docs/modernization/01-rediscovery/integrations.md:362`; audit rows: `docs/modernization/02-substitution-audit/audit.md:35`, `docs/modernization/02-substitution-audit/audit.md:54`). Existing Java/JDBC and Liberty configuration make JVM-based candidates worth evaluating, but do not prove application portability, team capability or supported target versions (I03: `docs/modernization/01-rediscovery/integrations.md:319`, `docs/modernization/01-rediscovery/integrations.md:327`; A02: `docs/modernization/01-rediscovery/integrations.md:364`; audit: `docs/modernization/02-substitution-audit/audit.md:48`). INT-Q01 leaves deployed callers, owners and service contracts unknown, while INT-Q06/Q07 leave identity enforcement and security/accounting dependencies unresolved; these inputs do not establish an approved host, capacity, availability target or platform preference (`docs/modernization/01-rediscovery/open-questions.md:332`, `docs/modernization/01-rediscovery/open-questions.md:404`, `docs/modernization/01-rediscovery/open-questions.md:415`). The audit also blocks transaction-manager substitution, so a framework transaction annotation cannot be assumed equivalent to the required banking unit of work (`docs/modernization/02-substitution-audit/audit.md:37`, `docs/modernization/02-substitution-audit/audit.md:56`).

### Two options

These are evaluation candidates, not approved substitutions or claims of compatibility.

| Option | Language, framework and deployment target | Trade-offs against the evidence |
|---|---|---|
| A - Off-mainframe JVM application | Java with Spring Boot, packaged as a Linux container on an operator-approved container hosting platform; provider and versions unselected. | Allows a separately operated HTTP application to be evaluated against the A01/A02 seam without initially relocating its dependencies. Adds a cross-platform network and identity boundary whose connectivity, latency, availability and support ownership need acceptance under INT-Q01/Q06/Q07; no evidence establishes container-hosting capability or that existing Java/JNI dependencies will run there. |
| B - Mainframe-hosted JVM application | Java with Jakarta EE on a supported Liberty runtime on z/OS; product edition and versions unselected. | Allows evaluation within the existing z/OS operational setting evidenced by A02, while retaining dependence on mainframe capacity, licensing and release operations. Existing z/OS Connect Liberty configuration is not evidence that it can host the new application, that its dependencies are Jakarta-compatible, or that co-location preserves transaction or identity semantics. |

### Consequences and selection gates

- Neither option authorizes provisioning, implementation, dependency upgrades, traffic movement or retirement. INT-Q01 and the consumed audit rows remain unresolved, not reclassified as keep/replace verdicts (`docs/modernization/01-rediscovery/open-questions.md:332`; `docs/modernization/02-substitution-audit/audit.md:7`).
- Before selection, obtain an owner-accepted deployment/service contract covering supported runtime versions, operating responsibility, cost/capacity, network access, latency, availability/recovery and enforced identity. Assess both candidates against that contract and the A01/A02 metadata requirements rather than assuming an existing Java or Liberty artifact determines the target (`docs/modernization/01-rediscovery/open-questions.md:332`, `docs/modernization/01-rediscovery/open-questions.md:404`, `docs/modernization/01-rediscovery/open-questions.md:415`).
- This record transfers no data ownership and selects no DB2/IMS migration or direct-access mechanism. A later coexistence decision must identify owners and supported access paths; preserve fixed-point money, distinguish NULL from zero/spaces and retain unresolved numeric declarations rather than normalizing them silently (`docs/modernization/02-substitution-audit/audit.md:39`; `docs/modernization/01-rediscovery/data-model.md:34`).
- A later transaction decision must define the atomic boundary for balances and history, commit/rollback ownership and failure/retry behavior before moving writes; neither HTTP calls nor co-location establish a shared unit of work. CICS and IMS durability questions remain blockers, not permission to split a transfer into independently committed calls (`docs/modernization/02-substitution-audit/audit.md:37`, `docs/modernization/02-substitution-audit/audit.md:56`).

**Verification boundary:** BNK1CCS, BNK1DCS, CRECUST, IBTRAN and BNKSTMTC have control flow **unverified by tooling**; the API and durability evidence above does not remove those limitations (`docs/modernization/00-source-of-truth/index.md:16`; `docs/modernization/01-rediscovery/integrations.md:9`). No module boundaries, integration replacement contracts or migration mechanism are selected in this runtime-only record.
