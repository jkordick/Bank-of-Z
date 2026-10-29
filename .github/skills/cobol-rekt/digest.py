#!/usr/bin/env python3
"""digest.py - turn cobol-rekt output plus a scan of the ORIGINAL sources into
small, citeable markdown files that agents (and humans) can actually read.

Every fact in the output is tagged with its provenance:

  [rekt]  derived from the cobol-rekt parse tree (flow AST / data structures)
  [scan]  deterministic regex scan of the original source file

Nothing here uses an LLM. Re-running on the same sources yields the same files.
"""

from __future__ import annotations

import argparse
import bisect
import json
import re
from collections import defaultdict
from pathlib import Path

RESERVED_HEADERS = {
    "EXIT", "GOBACK", "CONTINUE", "END-IF", "END-EVALUATE", "END-PERFORM",
    "END-EXEC", "DECLARATIVES", "STOP", "ELSE",
}
CICS_TWO_WORD = {"SEND", "RECEIVE", "HANDLE", "GET", "PUT", "WRITEQ", "READQ", "DELETEQ",
                 "INQUIRE", "SET", "IGNORE", "PUSH", "POP", "ISSUE", "FETCH"}
STATEMENT_START = ("IF|ELSE|END-[A-Z]+|MOVE|PERFORM|DISPLAY|CALL|EXEC|GO|GOBACK|ADD|SUBTRACT|COMPUTE|"
                   "MULTIPLY|DIVIDE|EVALUATE|WHEN|STRING|UNSTRING|INITIALIZE|SET|CONTINUE|STOP|EXIT|READ|WRITE|"
                   "OPEN|CLOSE|ACCEPT|INSPECT|SEARCH|RETURN|REWRITE|DELETE")
CICS_OPTIONS = ("PROGRAM", "FILE", "DATASET", "MAP", "MAPSET", "TRANSID", "QUEUE", "QNAME",
                "CHANNEL", "CONTAINER", "ABCODE", "SYSID", "TS", "TD")
IBM_STUB_HINTS = (
    (re.compile(r"^DFH"), "IBM-supplied CICS copybook (not in repo)"),
    (re.compile(r"^CEE"), "IBM Language Environment copybook (not in repo)"),
    (re.compile(r"^(SQLCA|SQLDA)$"), "DB2 precompiler member (not in repo)"),
)
MAX_DATA_ROWS = 150


# --------------------------------------------------------------------------- utils

def collapse(text: str) -> str:
    """Collapse whitespace outside quoted literals ('TO  ' keeps its two blanks)."""
    text = text.replace("\u200b", "")
    parts = re.split(r"('[^']*'|\"[^\"]*\")", text)
    return "".join(p if i % 2 else re.sub(r"\s+", " ", p) for i, p in enumerate(parts)).strip()


GAP_PRIORITY = ("Syntax error on", "Extraneous input", "Unknown type", "Exception", "Error", "No viable")


def gap_summary(reasons: list) -> list:
    """Most telling parser-gap reasons first."""
    def rank(r):
        return next((i for i, k in enumerate(GAP_PRIORITY) if k in r), len(GAP_PRIORITY))
    return sorted((r for r in reasons if "DiagnosticRuntimeError" not in r), key=rank)


def shorten(text: str, n: int) -> str:
    return text if len(text) <= n else text[: n - 1] + "…"


def md_escape(text: str) -> str:
    return text.replace("|", "\\|").replace("`", "'")


def node_id(name: str) -> str:
    return "n_" + re.sub(r"[^A-Za-z0-9_]", "_", name)


class Source:
    """An original (un-normalised) COBOL source file."""

    def __init__(self, root: Path, rel: str):
        self.rel = rel
        self.name = Path(rel).name
        text = (root / rel).read_text(encoding="latin-1")
        self.lines = text.splitlines()

    def code(self, lineno: int) -> str:
        line = self.lines[lineno - 1] if 0 < lineno <= len(self.lines) else ""
        return line[7:72].upper() if len(line) > 7 else ""

    def text(self, lineno: int) -> str:
        """Code area (cols 8-72) in original case, for quoting."""
        line = self.lines[lineno - 1] if 0 < lineno <= len(self.lines) else ""
        return line[7:72] if len(line) > 7 else ""

    def live(self, lineno: int) -> bool:
        line = self.lines[lineno - 1]
        if len(line) < 7:
            return False
        return line[6] not in "*/" and line[7:72].strip() != ""

    def live_lines(self):
        for i in range(1, len(self.lines) + 1):
            if self.live(i):
                yield i, self.code(i)

    def ref(self, lineno) -> str:
        return f"`{self.rel}:{lineno}`" if lineno else f"`{self.rel}`"


# --------------------------------------------------------------------------- source scan

def scan_program(src: Source) -> dict:
    proc_start = next((i for i, c in src.live_lines() if re.search(r"\bPROCEDURE\s+DIVISION\b", c)), None)

    headers = []  # (lineno, name, kind)
    if proc_start:
        for i, c in src.live_lines():
            if i <= proc_start:
                continue
            lead = len(c) - len(c.lstrip())
            m = re.fullmatch(r"([A-Z0-9][A-Z0-9-]*)(\s+SECTION)?\s*\.?\s*", c.strip())
            if lead <= 3 and m and m.group(1) not in RESERVED_HEADERS and c.strip().endswith("."):
                headers.append((i, m.group(1), "section" if m.group(2) else "paragraph"))
    starts = [h[0] for h in headers]
    ranges = {}
    for idx, (ln, name, kind) in enumerate(headers):
        end = headers[idx + 1][0] - 1 if idx + 1 < len(headers) else len(src.lines)
        ranges.setdefault(name, (ln, end, kind))

    def para_at(lineno):
        k = bisect.bisect_right(starts, lineno) - 1
        return headers[k][1] if k >= 0 else "(data division)"

    execs, calls, copies = [], [], []
    block = None
    for i, c in src.live_lines():
        if block is not None:
            block["text"] += " " + c
            block["orig"] += " " + src.text(i)
            if "END-EXEC" in c:
                execs.append(finish_exec(block))
                block = None
            continue
        m = re.search(r"\bEXEC\s+(CICS|SQL|DLI)\b(.*)", c)
        if m:
            block = {"line": i, "kind": m.group(1), "text": m.group(2), "para": para_at(i),
                     "orig": src.text(i)[m.start(2):]}
            if "END-EXEC" in c:
                execs.append(finish_exec(block))
                block = None
            continue
        for cm in re.finditer(r"\bCOPY\s+[\"']?([A-Z0-9#@$-]+)", c):
            copies.append((i, cm.group(1)))
        lits = [m_.span() for m_ in re.finditer(r"'[^']*'|\"[^\"]*\"", c)]
        for cm in re.finditer(r"(?<![A-Z0-9-])CALL\s+('[^']+'|\"[^\"]+\"|[A-Z0-9-]+)", c):
            if any(a_ <= cm.start() < b_ for a_, b_ in lits):
                continue  # inside a literal, e.g. DISPLAY 'CALL GU'
            target = cm.group(1)
            stmt = collapse(src.text(i)[cm.start():])
            j = i + 1
            while j <= min(i + 4, len(src.lines)) and not stmt.endswith("."):
                if src.live(j):
                    if re.match(r"\s*(" + STATEMENT_START + r")\b", src.code(j)):
                        break
                    stmt = collapse(stmt + " " + src.text(j))
                j += 1
            calls.append({"line": i, "target": target, "stmt": stmt[:110], "para": para_at(i)})
    for e in execs:
        if e["kind"] == "SQL" and e["verb"] == "INCLUDE" and e["targets"]:
            copies.append((e["line"], e["targets"][0]))

    # literal values that a variable may hold (for resolving PROGRAM(var) / CALL var)
    literals = defaultdict(list)
    pending = None
    for i, c in src.live_lines():
        for vm in re.finditer(r"\b(\d\d)\s+([A-Z0-9-]+)\b[^.]*?\bVALUE\s+(?:IS\s+)?'([^']*)'", c):
            literals[vm.group(2)].append(("VALUE clause", vm.group(3).strip(), i))
        for mm in re.finditer(r"\bMOVE\s+'([^']*)'\s+TO\s+([A-Z0-9-]+)", c):
            literals[mm.group(2)].append(("MOVE literal", mm.group(1).strip(), i))
        if pending:  # MOVE 'X' on one line, TO var on the next
            tm = re.match(r"\s*TO\s+([A-Z0-9-]+)", c)
            if tm:
                literals[tm.group(1)].append(("MOVE literal", pending[0], pending[1]))
            pending = None
        pm = re.search(r"\bMOVE\s+'([^']*)'\s*$", c)
        if pm:
            pending = (pm.group(1).strip(), i)

    return {"proc_start": proc_start, "headers": headers, "ranges": ranges, "execs": execs,
            "calls": calls, "copies": copies, "literals": literals, "para_at": para_at}


def finish_exec(block: dict) -> dict:
    text = collapse(block["text"].split("END-EXEC")[0])
    words = re.findall(r"[A-Z0-9#@$_-]+(?:\([^)]*\))?", text)
    bare = [w.split("(")[0] for w in words]
    verb = bare[0] if bare else "?"
    if block["kind"] == "CICS" and verb in CICS_TWO_WORD and len(words) > 1 and "(" not in words[1]:
        verb = f"{verb} {bare[1]}"
    targets = []
    if block["kind"] == "CICS":
        for opt, val in re.findall(r"\b(" + "|".join(CICS_OPTIONS) + r")\s*\(\s*([^)]*?)\s*\)", text):
            targets.append(f"{opt}({val})")
    elif block["kind"] == "SQL":
        if verb == "INCLUDE" and len(bare) > 1:
            targets.append(bare[1])
        kw = r"(?<![A-Z0-9_:#@$-])"
        for pat in (kw + r"FROM\s+([A-Z][A-Z0-9_.#@$]*)", kw + r"INSERT\s+INTO\s+([A-Z][A-Z0-9_.#@$]*)",
                    r"^UPDATE\s+([A-Z][A-Z0-9_.#@$]*)", kw + r"JOIN\s+([A-Z][A-Z0-9_.#@$]*)",
                    r"\bDECLARE\s+([A-Z0-9_.#@$-]+)\s+(?:CURSOR|TABLE)",
                    r"^(?:OPEN|CLOSE|FETCH)\s+(?:NEXT\s+)?(?:FROM\s+)?([A-Z0-9_#@$-]+)"):
            for t in re.findall(pat, text):
                if t not in targets and not t.startswith(":"):
                    targets.append(t)
    shown = collapse(re.split(r"END-EXEC", block["orig"], flags=re.I)[0])
    return {**block, "text": text, "shown": shown, "verb": verb, "targets": targets}


def scan_conditions(src: Source) -> list:
    """88-level condition names: (lineno, name, values, parent)."""
    out, parent, i = [], None, 1
    n = len(src.lines)
    while i <= n:
        if not src.live(i):
            i += 1
            continue
        c = src.code(i).strip()
        m88 = re.match(r"88\s+([A-Z0-9-]+)\s*(.*)", c)
        if m88:
            text, j = m88.group(2), i
            while not text.rstrip().endswith(".") and j < n:
                j += 1
                if src.live(j):
                    text += " " + src.code(j).strip()
            vals = re.sub(r"^VALUES?\s+(ARE\s+|IS\s+)?", "", collapse(text)).rstrip(".")
            out.append((i, m88.group(1), vals, parent))
            i = j + 1
            continue
        md = re.match(r"(\d\d)\s+([A-Z0-9-]+)", c)
        if md and md.group(1) != "88":
            parent = md.group(2)
        i += 1
    return out


def find_data_def(sources: list, level: int, name: str):
    pat = re.compile(r"^\s*0?" + str(level) + r"\s+" + re.escape(name) + r"(\s|\.|$)")
    for s in sources:
        for i, c in s.live_lines():
            if pat.match(c):
                return s, i
    return None, None


def source_01_items(src: Source) -> list:
    """All 01/77 items in the program with the DATA DIVISION section they live in."""
    out, section = [], "?"
    for i, c in src.live_lines():
        sm = re.match(r"\s*(FILE|WORKING-STORAGE|LOCAL-STORAGE|LINKAGE)\s+SECTION\b", c)
        if sm:
            section = sm.group(1)
            continue
        if re.search(r"\bPROCEDURE\s+DIVISION\b", c):
            break
        m = re.match(r"\s*(01|77)\s+([A-Z0-9-]+)", c)
        if m and m.group(2) != "FILLER":
            out.append((i, m.group(2), section))
    return out


# --------------------------------------------------------------------------- cobol-rekt output

def rekt_flow(flow: dict) -> list:
    """Linearise the flow AST into ordered events."""
    events = []

    def first_line(node):
        return collapse((node.get("originalText") or "").split("\n")[0].split("_DIALECT_")[0])

    def rec(node, depth, top):
        t = node.get("type")
        name = node.get("name", "")
        if t == "SECTION":
            events.append({"ev": "section", "name": name})
            return
        if t in ("PARAGRAPH",) or (t == "ATOMIC" and top and "/" not in name
                                    and collapse(node.get("originalText", "")).rstrip(".") == name):
            events.append({"ev": "para", "name": name})
            return
        if t == "PERFORM":
            events.append({"ev": "perform", "text": first_line(node), "depth": depth})
        elif t == "GOTO":
            events.append({"ev": "goto", "text": first_line(node), "depth": depth})
        elif t == "IF_BRANCH":
            events.append({"ev": "if", "text": first_line(node), "depth": depth})
        elif t == "EVALUATE":
            events.append({"ev": "evaluate", "text": first_line(node), "depth": depth})
        child_depth = depth + (1 if t in ("IF_BRANCH", "EVALUATE") else 0)
        for ch in node.get("children", []):
            rec(ch, child_depth, top and t == "PROCEDURE_DIVISION_BODY")

    rec(flow, 0, True)
    return events


def rekt_data(data: dict) -> list:
    """Flat list of data items; each carries `_fields` = number of subordinate items."""
    rows = []

    def rec(node, depth):
        count = 0
        for ch in node.get("children", []):
            lvl = ch.get("levelNumber", -1)
            nm = ch.get("name", "")
            if lvl >= 1 and not nm.startswith(("[", "TypedRecord")) and ch.get("sourceSection") != "PROCEDURE_DIVISION":
                row = dict(ch, _depth=depth)
                rows.append(row)
                row["_fields"] = rec(ch, depth + 1)
                count += 1 + row["_fields"]
        return count

    rec(data, 0)
    return rows


# --------------------------------------------------------------------------- per program

def program_digest(name, rel, ctx) -> dict:
    root, raw = ctx["root"], ctx["raw"]
    src = Source(root, rel)
    scan = scan_program(src)
    status_file = raw / "status" / name
    status_lines = status_file.read_text().splitlines() if status_file.exists() else ["not-run"]
    status = status_lines[0]
    gap_reasons = gap_summary([collapse(x) for x in status_lines[1:] if x.strip()])
    # Parser positions refer to the pre-processed text (copybooks expanded), so
    # map offending tokens back to the original source by searching for them.
    gap_at = []  # (reason, [lines])
    for reason in gap_reasons:
        tm = re.match(r"(?:Syntax error on '|Extraneous input )([A-Z0-9-]{3,})'?$", reason)
        if not tm:
            continue
        tok = tm.group(1)
        hits = [i for i, c in src.live_lines() if re.search(r"(?<![A-Z0-9-])" + re.escape(tok) + r"(?![A-Z0-9-])", c)]
        gap_at.append((reason, hits))

    # transitive copybooks that exist in the repo
    copy_srcs, seen, queue = [], set(), [c for _, c in scan["copies"]]
    while queue:
        cp = queue.pop(0)
        if cp in seen:
            continue
        seen.add(cp)
        if cp in ctx["copybooks"]:
            cs = Source(root, ctx["copybooks"][cp])
            copy_srcs.append(cs)
            queue += [c for _, c in scan_program(cs)["copies"]]

    report = raw / "rekt" / f"{src.name}.report"
    flow_path = report / "flow_ast" / f"flow-ast-{src.name}.json"
    data_path = report / "data_structures" / f"{src.name}-data.json"
    events = rekt_flow(json.loads(flow_path.read_text())) if status == "ok" and flow_path.exists() else None
    data_rows = rekt_data(json.loads(data_path.read_text())) if status == "ok" and data_path.exists() else None

    known = set(scan["ranges"])
    if events:
        known |= {e["name"] for e in events if e["ev"] in ("section", "para")}

    per_para = defaultdict(lambda: {"perform": [], "goto": [], "decisions": []})
    edges = []
    cursor = {}
    current = None

    def locate(para, text):
        rng = scan["ranges"].get(para)
        if not rng or not text:
            return None
        start = cursor.get(para, rng[0])
        for ln in range(start, rng[1] + 1):
            if src.live(ln) and collapse(src.code(ln)).startswith(text.upper()[:60]):
                cursor[para] = ln + 1
                return ln
        return None

    if events:
        for e in events:
            if e["ev"] in ("section", "para"):
                current = e["name"]
                continue
            para = current or "(entry)"
            ln = locate(para, e["text"])
            if e["ev"] == "perform":
                m = re.match(r"PERFORM\s+([A-Z0-9][A-Z0-9-]*)(?:\s+(?:THRU|THROUGH)\s+([A-Z0-9][A-Z0-9-]*))?(.*)", e["text"])
                if m and m.group(1) in known:
                    tgt = m.group(1) + (f" THRU {m.group(2)}" if m.group(2) else "")
                    loop = bool(re.search(r"\b(UNTIL|VARYING|TIMES)\b", m.group(3) or ""))
                    per_para[para]["perform"].append((ln, tgt, loop))
                    edges.append((para, m.group(1), "PERFORM"))
            elif e["ev"] == "goto":
                m = re.match(r"GO\s+TO\s+(.*?)(?:\s+DEPENDING\b.*)?$", e["text"])
                for tgt in re.findall(r"[A-Z0-9][A-Z0-9-]*", m.group(1) if m else ""):
                    if tgt in known:
                        per_para[para]["goto"].append((ln, tgt))
                        edges.append((para, tgt, "GO TO"))
            else:
                per_para[para]["decisions"].append((ln, e["ev"], e["depth"], decision_text(src, ln, e)))

    execs_by_para = defaultdict(lambda: {"CICS": 0, "SQL": 0})
    for x in scan["execs"]:
        execs_by_para[x["para"]][x["kind"]] = execs_by_para[x["para"]].get(x["kind"], 0) + 1
    dli_calls = [c for c in scan["calls"] if c["target"].strip("'\"") in ("CBLTDLI", "AIBTDLI", "CEETDLI")]

    conditions = []
    for s in [src] + copy_srcs:
        for ln, cname, vals, parent in scan_conditions(s):
            conditions.append((s, ln, cname, vals, parent))

    deps = []  # program-to-program edges
    for x in scan["execs"]:
        if x["kind"] == "CICS" and x["verb"] in ("LINK", "XCTL"):
            for t in x["targets"]:
                if t.startswith("PROGRAM("):
                    deps.append((x["line"], x["verb"], *resolve(t[8:-1], scan["literals"]), x["para"]))
        if x["kind"] == "CICS" and x["verb"] in ("RETURN", "START", "RUN"):
            for t in x["targets"]:
                if t.startswith("TRANSID("):
                    deps.append((x["line"], f"{x['verb']} TRANSID", *resolve(t[8:-1], scan["literals"]), x["para"]))
    for c in scan["calls"]:
        deps.append((c["line"], "CALL", *resolve(c["target"], scan["literals"]), c["para"]))

    return {"name": name, "src": src, "scan": scan, "status": status, "gap": gap_reasons, "gap_at": gap_at[:8],
            "events": events, "data_rows": data_rows, "per_para": per_para, "edges": edges,
            "execs_by_para": execs_by_para, "dli": dli_calls, "conditions": conditions,
            "deps": deps, "copy_srcs": copy_srcs}


def resolve(expr: str, literals: dict):
    expr = expr.strip()
    if expr[:1] in "'\"":
        return [expr.strip("'\"").strip()], "literal"
    vals = sorted({v for _, v, _ in literals.get(expr, []) if v})
    if vals:
        how = sorted({h for h, v, _ in literals.get(expr, []) if v})
        return vals, f"variable `{expr}` ({', '.join(how)})"
    return [f"?{expr}"], f"variable `{expr}` (unresolved)"


def decision_text(src: Source, ln, e) -> str:
    if not ln:
        return e["text"]
    text = collapse(src.text(ln))
    if e["ev"] == "if":
        j = ln + 1
        while j <= len(src.lines) and src.live(j) and re.match(r"\s*(AND|OR|NOT)\b|\s*[(=<>]", src.code(j)):
            text += " " + collapse(src.text(j))
            j += 1
        return re.sub(r"\s+THEN$", "", text, flags=re.I)
    whens, depth, j = [], 0, ln + 1
    while j <= len(src.lines) and len(whens) < 25:
        if src.live(j):
            c = collapse(src.text(j))
            cu = c.upper()
            if re.match(r"EVALUATE\b", cu):
                depth += 1
            elif cu.startswith("END-EVALUATE"):
                if depth == 0:
                    break
                depth -= 1
            elif depth == 0 and cu.startswith("WHEN"):
                whens.append(c)
        j += 1
    return text + (" → " + " · ".join(whens) if whens else "")


# --------------------------------------------------------------------------- rendering

def render_program(d: dict, ctx) -> str:
    src, scan = d["src"], d["scan"]
    subsystem = subsystem_of(src.rel)
    n_dec = sum(len(v["decisions"]) for v in d["per_para"].values())
    n_cics = sum(1 for x in scan["execs"] if x["kind"] == "CICS")
    n_sql = sum(1 for x in scan["execs"] if x["kind"] == "SQL" and x["verb"] != "INCLUDE")
    parse = "✅ parsed" if d["status"] == "ok" else ("⏳ not run yet" if d["status"] == "not-run"
                                                    else "❌ parser gap — " + md_escape("; ".join(d["gap"][:3]) or "see raw log"))
    o = [f"# {d['name']} — source of truth", "",
         "> Generated by `.github/skills/cobol-rekt/run-cobol-rekt.sh`. Do not edit by hand — re-run the script.",
         "> **[rekt]** = from the cobol-rekt parse tree · **[scan]** = deterministic scan of the original source.", "",
         "| | |", "|---|---|",
         f"| Source | {src.ref(None)} ({len(src.lines)} lines) |",
         f"| Subsystem | `{subsystem}` |",
         f"| cobol-rekt | {parse} |",
         f"| Sections / paragraphs [scan] | {len(scan['headers'])} |",
         f"| Decisions (IF / EVALUATE) [rekt] | {n_dec if d['events'] is not None else 'n/a (parser gap)'} |",
         f"| EXEC CICS / EXEC SQL / DL/I calls [scan] | {n_cics} / {n_sql} / {len(d['dli'])} |", ""]

    # 1. structure
    o += ["## 1. Procedure structure", ""]
    if d["events"] is not None and d["edges"]:
        o += ["Control transfers inside the program [rekt]. Solid = `PERFORM`, dashed = `GO TO`.", "",
              "```mermaid", "flowchart TD"]
        names = sorted({a for a, _, _ in d["edges"]} | {b for _, b, _ in d["edges"]})
        for nm in names:
            o.append(f'  {node_id(nm)}["{nm}"]')
        for a, b, kind in sorted(set(d["edges"])):
            o.append(f"  {node_id(a)} {'-->' if kind == 'PERFORM' else '-.->|GO TO|'} {node_id(b)}")
        o += ["```", ""]
    elif d["events"] is None:
        o += ["_cobol-rekt could not parse this program, so the PERFORM / GO TO graph and decision points are missing. "
              "Treat every claim about its control flow as unverified and read the source directly._", ""]
        if d["gap"]:
            o += ["Parser messages (from `raw/logs/`):", ""]
            hits = dict(d["gap_at"])
            for why in d["gap"]:
                lines = hits.get(why)
                where = ""
                if lines and len(lines) <= 6:
                    where = " — token found at " + ", ".join(src.ref(ln) for ln in lines)
                elif lines:
                    where = f" — token appears on {len(lines)} lines"
                o.append(f"- {md_escape(why)}{where}")
            o.append("")
    o += ["| Section / paragraph | Lines | PERFORMs [rekt] | GO TO [rekt] | Decisions [rekt] | EXEC CICS | EXEC SQL |",
          "|---|---|---|---|---|---|---|"]
    for ln, nm, kind in scan["headers"]:
        start, end, _ = scan["ranges"].get(nm, (ln, ln, kind))
        pp = d["per_para"].get(nm, {"perform": [], "goto": [], "decisions": []})
        perf = ", ".join(sorted({t + (" (loop)" if lp else "") for _, t, lp in pp["perform"]})) or "—"
        gt = ", ".join(sorted({t for _, t in pp["goto"]})) or "—"
        ex = d["execs_by_para"].get(nm, {})
        label = f"**{nm}** (section)" if kind == "section" else nm
        dec = len(pp["decisions"]) if d["events"] is not None else "n/a"
        o.append(f"| {label} | {start}–{end} | {perf} | {gt} | {dec} | {ex.get('CICS', 0) or '—'} | {ex.get('SQL', 0) or '—'} |")
    o.append("")

    # 2. decisions
    if d["events"] is not None:
        o += ["## 2. Decision points", "",
              "Every `IF` / `EVALUATE` found by cobol-rekt, in source order. Condition text is quoted from the original "
              "source line. These are where business rules live.", ""]
        any_dec = False
        for ln, nm, _ in scan["headers"]:
            decs = d["per_para"].get(nm, {}).get("decisions", [])
            if not decs:
                continue
            any_dec = True
            o.append(f"**{nm}**")
            o.append("")
            for dl, ev, depth, text in decs:
                o.append(f"{'  ' * depth}- {src.ref(dl) if dl else '`?`'} `{md_escape(text)[:220]}`")
            o.append("")
        if not any_dec:
            o += ["_No decisions found._", ""]
    else:
        o += ["## 2. Decision points", "", "_Not available — parser gap (see above)._", ""]

    # 3. touchpoints
    o += ["## 3. External touchpoints [scan]", "",
          "Every `EXEC CICS`, `EXEC SQL` and IMS DL/I call, as written in the original source.", ""]
    rows = [(x["line"], x["para"], "EXEC " + x["kind"], x["verb"], "`" + md_escape(shorten(x["shown"], 110)) + "`")
            for x in scan["execs"] if not (x["kind"] == "SQL" and x["verb"] == "INCLUDE")]
    rows += [(c["line"], c["para"], "IMS DL/I", "CALL " + c["target"], "`" + md_escape(shorten(c["stmt"], 110)) + "`")
             for c in d["dli"]]
    if rows:
        o += ["| Line | Paragraph | Kind | Verb | Statement |", "|---|---|---|---|---|"]
        for ln, para, kind, verb, tg in sorted(rows):
            o.append(f"| {src.ref(ln)} | {para} | {kind} | `{verb}` | {tg} |")
    else:
        o.append("_None found._")
    o.append("")

    # 4. dependencies
    o += ["## 4. Dependencies [scan]", "", "**Program / transaction transfers**", ""]
    if d["deps"]:
        grouped = defaultdict(list)
        for ln, kind, targets, how, para in sorted(d["deps"]):
            grouped[(kind, tuple(targets), how)].append((ln, para))
        o += [f"Lines refer to {src.ref(None)}.", "",
              "| Kind | Target(s) | Resolved via | Lines · paragraph |", "|---|---|---|---|"]
        for (kind, targets, how), where in sorted(grouped.items(), key=lambda kv: kv[1][0][0]):
            locs = ", ".join(f"`:{ln}` {para}" for ln, para in where[:12])
            more = f" … +{len(where) - 12} more" if len(where) > 12 else ""
            o.append(f"| {kind} | {', '.join(f'`{t}`' for t in targets)} | {how} | {locs}{more} |")
    else:
        o.append("_None found._")
    o += ["", "**Copybooks**", ""]
    seen = set()
    for ln, cp in sorted(d["scan"]["copies"]):
        if cp in seen:
            continue
        seen.add(cp)
        where = f"`{ctx['copybooks'][cp]}`" if cp in ctx["copybooks"] else ctx["stub_reason"](cp)
        o.append(f"- `{cp}` ({src.ref(ln)}) → {where}")
    o.append("")

    # 5. 88 levels
    o += ["## 5. Condition names (88-levels) [scan]", "",
          "Named conditions encode business codes. Includes copybooks reached from this program.", ""]
    if d["conditions"]:
        o += ["| Condition | Value(s) | Parent item | Defined at |", "|---|---|---|---|"]
        for s, ln, cname, vals, parent in d["conditions"]:
            o.append(f"| `{cname}` | `{md_escape(vals)[:80]}` | {parent or '—'} | {s.ref(ln)} |")
    else:
        o.append("_None found._")
    o.append("")

    # 6. data items
    o += ["## 6. Data items [rekt]", ""]
    if d["data_rows"] is None:
        o += ["_Not available — parser gap._", ""]
    else:
        sources = [src] + d["copy_srcs"]
        rows = d["data_rows"]

        def clause_of(row):
            return collapse(re.sub(r"^\s*\d+\s+[A-Z0-9-]+\s*", "", row.get("rawText", ""), flags=re.I)).rstrip(".")

        def where(row):
            s_, ln_ = find_data_def(sources, row.get("levelNumber"), row.get("name", "").upper())
            return s_.ref(ln_) if s_ else "—"

        o += ["**Records (01 / 77 levels)**", "",
              "| Name | Clause | Fields | Section | Defined at |", "|---|---|---|---|---|"]
        reported = {r.get("name", "").upper() for r in rows}
        for row in rows:
            if row.get("levelNumber") in (1, 77):
                red = f" REDEFINES {row['redefines']}" if row.get("isRedefinition") else ""
                o.append(f"| `{row['name']}` | `{md_escape(clause_of(row))[:60] or '(group)'}`{red} | {row['_fields'] or '—'} "
                         f"| {row.get('sourceSection', '')} | {where(row)} |")
        link = [r for r in rows if r.get("sourceSection") == "LINKAGE"]
        if link:
            o += ["", "**LINKAGE SECTION — the program's interface contract (COMMAREA / parameters)**", "",
                  "| Level | Name | Clause | Type | Defined at |", "|---|---|---|---|---|"]
            for row in link[:MAX_DATA_ROWS]:
                lvl = row.get("levelNumber")
                indent = "&nbsp;&nbsp;" * row["_depth"]
                red = f" REDEFINES {row['redefines']}" if row.get("isRedefinition") else ""
                o.append(f"| {lvl:02d} | {indent}{row['name']} | `{md_escape(clause_of(row))[:60] or '(group)'}`{red} "
                         f"| {row.get('dataType', '')} | {where(row)} |")
            if len(link) > MAX_DATA_ROWS:
                o.append(f"| … | _{len(link) - MAX_DATA_ROWS} more fields — see the source_ | | | |")
        o += ["", "_Working-storage fields below the 01 level are not listed — open the definition to drill in._"]
        missing = [(ln, nm, sec) for ln, nm, sec in source_01_items(src) if nm.upper() not in reported]
        o.append("")
        if missing:
            o += ["**Not reported by cobol-rekt** — present in the source, so read them there [scan]:", ""]
            for ln, nm, sec in missing:
                o.append(f"- `{nm}` ({sec}) {src.ref(ln)}")
            o.append("")
    return "\n".join(o).rstrip() + "\n"


def subsystem_of(rel: str) -> str:
    parts = Path(rel).parts
    if "base" in parts and parts.index("base") + 1 < len(parts):
        return parts[parts.index("base") + 1]
    return parts[-3] if len(parts) >= 3 else parts[0]


def render_index(digests, ctx) -> str:
    root = ctx["root"]
    ok = sum(1 for d in digests if d["status"] == "ok")
    gap = sum(1 for d in digests if d["status"] not in ("ok", "not-run"))
    o = ["# Source of truth — deterministic analysis", "",
         "> Generated by `.github/skills/cobol-rekt/run-cobol-rekt.sh` using "
         f"[cobol-rekt](https://github.com/avishek-sen-gupta/cobol-rekt) `{ctx['version']}` "
         f"(sha256 `{ctx['sha'][:12]}…`). No LLM was involved. Do not edit by hand — re-run the script.", "",
         "## How to use this", "",
         "This folder is the **ground truth** that phase-1 rediscovery is checked against. Order of trust:", "",
         "1. The original source file and line (`path:LINE`) — always wins.",
         "2. **[rekt]** facts — from the cobol-rekt parse tree (control flow, decisions, data items).",
         "3. **[scan]** facts — deterministic regex scan of the original source (EXEC CICS/SQL, CALL, COPY, 88-levels).",
         "4. Anything an LLM wrote in `01-rediscovery/` — must be consistent with 1–3. If it is not, it goes into "
         "`open-questions.md`.", "",
         "Programs marked ❌ could not be parsed: the tool **cannot vouch** for their control flow or data items.", "",
         f"## Coverage — {ok} parsed, {gap} parser gap(s), {len(digests)} programs", "",
         "| Program | Subsystem | cobol-rekt | Paragraphs | Decisions | EXEC CICS | EXEC SQL | DL/I | Digest |",
         "|---|---|---|---|---|---|---|---|---|"]
    for d in sorted(digests, key=lambda x: (subsystem_of(x["src"].rel), x["name"])):
        sc = d["scan"]
        st = {"ok": "✅", "not-run": "⏳ not run"}.get(d["status"], "❌ " + md_escape(shorten((d["gap"] or ["parser gap"])[0], 70)))
        n_dec = sum(len(v["decisions"]) for v in d["per_para"].values()) if d["events"] is not None else "—"
        o.append(f"| `{d['name']}` | {subsystem_of(d['src'].rel)} | {st} | {len(sc['headers'])} | {n_dec} | "
                 f"{sum(1 for x in sc['execs'] if x['kind'] == 'CICS')} | "
                 f"{sum(1 for x in sc['execs'] if x['kind'] == 'SQL' and x['verb'] != 'INCLUDE')} | {len(d['dli'])} | "
                 f"[{d['name']}.md](programs/{d['name']}.md) |")
    o += ["", "See also: [call-graph.md](call-graph.md) · [data-access.md](data-access.md)", ""]

    o += ["## Stubbed copybooks", "",
          "Referenced but not in the repository. An empty stub was given to the parser so it could continue; "
          "fields from these members are therefore unknown to cobol-rekt.", ""]
    if ctx["stubs"]:
        o += ["| Copybook | Why it is missing |", "|---|---|"]
        for s in ctx["stubs"]:
            o.append(f"| `{s}` | {ctx['stub_reason'](s)} |")
    else:
        o.append("_None._")
    o.append("")

    if ctx["duplicates"]:
        o += ["## Duplicate member names", "",
              "COBOL resolves `COPY x` by member name only, so when the same name exists twice one file has to win. "
              "Members under a `copy/` folder are preferred over generated ones.", "",
              "| Member | Used | Ignored |", "|---|---|---|"]
        for nm, used, ignored in ctx["duplicates"]:
            o.append(f"| `{nm}` | `{used}` | `{ignored}` |")
        o.append("")
    o += ["## Not analysed by cobol-rekt", "",
          "cobol-rekt only understands COBOL. These artifacts are **outside** the source of truth — rediscovery claims "
          "about them must cite the files directly.", "", "| Artifact | Count | Example |", "|---|---|---|"]
    for label, globs in (("BMS maps", ["*.bms"]), ("JCL", ["*.jcl"]), ("PL/I", ["*.pli", "*.pl1"]),
                         ("Assembler (IMS DBD/PSB, ...)", ["*.asm"]), ("Java", ["*.java"]),
                         ("z/OS Connect API definitions", ["*.yaml", "*.json"])):
        files = []
        for g in globs:
            files += [p for p in root.rglob(g) if not any(x in p.parts for x in (".git", ".tools", "docs", "node_modules", ".vscode"))
                      and (label != "z/OS Connect API definitions" or "api" in p.parts)]
        if files:
            o.append(f"| {label} | {len(files)} | `{files[0].relative_to(root)}` |")
    o += ["", "## Normalisation applied before parsing", "",
          "cobol-rekt's parser rejects some IBM Enterprise COBOL constructs. `normalise.awk` works on a scratch copy "
          "(the repository is never modified) and keeps every line in place, so all line numbers above match the "
          "original files:", "",
          "- `CBL` / `PROCESS` compiler-option lines → commented out",
          "- `AUTHOR.` / `DATE-WRITTEN.` / … paragraphs → commented out (free text breaks the tokenizer)",
          "- `EXEC SQL INCLUDE x` → `COPY x.`",
          "- `RESP` / `RESP2` lines inside `EXEC CICS ENQ` / `DEQ` → commented out (rejected by the parser there)",
          "- figurative constants written in lower case (`spaces`, `zero`, …) → upper case",
          "- other `EXEC SQL … END-EXEC` → `CONTINUE` (re-attached from the original source in section 3 of each digest)",
          "- missing copybooks → empty stubs (listed above)", ""]
    return "\n".join(o)


def render_call_graph(digests, ctx) -> str:
    progs = {d["name"] for d in digests}
    edges = []
    for d in digests:
        for ln, kind, targets, how, para in d["deps"]:
            for t in targets:
                if kind == "CALL" and t in ("CBLTDLI", "AIBTDLI", "CEETDLI"):
                    continue
                edges.append((d["name"], t, kind, d["src"].ref(ln), how))
    o = ["# Program call graph [scan]", "",
         "> Generated — do not edit. `LINK` / `XCTL` / `CALL` between programs and `RETURN` / `START` / `RUN TRANSID` "
         "hand-offs to CICS transactions (which program a transaction runs is CICS resource definition — not in this repo). Targets held in variables are resolved from `VALUE` clauses and "
         "`MOVE 'literal'` statements in the same program; anything starting with `?` could not be resolved.", "",
         "```mermaid", "flowchart LR"]
    nodes = set()
    for a, b, kind, _, _ in edges:
        for n in (a, b):
            if n not in nodes:
                nodes.add(n)
                if kind.endswith("TRANSID") and n == b:
                    o.append(f'  {node_id("tx_" + n)}(["{n}"])')
                else:
                    o.append(f'  {node_id(n)}["{n}"]')
    for a, b, kind, _, _ in sorted(set((a, b, k, "", "") for a, b, k, _, _ in edges)):
        tgt = node_id("tx_" + b) if kind.endswith("TRANSID") else node_id(b)
        arrow = {"LINK": "-->|LINK|", "XCTL": "==>|XCTL|", "CALL": "-.->|CALL|"}.get(kind, f"-.->|{kind}|")
        o.append(f"  {node_id(a)} {arrow} {tgt}")
    o += ["```", "", "Rounded nodes are CICS transaction IDs, not programs.", "",
          "| From | Kind | To | In repo? | Where | Resolved via |", "|---|---|---|---|---|---|"]
    for a, b, kind, ref, how in sorted(edges):
        inrepo = "—" if kind.endswith("TRANSID") else ("✅" if b in progs else
                                                        "external (IBM Language Environment)" if re.match(r"CEE|__", b) else
                                                        "❓ unresolved" if b.startswith("?") else "❌ not in repo")
        o.append(f"| `{a}` | {kind} | `{b}` | {inrepo} | {ref} | {how} |")
    return "\n".join(o) + "\n"


def render_data_access(digests) -> str:
    tables, files, queues, dli = defaultdict(set), defaultdict(set), defaultdict(set), defaultdict(set)
    for d in digests:
        for x in d["scan"]["execs"]:
            if x["kind"] == "SQL" and x["verb"] in ("SELECT", "INSERT", "UPDATE", "DELETE", "DECLARE"):
                dm = re.match(r"DECLARE\s+([A-Z0-9_#@$-]+)\s+(?:\w+\s+)*CURSOR\b", x["text"])
                for t in x["targets"]:
                    if dm and t == dm.group(1):
                        continue
                    verb = "cursor " + dm.group(1) if dm else x["verb"]
                    schema, _, table = t.rpartition(".")
                    tables[table].add((d["name"], verb + (f", as {t}" if schema else "")))
            if x["kind"] == "CICS":
                for t in x["targets"]:
                    if t.startswith(("FILE(", "DATASET(")):
                        files[t.split("(", 1)[1].rstrip(")").strip("'\"")].add((d["name"], x["verb"]))
                    if t.startswith(("QUEUE(", "QNAME(", "TS(", "TD(")):
                        queues[t.split("(", 1)[1].rstrip(")").strip("'\"")].add((d["name"], x["verb"]))
        for c in d["dli"]:
            m = re.search(r"USING\s+([A-Z0-9-]+)", c["stmt"])
            dli[d["name"]].add(m.group(1) if m else "?")

    def table(title, mapping, col):
        out = [f"## {title}", ""]
        if not mapping:
            return out + ["_None found._", ""]
        out += [f"| {col} | Programs (verb) |", "|---|---|"]
        for k in sorted(mapping):
            progs = ", ".join(f"`{p}` ({v})" for p, v in sorted(mapping[k]))
            out.append(f"| `{k}` | {progs} |")
        return out + [""]

    o = ["# Data access map [scan]", "",
         "> Generated — do not edit. Which program touches which data store, taken from `EXEC SQL`, `EXEC CICS` "
         "and IMS DL/I `CALL`s in the original sources. Use it to check `data-model.md` and `integrations.md`.", ""]
    o += table("DB2 tables (EXEC SQL)", tables, "Table")
    o += table("CICS files / VSAM datasets (EXEC CICS)", files, "File")
    o += table("Queues (EXEC CICS)", queues, "Queue")
    o += ["## IMS DL/I calls", ""]
    if dli:
        o += ["| Program | Function argument(s) |", "|---|---|"]
        for p in sorted(dli):
            o.append(f"| `{p}` | {', '.join(f'`{f}`' for f in sorted(dli[p]))} |")
    else:
        o.append("_None found._")
    o.append("")
    return "\n".join(o)


# --------------------------------------------------------------------------- main

def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--root", required=True)
    ap.add_argument("--out", required=True)
    ap.add_argument("--raw", required=True)
    ap.add_argument("--rekt-version", default="?")
    ap.add_argument("--rekt-sha256", default="?")
    a = ap.parse_args()
    root, out, raw = Path(a.root), Path(a.out), Path(a.raw)

    def tsv(p):
        return dict(line.split("\t", 1) for line in p.read_text().splitlines() if "\t" in line) if p.exists() else {}

    programs = tsv(raw / "programs.tsv")
    copybooks = tsv(raw / "copybooks.tsv")
    stubs = [s for s in (raw / "stubs.tsv").read_text().split() if s] if (raw / "stubs.tsv").exists() else []
    bms = {p.stem.upper() for p in root.rglob("*.bms") if ".git" not in p.parts}

    def stub_reason(name):
        if name in bms:
            return f"BMS symbolic map — generated at build time from `{next(p for p in root.rglob('*.bms') if p.stem.upper() == name).relative_to(root)}`"
        for rx, why in IBM_STUB_HINTS:
            if rx.search(name):
                return why
        return "not found in repository (stubbed)" if name in stubs else "not found in repository"

    dup_file = raw / "duplicates.tsv"
    duplicates = [tuple(l.split("\t")) for l in dup_file.read_text().splitlines() if l.count("\t") == 2] if dup_file.exists() else []
    ctx = {"root": root, "raw": raw, "copybooks": copybooks, "stubs": stubs, "stub_reason": stub_reason,
           "duplicates": duplicates,
           "version": a.rekt_version, "sha": a.rekt_sha256}
    (out / "programs").mkdir(parents=True, exist_ok=True)
    digests = []
    for name, rel in sorted(programs.items()):
        d = program_digest(name, rel, ctx)
        digests.append(d)
        (out / "programs" / f"{name}.md").write_text(render_program(d, ctx), encoding="utf-8")
    (out / "index.md").write_text(render_index(digests, ctx), encoding="utf-8")
    (out / "call-graph.md").write_text(render_call_graph(digests, ctx), encoding="utf-8")
    (out / "data-access.md").write_text(render_data_access(digests), encoding="utf-8")


if __name__ == "__main__":
    main()
