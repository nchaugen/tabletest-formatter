# TableTest Formatter - Development Notes

This document contains detailed implementation notes, architectural decisions, and AI-specific guidance for the TableTest Formatter project.

**For basic setup, build instructions, and getting started:** See [CONTRIBUTING.md](CONTRIBUTING.md)

**For user documentation and format specification:** See [README.md](README.md)

---

## Project Context

A tool to format TableTest tables (CLI and Spotless integration) with consistent, readable formatting rules.

**Architecture**: Multi-module Maven project with hexagonal architecture (core, CLI, Spotless). See [CONTRIBUTING.md](CONTRIBUTING.md) for detailed project structure.

## Key Technologies & Decisions

**Parser**: Uses `tabletest-parser:0.5.8` from Maven Central (external dependency, not part of this project)

**CLI**: Picocli-based with maven-shade-plugin creating uber JAR

**Spotless Integration**: Implements `FormatterStep` for programmatic integration (Gradle via `addStep()`)

**Issue Tracking**: Beads (`bd` command) - see Implementation Workflow section below

## Key Files

- `pom.xml` - Root POM with plugin configurations and versions
- `git-hooks/` - Custom git hooks (pre-commit, pre-push, commit-msg)
- `scripts/install-git-hooks.sh` - Installs custom hooks (run before `bd init`)
- `NEW_PROJECT.md` - Project setup template (in .gitignore, reference only)
- `.java-version` - jenv configuration (Java 21)
- `README.md` - User-facing documentation
- `CONTRIBUTING.md` - Contributor setup and guidelines
- `CLAUDE.md` - This file (AI/developer implementation notes)

## Git Hooks Strategy

**For setup instructions:** See [CONTRIBUTING.md](CONTRIBUTING.md)

**Architectural decisions:**

**Why custom hooks + Beads chaining:**
- Need both: project-specific checks (Spotless, tests) AND beads sync
- Beads provides chaining mechanism via `.git/hooks/pre-commit` wrapper
- Our hooks: `.git/hooks/pre-commit.old`, `.git/hooks/pre-push`, `.git/hooks/commit-msg`

**Installation order matters:**
1. Install custom hooks first (`bash scripts/install-git-hooks.sh`)
2. Then run `bd init` (select "merge") - creates chaining wrapper

**Recent optimization (2025-12-30):**
- Pre-commit: auto-restages files modified by build (copyright headers)
- Pre-push: simplified to only check force-push (faster, trusts pre-commit)

**Rejected alternatives:**
- Inline hooks in install script → harder to maintain
- Complex detection of beads installation → unnecessary complexity
- `bd hooks install` → uses different pattern (thin shims) that doesn't call custom hooks

## Implementation Reference

### IntelliJ IDEA Plugin

A working reference implementation exists: `~/IdeaProjects/tabletest-intellij`

**Key insights:**
- Uses IntelliJ's PSI-based formatting (FormattingModelBuilder with SpacingBuilder)
- Column alignment via Alignment objects created for each pipe position
- Comprehensive test data in `src/test/testData/` showing before/after formatting
- Handles Unicode, emojis, nested collections, and all edge cases

**Critical difference:** Our standalone formatter cannot use IntelliJ's PSI infrastructure. We must:
- Use tabletest-parser library directly (`TableParser.parse()` → `Table` object)
- Implement formatting logic without PSI (text manipulation based on parsed structure)
- Handle raw cell text extraction and reconstruction

### Implementation Workflow

Use `bd ready` to find available work. Issues are organized with dependencies to enforce logical ordering.

## Dependency Management

**Plugin versions**: Keep up to date using `mvn versions:display-plugin-updates`

Current versions (as of 2025-12-27):
- maven-compiler-plugin: 3.14.1
- maven-surefire-plugin: 3.5.4
- maven-javadoc-plugin: 3.12.0
- picocli: 4.7.7
- versions-maven-plugin: 2.18.0

## Reference Documentation

- TableTest: https://github.com/nchaugen/tabletest
- TableTest AGENTS.md: https://raw.githubusercontent.com/nchaugen/tabletest/refs/heads/main/AGENTS.md (format specification)
- TableTest IntelliJ Plugin: `~/IdeaProjects/tabletest-intellij` (working reference implementation)
- tabletest-parser API: `TableParser.parse(String)` → `Table` with `headers()`, `row(int)`, etc.
- Spotless: https://github.com/diffplug/spotless
- Spotless FormatterStep docs: Check contributing guide in Spotless repo
- Beads: Issue tracking tool (bd command)

## Unicode Width Handling

**DisplayWidth implementation:** Uses wcwidth algorithm (IEEE Std 1002.1-2001) adapted from termd project (Apache 2.0). Unicode 5.0 (2007) implementation means modern emojis display as width 1.

**Separator format:** `"| "` (pipe-space), NOT `" | "` (space-pipe-space)

**IDE rendering caveat:** IntelliJ IDEA's source editor may show CJK/Unicode text as misaligned even when formatting is correct. IDE fonts don't always render Unicode with true monospace widths. Always verify output in actual terminals.

## Notes for Claude

- User prefers pair programming style (discuss then implement)
- Follow TDD: Red → Green → Refactor
- **When using TableTest: ALWAYS invoke the tabletest skill first** (`Skill(skill="tabletest")`)
- British English (Oxford spelling)
- No copyright headers (added by build)
- Keep commits focused and atomic

### Test Organization

**Regular JUnit @Test:**
- Use for single scenarios or complex setup
- Use when test logic differs significantly between cases

**TableTest (via skill):**
- **CRITICAL: Invoke `Skill(skill="tabletest")` BEFORE writing TableTest code**
- Use for multiple similar test cases differing only in data
- Quote values to preserve whitespace: `'  @TableTest...'`
- Scenario column (first) doesn't need a parameter
- Expected columns should use `?` suffix: `indent?`, `formatted?`

**Test Consolidation:**
- When you have 3+ similar @Test methods, consider consolidating to TableTest
- Example: 8 indentation tests → 2 TableTest methods (more maintainable)

### Debugging Workflow

When investigating bugs or test failures:
1. **Reproduce** - Add a minimal failing test case
2. **Isolate** - Add debug output (System.out.println) to trace execution
3. **Fix** - Make the minimal change to fix the issue
4. **Clean** - Remove debug output, ensure tests pass
5. **Refactor** - If needed, improve code structure (separate commit)

Example from indentation bug investigation:
- Added debug output to see actual vs expected strings
- Traced through formatTable → addBackCommentsAndBlankLines
- Found two bugs: lost trailing lines, extra blank lines
- Fixed both, removed debug output, all tests pass

### Programming Style

- **Functional style preferred**: Use streams and functional patterns over imperative loops
- **Imports**: Always use `import` and `static import` instead of fully qualified names
  - Good: `import static java.util.stream.Collectors.joining;`
  - Bad: `java.util.stream.Collectors.joining(...)`
- **Pure functions**: Extract behaviour into pure functions where possible
- **Method chaining**: Prefer fluent APIs and method chaining for readability
- **Ternary operators**: Prefer ternary operators over if-else for simple return statements
  - Good: `return isLast ? value : value + padding;`
  - Bad: `if (isLast) { return value; } return value + padding;`
- **Multiline strings**: Use text blocks (multiline strings) in tests for readability
  - Good: `var input = """\n    name|age\n    Alice|30\n    """;`
  - Bad: `var input = "name|age\\nAlice|30";`
- **Trailing newlines**: Include trailing newlines in test string literals to match actual file content

### Package Structure

- **Package naming**: Must match Maven groupId structure
  - Maven groupId: `io.github.nchaugen.tabletest`
  - Package name: `io.github.nchaugen.tabletest.formatter.core`
  - NOT: `io.nchaugen.tabletest.formatter.core` (missing `github`)

### Workflow

- **Issue closure timing**: Close beads issues BEFORE committing
  - Keeps commit and issue status in sync
  - Include `.beads/issues.jsonl` changes in the same commit as the implementation
  - Can amend commits (before push) to add issue closure

- **Standard commit workflow**:
  1. Stage code files: `git add <files>`
  2. Close issue: `bd close <issue-id>`
  3. Stage beads update: `git add .beads/issues.jsonl`
  4. Commit: `git commit -m "..."`
  5. Push: `git push`

  **Note:** Pre-commit hook automatically handles:
  - Spotless code formatting
  - Copyright header insertion (and re-staging modified files)
  - Build and tests (`mvn clean install`)

  No manual build step needed before committing!
