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

**Quoting:** Use quotes when values contain special characters: pipe (`|`), quotation marks, or start with bracket/brace. Choose single or double quotes based on content.

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
4. **Indentation** - Preserved when formatting tables inside Java/Kotlin files
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
Before:                          After:
a|b|c                           a | b | c
||                                |   |
```

**Comments and blank lines:**
```
Before:                          After:
a|b                             a  | b
// comment                      // comment

x|y                             x  | y
```

**Note:** Future versions may add configuration options for formatting preferences.

## Usage

Documentation coming soon.

## License

Licensed under the Apache Licence, Version 2.0. See [LICENSE](LICENSE) for details.
