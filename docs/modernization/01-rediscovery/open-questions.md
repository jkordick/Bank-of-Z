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
