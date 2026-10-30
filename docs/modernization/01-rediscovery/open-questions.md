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
