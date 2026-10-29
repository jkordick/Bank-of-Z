---
name: cobol-dependency-scan
description: Lists what a COBOL program depends on - COPY members, EXEC SQL INCLUDEs, EXEC CICS LINK / XCTL / RETURN TRANSID / START / RUN, static CALLs and IMS DL/I calls - with file:LINE citations. Use when asked what a program calls, includes, links to or depends on, or before drawing a dependency diagram. Lightweight (awk only); for control flow, decisions and data items use the cobol-rekt skill instead.
---

1. Run the scan from the repository root, passing one or more program names
   (case-insensitive) or paths:

   ```bash
   .github/skills/cobol-dependency-scan/scan.sh BNKMENU
   .github/skills/cobol-dependency-scan/scan.sh XFRFUN src/base/cics/cobol/INQCUST.cbl
   .github/skills/cobol-dependency-scan/scan.sh --all
   ```

2. Use the table as the factual basis of your answer. Cite the `file:LINE`
   values exactly as printed.

3. Rows marked **variable** hold the target in a data item (e.g.
   `PROGRAM(WS-ABEND-PGM)`). Resolve them by searching the same program for the
   item's `VALUE` clause or `MOVE '...' TO <item>` and cite that line too. If you
   cannot resolve it, say so - never guess a program name.

4. For `IMS DL/I` rows marked "function on next line", read the following line
   to find the DL/I function code (`GU`, `GHU`, `GN`, `ISRT`, `REPL`, `DLET`).

5. Targets that are not in the repository (IBM-supplied `CEE*` services,
   `DFH*` copybooks, BMS symbolic maps generated at build time) are expected -
   label them as external rather than as missing code.

6. If the user asks for a diagram, render it as a mermaid `flowchart` with
   `LINK` / `XCTL` / `CALL` / `RETURN TRANSID` as edge labels.
