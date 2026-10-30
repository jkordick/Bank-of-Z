# INQACC parity

## Slice 1 - INQACC-R01 and INQACC-R03

**Status: green locally; pending human review.** This is executable parity
against the accepted source-derived fixtures, not a comparison with a running
mainframe or approval to switch traffic. The acceptance matrix maps R01 to
T01/T13/T14 and R03 to T03/T19
(`docs/modernization/03b-rewrite/INQACC/acceptance-tests.md:209`).
T01/T13/T14 necessarily exercise the ordinary output-copy portion of R05;
T19 includes the CLOSE-error portion of R06. Neither implies full R05/R06
coverage (`src/base/cics/cobol/INQACC.cbl:229`,
`src/base/cics/cobol/INQACC.cbl:353`).

| Test id | Status | commit |
|---|---|---|
| INQACC-T01 | PASS - configured sort code, account selector, complete output copy, pointer retained | pending |
| INQACC-T03 | PASS - FETCH +100, successful CLOSE, only success changes to Y | pending |
| INQACC-T13 | PASS - 00000000 remains an ordinary lookup | pending |
| INQACC-T14 | PASS - 99999998 remains an ordinary lookup | pending |
| INQACC-T19 | PASS - FETCH +100 then CLOSE -904; exact diagnostic LINK, HRAC NODUMP CANCEL, no normal result | pending |

### Red / green evidence

On 2026-10-30, `bash modern/inqacc/run-tests.sh` first exited 1 because the
implementation did not exist. After adding only the typed contract and an
unimplemented entry stub, it compiled and exited 1 at T01 with
`UnsupportedOperationException: INQACC slice 1 is not implemented`.
The inquiry behavior was added only after that executable red result.

The complete module suite then passed all five tests. The runner compiles all
module production/test Java with `--release 21 -Xlint:all -Werror` before
execution (`modern/inqacc/run-tests.sh:13`). No dependencies, mainframe access
or legacy edits were required. Tests compare all logical COMMAREA fields,
fixed widths, exact scale-2 decimal values and pointer identity; T19 checks
the supplied context/clock, record padding, original message spelling and
LINK-before-DISPLAY ordering
(`modern/inqacc/src/test/java/com/bankofz/inqacc/InqaccAcceptanceTest.java:31`,
`modern/inqacc/src/test/java/com/bankofz/inqacc/InqaccAcceptanceTest.java:72`,
`modern/inqacc/src/test/java/com/bankofz/inqacc/InqaccAcceptanceTest.java:134`).

### Remaining decision coverage

All eight rules and all digest decisions retain their specification mappings
in `acceptance-tests.md`; only the five rows above are executable. **43 of
48 specified tests remain deferred**, not passing or waived. W01 remains the
existing source-supported waiver, not a new slice waiver
(`docs/modernization/03b-rewrite/INQACC/acceptance-tests.md:213`).

| Source decision | Slice 1 evidence / deferred arms |
|---|---|
| `src/base/cics/cobol/INQACC.cbl:218` | Ordinary arm: T01/T13/T14. Sentinel arm deferred to R02. |
| `src/base/cics/cobol/INQACC.cbl:229` | Nonblank copy: T01/T13/T14; initialized internal type after +100: T03. Fetched spaces/low-values deferred to R04. |
| `src/base/cics/cobol/INQACC.cbl:273` | OPEN 0 in all five tests. OPEN nonzero deferred to R06. |
| `src/base/cics/cobol/INQACC.cbl:353` | CLOSE 0: T01/T03/T13/T14; CLOSE -904: T19. Other codes deferred. |
| `src/base/cics/cobol/INQACC.cbl:450` | Both arms: T01/T13/T14 versus T03/T19. |
| `src/base/cics/cobol/INQACC.cbl:460` | FETCH 0: T01/T13/T14; +100 exits before this decision. Other codes deferred to R06. |
| `src/base/cics/cobol/INQACC.cbl:604` | WHEN OTHER, CLOSE -904: T19. Positive 923 and other sites deferred to R07. |
| `src/base/cics/cobol/INQACC.cbl:616` | No storm DISPLAY for -904: T19. Positive branch deferred to R07. |
| `src/base/cics/cobol/INQACC.cbl:646` | Handler decisions deferred to R08. |
| `src/base/cics/cobol/INQACC.cbl:763` | Handler guard deferred to R08; existing W01 applies to its false arm. |
| `src/base/cics/cobol/INQACC.cbl:874` | Highest SELECT deferred to R02. |
| `src/base/cics/cobol/INQACC.cbl:687` | Source-only rollback-response decision, **unverified by tooling**; deferred to R08. |

The development guards and logical-to-legacy field mapping are documented in
`docs/modernization/03b-rewrite/INQACC/interface.md:360`. No REST handler,
Spring service, binary COMMAREA adapter, real DB2/CICS integration, cutover or
component retirement is claimed. The audit remains blocked
(`docs/modernization/02-substitution-audit/audit.md:7`), and the strangler
routes remain disabled (`docs/modernization/03b-rewrite/INQACC/interface.md:344`).
Stop here for review; do not begin the next slice.
