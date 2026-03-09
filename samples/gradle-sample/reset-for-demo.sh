#!/usr/bin/env bash
# Strips alignment spaces from @TableTest tables to create formatting violations.
# Run this before spotlessCheck to test the full check → apply → check cycle.

set -euo pipefail

if [[ "$OSTYPE" == darwin* ]]; then SED=(sed -i ''); else SED=(sed -i); fi
"${SED[@]}" 's/ | /|/g' src/test/java/com/example/CalculatorTest.java
"${SED[@]}" 's/ | /|/g' src/test/kotlin/com/example/StringUtilsTest.kt

echo "Done. Tables are now unformatted — run spotlessCheck to detect violations."
