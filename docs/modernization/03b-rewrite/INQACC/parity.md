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
| INQACC-T01 | PASS - configured sort code, account selector, complete output copy, pointer retained | 3f18576 |
| INQACC-T03 | PASS - FETCH +100, successful CLOSE, only success changes to Y | 3f18576 |
| INQACC-T13 | PASS - 00000000 remains an ordinary lookup | 3f18576 |
| INQACC-T14 | PASS - 99999998 remains an ordinary lookup | 3f18576 |
| INQACC-T19 | PASS - FETCH +100 then CLOSE -904; exact diagnostic LINK, HRAC NODUMP CANCEL, no normal result | 3f18576 |

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

## Slice 2 - INQACC-R02

**Status: green locally; pending human review.** This section supersedes
slice 1's remaining-coverage snapshot, not its historical execution evidence.
All 12 tests mapped to R02 are now executable, including their required R04,
R05 and R07 interactions; this does not claim full coverage of those rules
(`docs/modernization/03b-rewrite/INQACC/acceptance-tests.md:209`).

| Test id | Status | commit |
|---|---|---|
| INQACC-T02 | PASS - highest number at configured sort code, not newest date or global highest; complete output copy | 11057ad |
| INQACC-T06 | PASS - highest type spaces retains all incoming fields except success Y; no lower-row fallback | 11057ad |
| INQACC-T07 | PASS - highest type low-values retains sentinel, other fields and pointer; success Y | 11057ad |
| INQACC-T11 | PASS - exact opposite-sign balance limits copied through highest mode | 11057ad |
| INQACC-T15 | PASS - actual row 99999999 still uses highest SELECT, not cursor lookup | 11057ad |
| INQACC-T28 | PASS - SELECT -911 records HNCS and abends; no retry or rollback | 11057ad |
| INQACC-T29 | PASS - SELECT -913 records HNCS and abends; no retry or rollback | 11057ad |
| INQACC-T30 | PASS - no configured-sort row gives +100, diagnostic HNCS and abend, not success | 11057ad |
| INQACC-T31 | PASS - SELECT -904 records HNCS and abends | 11057ad |
| INQACC-T32 | PASS - SELECT +923 records HNCS and abends without storm diagnostic | 11057ad |
| INQACC-T36 | PASS - SELECT +1 records HNCS and abends | 11057ad |
| INQACC-T38 | PASS - SELECT -305 records HNCS and abends without NULL defaulting | 11057ad |

### Red / green evidence

On 2026-10-30, after adding the 12 runnable tests and before modifying
production code, `bash modern/inqacc/run-tests.sh` compiled successfully
and exited 1: every new test failed with
`UnsupportedOperationException: Highest-account mode is outside slice 1`;
all five slice-1 tests passed. The runner reports every failing test before
exiting nonzero
(`modern/inqacc/src/test/java/com/bankofz/inqacc/InqaccAcceptanceTest.java:24`,
`modern/inqacc/src/test/java/com/bankofz/inqacc/InqaccAcceptanceTest.java:285`).

After adding highest-mode dispatch, shared output mapping and the exact SELECT
diagnostic/abend path, the same full-suite command exited 0 with **17/17
acceptance tests passing**, compiled with Java 21 `-Xlint:all -Werror`.
No downloaded dependencies or mainframe commands were used
(`modern/inqacc/run-tests.sh:13`,
`modern/inqacc/src/main/java/com/bankofz/inqacc/Inqacc.java:17`,
`modern/inqacc/src/main/java/com/bankofz/inqacc/Inqacc.java:90`).

Success fixtures compare every COMMAREA field, widths, date order, scale-2
decimals and pointer identity. Failure fixtures assert one configured-sort
SELECT, supplied context/clock, exact 600-character diagnostic including
the padded NCS name, SQLCODE sign, LINK-before-DISPLAY and HNCS NODUMP CANCEL;
there is no cursor, retry, storm display, normal result or explicit rollback
(`modern/inqacc/src/test/java/com/bankofz/inqacc/InqaccAcceptanceTest.java:119`,
`modern/inqacc/src/test/java/com/bankofz/inqacc/InqaccAcceptanceTest.java:163`,
`modern/inqacc/src/test/java/com/bankofz/inqacc/InqaccAcceptanceTest.java:177`,
`src/base/cics/cobol/INQACC.cbl:145`,
`src/base/cics/cobol/INQACC.cbl:874`,
`src/base/cics/cobol/INQACC.cbl:914`).

### Remaining coverage and boundary

**17 of 48 specified tests pass locally; 31 remain deferred, not waived.**
Both sentinel dispatch arms at source line 218 and all specified SELECT
SQLCODE classes at line 874 are covered. Highest-mode spaces/low-values
cover the corresponding output-gate arms at line 229. Other decision
deferrals and W01 remain as recorded for slice 1; the rollback-response IF
at line 687 remains **unverified by tooling**, outside this slice
(`docs/modernization/00-source-of-truth/programs/INQACC.md:79`,
`docs/modernization/03b-rewrite/INQACC/acceptance-tests.md:195`).

The public logical COMMAREA is unchanged; the new SELECT seam is an internal
adapter contract implemented only by a test double. No live DB2 ordering,
binary encoding, REST projection or caller-unit-of-work parity is claimed
(`docs/modernization/03b-rewrite/INQACC/interface.md:360`,
`modern/inqacc/src/test/java/com/bankofz/inqacc/InqaccAcceptanceTest.java:242`,
`modern/inqacc/src/test/java/com/bankofz/inqacc/InqaccAcceptanceTest.java:309`).
Legacy sources and routing are unchanged; the substitution audit remains
blocked and the strangler switch disabled
(`docs/modernization/02-substitution-audit/audit.md:7`,
`docs/modernization/03b-rewrite/INQACC/interface.md:344`).
Stop here for review; do not begin the next slice.

## Slice 3 - INQACC-R04 and INQACC-R05

**Status: green locally; pending human review.** This section supersedes
slice 2's remaining-coverage snapshot. All specified tests mapped to R04/R05
are now executable; eight new cases complete the coverage already supplied
by slices 1 and 2
(`docs/modernization/03b-rewrite/INQACC/acceptance-tests.md:209`).

| Test id | Status | commit |
|---|---|---|
| INQACC-T04 | PASS - fetched spaces suppress all account-field copies; only success becomes Y | 8781417 |
| INQACC-T05 | PASS - fetched eight low-values suppress copies without treating them as NULL | 8781417 |
| INQACC-T08 | PASS - MYSTERY! type copied exactly without domain validation | 8781417 |
| INQACC-T09 | PASS - TEST eyecatcher copied without ACCT validation | 8781417 |
| INQACC-T10 | PASS - opposite-sign balance limits, maximum interest and overdraft copied exactly | 8781417 |
| INQACC-T12 | PASS - zero interest/balance/overdraft and negative cent copied without recalculation | 8781417 |
| INQACC-T47 | PASS - leading/embedded/trailing spaces in nonblank type retained exactly | 8781417 |
| INQACC-T48 | PASS - leap-day and year-boundary dates reordered to DDMMYYYY | 8781417 |

### Execution evidence and tests-first exception

On 2026-10-30, the eight tests were added before any production change.
`bash modern/inqacc/run-tests.sh` compiled with Java 21
`--release 21 -Xlint:all -Werror` and passed **25/25 acceptance tests**,
including all 17 previously passing cases
(`modern/inqacc/run-tests.sh:13`,
`modern/inqacc/src/test/java/com/bankofz/inqacc/InqaccAcceptanceTest.java:24`).

**No red result was obtained.** The unchanged core already implements the
exact type gate and shared output mapping required by this slice
(`modern/inqacc/src/main/java/com/bankofz/inqacc/Inqacc.java:32`,
`modern/inqacc/src/main/java/com/bankofz/inqacc/Inqacc.java:36`).
The prompt's red-before-production step therefore has no failing behavior
to drive here: production code was not changed and no artificial failure was
introduced. These are regression tests for existing behavior, not evidence
of a new production implementation.

Each new fixture checks every logical COMMAREA field, caller snapshot, pointer
identity, fixed widths, exact scale-2 decimals, and precisely one ordered
OPEN/FETCH/CLOSE with no diagnostic LINK
(`modern/inqacc/src/test/java/com/bankofz/inqacc/InqaccAcceptanceTest.java:162`,
`modern/inqacc/src/test/java/com/bankofz/inqacc/InqaccAcceptanceTest.java:329`).
The expectations follow the source type/output gate and date/balance moves
(`src/base/cics/cobol/INQACC.cbl:229`,
`src/base/cics/cobol/INQACC.cbl:232`,
`src/base/cics/cobol/INQACC.cbl:546`,
`src/base/cics/cobol/INQACC.cbl:569`).

### Remaining coverage and boundary

**25 of 48 specified tests pass locally; 23 remain deferred, not waived.**
R04 has T04-T08/T47 and R05 has T01-T02/T08-T15/T47-T48 passing.
Both suppression cases now cover ordinary as well as highest lookup at
source line 229. Other decision deferrals and W01 remain unchanged.
The source-only rollback-response decision at line 687 remains **unverified
by tooling**, outside this slice
(`docs/modernization/03b-rewrite/INQACC/acceptance-tests.md:194`,
`docs/modernization/03b-rewrite/INQACC/acceptance-tests.md:213`).

The public contract, production core and legacy sources are unchanged;
no dependency, adapter, fail-code field or retirement is introduced.
The substitution audit remains blocked and strangler routes disabled
(`docs/modernization/02-substitution-audit/audit.md:7`,
`docs/modernization/03b-rewrite/INQACC/interface.md:344`).
No mainframe comparison, binary encoding or live unit-of-work parity is claimed.
Stop here for review; do not begin the next slice.

## Slice 4 - INQACC-R06

**Status: green locally; pending human review.** This section supersedes
slice 3's remaining-coverage snapshot. All 16 tests mapped to R06 pass,
including T19 from slice 1; 15 new cases complete R06 and the required
R07 diagnostic interactions
(`docs/modernization/03b-rewrite/INQACC/acceptance-tests.md:209`).

| Test id | Status | commit |
|---|---|---|
| INQACC-T16 | PASS - OPEN -904; exact HRAC diagnostic; no FETCH/CLOSE or storm display | 557bd85 |
| INQACC-T17 | PASS - FETCH -904; HRAC; no CLOSE or storm display | 557bd85 |
| INQACC-T18 | PASS - fetched row then CLOSE -904; HRAC, no populated result | 557bd85 |
| INQACC-T19 | PASS - existing FETCH +100 then CLOSE -904 regression retained | 557bd85 |
| INQACC-T20 | PASS - OPEN +923; storm display after LINK/error DISPLAY; HRAC | 557bd85 |
| INQACC-T21 | PASS - OPEN -923; no storm display; HRAC | 557bd85 |
| INQACC-T22 | PASS - FETCH +923; storm display before diagnostic LINK; HRAC | 557bd85 |
| INQACC-T23 | PASS - FETCH -923; no storm display; HRAC | 557bd85 |
| INQACC-T24 | PASS - CLOSE +923; storm display after LINK/error DISPLAY; HRAC | 557bd85 |
| INQACC-T25 | PASS - CLOSE -923; no storm display or normal result; HRAC | 557bd85 |
| INQACC-T26 | PASS - FETCH -911; exactly one FETCH, no CLOSE/retry; HRAC | 557bd85 |
| INQACC-T27 | PASS - FETCH -913; exactly one FETCH, no CLOSE/retry; HRAC | 557bd85 |
| INQACC-T33 | PASS - OPEN +100 is an error, not missing-account success; HRAC | 557bd85 |
| INQACC-T34 | PASS - CLOSE +100 is an error, not missing-account success; HRAC | 557bd85 |
| INQACC-T35 | PASS - FETCH +1 is an error; no CLOSE; HRAC | 557bd85 |
| INQACC-T37 | PASS - FETCH -305; no NULL default or CLOSE; HRAC | 557bd85 |

### Red / green evidence

On 2026-10-30, the 15 tests and OPEN/FETCH outcome injection were added
before production changes. `bash modern/inqacc/run-tests.sh` compiled and
exited 1: **11 of 40 tests failed**, all at the existing OPEN/FETCH
`UnsupportedOperationException` guards. All 25 prior tests and the four
new CLOSE cases passed. No artificial failure was introduced for existing
CLOSE behavior
(`modern/inqacc/src/test/java/com/bankofz/inqacc/InqaccAcceptanceTest.java:24`,
`modern/inqacc/src/test/java/com/bankofz/inqacc/InqaccAcceptanceTest.java:221`).

After implementing only OPEN/FETCH HRAC paths and sharing the existing
positive-923 storm display with CLOSE, the full module suite exited 0:
**40/40 acceptance tests pass**, compiled with Java 21
`--release 21 -Xlint:all -Werror`
(`modern/inqacc/run-tests.sh:13`,
`modern/inqacc/src/main/java/com/bankofz/inqacc/Inqacc.java:74`,
`modern/inqacc/src/main/java/com/bankofz/inqacc/Inqacc.java:84`,
`modern/inqacc/src/main/java/com/bankofz/inqacc/Inqacc.java:105`).

Fixtures assert exact ordered cursor/context/LINK/DISPLAY events, one padded
600-character diagnostic, supplied context/clock, signed SQLCODE, source
spelling, HRAC NODUMP/CANCEL, no normal result and no output/success assignment.
The complete record, widths, scale-2 amounts and pointer identity remain
unchanged. No retry, explicit rollback or handler substitution is introduced
(`modern/inqacc/src/test/java/com/bankofz/inqacc/InqaccAcceptanceTest.java:221`,
`modern/inqacc/src/test/java/com/bankofz/inqacc/InqaccAcceptanceTest.java:409`,
`src/base/cics/cobol/INQACC.cbl:273`,
`src/base/cics/cobol/INQACC.cbl:353`,
`src/base/cics/cobol/INQACC.cbl:460`).

### Remaining coverage and boundary

**40 of 48 specified tests pass locally; T39-T46 remain deferred, not waived.**
R01-R07 have all their mapped cases executable. Both SQLCODE arms at
source lines 273/353/460 and both storm-check arms at lines 604/616 now
have executable evidence. R08 handler decisions at lines 646/763 remain
deferred, with W01 unchanged; the rollback-response IF at line 687 remains
**unverified by tooling** and deferred
(`docs/modernization/03b-rewrite/INQACC/acceptance-tests.md:194`,
`docs/modernization/03b-rewrite/INQACC/acceptance-tests.md:213`).

The public logical contract and internal ports are unchanged. Legacy sources,
REST routing and dependencies are untouched; no component is retired.
The audit remains blocked, and strangler routes disabled
(`modern/inqacc/src/main/java/com/bankofz/inqacc/Inqacc.java:133`,
`modern/inqacc/src/main/java/com/bankofz/inqacc/Inqacc.java:155`,
`docs/modernization/02-substitution-audit/audit.md:7`,
`docs/modernization/03b-rewrite/INQACC/interface.md:344`).
No mainframe comparison, live adapter or unit-of-work parity is claimed.
Stop here for review; do not begin the next slice.

## Slice 5 - INQACC-R08 and remaining acceptance cases

**Status: green locally; pending human review.** This section supersedes
slice 4's remaining-coverage snapshot. T39-T46 were the only deferred cases;
all **48/48 specified acceptance tests now pass locally**, with no deferred
test IDs. This is source-derived fixture parity, not running-mainframe parity
or cutover approval
(`docs/modernization/03b-rewrite/INQACC/acceptance-tests.md:177`,
`modern/inqacc/src/test/java/com/bankofz/inqacc/InqaccAcceptanceTest.java:27`).

| Test id | Status | commit |
|---|---|---|
| INQACC-T39 | PASS - AFCR, one caller-unit-of-work rollback, unchanged fields with success N, no LINK/re-abend | fc2dcd4 |
| INQACC-T40 | PASS - AFCS, NORMAL rollback and success N, pointer retained | fc2dcd4 |
| INQACC-T41 | PASS - AFCT, NORMAL rollback and success N, no clearing/retry | fc2dcd4 |
| INQACC-T42 | PASS - AFCR failed rollback, exact HROL diagnostic/DISPLAY, no N assignment | fc2dcd4 |
| INQACC-T43 | PASS - AFCS failed rollback, HROL NODUMP/CANCEL, one rollback only | fc2dcd4 |
| INQACC-T44 | PASS - AFCT failed rollback, HROL not AFCT, no normal result | fc2dcd4 |
| INQACC-T45 | PASS - AD2Z displays captured SQL diagnostics, records SQLCODE zero and re-abends, no rollback/retry | fc2dcd4 |
| INQACC-T46 | PASS - ASRA at OPEN/FETCH/CLOSE/SELECT, exact handler diagnostic and re-abend, no SQL/storm display or rollback | fc2dcd4 |

### Red / green evidence

On 2026-10-31, the eight test registrations, dependency doubles and typed
handler/rollback carriers were added before handler behavior. An initial
compile exposed a serialization lint warning in the exception payload; marking
that invocation-local payload transient restored strict compilation.
`bash modern/inqacc/run-tests.sh` then exited 1: **all eight new cases failed
with the uncaught CapturedAbend**, while all 40 previously green cases passed.
No handler behavior was present at that red run
(`modern/inqacc/src/test/java/com/bankofz/inqacc/InqaccAcceptanceTest.java:66`,
`modern/inqacc/src/main/java/com/bankofz/inqacc/Inqacc.java:245`).

After adding the installed-handler catch boundary, source-derived R08 branches
and shared diagnostic construction, the same full module command exited 0
with **48/48 passing**, compiled with Java 21
`--release 21 -Xlint:all -Werror`. No dependencies were installed
(`modern/inqacc/run-tests.sh:13`,
`modern/inqacc/src/main/java/com/bankofz/inqacc/Inqacc.java:18`,
`modern/inqacc/src/main/java/com/bankofz/inqacc/Inqacc.java:83`).

The fixtures assert complete logical record shape/widths, exact scale-2
amounts, pointer identity, ordered DISPLAY/rollback/context/LINK effects,
one caller-unit-of-work rollback and its NORMAL/non-NORMAL outcomes.
Distinguishable EIB and rollback response fields verify HROL freeform versus
DISPLAY sources; AD2Z verifies SQLERRML slicing, all six adapter-supplied
SQLERRD DISPLAY strings and recorded-versus-displayed SQLCODE. The previous
HRAC/HNCS tests still prove explicit CANCEL abends do not invoke R08
(`modern/inqacc/src/test/java/com/bankofz/inqacc/InqaccAcceptanceTest.java:388`,
`modern/inqacc/src/test/java/com/bankofz/inqacc/InqaccAcceptanceTest.java:433`,
`modern/inqacc/src/test/java/com/bankofz/inqacc/InqaccAcceptanceTest.java:463`,
`modern/inqacc/src/test/java/com/bankofz/inqacc/InqaccAcceptanceTest.java:547`,
`src/base/cics/cobol/INQACC.cbl:724`,
`src/base/cics/cobol/INQACC.cbl:744`,
`src/base/cics/cobol/INQACC.cbl:799`).

### Final decision coverage and boundary

All R01-R08 mapped cases are executable. **11/11 digest decision points and
12/12 source-observed decisions have executed evidence or the existing W01
arm waiver**. Both outcomes of the source-only rollback-response IF are tested;
that decision remains **unverified by tooling**. No mutable storm flag or
invented path was added to reach the waived false arm at line 763
(`docs/modernization/00-source-of-truth/programs/INQACC.md:79`,
`src/base/cics/cobol/INQACC.cbl:687`,
`docs/modernization/03b-rewrite/INQACC/acceptance-tests.md:195`,
`docs/modernization/03b-rewrite/INQACC/acceptance-tests.md:215`).

The public `inquire(Commarea)` contract remains unchanged; only internal
captured-abend/SQL-diagnostic and rollback seams were added. There is no
fail-code field, retry, new commit, real transaction/data adapter, REST
implementation or binary codec. Caller-work effects are checked in a double,
not against a live unit of work
(`modern/inqacc/src/main/java/com/bankofz/inqacc/Inqacc.java:192`,
`modern/inqacc/src/main/java/com/bankofz/inqacc/Inqacc.java:236`,
`modern/inqacc/src/test/java/com/bankofz/inqacc/InqaccAcceptanceTest.java:672`).
Legacy sources, dependencies and routing remain untouched. The audit remains
blocked and strangler routes disabled; no component retirement or cutover
is claimed
(`docs/modernization/02-substitution-audit/audit.md:7`,
`docs/modernization/03b-rewrite/INQACC/interface.md:345`).
Stop here for review; do not begin another slice.
