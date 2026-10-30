# Bank of Z: rediscovery orientation

Scope: repository orientation only, not business rules, a data model, or an integration catalogue. The deterministic inventory contains **44 COBOL programs: 32 CICS, 11 IMS, one batch; 39 parsed and five parser gaps** (`docs/modernization/00-source-of-truth/index.md:16`, `docs/modernization/00-source-of-truth/index.md:20`). The program tables below reproduce that inventory exactly; non-COBOL artifacts are listed separately.

**Verification boundary:** entries marked **GAP** have control flow **unverified by tooling**. These are BNK1CCS, BNK1DCS, CRECUST, IBTRAN and BNKSTMTC; their roles below describe source intent or scanned touchpoints, not verified execution paths (`docs/modernization/00-source-of-truth/index.md:20`, `docs/modernization/00-source-of-truth/index.md:25`, `docs/modernization/00-source-of-truth/index.md:28`, `docs/modernization/00-source-of-truth/index.md:38`, `docs/modernization/00-source-of-truth/index.md:58`). Existing limitations are recorded in `docs/modernization/01-rediscovery/open-questions.md:3`.

## COBOL inventory

Each program has a corresponding digest at `docs/modernization/00-source-of-truth/programs/<PROGRAM>.md`; the original-source citations below support its orientation-level role.

### CICS: 32 programs

Inventory authority: `docs/modernization/00-source-of-truth/index.md:21`.

| Program | Role | Original-source evidence |
|---|---|---|
| `ABNDPROC` | Records application abends in a central datastore. | `src/base/cics/cobol/ABNDPROC.cbl:11` |
| `BANKDATA` | Populates banking sample data in DB2. | `src/base/cics/cobol/BANKDATA.cbl:16` |
| `BNK1CAC` | BMS create-account front-end. | `src/base/cics/cobol/BNK1CAC.cbl:10` |
| `BNK1CCA` | Lists accounts for a specified customer. | `src/base/cics/cobol/BNK1CCA.cbl:10` |
| `BNK1CCS` | BMS create-customer front-end. **GAP**. | `src/base/cics/cobol/BNK1CCS.cbl:11` |
| `BNK1CRA` | BMS credit/debit front-end. | `src/base/cics/cobol/BNK1CRA.cbl:11` |
| `BNK1DAC` | BMS display/delete-account front-end. | `src/base/cics/cobol/BNK1DAC.cbl:9` |
| `BNK1DCS` | BMS display/update/delete-customer front-end. **GAP**. | `src/base/cics/cobol/BNK1DCS.cbl:11` |
| `BNK1TFN` | BMS transfer-funds front-end. | `src/base/cics/cobol/BNK1TFN.cbl:11` |
| `BNK1UAC` | BMS update-account front-end. | `src/base/cics/cobol/BNK1UAC.cbl:11` |
| `BNKMENU` | Banking main-menu front-end. | `src/base/cics/cobol/BNKMENU.cbl:11` |
| `CRDTAGY1` | Dummy credit-agency responder. | `src/base/cics/cobol/CRDTAGY1.cbl:8` |
| `CRDTAGY2` | Dummy credit-agency responder. | `src/base/cics/cobol/CRDTAGY2.cbl:8` |
| `CRDTAGY3` | Dummy credit-agency responder. | `src/base/cics/cobol/CRDTAGY3.cbl:8` |
| `CRDTAGY4` | Dummy credit-agency responder. | `src/base/cics/cobol/CRDTAGY4.cbl:8` |
| `CRDTAGY5` | Dummy credit-agency responder. | `src/base/cics/cobol/CRDTAGY5.cbl:8` |
| `CREACC` | Account-creation back-end. | `src/base/cics/cobol/CREACC.cbl:9` |
| `CRECUST` | Customer-creation back-end. **GAP**. | `src/base/cics/cobol/CRECUST.cbl:9` |
| `DBCRFUN` | Cash credit/debit back-end. | `src/base/cics/cobol/DBCRFUN.cbl:10` |
| `DELACC` | Account-deletion back-end. | `src/base/cics/cobol/DELACC.cbl:9` |
| `DELCUS` | Customer-deletion back-end, including associated accounts. | `src/base/cics/cobol/DELCUS.cbl:10` |
| `GETCOMPY` | Returns the sample application's company name. | `src/base/cics/cobol/GETCOMPY.cbl:38` |
| `GETSCODE` | Returns the sort-code copybook value. | `src/base/cics/cobol/GETSCODE.cbl:28`, `src/base/cics/cobol/GETSCODE.cbl:39` |
| `INQACC` | Account-inquiry back-end. | `src/base/cics/cobol/INQACC.cbl:10` |
| `INQACCCU` | Retrieves accounts associated with a customer. | `src/base/cics/cobol/INQACCCU.cbl:9` |
| `INQACCS` | Retrieves the account list. | `src/base/cics/cobol/INQACCS.cbl:10` |
| `INQCUST` | Customer-inquiry back-end. | `src/base/cics/cobol/INQCUST.cbl:10` |
| `INQTRAND` | Retrieves one transaction's details. | `src/base/cics/cobol/INQTRAND.cbl:10` |
| `INQTRANL` | Retrieves an account's transaction list. | `src/base/cics/cobol/INQTRANL.cbl:10` |
| `UPDACC` | Updates account details. | `src/base/cics/cobol/UPDACC.cbl:10` |
| `UPDCUST` | Updates customer details. | `src/base/cics/cobol/UPDCUST.cbl:10` |
| `XFRFUN` | Transfer-funds back-end. | `src/base/cics/cobol/XFRFUN.cbl:11` |

### IMS: 11 programs

Inventory authority: `docs/modernization/00-source-of-truth/index.md:53`.

| Program | Role | Original-source evidence |
|---|---|---|
| `IBACSUM` | Retrieves a customer's account summary. | `src/base/ims/cobol/IBACSUM.cbl:202` |
| `IBGCUDAT` | Retrieves customer data through DL/I. | `src/base/ims/cobol/IBGCUDAT.cbl:178` |
| `IBLOGIN1` | COBOL customer-login handler. | `src/base/ims/cobol/IBLOGIN1.cbl:211` |
| `IBLOGOUT` | Customer-logout handler. | `src/base/ims/cobol/IBLOGOUT.cbl:180` |
| `IBSCUDAT` | Updates customer data through DL/I. | `src/base/ims/cobol/IBSCUDAT.cbl:206`, `src/base/ims/cobol/IBSCUDAT.cbl:255` |
| `IBTRAN` | Account transaction handler with a Java history-writing bridge. **GAP**. | `src/base/ims/cobol/IBTRAN.cbl:33`, `src/base/ims/cobol/IBTRAN.cbl:452` |
| `LOADACCT` | Inserts ACCOUNT data from loader input. | `src/base/ims/cobol/LOADACCT.cbl:18`, `src/base/ims/cobol/LOADACCT.cbl:159` |
| `LOADCUSA` | Inserts CUSTACCS data from loader input. | `src/base/ims/cobol/LOADCUSA.cbl:18`, `src/base/ims/cobol/LOADCUSA.cbl:158` |
| `LOADCUST` | Inserts CUSTOMER data from loader input. | `src/base/ims/cobol/LOADCUST.cbl:18`, `src/base/ims/cobol/LOADCUST.cbl:196` |
| `LOADHIST` | Inserts HISTORY data from loader input. | `src/base/ims/cobol/LOADHIST.cbl:18`, `src/base/ims/cobol/LOADHIST.cbl:175` |
| `LOADTSTA` | Inserts TSTAT data from loader input. | `src/base/ims/cobol/LOADTSTA.cbl:18`, `src/base/ims/cobol/LOADTSTA.cbl:168` |

### Batch: one program

Inventory authority: `docs/modernization/00-source-of-truth/index.md:20`.

| Program | Role | Original-source evidence |
|---|---|---|
| `BNKSTMTC` | Monthly customer-statement report using DB2. **GAP**. | `src/base/batch/cobol/BNKSTMTC.cbl:8` |

## Other artifacts and setup

These are orientation pointers, not parser-verified analysis. Non-COBOL artifacts are outside cobol-rekt's scope (`docs/modernization/00-source-of-truth/index.md:106`).

| Area | Where to look and why | Evidence |
|---|---|---|
| CICS copybooks | `src/base/cics/copy/`: shared interfaces and DB2 declarations. | `src/base/cics/cobol/GETCOMPY.cbl:29`, `src/base/cics/copy/ACCDB2.cpy:7` |
| IMS copybooks | `src/base/ims/copy/`: message layouts and JNI support. | `src/base/ims/copy/IBTRAN.cpy:12`, `src/base/ims/cobol/IBTRAN.cbl:208` |
| BMS maps | `src/base/cics/bms/`: 3270 mapsets; BMS build has separate DSECT and MAP options. | `src/base/cics/bms/BNK1MAI.bms:20`, `.setup/build/languages/BMS.yaml:26`, `.setup/build/languages/BMS.yaml:30` |
| Batch JCL | `src/base/batch/jcl/`: COBOL and PL/I statement-job examples. | `src/base/batch/jcl/BNKSTMTC.jcl:8`, `src/base/batch/jcl/BNKSTMTP.jcl:1` |
| PL/I | `src/base/batch/pli/BNKSTMTP.pli`: monthly statements; `src/base/ims/pli/IBLOGIN.pli`: IMS login implementation. Not additional COBOL inventory entries. | `src/base/batch/pli/BNKSTMTP.pli:12`, `src/base/ims/pli/IBLOGIN.pli:14` |
| IMS DBD/PSB assembler | `src/base/ims/DBD/` defines databases; `src/base/ims/PSB/` defines program views. | `src/base/ims/DBD/ACCOUNT.asm:13`, `src/base/ims/PSB/IBACSUM.asm:10` |
| IMS Java | `src/base/ims/java/`: history controllers, message classes and DB2 access. | `src/base/ims/java/src/main/java/nazare/jmp/controller/IMSBankHistory.java:29`, `src/base/ims/java/src/main/java/nazare/jmp/controller/IMSBankHistory.java:70`, `src/base/ims/java/src/main/java/nazare/jmp/message/InputMessage.java:17` |
| MQ Java | `src/base/mq/java/`: transfer clients for CICS and IMS. | `src/base/mq/java/src/main/java/com/ibm/boq/TransferMoneyToBOZViaCICS.java:32`, `src/base/mq/java/src/main/java/com/ibm/boq/TransferMoneyToBOZViaIMS.java:33` |
| z/OS Connect | `src/api/src/main/api/openapi.yaml`, operation mappings and `zosAssets/` provider definitions; Gradle z/OS Connect plugin. | `src/api/src/main/api/openapi.yaml:1`, `src/api/src/main/zosAssets/IBACSUM/zosAsset.yaml:3`, `src/api/build.gradle:19` |
| DB2 DDL | `.setup/jcl/cics/Db2-create.j2` and `.setup/jcl/ims/Db2-create.j2`; both contain database/table creation templates. | `.setup/jcl/cics/Db2-create.j2:18`, `.setup/jcl/cics/Db2-create.j2:24`, `.setup/jcl/ims/Db2-create.j2:18`, `.setup/jcl/ims/Db2-create.j2:25` |
| Build/deploy | `.setup/build/` DBB lifecycles, `.setup/deploy/` deployment environment, `.setup/zconfig/` resource definitions. | `.setup/build/dbb-build.yaml:39`, `.setup/deploy/Development.yml:11`, `.setup/jcl/ims/Ims-spoc.jcl.j2:56`, `docs/modernization/00-source-of-truth/call-graph.md:149` |
| CI setup | `.github/workflows/PR.yml` calls `Setup.yml`; setup uses self-hosted runners and remote z/OS installation/verification. Do not execute during rediscovery. | `.github/workflows/PR.yml:23`, `.github/workflows/Setup.yml:18`, `.github/workflows/Setup.yml:34`, `.github/workflows/Setup.yml:47` |
| Browser frontend | `src/frontend/`: static HTML and JavaScript; index redirects to `admin.html`; Carbon components load from IBM's CDN. | `src/frontend/index.html:5`, `src/frontend/js/carbon.js:11`, `src/frontend/js/carbon.js:27` |

## Entry points

These are **checked-in definitions**, not evidence that a transaction or endpoint is deployed or used in production.

### CICS screens

The following IDs come from the generated call graph's configuration table (`docs/modernization/00-source-of-truth/call-graph.md:149`). Screen roles are described in the COBOL inventory above. GAP warnings there apply to the corresponding entry points too.

| Transaction | Program | Resource definition |
|---|---|---|
| `OMEN` | BNKMENU | `.setup/zconfig/bank-of-z-definitions.yaml:1028` |
| `OCAC` | BNK1CAC | `.setup/zconfig/bank-of-z-definitions.yaml:842` |
| `OCCA` | BNK1CCA | `.setup/zconfig/bank-of-z-definitions.yaml:873` |
| `OCCS` | BNK1CCS (GAP) | `.setup/zconfig/bank-of-z-definitions.yaml:904` |
| `OCRA` | BNK1CRA | `.setup/zconfig/bank-of-z-definitions.yaml:935` |
| `ODAC` | BNK1DAC | `.setup/zconfig/bank-of-z-definitions.yaml:966` |
| `ODCS` | BNK1DCS (GAP) | `.setup/zconfig/bank-of-z-definitions.yaml:997` |
| `OTFN` | BNK1TFN | `.setup/zconfig/bank-of-z-definitions.yaml:1059` |
| `OUAC` | BNK1UAC | `.setup/zconfig/bank-of-z-definitions.yaml:1090` |

Related non-screen transactions are `OCR1` through `OCR5`, mapped to the dummy agency programs (`docs/modernization/00-source-of-truth/call-graph.md:173`). CRECUST's dynamically built `RUN TRANSID` remains unresolved by the scanner; do not treat these configured IDs as proof of that runtime target (`src/base/cics/cobol/CRECUST.cbl:687`, `docs/modernization/01-rediscovery/open-questions.md:20`). The graph also lists debug/socket-support transactions separately from the bank's screen IDs (`docs/modernization/00-source-of-truth/call-graph.md:155`).

### IMS transactions

| Transaction | Configured program | Definition |
|---|---|---|
| `IBLOGIN` | IBLOGIN | `.setup/jcl/ims/Ims-spoc.jcl.j2:56` |
| `IBLOGOUT` | IBLOGOUT | `.setup/jcl/ims/Ims-spoc.jcl.j2:58` |
| `IBTRAN` | IBTRAN (COBOL GAP) | `.setup/jcl/ims/Ims-spoc.jcl.j2:60` |
| `IBACSUM` | IBACSUM | `.setup/jcl/ims/Ims-spoc.jcl.j2:62` |
| `IBGCUDAT` | IBGCUDAT | `.setup/jcl/ims/Ims-spoc.jcl.j2:64` |
| `IBSCUDAT` | IBSCUDAT | `.setup/jcl/ims/Ims-spoc.jcl.j2:66` |
| `IBGHIST` | IBGHIST (defined as Java) | `.setup/jcl/ims/Ims-spoc.jcl.j2:44`, `.setup/jcl/ims/Ims-spoc.jcl.j2:68` |

Keep the names distinct: the configuration says `IBLOGIN`, the COBOL inventory says `IBLOGIN1`, and the PL/I source declares `IBLOGIN`; this map does not equate the configured name with the COBOL member (`.setup/jcl/ims/Ims-spoc.jcl.j2:56`, `src/base/ims/cobol/IBLOGIN1.cbl:3`, `src/base/ims/pli/IBLOGIN.pli:14`).

### REST operations

The OpenAPI server base path is `/api` (`src/api/src/main/api/openapi.yaml:25`). Below are the **21 checked-in operation mappings** under `src/api/src/main/operations/`; paths are decoded from their directory names. A zasset name is a mapping target, not a deployment guarantee. CRECUST and IBTRAN retain the control-flow verification gaps noted above.

| Method and path (relative to `/api`) | zasset | Operation mapping |
|---|---|---|
| GET `/accounts` | INQACCS | `src/api/src/main/operations/%2Faccounts/get/operation.yaml:3` |
| POST `/accounts` | CREACC | `src/api/src/main/operations/%2Faccounts/post/operation.yaml:3` |
| GET `/accounts/{accountId}` | INQACC | `src/api/src/main/operations/%2Faccounts%2F%7BaccountId%7D/get/operation.yaml:3` |
| PUT `/accounts/{accountId}` | UPDACC | `src/api/src/main/operations/%2Faccounts%2F%7BaccountId%7D/put/operation.yaml:3` |
| DELETE `/accounts/{accountId}` | DELACC | `src/api/src/main/operations/%2Faccounts%2F%7BaccountId%7D/delete/operation.yaml:3` |
| GET `/accounts/{accountId}/balances` | INQACC | `src/api/src/main/operations/%2Faccounts%2F%7BaccountId%7D%2Fbalances/get/operation.yaml:3` |
| POST `/accounts/{accountId}/deposit` | DBCRFUN | `src/api/src/main/operations/%2Faccounts%2F%7BaccountId%7D%2Fdeposit/post/operation.yaml:3` |
| POST `/accounts/{accountId}/transfer` | XFRFUN | `src/api/src/main/operations/%2Faccounts%2F%7BaccountId%7D%2Ftransfer/post/operation.yaml:3` |
| GET `/accounts/{accountId}/transactions` | INQTRANL | `src/api/src/main/operations/%2Faccounts%2F%7BaccountId%7D%2Ftransactions/get/operation.yaml:3` |
| GET `/accounts/{accountId}/transactions/{transactionId}` | INQTRAND | `src/api/src/main/operations/%2Faccounts%2F%7BaccountId%7D%2Ftransactions%2F%7BtransactionId%7D/get/operation.yaml:3` |
| POST `/customers` | CRECUST (GAP) | `src/api/src/main/operations/%2Fcustomers/post/operation.yaml:3` |
| GET `/customers/{customerId}` | INQCUST | `src/api/src/main/operations/%2Fcustomers%2F%7BcustomerId%7D/get/operation.yaml:3` |
| PUT `/customers/{customerId}` | UPDCUST | `src/api/src/main/operations/%2Fcustomers%2F%7BcustomerId%7D/put/operation.yaml:3` |
| DELETE `/customers/{customerId}` | DELCUS | `src/api/src/main/operations/%2Fcustomers%2F%7BcustomerId%7D/delete/operation.yaml:3` |
| GET `/customers/{customerId}/accounts` | INQACCCU | `src/api/src/main/operations/%2Fcustomers%2F%7BcustomerId%7D%2Faccounts/get/operation.yaml:3` |
| GET `/ims/accounts/{accountId}` | IBACSUM | `src/api/src/main/operations/%2Fims%2Faccounts%2F%7BaccountId%7D/get/operation.yaml:3` |
| GET `/ims/accounts/{accountId}/balances` | IBACSUM | `src/api/src/main/operations/%2Fims%2Faccounts%2F%7BaccountId%7D%2Fbalances/get/operation.yaml:3` |
| POST `/ims/accounts/{customerId}/{accountId}/deposit` | IBTRAN (GAP) | `src/api/src/main/operations/%2Fims%2Faccounts%2F%7BcustomerId%7D%2F%7BaccountId%7D%2Fdeposit/post/operation.yaml:3` |
| GET `/ims/customers/{customerId}` | IBGCUDAT | `src/api/src/main/operations/%2Fims%2Fcustomers%2F%7BcustomerId%7D/get/operation.yaml:3` |
| PUT `/ims/customers/{customerId}` | IBSCUDAT | `src/api/src/main/operations/%2Fims%2Fcustomers%2F%7BcustomerId%7D/put/operation.yaml:3` |
| GET `/ims/customers/{customerId}/accounts` | IBACSUM | `src/api/src/main/operations/%2Fims%2Fcustomers%2F%7BcustomerId%7D%2Faccounts/get/operation.yaml:3` |

## Unusual and generated material

- **Generated provider interfaces:** `src/api/**/providerFiles/gen/` contains copybooks such as `src/api/src/main/zosAssets/IBACSUM/providerFiles/gen/INPUT-AREA_request_0.cpy:1`. Repeated generated member names are explicitly recorded by the scanner (`docs/modernization/00-source-of-truth/index.md:96`). They are not additional COBOL programs.
- **Duplicate copybook names:** `INQACCSZ` and `XFRFUN` occur in both base and API trees; the base `copy/` members win for analysis. Generic `COMMAREA` and generated input/output names also collide across API assets; the scanner's selected member is not evidence that those interfaces are interchangeable (`docs/modernization/00-source-of-truth/index.md:88`, `docs/modernization/00-source-of-truth/index.md:94`, `docs/modernization/00-source-of-truth/index.md:102`).
- **Missing/generated members:** BMS symbolic copybooks and IBM-supplied members were stubbed for parsing; their absent fields are not verified. Do not invent replacements (`docs/modernization/00-source-of-truth/index.md:67`). The build's DSECT option is visible at `.setup/build/languages/BMS.yaml:26`.
- **Excluded map, not proven dead code:** `BNK1B2M.bms` internally declares mapset `BNK1TFM`; deployment explicitly excludes `BNK1B2M` as a temporary map. This is a concrete exclusion, not proof about all runtime usage (`src/base/cics/bms/BNK1B2M.bms:21`, `.setup/deploy/Development.yml:16`).
- **Third-party UI/build material:** the frontend includes Carbon styles and loads Carbon components from IBM's CDN; DBB language configurations contain IBM sample-configuration instructions. Do not treat these as banking COBOL (`src/frontend/README.md:8`, `src/frontend/README.md:105`, `src/frontend/js/carbon.js:11`, `.setup/build/dbb-build.yaml:14`). No directory is labelled dead solely because it contains placeholders or lacks a scanned caller.

## Suggested reading order

| Priority | Subsystem/slice | Reason |
|---|---|---|
| First | CICS menu and account/customer inquiry | A visible screen entry and parsed inquiry programs provide a tool-checkable starting point (`src/base/cics/cobol/BNKMENU.cbl:11`, `docs/modernization/00-source-of-truth/index.md:31`, `docs/modernization/00-source-of-truth/index.md:44`). |
| Next | Parsed CICS updates/transfers, then IMS inquiry and loaders | Extend into parsed write paths and DL/I programs before tackling tooling gaps (`docs/modernization/00-source-of-truth/index.md:50`, `docs/modernization/00-source-of-truth/index.md:52`, `docs/modernization/00-source-of-truth/index.md:53`, `docs/modernization/00-source-of-truth/index.md:59`). |
| Worst verification burden | CICS customer creation/maintenance and IMS transaction/Java bridge | BNK1CCS, BNK1DCS, CRECUST and IBTRAN have control flow unverified by tooling; CRECUST also has an unresolved transaction target (`docs/modernization/00-source-of-truth/index.md:25`, `docs/modernization/00-source-of-truth/index.md:28`, `docs/modernization/00-source-of-truth/index.md:38`, `docs/modernization/00-source-of-truth/index.md:58`, `src/base/cics/cobol/CRECUST.cbl:687`). |
| Last | Batch statements, with the PL/I/JCL companion artifacts | BNKSTMTC's control flow is unverified by tooling, and PL/I/JCL require direct reading rather than parser-backed verification (`docs/modernization/00-source-of-truth/index.md:20`, `docs/modernization/00-source-of-truth/index.md:106`, `src/base/batch/pli/BNKSTMTP.pli:12`, `src/base/batch/jcl/BNKSTMTC.jcl:8`). |
