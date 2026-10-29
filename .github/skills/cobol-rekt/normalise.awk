# normalise.awk - line-preserving clean-up so the cobol-rekt parser (Eclipse
# Che4z grammar, plain COBOL dialect) accepts IBM Enterprise COBOL sources.
#
# Every input line produces exactly one output line, so line numbers in the
# normalised copy are identical to the original. The script only:
#   1. comments out compiler-option lines (CBL / PROCESS),
#   2. comments out the obsolete IDENTIFICATION paragraphs (AUTHOR, DATE-WRITTEN,
#      ...), whose free text (e.g. "O'Grady") breaks the tokenizer,
#   3. rewrites EXEC SQL INCLUDE x END-EXEC as COPY x.,
#   4. replaces every other EXEC SQL ... END-EXEC block with CONTINUE (procedure
#      division) or a comment (data division), keeping a sentence-ending period,
#   5. comments out RESP / RESP2 lines inside EXEC CICS ENQ / DEQ (the parser
#      rejects them there; the digest reads EXEC CICS from the original),
#   6. upper-cases figurative constants written in lower case (e.g. "spaces"),
#      which cobol-rekt rejects although COBOL is case-insensitive.
#
# EXEC SQL statements therefore disappear from cobol-rekt's view. The digest
# re-attaches them from the ORIGINAL source, so nothing is lost in the output.

function cmt(l) { return (length(l) < 7) ? "      *" : substr(l, 1, 6) "*" substr(l, 8) }

function upfig(seg,   out, low, pre) {
  out = ""
  low = tolower(seg)
  while (match(low, /(^|[^a-z0-9-])(spaces?|zeros?|zeroes|low-values?|high-values?|quotes?|nulls?)([^a-z0-9-]|$)/)) {
    pre = (RSTART > 1 || substr(low, 1, 1) !~ /[a-z]/) ? 1 : 0
    out = out substr(seg, 1, RSTART - 1 + pre) toupper(substr(seg, RSTART + pre, RLENGTH - pre))
    seg = substr(seg, RSTART + RLENGTH); low = substr(low, RSTART + RLENGTH)
  }
  return out seg
}

function upfigs(l,   i, c, q, seg, out) {
  out = substr(l, 1, 7); seg = ""; q = ""
  for (i = 8; i <= length(l); i++) {
    c = substr(l, i, 1)
    if (q == "" && (c == "'" || c == "\"")) { out = out upfig(seg) c; seg = ""; q = c }
    else if (q != "") { out = out c; if (c == q) q = "" }
    else seg = seg c
  }
  return out upfig(seg)
}

function flush(   i, txt, name, indent, dot, isinclude) {
  txt = ""
  for (i = 1; i <= n; i++) txt = txt " " toupper(buf[i])
  dot = (txt ~ /END-EXEC[ \t]*\.[ \t]*$/) ? "." : ""
  indent = toupper(buf[1]); sub(/EXEC[ \t]+SQL.*/, "", indent)
  isinclude = match(txt, /EXEC[ \t]+SQL[ \t]+INCLUDE[ \t]+[A-Z0-9#@$-]+/)
  if (isinclude) {
    name = substr(txt, RSTART, RLENGTH); sub(/.*INCLUDE[ \t]+/, "", name)
    print indent "COPY " name "."
  } else if (inproc) {
    print indent "CONTINUE" (n == 1 ? dot : "")
  } else {
    print cmt(buf[1])
  }
  for (i = 2; i <= n; i++) {
    if (i == n && inproc && dot != "" && !isinclude) print substr(buf[i], 1, 6) "     ."
    else print cmt(buf[i])
  }
  n = 0
}

{
  line = $0
  sub(/\r$/, "", line)
  up = toupper(line)
  live = (substr(up, 7, 1) != "*" && substr(up, 7, 1) != "/")

  if (n > 0) { buf[++n] = line; if (live && up ~ /END-EXEC/) flush(); next }

  if (up ~ /^(CBL|PROCESS)[ \t]/) { print "      *" substr(line, 1, 65); next }
  if (live && up ~ /PROCEDURE[ \t]+DIVISION/) inproc = 1
  if (live && up ~ /^.......[ \t]*(AUTHOR|DATE-WRITTEN|DATE-COMPILED|INSTALLATION|SECURITY)\./) { print cmt(line); next }
  if (live && up ~ /EXEC[ \t]+SQL/) { buf[n = 1] = line; if (up ~ /END-EXEC/) flush(); next }
  if (live && up ~ /EXEC[ \t]+CICS[ \t]+(ENQ|DEQ)([ \t]|$)/) enqdeq = 1
  if (enqdeq && live && substr(up, 8) ~ /^[ \t]*RESP2?[ \t]*\([^)]*\)[ \t]*$/) { print cmt(line); next }
  if (enqdeq && live && up ~ /END-EXEC/) enqdeq = 0
  if (live && line ~ /[a-z]/) line = upfigs(line)
  print line
}

END { if (n > 0) flush() }
