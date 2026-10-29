#!/usr/bin/env bash
# run-cobol-rekt.sh - build a deterministic "source of truth" for the COBOL
# programs in this repository using cobol-rekt
# (https://github.com/avishek-sen-gupta/cobol-rekt).
#
# Usage:
#   .github/skills/cobol-rekt/run-cobol-rekt.sh              # every program
#   .github/skills/cobol-rekt/run-cobol-rekt.sh XFRFUN INQCUST
#
# Output (relative to the repository root):
#   docs/modernization/00-source-of-truth/index.md          committed
#   docs/modernization/00-source-of-truth/call-graph.md     committed
#   docs/modernization/00-source-of-truth/data-access.md    committed
#   docs/modernization/00-source-of-truth/programs/*.md     committed
#   docs/modernization/00-source-of-truth/raw/              gitignored (JSON, logs)
#
# Requirements: bash, awk, curl, Java 21+, Python 3.9+.

set -euo pipefail

REKT_VERSION="${REKT_VERSION:-v0.1.1-RC03}"
REKT_SHA256="${REKT_SHA256:-0144464c0129e79e7815428fb66e8e0d9bd1f5992ebf75cc39fc0edb516cdf5d}"
REKT_URL="https://github.com/avishek-sen-gupta/cobol-rekt/releases/download/${REKT_VERSION}/smojol-cli.jar"

SKILL_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT="$(git -C "$SKILL_DIR" rev-parse --show-toplevel 2>/dev/null || (cd "$SKILL_DIR/../../.." && pwd))"
TOOLS="$ROOT/.tools/cobol-rekt/$REKT_VERSION"
JAR="$TOOLS/smojol-cli.jar"
OUT="$ROOT/docs/modernization/00-source-of-truth"
RAW="$OUT/raw"
WORK="$RAW/work"

die() { echo "ERROR: $*" >&2; exit 2; }
log() { echo "[cobol-rekt] $*"; }

# --- prerequisites -----------------------------------------------------------
command -v java >/dev/null || die "Java 21+ is required (https://adoptium.net)."
JAVA_MAJOR="$(java -version 2>&1 | awk -F'"' '/version/ {split($2, v, "."); print (v[1] == "1" ? v[2] : v[1]); exit}')"
[ "${JAVA_MAJOR:-0}" -ge 21 ] 2>/dev/null || die "Java 21+ is required, found '${JAVA_MAJOR:-unknown}'."
PY="$(command -v python3 || command -v python || true)"
[ -n "$PY" ] || die "Python 3 is required for the digest step."
command -v curl >/dev/null || die "curl is required to download cobol-rekt."

sha256() {
  if command -v sha256sum >/dev/null; then sha256sum "$1" | awk '{print $1}'
  else shasum -a 256 "$1" | awk '{print $1}'; fi
}

# --- download pinned cobol-rekt release ---------------------------------------
if [ ! -f "$JAR" ]; then
  log "Downloading cobol-rekt $REKT_VERSION (~75 MB, one-time) ..."
  mkdir -p "$TOOLS"
  curl -fsSL "$REKT_URL" -o "$JAR.part" || die "Download failed: $REKT_URL"
  mv "$JAR.part" "$JAR"
fi
if [ -n "$REKT_SHA256" ] && [ "$(sha256 "$JAR")" != "$REKT_SHA256" ]; then
  rm -f "$JAR"
  die "Checksum mismatch for $JAR (expected $REKT_SHA256). Deleted - re-run to download again."
fi

# --- discover and normalise sources -------------------------------------------
log "Discovering COBOL programs and copybooks under $ROOT ..."
rm -rf "$WORK"
mkdir -p "$WORK/src" "$WORK/copy" "$RAW/logs" "$RAW/status" "$RAW/rekt" "$OUT/programs"

# Files under a folder called copy/copybook(s)/cpy/copylib win over same-named
# members elsewhere (e.g. generated z/OS Connect interface copybooks).
find_sources() {
  find "$ROOT" \( -path "$ROOT/.git" -o -path "$ROOT/.tools" -o -path "$ROOT/docs" -o -name node_modules \) -prune \
    -o -type f \( "$@" \) -print \
    | awk '{ p = tolower($0); print (p ~ /\/(copy|copybooks?|cpy|copylib)\// ? 0 : 1) "\t" $0 }' \
    | LC_ALL=C sort | cut -f2-
}

: > "$RAW/programs.tsv"
: > "$RAW/copybooks.tsv"
: > "$RAW/duplicates.tsv"   # name <TAB> used <TAB> ignored
while IFS= read -r f; do
  base="$(basename "$f")"
  if [ -e "$WORK/src/$base" ]; then
    printf '%s\t%s\t%s\n' "${base%.*}" "$(awk -F'\t' -v n="${base%.*}" '$1 == n { print $2; exit }' "$RAW/programs.tsv")" "${f#"$ROOT"/}" >> "$RAW/duplicates.tsv"
    continue
  fi
  awk -f "$SKILL_DIR/normalise.awk" "$f" > "$WORK/src/$base"
  printf '%s\t%s\n' "${base%.*}" "${f#"$ROOT"/}" >> "$RAW/programs.tsv"
done < <(find_sources -iname '*.cbl' -o -iname '*.cob')

while IFS= read -r f; do
  base="$(basename "$f")"
  name="$(echo "${base%.*}" | tr '[:lower:]' '[:upper:]')"
  if [ -e "$WORK/copy/$name.cpy" ]; then
    printf '%s\t%s\t%s\n' "$name" "$(awk -F'\t' -v n="$name" '$1 == n { print $2; exit }' "$RAW/copybooks.tsv")" "${f#"$ROOT"/}" >> "$RAW/duplicates.tsv"
    continue
  fi
  awk -f "$SKILL_DIR/normalise.awk" "$f" > "$WORK/copy/$name.cpy"
  printf '%s\t%s\n' "$name" "${f#"$ROOT"/}" >> "$RAW/copybooks.tsv"
done < <(find_sources -iname '*.cpy')

[ -s "$RAW/programs.tsv" ] || die "No COBOL programs (*.cbl, *.cob) found."
[ -s "$RAW/duplicates.tsv" ] && log "$(wc -l < "$RAW/duplicates.tsv" | tr -d ' ') duplicate member name(s) skipped - listed in index.md"

# Copybooks that are referenced but not in the repo (IBM-supplied DFH*/CEE*
# members, BMS symbolic maps generated at build time) get an empty stub so the
# parser can continue. Every stub is listed in index.md.
: > "$RAW/stubs.tsv"
cat "$WORK"/src/* "$WORK"/copy/* 2>/dev/null \
  | awk 'substr($0,7,1) != "*" { u = toupper($0); while (match(u, /(^|[ \t])COPY[ \t]+["'\'']?[A-Z0-9#@$-]+/)) { s = substr(u, RSTART, RLENGTH); sub(/.*COPY[ \t]+["'\'']?/, "", s); print s; u = substr(u, RSTART + RLENGTH) } }' \
  | LC_ALL=C sort -u | while IFS= read -r cp; do
      if [ ! -e "$WORK/copy/$cp.cpy" ]; then
        : > "$WORK/copy/$cp.cpy"
        printf '%s\n' "$cp" >> "$RAW/stubs.tsv"
      fi
    done

# --- run cobol-rekt per program ----------------------------------------------
if [ "$#" -gt 0 ]; then
  SELECTED=()
  for p in "$@"; do
    up="$(echo "${p%.*}" | tr '[:lower:]' '[:upper:]')"
    match="$(awk -F'\t' -v p="$up" 'toupper($1) == p { print $1; exit }' "$RAW/programs.tsv")"
    [ -n "$match" ] || die "Program '$p' not found. Known programs: $(cut -f1 "$RAW/programs.tsv" | tr '\n' ' ')"
    SELECTED+=("$match")
  done
else
  SELECTED=()
  while IFS=$'\t' read -r name _; do SELECTED+=("$name"); done < "$RAW/programs.tsv"
fi

ok=0; failed=0
for name in "${SELECTED[@]}"; do
  file="$(cd "$WORK/src" && ls | awk -v n="$name" '{ b=$0; sub(/\.[^.]*$/, "", b); if (toupper(b) == toupper(n)) { print; exit } }')"
  rm -rf "$RAW/rekt/$file.report"
  if java -jar "$JAR" run "$file" \
        --commands="BUILD_BASE_ANALYSIS WRITE_FLOW_AST WRITE_DATA_STRUCTURES" \
        --srcDir "$WORK/src" --copyBooksDir "$WORK/copy" \
        --reportDir "$RAW/rekt" --generation=NODRAW > "$RAW/logs/$name.log" 2>&1 \
     && [ -f "$RAW/rekt/$file.report/flow_ast/flow-ast-$file.json" ]; then
    echo "ok" > "$RAW/status/$name"
    ok=$((ok + 1)); log "  $name  parsed"
  else
    { echo "parser-gap"
      grep -oE 'suggestion=[^,]+' "$RAW/logs/$name.log" | grep -v 'suggestion=null' | sed 's/^suggestion=//' | LC_ALL=C sort -u | head -5 || true
      grep -oE '^[A-Za-z.]+(Exception|Error): .*' "$RAW/logs/$name.log" | head -2 || true
    } > "$RAW/status/$name"
    failed=$((failed + 1)); log "  $name  PARSER GAP (see raw/logs/$name.log)"
  fi
done

# --- digest -------------------------------------------------------------------
log "Writing digest ..."
"$PY" "$SKILL_DIR/digest.py" --root "$ROOT" --out "$OUT" --raw "$RAW" \
  --rekt-version "$REKT_VERSION" --rekt-sha256 "$REKT_SHA256"

log "Done: $ok parsed, $failed parser gap(s). Start at docs/modernization/00-source-of-truth/index.md"
