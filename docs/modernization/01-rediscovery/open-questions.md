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

### XFRFUN-Q04 - CONTRADICTION: TO error reason and abend diagnostics disagree

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

## Data-model pass

These questions describe checked-in evidence, not the deployed schema or
observed production behavior. CRECUST and IBTRAN have control flow
**unverified by tooling** (`docs/modernization/00-source-of-truth/index.md:16`).
No generated source-of-truth files or legacy sources were edited.

### DM-Q01 - CONTRADICTION: ACCOUNT decimal declarations disagree

DDL specifies interest `DECIMAL(6,2)` and both balances `DECIMAL(12,2)`
(`.setup/jcl/cics/Db2-create.j2:30`,
`.setup/jcl/cics/Db2-create.j2:35`), but ACCDB2 declares
`DECIMAL(4,2)` and `DECIMAL(10,2)`
(`src/base/cics/copy/ACCDB2.cpy:13`,
`src/base/cics/copy/ACCDB2.cpy:18`).
The host variables are `S9(4)V99 COMP-3` and `S9(10)V99 COMP-3`
(`src/base/cics/cobol/UPDACC.cbl:68`,
`src/base/cics/cobol/UPDACC.cbl:73`), agreeing with DDL precision.
Which declaration is deployed/precompiled, and is the smaller SQL
declaration stale? The model preserves DDL and reports, rather than
silently reconciles, the mismatch.

### DM-Q02 - CONTRADICTION: CONTROL schema/name nullability disagree

DDL creates `BANKZ.CONTROL` with nullable `CONTROL_NAME`
(`.setup/jcl/cics/Db2-create.j2:67`).
CONTDB2 declares `STTESTER.CONTROL` with `CONTROL_NAME NOT NULL`
(`src/base/cics/copy/CONTDB2.cpy:7`).
Which qualification and nullability are authoritative in each supported
environment? A unique index on the name is present in DDL
(`.setup/jcl/cics/Db2-create.j2:75`); it must not be relabelled as a
declared NOT NULL primary key.

### DM-Q03 - CONTRADICTION: IMS HISTORY is 56 bytes; writer uses 64

HISTORY DBD says `BYTES=(56)` and ends with ACCID at bytes 49-56
(`src/base/ims/DBD/HISTORY.asm:31`,
`src/base/ims/DBD/HISTORY.asm:72`).
LOADHIST's six-field segment matches that shape
(`src/base/ims/cobol/LOADHIST.cbl:64`).
IBTRAN's HISTORY-SEG has a seventh field, BALANCE-HIST
`S9(13)V9(2) COMP-3`, after ACCID
(`src/base/ims/cobol/IBTRAN.cbl:88`,
`src/base/ims/cobol/IBTRAN.cbl:95`), and Java reads those additional
eight bytes (`src/base/ims/java/src/main/java/nazare/jmp/controller/InsertHist.java:76`).
DB2 also declares BALANCE (`.setup/jcl/ims/Db2-create.j2:33`).
Is the deployed IMS DBD intentionally shorter, or is a DBD revision
absent? What bytes does the supported IMS runtime persist on the ISRT
at `src/base/ims/cobol/IBTRAN.cbl:358`?
IBTRAN control flow is **unverified by tooling**. The model does not
invent an IMS BALANCE field or claim runtime truncation/corruption.

### DM-Q04 - CUSTACCS key semantics, ownership and ACCNUM

CUSTID is `(CUSTID,SEQ,M)`, while CUSACCID overlaps the first 12 bytes
without a SEQ,U declaration (`src/base/ims/DBD/CUSTACCS.asm:32`,
`src/base/ims/DBD/CUSTACCS.asm:38`). The view specifies KEYLEN=12
(`src/base/ims/PSB/IBACSUM.asm:13`).
What uniqueness, if any, is required for `(CUSTID,ACCID)`? May multiple
customers own the same account, and may the same pair appear twice?
ACCNUM is a four-byte INT with a matching `S9(9) COMP-5` loader field,
but its business meaning is unknown
(`src/base/ims/DBD/CUSTACCS.asm:50`,
`src/base/ims/cobol/LOADCUSA.cbl:67`).
Is it a count, ordinal or another identifier? The model asserts none of
these interpretations and does not infer a composite unique key from
PSB KEYLEN.

### DM-Q05 - Cross-system identity and field correspondence

CICS customer/account keys contain sort code and character identifiers
(`.setup/jcl/cics/Db2-create.j2:27`,
`.setup/jcl/cics/Db2-create.j2:83`); IMS uses binary CUSTID/ACCID
(`src/base/ims/DBD/CUSTOMER.asm:31`,
`src/base/ims/DBD/ACCOUNT.asm:31`).
CICS has available/actual balances, but IMS has one balance
(`.setup/jcl/cics/Db2-create.j2:35`,
`src/base/ims/DBD/ACCOUNT.asm:43`).
Is there a supported crosswalk or synchronization contract, and which
balance, status and address fields correspond? Matching business concepts
does not prove that records with similar numbers represent the same
customer/account. No implemented ID conversion or lossless mapping is
asserted.

### DM-Q06 - Nullable DB2 columns versus fixed COBOL records

Most ACCOUNT/CUSTOMER/PROCTRAN columns are nullable in the DDL
(`.setup/jcl/cics/Db2-create.j2:24`,
`.setup/jcl/cics/Db2-create.j2:51`,
`.setup/jcl/cics/Db2-create.j2:81`), whereas the shown hosts and
business records have fixed numeric/character fields
(`src/base/cics/cobol/UPDACC.cbl:61`,
`src/base/cics/cobol/UPDCUST.cbl:56`,
`src/base/cics/copy/CUSTOMER.cpy:17`).
Are SQL NULLs forbidden by an external data contract, and how are
existing NULL rows handled? Are negative SQL overdrafts/credit scores or
nonnumeric CHAR identifiers possible despite unsigned/numeric DISPLAY
business fields (`src/base/cics/copy/ACCOUNT.cpy:10`,
`src/base/cics/copy/ACCOUNT.cpy:21`,
`src/base/cics/copy/CUSTOMER.cpy:36`)?
The model does not equate NULL with spaces/zero or promote PICTURE
restrictions to SQL CHECK constraints.

### DM-Q07 - CONTRADICTION: scanner omits CRECUST CUSTOMER insert

The generated CUSTOMER access row omits CRECUST
(`docs/modernization/00-source-of-truth/data-access.md:11`), and the
CRECUST digest's external-touchpoint section lists only PROCTRAN INSERT
and CONTROL SELECT/UPDATE as SQL
(`docs/modernization/00-source-of-truth/programs/CRECUST.md:91`,
`docs/modernization/00-source-of-truth/programs/CRECUST.md:97`,
`docs/modernization/00-source-of-truth/programs/CRECUST.md:98`).
Original source explicitly says `EXEC SQL` then `INSERT INTO CUSTOMER`
(`src/base/cics/cobol/CRECUST.cbl:1219`,
`src/base/cics/cobol/CRECUST.cbl:1220`), ending at
`src/base/cics/cobol/CRECUST.cbl:1256`.
Why is this statement absent from deterministic scan output? The data
model includes the source-observed insert, while retaining the
qualification that CRECUST control flow is **unverified by tooling**.
The inventory is not evidence that this write is absent or unreachable.

### DM-Q08 - Durability and consistency of the two IMS history stores

IBTRAN attempts Java history saving, IMS HISTORY ISRT and ACCOUNT REPL
(`src/base/ims/cobol/IBTRAN.cbl:350`,
`src/base/ims/cobol/IBTRAN.cbl:358`,
`src/base/ims/cobol/IBTRAN.cbl:366`).
Java disables auto-commit and closes the connection after INSERT; the
shown save method has no explicit commit
(`src/base/ims/java/src/main/java/nazare/jmp/history/TransactionService.java:136`,
`src/base/ims/java/src/main/java/nazare/jmp/history/TransactionService.java:140`,
`src/base/ims/java/src/main/java/nazare/jmp/history/TransactionService.java:161`).
Which runtime transaction/syncpoint contract makes the DB2 write durable,
and are the IMS account/history and DB2 history changes coordinated?
Which store is authoritative when one write fails? IBTRAN control flow
is **unverified by tooling**; neither atomicity nor equal row sets is
inferred from the write attempts.

### DM-Q09 - IMS transaction inputs and message capacity

TTYPE seed data includes `t` Transfer
(`src/base/ims/LoadData/TTYPE.data:2`), but IBTRAN's activity gate only
accepts d/D/w/W (`src/base/ims/cobol/IBTRAN.cbl:294`).
It stores the supplied case unchanged and NUMVAL-converts the amount,
then adds/subtracts it without a positive-amount guard in that block
(`src/base/ims/cobol/IBTRAN.cbl:318`,
`src/base/ims/cobol/IBTRAN.cbl:339`).
Where are transfer, case-normalization and positive-amount rules
enforced, if required? The model does not assume the reference table is
validated by this handler. IBTRAN control flow is **unverified by tooling**.

Separately, account-summary messages allow 1-to-6 entries, while the
IBACSUM traversal increments TOTAL-ACCS and indexes the output without
a six-entry termination test in the shown loop
(`src/base/ims/copy/IBTRAN.cpy:31`,
`src/base/ims/cobol/IBACSUM.cbl:101`,
`src/base/ims/cobol/IBACSUM.cbl:220`,
`src/base/ims/cobol/IBACSUM.cbl:256`).
Is a six-account limit imposed elsewhere? A message OCCURS bound is
not modelled as a persisted ownership constraint.

### DM-Q10 - Processed-event identity and incomplete code meaning

PROCTRAN has no declared unique event key
(`.setup/jcl/cics/Db2-create.j2:51`).
INQTRAND filters by sort code/account/date/time/ref
(`src/base/cics/cobol/INQTRAND.cbl:75`), and customer deletion uses
task number as ref (`src/base/cics/cobol/DELCUS.cbl:739`).
What prevents locator collisions across repeated/reused task IDs and
second-resolution timestamps? Is uniqueness a documented operational
contract rather than a database constraint?

`OCS` is named `PROC-TY-CREATE-SODD`; the declaration does not expand
SODD (`src/base/cics/copy/PROCTRAN.cpy:44`).
What is its precise business meaning and supported producer? The model
retains the literal condition name instead of guessing its expansion
or declaring it unused.

### DM-Q11 - CREACC date representations and CONTRADICTION in decisions

WAD010 calls CALCULATE-DATES, then separately computes a DB2 next-statement
host date as today +30 days
(`src/base/cics/cobol/CREACC.cbl:880`,
`src/base/cics/cobol/CREACC.cbl:892`,
`src/base/cics/cobol/CREACC.cbl:898`,
`src/base/cics/cobol/CREACC.cbl:914`).
CALCULATE-DATES instead adds 28/29 days in February and 30 otherwise,
assigning ACCOUNT-NEXT-STMT-DATE
(`src/base/cics/cobol/CREACC.cbl:1231`,
`src/base/cics/cobol/CREACC.cbl:1246`,
`src/base/cics/cobol/CREACC.cbl:1278`).
Which date is the intended contract for each representation/caller?
The model distinguishes the assignments rather than claiming they agree.

The digest reports only one CD010 decision, the EVALUATE at 1231
(`docs/modernization/00-source-of-truth/programs/CREACC.md:87`,
`docs/modernization/00-source-of-truth/programs/CREACC.md:133`).
The original source also has three nested IF decisions for leap-year
remainders (`src/base/cics/cobol/CREACC.cbl:1250`,
`src/base/cics/cobol/CREACC.cbl:1254`,
`src/base/cics/cobol/CREACC.cbl:1259`).
Why are these absent from the parsed decision inventory despite the
successful parse marker? As with XFRFUN-Q01, the marker is not a guarantee
of complete decision coverage; generated files remain unchanged.

## Integration-inventory pass

The inventory is in `integrations.md`. **BNK1CCS, BNK1DCS, CRECUST,
IBTRAN and BNKSTMTC have control flow unverified by tooling**
(`docs/modernization/00-source-of-truth/index.md:16`). No live runtime,
production traffic or installed-resource state was available for this pass.

### INT-Q01 - Which interfaces are load-bearing in production?

Checked-in CICS resources, REST assets and statement jobs establish
interfaces, not deployed use (`.setup/zconfig/bank-of-z-definitions.yaml:842`,
`src/api/src/main/zosAssets/INQACC/zosAsset.yaml:9`,
`src/base/batch/jcl/BNKSTMTC.jcl:29`). Which are actually deployed and
business-critical, with which callers, owners and service contracts?
The inventory uses L? for load-bearing candidates and reserves legacy-only
for evidenced setup/load/test/development roles. Neither absence of a caller
nor a sample label proves an artifact can be retired.

### INT-Q02 - Bank of Q and deployed MQ bridge routing

The client names Bank of Q and selects local bindings-mode CICS or IMS
paths (`src/base/mq/java/src/main/java/com/ibm/boq/TransferMoneyToBOZ.java:15`,
`src/base/mq/java/src/main/java/com/ibm/boq/TransferMoneyToBOZ.java:72`,
`src/base/mq/java/src/main/java/com/ibm/boq/TransferMoneyToBOZViaCICS.java:49`,
`src/base/mq/java/src/main/java/com/ibm/boq/TransferMoneyToBOZViaIMS.java:81`).
CICS MQMONITOR specifies CKBR, with bridge definitions said to come from
DFHLIST (`.setup/zconfig/bank-of-z-definitions.yaml:1225`,
`.setup/zconfig/bank-of-z-definitions.yaml:1228`); IMS request queues use
IMSBRDG/XCF settings (`.setup/zconfig/mq-queue-manager.yaml:441`,
`.setup/zconfig/mq-queue-manager.yaml:2993`).
Is Bank of Q exclusively a demo peer? Which queue manager, CKBR/adapter
resources, OTMA member and authorization definitions are installed?
The client target strings DBCRFUN and IBTRAN are proved, but neither a
real partner nor successful end-to-end routing is inferred.

### INT-Q03 - JNI initialization and unresolved runtime pointers

The five graph targets are JNI function pointers, not missing COBOL
programs (`src/base/ims/copy/JNI.cpy:90`,
`src/base/ims/copy/JNI.cpy:107`, `src/base/ims/copy/JNI.cpy:197`,
`src/base/ims/copy/JNI.cpy:225`, `src/base/ims/copy/JNI.cpy:313`).
IBTRAN addresses the JNI tables from JNIEnvPtr, requests InsertHist and
insertHist(ByteBuffer), and calls the returned method ID
(`src/base/ims/cobol/IBTRAN.cbl:508`,
`src/base/ims/cobol/IBTRAN.cbl:511`,
`src/base/ims/cobol/IBTRAN.cbl:516`,
`src/base/ims/cobol/IBTRAN.cbl:540`,
`src/base/ims/cobol/IBTRAN.cbl:554`,
`src/base/ims/cobol/IBTRAN.cbl:490`).
What initializes JNIEnvPtr and supplies the supported JVM invocation
contract? What validates the function-table layout and exception handling
on the deployed runtime? Source-level operation identities are explained;
actual runtime addresses/class/method IDs remain unresolved. IBTRAN control
flow is unverified by tooling; see DM-Q03/DM-Q08 for layout/durability.

### INT-Q04 - Which login implementation is installed?

The transaction maps IBLOGIN to IBLOGIN, and its PSB says LANG=PL/I
(`.setup/jcl/ims/Ims-spoc.jcl.j2:56`,
`src/base/ims/PSB/IBLOGIN.asm:13`); the PL/I procedure declares IBLOGIN
(`src/base/ims/pli/IBLOGIN.pli:20`). The alternative COBOL member declares
IBLOGIN1 (`src/base/ims/cobol/IBLOGIN1.cbl:3`).
Which load module and PSB are installed in each environment? Is IBLOGIN1
called under another definition or retained for comparison? No alias,
dead-code conclusion or equivalence of message layouts is inferred.

### INT-Q05 - IMS reference/status databases and generic PSBs

IB and IBLOAD expose ACCTYPE, CUSTTYPE, TSTATTYP and TTYPE alongside the
five handler/loader databases (`src/base/ims/PSB/IB.asm:13`,
`src/base/ims/PSB/IB.asm:22`, `src/base/ims/PSB/IB.asm:31`,
`src/base/ims/PSB/IB.asm:34`, `src/base/ims/PSB/IBLOAD.asm:13`).
LOADTSTA inserts TSTAT, but no online TSTAT consumer is established by the
handler calls (`src/base/ims/cobol/LOADTSTA.cbl:168`).
Which applications use these databases and generic program IB, and where
are their load/allocation and code-validation contracts? PSB sensitivity
and seed records do not prove an executed application integration or a
mandatory business lookup. Their online purpose remains unknown.

### INT-Q06 - Placeholder OAuth and API support endpoints

OpenAPI advertises OAuth2 URLs under auth.bankofz.example.com and a
bankofz.example.com support mailbox
(`src/api/src/main/api/openapi.yaml:790`,
`src/api/src/main/api/openapi.yaml:791`,
`src/api/src/main/api/openapi.yaml:10`).
What identity provider, authorization configuration and support destination
replace these placeholders? The declarations are not evidence of live
authentication, an external service call or enforced scopes.

### INT-Q07 - ITRL/ITRD EIB metadata versus transaction resources

INQTRANL and INQTRAND providers set transidUsage=EIB_ONLY and transids
ITRL/ITRD (`src/api/src/main/zosAssets/INQTRANL/zosAsset.yaml:7`,
`src/api/src/main/zosAssets/INQTRAND/zosAsset.yaml:6`). The generated
configuration table lists neither ID
(`docs/modernization/00-source-of-truth/call-graph.md:153`).
Are these intentionally metadata-only identifiers in every supported
configuration, and what security/accounting definitions depend on them?
No transaction-to-program binding is invented. Their same-spelled ABEND
codes are separate uses (`src/base/cics/cobol/INQTRANL.cbl:561`,
`src/base/cics/cobol/INQTRAND.cbl:338`).

### INT-Q08 - Unestablished utility, bank-to-bank and support consumers

GETCOMPY and GETSCODE have callable COMMAREAs and literal results, but no
incoming edge in the generated graph
(`src/base/cics/cobol/GETCOMPY.cbl:35`,
`src/base/cics/cobol/GETCOMPY.cbl:38`,
`src/base/cics/cobol/GETSCODE.cbl:35`,
`src/base/cics/cobol/GETSCODE.cbl:39`).
A configured BNK1B2B is labelled Bank to Bank Transfer, while BNK1B2M is
explicitly excluded as temporary
(`.setup/zconfig/bank-of-z-definitions.yaml:180`,
`.setup/zconfig/bank-of-z-definitions.yaml:182`,
`.setup/deploy/Development.yml:16`).
Which callers/implementations, if any, use these interfaces? Do not infer
that BNK1B2B is the MQ bridge or that these artifacts are dead.

The support queue TRAA specifies transaction TRAA, without an established
program mapping in this inventory
(`.setup/zconfig/debug-definitions.yaml:1531`).
Which installed resource resolves it, and which debugger/socket-support
definitions are actually required? Support configuration is not proof of
a banking integration.

### INT-Q09 - CONTRADICTION: batch SYSOUT contract versus JCL allocation

The COBOL statement job documents a SYSOUT diagnostic DD and RC12 when
SYSOUT is unavailable (`src/base/batch/jcl/BNKSTMTC.jcl:16`,
`src/base/batch/jcl/BNKSTMTC.jcl:19`), but its execution DD block supplies
SYSIN, SYSPRINT, SYSUDUMP, CEEDUMP, SYSTSPRT and SYSTSIN, not SYSOUT
(`src/base/batch/jcl/BNKSTMTC.jcl:42`).
Source comments state DISPLAY routes to SYSOUT and assume reaching the
probe means it is available (`src/base/batch/cobol/BNKSTMTC.cbl:563`).
Does the supported runtime allocate it implicitly, require external
allocation, or fail before a program RC can be returned? Which output
and return-code contract do operations rely on? BNKSTMTC control flow
is unverified by tooling; no z/OS execution was attempted.

### INT-Q10 - Statement schedules and delivery are unknown

Statement JCL has dated sample cards rather than scheduler definitions
(`src/base/batch/jcl/BNKSTMTC.jcl:42`,
`src/base/batch/jcl/BNKSTMTP.jcl:9`). What scheduler/calendar, cutoff,
predecessors, retries, retention and report recipients apply? Are the
COBOL and PL/I jobs alternatives or independently used?
"Monthly" is report intent, not a proven installed cadence, and spool
output is not proof of customer delivery. BNKSTMTC control flow is
unverified by tooling.

### INT-Q11 - CONTRADICTION: incomplete CRECUST command scan

The digest reports only 20 EXEC CICS commands and its touchpoint table
contains an apparent FETCH ANY command sourced from a DISPLAY message
(`docs/modernization/00-source-of-truth/programs/CRECUST.md:13`,
`docs/modernization/00-source-of-truth/programs/CRECUST.md:88`).
Original source at that latter location is a diagnostic string
`'EXEC CICS FETCH ANY worked, '`, not an executable command
(`src/base/cics/cobol/CRECUST.cbl:1030`).
Conversely, an actual GET CONTAINER using the fetched child channel occurs
at `src/base/cics/cobol/CRECUST.cbl:1010`, absent from that table.
Why does the scan omit the real command and recognize the DISPLAY text?
The integration inventory cites the actual PUT/RUN/FETCH/GET statements
directly, not the false-positive command. This is separate from the
CUSTOMER INSERT omission already recorded in DM-Q07. Generated files
were not edited; CRECUST control flow remains unverified by tooling.

### CRECUST unresolved-target follow-through

The earlier tooling-gap entry is retained as the original scanner result.
This pass establishes a manual target-set inference: WS-CC-CNT is PIC 9,
the loop varies 1 through 5, STRING builds OCR plus that digit, and RUN
uses the constructed field (`src/base/cics/cobol/CRECUST.cbl:231`,
`src/base/cics/cobol/CRECUST.cbl:620`,
`src/base/cics/cobol/CRECUST.cbl:626`,
`src/base/cics/cobol/CRECUST.cbl:687`).
The five resource definitions map OCR1..OCR5 to CRDTAGY1..CRDTAGY5
(`.setup/zconfig/bank-of-z-definitions.yaml:691`,
`.setup/zconfig/bank-of-z-definitions.yaml:722`,
`.setup/zconfig/bank-of-z-definitions.yaml:753`,
`.setup/zconfig/bank-of-z-definitions.yaml:784`,
`.setup/zconfig/bank-of-z-definitions.yaml:815`).
The construction and definitions are proven source facts; the finite
target-set association is manual inference, not a repaired parser graph
or evidence of installed routing. CRECUST control flow remains unverified
by tooling, and production agency use remains unknown under INT-Q01.

## Consolidated artifact audit - 2026-10-30

This register is the counting authority: **38 open question IDs**. Existing
questions and their evidence above are retained; related subquestions count
once per ID, not once per question mark. TOOL-Q01..Q05 give the existing
parser gaps stable IDs. TOOL-Q06 records the missing-member evidence gap.
ROUTE-Q01 consolidates the unresolved-target entry and its follow-through;
it does not reopen the manually inferred OCR1..OCR5 construction.

Blast radius identifies the **primary downstream acceptance gate affected
by the answer**, not severity or a proposed implementation. Other affected
phases are named in each row. Phase 2 means design/contracts, Phase 3 implementation
and equivalence checks, and Phase 4 deployment/operations. Owners named
below are requested reviewers, not discovered operational owners.

| Primary blast radius | Open questions | Consequence of a wrong answer |
|---|---:|---|
| Phase 2 - design/contracts (then Phases 3 and 4) | 30 | Incorrect scope, data/interface contracts, or behavior baseline |
| Phase 3 - implementation/equivalence (then Phase 4) | 1 | Incorrect JNI/runtime integration behavior |
| Phase 4 - deployment/operations | 7 | Incorrect routing, identity/security configuration, diagnostics or scheduling |
| **Total** | **38** | |

**Contradictions: 11 distinct question IDs**: XFRFUN-Q01, XFRFUN-Q04,
DM-Q01, DM-Q02, DM-Q03, DM-Q07, DM-Q11, INT-Q09, INT-Q11, AUD-Q01
and AUD-Q02. Two mismatches are newly identified (AUD-Q01/AUD-Q02);
XFRFUN-Q04's existing diagnostic disagreement is now explicitly labelled.
These include source/source and documentation/wiring disagreements, not
only contradictions with generated facts. A digest-summary omission is
not, by itself, proof that the parser lost the corresponding branch.

### Questions affecting Phase 2 - design/contracts

For existing IDs, the earlier same-ID entry supplies the detailed source
and source-of-truth evidence. Each row adds the specific audited artifact,
an answerable review question and its downstream consequence.

| ID | Specific artifact reference | Question for the responsible expert | Blast radius if wrong |
|---|---|---|---|
| TOOL-Q01 | `docs/modernization/01-rediscovery/repo-map.md:74`; `docs/modernization/01-rediscovery/integrations.md:396`; digest `docs/modernization/00-source-of-truth/programs/BNKSTMTC.md:10` | Has a batch reviewer signed off the statement paths described here despite the parser gap: yes/no; reviewer/evidence = ___? | Phase 2 statement/input/output baseline; Phase 3 equivalence. BNKSTMTC control flow is **unverified by tooling**. |
| TOOL-Q02 | `docs/modernization/01-rediscovery/repo-map.md:21`; `docs/modernization/01-rediscovery/integrations.md:39`; digest `docs/modernization/00-source-of-truth/programs/BNK1CCS.md:10` | Has a CICS reviewer signed off the create-customer screen paths despite the parser gap: yes/no; evidence = ___? | Phase 2 customer-entry contract; Phase 3 screen-path equivalence. BNK1CCS control flow is **unverified by tooling**. |
| TOOL-Q03 | `docs/modernization/01-rediscovery/repo-map.md:24`; `docs/modernization/01-rediscovery/integrations.md:42`; digest `docs/modernization/00-source-of-truth/programs/BNK1DCS.md:10` | Has a CICS reviewer signed off customer display/update/delete paths despite the parser gap: yes/no; evidence = ___? | Phase 2 maintenance/deletion contract; Phase 3 equivalence. BNK1DCS control flow is **unverified by tooling**. |
| TOOL-Q04 | `docs/modernization/01-rediscovery/data-model.md:272`; `docs/modernization/01-rediscovery/integrations.md:89`; digest `docs/modernization/00-source-of-truth/programs/CRECUST.md:10` | Has a domain/CICS reviewer signed off customer validation, allocation and credit-response paths: yes/no; evidence = ___? | Phase 2 customer/credit contract; Phase 3 equivalence. CRECUST control flow is **unverified by tooling**. |
| TOOL-Q05 | `docs/modernization/01-rediscovery/data-model.md:279`; `docs/modernization/01-rediscovery/integrations.md:301`; digest `docs/modernization/00-source-of-truth/programs/IBTRAN.md:10` | Has an IMS reviewer signed off transaction/update/history paths despite the parser gap: yes/no; evidence = ___? | Phase 2 transaction/history baseline; Phase 3 equivalence. IBTRAN control flow is **unverified by tooling**. |
| TOOL-Q06 | `docs/modernization/01-rediscovery/repo-map.md:164`; `docs/modernization/00-source-of-truth/index.md:67` | Authoritative generated BMS symbolic maps and IBM-supplied member versions, with the reviewer responsible for validating omitted fields = ___? | Phase 2 field/interface baseline; Phase 3 layout and screen equivalence. Empty parser stubs do not verify missing fields. |
| XFRFUN-Q01 | `docs/modernization/01-rediscovery/business-rules.md:84` | Does the tooling maintainer confirm that the rollback-response IF at source line 1966 is missing from the 40-decision digest: yes/no; accepted coverage evidence = ___? | Phase 2 decision completeness; Phase 3 rollback-error equivalence. **CONTRADICTION** retained. |
| XFRFUN-Q02 | `docs/modernization/01-rediscovery/business-rules.md:23` | Is arithmetic/UPDATE after a TO SELECT error other than +100/-911 supported behavior: yes/no; permitted host-variable state/outcome = ___? | Phase 2 failure contract; Phase 3 balance/error equivalence. |
| XFRFUN-Q03 | `docs/modernization/01-rediscovery/business-rules.md:25` | Supported compiler/runtime and approved PERFORM-return behavior after these retry jumps = ___; exactly one transfer/two history rows guaranteed: yes/no? | Phase 2 retry/atomicity contract; Phase 3 duplicate-effect checks. |
| XFRFUN-Q05 | `docs/modernization/01-rediscovery/business-rules.md:33` | Supported invocation delivering AFCR/AFCS/AFCT = ___ (or none); caller discriminator for overloaded failure code 2 = ___? | Phase 2 reachable failure contract; Phase 3 storm-drain equivalence. Not confirmed dead. |
| DM-Q01 | `docs/modernization/01-rediscovery/data-model.md:61` | Deployed/precompiled ACCOUNT interest/balance precision = ___; smaller ACCDB2 declaration obsolete: yes/no? | Phase 2 numeric/schema contract; Phase 3 precision equivalence. **CONTRADICTION** retained. |
| DM-Q02 | `docs/modernization/01-rediscovery/data-model.md:125`; `docs/modernization/01-rediscovery/integrations.md:190` | Authoritative CONTROL qualification and CONTROL_NAME nullability per supported environment = ___? | Phase 2 allocation/schema contract; Phase 3 counter behavior. **CONTRADICTION** retained. |
| DM-Q03 | `docs/modernization/01-rediscovery/data-model.md:184`; `docs/modernization/01-rediscovery/data-model.md:236` | Installed IMS HISTORY segment length = ___ bytes; BALANCE-HIST persisted by IMS: yes/no; runtime evidence = ___? | Phase 2 history layout; Phase 3 byte-level equivalence. **CONTRADICTION** retained. IBTRAN control flow is **unverified by tooling**. |
| DM-Q04 | `docs/modernization/01-rediscovery/data-model.md:180`; `docs/modernization/01-rediscovery/data-model.md:182` | ACCNUM means ___; duplicate customer/account pairs allowed: yes/no; shared account ownership allowed: yes/no? | Phase 2 identity/cardinality contract; Phase 3 traversal equivalence. |
| DM-Q05 | `docs/modernization/01-rediscovery/data-model.md:251` | Supported CICS/IMS identity crosswalk and balance/status/address correspondence = ___ (or no cross-system contract)? | Phase 2 entity boundaries; Phase 3 cross-system equivalence. |
| DM-Q06 | `docs/modernization/01-rediscovery/data-model.md:42`; `docs/modernization/01-rediscovery/data-model.md:63` | SQL NULLs forbidden externally: yes/no; existing NULL handling and permitted negative/nonnumeric field values = ___? | Phase 2 nullability/value domains; Phase 3 edge-case equivalence. |
| DM-Q07 | `docs/modernization/01-rediscovery/data-model.md:341`; `docs/modernization/01-rediscovery/integrations.md:207` | Does the tooling maintainer confirm the omitted CUSTOMER INSERT: yes/no; accepted complete write inventory = ___? | Phase 2 customer-write inventory; Phase 3 persistence equivalence. **CONTRADICTION** retained. CRECUST control flow is **unverified by tooling**. |
| DM-Q08 | `docs/modernization/01-rediscovery/data-model.md:236`; `docs/modernization/01-rediscovery/integrations.md:317` | Runtime owner/transaction contract coordinating IMS and DB2 history = ___; authoritative store on partial failure = ___? | Phase 2 durability/atomicity contract; Phase 3 failure equivalence. IBTRAN control flow is **unverified by tooling**. |
| DM-Q09 | `docs/modernization/01-rediscovery/data-model.md:279`; `docs/modernization/01-rediscovery/data-model.md:346` | Supported transaction types/case/amount validation layer = ___; enforced maximum summary size and behavior above six accounts = ___? | Phase 2 request/capacity contract; Phase 3 boundary equivalence. IBTRAN control flow is **unverified by tooling**. |
| DM-Q10 | `docs/modernization/01-rediscovery/data-model.md:89`; `docs/modernization/01-rediscovery/data-model.md:303` | PROCTRAN locator uniqueness guaranteed operationally: yes/no; guarantee = ___; OCS/SODD meaning and supported producer = ___? | Phase 2 event identity/code domain; Phase 3 lookup and history equivalence. |
| DM-Q11 | `docs/modernization/01-rediscovery/data-model.md:270` | Intended next-statement date for DB2 and returned record respectively = ___; omitted leap-year IF decisions acknowledged by tooling maintainer: yes/no? | Phase 2 date/decision baseline; Phase 3 leap-year equivalence. **CONTRADICTION** retained. |
| INT-Q01 | `docs/modernization/01-rediscovery/integrations.md:5` | Per inventory entry: deployed environments, accountable owner, caller, purpose, criticality and service contract = ___ (or explicitly unused, with evidence)? | Phase 2 scope/dependency ownership; Phases 3/4 integration and cutover assumptions. Covers all entries, including CDN/JDBC/browser/support dependencies, not just banking programs. |
| INT-Q04 | `docs/modernization/01-rediscovery/repo-map.md:130`; `docs/modernization/01-rediscovery/integrations.md:299` | Installed IBLOGIN module/PSB per environment = ___; supported IBLOGIN1 caller = ___ (or none)? | Phase 2 login/message contract; Phase 3 authentication equivalence. No alias/dead-code inference. |
| INT-Q05 | `docs/modernization/01-rediscovery/integrations.md:263`; `docs/modernization/01-rediscovery/data-model.md:213` | Executed consumers/owners of TSTAT, the four reference databases and generic IB/IBLOAD views = ___; code-validation contract = ___? | Phase 2 database/domain scope; Phase 3 lookup equivalence. |
| INT-Q08 | `docs/modernization/01-rediscovery/integrations.md:109`; `docs/modernization/01-rediscovery/integrations.md:128`; `docs/modernization/01-rediscovery/integrations.md:158` | Supported callers/implementations for GETCOMPY, GETSCODE, BNK1B2B/BNK1B2M and TRAA = ___; required debugger/socket definitions = ___? Also EQAL -> CSSL consumer, direction, owner and purpose = ___ (`docs/modernization/01-rediscovery/integrations.md:160`; `.setup/zconfig/debug-definitions.yaml:1483`). | Phase 2 interface scope; Phase 4 support-resource dependencies. Exclusion/no incoming graph edge does not confirm dead code. |
| INT-Q11 | `docs/modernization/01-rediscovery/integrations.md:89`; `docs/modernization/01-rediscovery/integrations.md:510` | Does the tooling maintainer confirm that DISPLAY text is scanned as a command while GET CONTAINER is omitted: yes/no; accepted command inventory = ___? | Phase 2 credit-response dependency baseline; Phase 3 error-path equivalence. **CONTRADICTION** retained. CRECUST control flow is **unverified by tooling**. |
| AUD-Q01 | `docs/modernization/01-rediscovery/data-model.md:276` versus `docs/modernization/01-rediscovery/business-rules.md:9` and `docs/modernization/01-rediscovery/business-rules.md:13` | Should the data-model contract describe identity comparison **after both sort codes are overwritten**, making equal numbers sufficient for a positive transfer's SAME abend: yes/no? | Phase 2 effective account identity; Phase 3 same-number/different-input-sort-code equivalence. **CONTRADICTION**, detailed below. |
| AUD-Q02 | `docs/modernization/01-rediscovery/data-model.md:268`; digest `docs/modernization/00-source-of-truth/programs/CREACC.md:137` | Is the EVALUATE summary intentionally abbreviated: yes/no; approved evidence for LOAN and WHEN OTHER branches = ___? | Phase 2 accepted account-type/decision contract; Phase 3 rejection equivalence. **CONTRADICTION** in summary completeness, detailed below. |
| AUD-Q03 | `docs/modernization/01-rediscovery/data-model.md:274`; `docs/modernization/01-rediscovery/data-model.md:314`; `docs/modernization/01-rediscovery/data-model.md:318`; `docs/modernization/01-rediscovery/integrations.md:418` | Approved per-interface failure/status-code matrix, retryability and consumer action = ___; facility 496/NONE business origin and other supported facility values = ___; supported CUSTOMER-STATUS transitions and PROCTRAN logical-delete consumers = ___? | Phase 2 result/code/lifecycle contracts; Phase 3 error and state-transition equivalence. The operational inventory names code families but does not supply these contracts. |
| AUD-Q04 | `docs/modernization/01-rediscovery/business-rules.md:3`; `docs/modernization/01-rediscovery/business-rules.md:84`; `docs/modernization/01-rediscovery/data-model.md:267` | Is business-rule/decision coverage intentionally limited to XFRFUN: yes/no; required remaining program scope and reviewer = ___; IBACSUM RULES-CHECK inactive in every supported build: yes/no, evidence = ___? | Phase 2 completeness acceptance; Phase 3 equivalence scope. No coverage table for other programs is not the same as zero uncovered decisions; no dead-code conclusion is drawn. |

### Question affecting Phase 3 - implementation/equivalence

| ID | Specific artifact reference | Question for the responsible expert | Blast radius if wrong |
|---|---|---|---|
| INT-Q03 | `docs/modernization/01-rediscovery/integrations.md:307`; `docs/modernization/01-rediscovery/integrations.md:315`; `docs/modernization/01-rediscovery/integrations.md:476` | Supported JVM/JNI initialization owner, JNIEnvPtr setup, ABI/function-table version and exception-checking contract = ___? | Phase 3 JNI invocation/exception equivalence; Phase 4 runtime/library wiring. All five pointer targets remain runtime-unresolved; IBTRAN control flow is **unverified by tooling**. |

### Questions affecting Phase 4 - deployment/operations

| ID | Specific artifact reference | Question for the responsible expert | Blast radius if wrong |
|---|---|---|---|
| XFRFUN-Q04 | `docs/modernization/01-rediscovery/business-rules.md:27`; `docs/modernization/01-rediscovery/integrations.md:425` | Approved SELECT/UPDATE timeout reason numbers and recorded/raised abend-code mapping = ___; mismatches deliberate: yes/no? | Phase 4 monitoring/runbook diagnosis; Phase 3 diagnostic-output equivalence also inherits the answer. **CONTRADICTION** retained. |
| INT-Q02 | `docs/modernization/01-rediscovery/integrations.md:382`; `docs/modernization/01-rediscovery/integrations.md:386`; `docs/modernization/01-rediscovery/integrations.md:392` | Bank of Q demo-only: yes/no; installed queue manager, CKBR/adapter, OTMA/XCF routing, authorization and accountable operator = ___? | Phase 4 partner routing/access and end-to-end operation. IBTRAN control flow is **unverified by tooling**. |
| INT-Q06 | `docs/modernization/01-rediscovery/integrations.md:375`; `docs/modernization/01-rediscovery/integrations.md:376` | Deployed identity provider, enforced authorization/scopes, support destination and owner = ___? | Phase 4 authentication/authorization and operational support; placeholder URLs are not live controls. |
| INT-Q07 | `docs/modernization/01-rediscovery/integrations.md:366` | ITRL/ITRD intentionally EIB-only in every supported environment: yes/no; dependent security/accounting resources and UPDCUST's omitted-metadata behavior = ___? | Phase 4 transaction identity/security/accounting configuration. No transaction-to-program binding inferred. |
| INT-Q09 | `docs/modernization/01-rediscovery/integrations.md:402` | SYSOUT allocated implicitly, externally, or missing = ___; actual unavailable-output behavior/return code = ___? | Phase 4 batch diagnostics/output allocation. **CONTRADICTION** retained; BNKSTMTC control flow is **unverified by tooling**. |
| INT-Q10 | `docs/modernization/01-rediscovery/integrations.md:408`; `docs/modernization/01-rediscovery/integrations.md:412` | Installed statement-job variant(s), scheduler/calendar, cutoff, predecessors, retries, retention and delivery recipients/channel = ___? | Phase 4 statement schedule, recovery and delivery. BNKSTMTC control flow is **unverified by tooling**; monthly intent does not prove cadence. |
| ROUTE-Q01 | `docs/modernization/01-rediscovery/repo-map.md:116`; `docs/modernization/01-rediscovery/integrations.md:475`; `docs/modernization/00-source-of-truth/call-graph.md:326` | Are OCR1..OCR5 installed and routed to CRDTAGY1..5 in each supported environment: yes/no; overrides/operator/evidence = ___? | Phase 4 credit-agency routing; installed execution remains unresolved despite the manual finite-target inference. CRECUST control flow is **unverified by tooling**. |

### Newly identified contradictions and coverage gaps

**AUD-Q01 - CONTRADICTION: effective transfer identity is inconsistent
across artifacts.** The data model says the check is composite, "not simply
equal account numbers" (`docs/modernization/01-rediscovery/data-model.md:276`).
R01/R03 instead state that both supplied sort codes are overwritten before
the identity gate (`docs/modernization/01-rediscovery/business-rules.md:9`,
`docs/modernization/01-rediscovery/business-rules.md:13`). The digest quotes
the composite IF (`docs/modernization/00-source-of-truth/programs/XFRFUN.md:99`),
but the focused source excerpt also shows `MOVE SORTCODE TO COMM-FSCODE
COMM-TSCODE` before the amount gate (`src/base/cics/cobol/XFRFUN.cbl:281`,
`src/base/cics/cobol/XFRFUN.cbl:289`, `src/base/cics/cobol/XFRFUN.cbl:316`).
The IF text is accurate; the data model's unqualified behavioral conclusion
omits the normalization. Example to approve: a positive transfer with equal
account numbers but different supplied sort codes still takes SAME.
The source is not changed, and the disagreeing artifacts are not silently
reconciled.

**AUD-Q02 - CONTRADICTION: CREACC decision summary omits LOAN and
WHEN OTHER.** The data model includes LOAN and failure A
(`docs/modernization/01-rediscovery/data-model.md:268`).
The digest's EVALUATE entry displays only ISA/MORTGAGE/SAVING/CURRENT,
ending mid-literal (`docs/modernization/00-source-of-truth/programs/CREACC.md:137`).
The cited source additionally contains `WHEN ... (1:4) = 'LOAN'`, then
`WHEN OTHER` setting N/A (`src/base/cics/cobol/CREACC.cbl:1314`,
`src/base/cics/cobol/CREACC.cbl:1316`,
`src/base/cics/cobol/CREACC.cbl:1318`). This is an incomplete digest
summary, not evidence that LOAN is unsupported or that parsing omitted
those branches. It is separate from DM-Q11's missing nested IF decisions.

**AUD-Q03 - code-contract gaps, not invented meanings.** The artifacts
identify response fields and operational code groups, but do not give a
per-operation failure-code/action matrix
(`docs/modernization/01-rediscovery/integrations.md:418`,
`docs/modernization/01-rediscovery/integrations.md:428`).
For example, the digest records facility-496 gates
(`docs/modernization/00-source-of-truth/programs/DBCRFUN.md:78`,
`docs/modernization/00-source-of-truth/programs/DBCRFUN.md:79`);
the focused source comment calls 496 `NONE` and contrasts payment with
teller, while branches set failure 4 for loan/mortgage and failure 3 for
insufficient funds (`src/base/cics/cobol/DBCRFUN.cbl:323`,
`src/base/cics/cobol/DBCRFUN.cbl:325`,
`src/base/cics/cobol/DBCRFUN.cbl:330`,
`src/base/cics/cobol/DBCRFUN.cbl:344`). Those literal observations do not
establish every supported caller/facility or recovery policy. Likewise,
named status conditions and a logical-delete overlay do not specify
supported transitions or consumers
(`docs/modernization/01-rediscovery/data-model.md:314`,
`docs/modernization/01-rediscovery/data-model.md:315`).
The already-open OCS expansion/producer question remains DM-Q10 rather
than being counted twice.

**AUD-Q04 - coverage outside the transfer slice is not established.**
The sole Decision coverage table explicitly covers XFRFUN, reporting
33 covered / 7 technical / 0 uncovered plus one source-only technical
decision (`docs/modernization/01-rediscovery/business-rules.md:37`,
`docs/modernization/01-rediscovery/business-rules.md:84`).
That is not a repository-wide completeness result. For example, the
IBACSUM digest lists `IF CA-CUSTID < 16` in RULES-CHECK but shows no
incoming PERFORM to that paragraph
(`docs/modernization/00-source-of-truth/programs/IBACSUM.md:20`,
`docs/modernization/00-source-of-truth/programs/IBACSUM.md:58`).
The focused traversal excerpt contains a commented-out PERFORM
(`src/base/ims/cobol/IBACSUM.cbl:223`). This establishes a review
candidate, not that the paragraph is dead in every supported build.
No whole-program rereading was used to infer reachability.

### Citation spot-check ledger

These are checks actually made against individual digests and focused
original-source excerpts. **17 citation checks** are recorded below;
multiple observations of one issue count as only one contradiction ID.
DDL/JCL/DBD and Java semantics remain outside parser scope.

| # | Audited artifact citation | Digest / source-of-truth check | Original-source check and result |
|---|---|---|---|
| 1 | `docs/modernization/01-rediscovery/business-rules.md:11` | Amount IF present at `docs/modernization/00-source-of-truth/programs/XFRFUN.md:95` | `src/base/cics/cobol/XFRFUN.cbl:289` sets N/4 at <=0: confirmed. |
| 2 | `docs/modernization/01-rediscovery/business-rules.md:13`; `docs/modernization/01-rediscovery/data-model.md:276` | Same-account IF present at `docs/modernization/00-source-of-truth/programs/XFRFUN.md:99` | `src/base/cics/cobol/XFRFUN.cbl:281` and `src/base/cics/cobol/XFRFUN.cbl:316`: prior overwrite confirms R01/R03; data-model qualification mismatch is AUD-Q01. |
| 3 | `docs/modernization/01-rediscovery/business-rules.md:25` | Reason/counter decisions at `docs/modernization/00-source-of-truth/programs/XFRFUN.md:126` and `docs/modernization/00-source-of-truth/programs/XFRFUN.md:127` | `src/base/cics/cobol/XFRFUN.cbl:1199` and `src/base/cics/cobol/XFRFUN.cbl:1203`: reason 13172872 and increment-before-<6 confirmed; runtime guarantee remains XFRFUN-Q03. |
| 4 | `docs/modernization/01-rediscovery/business-rules.md:27` | UPDATE timeout test at `docs/modernization/00-source-of-truth/programs/XFRFUN.md:136` | `src/base/cics/cobol/XFRFUN.cbl:1485`: 13172872 confirmed, not silently changed to SELECT's timeout reason. Existing XFRFUN-Q04. |
| 5 | `docs/modernization/01-rediscovery/integrations.md:425` | UPDATE ABEND touchpoint at `docs/modernization/00-source-of-truth/programs/XFRFUN.md:223` | `src/base/cics/cobol/XFRFUN.cbl:1522` records RUF2; `src/base/cics/cobol/XFRFUN.cbl:1546` raises RUF3. Existing XFRFUN-Q04, not a second contradiction. |
| 6 | `docs/modernization/01-rediscovery/business-rules.md:84` | AH010 has only two decisions at `docs/modernization/00-source-of-truth/programs/XFRFUN.md:83`; list at `docs/modernization/00-source-of-truth/programs/XFRFUN.md:151` | `src/base/cics/cobol/XFRFUN.cbl:1966`: extra rollback IF exists. Existing XFRFUN-Q01 confirmed. |
| 7 | `docs/modernization/01-rediscovery/integrations.md:233` | WRITE touchpoint at `docs/modernization/00-source-of-truth/programs/ABNDPROC.md:49` | `src/base/cics/cobol/ABNDPROC.cbl:141`: ABNDFILE/record key confirmed. |
| 8 | `docs/modernization/01-rediscovery/business-rules.md:35` | Failure IF at `docs/modernization/00-source-of-truth/programs/ABNDPROC.md:41` | `src/base/cics/cobol/ABNDPROC.cbl:149`: failed write DISPLAYs and RETURNs, not guaranteed persistence: confirmed. |
| 9 | `docs/modernization/01-rediscovery/data-model.md:267` | Count decision at `docs/modernization/00-source-of-truth/programs/CREACC.md:105` | `src/base/cics/cobol/CREACC.cbl:380`: >9, not >=9; N/8 confirmed. |
| 10 | `docs/modernization/01-rediscovery/data-model.md:268` | EVALUATE summary at `docs/modernization/00-source-of-truth/programs/CREACC.md:137` | `src/base/cics/cobol/CREACC.cbl:1309` through `src/base/cics/cobol/CREACC.cbl:1318`: LOAN/OTHER present but not displayed in digest; AUD-Q02. |
| 11 | `docs/modernization/01-rediscovery/data-model.md:270` | Only EVALUATE in CD010 at `docs/modernization/00-source-of-truth/programs/CREACC.md:133` | `src/base/cics/cobol/CREACC.cbl:1250`, `src/base/cics/cobol/CREACC.cbl:1254`, `src/base/cics/cobol/CREACC.cbl:1259`: three nested IFs absent from digest. Existing DM-Q11. |
| 12 | `docs/modernization/01-rediscovery/integrations.md:241` | DL/I touchpoints at `docs/modernization/00-source-of-truth/programs/IBACSUM.md:71` and `docs/modernization/00-source-of-truth/programs/IBACSUM.md:72` | `src/base/ims/cobol/IBACSUM.cbl:212` and `src/base/ims/cobol/IBACSUM.cbl:218`: PCB-selected CUSTACCS traversal and ACCOUNT GHU confirmed. |
| 13 | `docs/modernization/01-rediscovery/integrations.md:207` | CUSTOMER row `docs/modernization/00-source-of-truth/data-access.md:11`; no matching INSERT in `docs/modernization/00-source-of-truth/programs/CRECUST.md:72` | `src/base/cics/cobol/CRECUST.cbl:1219`: CUSTOMER INSERT exists. Existing DM-Q07; control flow **unverified by tooling**. |
| 14 | `docs/modernization/01-rediscovery/integrations.md:89` | False DISPLAY command at `docs/modernization/00-source-of-truth/programs/CRECUST.md:88`; GET missing | `src/base/cics/cobol/CRECUST.cbl:1010` is GET; `src/base/cics/cobol/CRECUST.cbl:1030` is DISPLAY. Existing INT-Q11; control flow **unverified by tooling**. |
| 15 | `docs/modernization/01-rediscovery/data-model.md:61` | DDL is outside parser scope (`docs/modernization/00-source-of-truth/index.md:106`) | `.setup/jcl/cics/Db2-create.j2:30` and `.setup/jcl/cics/Db2-create.j2:35` versus `src/base/cics/copy/ACCDB2.cpy:13` and `src/base/cics/copy/ACCDB2.cpy:18`: precision disagreement confirmed, DM-Q01. |
| 16 | `docs/modernization/01-rediscovery/data-model.md:125` | CONTROL map at `docs/modernization/00-source-of-truth/data-access.md:10` does not settle qualification/nullability | `.setup/jcl/cics/Db2-create.j2:67` versus `src/base/cics/copy/CONTDB2.cpy:7`: BANKZ/nullable versus STTESTER/NOT NULL confirmed, DM-Q02. |
| 17 | `docs/modernization/01-rediscovery/integrations.md:402` | Parser gap at `docs/modernization/00-source-of-truth/programs/BNKSTMTC.md:10`; JCL outside scope | `src/base/batch/jcl/BNKSTMTC.jcl:16` documents SYSOUT; execution DDs at `src/base/batch/jcl/BNKSTMTC.jcl:42` do not allocate it. Existing INT-Q09; runtime allocation unknown, control flow **unverified by tooling**. |

Additional retained source/source check: HISTORY's `BYTES=(56)` at
`src/base/ims/DBD/HISTORY.asm:33` versus the seventh packed-decimal field
at `src/base/ims/cobol/IBTRAN.cbl:95` confirms DM-Q03's layout
disagreement, not a runtime truncation conclusion. IBTRAN control flow
is **unverified by tooling**.

### Sweep disposition

No affirmative rule phrased "appears to", "seems to" or "probably", and no
literal `UNCOVERED` decision-table entry, was found in the audited artifacts.
The only decision table's zero-uncovered result is limited to XFRFUN
(`docs/modernization/01-rediscovery/business-rules.md:84`); AUD-Q04 retains
the broader coverage question. No artifact asserting that a candidate
branch/interface is definitely dead was found; XFRFUN-Q05, INT-Q04,
INT-Q08 and AUD-Q04 retain the relevant confirmation questions.

The artifacts relying on parser-gap programs carry explicit warnings:
`docs/modernization/01-rediscovery/repo-map.md:5`,
`docs/modernization/01-rediscovery/data-model.md:34`,
`docs/modernization/01-rediscovery/integrations.md:9`, and the tooling-gap
section of this file. `business-rules.md` restricts its claims to XFRFUN
and ABNDPROC (`docs/modernization/01-rediscovery/business-rules.md:5`,
`docs/modernization/01-rediscovery/business-rules.md:35`); no missing
parser-gap disclaimer was identified there.

All unknown ownership/production-use classifications roll up to INT-Q01;
utility/support consumers to INT-Q08; reference consumers to INT-Q05;
placeholder authentication to INT-Q06; five JNI pointers to INT-Q03;
and installed OCR routing to ROUTE-Q01. Historical scanner-unresolved
wording in the orientation does not negate the explicitly qualified
manual target inference (`docs/modernization/01-rediscovery/repo-map.md:116`,
`docs/modernization/01-rediscovery/integrations.md:475`).
No legacy sources or generated source-of-truth files were changed.

## Phase 3b - INQACC acceptance-test prerequisite

### INQACC-Q01 - Missing module business-rule and decision-coverage baseline

**Status: baseline supplied; reviewer acceptance and contract questions remain open.**
The INQACC section and Decision coverage table have now been appended to
`business-rules.md`, with INQACC-R01 through R08 and all 11 digest decisions
mapped. An additional source-only decision is recorded in INQACC-Q02.
This supplies the previously missing documentation, not domain approval
or resolution of AUD-Q03, DM-Q01 and DM-Q06. The historical blocker and
test-authoring disposition below describe the earlier phase 3b attempt.
This is the
module-specific instance of AUD-Q04, not a resolution of that broader
coverage question (`docs/modernization/01-rediscovery/open-questions.md:595`).
The requested test-first prompt requires the module's business rules and
Decision coverage table and instructs the author to stop on ambiguity
(`.github/prompts/03b-rewrite-tests.prompt.md:10`,
`.github/prompts/03b-rewrite-tests.prompt.md:21`).
At that attempt, `business-rules.md` contained only the XFRFUN section and its
coverage table, not INQACC rules or rule identifiers
(`docs/modernization/01-rediscovery/business-rules.md:3`,
`docs/modernization/01-rediscovery/business-rules.md:37`).
INQACC's successfully parsed digest lists **11** decision points
(`docs/modernization/00-source-of-truth/programs/INQACC.md:12`,
`docs/modernization/00-source-of-truth/programs/INQACC.md:79`).
ADR-002 explicitly requires complete inquiry rules and review of the
special account-number and blank/low-value account-type outcomes
(`docs/modernization/03a-architecture/target-architecture.md:55`;
source decisions: `src/base/cics/cobol/INQACC.cbl:218`,
`src/base/cics/cobol/INQACC.cbl:229`).

**Question for the domain/CICS reviewer:** Who owns the INQACC rule
baseline, and what are the accepted rule identifiers, expected outcomes
and rule-to-decision mappings for all 11 digest decisions (including any
additional source-only decisions or explicitly justified waivers)?
Supply the INQACC section and Decision coverage table in
`business-rules.md`, explicitly addressing `99999999`, missing accounts,
blank/low-value account types, SQL failures and abend/rollback behavior.
Resolve or explicitly scope the applicable existing AUD-Q03, DM-Q01 and
DM-Q06 contract questions rather than inventing failure meanings,
precision or NULL defaults
(`docs/modernization/01-rediscovery/open-questions.md:579`,
`docs/modernization/01-rediscovery/open-questions.md:561`,
`docs/modernization/01-rediscovery/open-questions.md:566`).

**Historical disposition:** Stopped before creating an acceptance matrix, executable
tests or implementation. No business rules, expected outputs or waivers
were invented. **0 tests authored; 0/11 digest decision points covered by
acceptance tests; 1 new blocking question raised.** This is not a claim
that INQACC has zero business rules or that 11 is a verified exhaustive
source-level decision count. Resume phase 3b after the missing baseline
and applicable contract questions are resolved.

## INQACC business-rule pass

### INQACC-Q02 - CONTRADICTION: rollback-response decision absent from digest

**Status: open; tooling completeness unverified at this branch.** The
digest reports 11 decisions and lists only the abend-code EVALUATE and
storm-drain-flag IF in AH010
(`docs/modernization/00-source-of-truth/programs/INQACC.md:12`,
`docs/modernization/00-source-of-truth/programs/INQACC.md:103`).
The original source additionally contains
`IF WS-CICS-RESP NOT = DFHRESP(NORMAL)` after `SYNCPOINT ROLLBACK`
(`src/base/cics/cobol/INQACC.cbl:682`,
`src/base/cics/cobol/INQACC.cbl:687`).
Its failure arm attempts recording and abends HROL; its normal arm
continues to `INQACC-SUCCESS='N'` and RETURN
(`src/base/cics/cobol/INQACC.cbl:738`,
`src/base/cics/cobol/INQACC.cbl:748`,
`src/base/cics/cobol/INQACC.cbl:756`).

**Question:** Does the tooling maintainer confirm this omitted IF, and
what evidence is accepted for its two outcomes? INQACC-R08 preserves
the source behavior. Coverage distinguishes 11 digest decisions from
12 source-observed decisions; generated files were not hand-corrected.

### INQACC-Q03 - Meaning of success with unchanged account fields

**Status: open; result-contract interpretation stopped.** Ordinary
FETCH `+100` initializes internal OUTPUT-DATA and exits FETCH, but the
blank-type gate then sets only `INQACC-SUCCESS='Y'`
(`src/base/cics/cobol/INQACC.cbl:450`,
`src/base/cics/cobol/INQACC.cbl:452`,
`src/base/cics/cobol/INQACC.cbl:456`,
`src/base/cics/cobol/INQACC.cbl:229`).
Successful rows with all-spaces or all-low-values types take the same
gate, including the highest-account path
(`src/base/cics/cobol/INQACC.cbl:542`,
`src/base/cics/cobol/INQACC.cbl:949`).
In contrast, highest-account SELECT `+100` enters HNCS handling
(`src/base/cics/cobol/INQACC.cbl:874`,
`src/base/cics/cobol/INQACC.cbl:932`).

**Question:** Are these distinct outcomes intended, which callers depend
on unchanged incoming fields, and how should a caller distinguish a
missing/blank-type account from a populated success? Are blank and
low-values types permitted or externally prevented? INQACC-R02 through
R05 preserve the observable assignments without inventing not-found
flags, cleared balances or a type domain. This is the INQACC-specific
instance of the existing AUD-Q03 result-contract question; NULLs are not
equated with spaces/low-values (DM-Q06).

### INQACC-Q04 - Reachability of AFCR/AFCS/AFCT handling in DB2 inquiry

**Status: open; no dead-code conclusion.** The installed abend handler
explicitly recognizes AFCR/AFCS/AFCT, requests rollback and returns
`INQACC-SUCCESS='N'` on a normal rollback response
(`src/base/cics/cobol/INQACC.cbl:210`,
`src/base/cics/cobol/INQACC.cbl:671`,
`src/base/cics/cobol/INQACC.cbl:682`,
`src/base/cics/cobol/INQACC.cbl:756`).
The inquiry reads ACCOUNT through DB2 and its explicit SQL-error abends
use CANCEL
(`src/base/cics/cobol/INQACC.cbl:66`,
`src/base/cics/cobol/INQACC.cbl:335`,
`src/base/cics/cobol/INQACC.cbl:843`,
`src/base/cics/cobol/INQACC.cbl:932`).

**Question:** Which supported runtime invocation can deliver these
VSAM-labelled abends to this handler, and is the resulting N return a
supported caller contract? INQACC-R08 documents conditional behavior
only; no branch is declared dead and no workload storm-drain
configuration is inferred.

### INQACC-Q05 - CONTRADICTION: header claims account-type matching

**Status: open; intended selection contract unknown.** The program
header says it retrieves a row "matching on the account number & the
account_type" (`src/base/cics/cobol/INQACC.cbl:10`).
The ordinary cursor instead predicates on ACCOUNT_SORTCODE and
ACCOUNT_NUMBER only, with the sort-code host set from SORTCODE
(`src/base/cics/cobol/INQACC.cbl:80`,
`src/base/cics/cobol/INQACC.cbl:263`,
`src/base/cics/cobol/INQACC.cbl:265`).
The highest-account query filters only sort code
(`src/base/cics/cobol/INQACC.cbl:869`).

**Question:** Is the header stale, or is account-type restriction supplied
by an external caller contract? INQACC-R01 describes the actual SQL
without treating the comment as executable filtering or inferring
authorization. No source or comment has been changed.
