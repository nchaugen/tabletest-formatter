# TableTest Formatter Project

## Project Overview

A tool to format TableTest tables, usable both as a CLI tool and as a Spotless integration for Maven/Gradle projects.

**Purpose**: Format TableTest tables (used in Java/Kotlin files or standalone `.table` files) according to consistent formatting rules.

**Formatting details:** See [README.md](README.md) for complete format specification and formatting rules

## Project Structure

Multi-module Maven project with hexagonal architecture:

```
tabletest-formatter/
├── tabletest-formatter-core/       # Core formatting logic
├── tabletest-formatter-cli/        # Command-line interface
└── tabletest-formatter-spotless/   # Spotless integration (FormatterStep)
```

### Module Responsibilities

- **core**: Pure formatting logic, depends on `tabletest-parser:0.5.8`
- **cli**: Picocli-based CLI, creates uber JAR with maven-shade-plugin
- **spotless**: Implements Spotless FormatterStep for build tool integration

## Technology Stack

- **Java**: 21
- **Build**: Maven 3.6+
- **Testing**: JUnit 6.0.1, AssertJ 3.27.6
- **CLI**: picocli 4.7.7
- **Parser**: tabletest-parser 0.5.8 (from Maven Central)
- **Formatting**: Spotless 3.1.0 with Palantir Java Format 2.83.0
- **Issue Tracking**: Beads (bd)

## Key Files

- `pom.xml` - Root POM with plugin configurations and versions
- `git-hooks/` - Custom git hooks (pre-commit, pre-push, commit-msg)
- `scripts/install-git-hooks.sh` - Installs custom hooks (run before `bd init`)
- `NEW_PROJECT.md` - Project setup template (in .gitignore, reference only)
- `.java-version` - jenv configuration (Java 21)

## Development Workflow

### Build Commands

```bash
# Compile
mvn clean compile

# Build and test
mvn clean install

# Full verification
mvn clean verify

# Format code
mvn spotless:apply

# Check for updates
mvn versions:display-plugin-updates
mvn versions:display-dependency-updates
```

### Git Hooks

**Installation order** (important!):
1. Custom hooks: `bash scripts/install-git-hooks.sh`
2. Beads: `bd init` (select "merge" when prompted)

**Hook chain**:
- **pre-commit**: Beads wrapper → custom hook (Spotless format + `mvn clean install`) → beads sync
- **pre-push**: Custom hook (force-push protection + `mvn clean verify`)
- **commit-msg**: Custom hook (validates conventional commits, checks for Claude attribution)

**Hook files**:
- `.git/hooks/pre-commit` - Beads chaining wrapper
- `.git/hooks/pre-commit.old` - Our custom formatting + build hook
- `.git/hooks/pre-push` - Force-push protection + full test suite
- `.git/hooks/commit-msg` - Commit message validation

### Committing

Follow conventional commits:
- `feat:` - New features
- `fix:` - Bug fixes
- `docs:` - Documentation
- `refactor:` - Code refactoring
- `test:` - Tests
- `chore:` - Build/tooling

**Important**: Omit Claude Code attribution footer (checked by commit-msg hook)

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

### Implementation Strategy

**Issue structure:** Issue bn7 (core formatter) depends on 11 implementation steps (h79-tod):
- **Phase 1** (sequential): h79 → 8mq → sy0 (parse/rebuild, calculate widths, align)
- **Phase 2** (after sy0): zno, yv2, bxi (collection formatting) → bm3 (nested)
- **Phase 3** (after sy0): 9jl, lc4, 9st, tod (edge cases)

Use `bd ready` to find next available step. Dependencies enforce logical ordering.

## Important Decisions & Conventions

### Plugin Versions

Keep plugins up to date. Current versions (as of 2025-12-27):
- maven-compiler-plugin: 3.14.1
- maven-surefire-plugin: 3.5.4
- maven-javadoc-plugin: 3.12.0
- picocli: 4.7.7
- versions-maven-plugin: 2.18.0 (use for checking updates)

### Git Hooks Strategy

**Decided approach**:
- Keep `git-hooks/` directory in repo as source of truth
- Simple installation script (just copies files)
- No complex detection logic
- Run custom hooks first, then install beads (creates chaining automatically)

**Rejected alternatives**:
- Inline hooks in install script (harder to maintain)
- Complex detection of beads installation (unnecessary complexity)
- Only documenting hooks in NEW_PROJECT.md (team needs version-controlled hooks)

### Beads Integration

After trying different approaches, the working solution is:
1. Install custom hooks with `bash scripts/install-git-hooks.sh`
2. Run `bd init` and select "merge" for git hooks
3. Beads creates chaining wrapper that calls custom hooks first

**Note**: `bd hooks install` uses a different pattern (thin shims) that doesn't call custom hooks - use `bd init` instead.

## File Targets

TableTest can appear in:
1. Java files (multiline string parameter to `@TableTest` annotation)
2. Kotlin files (multiline string parameter to `@TableTest` annotation)
3. Standalone `.table` files

## Reference Documentation

- TableTest: https://github.com/nchaugen/tabletest
- TableTest AGENTS.md: https://raw.githubusercontent.com/nchaugen/tabletest/refs/heads/main/AGENTS.md (format specification)
- TableTest IntelliJ Plugin: `~/IdeaProjects/tabletest-intellij` (working reference implementation)
- tabletest-parser API: `TableParser.parse(String)` → `Table` with `headers()`, `row(int)`, etc.
- Spotless: https://github.com/diffplug/spotless
- Spotless FormatterStep docs: Check contributing guide in Spotless repo
- Beads: Issue tracking tool (bd command)

## Notes for Claude

- User prefers pair programming style (discuss then implement)
- Follow TDD: Red → Green → Refactor
- Use TableTest for test organization when appropriate
- Prefer functional style, streams over loops
- British English (Oxford spelling)
- No copyright headers (added by build)
- Keep commits focused and atomic
