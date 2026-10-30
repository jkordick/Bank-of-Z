# Open questions

## Tooling gaps

The full cobol-rekt `v0.1.1-RC03` run reports five parser gaps
(`docs/modernization/00-source-of-truth/index.md:16`). These are tooling
limitations, not conclusions about source correctness. Scan-derived facts
remain available, but do not verify control flow or decisions.

| Program | cobol-rekt messages | Consequence | Evidence |
|---|---|---|---|
| `BNKSTMTC` (batch) | `java.lang.UnsupportedOperationException: Unknown type: $$$$$$$$$$999` | Control flow and decisions of BNKSTMTC are unverified by tooling; rediscovery claims about BNKSTMTC need manual review. | `docs/modernization/00-source-of-truth/programs/BNKSTMTC.md:21`; source: `src/base/batch/cobol/BNKSTMTC.cbl` |
| `BNK1CCS` (CICS) | `Syntax error on 'NOUCTRAN'`; `Syntax error on 'OR'`; `Syntax error on 'TRANIDONLY'`; `No viable alternative at input E`; `Encountered invalid token. Analysis skipped to the next verb or period.` | Control flow and decisions of BNK1CCS are unverified by tooling; rediscovery claims about BNK1CCS need manual review. | `docs/modernization/00-source-of-truth/programs/BNK1CCS.md:21`; reported tokens: `src/base/cics/cobol/BNK1CCS.cbl:420`, `src/base/cics/cobol/BNK1CCS.cbl:422`, `src/base/cics/cobol/BNK1CCS.cbl:1145`, `src/base/cics/cobol/BNK1CCS.cbl:1147` |
| `BNK1DCS` (CICS) | `Syntax error on 'NOUCTRAN'`; `Syntax error on 'OR'`; `Syntax error on 'TRANIDONLY'`; `No viable alternative at input E`; `Encountered invalid token. Analysis skipped to the next verb or period.` | Control flow and decisions of BNK1DCS are unverified by tooling; rediscovery claims about BNK1DCS need manual review. | `docs/modernization/00-source-of-truth/programs/BNK1DCS.md:21`; reported tokens: `src/base/cics/cobol/BNK1DCS.cbl:558`, `src/base/cics/cobol/BNK1DCS.cbl:560`, `src/base/cics/cobol/BNK1DCS.cbl:1767`, `src/base/cics/cobol/BNK1DCS.cbl:1769` |
| `CRECUST` (CICS) | `Syntax error on '('`; `Syntax error on 'ABEND'`; `No viable alternative at input (`; `A period was assumed before "WHEN".`; `Encountered invalid token. Analysis skipped to the next verb or period.` | Control flow and decisions of CRECUST are unverified by tooling; rediscovery claims about CRECUST need manual review. | `docs/modernization/00-source-of-truth/programs/CRECUST.md:21`; reported ABEND tokens: `src/base/cics/cobol/CRECUST.cbl:1055`, `src/base/cics/cobol/CRECUST.cbl:1455` |
| `IBTRAN` (IMS) | `java.lang.NullPointerException: Cannot invoke "org.eclipse.lsp.cobol.core.CobolParser$EntryNameContext.getText()" because the return value of "org.eclipse.lsp.cobol.core.CobolParser$DataDescriptionEntryFormat1Context.entryName()" is null` | Control flow and decisions of IBTRAN are unverified by tooling; rediscovery claims about IBTRAN need manual review. | `docs/modernization/00-source-of-truth/programs/IBTRAN.md:21`; source: `src/base/ims/cobol/IBTRAN.cbl` |

### Unresolved target

The generated call graph leaves `CRECUST`'s `RUN TRANSID` target as
`?WS-RUN-TRANSID` (`docs/modernization/00-source-of-truth/call-graph.md:326`;
`src/base/cics/cobol/CRECUST.cbl:687`). Which transaction IDs this target can
take remains unresolved here. CRECUST's control flow and decisions are
unverified by tooling, as recorded above.

## XFRFUN

### XFRFUN-Q01 - CONTRADICTION: incomplete parsed decision inventory

The digest reports **40 decisions** and only **two in AH010**: `EVALUATE
MY-ABEND-CODE` at 1931 and `IF WS-STORM-DRAIN = 'N'` at 2042
(`docs/modernization/00-source-of-truth/programs/XFRFUN.md:12`,
`docs/modernization/00-source-of-truth/programs/XFRFUN.md:83`,
`docs/modernization/00-source-of-truth/programs/XFRFUN.md:151`).
The original source also contains `IF WS-CICS-RESP NOT = DFHRESP(NORMAL)`
inside the AFCR/AFCS/AFCT branch at
`src/base/cics/cobol/XFRFUN.cbl:1966`, ending at
`src/base/cics/cobol/XFRFUN.cbl:2032`. This makes 41 source-observed
IF/EVALUATE decisions, not 40. Why does tooling omit this rollback-failure
branch? Do not infer full control-flow coverage from the successful-parse
marker. The business-rules coverage table preserves all 40 digest entries
and reports the additional technical decision separately; generated files
have not been hand-edited.

### XFRFUN-Q02 - TO SELECT errors other than +100/-911 fall through

The parsed inventory includes the nonzero SELECT gate, +100 test and -911
test (`docs/modernization/00-source-of-truth/programs/XFRFUN.md:122`).
Re-reading the original source shows `COMM-SUCCESS='N'` and failure code
`'3'` on a non-+100 TO read failure, but the diagnostic LINK and `RUF2`
abend are inside `IF SQLCODE = -911`
(`src/base/cics/cobol/XFRFUN.cbl:1188`,
`src/base/cics/cobol/XFRFUN.cbl:1197`,
`src/base/cics/cobol/XFRFUN.cbl:1341`,
`src/base/cics/cobol/XFRFUN.cbl:1345`).
The sentence closes at `src/base/cics/cobol/XFRFUN.cbl:1350`, followed
by balance addition at `src/base/cics/cobol/XFRFUN.cbl:1357` and UPDATE
at `src/base/cics/cobol/XFRFUN.cbl:1362`.
Is continuing after another read error intentional? What values may the
host variables contain after that failed SELECT, and what outcome occurs
on the supported DB2 runtime? Interpretation stops at the observed
fall-through; neither a safe rejection nor a successful transfer is
asserted.

### XFRFUN-Q03 - Restart crosses active PERFORM boundaries

The digest confirms GO TO edges from UADT010 to UPDATE-ACCOUNT-DB2
(`docs/modernization/00-source-of-truth/programs/XFRFUN.md:50`).
Those jumps occur at `src/base/cics/cobol/XFRFUN.cbl:1282` and
`src/base/cics/cobol/XFRFUN.cbl:1479`, while TO was entered with PERFORM
at `src/base/cics/cobol/XFRFUN.cbl:398` or
`src/base/cics/cobol/XFRFUN.cbl:600`, within the account-update section
performed at `src/base/cics/cobol/XFRFUN.cbl:296`.
The source increments the shared counter before testing `< 6`
(`src/base/cics/cobol/XFRFUN.cbl:1201`,
`src/base/cics/cobol/XFRFUN.cbl:1203`,
`src/base/cics/cobol/XFRFUN.cbl:1400`,
`src/base/cics/cobol/XFRFUN.cbl:1402`).
What are the supported compiler/runtime's return semantics after repeated
restarts through these active PERFORM ranges? The graph proves jump
targets, not that every restart produces exactly one complete transfer
and exactly two history rows. No runtime guarantee is inferred.

### XFRFUN-Q04 - TO error reason and abend diagnostics disagree

The digest preserves SELECT timeout tests using `13172894` but UPDATE
timeout tests using `13172872`
(`docs/modernization/00-source-of-truth/programs/XFRFUN.md:129`,
`docs/modernization/00-source-of-truth/programs/XFRFUN.md:136`).
The source uses `13172872` for deadlock/retry
(`src/base/cics/cobol/XFRFUN.cbl:1199`,
`src/base/cics/cobol/XFRFUN.cbl:1398`) and `13172894` for SELECT
timeout text (`src/base/cics/cobol/XFRFUN.cbl:1288`,
`src/base/cics/cobol/XFRFUN.cbl:1329`), but UPDATE timeout text tests
`13172872` (`src/base/cics/cobol/XFRFUN.cbl:1485`,
`src/base/cics/cobol/XFRFUN.cbl:1529`). The UPDATE error record is
assigned `ABND-CODE='RUF2'` at
`src/base/cics/cobol/XFRFUN.cbl:1522`, while the actual abend is
`RUF3` at `src/base/cics/cobol/XFRFUN.cbl:1546`.
Are these deliberate diagnostic conventions or defects? Literal behavior
is documented unchanged; no intended reason-code mapping is guessed.

### XFRFUN-Q05 - Reachability and meaning of VSAM storm-drain return

The digest lists AFCR/AFCS/AFCT handler cases
(`docs/modernization/00-source-of-truth/programs/XFRFUN.md:151`), but
the transfer's datastore operations here are DB2 SELECT/UPDATE/INSERT
(`src/base/cics/cobol/XFRFUN.cbl:930`,
`src/base/cics/cobol/XFRFUN.cbl:992`,
`src/base/cics/cobol/XFRFUN.cbl:1624`).
Which supported invocation can deliver these abends to this handler?
The source returns `COMM-SUCCESS='N'`, `COMM-FAIL-CODE='2'` after its
rollback (`src/base/cics/cobol/XFRFUN.cbl:1952`,
`src/base/cics/cobol/XFRFUN.cbl:2034`), reusing the code assigned for
a missing TO account at `src/base/cics/cobol/XFRFUN.cbl:1103`.
How do callers distinguish those cases? Reachability and production
usage remain unknown; these branches are not declared dead, and code
`2` is not described as uniquely meaning a missing destination.
