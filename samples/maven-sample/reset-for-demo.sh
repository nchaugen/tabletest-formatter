#!/usr/bin/env bash
# Strips alignment spaces from @TableTest tables to create formatting violations.
# Run this before spotless:check to test the full check → apply → check cycle.

set -euo pipefail

sed -i '' 's/ | /|/g' src/test/java/com/example/CalculatorTest.java
sed -i '' 's/ | /|/g' src/test/kotlin/com/example/StringUtilsTest.kt

echo "Done. Tables are now unformatted — run spotless:check to detect violations."
