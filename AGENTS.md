# tabletest-formatter

CLI that formats `@TableTest` tables to consistent, readable rules; also integrated
natively by diffplug/spotless. See CONTRIBUTING.md for setup, build/test, and hook install.

Tests use TableTest — invoke the `tabletest` plugin skill before writing or converting them.

## Architecture

Multi-module Maven, hexagonal:

- **tabletest-formatter-core** — pure formatting logic; parses tables with the external
  `org.tabletest:tabletest-parser` dependency.
- **tabletest-formatter-cli** — Picocli CLI, packaged as an uber JAR via maven-shade.

Configuration comes from `.editorconfig` files (`EditorConfigProvider` searches up the
directory tree), **not** CLI parameters — one source of truth across CLI, IDE plugins, and
Spotless. Custom `tabletest_*` properties extend it without touching tool integrations.
Package names must match the Maven groupId (`org.tabletest`).

## Gotchas

- `@TableTest` extraction from source is deliberately context-aware
  (`SmartTableTestExtractor`, a pure-Java state machine) so it ignores annotations inside
  string literals and comments — a naive/regex match corrupts the formatter's own test
  files. Keep it a state machine; extend rather than replace it.
- Column separator is `"| "` (pipe-space), **not** `" | "`.
- IntelliJ's editor can render CJK/Unicode as misaligned even when output is correct —
  verify alignment in a real terminal, not the IDE.

## Build

The pre-commit hook runs Spotless, inserts copyright headers (re-staging files), and builds
with tests — no manual format or build step before committing, and new files need no
copyright header. Keep plugin versions current with `mvn versions:display-plugin-updates`.
