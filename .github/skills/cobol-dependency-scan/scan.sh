#!/usr/bin/env bash
# scan.sh - list what a COBOL program depends on: COPY / EXEC SQL INCLUDE,
# EXEC CICS LINK / XCTL / RETURN TRANSID / START / RUN, static CALLs, IMS DL/I
# calls. Plain awk, no Java. Output is markdown with file:LINE citations.
#
# Usage: .github/skills/cobol-dependency-scan/scan.sh <PROGRAM|path.cbl> [...]
#        .github/skills/cobol-dependency-scan/scan.sh --all

set -euo pipefail
ROOT="$(git rev-parse --show-toplevel 2>/dev/null || pwd)"
cd "$ROOT"

list_programs() {
  find . \( -path ./.git -o -path ./.tools -o -path ./docs -o -name node_modules \) -prune \
    -o -type f \( -iname '*.cbl' -o -iname '*.cob' \) -print | sed 's|^\./||' | LC_ALL=C sort
}

resolve() {
  case "$1" in
    *.cbl|*.CBL|*.cob|*.COB) [ -f "$1" ] && { echo "$1"; return; } ;;
  esac
  list_programs | awk -v p="$(echo "$1" | tr '[:lower:]' '[:upper:]')" '{
    b = $0; sub(/.*\//, "", b); sub(/\.[^.]*$/, "", b); if (toupper(b) == p) { print; exit } }'
}

scan() {
  awk -v file="$1" '
    function emit(kind, target, how) { printf "| %s | `%s` | %s | `%s:%d` |\n", kind, target, how, file, start }
    function opt(text, name,   m) {
      if (match(text, name "[ \t]*\\([^)]*\\)")) { m = substr(text, RSTART, RLENGTH); sub(/^[^(]*\([ \t]*/, "", m); sub(/[ \t]*\)$/, "", m); return m }
      return ""
    }
    function lit(v) { return (v ~ /^["\x27]/) ? "literal" : "variable" }
    function strip(v) { gsub(/["\x27]/, "", v); sub(/[ \t]+$/, "", v); return v }
    {
      line = $0; sub(/\r$/, "", line)
      ind = substr(line, 7, 1)
      if (ind == "*" || ind == "/") next
      code = toupper(substr(line, 8, 65))
      if (inexec) { buf = buf " " code; if (code ~ /END-EXEC/) done(); next }
      if (code ~ /EXEC[ \t]+(CICS|SQL)/) { inexec = 1; buf = code; start = NR; if (code ~ /END-EXEC/) done(); next }
      start = NR
      t = code
      while (match(t, /(^|[ \t])COPY[ \t]+["\x27]?[A-Z0-9#@$-]+/)) {
        m = substr(t, RSTART, RLENGTH); sub(/.*COPY[ \t]+["\x27]?/, "", m); emit("COPY", m, "copybook"); t = substr(t, RSTART + RLENGTH)
      }
      if (match(code, /(^|[ \t])CALL[ \t]+("[^"]+"|\x27[^\x27]+\x27|[A-Z0-9-]+)/)) {
        m = substr(code, RSTART, RLENGTH); sub(/.*CALL[ \t]+/, "", m)
        if (strip(m) ~ /^(CBLTDLI|AIBTDLI|CEETDLI)$/) {
          rest = substr(code, RSTART + RLENGTH); fn = ""
          if (match(rest, /USING[ \t]+[A-Z0-9-]+/)) { fn = substr(rest, RSTART, RLENGTH); sub(/USING[ \t]+/, "", fn) }
          emit("IMS DL/I", strip(m), fn == "" ? "function on next line" : "function " fn)
        } else emit("CALL", strip(m), lit(m))
      }
    }
    function done(   verb, v) {
      inexec = 0
      if (buf ~ /EXEC[ \t]+SQL[ \t]+INCLUDE/) { v = buf; sub(/.*INCLUDE[ \t]+/, "", v); sub(/[ \t].*/, "", v); emit("SQL INCLUDE", v, "copybook"); return }
      if (buf !~ /EXEC[ \t]+CICS/) return
      v = buf; sub(/.*EXEC[ \t]+CICS[ \t]+/, "", v); split(v, w, /[ \t(]+/); verb = w[1]
      if (verb == "LINK" || verb == "XCTL") { v = opt(buf, "PROGRAM"); if (v != "") emit("CICS " verb, strip(v), lit(v)) }
      if (verb == "RETURN" || verb == "START" || verb == "RUN") { v = opt(buf, "TRANSID"); if (v != "") emit("CICS " verb " TRANSID", strip(v), lit(v)) }
    }
  ' "$1"
}

if [ "${1:-}" = "--all" ]; then set -- $(list_programs); fi
[ "$#" -gt 0 ] || { sed -n '2,8p' "$0"; exit 1; }

for arg in "$@"; do
  f="$(resolve "$arg")"
  [ -n "$f" ] || { echo "Program '$arg' not found." >&2; exit 2; }
  echo "### $(basename "${f%.*}") — \`$f\`"
  echo
  echo "| Kind | Target | Resolved | Where |"
  echo "|---|---|---|---|"
  scan "$f"
  echo
done
echo "_\"variable\" means the target is held in a data item — look up its VALUE / MOVE to find the real name._"
