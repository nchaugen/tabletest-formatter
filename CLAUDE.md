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

## TableTestExtractor Implementation

**Design Decision: State Machine Parser**

The project uses a custom state machine parser (`TableTestExtractor`) to extract `@TableTest` annotations from source code. This approach was chosen after evaluating several alternatives:

**Rejected alternatives:**
- **Regex** - Cannot distinguish real annotations from ones in string literals or comments
- **JavaParser** - Java-only solution, would need separate approach for Kotlin
- **Tree-sitter** - Platform-specific native libraries, complex multi-platform build requirements

**State machine advantages:**
- Correctly distinguishes real annotations from string literals and comments (solves "dogfooding" problem)
- Handles both Java and Kotlin with single implementation
- Supports fully qualified annotation names (`@io.github.nchaugen.tabletest.junit.TableTest`)
- No external parser dependencies or platform-specific native libraries
- Simple, maintainable implementation (~300 lines)

**How it works:**
- Tracks parsing state (CODE, LINE_COMMENT, BLOCK_COMMENT, STRING, TEXT_BLOCK, CHAR_LITERAL, LOOKING_FOR_TEXT_BLOCK)
- Transitions between states based on characters encountered
- Only extracts `@TableTest` annotations found in CODE state
- Properly handles escaped quotes and nested structures

See closed beads issues for detailed investigation notes: `tabletest-formatter-swo` (tree-sitter), `tabletest-formatter-3b9` (JavaParser).

## Key Files

- `pom.xml` - Root POM with plugin configurations and versions
- `README.md` - User-facing documentation
- `CONTRIBUTING.md` - Contributor setup and guidelines (including git hooks setup)
- `CLAUDE.md` - This file (AI/developer implementation notes)

## Implementation Workflow

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
