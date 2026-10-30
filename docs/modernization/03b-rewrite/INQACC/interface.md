# INQACC interface contract

**Status: interface definition complete under the INQACC-Q07 domain answer;
slices 1 and 2 implemented locally; full implementation and cutover remain gated**
(`docs/modernization/01-rediscovery/open-questions.md:1039`,
`docs/modernization/03a-architecture/target-architecture.md:225`).
The checked-in mappings are authoritative, including placeholders. Accepted
undefined behavior is recorded below, not filled in. Slice evidence is in
`docs/modernization/03b-rewrite/INQACC/parity.md:1`. No routing change was made.

## Scope and authority

This is the source and REST contract for one module, not an implementation or
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

## REST input and output projections

The two GETs consume a required string path parameter `accountId`, have no
mapped request body, and declare `application/json` success responses
(`src/api/src/main/api/openapi.yaml:480`,
`src/api/src/main/api/openapi.yaml:491`,
`src/api/src/main/api/openapi.yaml:577`,
`src/api/src/main/api/openapi.yaml:588`).

Both GET request mappings assign only path accountId to INQACC-ACCNO; there
is no prefix stripping or caller-supplied sort-code/type mapping
(`src/api/src/main/operations/%2Faccounts%2F%7BaccountId%7D/get/request.yaml:10`,
`src/api/src/main/operations/%2Faccounts%2F%7BaccountId%7D%2Fbalances/get/request.yaml:10`).
Both the COMMAREA group and mapped ACCNO have `required: false`,
`nullable: false`; preserve these mapping attributes, distinct from the
required HTTP path parameter
(`src/api/src/main/operations/%2Faccounts%2F%7BaccountId%7D/get/request.yaml:6`,
`src/api/src/main/operations/%2Faccounts%2F%7BaccountId%7D%2Fbalances/get/request.yaml:6`).

In the tables, field names after `INQACC-` refer to the returned COMMAREA,
**template** retains the mapping's string interpolation and **expression**
retains its numeric value. Required/nullable are the literal mapping attributes,
not new validation rules. Monetary and interest expressions use exact decimal
JSON numbers; the OpenAPI `double` label is an accepted documentation defect,
not permission for binary floating point
(`src/api/src/main/operations/%2Faccounts%2F%7BaccountId%7D/get/response_200.yaml:6`,
`src/api/src/main/operations/%2Faccounts%2F%7BaccountId%7D%2Fbalances/get/response_200.yaml:6`,
`docs/modernization/01-rediscovery/open-questions.md:1039`).

### getAccount - 200 JSON object

| Property | Literal projection | Required / nullable | Evidence |
|---|---|---|---|
| accountId | Template: original request path `accountId`, not returned ACCNO. | true / false | `src/api/src/main/operations/%2Faccounts%2F%7BaccountId%7D/get/response_200.yaml:6` |
| accountType | Template: INQACC-ACC-TYPE; no enum validation. | true / false | `src/api/src/main/operations/%2Faccounts%2F%7BaccountId%7D/get/response_200.yaml:10`, `docs/modernization/01-rediscovery/open-questions.md:1039` |
| currency | Template constant: `GBP`. | true / false | `src/api/src/main/operations/%2Faccounts%2F%7BaccountId%7D/get/response_200.yaml:14` |
| accountNumber | Template: INQACC-ACCNO. | false / false | `src/api/src/main/operations/%2Faccounts%2F%7BaccountId%7D/get/response_200.yaml:18` |
| sortCode | Template: INQACC-SCODE; no inserted hyphens. | false / false | `src/api/src/main/operations/%2Faccounts%2F%7BaccountId%7D/get/response_200.yaml:22` |
| status | Template constant: `ACTIVE`, not derived account state. | true / false | `src/api/src/main/operations/%2Faccounts%2F%7BaccountId%7D/get/response_200.yaml:26` |
| openingDate | Template: INQACC-OPENED-GROUP YEAR + `-` + MONTH + `-` + DAY, in that order. | false / false | `src/api/src/main/operations/%2Faccounts%2F%7BaccountId%7D/get/response_200.yaml:30` |
| customerId | Template: INQACC-CUSTNO; no added prefix. | false / false | `src/api/src/main/operations/%2Faccounts%2F%7BaccountId%7D/get/response_200.yaml:34` |
| interestRate | Expression: INQACC-INT-RATE, exact decimal scale 2. | false / true | `src/api/src/main/operations/%2Faccounts%2F%7BaccountId%7D/get/response_200.yaml:38`, `src/base/cics/copy/INQACC.cpy:13` |
| overdraftLimit | Expression: INQACC-OVERDRAFT, unsigned whole-unit integer. | false / true | `src/api/src/main/operations/%2Faccounts%2F%7BaccountId%7D/get/response_200.yaml:42`, `src/base/cics/copy/INQACC.cpy:19` |
| availableBalance | Expression: INQACC-AVAIL-BAL, signed exact decimal scale 2. | false / true | `src/api/src/main/operations/%2Faccounts%2F%7BaccountId%7D/get/response_200.yaml:46`, `src/base/cics/copy/INQACC.cpy:30` |
| actualBalance | Expression: INQACC-ACTUAL-BAL, signed exact decimal scale 2. | false / true | `src/api/src/main/operations/%2Faccounts%2F%7BaccountId%7D/get/response_200.yaml:50`, `src/base/cics/copy/INQACC.cpy:31` |

These are the mapped properties, not the entire optional OpenAPI Account
catalog; do not synthesize accountSubType, nickname or iban. Highest-account
mode can return an accountNumber different from the echoed `99999999` accountId.
Neither INQACC-SUCCESS nor the opaque pointer is exposed in this projection
(`src/api/src/main/operations/%2Faccounts%2F%7BaccountId%7D/get/response_200.yaml:6`,
`src/api/src/main/api/openapi.yaml:1139`,
`src/base/cics/cobol/INQACC.cbl:218`).

### getAccountBalances - 200 JSON object

| Property | Literal projection | Required / nullable | Evidence |
|---|---|---|---|
| balanceType | Template constant: `AVAILABLE`. | true / false | `src/api/src/main/operations/%2Faccounts%2F%7BaccountId%7D%2Fbalances/get/response_200.yaml:6` |
| amount | Expression: INQACC-AVAIL-BAL, signed exact decimal scale 2; retain the sign, not an absolute value. | true / false | `src/api/src/main/operations/%2Faccounts%2F%7BaccountId%7D%2Fbalances/get/response_200.yaml:10`, `src/base/cics/copy/INQACC.cpy:30` |
| currency | Template constant: `GBP`. | true / false | `src/api/src/main/operations/%2Faccounts%2F%7BaccountId%7D%2Fbalances/get/response_200.yaml:14` |
| creditDebitIndicator | Template: `DEBIT` if INQACC-AVAIL-BAL < 0, otherwise `CREDIT` (including zero). | false / false | `src/api/src/main/operations/%2Faccounts%2F%7BaccountId%7D%2Fbalances/get/response_200.yaml:18` |
| dateTime | Template: INQACC-LAST-STMT-GROUP YEAR + `-` + MONTH + `-` + DAY + `T00:00:00Z`. Last statement date, not execution time. | true / false | `src/api/src/main/operations/%2Faccounts%2F%7BaccountId%7D%2Fbalances/get/response_200.yaml:23` |

This is one Balance object, not an array or BalanceList wrapper; it does not
project actual balance, accountId, INQACC-SUCCESS or the pointer
(`src/api/src/main/api/openapi.yaml:590`,
`src/api/src/main/operations/%2Faccounts%2F%7BaccountId%7D%2Fbalances/get/response_200.yaml:4`).

## REST status and error semantics

The checked-in response selectors use COMMAREA-exists **or** abendCode-absent
for 200, with a final true condition for 500; neither tests INQACC-SUCCESS.
Do not invent a missing-account 404 or map N to a new status
(`src/api/src/main/operations/%2Faccounts%2F%7BaccountId%7D/get/response_mapping.yaml:3`,
`src/api/src/main/operations/%2Faccounts%2F%7BaccountId%7D%2Fbalances/get/response_mapping.yaml:3`).
Retain the selector order and predicates literally: existence of a COMMAREA
satisfies the 200 predicate even if abendCode exists; with no COMMAREA and no
abendCode the predicate also holds. There is no separate per-abend HTTP mapping
(`src/api/src/main/operations/%2Faccounts%2F%7BaccountId%7D/get/response_mapping.yaml:4`,
`src/api/src/main/operations/%2Faccounts%2F%7BaccountId%7D%2Fbalances/get/response_mapping.yaml:4`).

| Status | Both operations' checked-in mapping contract | Evidence |
|---|---|---|
| 200 | Predicate above; operation-specific body defined above. | `src/api/src/main/operations/%2Faccounts%2F%7BaccountId%7D/get/response_mapping.yaml:3`, `src/api/src/main/operations/%2Faccounts%2F%7BaccountId%7D%2Fbalances/get/response_mapping.yaml:3` |
| 400 | Entry names response_400.yaml, but the file is absent. **legacy: undefined, not reproduced**; no invented status trigger or body. | `src/api/src/main/operations/%2Faccounts%2F%7BaccountId%7D/get/response_mapping.yaml:7`, `src/api/src/main/operations/%2Faccounts%2F%7BaccountId%7D%2Fbalances/get/response_mapping.yaml:7`, `docs/modernization/01-rediscovery/open-questions.md:1039` |
| 401 | Entry with no condition; literal body `{"code":"ERROR_CODE","message":"Error message"}`. No module-specific authorization trigger is defined here. | `src/api/src/main/operations/%2Faccounts%2F%7BaccountId%7D/get/response_mapping.yaml:9`, `src/api/src/main/operations/%2Faccounts%2F%7BaccountId%7D/get/response_401.yaml:6`, `src/api/src/main/operations/%2Faccounts%2F%7BaccountId%7D%2Fbalances/get/response_401.yaml:6` |
| 403 | Entry with no condition; same literal body. No new access-denial rule. | `src/api/src/main/operations/%2Faccounts%2F%7BaccountId%7D/get/response_mapping.yaml:11`, `src/api/src/main/operations/%2Faccounts%2F%7BaccountId%7D/get/response_403.yaml:6`, `src/api/src/main/operations/%2Faccounts%2F%7BaccountId%7D%2Fbalances/get/response_403.yaml:6` |
| 404 | Entry with no condition; same literal body. Not a new missing-account test. | `src/api/src/main/operations/%2Faccounts%2F%7BaccountId%7D/get/response_mapping.yaml:13`, `src/api/src/main/operations/%2Faccounts%2F%7BaccountId%7D/get/response_404.yaml:6`, `src/api/src/main/operations/%2Faccounts%2F%7BaccountId%7D%2Fbalances/get/response_404.yaml:6` |
| 500 | Final `condition: true`; same literal body, not the abend code or diagnostic text. | `src/api/src/main/operations/%2Faccounts%2F%7BaccountId%7D/get/response_mapping.yaml:15`, `src/api/src/main/operations/%2Faccounts%2F%7BaccountId%7D/get/response_500.yaml:6`, `src/api/src/main/operations/%2Faccounts%2F%7BaccountId%7D%2Fbalances/get/response_500.yaml:6` |

Both `code` and `message` in each existing error body are required and
non-nullable. Keep the placeholder strings literally; do not substitute HRAC,
HNCS, HROL, SQLCODE or new explanatory text
(`src/api/src/main/operations/%2Faccounts%2F%7BaccountId%7D/get/response_500.yaml:6`,
`src/api/src/main/operations/%2Faccounts%2F%7BaccountId%7D%2Fbalances/get/response_500.yaml:6`,
`docs/modernization/01-rediscovery/open-questions.md:1039`).

### Accepted known gaps and declaration defects

INQACC-Q07 is answered, not an instruction to infer runtime behavior from absent
artifacts. Its accepted disposition is binding for this interface definition
(`docs/modernization/01-rediscovery/open-questions.md:1039`):

- Missing 400 mapping: **legacy: undefined, not reproduced**. This does not
  assert that z/OS Connect skips the missing file, or predict deployment/runtime
  failure from it.
- Non-numeric or overlength accountId: retain the direct assignment and
  COMMAREA `PIC 9(8)` move behavior. Exact HTTP conversion/rejection remains a
  known gap; no new regex, prefix stripping, numeric validation, truncation
  algorithm or error response is specified.
- Account-type enum: the OpenAPI enumeration is not a new validation rule;
  reproduce the source type gate and direct output projection.
- Money and interest: OpenAPI `double` is a documentation defect. Preserve
  exact fixed-point decimal JSON numbers, without new rounding or float
  conversion.

The underlying contradictions and missing artifacts remain recorded with
source citations in INQACC-Q07; its domain answer accepts these dispositions
without requiring another REST clarification. This contract does not claim
that undefined cases have executable parity or that deployment/identity gates
are resolved
(`docs/modernization/01-rediscovery/open-questions.md:966`,
`docs/modernization/01-rediscovery/open-questions.md:1039`,
`docs/modernization/03a-architecture/target-architecture.md:225`).

## Strangler-fig switch point

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

## Local implementation boundary - slices 1 through 3

The local workspace is `modern/inqacc/`, with standard
`src/main/java` and `src/test/java` directories. Phase 3a selects Java/Spring
Boot but no concrete project path or runtime versions
(`docs/modernization/03a-architecture/target-architecture.md:5`).
This slice uses a framework-independent Java core suitable for later Spring
Boot composition, not a running Spring service or a selected deployment
version. Run its complete local suite with Java 21+:

```bash
bash modern/inqacc/run-tests.sh
```

The runner compiles with `--release 21`, needs no downloaded dependencies and
removes its temporary classes on exit (`modern/inqacc/run-tests.sh:5`,
`modern/inqacc/run-tests.sh:13`).

`Inqacc.inquire(Commarea)` returns an immutable logical COMMAREA snapshot;
it does not mutate or replace any existing CICS caller
(`modern/inqacc/src/main/java/com/bankofz/inqacc/Inqacc.java:17`).
Java component names correspond to the existing INQACC field suffixes:

| Java component | Exact legacy field |
|---|---|
| eye | INQACC-EYE |
| custno | INQACC-CUSTNO |
| scode | INQACC-SCODE |
| accno | INQACC-ACCNO |
| accType | INQACC-ACC-TYPE |
| intRate | INQACC-INT-RATE |
| opened | INQACC-OPENED |
| overdraft | INQACC-OVERDRAFT |
| lastStmtDt | INQACC-LAST-STMT-DT |
| nextStmtDt | INQACC-NEXT-STMT-DT |
| availBal | INQACC-AVAIL-BAL |
| actualBal | INQACC-ACTUAL-BAL |
| success | INQACC-SUCCESS |
| pcb1Pointer | INQACC-PCB1-POINTER |

The mapping follows the base copybook, not a new external schema
(`src/base/cics/copy/INQACC.cpy:8`,
`modern/inqacc/src/main/java/com/bankofz/inqacc/Inqacc.java:110`).
Identifiers, text, whole-unit overdraft and DDMMYYYY dates retain fixed-width
strings; interest and balances use `BigDecimal`, with no floating-point
conversion or rounding. The opaque pointer retains object identity. The
supported domain is the specification's representable, non-NULL fixtures;
this adds no validation policy for undefined inputs. Date overlays remain
views of the existing eight-character values, not separate stored fields
(`src/base/cics/copy/INQACC.cpy:15`,
`modern/inqacc/src/main/java/com/bankofz/inqacc/Inqacc.java:71`).

`Ports` separates query operations, supplied CICS context/clock, diagnostic
LINK attempts and DISPLAY effects. Only test doubles exist. The core throws
`Abend` carrying HRAC after a CLOSE failure or HNCS after any nonzero highest
SELECT outcome, both NODUMP/CANCEL. This is an internal control-transfer
representation, not a new return field or HTTP error
(`modern/inqacc/src/main/java/com/bankofz/inqacc/Inqacc.java:75`,
`modern/inqacc/src/main/java/com/bankofz/inqacc/Inqacc.java:90`).
There is no commit, rollback, retry, real data adapter, native COMMAREA codec
or REST projection in this slice. Binary CCSID/sign/pointer encoding and live
unit-of-work equivalence are therefore not verified.

OPEN/FETCH technical failures explicitly throw
`UnsupportedOperationException` rather than returning success or silently
delegating. These are development-only guards, **not legacy error semantics**
or permission to route requests to this partial core
(`modern/inqacc/src/main/java/com/bankofz/inqacc/Inqacc.java:44`).
The unchanged switch above remains disabled until full module, REST and
operational acceptance; no legacy dependency is retired or replaced.

Slice 2 adds the internal `Ports.selectHighest(sortcode)` seam. Its adapter
contract is one SELECT at the supplied configured sort code, ordered by
ACCOUNT_NUMBER descending with one row only, without cursor calls or extra
customer/type/account-number predicates. It returns the existing `Fetch`
SQLCODE/row carrier, also used for cursor outcomes; the name does not imply
a cursor FETCH in highest mode. The test double implements that selection,
but no real SQL adapter or deployed ordering/isolation behavior is verified
(`src/base/cics/cobol/INQACC.cbl:843`,
`src/base/cics/cobol/INQACC.cbl:869`,
`modern/inqacc/src/main/java/com/bankofz/inqacc/Inqacc.java:133`,
`modern/inqacc/src/test/java/com/bankofz/inqacc/InqaccAcceptanceTest.java:396`).
The public `inquire(Commarea)` entry, complete logical record shape, widths,
exact scale-2 amounts, success flags and pointer identity are unchanged
(`modern/inqacc/src/main/java/com/bankofz/inqacc/Inqacc.java:17`,
`modern/inqacc/src/main/java/com/bankofz/inqacc/Inqacc.java:110`,
`modern/inqacc/src/test/java/com/bankofz/inqacc/InqaccAcceptanceTest.java:329`).

Slice 3 adds only acceptance tests for R04/R05; the production core and its
public surface are unchanged. The new ordinary-mode fixtures verify suppression
of all-space/all-low-value retrieved types, exact copying of other types and
eyecatchers, numeric limits and zero/negative-cent values, and leap-day/year-boundary
date ordering. Each compares the complete logical record, field widths,
scale-2 decimals, success Y and unchanged pointer identity
(`modern/inqacc/src/test/java/com/bankofz/inqacc/InqaccAcceptanceTest.java:97`,
`modern/inqacc/src/test/java/com/bankofz/inqacc/InqaccAcceptanceTest.java:162`,
`modern/inqacc/src/test/java/com/bankofz/inqacc/InqaccAcceptanceTest.java:329`).
No fail-code field, validation rule, routing change or adapter is introduced.
