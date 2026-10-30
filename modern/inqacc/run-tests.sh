#!/usr/bin/env bash
set -euo pipefail

cd "$(dirname "$0")"
classes="$(mktemp -d "${TMPDIR:-/tmp}/inqacc-tests.XXXXXX")"
trap 'rm -rf "$classes"' EXIT

sources=()
while IFS= read -r source; do
    sources+=("$source")
done < <(find src -name '*.java' -type f | sort)

javac --release 21 -Xlint:all -Werror -Xmaxerrs 3 -d "$classes" "${sources[@]}"
java -cp "$classes" com.bankofz.inqacc.InqaccAcceptanceTest
