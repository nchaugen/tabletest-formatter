#!/usr/bin/env bash
# Smoke test for both Spotless integration samples.
# Mirrors the steps documented in README.md.
#
# Usage: ./smoke-test.sh

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

ok()        { printf '  ✅  %s\n' "$1"; }
fail_step() { printf '  ❌  %s\n' "$1" >&2; exit 1; }

expect_success() {
    local description="$1"; shift
    if "$@"; then
        ok "$description"
    else
        fail_step "$description"
    fi
}

expect_failure() {
    local description="$1"; shift
    if "$@" > /dev/null 2>&1; then
        fail_step "Expected failure but got success: $description"
    else
        ok "$description"
    fi
}

# ──────────────────────────────────────────────────────────────────────────────
printf '\nGradle sample (plugin 8.4.0)\n'
printf '%.0s─' {1..40}; echo
cd "$SCRIPT_DIR/gradle-sample"

if [[ ! -f gradlew ]]; then
    echo "  Generating Gradle wrapper..."
    gradle wrapper --gradle-version 8.12 -q
fi

expect_success "spotlessCheck passes on formatted files (idempotency)" \
    ./gradlew spotlessCheck --no-daemon -q

./reset-for-demo.sh > /dev/null

expect_failure "spotlessCheck detects violations after reset" \
    ./gradlew spotlessCheck --no-daemon -q

expect_success "spotlessApply formats successfully" \
    ./gradlew spotlessApply --no-daemon -q

expect_success "spotlessCheck passes after apply (idempotent)" \
    ./gradlew spotlessCheck --no-daemon -q

# ──────────────────────────────────────────────────────────────────────────────
printf '\nMaven sample (plugin 3.4.0)\n'
printf '%.0s─' {1..40}; echo
cd "$SCRIPT_DIR/maven-sample"

expect_success "spotless:check passes on formatted files (idempotency)" \
    mvn spotless:check -q

./reset-for-demo.sh > /dev/null

expect_failure "spotless:check detects violations after reset" \
    mvn spotless:check -q

expect_success "spotless:apply formats successfully" \
    mvn spotless:apply -q

expect_success "spotless:check passes after apply (idempotent)" \
    mvn spotless:check -q

# ──────────────────────────────────────────────────────────────────────────────
printf '\nAll smoke tests passed ✅\n\n'
