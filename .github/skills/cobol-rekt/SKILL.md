---
name: cobol-rekt
description: Builds the deterministic "source of truth" for the COBOL programs in this repository with cobol-rekt (parse tree, PERFORM/GO TO graph, IF/EVALUATE decision points, data items, EXEC CICS/SQL, DL/I calls, LINK/XCTL/CALL graph, DB2 table access). Use before rediscovery, when asked to build or refresh the source of truth, or when a claim about a program's control flow, data or dependencies has to be verified.
---

# cobol-rekt — source of truth

[cobol-rekt](https://github.com/avishek-sen-gupta/cobol-rekt) parses COBOL with
the Eclipse Che4z grammar. No LLM is involved, so its output is the reference
that anything an LLM writes about this code base is checked against.

## Build or refresh

From the repository root:

```bash
.github/skills/cobol-rekt/run-cobol-rekt.sh               # all programs (~1 min)
.github/skills/cobol-rekt/run-cobol-rekt.sh XFRFUN INQCUST  # only these
```

Requires Java 21+, Python 3.9+, curl. The first run downloads a pinned,
checksum-verified `smojol-cli.jar` (~75 MB) into `.tools/` (gitignored).

The script never modifies the sources. It normalises a scratch copy (line
numbers preserved), stubs copybooks that are not in the repo (CICS `DFH*`,
BMS symbolic maps, `SQLCA`), runs cobol-rekt per program and writes:

| File | Content |
|---|---|
| `docs/modernization/00-source-of-truth/index.md` | coverage per program, parser gaps, stubs, artifacts outside scope — **read first** |
| `docs/modernization/00-source-of-truth/programs/<PROGRAM>.md` | structure, decisions, EXEC CICS/SQL/DL/I, dependencies, 88-levels, data items |
| `docs/modernization/00-source-of-truth/call-graph.md` | LINK / XCTL / CALL / RETURN / START / RUN TRANSID, plus transaction → program from the CICS resource definitions in `.setup/zconfig/*.yaml` ([config]) |
| `docs/modernization/00-source-of-truth/data-access.md` | DB2 tables, CICS files, queues, DL/I calls per program |
| `docs/modernization/00-source-of-truth/raw/` | JSON + logs (gitignored) |

## Using it

1. Run the script if `index.md` is missing or older than the COBOL sources.
2. Read `index.md` and the digest of every program you make claims about.
3. Order of trust: **original source line** > **[rekt]** facts > **[scan]** / **[config]** facts > LLM output.
4. When an LLM-written claim contradicts the source of truth, do not silently
   fix either side — record it in `docs/modernization/01-rediscovery/open-questions.md`
   with both citations.
5. Programs marked ❌ (parser gap) have no verified control flow. Say so
   explicitly in any document that relies on them.
6. Never edit generated files by hand. Re-run the script instead.

## Known limits (be honest about them)

- Parser gaps on Bank-of-Z (v0.1.1-RC03): `BNK1CCS`, `BNK1DCS` (`DFHVALUE(UCTRAN)` /
  `DFHVALUE(TRANIDONLY)`), `CRECUST` (`DFHVALUE(...)` in `EVALUATE … WHEN`),
  `BNKSTMTC` (`$$$` floating-currency picture), `IBTRAN` (JNI copybook with unnamed fields).
  Their digests still contain every [scan] fact; only [rekt] facts are missing.
- `LOCAL-STORAGE` items and 88-levels are not in cobol-rekt's data model; the
  digest adds them from a source scan.
- Variables used as `PROGRAM(...)` / `TRANSID(...)` are resolved only from
  `VALUE` clauses and `MOVE 'literal'`. Values built with `STRING` show as `?NAME` and are not linked
  to transaction definitions — which IDs they can take is an inference to verify.
- BMS maps, JCL, PL/I, assembler (DBD/PSB), Java and z/OS Connect definitions
  are outside cobol-rekt's scope; they are listed in `index.md` but not analysed.
