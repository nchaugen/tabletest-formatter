# TableTest Formatter

Tools to format TableTest tables (CLI and Spotless integration).

## Modules

- **tabletest-formatter-core**: Core formatting logic for TableTest tables
- **tabletest-formatter-cli**: Command-line interface for the formatter
- **tabletest-formatter-spotless**: Spotless integration for automatic formatting

## Requirements

- Java 21 or later
- Maven 3.6+

## Building

```bash
mvn clean install
```

## TableTest Format

TableTest uses a pipe-delimited table format for data-driven testing. Tables can appear in:
- Java files (as multiline string parameters to `@TableTest` annotation)
- Kotlin files (as multiline string parameters to `@TableTest` annotation)
- Standalone `.table` files

### Basic Structure

```java
@TableTest("""
    Scenario          | Input | Expected
    Basic case        | 5     | 10
    Edge case at zero | 0     | 0
    """)
```

**Core components:**
- Header row defining column names separated by `|`
- Data rows containing test values
- Optional scenario column (leftmost) for describing each row
- Comments: lines starting with `//` are ignored
- Blank lines permitted for readability

### Value Formatting

**Quoting:** Use quotes when values contain special characters: pipe (`|`), quotation marks, or start with bracket/brace. Choose single or double quotes based on content. To include quotes within values, use nested quotes (e.g., `'He said "hello"'` or `"It's working"`). Backslash escaping is not supported.

**Empty values:**
- `''` or `""` represents empty string
- Blank cells indicate null for non-primitive types

**Collections:**
- Lists: `[1, 2, 3]`
- Sets: `{1, 2, 3}`
- Maps: `[key: value, key: value]`
- Nested structures supported: `[Alice: [95, 87], Bob: [78, 85]]`

## Formatting Rules

This formatter applies consistent formatting with sensible defaults based on established patterns from the [IntelliJ IDEA plugin](https://github.com/nchaugen/tabletest-intellij).

### Core Rules

1. **Column alignment** - All pipes align vertically based on the widest cell in each column
2. **Pipe spacing** - Pipes always have space padding: ` | `
3. **Quote preservation** - User's original quote choices preserved (`'1'` stays `'1'`, `"2"` stays `"2"`)
4. **Indentation** - Tables inside Java/Kotlin files are indented to match their `@TableTest` annotation position (base indentation is auto-detected from the annotation's leading whitespace)
5. **Comments and blank lines** - Preserved exactly as-is, including indentation
6. **Empty cells** - Padded with spaces to maintain column alignment

### Collection Formatting

**Spacing normalization inside collection literals:**
- Space after comma: `[1,2,3]` → `[1, 2, 3]`
- Space after colon in maps: `[a:b,c:d]` → `[a: b, c: d]`
- Remove extra spaces inside brackets/braces: `[ [] ]` → `[[]]`, `{ [ ] }` → `{[]}`
- Applies to lists `[...]`, sets `{...}`, and maps `[key: value, ...]`
- Nested structures formatted recursively: `[a:[1,2],b:[3,4]]` → `[a: [1, 2], b: [3, 4]]`

**Important:** Spacing rules only apply to collection literals. Plain values like `a,b,c` remain unchanged (no spaces added around commas).

### Edge Cases Handled

- **Unicode characters**: Chinese, Japanese, Korean, Arabic, Hebrew, Thai, Hindi, Greek, etc.
- **Emojis**: Including complex emojis with flags and skin tones (proper width calculation)
- **Empty collections**: `[]`, `[:]`, `{}`
- **Empty strings**: `''` and `""`
- **Special characters**: Pipes, quotes, brackets in quoted values
- **Nested collections**: Multiple levels of nesting with proper spacing

### Error Handling

The formatter follows a **graceful degradation** policy to ensure it never breaks your build:

**Graceful degradation for malformed input:**
- **Malformed tables** (mismatched columns, corrupted structure) → returned unchanged
- **Unparseable content** (invalid syntax, unbalanced quotes) → returned unchanged
- **Empty or whitespace-only input** → returned unchanged
- **Parse failures** (any `TableTestParseException`) → returned unchanged

This "fail-safe" behaviour ensures that:
- Formatting never causes compilation errors
- Syntax errors in tables don't block your build
- You can gradually fix table syntax issues without breaking CI

**Exceptions that propagate to caller:**
- `NullPointerException` - if required parameters are null
- `IllegalArgumentException` - if indent/tab size parameters are negative

**Successful formatting:**
- **Single-column tables** → formatted correctly (no pipes needed)
- **Empty cells** → padded to maintain alignment
- **Comments and blank lines** → preserved exactly as-is

### Formatting Examples

**Basic alignment:**
```
Before:                          After:
Scenario|Input|Expected          Scenario   | Input | Expected
Basic case|5|10                  Basic case | 5     | 10
```

**Collection spacing:**
```
Before:                          After:
Input|Expected                   Input        | Expected
[1,2,3]|[10,20,30]               [1, 2, 3]    | [10, 20, 30]
[a:1,b:2]|result                 [a: 1, b: 2] | result
[ [] ]|[a:[]]                    [[]]         | [a: []]
```

**Empty cells:**
```
Before:                         After:
a|b|c                           a | b | c
||                                |   |
```

**Comments and blank lines:**
```
Before:                         After:
a|b                             a  | b
// comment                      // comment

x|y                             x  | y
```

**Indentation in Java/Kotlin files:**
```java
Before:
    @TableTest("""
Scenario|Input|Expected
Basic case|5|10
        """)

After:
    @TableTest("""
        Scenario   | Input | Expected
        Basic case | 5     | 10
        """)
```
The table content aligns with the `@TableTest` annotation's indentation level, with additional indentation added based on the configured `indentSize`.

## Configuration Parameters

Both the CLI and Spotless integration support configurable `tabSize` and `indentSize` parameters:

| Parameter | CLI Option | Spotless Method | Default | Description |
|-----------|------------|-----------------|---------|-------------|
| **Tab Size** | `--tab-size <N>` | `create(N, ...)` | `4` | Number of spaces a tab character represents when converting tabs to spaces |
| **Indent Size** | `--indent-size <N>` | `create(..., N)` | `4` | Number of spaces per indent level for table content |

**How indentation works:**

**CLI**:
- Both `--tab-size` and `--indent-size` are configurable
- Tables are indented using `indentSize` spaces per level
- Useful for standalone `.table` files or when you want additional indentation

**Spotless**:
- Both `tabSize` and `indentSize` are configurable via `create(tabSize, indentSize)`
- Base indentation is **auto-detected** from the `@TableTest` annotation's position in the source file
- Additional indentation can be added via `indentSize` parameter (default: `4`)
- Example: `create(4, 0)` aligns tables with their annotation, `create(4, 4)` adds 4 spaces of extra indentation (default)

## Usage

### Spotless Integration (Gradle)

The tabletest-formatter-spotless module integrates with [Spotless](https://github.com/diffplug/spotless) to automatically format TableTest tables in your Gradle projects.

**Supported file types:**
- Java files (`.java`) with `@TableTest` annotations
- Kotlin files (`.kt`) with `@TableTest` annotations
- Standalone `.table` files

**Note:** Maven integration requires contributing the formatter to the Spotless project itself, as the Spotless Maven plugin doesn't support programmatic custom steps. Gradle projects can use the formatter today via `addStep()`.

#### Gradle Configuration

Add to your `build.gradle`:

```groovy
import io.github.nchaugen.tabletest.formatter.spotless.TableTestFormatterStep

buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath 'io.github.nchaugen.tabletest:tabletest-formatter-spotless:0.1.0-SNAPSHOT'
    }
}

plugins {
    id 'com.diffplug.spotless' version '8.1.0'
}

spotless {
    // Format standalone .table files
    format 'tableFiles', {
        target 'src/**/*.table'
        addStep(TableTestFormatterStep.create())
    }

    // Format @TableTest in Java files
    java {
        target 'src/**/*.java'
        addStep(TableTestFormatterStep.create())
    }

    // Format @TableTest in Kotlin files
    kotlin {
        target 'src/**/*.kt'
        addStep(TableTestFormatterStep.create())
    }
}
```

#### Configuration Options

The Spotless integration supports two configuration parameters: **tab size** and **indent size**.

```groovy
// Default configuration (tab size: 4, indent size: 4)
addStep(TableTestFormatterStep.create())

// Custom tab size only (indent size defaults to 4)
addStep(TableTestFormatterStep.create(2))

// Custom tab size and indent size
addStep(TableTestFormatterStep.create(4, 0))  // align with annotation, no extra indent
addStep(TableTestFormatterStep.create(2, 2))  // 2-space tabs, 2-space indent
```

**Tab size** controls how tab characters in source files are converted to spaces when calculating indentation. If your project uses 2-space tabs instead of 4, configure accordingly.

**Indent size** controls additional indentation added to table content beyond the base indentation:
- **Default (4)**: Adds 4 spaces of indentation to table content
- **Zero (0)**: Tables align exactly with their `@TableTest` annotation position
- **Other values**: Adds that many spaces of extra indentation (e.g., `2` adds 2 spaces to each table line)

**Indentation behaviour**: The formatter automatically detects the base indentation from the `@TableTest` annotation's position in your source file. The `indentSize` parameter adds additional indentation on top of this base level.

**Usage:**
```bash
# Check formatting (exits with error if changes needed)
./gradlew spotlessCheck

# Apply formatting
./gradlew spotlessApply
```

#### CI Integration

Add to your CI pipeline to enforce formatting:

```yaml
# GitHub Actions example
- name: Check TableTest formatting
  run: ./gradlew spotlessCheck
```

### Command-Line Interface

The tabletest-formatter-cli module provides a standalone command-line tool for formatting TableTest tables.

#### Installation

Build the CLI tool:

```bash
mvn clean install
```

The executable JAR will be located at:
```
tabletest-formatter-cli/target/tabletest-formatter-cli-0.1.0-SNAPSHOT.jar
```

#### Basic Usage

**Format files in-place:**
```bash
java -jar tabletest-formatter-cli.jar <files-or-directories>
```

**Check formatting without modifying files:**
```bash
java -jar tabletest-formatter-cli.jar --check <files-or-directories>
```

#### Command-Line Options

| Option | Description | Default |
|--------|-------------|---------|
| `-c, --check` | Check if files need formatting without modifying them | `false` |
| `-v, --verbose` | Print detailed output for each file | `false` |
| `--indent-size <N>` | Number of spaces per indent level | `4` |
| `--tab-size <N>` | Number of spaces a tab character represents | `4` |
| `-h, --help` | Show help message | |
| `--version` | Show version information | |

#### Examples

**Format all TableTest files in a project:**
```bash
java -jar tabletest-formatter-cli.jar src/
```

**Check if files need formatting (useful in CI):**
```bash
java -jar tabletest-formatter-cli.jar --check src/
```

**Format with custom indentation:**
```bash
java -jar tabletest-formatter-cli.jar --indent-size 2 --tab-size 2 src/
```

**Format specific files with verbose output:**
```bash
java -jar tabletest-formatter-cli.jar --verbose \
  src/test/java/MyTest.java \
  src/test/resources/test-data.table
```

#### Exit Codes

The CLI uses the following exit codes for automation and CI integration:

| Exit Code | Meaning |
|-----------|---------|
| `0` | Success (check: no changes needed, apply: formatting succeeded) |
| `1` | Failure (check: changes needed OR errors, apply: errors occurred) |
| `2` | Invalid usage (incorrect command-line arguments) |

**CI Integration Example:**

```yaml
# GitHub Actions example
- name: Check TableTest formatting
  run: |
    java -jar tabletest-formatter-cli.jar --check src/
    if [ $? -ne 0 ]; then
      echo "Formatting issues found. Run 'java -jar tabletest-formatter-cli.jar src/' to fix."
      exit 1
    fi
```

## License

Licensed under the Apache Licence, Version 2.0. See [LICENSE](LICENSE) for details.
