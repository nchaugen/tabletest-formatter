# TableTest Formatter Features

This document describes the features provided by the TableTest Formatter.

**Key features:**
- **Consistent, readable tables** – Vertically aligned columns and rows with normalized spacing
- **Context-aware** – Works in standalone `.table` files, Java text blocks, and Kotlin raw strings
- **Smart collection formatting** – Normalizes spacing in lists, sets, and maps while preserving your quote choices
- **Preserves structure** – Comments and blank lines maintained exactly as written
- **Unicode support** – Accurate width calculation for CJK characters, emojis, and special characters
- **Safe by default** – Never breaks builds; returns input unchanged on parse errors
- **Flexible integration** – Spotless plugin (Gradle), CLI tool, or Maven exec-maven-plugin

## Table of Contents

- [Supported Contexts](#supported-contexts)
- [Integration Methods](#integration-methods)
- [Features](#features)
  - [Column Alignment](#column-alignment)
  - [Row Alignment](#row-alignment)
  - [Pipe Spacing](#pipe-spacing)
  - [Collection Formatting](#collection-formatting)
  - [Quote Preservation](#quote-preservation)
  - [Indentation](#indentation)
  - [Comments and Blank Lines](#comments-and-blank-lines)
  - [Empty Cell Handling](#empty-cell-handling)
  - [Error Handling (Graceful Degradation)](#error-handling-graceful-degradation)
- [Configuration Options](#configuration-options)
- [Feature Summary](#feature-summary)
- [Platform Support](#platform-support)

## Supported Contexts

The formatter provides TableTest formatting support in three contexts:

| Context              | Description                                                  |
|----------------------|--------------------------------------------------------------|
| **Native files**     | Standalone `.table` files                                    |
| **Java injection**   | TableTest content inside `@TableTest` annotation text blocks |
| **Kotlin injection** | TableTest content inside `@TableTest` annotation raw strings |

All formatting features work identically across these contexts.

## Integration Methods

TableTest Formatter is available through three integration methods:

| Method                        | Description                                     | Best For                                 |
|-------------------------------|-------------------------------------------------|------------------------------------------|
| **Spotless (Gradle)**         | Build-integrated formatting via Spotless plugin | Gradle projects, automated formatting    |
| **CLI**                       | Standalone command-line tool                    | CI/CD, manual formatting, Maven projects |
| **Maven (exec-maven-plugin)** | Maven build integration using CLI               | Maven projects without Spotless          |

## Features

### Column Alignment

Aligns columns vertically by padding cells to match the widest value in each column. Pipe delimiters (`|`) line up across all rows, creating visually consistent tables.

**Before:**
```
Scenario|Input|Expected
Basic case|5|10
Edge case at zero|0|0
```

**After:**
```
Scenario          | Input | Expected
Basic case        | 5     | 10
Edge case at zero | 0     | 0
```

**Width calculation:**
- Uses wcwidth algorithm (IEEE Std 1002.1-2001) for accurate Unicode width
- Correctly handles CJK characters (Chinese, Japanese, Korean)
- Supports emojis including complex ones with flags and skin tones
- Note: IDE fonts may not render with true monospace widths; verify output in terminals

### Row Alignment

Creates a straight left edge – all rows start at the same column position.

**Before:**
```
    a | b | c
longer value | d | e
        x | y | z
```

**After:**
```
a            | b | c
longer value | d | e
x            | y | z
```

### Pipe Spacing

Applies consistent spacing around pipe delimiters: ` | ` (space-pipe-space). This creates clear visual separation between columns.

**Format:** Each pipe has exactly one space before and one space after (except at line boundaries).

### Collection Formatting

Normalises spacing inside collection literals while preserving user quote choices:

**Spacing rules:**
- Space after comma: `[1,2,3]` → `[1, 2, 3]`
- Space after colon in maps: `[a:b,c:d]` → `[a: b, c: d]`
- Remove extra spaces inside brackets/braces: `[ [] ]` → `[[]]`, `{ [ ] }` → `{[]}`
- Nested structures formatted recursively: `[a:[1,2],b:[3,4]]` → `[a: [1, 2], b: [3, 4]]`

**Applies to:**
- Lists: `[...]`
- Sets: `{...}`
- Maps: `[key: value, ...]`

**Important:** Spacing rules only apply to collection literals. Plain values like `a,b,c` remain unchanged.

### Quote Preservation

Preserves the user's original quote choices (`'single'` vs `"double"`). The formatter does not change which quote type is used, only normalizes spacing and alignment.

**Example:**
- Input: `'1'` stays `'1'`
- Input: `"2"` stays `"2"`
- Input: `'He said "hello"'` stays `'He said "hello"'`

### Indentation

Positions the table appropriately within its context:

**Standalone `.table` files:**
- Tables start at the left margin (no indentation)

**Java and Kotlin files:**
- Tables are indented relative to their `@TableTest` annotation position
- Base indentation from source files is preserved (tabs stay tabs, spaces stay spaces)
- Additional indentation is added using the configured `indentType` (default: 4 spaces)
- Configurable via `indentSize` parameter (0 = align with annotation, N = add N indent characters)

**Before:**
```java
    @TableTest("""
Scenario|Input|Expected
Basic case|5|10
        """)
```

**After:**
```java
    @TableTest("""
        Scenario   | Input | Expected
        Basic case | 5     | 10
        """)
```

### Comments and Blank Lines

Preserves comments and blank lines exactly as-is, including their indentation. Comments (lines starting with `//`) and blank lines are formatted along with the rest of the table.

**Example:**
```
Scenario   | Input | Expected
Basic case | 5     | 10
// This comment is preserved
Edge case  | 0     | 0

// Blank lines are preserved too
```

### Empty Cell Handling

Empty cells are padded with spaces to maintain column alignment. This ensures that pipes line up vertically even when some cells have no content.

**Before:**
```
a|b|c
||
x|y|
```

**After:**
```
a | b | c
  |   |
x | y |
```

### Error Handling (Graceful Degradation)

The formatter follows a **fail-safe policy** to ensure it never breaks your build:

**Returns input unchanged for:**
- Malformed tables (mismatched columns, corrupted structure)
- Unparseable content (invalid syntax, unbalanced quotes)
- Empty or whitespace-only input

**Propagates to caller:**
- `NullPointerException` - if required parameters are null
- `IllegalArgumentException` - if indent/tab size parameters are negative

**Benefits:**
- Formatting never causes compilation errors
- Syntax errors in tables don't block builds
- Gradual fixing of table syntax issues without breaking CI
- Safe to apply in any codebase state

## Configuration Options

All integration methods support two configuration parameters:

| Parameter       | Default | Values         | Description                                    |
|-----------------|---------|----------------|------------------------------------------------|
| **Indent Type** | `space` | `space`, `tab` | Type of indentation for additional indent      |
| **Indent Size** | `4`     | `0`-`N`        | Number of indent characters to add beyond base |

**Indent Type:**
- `space` - Additional indentation uses spaces
- `tab` - Additional indentation uses tabs
- Base indentation from source files is always preserved

**Indent Size:**
- `0` - Tables align exactly with their `@TableTest` annotation
- `N` - Adds N indent characters (spaces or tabs) beyond the base level

### Configuration by Integration Method

**Spotless (Gradle):**
```groovy
import io.github.nchaugen.tabletest.formatter.core.IndentType

addStep(TableTestFormatterStep.create())                          // defaults: space, 4
addStep(TableTestFormatterStep.create(2))                         // space, 2
addStep(TableTestFormatterStep.create(IndentType.SPACE, 0))       // space, 0
addStep(TableTestFormatterStep.create(IndentType.TAB, 4))         // tab, 4
```

**CLI:**
```bash
java -jar tabletest-formatter-cli.jar src/                        # defaults: space, 4
java -jar tabletest-formatter-cli.jar --indent-size 2 src/        # space, 2
java -jar tabletest-formatter-cli.jar --indent-type tab src/      # tab, 4
```

**Maven (exec-maven-plugin):**
```xml
<arguments>
    <argument>-jar</argument>
    <argument>${project.build.directory}/formatter/tabletest-formatter-cli-0.1.0-shaded.jar</argument>
    <argument>--indent-size</argument>
    <argument>2</argument>
    <argument>--indent-type</argument>
    <argument>tab</argument>
    <argument>${project.basedir}/src/test/java</argument>
</arguments>
```

## Feature Summary

| Feature                    | Description                                                    |
|----------------------------|----------------------------------------------------------------|
| **Column Alignment**       | Aligns pipes and pads cells based on widest value per column   |
| **Row Alignment**          | Creates straight left edge across all rows                     |
| **Pipe Spacing**           | Consistent `\|` format around all pipes                        |
| **Collection Formatting**  | Normalises spacing in lists, sets, and maps                    |
| **Quote Preservation**     | Maintains user's choice of single vs double quotes             |
| **Indentation**            | Context-aware indentation (file type and annotation position)  |
| **Comments & Blank Lines** | Preserves exactly as-is with proper indentation                |
| **Empty Cell Handling**    | Pads empty cells to maintain alignment                         |
| **Error Handling**         | Graceful degradation – never breaks builds                     |
| **Unicode Width**          | Accurate width calculation for CJK, emojis, special characters |

## Platform Support

The formatter runs on any platform with Java 21+:
- Linux
- macOS
- Windows

All features work identically across platforms.
