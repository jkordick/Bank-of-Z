# INQACC acceptance tests

## Scope and evidence

Test specification only: no executable tests or implementation. The module is
INQACC; the first-wave REST boundary is limited to the two inquiry GETs, not its
shared CICS callers (`docs/modernization/03a-architecture/target-architecture.md:41`,
`docs/modernization/03a-architecture/target-architecture.md:53`). These tests
specify the module contract, not HTTP mappings, authorization or cutover approval.
No existing `interface.md` is present.

The rule baseline is INQACC-R01 through R08
(`docs/modernization/01-rediscovery/business-rules.md:86`). Domain answers accept
source behavior, including quirks; the final INQACC-Q06 answer confirms that an
empty highest-account lookup abends HNCS, superseding the earlier Q03 wording
(`docs/modernization/01-rediscovery/open-questions.md:907`,
`docs/modernization/01-rediscovery/open-questions.md:964`). No new ambiguity or
question ID is raised by this pass.

The digest lists 11 decisions
(`docs/modernization/00-source-of-truth/programs/INQACC.md:79`). The additional
rollback-response IF at `src/base/cics/cobol/INQACC.cbl:687` remains **unverified by
tooling**; it is tested from source as accepted in INQACC-Q02
(`docs/modernization/01-rediscovery/open-questions.md:916`). Counts below
distinguish digest coverage from all 12 source-observed decisions.

## Fixtures and common assertions

Each row is one independent test. Begin with a fresh invocation and the initial
storm-drain flag N (`src/base/cics/cobol/INQACC.cbl:134`). Strings are fixed-width;
quoted shorter strings are right-padded with spaces. Identifiers and dates retain
leading zeroes. `LOW8` means eight low-value bytes, not NULL, an empty string or
spaces (`src/base/cics/copy/ACCOUNT.cpy:14`,
`src/base/cics/cobol/INQACC.cbl:229`).

**Input C** is a complete caller COMMAREA with these values:

| Field | C value | Definition |
|---|---|---|
| INQACC-EYE | `OLD!` | `src/base/cics/copy/INQACC.cpy:8` |
| INQACC-CUSTNO | `0000000007` | `src/base/cics/copy/INQACC.cpy:9` |
| INQACC-SCODE | `111111` | `src/base/cics/copy/INQACC.cpy:10` |
| INQACC-ACCNO | `00000042` | `src/base/cics/copy/INQACC.cpy:11` |
| INQACC-ACC-TYPE | `SAVING  ` | `src/base/cics/copy/INQACC.cpy:12` |
| INQACC-INT-RATE | `1.23` | `src/base/cics/copy/INQACC.cpy:13` |
| INQACC-OPENED | `01012020` | `src/base/cics/copy/INQACC.cpy:14` |
| INQACC-OVERDRAFT | `00000017` | `src/base/cics/copy/INQACC.cpy:19` |
| INQACC-LAST-STMT-DT | `01022020` | `src/base/cics/copy/INQACC.cpy:20` |
| INQACC-NEXT-STMT-DT | `01032020` | `src/base/cics/copy/INQACC.cpy:25` |
| INQACC-AVAIL-BAL | `12.34` | `src/base/cics/copy/INQACC.cpy:30` |
| INQACC-ACTUAL-BAL | `-56.78` | `src/base/cics/copy/INQACC.cpy:31` |
| INQACC-SUCCESS | `N` | `src/base/cics/copy/INQACC.cpy:32` |
| INQACC-PCB1-POINTER | a distinguishable valid pointer P | `src/base/cics/copy/INQACC.cpy:33` |

**Row R** supplies host values in the order declared at
`src/base/cics/cobol/INQACC.cbl:46`: eyecatcher `ACCT`, customer `0000000123`,
sort code `987654`, account `00000042`, type `CURRENT `, interest `3.25`,
opened `2026-01-02`, overdraft `250`, last statement `2026-09-30`, next statement
`2026-10-31`, available `-12.34`, actual `56.78`. Except for explicit overrides,
all values are non-NULL and all SQL operations return 0.

**Populated(R)** means C's account fields are replaced with R's values, with
opened/last/next dates `02012026`/`30092026`/`31102026`, overdraft `00000250`,
success Y and pointer P unchanged. Apply each row's overrides to this expectation.
Both lookup paths stage dates by field movement, not calendar arithmetic, and
move balances without rounding (`src/base/cics/cobol/INQACC.cbl:232`,
`src/base/cics/cobol/INQACC.cbl:546`,
`src/base/cics/cobol/INQACC.cbl:569`,
`src/base/cics/cobol/INQACC.cbl:953`,
`src/base/cics/cobol/INQACC.cbl:976`). **Unchanged(Y/N)** means every incoming
field, including the pointer, remains byte-for-byte unchanged except success,
which becomes the specified flag (`src/base/cics/cobol/INQACC.cbl:229`,
`src/base/cics/cobol/INQACC.cbl:756`).

**Ordinary** means one OPEN, one FETCH and one CLOSE in that order, using configured
sort code `987654` and the input account number, not input customer, sort code or
type (`src/base/cics/copy/SORTCODE.cpy:7`,
`src/base/cics/cobol/INQACC.cbl:80`,
`src/base/cics/cobol/INQACC.cbl:263`,
`src/base/cics/cobol/INQACC.cbl:345`,
`src/base/cics/cobol/INQACC.cbl:350`). **Highest** means a single SELECT filtered
by configured sort code, ordered by account number descending with one row only;
no cursor calls (`src/base/cics/cobol/INQACC.cbl:843`,
`src/base/cics/cobol/INQACC.cbl:869`). Injected SQL outcomes and handler entry are
dependency-level fixtures, not a claim that particular data or abends occur in
production.

**Fail(code, sqlcode, site)** means one diagnostic LINK attempt to ABNDPROC,
then the stated `NODUMP CANCEL` abend; no normal return, no invented failure
field, no retry and no explicit SYNCPOINT ROLLBACK in that path. The COMMAREA
has not been populated or assigned success at these injection sites
(`src/base/cics/cobol/INQACC.cbl:193`,
`src/base/cics/cobol/INQACC.cbl:229`,
`src/base/cics/cobol/INQACC.cbl:335`,
`src/base/cics/cobol/INQACC.cbl:415`,
`src/base/cics/cobol/INQACC.cbl:524`,
`src/base/cics/cobol/INQACC.cbl:932`).

For each diagnostic attempt assert ABND-CODE, ABND-SQLCODE and the site's
freeform message (OPEN/CLOSE RAD010, FETCH FD010, SELECT GLAD010), plus the
captured EIB response codes, task, transaction, application, program and supplied
clock values. Supply a deterministic CICS context/clock; compare the source's
date/time formatting rather than real elapsed time. Record fields are defined at
`src/base/cics/copy/ABNDINFO.cpy:7`; construction is at
`src/base/cics/cobol/INQACC.cbl:281`,
`src/base/cics/cobol/INQACC.cbl:361`,
`src/base/cics/cobol/INQACC.cbl:476`,
`src/base/cics/cobol/INQACC.cbl:883`,
`src/base/cics/cobol/INQACC.cbl:990`. The LINK double normally returns without
changing SQLCA. Assert an attempt, not guaranteed persisted ABNDFILE contents:
the recorder can display a WRITE failure and return
(`src/base/cics/cobol/ABNDPROC.cbl:141`,
`src/base/cics/cobol/ABNDPROC.cbl:149`).

All amounts are exact fixed-point decimals: host and interface balances are
`S9(10)V99`, with host `COMP-3`
(`src/base/cics/cobol/INQACC.cbl:57`,
`src/base/cics/copy/INQACC.cpy:30`). Boundary tests use these declarations,
not the conflicting ACCDB2 SQL declaration or another program's host variables
(`src/base/cics/copy/ACCDB2.cpy:18`,
`docs/modernization/01-rediscovery/data-model.md:44`,
`docs/modernization/01-rediscovery/open-questions.md:920`). No floating-point
conversion or new rounding/validation is allowed.

## Acceptance matrix

Rule references below refer to `docs/modernization/01-rediscovery/business-rules.md:92`
through `docs/modernization/01-rediscovery/business-rules.md:106`.
All decision citations are original source, including the source-only IF.

| Test id | Business rule ref | Decision point (file:LINE) | Input | Expected output | Notes |
|---|---|---|---|---|---|
| INQACC-T01 | INQACC-R01, R05 | `src/base/cics/cobol/INQACC.cbl:218`, `src/base/cics/cobol/INQACC.cbl:229`, `src/base/cics/cobol/INQACC.cbl:273`, `src/base/cics/cobol/INQACC.cbl:353`, `src/base/cics/cobol/INQACC.cbl:450`, `src/base/cics/cobol/INQACC.cbl:460` | C; R at 987654/00000042; distractor at 111111/00000042 | Ordinary; Populated(R); normal RETURN | Incoming customer/type mismatch does not reject R; incoming sort code is not used. Covers all ordinary success arms. |
| INQACC-T02 | INQACC-R02, R05 | `src/base/cics/cobol/INQACC.cbl:218`, `src/base/cics/cobol/INQACC.cbl:874`, `src/base/cics/cobol/INQACC.cbl:229` | C with ACCNO=99999999; R and account 00000010 at 987654; account 99999998 at 111111; lower account has newer opened date | Highest; Populated(R), including ACCNO=00000042 | Highest number at configured sort code, not newest opened date or global highest. |
| INQACC-T03 | INQACC-R03 | `src/base/cics/cobol/INQACC.cbl:450`, `src/base/cics/cobol/INQACC.cbl:353`, `src/base/cics/cobol/INQACC.cbl:229` | C; FETCH +100; CLOSE 0 | Ordinary; Unchanged(Y); no diagnostic LINK | Do not copy initialized internal data or invent not-found status. |
| INQACC-T04 | INQACC-R04 | `src/base/cics/cobol/INQACC.cbl:229` | C; R with type eight spaces | Ordinary; Unchanged(Y) | Gate uses fetched type, not incoming SAVING. |
| INQACC-T05 | INQACC-R04 | `src/base/cics/cobol/INQACC.cbl:229` | C; R with type LOW8 | Ordinary; Unchanged(Y) | Conditional low-value branch; do not waive as possibly dead or equate to NULL. |
| INQACC-T06 | INQACC-R02, R04 | `src/base/cics/cobol/INQACC.cbl:218`, `src/base/cics/cobol/INQACC.cbl:229` | C with ACCNO=99999999; highest R type eight spaces | Highest; Unchanged(Y); ACCNO stays 99999999 | No fallback to a lower nonblank row. |
| INQACC-T07 | INQACC-R02, R04 | `src/base/cics/cobol/INQACC.cbl:229` | C with ACCNO=99999999; highest R type LOW8 | Highest; Unchanged(Y) | Conditional host-value fixture; retain sentinel and all incoming fields. |
| INQACC-T08 | INQACC-R04, R05 | `src/base/cics/cobol/INQACC.cbl:229` | C; R type `MYSTERY!` | Ordinary; Populated(R) with type MYSTERY! | No named account-type domain validation. |
| INQACC-T09 | INQACC-R05 | `src/base/cics/cobol/INQACC.cbl:229` | C; R eyecatcher `TEST` | Ordinary; Populated(R) with eye TEST | ACCT 88-level is a value, not a validation gate (`src/base/cics/copy/ACCOUNT.cpy:9`). |
| INQACC-T10 | INQACC-R05 | `src/base/cics/cobol/INQACC.cbl:229` | C; R available 9999999999.99, actual -9999999999.99, interest 9999.99, overdraft 99999999 | Ordinary; exact supplied decimals and overdraft copied; Y; pointer P unchanged | Host/interface representable limits; not a claim about deployed DB2 capacity. |
| INQACC-T11 | INQACC-R02, R05 | `src/base/cics/cobol/INQACC.cbl:874`, `src/base/cics/cobol/INQACC.cbl:229` | C with ACCNO=99999999; R available -9999999999.99, actual 9999999999.99 | Highest; Populated(R) with exact opposite-sign extremes | Verify separate SELECT mapping, not only cursor mapping. |
| INQACC-T12 | INQACC-R05 | `src/base/cics/cobol/INQACC.cbl:229` | C; R available 0.00, actual -0.01, interest 0.00, overdraft 0 | Ordinary; exact 0.00/-0.01/0.00 and overdraft 00000000; Y | No clamping, balance recalculation or rounding. |
| INQACC-T13 | INQACC-R01, R05 | `src/base/cics/cobol/INQACC.cbl:218` | C with ACCNO=00000000; R with number 00000000 | Ordinary; Populated(R) with number 00000000 | No minimum-positive identifier check. |
| INQACC-T14 | INQACC-R01, R05 | `src/base/cics/cobol/INQACC.cbl:218` | C with ACCNO=99999998; R with number 99999998 | Ordinary; Populated(R) with number 99999998 | Adjacent value does not select Highest. |
| INQACC-T15 | INQACC-R02, R05 | `src/base/cics/cobol/INQACC.cbl:218` | C with ACCNO=99999999; configured-sort rows include R with number 99999999 and a lower number | Highest; Populated(R) with number 99999999 | Sentinel still selects Highest, even if an actual row has that number. |
| INQACC-T16 | INQACC-R06, R07 | `src/base/cics/cobol/INQACC.cbl:273`, `src/base/cics/cobol/INQACC.cbl:604`, `src/base/cics/cobol/INQACC.cbl:616` | C; OPEN -904 | Fail(HRAC, -904, OPEN); no FETCH/CLOSE; no storm diagnostic | Nonzero OPEN arm; only ordinary error display/record attempt. |
| INQACC-T17 | INQACC-R06, R07 | `src/base/cics/cobol/INQACC.cbl:450`, `src/base/cics/cobol/INQACC.cbl:460` | C; OPEN 0; FETCH -904 | Fail(HRAC, -904, FETCH); no CLOSE; no storm diagnostic | +100 test is false before other error handling. |
| INQACC-T18 | INQACC-R06, R07 | `src/base/cics/cobol/INQACC.cbl:353` | C; FETCH R; CLOSE -904 | Fail(HRAC, -904, CLOSE); no normal populated result | Successful FETCH does not bypass CLOSE failure. |
| INQACC-T19 | INQACC-R03, R06 | `src/base/cics/cobol/INQACC.cbl:450`, `src/base/cics/cobol/INQACC.cbl:353` | C; FETCH +100; CLOSE -904 | Fail(HRAC, -904, CLOSE), not Unchanged(Y) | Missing-account success requires successful CLOSE. |
| INQACC-T20 | INQACC-R06, R07 | `src/base/cics/cobol/INQACC.cbl:273`, `src/base/cics/cobol/INQACC.cbl:604`, `src/base/cics/cobol/INQACC.cbl:616` | C; OPEN +923 | Fail(HRAC, +923, OPEN); storm diagnostic with condition `DB2 Connection lost ` | Positive 923 exactly; diagnostic occurs after LINK on OPEN (`src/base/cics/cobol/INQACC.cbl:321`, `src/base/cics/cobol/INQACC.cbl:333`). |
| INQACC-T21 | INQACC-R06, R07 | `src/base/cics/cobol/INQACC.cbl:273`, `src/base/cics/cobol/INQACC.cbl:604`, `src/base/cics/cobol/INQACC.cbl:616` | C; OPEN -923 | Fail(HRAC, -923, OPEN); no storm diagnostic | Condition becomes `Not Storm Drain     `; no failure return or rollback. |
| INQACC-T22 | INQACC-R06, R07 | `src/base/cics/cobol/INQACC.cbl:460`, `src/base/cics/cobol/INQACC.cbl:604`, `src/base/cics/cobol/INQACC.cbl:616` | C; FETCH +923 | Fail(HRAC, +923, FETCH); storm diagnostic before LINK | Order differs from OPEN/CLOSE (`src/base/cics/cobol/INQACC.cbl:466`, `src/base/cics/cobol/INQACC.cbl:516`). |
| INQACC-T23 | INQACC-R06, R07 | `src/base/cics/cobol/INQACC.cbl:460`, `src/base/cics/cobol/INQACC.cbl:604`, `src/base/cics/cobol/INQACC.cbl:616` | C; FETCH -923 | Fail(HRAC, -923, FETCH); no storm diagnostic | Do not normalize SQLCODE sign. |
| INQACC-T24 | INQACC-R06, R07 | `src/base/cics/cobol/INQACC.cbl:353`, `src/base/cics/cobol/INQACC.cbl:604`, `src/base/cics/cobol/INQACC.cbl:616` | C; FETCH R; CLOSE +923 | Fail(HRAC, +923, CLOSE); storm diagnostic after LINK | No normal success despite retrieved row. |
| INQACC-T25 | INQACC-R06, R07 | `src/base/cics/cobol/INQACC.cbl:353`, `src/base/cics/cobol/INQACC.cbl:604`, `src/base/cics/cobol/INQACC.cbl:616` | C; FETCH R; CLOSE -923 | Fail(HRAC, -923, CLOSE); no storm diagnostic | No automatic retry or rollback. |
| INQACC-T26 | INQACC-R06 | `src/base/cics/cobol/INQACC.cbl:460` | C; FETCH -911 | Fail(HRAC, -911, FETCH); exactly one FETCH; no CLOSE | No retry loop to exhaust; do not import transfer-module retry behavior. |
| INQACC-T27 | INQACC-R06 | `src/base/cics/cobol/INQACC.cbl:460` | C; FETCH -913 | Fail(HRAC, -913, FETCH); exactly one FETCH; no CLOSE | Lock/deadlock SQL error is not handler-entry AD2Z. |
| INQACC-T28 | INQACC-R02 | `src/base/cics/cobol/INQACC.cbl:874` | C with ACCNO=99999999; SELECT -911 | Fail(HNCS, -911, SELECT); exactly one SELECT | No retry/exhaustion policy or explicit rollback exists here. |
| INQACC-T29 | INQACC-R02 | `src/base/cics/cobol/INQACC.cbl:874` | C with ACCNO=99999999; SELECT -913 | Fail(HNCS, -913, SELECT); exactly one SELECT | Preserve SELECT failure code, not HRAC. |
| INQACC-T30 | INQACC-R02 | `src/base/cics/cobol/INQACC.cbl:874` | C with ACCNO=99999999; no configured-sort rows; SELECT +100 | Fail(HNCS, +100, SELECT) | Q06 resolution; not ordinary missing-account success. |
| INQACC-T31 | INQACC-R02 | `src/base/cics/cobol/INQACC.cbl:874` | C with ACCNO=99999999; SELECT -904 | Fail(HNCS, -904, SELECT) | Generic negative SELECT failure. |
| INQACC-T32 | INQACC-R02, R07 | `src/base/cics/cobol/INQACC.cbl:874` | C with ACCNO=99999999; SELECT +923 | Fail(HNCS, +923, SELECT); no DB2 storm diagnostic | Highest failure path does not call the storm check (`src/base/cics/cobol/INQACC.cbl:874`). |
| INQACC-T33 | INQACC-R06 | `src/base/cics/cobol/INQACC.cbl:273` | C; injected OPEN +100 | Fail(HRAC, +100, OPEN); no FETCH/CLOSE | +100 exception is FETCH-specific, not global. |
| INQACC-T34 | INQACC-R06 | `src/base/cics/cobol/INQACC.cbl:353` | C; FETCH R; injected CLOSE +100 | Fail(HRAC, +100, CLOSE) | Do not treat every +100 as a missing record. |
| INQACC-T35 | INQACC-R06 | `src/base/cics/cobol/INQACC.cbl:450`, `src/base/cics/cobol/INQACC.cbl:460` | C; injected FETCH +1 | Fail(HRAC, +1, FETCH); no CLOSE | Nonzero positive warning-shaped outcome is still an error here. |
| INQACC-T36 | INQACC-R02 | `src/base/cics/cobol/INQACC.cbl:874` | C with ACCNO=99999999; injected SELECT +1 | Fail(HNCS, +1, SELECT) | Nonzero means positive as well as negative. |
| INQACC-T37 | INQACC-R06 | `src/base/cics/cobol/INQACC.cbl:460` | C; FETCH reports -305 for a NULL result without an indicator | Fail(HRAC, -305, FETCH); no CLOSE | Assert response to supplied SQLCODE; no invented NULL-to-zero/space default (`src/base/cics/cobol/INQACC.cbl:431`). |
| INQACC-T38 | INQACC-R02 | `src/base/cics/cobol/INQACC.cbl:874` | C with ACCNO=99999999; SELECT reports -305 | Fail(HNCS, -305, SELECT) | No indicator variables/defaulting in SELECT targets (`src/base/cics/cobol/INQACC.cbl:856`). |
| INQACC-T39 | INQACC-R08 | `src/base/cics/cobol/INQACC.cbl:646`, `src/base/cics/cobol/INQACC.cbl:687` | C with SUCCESS=Y; enter installed handler with AFCR before output copy; rollback NORMAL | One caller-unit-of-work SYNCPOINT ROLLBACK; Unchanged(N); RETURN; no diagnostic LINK or re-abend | Tests success flag actually changes; handler fixture, not runtime reachability proof. |
| INQACC-T40 | INQACC-R08 | `src/base/cics/cobol/INQACC.cbl:646`, `src/base/cics/cobol/INQACC.cbl:687` | C with SUCCESS=Y; handler AFCS; rollback NORMAL | One rollback; Unchanged(N); RETURN; no diagnostic LINK or re-abend | Test each grouped WHEN, even if rarely reached. |
| INQACC-T41 | INQACC-R08 | `src/base/cics/cobol/INQACC.cbl:646`, `src/base/cics/cobol/INQACC.cbl:687` | C with SUCCESS=Y; handler AFCT; rollback NORMAL | One rollback; Unchanged(N); RETURN; no diagnostic LINK or re-abend | Do not clear balances or retry inquiry. |
| INQACC-T42 | INQACC-R08 | `src/base/cics/cobol/INQACC.cbl:646`, `src/base/cics/cobol/INQACC.cbl:687` | C with SUCCESS=Y; handler AFCR; rollback response non-NORMAL with distinguishable RESP/RESP2 | One rollback; diagnostic LINK with HROL and SQLCODE 0; HROL NODUMP CANCEL; no normal return; no N assignment | Preserve response/context fields and AH010 rollback-failure message (`src/base/cics/cobol/INQACC.cbl:695`, `src/base/cics/cobol/INQACC.cbl:724`). |
| INQACC-T43 | INQACC-R08 | `src/base/cics/cobol/INQACC.cbl:646`, `src/base/cics/cobol/INQACC.cbl:687` | C with SUCCESS=Y; handler AFCS; rollback non-NORMAL | Same HROL failure outcome as T42; one rollback only | Distinct grouped abend code; no rollback retry/exhaustion loop. |
| INQACC-T44 | INQACC-R08 | `src/base/cics/cobol/INQACC.cbl:646`, `src/base/cics/cobol/INQACC.cbl:687` | C with SUCCESS=Y; handler AFCT; rollback non-NORMAL | Same HROL failure outcome as T42; one rollback only | Do not fall through to reissue AFCT or return N. |
| INQACC-T45 | INQACC-R08 | `src/base/cics/cobol/INQACC.cbl:646`, `src/base/cics/cobol/INQACC.cbl:763` | C; handler AD2Z; SQLCODE -911, host account 00000042 and distinguishable SQLSTATE/SQLERRMC/SQLERRD(1..6) | Display supplied deadlock SQL diagnostics; LINK with AD2Z and ABND-SQLCODE 0; re-abend AD2Z NODUMP CANCEL; no explicit rollback, retry or normal result | Displayed SQLCODE differs from recorded zero (`src/base/cics/cobol/INQACC.cbl:653`, `src/base/cics/cobol/INQACC.cbl:799`). |
| INQACC-T46 | INQACC-R08 | `src/base/cics/cobol/INQACC.cbl:646`, `src/base/cics/cobol/INQACC.cbl:763` | C; handler code ASRA | No AD2Z SQL display or storm diagnostic; LINK with ASRA and ABND-SQLCODE 0; re-abend ASRA NODUMP CANCEL; no explicit rollback/retry or normal result | EVALUATE has no matching WHEN; final N guard is true. Preserve AH010 freeform text (`src/base/cics/cobol/INQACC.cbl:801`). |
| INQACC-T47 | INQACC-R04, R05 | `src/base/cics/cobol/INQACC.cbl:229` | C; R type ` X      ` | Ordinary; Populated(R) with exact type bytes | Only all-spaces/all-low-values suppress copying; no trim-based emptiness or type normalization. |
| INQACC-T48 | INQACC-R05 | `src/base/cics/cobol/INQACC.cbl:229` | C; R opened 2024-02-29, last 2026-12-31, next 2027-01-01 | Ordinary; opened 29022024, last 31122026, next 01012027; other fields Populated(R) | Leap-day/year-boundary fixtures verify field ordering, not a new date computation. |

## Decision and rule coverage

**48 specified tests; all 8 rules mapped; 11/11 digest decision points and
12/12 source-observed decision points covered at least once.** These are
specification counts, not executed or passing-test claims.

| Decision point | Arms / test evidence |
|---|---|
| `src/base/cics/cobol/INQACC.cbl:218` | False/ordinary T01, T13-T14; true/highest T02, T15, T30. |
| `src/base/cics/cobol/INQACC.cbl:229` | True/spaces T03-T04, T06; true/low-values T05, T07; false T01-T02, T08-T15, T47-T48. |
| `src/base/cics/cobol/INQACC.cbl:273` | False/OPEN 0 T01; true/nonzero T16, T20-T21, T33. |
| `src/base/cics/cobol/INQACC.cbl:353` | False/CLOSE 0 T01, T03; true/nonzero T18-T19, T24-T25, T34. |
| `src/base/cics/cobol/INQACC.cbl:450` | True/+100 T03, T19; false/0 T01 and false/other errors T17, T22-T23, T26-T27, T35, T37. |
| `src/base/cics/cobol/INQACC.cbl:460` | False/0 T01; true/nonzero except +100 T17, T22-T23, T26-T27, T35, T37. +100 exits before this decision (T03). |
| `src/base/cics/cobol/INQACC.cbl:604` | WHEN 923 T20, T22, T24; WHEN OTHER T16, T21, T23, T25. |
| `src/base/cics/cobol/INQACC.cbl:616` | True/display T20, T22, T24; false/no display T16, T21, T23, T25. |
| `src/base/cics/cobol/INQACC.cbl:646` | AD2Z T45; AFCR T39/T42; AFCS T40/T43; AFCT T41/T44; no matching WHEN T46. |
| `src/base/cics/cobol/INQACC.cbl:763` | True T45-T46; false-arm waiver W01 below. |
| `src/base/cics/cobol/INQACC.cbl:874` | False/0 T02; true/+100 T30; true/negative T28-T29, T31, T38; true/positive other T32, T36. |
| `src/base/cics/cobol/INQACC.cbl:687` | Source-only, unverified by tooling: false/NORMAL T39-T41; true/non-NORMAL T42-T44. |

Rule-to-test index: R01 T01/T13-T14; R02 T02/T06-T07/T11/T15/T28-T32/T36/T38;
R03 T03/T19; R04 T04-T08/T47; R05 T01-T02/T08-T15/T47-T48;
R06 T16-T27/T33-T35/T37; R07 T16-T18/T20-T25/T32; R08 T39-T46.

**W01 - No normal-entry test for the false arm at line 763.** WS-STORM-DRAIN
starts N (`src/base/cics/cobol/INQACC.cbl:134`). Its only Y assignment is in the
AFCR/AFCS/AFCT arm (`src/base/cics/cobol/INQACC.cbl:677`), which terminates in
HROL CANCEL on rollback failure or RETURN on normal rollback
(`src/base/cics/cobol/INQACC.cbl:748`,
`src/base/cics/cobol/INQACC.cbl:758`). Neither outcome reaches line 763.
AD2Z and unmatched-code paths reach that guard with N (T45-T46).
The false arm therefore has no source-supported normal-entry fixture; waiving
that arm avoids inventing mutable internal-state inputs. This is not a waiver
of AFCR/AFCS/AFCT, rollback or their conditional reachability: all six outcomes
are specified in T39-T44, including the tooling-gap IF.

There is **no deadlock retry-exhaustion branch in INQACC**: supplied -911/-913
SQL outcomes terminate through the ordinary error arms or highest SELECT
failure, and AD2Z supplies diagnostics then re-abends
(`src/base/cics/cobol/INQACC.cbl:460`,
`src/base/cics/cobol/INQACC.cbl:651`,
`src/base/cics/cobol/INQACC.cbl:814`,
`src/base/cics/cobol/INQACC.cbl:874`). T26-T29/T45 explicitly require zero
retries rather than importing a different module's loop. Rollback tests assert
the caller-unit-of-work request, not just unchanged ACCOUNT data; no new
commit, database write or retry policy is specified.

**Questions raised: 0.** Historical questions are retained, not silently
rewritten. The source-only parser gap and W01 remain explicit qualifications;
deployed REST contracts, operational gates and live parity are outside this
test-authoring result (`docs/modernization/03a-architecture/target-architecture.md:55`).
