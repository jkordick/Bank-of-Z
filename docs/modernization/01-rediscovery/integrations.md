# Integration inventory

## Evidence and classification

This pass inventories checked-in interfaces, not an observed deployment. **Production usage is unknown for every entry.** `L?` means a load-bearing application/runtime candidate, not proven production use; `legacy-only` means an explicitly identified loader, setup, test or development artifact; `unknown` means even that classification is not established. These distinctions must not be used to retire an interface. See INT-Q01 in `open-questions.md`.

Direction is relative to the named program/application: **in** consumes input, **out** emits data or initiates a hand-off, **both** exchanges a request and response. For stores, reads are in, writes are out, and read/write is both. Calls within Bank of Z still cross a program boundary and are included.

**Control flow is unverified by tooling for BNK1CCS, BNK1DCS, CRECUST, IBTRAN and BNKSTMTC.** All references to these programs below retain that qualification, including API, MQ, batch and reconciliation entries. Their scanned statements do not prove execution paths (`docs/modernization/00-source-of-truth/index.md:16`). The program digests, call graph and data-access map were consulted; original source remains the authority. Non-COBOL/configuration extensions are explicitly marked **outside tool scope**; source-scanner omissions are distinguished from scope exclusions.

Entry IDs below are the targets of the final reconciliation.

## CICS transactions and terminal hand-offs

### C01 - Screen transactions

Direction **in** from the terminal and **out** on `RETURN TRANSID`; status **L?** throughout. `RETURN TRANSID` is documented as a next-transaction hand-off, not a synchronous program call. The resource definition, not the transaction's spelling, establishes the destination program. The configuration mappings are also present as [config] in `docs/modernization/00-source-of-truth/call-graph.md:170`.

| Transaction -> configured program | RETURN TRANSID sources and statement starts | Resource evidence |
|---|---|---|
| `OMEN` -> `BNKMENU` | `BNKMENU` self: `src/base/cics/cobol/BNKMENU.cbl:170`; returns from `BNK1CAC`: `src/base/cics/cobol/BNK1CAC.cbl:186`, `BNK1CCA`: `src/base/cics/cobol/BNK1CCA.cbl:202`, `BNK1CCS`: `src/base/cics/cobol/BNK1CCS.cbl:223`, `BNK1CRA`: `src/base/cics/cobol/BNK1CRA.cbl:201`, `BNK1DAC`: `src/base/cics/cobol/BNK1DAC.cbl:214`, `BNK1DCS`: `src/base/cics/cobol/BNK1DCS.cbl:260`, `BNK1TFN`: `src/base/cics/cobol/BNK1TFN.cbl:198`, `BNK1UAC`: `src/base/cics/cobol/BNK1UAC.cbl:222` | `.setup/zconfig/bank-of-z-definitions.yaml:1028`, program at `.setup/zconfig/bank-of-z-definitions.yaml:1032` |
| `OCAC` -> `BNK1CAC` | Self: `src/base/cics/cobol/BNK1CAC.cbl:248`; menu: `src/base/cics/cobol/BNKMENU.cbl:605` | `.setup/zconfig/bank-of-z-definitions.yaml:842`, `.setup/zconfig/bank-of-z-definitions.yaml:846` |
| `OCCA` -> `BNK1CCA` | Self: `src/base/cics/cobol/BNK1CCA.cbl:249`; menu: `src/base/cics/cobol/BNKMENU.cbl:898` | `.setup/zconfig/bank-of-z-definitions.yaml:873`, `.setup/zconfig/bank-of-z-definitions.yaml:877` |
| `OCCS` -> `BNK1CCS` | Self, both statements: `src/base/cics/cobol/BNK1CCS.cbl:264`, `src/base/cics/cobol/BNK1CCS.cbl:291`; menu: `src/base/cics/cobol/BNKMENU.cbl:532` | `.setup/zconfig/bank-of-z-definitions.yaml:904`, `.setup/zconfig/bank-of-z-definitions.yaml:908` |
| `OCRA` -> `BNK1CRA` | Self: `src/base/cics/cobol/BNK1CRA.cbl:263`; menu: `src/base/cics/cobol/BNKMENU.cbl:751` | `.setup/zconfig/bank-of-z-definitions.yaml:935`, `.setup/zconfig/bank-of-z-definitions.yaml:939` |
| `ODAC` -> `BNK1DAC` | Self: `src/base/cics/cobol/BNK1DAC.cbl:296`; menu: `src/base/cics/cobol/BNKMENU.cbl:459` | `.setup/zconfig/bank-of-z-definitions.yaml:966`, `.setup/zconfig/bank-of-z-definitions.yaml:970` |
| `ODCS` -> `BNK1DCS` | Self: `src/base/cics/cobol/BNK1DCS.cbl:364`; menu: `src/base/cics/cobol/BNKMENU.cbl:387` | `.setup/zconfig/bank-of-z-definitions.yaml:997`, `.setup/zconfig/bank-of-z-definitions.yaml:1001` |
| `OTFN` -> `BNK1TFN` | Self: `src/base/cics/cobol/BNK1TFN.cbl:247`; menu: `src/base/cics/cobol/BNKMENU.cbl:824` | `.setup/zconfig/bank-of-z-definitions.yaml:1059`, `.setup/zconfig/bank-of-z-definitions.yaml:1063` |
| `OUAC` -> `BNK1UAC` | Self: `src/base/cics/cobol/BNK1UAC.cbl:295`; menu: `src/base/cics/cobol/BNKMENU.cbl:678` | `.setup/zconfig/bank-of-z-definitions.yaml:1090`, `.setup/zconfig/bank-of-z-definitions.yaml:1094` |

### C02 - Synchronous business-program LINKs

Direction **both**, status **L?**, through the COMMAREA at each cited call. Trailing spaces in literal program names are padding, not separate targets.

| Caller -> target(s) | Original-source statement starts |
|---|---|
| `BNK1CAC` -> `CREACC` | `src/base/cics/cobol/BNK1CAC.cbl:775` |
| `BNK1CCA` -> `INQACCCU` | `src/base/cics/cobol/BNK1CCA.cbl:459`; target VALUE at `src/base/cics/cobol/BNK1CCA.cbl:108` |
| `BNK1CCS` -> `CRECUST` | `src/base/cics/cobol/BNK1CCS.cbl:997` |
| `BNK1CRA` -> `DBCRFUN` | `src/base/cics/cobol/BNK1CRA.cbl:511` |
| `BNK1DAC` -> `INQACC`, `DELACC` | `src/base/cics/cobol/BNK1DAC.cbl:554`, `src/base/cics/cobol/BNK1DAC.cbl:713` |
| `BNK1DCS` -> `INQCUST`, `DELCUS`, `UPDCUST` | `src/base/cics/cobol/BNK1DCS.cbl:866`, `src/base/cics/cobol/BNK1DCS.cbl:1011`, `src/base/cics/cobol/BNK1DCS.cbl:1216` |
| `BNK1TFN` -> `XFRFUN` | `src/base/cics/cobol/BNK1TFN.cbl:496` |
| `BNK1UAC` -> `INQACC`, `UPDACC` | `src/base/cics/cobol/BNK1UAC.cbl:767`, `src/base/cics/cobol/BNK1UAC.cbl:954` |
| `CREACC` -> `INQCUST`, `INQACCCU` | `src/base/cics/cobol/CREACC.cbl:327`, `src/base/cics/cobol/CREACC.cbl:1182` |
| `DELCUS` -> `INQCUST`, `DELACC`, `INQACCCU` | `src/base/cics/cobol/DELCUS.cbl:303`, `src/base/cics/cobol/DELCUS.cbl:378`, `src/base/cics/cobol/DELCUS.cbl:409`; INQCUST VALUE at `src/base/cics/cobol/DELCUS.cbl:256` |
| `INQACCCU` -> `INQCUST` | `src/base/cics/cobol/INQACCCU.cbl:851` |

### C03 - ABNDPROC diagnostic LINKs

Direction **out** diagnostics / **in** return from LINK; status **L?**, except the agency callers are dummy responders (C04). Each row covers **all** graph edges for that caller to `ABNDPROC`, including repeated statements. The additional line-number list is within the same cited source file; the VALUE citation proves the variable target.

| Caller | LINK statement line numbers | Direct LINK evidence; ABNDPROC VALUE evidence |
|---|---|---|
| `BNK1CAC` | 309, 416, 834, 1027, 1104, 1182, 1260 | `src/base/cics/cobol/BNK1CAC.cbl:309`, `src/base/cics/cobol/BNK1CAC.cbl:147` |
| `BNK1CCA` | 304, 413, 514, 702, 778, 857, 936 | `src/base/cics/cobol/BNK1CCA.cbl:304`, `src/base/cics/cobol/BNK1CCA.cbl:148` |
| `BNK1CCS` | 346, 477, 574, 1052, 1203, 1286, 1365, 1444, 1523, 1603 | `src/base/cics/cobol/BNK1CCS.cbl:346`, `src/base/cics/cobol/BNK1CCS.cbl:162` |
| `BNK1CRA` | 318, 425, 566, 705, 780, 856, 934 | `src/base/cics/cobol/BNK1CRA.cbl:318`, `src/base/cics/cobol/BNK1CRA.cbl:164` |
| `BNK1DAC` | 351, 482, 609, 884, 964, 1040, 1119 | `src/base/cics/cobol/BNK1DAC.cbl:351`, `src/base/cics/cobol/BNK1DAC.cbl:161` |
| `BNK1DCS` | 419, 616, 691, 921, 1066, 1271, 1566, 1644, 1723, 1825, 1912, 1993 | `src/base/cics/cobol/BNK1DCS.cbl:419`, `src/base/cics/cobol/BNK1DCS.cbl:184` |
| `BNK1TFN` | 302, 411, 551, 728, 803, 879, 957 | `src/base/cics/cobol/BNK1TFN.cbl:302`, `src/base/cics/cobol/BNK1TFN.cbl:160` |
| `BNK1UAC` | 350, 474, 822, 1009, 1131, 1208, 1285, 1364 | `src/base/cics/cobol/BNK1UAC.cbl:350`, `src/base/cics/cobol/BNK1UAC.cbl:172` |
| `BNKMENU` | 225, 337, 441, 513, 586, 659, 732, 805, 878, 952, 1035, 1111, 1189, 1268 | `src/base/cics/cobol/BNKMENU.cbl:225`, `src/base/cics/cobol/BNKMENU.cbl:96` |
| `CRDTAGY1` | 166 | `src/base/cics/cobol/CRDTAGY1.cbl:166`, `src/base/cics/cobol/CRDTAGY1.cbl:90` |
| `CRDTAGY2` | 165 | `src/base/cics/cobol/CRDTAGY2.cbl:165`, `src/base/cics/cobol/CRDTAGY2.cbl:89` |
| `CRDTAGY3` | 165 | `src/base/cics/cobol/CRDTAGY3.cbl:165`, `src/base/cics/cobol/CRDTAGY3.cbl:89` |
| `CRDTAGY4` | 166 | `src/base/cics/cobol/CRDTAGY4.cbl:166`, `src/base/cics/cobol/CRDTAGY4.cbl:89` |
| `CRDTAGY5` | 165 | `src/base/cics/cobol/CRDTAGY5.cbl:165`, `src/base/cics/cobol/CRDTAGY5.cbl:88` |
| `CREACC` | 564, 655, 753, 839, 1149 | `src/base/cics/cobol/CREACC.cbl:564`, `src/base/cics/cobol/CREACC.cbl:291` |
| `CRECUST` | 1441 | `src/base/cics/cobol/CRECUST.cbl:1441`, `src/base/cics/cobol/CRECUST.cbl:137` |
| `DBCRFUN` | 628, 815 | `src/base/cics/cobol/DBCRFUN.cbl:628`, `src/base/cics/cobol/DBCRFUN.cbl:185` |
| `DELACC` | 603 | `src/base/cics/cobol/DELACC.cbl:603`, `src/base/cics/cobol/DELACC.cbl:193` |
| `DELCUS` | 535, 691, 859 | `src/base/cics/cobol/DELCUS.cbl:535`, `src/base/cics/cobol/DELCUS.cbl:267` |
| `INQACC` | 321, 401, 516, 738, 810, 924 | `src/base/cics/cobol/INQACC.cbl:321`, `src/base/cics/cobol/INQACC.cbl:193` |
| `INQACCCU` | 321, 424, 563, 794 | `src/base/cics/cobol/INQACCCU.cbl:321`, `src/base/cics/cobol/INQACCCU.cbl:184` |
| `INQCUST` | 506, 628 | `src/base/cics/cobol/INQCUST.cbl:506`, `src/base/cics/cobol/INQCUST.cbl:185` |
| `INQTRAND` | 334, 382 | `src/base/cics/cobol/INQTRAND.cbl:334`, `src/base/cics/cobol/INQTRAND.cbl:90` |
| `INQTRANL` | 557, 605 | `src/base/cics/cobol/INQTRANL.cbl:557`, `src/base/cics/cobol/INQTRANL.cbl:117` |
| `XFRFUN` | 364, 463, 573, 664, 737, 813, 881, 1165, 1262, 1341, 1459, 1541, 1709, 1846, 2018 | `src/base/cics/cobol/XFRFUN.cbl:364`, `src/base/cics/cobol/XFRFUN.cbl:257` |

ABNDPROC's datastore and failure behavior are in S05; operational codes are in O01.

### C04 - Asynchronous credit-agency hand-off

Direction **both**, status **legacy-only dummy/test responders**, although CRECUST contains a banking-path dependency on them. This is not evidence of a real credit-bureau network integration. All five programs identify themselves as dummy agencies (`src/base/cics/cobol/CRDTAGY1.cbl:8`, `src/base/cics/cobol/CRDTAGY2.cbl:8`, `src/base/cics/cobol/CRDTAGY3.cbl:8`, `src/base/cics/cobol/CRDTAGY4.cbl:8`, `src/base/cics/cobol/CRDTAGY5.cbl:8`).

CRECUST sets channel `CIPCREDCHANN`, loops over 1..5, constructs `OCR` plus the one-digit counter, puts the COMMAREA in a container, and issues `RUN TRANSID` with a child token (`src/base/cics/cobol/CRECUST.cbl:615`, `src/base/cics/cobol/CRECUST.cbl:620`, `src/base/cics/cobol/CRECUST.cbl:626`, `src/base/cics/cobol/CRECUST.cbl:656`, `src/base/cics/cobol/CRECUST.cbl:687`). It delays three seconds and fetches children with `NOSUSPEND`, completion status and abend code; returned data is read from the child's channel (`src/base/cics/cobol/CRECUST.cbl:733`, `src/base/cics/cobol/CRECUST.cbl:749`, `src/base/cics/cobol/CRECUST.cbl:1010`).

| Constructed transaction -> configured program | Container chosen by parent | Resource definition proving program |
|---|---|---|
| `OCR1` -> `CRDTAGY1` | `CIPA`: `src/base/cics/cobol/CRECUST.cbl:633` | `.setup/zconfig/bank-of-z-definitions.yaml:688`, `.setup/zconfig/bank-of-z-definitions.yaml:691` |
| `OCR2` -> `CRDTAGY2` | `CIPB`: `src/base/cics/cobol/CRECUST.cbl:635` | `.setup/zconfig/bank-of-z-definitions.yaml:719`, `.setup/zconfig/bank-of-z-definitions.yaml:722` |
| `OCR3` -> `CRDTAGY3` | `CIPC`: `src/base/cics/cobol/CRECUST.cbl:637` | `.setup/zconfig/bank-of-z-definitions.yaml:750`, `.setup/zconfig/bank-of-z-definitions.yaml:753` |
| `OCR4` -> `CRDTAGY4` | `CIPD`: `src/base/cics/cobol/CRECUST.cbl:639` | `.setup/zconfig/bank-of-z-definitions.yaml:781`, `.setup/zconfig/bank-of-z-definitions.yaml:784` |
| `OCR5` -> `CRDTAGY5` | `CIPE`: `src/base/cics/cobol/CRECUST.cbl:641` | `.setup/zconfig/bank-of-z-definitions.yaml:812`, `.setup/zconfig/bank-of-z-definitions.yaml:815` |

The resulting OCR1..OCR5 set is a **manual inference from the proven STRING and loop**, not a parser-resolved edge or proof of installed transactions. Each agency GETs and PUTs its container: `src/base/cics/cobol/CRDTAGY1.cbl:178`, `src/base/cics/cobol/CRDTAGY1.cbl:212`; `src/base/cics/cobol/CRDTAGY2.cbl:179`, `src/base/cics/cobol/CRDTAGY2.cbl:212`; `src/base/cics/cobol/CRDTAGY3.cbl:177`, `src/base/cics/cobol/CRDTAGY3.cbl:211`; `src/base/cics/cobol/CRDTAGY4.cbl:180`, `src/base/cics/cobol/CRDTAGY4.cbl:214`; `src/base/cics/cobol/CRDTAGY5.cbl:178`, `src/base/cics/cobol/CRDTAGY5.cbl:214`. CRECUST control flow remains **unverified by tooling**.

### C05 - Language Environment calls

| Caller -> service | Direction; status | Evidence |
|---|---|---|
| `BANKDATA` -> `CEEGMT`, `CEEDATM` | both; legacy-only sample-data setup | `src/base/cics/cobol/BANKDATA.cbl:1876`, `src/base/cics/cobol/BANKDATA.cbl:1892`; setup invocation `.setup/jcl/cics/Db2-insert.j2:10` |
| `CRECUST` -> `CEEDAYS`, `CEELOCT` | both; L? | `src/base/cics/cobol/CRECUST.cbl:1570`, `src/base/cics/cobol/CRECUST.cbl:1585` |
| `IBTRAN` -> `__etoa` (graph `__ETOA`) | both; L?; EBCDIC-to-ASCII conversion before JNI lookups | `src/base/ims/cobol/IBTRAN.cbl:519`, `src/base/ims/cobol/IBTRAN.cbl:546`, `src/base/ims/cobol/IBTRAN.cbl:548` |

### C06 - COMMAREA utility entry points and configuration-only program

Direction **both**, status **unknown**. GETCOMPY returns the company name, GETSCODE returns the sort-code literal; a callable interface does not prove a caller (`src/base/cics/cobol/GETCOMPY.cbl:35`, `src/base/cics/cobol/GETCOMPY.cbl:38`, `src/base/cics/cobol/GETSCODE.cbl:35`, `src/base/cics/cobol/GETSCODE.cbl:39`). These entry points have no incoming edge in the generated graph; their purpose/usage beyond these literal results is INT-Q08.

**Outside tool scope:** a configured `BNK1B2B` program is labelled "Bank to Bank Transfer" (`.setup/zconfig/bank-of-z-definitions.yaml:180`, `.setup/zconfig/bank-of-z-definitions.yaml:182`). Its implementation and caller are not established by that resource. Do not equate it with XFRFUN or the MQ partner path (INT-Q08).

### C07 - 3270 BMS interfaces (outside tool scope)

Direction **both**, status **L?** for the nine screen pairs. Map definitions are not parsed COBOL. The COBOL SEND/RECEIVE commands are visible in digests, but the BMS layouts below require direct evidence.

| Program | Mapset / map | Layout and receive/send evidence |
|---|---|---|
| `BNKMENU` | `BNK1MAI` / `BNK1ME` | `src/base/cics/bms/BNK1MAI.bms:20`, `src/base/cics/bms/BNK1MAI.bms:24`; `src/base/cics/cobol/BNKMENU.cbl:275`, `src/base/cics/cobol/BNKMENU.cbl:979` |
| `BNK1CAC` | `BNK1CAM` / `BNK1CA` | `src/base/cics/bms/BNK1CAM.bms:20`; `src/base/cics/cobol/BNK1CAC.cbl:357`, `src/base/cics/cobol/BNK1CAC.cbl:966` |
| `BNK1CCA` | `BNK1ACC` / `BNK1ACC` | `src/base/cics/bms/BNK1ACC.bms:22`; `src/base/cics/cobol/BNK1CCA.cbl:358`, `src/base/cics/cobol/BNK1CCA.cbl:646` |
| `BNK1CCS` | `BNK1CCM` / `BNK1CC` | `src/base/cics/bms/BNK1CCM.bms:22`; `src/base/cics/cobol/BNK1CCS.cbl:518`, `src/base/cics/cobol/BNK1CCS.cbl:1308` |
| `BNK1CRA` | `BNK1CDM` / `BNK1CD` | `src/base/cics/bms/BNK1CDM.bms:21`; `src/base/cics/cobol/BNK1CRA.cbl:371`, `src/base/cics/cobol/BNK1CRA.cbl:649` |
| `BNK1DAC` | `BNK1DAM` / `BNK1DA` | `src/base/cics/bms/BNK1DAM.bms:21`; `src/base/cics/cobol/BNK1DAC.cbl:428`, `src/base/cics/cobol/BNK1DAC.cbl:828` |
| `BNK1DCS` | `BNK1DCM` / `BNK1DC` | `src/base/cics/bms/BNK1DCM.bms:21`; `src/base/cics/cobol/BNK1DCS.cbl:635`, `src/base/cics/cobol/BNK1DCS.cbl:1509` |
| `BNK1TFN` | `BNK1TFM` / `BNK1TF` | `src/base/cics/bms/BNK1TFM.bms:22`; `src/base/cics/cobol/BNK1TFN.cbl:357`, `src/base/cics/cobol/BNK1TFN.cbl:672` |
| `BNK1UAC` | `BNK1UAM` / `BNK1UA` | `src/base/cics/bms/BNK1UAM.bms:20`; `src/base/cics/cobol/BNK1UAC.cbl:420`, `src/base/cics/cobol/BNK1UAC.cbl:1074` |
| Unknown caller | File `BNK1B2M`, internal mapset `BNK1TFM` / map `BNK1B2` | **legacy-only temporary/excluded artifact**: `src/base/cics/bms/BNK1B2M.bms:21`, `src/base/cics/bms/BNK1B2M.bms:25`, `.setup/deploy/Development.yml:16`. This exclusion is not proof of dead code in every environment. |

### C08 - Debugger and socket-support configuration

**Outside application-parser scope**, but every following transaction mapping is present in the generated [config] table. Direction **in**, status **legacy-only development/support definitions**, not banking operations. Exact mappings, including programs absent from banking COBOL, are preserved.

| Transaction -> program | Original resource evidence |
|---|---|
| `CDT#` -> `EQA10XSC` | `.setup/zconfig/debug-definitions.yaml:1330` |
| `DTSC` -> `EQA10XSC` | `.setup/zconfig/debug-definitions.yaml:1336` |
| `DTCN` -> `EQADCCNM` | `.setup/zconfig/debug-definitions.yaml:1341` |
| `DTCD` -> `EQADCVSM` | `.setup/zconfig/debug-definitions.yaml:1346` |
| `DTCI` -> `EQADCVSM` | `.setup/zconfig/debug-definitions.yaml:1351` |
| `DTCX` -> `EQANCDTX` | `.setup/zconfig/debug-definitions.yaml:1356` |
| `DTCP` -> `EQA0CDTP` | `.setup/zconfig/debug-definitions.yaml:1361` |
| `DTSI` -> `EQA0CINF` | `.setup/zconfig/debug-definitions.yaml:1366` |
| `DTNP` -> `EQANPCNM` | `.setup/zconfig/debug-definitions.yaml:1371` |
| `DTST` -> `EQASTMOD` | `.setup/zconfig/debug-definitions.yaml:1376` |
| `EZAO` -> `EZACIC00` | `.setup/zconfig/debug-definitions.yaml:1381` |
| `CSKL` -> `EZACIC02` | `.setup/zconfig/debug-definitions.yaml:1388` |
| `EZAP` -> `EZACIC22` | `.setup/zconfig/debug-definitions.yaml:1395` |
| `EZAC` -> `EZACIC23` | `.setup/zconfig/debug-definitions.yaml:1402` |
| `SRV1` -> `EZACICSC` | `.setup/zconfig/debug-definitions.yaml:1409` |
| `SRV2` -> `EZACICSS` | `.setup/zconfig/debug-definitions.yaml:1416` |
| `SRV3` -> `EZACIC6C` | `.setup/zconfig/debug-definitions.yaml:1423` |
| `SRV4` -> `EZACIC6S` | `.setup/zconfig/debug-definitions.yaml:1430` |
| `SRV5` -> `EZACICAC` | `.setup/zconfig/debug-definitions.yaml:1437` |
| `SRV6` -> `EZACICAS` | `.setup/zconfig/debug-definitions.yaml:1444` |
| `LSN0` -> `EZACIC02` | `.setup/zconfig/debug-definitions.yaml:1451` |

Additional **outside tool scope** support stores/endpoints: debugger profile file `EQADPFMB` (both), socket configuration `EZACONFG` -> `BANKZ.CICSBOZ.TCP.CONFIG` (in), socket message queue `TCPM` -> `TCPDATA` (out), and socket allocation queue `TRAA` -> transaction `TRAA` (out; target program unknown) (`.setup/zconfig/debug-definitions.yaml:1517`, `.setup/zconfig/debug-definitions.yaml:1531`, `.setup/zconfig/debug-definitions.yaml:1542`, `.setup/zconfig/debug-definitions.yaml:1558`).

Debugger TD queues are `CINI` -> `CINSPIN` (in), `CINL` -> `CINSPLS` (in), `CINO` -> `CINSPOT` (out), `EQAL` -> `CSSL` (indirect queue routing; direction/consumer unknown), `CIGZ` -> `IGZDBGIN` (in), `EQAM` -> `EQADBGM` (in), `CIBM` -> `IBMDBGIN` (in), and `EQAD` -> `EQADBGDS` (in) (`.setup/zconfig/debug-definitions.yaml:1461`, `.setup/zconfig/debug-definitions.yaml:1469`, `.setup/zconfig/debug-definitions.yaml:1476`, `.setup/zconfig/debug-definitions.yaml:1483`, `.setup/zconfig/debug-definitions.yaml:1488`, `.setup/zconfig/debug-definitions.yaml:1495`, `.setup/zconfig/debug-definitions.yaml:1503`, `.setup/zconfig/debug-definitions.yaml:1510`). HTTP debugger service `EQADTCN` uses `CWXN`, URM `EQADCAN0`, port `{{ vars.cics_debug_port }}` (default 27103), and `/dtcn/*` (in; `.setup/zconfig/debug-definitions.yaml:1581`, `.setup/zconfig/debug-definitions.yaml:1595`, `.setup/config/config.yaml:223`). Their production need and the TRAA destination are unknown (INT-Q08).

### C09 - CICS IPIC, DB2 attach and load-library resources (outside tool scope)

Direction **both**, status **L?**. TCPIPSERVICE `ZOSEE` accepts IPIC on `{{ vars.cics_ipic_port }}`, using URM `DFHISAIP` and transaction `CISS` (`.setup/zconfig/bank-of-z-definitions.yaml:1123`). This is the configured server-side counterpart to A02, not an additional REST operation.

DB2CONN uses configured DB2 subsystem ID, plan `BANKZPLN`, SQLCODE connection-error reporting and message queue `CSMT` (diagnostics out); DB2ENTRY `BANKZ` selects the same plan (`.setup/zconfig/bank-of-z-definitions.yaml:1137`, `.setup/zconfig/bank-of-z-definitions.yaml:1146`). DB2TRAN resources associate OCAC, OCCA, OCCS, OCRA, ODAC, ODCS, OTFN and OUAC with entry BANKZ (`.setup/zconfig/bank-of-z-definitions.yaml:1159`). These associations are not additional transaction-to-program definitions. CICS LIBRARY BANKZ reads `BANKZ.V0R1M0.LOADLIB`, whereas the sample batch jobs name `.LOAD`; neither is silently substituted for the other (`.setup/zconfig/bank-of-z-definitions.yaml:1201`, `src/base/batch/jcl/BNKSTMTC.jcl:30`).

## Data-store and file boundaries

### S01 - DB2 ACCOUNT

Status **L?**, with **legacy-only** BANKDATA setup writes. The DDL names `BANKZ.ACCOUNT`; online SQL shown here is unqualified, whereas BNKSTMTC explicitly qualifies BANKZ. Runtime qualification of online SQL is not inferred from that difference (`.setup/jcl/cics/Db2-create.j2:24`, `src/base/batch/cobol/BNKSTMTC.cbl:103`).

| Program | Direction and access | Original-source evidence |
|---|---|---|
| `BANKDATA` | out: DELETE, INSERT | `src/base/cics/cobol/BANKDATA.cbl:1467`, `src/base/cics/cobol/BANKDATA.cbl:1005` |
| `CREACC` | out: INSERT | `src/base/cics/cobol/CREACC.cbl:917` |
| `DBCRFUN` | both: SELECT, UPDATE | `src/base/cics/cobol/DBCRFUN.cbl:270`, `src/base/cics/cobol/DBCRFUN.cbl:393` |
| `DELACC` | both: SELECT, DELETE | `src/base/cics/cobol/DELACC.cbl:273`, `src/base/cics/cobol/DELACC.cbl:439` |
| `INQACC` | in: ACC-CURSOR and separate SELECT | `src/base/cics/cobol/INQACC.cbl:66`, `src/base/cics/cobol/INQACC.cbl:843` |
| `INQACCCU` | in: ACC-CURSOR | `src/base/cics/cobol/INQACCCU.cbl:74`, `src/base/cics/cobol/INQACCCU.cbl:87` |
| `INQACCS` | in: ACC-ALL-CURSOR | `src/base/cics/cobol/INQACCS.cbl:71`, `src/base/cics/cobol/INQACCS.cbl:84` |
| `UPDACC` | both: SELECT, UPDATE | `src/base/cics/cobol/UPDACC.cbl:216`, `src/base/cics/cobol/UPDACC.cbl:279` |
| `XFRFUN` | both: SELECT and UPDATE of FROM and TO accounts | `src/base/cics/cobol/XFRFUN.cbl:955`, `src/base/cics/cobol/XFRFUN.cbl:993`, `src/base/cics/cobol/XFRFUN.cbl:1082`, `src/base/cics/cobol/XFRFUN.cbl:1363` |
| `BNKSTMTC` | in: ACCT-CURSOR, BANKZ.ACCOUNT | `src/base/batch/cobol/BNKSTMTC.cbl:98`, `src/base/batch/cobol/BNKSTMTC.cbl:103` |
| `BNKSTMTP` | in: ACCT_CURSOR; **outside tool scope**, PL/I | `src/base/batch/pli/BNKSTMTP.pli:142`, `src/base/batch/pli/BNKSTMTP.pli:155` |

### S02 - DB2 CONTROL and CICS serialization

Status **L?**, except **legacy-only** BANKDATA setup. DDL says `BANKZ.CONTROL`; a conflicting copybook qualification is already recorded as DM-Q02, not silently reconciled (`.setup/jcl/cics/Db2-create.j2:67`, `src/base/cics/copy/CONTDB2.cpy:7`).

| Program | Direction and access | Evidence |
|---|---|---|
| `BANKDATA` | out: DELETE, INSERT, UPDATE | `src/base/cics/cobol/BANKDATA.cbl:1535`, `src/base/cics/cobol/BANKDATA.cbl:773`, `src/base/cics/cobol/BANKDATA.cbl:761`; further INSERTs at 1631, 1658, 1678, 1698, 1718, 1738, 1768, 1794, UPDATEs at 809, 836 in that same file |
| `CREACC` | both: SELECT, UPDATE | `src/base/cics/cobol/CREACC.cbl:495`, `src/base/cics/cobol/CREACC.cbl:594`, `src/base/cics/cobol/CREACC.cbl:686`, `src/base/cics/cobol/CREACC.cbl:778` |
| `CRECUST` | both: SELECT, UPDATE | `src/base/cics/cobol/CRECUST.cbl:1496`, `src/base/cics/cobol/CRECUST.cbl:1525` |

Additional program-to-CICS boundary, **both / L?**: ENQ/DEQ protects 16-byte resource names based on `BANKZACCT` or `BANKZCUST` plus sort code and padding (`src/base/cics/cobol/CREACC.cbl:190`, `src/base/cics/cobol/CREACC.cbl:438`, `src/base/cics/cobol/CREACC.cbl:460`, `src/base/cics/cobol/CRECUST.cbl:177`, `src/base/cics/cobol/CRECUST.cbl:546`, `src/base/cics/cobol/CRECUST.cbl:571`). These commands are in program digests, not in the call/data maps. The routines retain "Named Counter" terminology, but the shown increment routines access DB2 CONTROL; that terminology is not proof of a separate counter-server connection (`src/base/cics/cobol/CRECUST.cbl:589`, `src/base/cics/cobol/CRECUST.cbl:594`, `src/base/cics/cobol/CREACC.cbl:495`).

### S03 - DB2 CUSTOMER

Status **L?**, except **legacy-only** BANKDATA setup. DDL names `BANKZ.CUSTOMER` (`.setup/jcl/cics/Db2-create.j2:81`).

| Program | Direction and access | Evidence |
|---|---|---|
| `BANKDATA` | out: DELETE, INSERT | `src/base/cics/cobol/BANKDATA.cbl:1402`, `src/base/cics/cobol/BANKDATA.cbl:677` |
| `CRECUST` | out: INSERT; **source-scanner omission**, not outside tool scope | `src/base/cics/cobol/CRECUST.cbl:1219`, `src/base/cics/cobol/CRECUST.cbl:1220`; existing CONTRADICTION DM-Q07 |
| `DELCUS` | both: SELECT, DELETE | `src/base/cics/cobol/DELCUS.cbl:476`, `src/base/cics/cobol/DELCUS.cbl:643` |
| `INQCUST` | in: SELECT, including last-customer lookup | `src/base/cics/cobol/INQCUST.cbl:310`, `src/base/cics/cobol/INQCUST.cbl:675` |
| `UPDCUST` | both: SELECT, UPDATE | `src/base/cics/cobol/UPDCUST.cbl:300`, `src/base/cics/cobol/UPDCUST.cbl:364` |
| `BNKSTMTC` | in: SELECT, BANKZ.CUSTOMER | `src/base/batch/cobol/BNKSTMTC.cbl:1127`, `src/base/batch/cobol/BNKSTMTC.cbl:1146` |
| `BNKSTMTP` | in: SELECT; **outside tool scope**, PL/I | `src/base/batch/pli/BNKSTMTP.pli:435`, `src/base/batch/pli/BNKSTMTP.pli:469` |

### S04 - DB2 PROCTRAN

Status **L?**. DDL names `BANKZ.PROCTRAN` (`.setup/jcl/cics/Db2-create.j2:51`).

| Program | Direction and access | Evidence |
|---|---|---|
| `CREACC` | out: INSERT | `src/base/cics/cobol/CREACC.cbl:1060` |
| `CRECUST` | out: INSERT | `src/base/cics/cobol/CRECUST.cbl:1360` |
| `DBCRFUN` | out: INSERT | `src/base/cics/cobol/DBCRFUN.cbl:533` |
| `DELACC` | out: INSERT | `src/base/cics/cobol/DELACC.cbl:522` |
| `DELCUS` | out: INSERT | `src/base/cics/cobol/DELCUS.cbl:775` |
| `XFRFUN` | out: two INSERT statements | `src/base/cics/cobol/XFRFUN.cbl:1625`, `src/base/cics/cobol/XFRFUN.cbl:1762` |
| `INQTRAND` | in: TRAND-CURSOR | `src/base/cics/cobol/INQTRAND.cbl:64`, `src/base/cics/cobol/INQTRAND.cbl:74` |
| `INQTRANL` | in: TRAN-CURSOR and TRAN-COUNT-CURSOR | `src/base/cics/cobol/INQTRANL.cbl:74`, `src/base/cics/cobol/INQTRANL.cbl:95` |
| `BNKSTMTC` | in: TRAN-CURSOR, BANKZ.PROCTRAN | `src/base/batch/cobol/BNKSTMTC.cbl:112`, `src/base/batch/cobol/BNKSTMTC.cbl:118` |
| `BNKSTMTP` | in: TRAN_CURSOR; **outside tool scope**, PL/I | `src/base/batch/pli/BNKSTMTP.pli:160`, `src/base/batch/pli/BNKSTMTP.pli:170` |

### S05 - ABNDFILE / VSAM

Direction **out**, status **L?**: ABNDPROC writes ABNDINFO using the record key to CICS file `ABNDFILE` (`src/base/cics/cobol/ABNDPROC.cbl:139`, `src/base/cics/cobol/ABNDPROC.cbl:141`). The checked-in resource maps it to `BANKZ.V0R1M0.ABNDFILE`, record size 681 and key length 12 (`.setup/zconfig/bank-of-z-definitions.yaml:5`, `.setup/zconfig/bank-of-z-definitions.yaml:9`, `.setup/zconfig/bank-of-z-definitions.yaml:15`). Failed writes DISPLAY RESP/RESP2 and RETURN; a diagnostic LINK does not guarantee that an abend record was persisted (`src/base/cics/cobol/ABNDPROC.cbl:149`).

### S06 - IMS DL/I message and database boundaries

`CBLTDLI` crosses into IMS; GU/ISRT on `LTERMPCB` are **message input/output**, not database INSERTs. Database calls use the selected database PCB and SSA. Status **L?** for handlers, **legacy-only** for LOAD*. DBD/PSB mappings and dataset names are **outside tool scope**; DL/I call statements themselves are in the digests/data-access map.

| Program | Direction; message calls | Database calls and target evidence |
|---|---|---|
| `IBACSUM` | both; GU in / ISRT out: `src/base/ims/cobol/IBACSUM.cbl:182`, `src/base/ims/cobol/IBACSUM.cbl:282` | in: CUSTACCS traversal via NEXT-CALL, ACCOUNT GHU: `src/base/ims/cobol/IBACSUM.cbl:212`, `src/base/ims/cobol/IBACSUM.cbl:218`; PSB ACCOUNT/CUSTACCS: `src/base/ims/PSB/IBACSUM.asm:10`, `src/base/ims/PSB/IBACSUM.asm:13` |
| `IBGCUDAT` | both; `src/base/ims/cobol/IBGCUDAT.cbl:159`, `src/base/ims/cobol/IBGCUDAT.cbl:212` | in: CUSTOMER GU: `src/base/ims/cobol/IBGCUDAT.cbl:184`; `src/base/ims/PSB/IBGCUDAT.asm:10` |
| `IBLOGIN1` | both; `src/base/ims/cobol/IBLOGIN1.cbl:191`, `src/base/ims/cobol/IBLOGIN1.cbl:279` | both: CUSTOMER GHU/REPL: `src/base/ims/cobol/IBLOGIN1.cbl:219`, `src/base/ims/cobol/IBLOGIN1.cbl:257`. CUSTOMER SSA is source evidence, **not** proof that configured IBLOGIN runs this member; see I01. |
| `IBLOGOUT` | both; `src/base/ims/cobol/IBLOGOUT.cbl:160`, `src/base/ims/cobol/IBLOGOUT.cbl:222` | both: CUSTOMER GHU/REPL: `src/base/ims/cobol/IBLOGOUT.cbl:189`, `src/base/ims/cobol/IBLOGOUT.cbl:203`; `src/base/ims/PSB/IBLOGOUT.asm:10` |
| `IBSCUDAT` | both; `src/base/ims/cobol/IBSCUDAT.cbl:180`, `src/base/ims/cobol/IBSCUDAT.cbl:288` | both: CUSTOMER GHU/REPL: `src/base/ims/cobol/IBSCUDAT.cbl:223`, `src/base/ims/cobol/IBSCUDAT.cbl:254`; `src/base/ims/PSB/IBSCUDAT.asm:10` |
| `IBTRAN` | both; `src/base/ims/cobol/IBTRAN.cbl:258`, `src/base/ims/cobol/IBTRAN.cbl:444` | both: ACCOUNT GHU/REPL, HISTORY ISRT, CUSTACCS NEXT-CALL traversal, ACCOUNT GHU for summary: `src/base/ims/cobol/IBTRAN.cbl:301`, `src/base/ims/cobol/IBTRAN.cbl:366`, `src/base/ims/cobol/IBTRAN.cbl:358`, `src/base/ims/cobol/IBTRAN.cbl:394`, `src/base/ims/cobol/IBTRAN.cbl:400`; PSB: `src/base/ims/PSB/IBTRAN.asm:10`, `src/base/ims/PSB/IBTRAN.asm:13`, `src/base/ims/PSB/IBTRAN.asm:16` |
| `LOADACCT` | in: ACCTIN file; no terminal message calls listed | out: ACCOUNT ISRT: `src/base/ims/cobol/LOADACCT.cbl:159` |
| `LOADCUSA` | in: CUSAIN file | out: CUSTACCS ISRT: `src/base/ims/cobol/LOADCUSA.cbl:158` |
| `LOADCUST` | in: CUSTIN file | out: CUSTOMER ISRT: `src/base/ims/cobol/LOADCUST.cbl:196` |
| `LOADHIST` | in: HISTIN file | out: HISTORY ISRT: `src/base/ims/cobol/LOADHIST.cbl:175` |
| `LOADTSTA` | in: TSTAIN file | out: TSTAT ISRT: `src/base/ims/cobol/LOADTSTA.cbl:168` |

`NEXT-CALL` is not an unknown external program: IBACSUM assigns GHU initially and GHN subsequently (`src/base/ims/cobol/IBACSUM.cbl:207`, `src/base/ims/cobol/IBACSUM.cbl:256`); IBTRAN does likewise (`src/base/ims/cobol/IBTRAN.cbl:389`, `src/base/ims/cobol/IBTRAN.cbl:429`). These source assignments explain the data-access row without asserting parser-verified IBTRAN control flow.

### S07 - IMS databases and datasets (outside tool scope)

| Database / DBD DD1 | Touching programs, as evidenced in S06 | Physical dataset mapping; status |
|---|---|---|
| ACCOUNT / ACCOUNT | IBACSUM, IBTRAN, LOADACCT | `{{ ims.ims_hlq }}.ACCOUNT.DB`: `.setup/jcl/ims/Ims-dynalloc.j2:158`; DBD `src/base/ims/DBD/ACCOUNT.asm:13`, `src/base/ims/DBD/ACCOUNT.asm:19`; L?, loader legacy-only |
| CUSTACCS / CUSTACCS | IBACSUM, IBTRAN, LOADCUSA | `{{ ims.ims_hlq }}.CUSTACCS.DB`: `.setup/jcl/ims/Ims-dynalloc.j2:143`; DBD `src/base/ims/DBD/CUSTACCS.asm:13`, `src/base/ims/DBD/CUSTACCS.asm:19`; L?, loader legacy-only |
| CUSTOMER / CUSTOMER | IBLOGIN1, IBLOGOUT, IBGCUDAT, IBSCUDAT, LOADCUST; PL/I IBLOGIN (I01) | `{{ ims.ims_hlq }}.CUSTOMER.DB`: `.setup/jcl/ims/Ims-dynalloc.j2:146`; DBD `src/base/ims/DBD/CUSTOMER.asm:13`, `src/base/ims/DBD/CUSTOMER.asm:19`; L?, loader legacy-only |
| HISTORY / HISTORY | IBTRAN, LOADHIST; distinct from DB2 IMSBANK.HISTORY | `{{ ims.ims_hlq }}.HISTORY.DB`: `.setup/jcl/ims/Ims-dynalloc.j2:140`; DBD `src/base/ims/DBD/HISTORY.asm:16`, `src/base/ims/DBD/HISTORY.asm:22`; L?, loader legacy-only |
| TSTAT / TSTAT | LOADTSTA; no online caller established in S06 | `{{ ims.ims_hlq }}.TSTAT.DB`: `.setup/jcl/ims/Ims-dynalloc.j2:149`; DBD `src/base/ims/DBD/TSTAT.asm:13`, `src/base/ims/DBD/TSTAT.asm:19`; legacy-only load path, online purpose unknown |
| ACCTYPE / ACCTYPE | Generic IB / IBLOAD PSB sensitivity only; executed consumer unknown | `src/base/ims/DBD/ACCTYPE.asm:13`, `src/base/ims/DBD/ACCTYPE.asm:19`, `src/base/ims/PSB/IB.asm:13`, `src/base/ims/PSB/IBLOAD.asm:13`; unknown |
| CUSTTYPE / CUSTTYPE | Generic IB / IBLOAD PSB sensitivity only | `src/base/ims/DBD/CUSTTYPE.asm:13`, `src/base/ims/DBD/CUSTTYPE.asm:19`, `src/base/ims/PSB/IB.asm:22`, `src/base/ims/PSB/IBLOAD.asm:22`; unknown |
| TSTATTYP / TSTATTYP | Generic IB / IBLOAD PSB sensitivity only | `src/base/ims/DBD/TSTATTYP.asm:4`, `src/base/ims/DBD/TSTATTYP.asm:10`, `src/base/ims/PSB/IB.asm:31`, `src/base/ims/PSB/IBLOAD.asm:31`; dynamic allocation `.setup/jcl/ims/Ims-dynalloc.j2:161`; unknown |
| TTYPE / TTYPE | Generic IB / IBLOAD PSB sensitivity only | `src/base/ims/DBD/TTYPE.asm:13`, `src/base/ims/DBD/TTYPE.asm:19`, `src/base/ims/PSB/IB.asm:34`, `src/base/ims/PSB/IBLOAD.asm:34`; unknown |

Direction follows S06 for identified consumers; sensitivity-only definitions potentially permit both directions (`PROCOPT=AP` / `L`), but are not evidence of an executed read/write. Unresolved consumers and reference-database purpose are INT-Q05. The HISTORY layout mismatch and cross-store durability questions remain DM-Q03/DM-Q08.

### S08 - Loader files and JCL (partly outside tool scope)

Direction **in** file / **out** database; status **legacy-only data load**. COBOL SELECT/READ is **not inventoried by the current data-access map**, rather than outside the COBOL language scope. JCL dataset/program/PSB wiring is **outside tool scope**. Every loader job uses DFSRRC00 with PSB IBLOAD; its database PCBs are defined in `src/base/ims/PSB/IBLOAD.asm:10`.

| Loader | SELECT / READ source | Input and output datasets; JCL invocation |
|---|---|---|
| LOADACCT | `src/base/ims/cobol/LOADACCT.cbl:18`, `src/base/ims/cobol/LOADACCT.cbl:124` | ACCTIN -> `{{ ims.ims_hlq }}.ACCOUNT.INPUT`, ACCOUNT -> `.ACCOUNT.DB`; `.setup/jcl/ims/Ims-load-acct.j2:24`, `.setup/jcl/ims/Ims-load-acct.j2:31`, `.setup/jcl/ims/Ims-load-acct.j2:44` |
| LOADCUSA | `src/base/ims/cobol/LOADCUSA.cbl:18`, `src/base/ims/cobol/LOADCUSA.cbl:133` | CUSAIN -> `{{ ims.ims_hlq }}.CUSTACCS.INPUT`, CUSTACCS -> `.CUSTACCS.DB`; `.setup/jcl/ims/Ims-load-cusa.j2:24`, `.setup/jcl/ims/Ims-load-cusa.j2:31`, `.setup/jcl/ims/Ims-load-cusa.j2:44` |
| LOADCUST | `src/base/ims/cobol/LOADCUST.cbl:18`, `src/base/ims/cobol/LOADCUST.cbl:152` | CUSTIN -> `{{ ims.ims_hlq }}.CUSTOMER.INPUT`, CUSTOMER -> `.CUSTOMER.DB`; `.setup/jcl/ims/Ims-load-cust.j2:24`, `.setup/jcl/ims/Ims-load-cust.j2:31`, `.setup/jcl/ims/Ims-load-cust.j2:44` |
| LOADHIST | `src/base/ims/cobol/LOADHIST.cbl:18`, `src/base/ims/cobol/LOADHIST.cbl:139` | HISTIN -> `{{ ims.ims_hlq }}.HISTORY.INPUT`, HISTORY -> `.HISTORY.DB`; `.setup/jcl/ims/Ims-load-hist.j2:24`, `.setup/jcl/ims/Ims-load-hist.j2:31`, `.setup/jcl/ims/Ims-load-hist.j2:44` |
| LOADTSTA | `src/base/ims/cobol/LOADTSTA.cbl:18`, `src/base/ims/cobol/LOADTSTA.cbl:137` | TSTAIN -> `{{ ims.ims_hlq }}.TSTAT.INPUT`, TSTAT -> `.TSTAT.DB`; `.setup/jcl/ims/Ims-load-tsta.j2:24`, `.setup/jcl/ims/Ims-load-tsta.j2:31`, `.setup/jcl/ims/Ims-load-tsta.j2:44` |

## IMS transactions, Java and JNI

### I01 - IMS entry transactions and PL/I login (outside tool scope)

Direction **both** request/reply; status **L?**. The IMS SPOC template, not the CICS transaction graph, supplies these mappings:

| Transaction -> configured program | Configuration proof |
|---|---|
| `IBLOGIN` -> `IBLOGIN` | `.setup/jcl/ims/Ims-spoc.jcl.j2:56` |
| `IBLOGOUT` -> `IBLOGOUT` | `.setup/jcl/ims/Ims-spoc.jcl.j2:58` |
| `IBTRAN` -> `IBTRAN` | `.setup/jcl/ims/Ims-spoc.jcl.j2:60` |
| `IBACSUM` -> `IBACSUM` | `.setup/jcl/ims/Ims-spoc.jcl.j2:62` |
| `IBGCUDAT` -> `IBGCUDAT` | `.setup/jcl/ims/Ims-spoc.jcl.j2:64` |
| `IBSCUDAT` -> `IBSCUDAT` | `.setup/jcl/ims/Ims-spoc.jcl.j2:66` |
| `IBGHIST` -> `IBGHIST` (LANG JAVA) | `.setup/jcl/ims/Ims-spoc.jcl.j2:44`, `.setup/jcl/ims/Ims-spoc.jcl.j2:68` |

PL/I procedure IBLOGIN uses PLITDLI: GU terminal input, CUSTOMER GHU/REPL and ISRT terminal output (`src/base/ims/pli/IBLOGIN.pli:20`, `src/base/ims/pli/IBLOGIN.pli:212`, `src/base/ims/pli/IBLOGIN.pli:260`, `src/base/ims/pli/IBLOGIN.pli:305`, `src/base/ims/pli/IBLOGIN.pli:335`). Its PSB explicitly says `PSBNAME=IBLOGIN,LANG=PL/I` and CUSTOMER (`src/base/ims/PSB/IBLOGIN.asm:10`, `src/base/ims/PSB/IBLOGIN.asm:13`). The COBOL member instead declares `PROGRAM-ID. IBLOGIN1` (`src/base/ims/cobol/IBLOGIN1.cbl:3`). This supports the PL/I configuration reading, not an alias between IBLOGIN and IBLOGIN1; installed load-module identity is unknown (INT-Q04).

### I02 - IBTRAN -> JNI -> InsertHist -> DB2 history

Direction **out** history data, **both** JNI lookups/handles; status **L?**. JNI/Java semantics are **outside tool scope** even though scanned COBOL CALL sites are in the graph. IBTRAN's control flow is **unverified by tooling**.

IBTRAN copies JNI and addresses the JVM interface tables, then names `nazare/jmp/controller/InsertHist`, looks up static method `insertHist` with signature `(Ljava/nio/ByteBuffer;)V`, creates a direct byte buffer over HISTORY-SEG and calls that method (`src/base/ims/cobol/IBTRAN.cbl:208`, `src/base/ims/cobol/IBTRAN.cbl:508`, `src/base/ims/cobol/IBTRAN.cbl:511`, `src/base/ims/cobol/IBTRAN.cbl:516`, `src/base/ims/cobol/IBTRAN.cbl:522`, `src/base/ims/cobol/IBTRAN.cbl:540`, `src/base/ims/cobol/IBTRAN.cbl:541`, `src/base/ims/cobol/IBTRAN.cbl:554`, `src/base/ims/cobol/IBTRAN.cbl:470`, `src/base/ims/cobol/IBTRAN.cbl:474`, `src/base/ims/cobol/IBTRAN.cbl:490`).

| Graph unresolved target | Source-level identity proven by JNI copybook | COBOL call site(s) |
|---|---|---|
| `?DELETELOCALREF` | `DeleteLocalRef` function-pointer: `src/base/ims/copy/JNI.cpy:107` | `src/base/ims/cobol/IBTRAN.cbl:281`, `src/base/ims/cobol/IBTRAN.cbl:284` |
| `?NEWDIRECTBYTEBUFFER` | `NewDirectByteBuffer` function-pointer: `src/base/ims/copy/JNI.cpy:313` | `src/base/ims/cobol/IBTRAN.cbl:474` |
| `?CALLSTATICVOIDMETHOD` | `CallStaticVoidMethod` function-pointer: `src/base/ims/copy/JNI.cpy:225` | `src/base/ims/cobol/IBTRAN.cbl:490` |
| `?FINDCLASS` | `FindClass` function-pointer: `src/base/ims/copy/JNI.cpy:90` | `src/base/ims/cobol/IBTRAN.cbl:522` |
| `?GETSTATICMETHODID` | `GetStaticMethodID` function-pointer: `src/base/ims/copy/JNI.cpy:197` | `src/base/ims/cobol/IBTRAN.cbl:554` |

These are not CICS transactions; a transaction resource mapping is **not applicable**. The copybook and table-address assignments prove the source-level JNI mechanism; the association of returned class/method IDs with the named Java method follows the arguments and handle use. Actual runtime function addresses and JVM initialization remain **still unresolved** (INT-Q03); no executable target address is claimed.

Java's `InsertHist.insertHist(ByteBuffer)` decodes EBCDIC and packed-decimal fields, then invokes TransactionService.saveTransactionDetail (`src/base/ims/java/src/main/java/nazare/jmp/controller/InsertHist.java:33`, `src/base/ims/java/src/main/java/nazare/jmp/controller/InsertHist.java:57`, `src/base/ims/java/src/main/java/nazare/jmp/controller/InsertHist.java:68`, `src/base/ims/java/src/main/java/nazare/jmp/controller/InsertHist.java:87`). That service INSERTs **DB2 `IMSBANK.HISTORY`**, a different datastore from IMS HISTORY (`src/base/ims/java/src/main/java/nazare/jmp/history/TransactionService.java:140`). DDL uses configured database/table values, defaulting to IMSBANK/HISTORY (`.setup/jcl/ims/Db2-create.j2:25`, `.setup/config/config.yaml:243`). Auto-commit is disabled and the connection is closed without a shown explicit commit; durability/coordinated IMS-DB2 commit is **unknown**, as recorded in DM-Q08 (`src/base/ims/java/src/main/java/nazare/jmp/history/TransactionService.java:136`, `src/base/ims/java/src/main/java/nazare/jmp/history/TransactionService.java:161`).

### I03 - Java history inquiry and JDBC endpoint (outside tool scope)

| Interface | Direction; status | Evidence |
|---|---|---|
| `IBGHIST` -> `nazare/jmp/controller/QueryTransaction` | both; L?; configured Java class mapping | `.setup/jcl/ims/templates/jmp/CREDFSJVMAP.j2:23` |
| QueryTransaction -> IMS message queue | both; L?; ApplicationFactory, getUnique input, DEFAULT_DESTINATION output, transaction commit | `src/base/ims/java/src/main/java/nazare/jmp/controller/QueryTransaction.java:38`, `src/base/ims/java/src/main/java/nazare/jmp/controller/QueryTransaction.java:57`, `src/base/ims/java/src/main/java/nazare/jmp/controller/QueryTransaction.java:110`, `src/base/ims/java/src/main/java/nazare/jmp/controller/QueryTransaction.java:112` |
| QueryTransaction -> TransactionService -> DB2 IMSBANK.HISTORY | in data / out query; L?; account-number query, latest 50 rows | `src/base/ims/java/src/main/java/nazare/jmp/controller/QueryTransaction.java:63`, `src/base/ims/java/src/main/java/nazare/jmp/history/TransactionService.java:47` |
| TransactionService -> IBM JCC JDBC driver | both; L?; URL `jdbc:db2:` + system property `db2.location`, default `DBD1LOC`; no TCP host/port in this active URL | `src/base/ims/java/src/main/java/nazare/jmp/history/TransactionService.java:27`, `src/base/ims/java/src/main/java/nazare/jmp/history/TransactionService.java:34`, `src/base/ims/java/src/main/java/nazare/jmp/history/TransactionService.java:39`, `src/base/ims/java/src/main/java/nazare/jmp/history/TransactionService.java:41`, `src/base/ims/java/src/main/java/nazare/jmp/history/TransactionService.java:128` |
| IMS/JDBC native and Java libraries | in dependencies; L? runtime configuration | `imsudb.jar`, application JAR, `db2jcc.jar`, license JAR, native-library paths: `.setup/jcl/ims/templates/jmp/dfsjvmpr.props.j2:1`, `.setup/jcl/ims/templates/jmp/dfsjvmpr.props.j2:2` |
| `IMSBankHistory` standalone main -> DB2 IMSBANK.HISTORY | both connection/query, in results; legacy-only test candidate, actual usage unknown | Reads host/port/location/user from system properties, constructs SSL JDBC URL, queries ACCID `1501`: `src/base/ims/java/src/main/java/nazare/jmp/controller/IMSBankHistory.java:31`, `src/base/ims/java/src/main/java/nazare/jmp/controller/IMSBankHistory.java:62`, `src/base/ims/java/src/main/java/nazare/jmp/controller/IMSBankHistory.java:70`, `src/base/ims/java/src/main/java/nazare/jmp/controller/IMSBankHistory.java:85`, `src/base/ims/java/src/main/java/nazare/jmp/controller/IMSBankHistory.java:89`. Its trust-all code is labelled testing-only at line 36; no production endpoint is inferred. |

The literal remote URL `jdbc:db2://tivlp02.svl.ibm.com:5050/DBC1` and location names DALLASD/DALLASC/DBC1 are **comments only**, not the active TransactionService connection (`src/base/ims/java/src/main/java/nazare/jmp/history/TransactionService.java:35`, `src/base/ims/java/src/main/java/nazare/jmp/history/TransactionService.java:134`). They are recorded for operational recognition, not asserted live partner systems.

## REST APIs and HTTP peers (outside tool scope)

### A01 - z/OS Connect operation -> program

Direction **both**, status **L?** for all 21 operation mappings. Paths below are relative to `/api` (`src/api/src/main/api/openapi.yaml:25`). Each row cites both the operation's zasset selection and its provider's actual program/transaction property. IMS transaction -> program proof is in I01. CRECUST/IBTRAN retain the control-flow qualification above.

| Method / path | Program or IMS transaction | Operation evidence; provider evidence |
|---|---|---|
| GET `/accounts` | INQACCS | `src/api/src/main/operations/%2Faccounts/get/operation.yaml:3`; `src/api/src/main/zosAssets/INQACCS/zosAsset.yaml:9` |
| POST `/accounts` | CREACC | `src/api/src/main/operations/%2Faccounts/post/operation.yaml:3`; `src/api/src/main/zosAssets/CREACC/zosAsset.yaml:9` |
| GET `/accounts/{accountId}` | INQACC | `src/api/src/main/operations/%2Faccounts%2F%7BaccountId%7D/get/operation.yaml:3`; `src/api/src/main/zosAssets/INQACC/zosAsset.yaml:9` |
| PUT `/accounts/{accountId}` | UPDACC | `src/api/src/main/operations/%2Faccounts%2F%7BaccountId%7D/put/operation.yaml:3`; `src/api/src/main/zosAssets/UPDACC/zosAsset.yaml:9` |
| DELETE `/accounts/{accountId}` | DELACC | `src/api/src/main/operations/%2Faccounts%2F%7BaccountId%7D/delete/operation.yaml:3`; `src/api/src/main/zosAssets/DELACC/zosAsset.yaml:9` |
| GET `/accounts/{accountId}/balances` | INQACC | `src/api/src/main/operations/%2Faccounts%2F%7BaccountId%7D%2Fbalances/get/operation.yaml:3`; `src/api/src/main/zosAssets/INQACC/zosAsset.yaml:9` |
| POST `/accounts/{accountId}/deposit` | DBCRFUN | `src/api/src/main/operations/%2Faccounts%2F%7BaccountId%7D%2Fdeposit/post/operation.yaml:3`; `src/api/src/main/zosAssets/DBCRFUN/zosAsset.yaml:9` |
| POST `/accounts/{accountId}/transfer` | XFRFUN | `src/api/src/main/operations/%2Faccounts%2F%7BaccountId%7D%2Ftransfer/post/operation.yaml:3`; `src/api/src/main/zosAssets/XFRFUN/zosAsset.yaml:9` |
| GET `/accounts/{accountId}/transactions` | INQTRANL | `src/api/src/main/operations/%2Faccounts%2F%7BaccountId%7D%2Ftransactions/get/operation.yaml:3`; `src/api/src/main/zosAssets/INQTRANL/zosAsset.yaml:10` |
| GET `/accounts/{accountId}/transactions/{transactionId}` | INQTRAND | `src/api/src/main/operations/%2Faccounts%2F%7BaccountId%7D%2Ftransactions%2F%7BtransactionId%7D/get/operation.yaml:3`; `src/api/src/main/zosAssets/INQTRAND/zosAsset.yaml:9` |
| POST `/customers` | CRECUST | `src/api/src/main/operations/%2Fcustomers/post/operation.yaml:3`; `src/api/src/main/zosAssets/CRECUST/zosAsset.yaml:9` |
| GET `/customers/{customerId}` | INQCUST | `src/api/src/main/operations/%2Fcustomers%2F%7BcustomerId%7D/get/operation.yaml:3`; `src/api/src/main/zosAssets/INQCUST/zosAsset.yaml:9` |
| PUT `/customers/{customerId}` | UPDCUST | `src/api/src/main/operations/%2Fcustomers%2F%7BcustomerId%7D/put/operation.yaml:3`; `src/api/src/main/zosAssets/UPDCUST/zosAsset.yaml:7` |
| DELETE `/customers/{customerId}` | DELCUS | `src/api/src/main/operations/%2Fcustomers%2F%7BcustomerId%7D/delete/operation.yaml:3`; `src/api/src/main/zosAssets/DELCUS/zosAsset.yaml:9` |
| GET `/customers/{customerId}/accounts` | INQACCCU | `src/api/src/main/operations/%2Fcustomers%2F%7BcustomerId%7D%2Faccounts/get/operation.yaml:3`; `src/api/src/main/zosAssets/INQACCCU/zosAsset.yaml:9` |
| GET `/ims/accounts/{accountId}` | IBACSUM | `src/api/src/main/operations/%2Fims%2Faccounts%2F%7BaccountId%7D/get/operation.yaml:3`; `src/api/src/main/zosAssets/IBACSUM/zosAsset.yaml:7` |
| GET `/ims/accounts/{accountId}/balances` | IBACSUM | `src/api/src/main/operations/%2Fims%2Faccounts%2F%7BaccountId%7D%2Fbalances/get/operation.yaml:3`; `src/api/src/main/zosAssets/IBACSUM/zosAsset.yaml:7` |
| POST `/ims/accounts/{customerId}/{accountId}/deposit` | IBTRAN | `src/api/src/main/operations/%2Fims%2Faccounts%2F%7BcustomerId%7D%2F%7BaccountId%7D%2Fdeposit/post/operation.yaml:3`; `src/api/src/main/zosAssets/IBTRAN/zosAsset.yaml:7` |
| GET `/ims/customers/{customerId}` | IBGCUDAT | `src/api/src/main/operations/%2Fims%2Fcustomers%2F%7BcustomerId%7D/get/operation.yaml:3`; `src/api/src/main/zosAssets/IBGCUDAT/zosAsset.yaml:7` |
| PUT `/ims/customers/{customerId}` | IBSCUDAT | `src/api/src/main/operations/%2Fims%2Fcustomers%2F%7BcustomerId%7D/put/operation.yaml:3`; `src/api/src/main/zosAssets/IBSCUDAT/zosAsset.yaml:7` |
| GET `/ims/customers/{customerId}/accounts` | IBACSUM | `src/api/src/main/operations/%2Fims%2Fcustomers%2F%7BcustomerId%7D%2Faccounts/get/operation.yaml:3`; `src/api/src/main/zosAssets/IBACSUM/zosAsset.yaml:7` |

### A02 - Provider connections and transaction metadata

Direction **both**, status **L?**. CICS providers select `bankzCicsConnection`; IPIC host/port come from `${CICS_HOST}` / `${CICS_PORT}`, not a hard-coded remote bank (`src/api/src/main/zosAssets/INQACC/zosAsset.yaml:10`, `src/api/src/main/liberty/config/cics.xml:7`). Defaults in setup are localhost:4320, with CMCI localhost:27100 for administration (`.setup/config/config.yaml:218`, `.setup/config/config.yaml:221`, `.setup/config/config.yaml:222`). IMS assets select `imsConn`; it uses `${IMS_DATASTORE}`, `${IMS_HOST}` / `${IMS_PORT}` through the IMS connection factory (`src/api/src/main/zosAssets/IBTRAN/zosAsset.yaml:8`, `src/api/src/main/liberty/config/ims.xml:7`, `src/api/src/main/liberty/config/ims.xml:10`). Setup defaults are localhost:9977 and an additional IMS Connect SSL port 9966 (`.setup/config/config.yaml:233`, `.setup/config/config.yaml:240`).

Most CICS providers explicitly set `transidUsage: EIB_ONLY` and `OMEN`; this is **not** proof that REST invokes BNKMENU instead of the provider's `program` (`src/api/src/main/zosAssets/INQACC/zosAsset.yaml:6`). INQTRANL sets EIB-only `ITRL`, INQTRAND sets EIB-only `ITRD`, while UPDCUST omits these properties (`src/api/src/main/zosAssets/INQTRANL/zosAsset.yaml:7`, `src/api/src/main/zosAssets/INQTRAND/zosAsset.yaml:6`, `src/api/src/main/zosAssets/UPDCUST/zosAsset.yaml:5`). No ITRL/ITRD resource mapping is established in C01; the required operational meaning of these IDs remains INT-Q07.

### A03 - Browser, HTTP server and named external URLs

| Peer/interface | Direction; classification | Evidence and limitation |
|---|---|---|
| z/OS Connect HTTP listener | in requests / out replies; L?, local-dev listener legacy-only | Local config binds `*`:9080 and labels HTTPS redirect disabled for development: `src/api/src/main/liberty/config/http-endpoint.xml:3`. Setup HTTP/HTTPS defaults are 9080/9444: `.setup/config/config.yaml:55`. |
| Browser frontend -> REST | both; L? | `fetch` at `src/frontend/js/api.js:72`; base URL uses `/api` at browser port 3001, otherwise same hostname with HTTPS 9444 / HTTP 9080: `src/frontend/config.js:19`. Setup frontend listener defaults 9081/9445: `.setup/config/config.yaml:60`. This does not establish a deployed public hostname. |
| Browser -> IBM Carbon CDN | out module requests / in JavaScript; L? UI dependency | `https://1.www.s81c.com/common/carbon/web-components/version/v2.47.0`: `src/frontend/js/carbon.js:11`, loading at `src/frontend/js/carbon.js:27` |
| OAuth authorization/token provider | out authorization/token requests implied by API contract; status unknown, placeholder domain | `https://auth.bankofz.example.com/oauth/authorize` and `/oauth/token`: `src/api/src/main/api/openapi.yaml:790`, `src/api/src/main/api/openapi.yaml:791`. No live identity provider or enforcement is proved by these declarations (INT-Q06). |
| API-support mailbox / license URL | metadata only, not a runtime call; legacy-only documentation | `api-support@bankofz.example.com`, Apache license link: `src/api/src/main/api/openapi.yaml:10`, `src/api/src/main/api/openapi.yaml:13` |

## MQ partner path (outside tool scope)

### M01 - Bank of Q client -> CICS DPL bridge

Direction **both**, status **L? sample partner path**, production partner identity unknown. The named source bank is **Bank of Q**, not a discovered real institution (`src/base/mq/java/src/main/java/com/ibm/boq/TransferMoneyToBOZ.java:15`). CLI arguments supply queue manager, customer ID, account and amount; C-prefixed customers select CICS, I-prefixed customers select IMS (`src/base/mq/java/src/main/java/com/ibm/boq/TransferMoneyToBOZ.java:40`, `src/base/mq/java/src/main/java/com/ibm/boq/TransferMoneyToBOZ.java:72`).

CICS client uses local MQ **bindings mode**, request `BOZ.CICS.REQUESTQ`, reply `BOZ.CICS.REPLYQ`, persistent delivery, EBCDIC Cp1047, a CIH followed by target `DBCRFUN ` and the PAYDBCR-shaped COMMAREA (`src/base/mq/java/src/main/java/com/ibm/boq/TransferMoneyToBOZViaCICS.java:33`, `src/base/mq/java/src/main/java/com/ibm/boq/TransferMoneyToBOZViaCICS.java:49`, `src/base/mq/java/src/main/java/com/ibm/boq/TransferMoneyToBOZViaCICS.java:55`, `src/base/mq/java/src/main/java/com/ibm/boq/TransferMoneyToBOZViaCICS.java:59`, `src/base/mq/java/src/main/java/com/ibm/boq/TransferMoneyToBOZViaCICS.java:65`, `src/base/mq/java/src/main/java/com/ibm/boq/TransferMoneyToBOZViaCICS.java:74`). Reply wait is 5,000 ms and COMM-SUCCESS/COMM-FAIL-CODE drive the printed result (`src/base/mq/java/src/main/java/com/ibm/boq/TransferMoneyToBOZViaCICS.java:120`, `src/base/mq/java/src/main/java/com/ibm/boq/TransferMoneyToBOZViaCICS.java:128`, `src/base/mq/java/src/main/java/com/ibm/boq/TransferMoneyToBOZViaCICS.java:132`).

Queue definitions: `.setup/zconfig/mq-queue-manager.yaml:2782`, `.setup/zconfig/mq-queue-manager.yaml:2835`. CICS MQCONN `BOZMQCON` uses configured queue manager and `BOZ.CICS.INITQ`; MQMONITOR `BOZMQMON` monitors the request queue with transaction `CKBR` (`.setup/zconfig/bank-of-z-definitions.yaml:1207`, `.setup/zconfig/bank-of-z-definitions.yaml:1213`, `.setup/zconfig/bank-of-z-definitions.yaml:1217`, `.setup/zconfig/bank-of-z-definitions.yaml:1225`). The template says bridge/adapter definitions come through DFHLIST, not a locally shown CKBR program mapping (`.setup/zconfig/bank-of-z-definitions.yaml:1228`). Queue-manager default `MQ21` is setup data, not a host/port (`.setup/config/config.yaml:91`). Actual bridge installation and partner use remain INT-Q02.

### M02 - Bank of Q client -> IMS MQ/OTMA bridge

Direction **both**, status **L? sample partner path**. Local bindings connection, request `BOZ.IMS.REQUESTQ`, reply `BOZ.IMS.REPLYQ`, persistent messages, Cp1047, LL/ZZ framing and transaction code `IBTRAN  ` are explicit (`src/base/mq/java/src/main/java/com/ibm/boq/TransferMoneyToBOZViaIMS.java:35`, `src/base/mq/java/src/main/java/com/ibm/boq/TransferMoneyToBOZViaIMS.java:81`, `src/base/mq/java/src/main/java/com/ibm/boq/TransferMoneyToBOZViaIMS.java:87`, `src/base/mq/java/src/main/java/com/ibm/boq/TransferMoneyToBOZViaIMS.java:91`, `src/base/mq/java/src/main/java/com/ibm/boq/TransferMoneyToBOZViaIMS.java:96`, `src/base/mq/java/src/main/java/com/ibm/boq/TransferMoneyToBOZViaIMS.java:110`). It strips the customer's leading I, encodes negative amounts as withdrawal `w`, otherwise deposit `d`, sets `MQIMSVS `, and waits 5,000 ms (`src/base/mq/java/src/main/java/com/ibm/boq/TransferMoneyToBOZViaIMS.java:69`, `src/base/mq/java/src/main/java/com/ibm/boq/TransferMoneyToBOZViaIMS.java:76`, `src/base/mq/java/src/main/java/com/ibm/boq/TransferMoneyToBOZViaIMS.java:125`, `src/base/mq/java/src/main/java/com/ibm/boq/TransferMoneyToBOZViaIMS.java:134`).

Queues are configured at `.setup/zconfig/mq-queue-manager.yaml:2888`, `.setup/zconfig/mq-queue-manager.yaml:2941`; the request queue selects storage class IMSBRDG (`.setup/zconfig/mq-queue-manager.yaml:2993`). IMSBRDG supplies XCF group/member from MQ OTMA settings (`.setup/zconfig/mq-queue-manager.yaml:441`); setup group default is `IMSXCFGN`, member follows the IMS datastore (`.setup/config/config.yaml:96`). Transaction-to-program `IBTRAN` is proved in I01; successful deployed routing is not. IBTRAN control flow is **unverified by tooling**.

## Batch jobs, datasets and scheduling (outside JCL/PL/I tool scope)

### B01 - COBOL monthly-statement job

Direction **in** SYSIN/DB2, **out** report/diagnostics; status **L? statement path**, the checked-in card is a sample. Job `BNKSTMTC` executes IKJEFT01, attaches to DB2 subsystem `DBD1`, runs program BNKSTMTC under plan `BANKZPLN` from `BANKZ.V0R1M0.LOAD` (`src/base/batch/jcl/BNKSTMTC.jcl:1`, `src/base/batch/jcl/BNKSTMTC.jcl:29`, `src/base/batch/jcl/BNKSTMTC.jcl:50`). STEPLIB includes that load library, `DB2V13.SDSNEXIT`, `DB2V13.SDSNLOAD`, `DBD1.RUNLIB.LOAD`, `CEE.SCEERUN`, `CEE.SCEERUN2` (`src/base/batch/jcl/BNKSTMTC.jcl:30`).

SYSIN supplies `C000000001 2026-10`; SYSPRINT, SYSUDUMP, CEEDUMP and SYSTSPRT are spool DDs (`src/base/batch/jcl/BNKSTMTC.jcl:42`). COBOL selects SYSIN and SYSPRINT-FILE, reads SYSIN and writes a 133-byte report record (`src/base/batch/cobol/BNKSTMTC.cbl:37`, `src/base/batch/cobol/BNKSTMTC.cbl:43`, `src/base/batch/cobol/BNKSTMTC.cbl:692`, `src/base/batch/cobol/BNKSTMTC.cbl:1867`). These sequential-file boundaries are not listed in data-access.md. BANKZ.ACCOUNT/CUSTOMER/PROCTRAN access is covered by S01/S03/S04.

Operations contract in job comments: RC 0 clean, 4 information (`BNKZI`), 8 errors (`BNKZE`), 12 unavailable SYSOUT (`src/base/batch/jcl/BNKSTMTC.jcl:19`). **CONTRADICTION / incomplete wiring:** the job documents SYSOUT diagnostics but has no SYSOUT DD in its execution DD block; source DISPLAY routing assumes that DD (`src/base/batch/jcl/BNKSTMTC.jcl:16`, `src/base/batch/jcl/BNKSTMTC.jcl:42`, `src/base/batch/cobol/BNKSTMTC.cbl:563`). Runtime output allocation and RC12 behavior are unknown (INT-Q09). BNKSTMTC control flow is **unverified by tooling**.

### B02 - PL/I monthly-statement job

Direction **in** DATECARD/SORTCODE/DB2, **out** spool; status **L? statement path**, checked-in cards are samples. JCL file BNKSTMTP.jcl declares job **BNKSTMT**, executes IKJEFT01 and runs BNKSTMTP under DB2 DBD1 / BANKZPLN from `BANKZ.V0R1M0.LOAD` (`src/base/batch/jcl/BNKSTMTP.jcl:1`, `src/base/batch/jcl/BNKSTMTP.jcl:3`, `src/base/batch/jcl/BNKSTMTP.jcl:20`). STEPLIB uses DB2V13.SDSNEXIT/SDSNLOAD; input cards are DATECARD `202606`, SORTCODE `123456`; SYSPRINT/SYSOUT/SYSTSPRT are spool outputs (`src/base/batch/jcl/BNKSTMTP.jcl:4`, `src/base/batch/jcl/BNKSTMTP.jcl:9`, `src/base/batch/jcl/BNKSTMTP.jcl:14`, `src/base/batch/jcl/BNKSTMTP.jcl:16`). PL/I declares and reads these files, queries ACCOUNT/CUSTOMER/PROCTRAN and emits report lines (`src/base/batch/pli/BNKSTMTP.pli:134`, `src/base/batch/pli/BNKSTMTP.pli:231`, `src/base/batch/pli/BNKSTMTP.pli:262`, `src/base/batch/pli/BNKSTMTP.pli:155`, `src/base/batch/pli/BNKSTMTP.pli:469`, `src/base/batch/pli/BNKSTMTP.pli:170`, `src/base/batch/pli/BNKSTMTP.pli:527`). This is not the same input contract as B01.

### B03 - Setup data population and schedule boundary

Direction **out** setup invocation/data changes; status **legacy-only**. Job DB2INSRT invokes BANKDATA via IKJEFT01 / DSN with configured subsystem/plan/load library and literal generation parameters (`.setup/jcl/cics/Db2-insert.j2:1`, `.setup/jcl/cics/Db2-insert.j2:4`, `.setup/jcl/cics/Db2-insert.j2:9`). Its table writes are S01/S02/S03; loader jobs are S08.

**Schedule unknown:** the two statement JCLs contain dated sample control cards, not an enterprise scheduler/calendar definition (`src/base/batch/jcl/BNKSTMTC.jcl:42`, `src/base/batch/jcl/BNKSTMTP.jcl:9`). "Monthly" describes report intent, not proof of a scheduled production run. No scheduler, cutoff time, predecessor, recipient or delivery channel is established by these jobs; INT-Q10 records the boundary.

## O01 - Operational error interfaces

Direction **out** codes/diagnostics, with **in** abend handling; status **L?** except dummy/test and sample-job paths already classified. These literals are operational identifiers in source, **not evidence of an existing monitor or runbook**.

| Codes / interface | Source use and evidence |
|---|---|
| `HBNK` | Screen error abends: `src/base/cics/cobol/BNK1CAC.cbl:1278`, `src/base/cics/cobol/BNK1CCA.cbl:956`, `src/base/cics/cobol/BNK1CCS.cbl:1639`, `src/base/cics/cobol/BNK1CRA.cbl:954`, `src/base/cics/cobol/BNK1DAC.cbl:1138`, `src/base/cics/cobol/BNK1DCS.cbl:2017`, `src/base/cics/cobol/BNK1TFN.cbl:980`, `src/base/cics/cobol/BNK1UAC.cbl:1385`, `src/base/cics/cobol/BNKMENU.cbl:1290` |
| `PLOP` | Dummy-agency error abends: `src/base/cics/cobol/CRDTAGY1.cbl:172`, `src/base/cics/cobol/CRDTAGY2.cbl:172`, `src/base/cics/cobol/CRDTAGY3.cbl:171`, `src/base/cics/cobol/CRDTAGY4.cbl:173`, `src/base/cics/cobol/CRDTAGY5.cbl:172` |
| `HNCS`, `HWPT` | CREACC CONTROL/counter-related and PROCTRAN failure paths; CRECUST PROCTRAN path: `src/base/cics/cobol/CREACC.cbl:576`, `src/base/cics/cobol/CREACC.cbl:1154`, `src/base/cics/cobol/CRECUST.cbl:1456`; INQACC HNCS: `src/base/cics/cobol/INQACC.cbl:933` |
| `HRAC`, `CVR1`, `WPV6`, `WPV7`, `HWPT` | Account/customer read/deletion/history diagnostics: `src/base/cics/cobol/INQACC.cbl:335`, `src/base/cics/cobol/DELACC.cbl:339`, `src/base/cics/cobol/INQCUST.cbl:518`, `src/base/cics/cobol/DELCUS.cbl:547`, `src/base/cics/cobol/DELCUS.cbl:703`, `src/base/cics/cobol/DELCUS.cbl:871`, `src/base/cics/cobol/DELACC.cbl:613` |
| `ITRL`, `ITRD` | Transaction-list/detail technical abends, distinct from same-spelled API EIB metadata: `src/base/cics/cobol/INQTRANL.cbl:561`, `src/base/cics/cobol/INQTRAND.cbl:338` |
| `SAME`, `FROM`, `TO  `, `RUF2`, `RUF3`, `WPCD`, `WPCT` | XFRFUN account/update/history paths: `src/base/cics/cobol/XFRFUN.cbl:371`, `src/base/cics/cobol/XFRFUN.cbl:580`, `src/base/cics/cobol/XFRFUN.cbl:489`, `src/base/cics/cobol/XFRFUN.cbl:1346`, `src/base/cics/cobol/XFRFUN.cbl:1546`, `src/base/cics/cobol/XFRFUN.cbl:1725`, `src/base/cics/cobol/XFRFUN.cbl:1862`. Recorded code RUF2 differs from raised RUF3 in one path: existing XFRFUN-Q04. |
| `HROL`, `AD2Z`, `AFCR`, `AFCS`, `AFCT` | HROL is raised on rollback/error paths; handlers explicitly inspect the other literals. Examples across DBCRFUN, INQACC, INQACCCU, INQCUST, INQACCS and XFRFUN: `src/base/cics/cobol/DBCRFUN.cbl:639`, `src/base/cics/cobol/DBCRFUN.cbl:728`, `src/base/cics/cobol/DBCRFUN.cbl:750`, `src/base/cics/cobol/INQACC.cbl:651`, `src/base/cics/cobol/INQACCCU.cbl:708`, `src/base/cics/cobol/INQCUST.cbl:564`, `src/base/cics/cobol/INQACCS.cbl:362`, `src/base/cics/cobol/XFRFUN.cbl:1936`, `src/base/cics/cobol/XFRFUN.cbl:1952`. Reachability of VSAM storm-drain branches remains XFRFUN-Q05. |
| RESP/RESP2, SQLCODE and free-form diagnostic record | ABNDPROC COMMAREA carries these fields; the write can fail independently of the caller: `src/base/cics/cobol/ABNDPROC.cbl:110`, `src/base/cics/cobol/ABNDPROC.cbl:121`, `src/base/cics/cobol/ABNDPROC.cbl:149` |
| COMM-SUCCESS / COMM-FAIL-CODE | Business result consumed by callers/MQ clients, not interchangeable with ABEND/SQLCODE: `src/base/mq/java/src/main/java/com/ibm/boq/TransferMoneyToBOZViaCICS.java:128`, `src/base/mq/java/src/main/java/com/ibm/boq/TransferMoneyToBOZViaCICS.java:132`, `src/base/cics/cobol/CRECUST.cbl:697` |
| IMS DBSTAT / TPSTAT | DL/I database/message status handling, not CICS RESP: `src/base/ims/cobol/IBTRAN.cbl:260`, `src/base/ims/cobol/IBGCUDAT.cbl:187`; program verification limits above apply |
| BNKZI / BNKZE, RC 0/4/8/12 | Batch diagnostics/return-code contract B01; `src/base/batch/jcl/BNKSTMTC.jcl:19` |

## Source-of-truth check

### Every call-graph edge -> entry

Reconciliation baseline: **the complete Edges [scan] table**, not just the deduplicated Mermaid diagram (`docs/modernization/00-source-of-truth/call-graph.md:191`). Repeated statements are included: C01 cites every RETURN statement, C02 every business LINK, C03 lists every diagnostic LINK line, C05 every LE call, I02 every JNI call. The following exhaustive selectors map every row; **missing: none**. A covered unresolved edge is not a resolved runtime target.

| Graph row selector (from / kind / to) | Integration entry |
|---|---|
| BANKDATA / CALL / CEEDATM, CEEGMT | C05, B03 |
| BNK1CAC / LINK / CREACC | C02 |
| BNK1CCA / LINK / INQACCCU | C02 |
| BNK1CCS / LINK / CRECUST | C02 |
| BNK1CRA / LINK / DBCRFUN | C02 |
| BNK1DAC / LINK / DELACC, INQACC | C02 |
| BNK1DCS / LINK / DELCUS, INQCUST, UPDCUST | C02 |
| BNK1TFN / LINK / XFRFUN | C02 |
| BNK1UAC / LINK / INQACC, UPDACC | C02 |
| CREACC / LINK / INQACCCU, INQCUST | C02 |
| DELCUS / LINK / DELACC, INQACCCU, INQCUST | C02 |
| INQACCCU / LINK / INQCUST | C02 |
| BNK1CAC / RETURN TRANSID / OCAC, OMEN | C01 |
| BNK1CCA / RETURN TRANSID / OCCA, OMEN | C01 |
| BNK1CCS / RETURN TRANSID / OCCS (both sites), OMEN | C01 |
| BNK1CRA / RETURN TRANSID / OCRA, OMEN | C01 |
| BNK1DAC / RETURN TRANSID / ODAC, OMEN | C01 |
| BNK1DCS / RETURN TRANSID / ODCS, OMEN | C01 |
| BNK1TFN / RETURN TRANSID / OMEN, OTFN | C01 |
| BNK1UAC / RETURN TRANSID / OMEN, OUAC | C01 |
| BNKMENU / RETURN TRANSID / OCAC, OCCA, OCCS, OCRA, ODAC, ODCS, OMEN, OTFN, OUAC | C01 |
| Each of BNK1CAC, BNK1CCA, BNK1CCS, BNK1CRA, BNK1DAC, BNK1DCS, BNK1TFN, BNK1UAC, BNKMENU / LINK / ABNDPROC | C03, caller-specific line lists |
| Each of CRDTAGY1, CRDTAGY2, CRDTAGY3, CRDTAGY4, CRDTAGY5 / LINK / ABNDPROC | C03, caller-specific line lists |
| Each of CREACC, CRECUST, DBCRFUN, DELACC, DELCUS, INQACC, INQACCCU, INQCUST, INQTRAND, INQTRANL, XFRFUN / LINK / ABNDPROC | C03, caller-specific line lists |
| CRECUST / RUN TRANSID / ?WS-RUN-TRANSID | C04, manual target-set inference below |
| CRECUST / CALL / CEEDAYS, CEELOCT | C05 |
| IBTRAN / CALL / ?CALLSTATICVOIDMETHOD, ?DELETELOCALREF (both sites), ?FINDCLASS, ?GETSTATICMETHODID, ?NEWDIRECTBYTEBUFFER | I02, source-level JNI identities; runtime addresses still unresolved |
| IBTRAN / CALL / __ETOA (all three sites) | C05, I02 |

The diagram's nine dotted `runs` arrows and all nine bank screen configuration rows map to C01. The five OCR configuration rows map to C04. **Every remaining [config] transaction row**, CDT#, CSKL, DTCD, DTCI, DTCN, DTCP, DTCX, DTNP, DTSC, DTSI, DTST, EZAC, EZAO, EZAP, LSN0, SRV1..SRV6, maps to C08 (`docs/modernization/00-source-of-truth/call-graph.md:155`). No XCTL or START TRANSID edge is reported in this baseline; that is a statement about the inventory, not proof about arbitrary installed modules.

### Every unresolved `?` target

| Target | Resolution and proof boundary |
|---|---|
| `?WS-RUN-TRANSID` | C04: literal `OCR` + PIC 9 WS-CC-CNT, loop 1..5, RUN statement prove the construction (`src/base/cics/cobol/CRECUST.cbl:231`, `src/base/cics/cobol/CRECUST.cbl:620`, `src/base/cics/cobol/CRECUST.cbl:626`, `src/base/cics/cobol/CRECUST.cbl:687`). OCR1..OCR5 -> CRDTAGY1..5 is separately proved by C04 resource citations. **Inference:** the finite target set from those assignments. **Still unresolved:** actual installed routing/runtime success; parser control flow remains unverified. |
| `?DELETELOCALREF` | I02: function-pointer at `src/base/ims/copy/JNI.cpy:107`, calls at `src/base/ims/cobol/IBTRAN.cbl:281` and `src/base/ims/cobol/IBTRAN.cbl:284`; source-level JNI operation identified, runtime address still unresolved. No CICS resource applies. |
| `?NEWDIRECTBYTEBUFFER` | I02: function-pointer at `src/base/ims/copy/JNI.cpy:313`, buffer address/length passed at `src/base/ims/cobol/IBTRAN.cbl:470`, call at `src/base/ims/cobol/IBTRAN.cbl:474`; runtime address still unresolved. No CICS resource applies. |
| `?CALLSTATICVOIDMETHOD` | I02: pointer `src/base/ims/copy/JNI.cpy:225`, invocation `src/base/ims/cobol/IBTRAN.cbl:490`; class/method strings and lookup at `src/base/ims/cobol/IBTRAN.cbl:516`, `src/base/ims/cobol/IBTRAN.cbl:540`, `src/base/ims/cobol/IBTRAN.cbl:554`, Java declaration `src/base/ims/java/src/main/java/nazare/jmp/controller/InsertHist.java:33`. Method association follows handle use; actual runtime IDs/address still unresolved. No CICS resource applies. |
| `?FINDCLASS` | I02: pointer `src/base/ims/copy/JNI.cpy:90`, string and call `src/base/ims/cobol/IBTRAN.cbl:516`, `src/base/ims/cobol/IBTRAN.cbl:522`; source requested class proved, loaded runtime class/address still unresolved. No CICS resource applies. |
| `?GETSTATICMETHODID` | I02: pointer `src/base/ims/copy/JNI.cpy:197`, method/signature and call `src/base/ims/cobol/IBTRAN.cbl:540`, `src/base/ims/cobol/IBTRAN.cbl:541`, `src/base/ims/cobol/IBTRAN.cbl:554`; source request proved, runtime method ID/address still unresolved. No CICS resource applies. |

### Every data-access row -> entry

| Source-of-truth row | Covering entry and complete row contents |
|---|---|
| DB2 ACCOUNT (`docs/modernization/00-source-of-truth/data-access.md:9`) | S01: BANKDATA DELETE/INSERT; BNKSTMTC ACCT-CURSOR as BANKZ.ACCOUNT; CREACC INSERT; DBCRFUN SELECT/UPDATE; DELACC DELETE/SELECT; INQACC SELECT/ACC-CURSOR; INQACCCU ACC-CURSOR; INQACCS ACC-ALL-CURSOR; UPDACC SELECT/UPDATE; XFRFUN SELECT/UPDATE |
| DB2 CONTROL (`docs/modernization/00-source-of-truth/data-access.md:10`) | S02: BANKDATA DELETE/INSERT/UPDATE; CREACC SELECT/UPDATE; CRECUST SELECT/UPDATE |
| DB2 CUSTOMER (`docs/modernization/00-source-of-truth/data-access.md:11`) | S03: BANKDATA DELETE/INSERT; BNKSTMTC SELECT as BANKZ.CUSTOMER; DELCUS DELETE/SELECT; INQCUST SELECT; UPDCUST SELECT/UPDATE. **Additional source-only CRECUST INSERT** is explicitly marked and remains CONTRADICTION DM-Q07. |
| DB2 PROCTRAN (`docs/modernization/00-source-of-truth/data-access.md:12`) | S04: BNKSTMTC TRAN-CURSOR as BANKZ.PROCTRAN; CREACC/CRECUST/DBCRFUN/DELACC/DELCUS/XFRFUN INSERT; INQTRAND TRAND-CURSOR; INQTRANL TRAN-COUNT-CURSOR/TRAN-CURSOR |
| CICS file ABNDFILE (`docs/modernization/00-source-of-truth/data-access.md:18`) | S05: ABNDPROC WRITE; dataset/config extension identified separately |
| Queues: none found (`docs/modernization/00-source-of-truth/data-access.md:22`) | No scanned EXEC CICS queue-access row to map. This does **not** negate MQ M01/M02, IMS message calls S06 or configuration-only TD queues C08. |
| IBACSUM DL/I (`docs/modernization/00-source-of-truth/data-access.md:28`) | S06: GHU, GU, ISRT, NEXT-CALL, with message versus database distinction and GHU/GHN assignments |
| IBGCUDAT DL/I (`docs/modernization/00-source-of-truth/data-access.md:29`) | S06: GU, ISRT |
| IBLOGIN1 DL/I (`docs/modernization/00-source-of-truth/data-access.md:30`) | S06: GHU, GU, ISRT, REPL; no alias to configured IBLOGIN asserted |
| IBLOGOUT DL/I (`docs/modernization/00-source-of-truth/data-access.md:31`) | S06: GHU, GU, ISRT, REPL |
| IBSCUDAT DL/I (`docs/modernization/00-source-of-truth/data-access.md:32`) | S06: GHU, GU, ISRT, REPL |
| IBTRAN DL/I (`docs/modernization/00-source-of-truth/data-access.md:33`) | S06: GHU, GU, ISRT, NEXT-CALL, REPL; control flow unverified by tooling |
| LOADACCT DL/I (`docs/modernization/00-source-of-truth/data-access.md:34`) | S06/S08: ISRT ACCOUNT |
| LOADCUSA DL/I (`docs/modernization/00-source-of-truth/data-access.md:35`) | S06/S08: ISRT CUSTACCS |
| LOADCUST DL/I (`docs/modernization/00-source-of-truth/data-access.md:36`) | S06/S08: ISRT CUSTOMER |
| LOADHIST DL/I (`docs/modernization/00-source-of-truth/data-access.md:37`) | S06/S08: ISRT HISTORY |
| LOADTSTA DL/I (`docs/modernization/00-source-of-truth/data-access.md:38`) | S06/S08: ISRT TSTAT |

**Missing data-access rows from this inventory: none.** Completeness relative to those rows is not completeness of the scanner.

### Integrations beyond the two generated maps

| Entries | Why absent / scope marking |
|---|---|
| C04 channel/container/FETCH, S02 ENQ/DEQ, O01 codes/RESP/SQLCODE | COBOL commands/values visible in program digests or direct source, but not represented as call/data-map edges. Not outside language scope. CRECUST GET CONTAINER omission/false DISPLAY match is logged as INT-Q11. |
| C06 utility entry points | No incoming graph edge; direct COMMAREA source evidence, callers unknown. BNK1B2B configuration-only extension is **outside tool scope**. |
| C07 BMS, C08 support files/queues/HTTP | **outside tool scope**; C08 transaction mappings themselves are already [config], not newly inferred. |
| C09 IPIC/DB2 attach, DB2TRAN, CSMT diagnostics and load-library resources | **outside tool scope**; DB2TRAN associates a transaction with an attachment entry, not a program. |
| S01/S03/S04 PL/I accesses, S07 DBD/PSB/dataset mappings, S08 JCL wiring | **outside tool scope**. S08 COBOL SELECT/READ files are instead missing coverage of the map's current scanner. |
| S03 CRECUST INSERT | In COBOL scope, omitted by scanner; existing CONTRADICTION DM-Q07, not silently repaired or labelled outside scope. |
| I01 IMS transaction definitions / PL/I, I02 JNI interpretation / Java / JDBC, I03 Java history | **outside tool scope** except the COBOL CALL sites already reconciled. DB2 IMSBANK.HISTORY is not in the EXEC SQL table map. |
| A01 all REST mappings, A02 provider connections, A03 HTTP/browser/CDN/OAuth metadata | **outside tool scope**; each cited directly, not presented as parsed control flow or live service proof. |
| M01/M02 Java JMS, queues, CICS/OTMA bridge definitions | **outside tool scope**; no contradiction with "Queues: none found" for EXEC CICS. |
| B01/B02/B03 JCL, PL/I, scheduler boundary | **outside tool scope**; B01 COBOL SQL is covered by S01/S03/S04; sequential-file IO is not captured in data-access.md. No scheduler inferred. |

Open questions added by this pass are INT-Q01..INT-Q11. Existing DM-Q02/DM-Q03/DM-Q07/DM-Q08 and XFRFUN-Q04/Q05 remain applicable. No generated facts or legacy sources were changed.
