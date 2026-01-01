# Contributing to TableTest Formatter

Thank you for your interest in contributing to TableTest Formatter!

## Project Structure

This is a multi-module Maven project with hexagonal architecture:

```
tabletest-formatter/
├── tabletest-formatter-core/       # Core formatting logic
├── tabletest-formatter-cli/        # Command-line interface
└── tabletest-formatter-spotless/   # Spotless integration (FormatterStep)
```

### Module Responsibilities

- **tabletest-formatter-core**: Pure formatting logic, depends on `tabletest-parser:0.5.8`
- **tabletest-formatter-cli**: Picocli-based CLI, creates uber JAR with maven-shade-plugin
- **tabletest-formatter-spotless**: Implements Spotless FormatterStep for build tool integration

## Requirements

- **Java 21** or later
- **Maven 3.6+**

## Building

Build the entire project:

```bash
mvn clean install
```

Build a specific module:

```bash
mvn clean install -pl tabletest-formatter-core
```

## Running Tests

Run all tests:

```bash
mvn test
```

Run tests for a specific module:

```bash
mvn test -pl tabletest-formatter-spotless
```

## Setting Up Your Development Environment

### Git Hooks

This project uses custom git hooks for code quality and Beads for issue tracking.

**Installation order (important!):**

1. **Install custom hooks first:**
   ```bash
   bash scripts/install-git-hooks.sh
   ```

2. **Then initialize Beads:**
   ```bash
   bd init
   ```
   When prompted about git hooks, select **"merge"** to enable hook chaining.

**What the hooks do:**
- **pre-commit**: Formats code with Spotless, runs full test suite, auto-restages modified files
- **pre-push**: Prevents force-push to main/master branches
- **commit-msg**: Validates conventional commit format, checks for Claude attribution

## Development Workflow

### Commit Message Format

Follow [conventional commits](https://www.conventionalcommits.org/):
- `feat:` - New features
- `fix:` - Bug fixes
- `docs:` - Documentation changes
- `refactor:` - Code refactoring
- `test:` - Test changes
- `chore:` - Build/tooling changes

**Important**: Omit Claude Code attribution footer (checked by commit-msg hook)

### Working with Beads

Find available work:
```bash
bd ready           # Show issues ready to work (no blockers)
bd list --status=open  # All open issues
```

Start working on an issue:
```bash
bd show <id>       # Review issue details
bd update <id> --status=in_progress  # Claim it
```

Complete work:
```bash
bd close <id>      # Close completed issue
bd sync            # Sync with git remote
```

### TDD Workflow

Follow the Red → Green → Refactor cycle:
1. Write a failing test (Red)
2. Write minimal code to pass (Green)
3. Improve code structure (Refactor)
4. Repeat

**Testing guidelines:**
- Use `@Test` for single scenarios or complex setup
- Use `@TableTest` for data-driven tests with multiple similar cases
- See [CLAUDE.md](CLAUDE.md) for detailed testing patterns

For detailed development guidelines, coding standards, and architectural decisions, see [CLAUDE.md](CLAUDE.md).

## Code Formatting

The project uses Spotless with Palantir Java Format:

```bash
# Check formatting
mvn spotless:check

# Apply formatting
mvn spotless:apply
```

Pre-commit hooks automatically format code and run tests.

## Project Documentation

- **README.md** - User-facing documentation
- **CLAUDE.md** - Detailed development notes and architectural decisions
- **CONTRIBUTING.md** - This file (contributor guidelines)

## Getting Help

- Check existing issues in the GitHub repository
- Review [CLAUDE.md](CLAUDE.md) for architectural context
- Open a new issue for bugs or feature requests
