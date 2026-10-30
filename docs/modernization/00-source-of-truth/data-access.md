# Data access map [scan]

> Generated — do not edit. Which program touches which data store, taken from `EXEC SQL`, `EXEC CICS` and IMS DL/I `CALL`s in the original sources. Use it to check `data-model.md` and `integrations.md`.

## DB2 tables (EXEC SQL)

| Table | Programs (verb) |
|---|---|
| `ACCOUNT` | `BANKDATA` (DELETE), `BANKDATA` (INSERT), `BNKSTMTC` (cursor ACCT-CURSOR, as BANKZ.ACCOUNT), `CREACC` (INSERT), `DBCRFUN` (SELECT), `DBCRFUN` (UPDATE), `DELACC` (DELETE), `DELACC` (SELECT), `INQACC` (SELECT), `INQACC` (cursor ACC-CURSOR), `INQACCCU` (cursor ACC-CURSOR), `INQACCS` (cursor ACC-ALL-CURSOR), `UPDACC` (SELECT), `UPDACC` (UPDATE), `XFRFUN` (SELECT), `XFRFUN` (UPDATE) |
| `CONTROL` | `BANKDATA` (DELETE), `BANKDATA` (INSERT), `BANKDATA` (UPDATE), `CREACC` (SELECT), `CREACC` (UPDATE), `CRECUST` (SELECT), `CRECUST` (UPDATE) |
| `CUSTOMER` | `BANKDATA` (DELETE), `BANKDATA` (INSERT), `BNKSTMTC` (SELECT, as BANKZ.CUSTOMER), `DELCUS` (DELETE), `DELCUS` (SELECT), `INQCUST` (SELECT), `UPDCUST` (SELECT), `UPDCUST` (UPDATE) |
| `PROCTRAN` | `BNKSTMTC` (cursor TRAN-CURSOR, as BANKZ.PROCTRAN), `CREACC` (INSERT), `CRECUST` (INSERT), `DBCRFUN` (INSERT), `DELACC` (INSERT), `DELCUS` (INSERT), `INQTRAND` (cursor TRAND-CURSOR), `INQTRANL` (cursor TRAN-COUNT-CURSOR), `INQTRANL` (cursor TRAN-CURSOR), `XFRFUN` (INSERT) |

## CICS files / VSAM datasets (EXEC CICS)

| File | Programs (verb) |
|---|---|
| `ABNDFILE` | `ABNDPROC` (WRITE) |

## Queues (EXEC CICS)

_None found._

## IMS DL/I calls

| Program | Function argument(s) |
|---|---|
| `IBACSUM` | `GHU`, `GU`, `ISRT`, `NEXT-CALL` |
| `IBGCUDAT` | `GU`, `ISRT` |
| `IBLOGIN1` | `GHU`, `GU`, `ISRT`, `REPL` |
| `IBLOGOUT` | `GHU`, `GU`, `ISRT`, `REPL` |
| `IBSCUDAT` | `GHU`, `GU`, `ISRT`, `REPL` |
| `IBTRAN` | `GHU`, `GU`, `ISRT`, `NEXT-CALL`, `REPL` |
| `LOADACCT` | `ISRT` |
| `LOADCUSA` | `ISRT` |
| `LOADCUST` | `ISRT` |
| `LOADHIST` | `ISRT` |
| `LOADTSTA` | `ISRT` |
