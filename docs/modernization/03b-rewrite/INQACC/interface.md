# INQACC interface contract

**Status: incomplete at the REST boundary; stopped on INQACC-Q07**
(`docs/modernization/01-rediscovery/open-questions.md:966`).
The source-level sections below are evidence-backed, not an approved
substitutable HTTP contract. No implementation or routing change was made.

## Scope and authority

This is the source-level contract for one module, not an implementation or
cutover approval. Observable source behavior, including quirks, is accepted by
the domain reviewer; the final Q06 answer supersedes Q03 for an empty
highest-account lookup
(`docs/modernization/01-rediscovery/open-questions.md:907`,
`docs/modernization/01-rediscovery/open-questions.md:964`).
The acceptance baseline is INQACC-R01 through R08 and tests T01 through T48
(`docs/modernization/01-rediscovery/business-rules.md:86`,
`docs/modernization/03b-rewrite/INQACC/acceptance-tests.md:128`).

INQACC is parsed successfully. Its rollback-response IF remains **unverified by
tooling** and is included from original source, as accepted in Q02
(`docs/modernization/00-source-of-truth/programs/INQACC.md:10`,
`src/base/cics/cobol/INQACC.cbl:687`,
`docs/modernization/01-rediscovery/open-questions.md:916`).

## Entry points

| Surface | Exact entry and retained behavior | Evidence |
|---|---|---|
| CICS program | `INQACC`, synchronous `LINK` with a mutable COMMAREA; procedure uses `DFHCOMMAREA`. | `src/base/cics/cobol/INQACC.cbl:19`, `src/base/cics/cobol/INQACC.cbl:202` |
| Display/delete screen caller | `BNK1DAC`, transaction `ODAC`; LINK passes `INQACC-COMMAREA`, RESP/RESP2 and `SYNCONRETURN`. Not switched in this wave. | `docs/modernization/00-source-of-truth/call-graph.md:249`, `src/base/cics/cobol/BNK1DAC.cbl:554`, `docs/modernization/01-rediscovery/integrations.md:26` |
| Update screen caller | `BNK1UAC`, transaction `OUAC`; LINK passes `DFHCOMMAREA`, RESP/RESP2 and `SYNCONRETURN`. Not switched in this wave. | `docs/modernization/00-source-of-truth/call-graph.md:287`, `src/base/cics/cobol/BNK1UAC.cbl:767`, `docs/modernization/01-rediscovery/integrations.md:29` |
| Account-detail REST operation | `getAccount`: GET `/api/accounts/{accountId}`, asset `INQACC`. | `src/api/src/main/api/openapi.yaml:25`, `src/api/src/main/api/openapi.yaml:479`, `src/api/src/main/operations/%2Faccounts%2F%7BaccountId%7D/get/operation.yaml:3` |
| Balance REST operation | `getAccountBalances`: GET `/api/accounts/{accountId}/balances`, same asset `INQACC`. | `src/api/src/main/api/openapi.yaml:576`, `src/api/src/main/operations/%2Faccounts%2F%7BaccountId%7D%2Fbalances/get/operation.yaml:3` |

The provider selects program `INQACC`, COBOL, `bankzCicsConnection`, CCSID `037`,
and `OMEN` with `transidUsage: EIB_ONLY`. That metadata does not dispatch the
request through BNKMENU or create an INQACC terminal transaction
(`src/api/src/main/zosAssets/INQACC/zosAsset.yaml:5`,
`docs/modernization/01-rediscovery/integrations.md:366`).

## Input and output shape

One in/out record, not separate request and response layouts. Start with the
digest's LINKAGE table; the authoritative base copybook is copied with
`INQACC-COMMAREA` replaced by `DFHCOMMAREA`
(`docs/modernization/00-source-of-truth/programs/INQACC.md:222`,
`src/base/cics/cobol/INQACC.cbl:198`).

In the table, **copy** means overwritten from the retrieved row only when its
account type is neither all spaces nor all low-values. Otherwise the incoming
account fields remain unchanged and only success becomes Y. The input record is
not initialized by INQACC; only its internal OUTPUT-DATA is initialized
(`src/base/cics/cobol/INQACC.cbl:206`,
`src/base/cics/cobol/INQACC.cbl:229`).

| Level / field, in storage order | Exact clause | Input role / output rule | Definition |
|---|---|---|---|
| 01 INQACC-COMMAREA / DFHCOMMAREA | group | Shared in/out record. | `src/base/cics/copy/INQACC.cpy:7` |
| 03 INQACC-EYE | `PIC X(4)` | Not a selector; copy eyecatcher. | `src/base/cics/copy/INQACC.cpy:8` |
| 03 INQACC-CUSTNO | `PIC 9(10)` | Not a selector; copy customer number. | `src/base/cics/copy/INQACC.cpy:9` |
| 03 INQACC-SCODE | `PIC 9(6)` | Not a selector; copy retrieved sort code. | `src/base/cics/copy/INQACC.cpy:10` |
| 03 INQACC-ACCNO | `PIC 9(8)` | Lookup selector; 99999999 selects highest-account mode; copy returned number. | `src/base/cics/copy/INQACC.cpy:11` |
| 03 INQACC-ACC-TYPE | `PIC X(8)` | Not a selector; copy type without adding validation. | `src/base/cics/copy/INQACC.cpy:12` |
| 03 INQACC-INT-RATE | `PIC 9(4)V99` | Copy exact unsigned decimal, scale 2. | `src/base/cics/copy/INQACC.cpy:13` |
| 03 INQACC-OPENED | `PIC 9(8)` | Copy DDMMYYYY date. | `src/base/cics/copy/INQACC.cpy:14` |
| 03 INQACC-OPENED-GROUP | `REDEFINES INQACC-OPENED` | Overlay, not additional storage. | `src/base/cics/copy/INQACC.cpy:15` |
| 05 INQACC-OPENED-DAY | `PIC 99` | Day overlay. | `src/base/cics/copy/INQACC.cpy:16` |
| 05 INQACC-OPENED-MONTH | `PIC 99` | Month overlay. | `src/base/cics/copy/INQACC.cpy:17` |
| 05 INQACC-OPENED-YEAR | `PIC 9999` | Year overlay. | `src/base/cics/copy/INQACC.cpy:18` |
| 03 INQACC-OVERDRAFT | `PIC 9(8)` | Copy unsigned whole-unit limit. | `src/base/cics/copy/INQACC.cpy:19` |
| 03 INQACC-LAST-STMT-DT | `PIC 9(8)` | Copy DDMMYYYY date. | `src/base/cics/copy/INQACC.cpy:20` |
| 03 INQACC-LAST-STMT-GROUP | `REDEFINES INQACC-LAST-STMT-DT` | Overlay, not additional storage. | `src/base/cics/copy/INQACC.cpy:21` |
| 05 INQACC-LAST-STMT-DAY | `PIC 99` | Day overlay. | `src/base/cics/copy/INQACC.cpy:22` |
| 05 INQACC-LAST-STMT-MONTH | `PIC 99` | Month overlay. | `src/base/cics/copy/INQACC.cpy:23` |
| 05 INQACC-LAST-STMT-YEAR | `PIC 9999` | Year overlay. | `src/base/cics/copy/INQACC.cpy:24` |
| 03 INQACC-NEXT-STMT-DT | `PIC 9(8)` | Copy DDMMYYYY date. | `src/base/cics/copy/INQACC.cpy:25` |
| 03 INQACC-NEXT-STMT-GROUP | `REDEFINES INQACC-NEXT-STMT-DT` | Overlay, not additional storage. | `src/base/cics/copy/INQACC.cpy:26` |
| 05 INQACC-NEXT-STMT-DAY | `PIC 99` | Day overlay. | `src/base/cics/copy/INQACC.cpy:27` |
| 05 INQACC-NEXT-STMT-MONTH | `PIC 99` | Month overlay. | `src/base/cics/copy/INQACC.cpy:28` |
| 05 INQACC-NEXT-STMT-YEAR | `PIC 9999` | Year overlay. | `src/base/cics/copy/INQACC.cpy:29` |
| 03 INQACC-AVAIL-BAL | `PIC S9(10)V99` | Copy exact signed balance, scale 2. | `src/base/cics/copy/INQACC.cpy:30` |
| 03 INQACC-ACTUAL-BAL | `PIC S9(10)V99` | Copy exact signed balance, scale 2. | `src/base/cics/copy/INQACC.cpy:31` |
| 03 INQACC-SUCCESS | `PIC X` | Incoming value not consulted; Y or handler N on normal return. | `src/base/cics/copy/INQACC.cpy:32` |
| 03 INQACC-PCB1-POINTER | `POINTER` | Opaque caller value, unchanged; not a lookup input. | `src/base/cics/copy/INQACC.cpy:33` |

All PIC fields above use default DISPLAY, not COMP or COMP-3; V is an implied
decimal point. Preserve widths, leading zeroes, spaces, low-values and signed
decimal representation at the COBOL boundary. Balances have representable range
-9999999999.99 through +9999999999.99; interest is 0 through 9999.99.
Use exact fixed-point values, never floating point. Host balances and interest
are packed decimal, unlike the interface, and the overdraft host is binary
(`src/base/cics/cobol/INQACC.cbl:46`,
`src/base/cics/copy/INQACC.cpy:13`,
`src/base/cics/copy/INQACC.cpy:30`).

The checked-in provider layout describes 103 bytes, including a four-byte
pointer representation; this is provider metadata, not permission to change
the authoritative `POINTER` declaration or dereference/transport a native pointer
as an application identifier
(`src/api/src/main/zosAssets/INQACC/providerFiles/request.dai:7`,
`src/api/src/main/zosAssets/INQACC/providerFiles/gen/requestSchema.json:227`,
`src/base/cics/copy/INQACC.cpy:33`).

Ordinary lookup uses configured SORTCODE and input account number only, with one
OPEN/FETCH/CLOSE. Highest mode selects one row at configured sort code ordered
by ACCOUNT_NUMBER descending; it does not mean latest opened date. Incoming
customer, sort code and type do not restrict either query
(`src/base/cics/cobol/INQACC.cbl:80`,
`src/base/cics/cobol/INQACC.cbl:263`,
`src/base/cics/cobol/INQACC.cbl:345`,
`src/base/cics/cobol/INQACC.cbl:843`).
Dates are rearranged from DB2 year-month-day into the overlays; there is no
new calendar calculation, rounding, NULL default or domain validation
(`src/base/cics/cobol/INQACC.cbl:431`,
`src/base/cics/cobol/INQACC.cbl:546`,
`src/base/cics/cobol/INQACC.cbl:953`,
`docs/modernization/01-rediscovery/open-questions.md:920`).

## Error and return semantics

There is no fail-code field or numeric return-code field in this COMMAREA.
Do not introduce COMM-FAIL-CODE or infer not-found from Y
(`src/base/cics/copy/INQACC.cpy:7`).

| Outcome | Exact result and diagnostic behavior | Evidence |
|---|---|---|
| Row with nonblank/non-low-value type | Copy all account fields; SUCCESS=Y; RETURN. Pointer unchanged. | `src/base/cics/cobol/INQACC.cbl:229`, `src/base/cics/cobol/INQACC.cbl:584` |
| Ordinary FETCH +100, then CLOSE 0 | All incoming account fields remain unchanged; SUCCESS=Y; RETURN. Internal initialized record is not copied out. | `src/base/cics/cobol/INQACC.cbl:450`, `src/base/cics/cobol/INQACC.cbl:353`, `src/base/cics/cobol/INQACC.cbl:229` |
| Retrieved type all spaces or all low-values, either query | Same unchanged-fields Y result, including retention of input 99999999 in highest mode. No fallback to a different row. | `src/base/cics/cobol/INQACC.cbl:218`, `src/base/cics/cobol/INQACC.cbl:229` |
| OPEN/CLOSE nonzero SQLCODE, or FETCH nonzero except +100 | Diagnostic LINK to ABNDPROC with HRAC and SQLCODE, then HRAC `CANCEL NODUMP`; no normal result. Positive nonzero codes also fail. | `src/base/cics/cobol/INQACC.cbl:273`, `src/base/cics/cobol/INQACC.cbl:353`, `src/base/cics/cobol/INQACC.cbl:460` |
| Highest-account SELECT nonzero SQLCODE | Diagnostic LINK with HNCS and SQLCODE, then HNCS `NODUMP CANCEL`, including empty result +100. | `src/base/cics/cobol/INQACC.cbl:874`, `src/base/cics/cobol/INQACC.cbl:924`, `src/base/cics/cobol/INQACC.cbl:932` |
| Handler receives AFCR / AFCS / AFCT; rollback NORMAL | Display storm diagnostic; SYNCPOINT ROLLBACK; SUCCESS=N; RETURN. No diagnostic LINK and no clearing of other COMMAREA fields. | `src/base/cics/cobol/INQACC.cbl:672`, `src/base/cics/cobol/INQACC.cbl:682`, `src/base/cics/cobol/INQACC.cbl:756` |
| Same handler codes; rollback not NORMAL | After rollback attempt, diagnostic LINK with HROL and recorded SQLCODE 0; display failure; HROL `NODUMP CANCEL`. Does not reach N assignment. | `src/base/cics/cobol/INQACC.cbl:687`, `src/base/cics/cobol/INQACC.cbl:719`, `src/base/cics/cobol/INQACC.cbl:738`, `src/base/cics/cobol/INQACC.cbl:748` |
| Handler receives AD2Z | Display deadlock/account/SQL diagnostics, then LINK recording AD2Z with SQLCODE 0 and re-abend AD2Z `NODUMP CANCEL`. | `src/base/cics/cobol/INQACC.cbl:651`, `src/base/cics/cobol/INQACC.cbl:763`, `src/base/cics/cobol/INQACC.cbl:799`, `src/base/cics/cobol/INQACC.cbl:814` |
| Handler receives any other abend | With initial storm flag N, LINK recording the received four-character code and SQLCODE 0, then reissue that code `NODUMP CANCEL`. No finite invented list of possible runtime abends. | `src/base/cics/cobol/INQACC.cbl:134`, `src/base/cics/cobol/INQACC.cbl:635`, `src/base/cics/cobol/INQACC.cbl:763` |

The DB2 storm check matches **positive 923 only**, displays the connection-lost
condition, and neither rolls back nor converts the failure to N. Negative -923
is not that condition. There is no SQL retry loop, including for -911/-913;
the highest-account failure does not call this check
(`src/base/cics/cobol/INQACC.cbl:604`,
`src/base/cics/cobol/INQACC.cbl:616`,
`src/base/cics/cobol/INQACC.cbl:874`).
Explicit abends above use CANCEL; do not model them as a second invocation
of the installed handler or fabricate a normal failure response
(`src/base/cics/cobol/INQACC.cbl:335`,
`src/base/cics/cobol/INQACC.cbl:814`).

## Ordered side effects and unit of work

| Path | Observable order |
|---|---|
| Ordinary success or FETCH +100 | OPEN -> FETCH -> CLOSE -> conditional output copy -> Y -> RETURN (`src/base/cics/cobol/INQACC.cbl:270`, `src/base/cics/cobol/INQACC.cbl:345`, `src/base/cics/cobol/INQACC.cbl:350`, `src/base/cics/cobol/INQACC.cbl:229`). |
| Highest success | SELECT -> internal row mapping -> conditional output copy -> Y -> RETURN (`src/base/cics/cobol/INQACC.cbl:843`, `src/base/cics/cobol/INQACC.cbl:940`, `src/base/cics/cobol/INQACC.cbl:229`). |
| OPEN or CLOSE error | Construct diagnostic -> LINK ABNDPROC -> DISPLAY error -> DB2 storm check/display -> HRAC. OPEN error does not FETCH/CLOSE (`src/base/cics/cobol/INQACC.cbl:281`, `src/base/cics/cobol/INQACC.cbl:321`, `src/base/cics/cobol/INQACC.cbl:401`, `src/base/cics/cobol/INQACC.cbl:415`). |
| FETCH error other than +100 | DB2 storm check/display -> construct diagnostic -> LINK ABNDPROC -> DISPLAY error -> HRAC; no CLOSE (`src/base/cics/cobol/INQACC.cbl:466`, `src/base/cics/cobol/INQACC.cbl:476`, `src/base/cics/cobol/INQACC.cbl:516`, `src/base/cics/cobol/INQACC.cbl:524`). |
| Highest SELECT error | Construct diagnostic -> LINK ABNDPROC -> DISPLAY error -> HNCS (`src/base/cics/cobol/INQACC.cbl:883`, `src/base/cics/cobol/INQACC.cbl:924`, `src/base/cics/cobol/INQACC.cbl:928`, `src/base/cics/cobol/INQACC.cbl:932`). |
| AFCR/AFCS/AFCT handler | Storm DISPLAY -> rollback attempt -> either N/RETURN or diagnostic LINK/DISPLAY/HROL after failed rollback (`src/base/cics/cobol/INQACC.cbl:678`, `src/base/cics/cobol/INQACC.cbl:682`, `src/base/cics/cobol/INQACC.cbl:687`). |
| Other handler | AD2Z-specific DISPLAYs if applicable -> construct diagnostic -> LINK ABNDPROC -> re-abend received code (`src/base/cics/cobol/INQACC.cbl:651`, `src/base/cics/cobol/INQACC.cbl:763`, `src/base/cics/cobol/INQACC.cbl:810`). |

Only ACCOUNT is queried; no DB2 row mutation, PROCTRAN audit/history insert,
IMS call, queue or banking message is issued by INQACC. Diagnostics are the
file-writing dependency, not PROCTRAN business history
(`docs/modernization/00-source-of-truth/programs/INQACC.md:118`,
`docs/modernization/00-source-of-truth/data-access.md:9`,
`src/base/cics/cobol/ABNDPROC.cbl:141`).

Each diagnostic record contains time/task key, APPLID, TRANID, date/time,
abend code, program, EIBRESP/EIBRESP2, SQLCODE and the site's exact freeform
text. Preserve the source text, including spelling and recorded-versus-displayed
SQLCODE differences; do not replace it with a generic exception
(`src/base/cics/copy/ABNDINFO.cpy:7`,
`src/base/cics/cobol/INQACC.cbl:281`,
`src/base/cics/cobol/INQACC.cbl:312`,
`src/base/cics/cobol/INQACC.cbl:392`,
`src/base/cics/cobol/INQACC.cbl:507`,
`src/base/cics/cobol/INQACC.cbl:724`,
`src/base/cics/cobol/INQACC.cbl:801`,
`src/base/cics/cobol/INQACC.cbl:914`,
`src/base/cics/cobol/INQACC.cbl:990`).
ABNDPROC copies the record and attempts WRITE ABNDFILE; WRITE failure displays
RESP/RESP2 and returns. A LINK attempt is not guaranteed persistence
(`src/base/cics/cobol/ABNDPROC.cbl:139`,
`src/base/cics/cobol/ABNDPROC.cbl:149`).

INQACC issues no successful SYNCPOINT/commit. Its only explicit SYNCPOINT is
the AFCR/AFCS/AFCT rollback, before any HROL diagnostic. Preserve the current
CICS unit-of-work scope, including caller work participating in it; do not
replace that with an isolated inquiry-only rollback. The two screen calls
specify SYNCONRETURN, so their invocation attributes must also be retained,
not reinterpreted as a new independent transaction policy
(`docs/modernization/00-source-of-truth/programs/INQACC.md:118`,
`src/base/cics/cobol/INQACC.cbl:682`,
`src/base/cics/cobol/BNK1DAC.cbl:554`,
`src/base/cics/cobol/BNK1UAC.cbl:767`).
Diagnostic LINKs have no separate explicit syncpoint; the checked-in ABNDFILE
definition has `recovery: NONE`. Do not promise atomic rollback of diagnostic
records with banking data, or infer deployed recovery settings from this file
(`src/base/cics/cobol/INQACC.cbl:738`,
`.setup/zconfig/bank-of-z-definitions.yaml:38`,
`docs/modernization/01-rediscovery/open-questions.md:572`).

## REST projections and switch point

Both GET request mappings assign only path accountId to INQACC-ACCNO; there
is no prefix stripping or caller-supplied sort-code/type mapping
(`src/api/src/main/operations/%2Faccounts%2F%7BaccountId%7D/get/request.yaml:10`,
`src/api/src/main/operations/%2Faccounts%2F%7BaccountId%7D%2Fbalances/get/request.yaml:10`).

Detail output echoes the request accountId, projects type/number/sort code/
customer/interest/overdraft/balances, constructs openingDate from the returned
date overlay, and supplies constants GBP and ACTIVE. In highest mode the echoed
accountId can therefore differ from returned accountNumber
(`src/api/src/main/operations/%2Faccounts%2F%7BaccountId%7D/get/response_200.yaml:6`).
Balance output is a single object, not a balance list: AVAILABLE, returned
available balance, GBP, DEBIT if negative otherwise CREDIT, and last-statement
date at `T00:00:00Z`, not inquiry execution time
(`src/api/src/main/operations/%2Faccounts%2F%7BaccountId%7D%2Fbalances/get/response_200.yaml:6`).

The checked-in response selectors use COMMAREA-exists **or** abendCode-absent
for 200, with a final true condition for 500; neither tests INQACC-SUCCESS.
Do not invent a missing-account 404 or map N to a new status
(`src/api/src/main/operations/%2Faccounts%2F%7BaccountId%7D/get/response_mapping.yaml:3`,
`src/api/src/main/operations/%2Faccounts%2F%7BaccountId%7D%2Fbalances/get/response_mapping.yaml:3`).
The following wire-contract gaps are recorded in INQACC-Q07; this source-level
record must not be used as a complete substitutable HTTP contract.

**Selected switch:** ADR-002/003/007 place the strangler router in front of
z/OS Connect at method/path plus an owner-approved caller cohort. Default and
route-back remain the existing mappings; only the two GETs above are eligible.
No CICS program rename/router, transaction reassignment, shared-caller switch
or provider retirement is authorized
(`docs/modernization/03a-architecture/target-architecture.md:51`,
`docs/modernization/03a-architecture/target-architecture.md:83`,
`docs/modernization/03a-architecture/target-architecture.md:226`).

Replacement routes remain disabled. Before activation, accept both projections,
module parity, identity/attribution, read-path consistency, diagnostics/recovery
and operating responsibility; select cohort sizes and numerical operational
thresholds and rehearse route-back. Route each request once; no silent fallback,
cross-destination replay or shadow duplication of rollback/diagnostic effects.
Route-back affects subsequent requests, not completed effects
(`docs/modernization/03a-architecture/target-architecture.md:225`,
`docs/modernization/03a-architecture/target-architecture.md:227`).
The selected implementation runtime is Java/Spring Boot in a Linux container,
but this document approves no data-access mechanism or transaction-manager
substitution; relevant audit verdicts remain blocked
(`docs/modernization/03a-architecture/target-architecture.md:5`,
`docs/modernization/03a-architecture/target-architecture.md:54`,
`docs/modernization/02-substitution-audit/audit.md:24`,
`docs/modernization/02-substitution-audit/audit.md:35`).
