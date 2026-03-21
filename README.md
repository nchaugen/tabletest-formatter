> [!NOTE]
> **TableTest Formatter is natively supported in Spotless!**
> Spotless Gradle 8.3.0+ / Maven 3.3.0+ support Java and Kotlin files (requires Java 21+ build).
> Spotless Gradle 8.4.0+ / Maven 3.4.0+ also support `.table` files and Java 17+ builds.
> No extra dependencies needed — see [Gradle](#gradle-spotless) and [Maven](#maven-spotless) below.

# TableTest Formatter

A formatter for [TableTest](https://github.com/nchaugen/tabletest) tables that ensures consistent, readable formatting across your test suite.

**Formats tables in:**
- Java files with `@TableTest` annotations
- Kotlin files with `@TableTest` annotations
- Standalone `.table` files

**Available as:**
- **Spotless plugin** for Gradle and Maven (automatic formatting in your build)
- **CLI tool** for standalone formatting or CI integration

## Key Features

- **Consistent, readable tables** – Vertically aligned columns and rows with normalized spacing
- **Context-aware** – Works in standalone `.table` files, Java/Kotlin text blocks, and Java string arrays
- **Smart collection formatting** – Normalizes spacing in lists, sets, and maps while preserving your quote choices
- **Preserves structure** – Comments and blank lines maintained exactly as written
- **Unicode support** – Accurate width calculation for CJK characters, emojis, and special characters
- **Safe by default** – Never breaks builds; returns input unchanged on parse errors
- **Flexible integration** – Spotless plugin (Gradle and Maven) or CLI tool

See [FEATURES.md](FEATURES.md) for comprehensive feature documentation.

## Table of Contents

- [What is TableTest Format?](#what-is-tabletest-format)
- [Formatting Behavior](#formatting-behavior)
  - [Formatting Rules](#formatting-rules)
  - [Configuration Options](#configuration-options)
- [Integration](#integration)
  - [Gradle (Spotless)](#gradle-spotless)
  - [Maven (Spotless)](#maven-spotless)
  - [CLI / Manual](#cli--manual)
- [Requirements](#requirements)
- [Contributing](#contributing)
- [License](#license)

## What is TableTest Format?

TableTest uses a pipe-delimited table format for data-driven testing. Tables can appear in Java files (as multiline string parameters to `@TableTest` annotation), Kotlin files (as multiline string parameters to `@TableTest` annotation), or standalone `.table` files.

**Text block syntax (Java/Kotlin):**

```java
@TableTest("""
    Scenario          | Input | Expected
    Basic case        | 5     | 10
    Edge case at zero | 0     | 0
    """)
```

**String array syntax (Java only):**

```java
@TableTest({
    "Scenario          | Input | Expected",
    "Basic case        | 5     | 10      ",
    "Edge case at zero | 0     | 0       "
})
```

**Core components:**
- Header row defining column names separated by `|`
- Data rows containing test values
- Optional scenario column (leftmost) for describing each row
- Comments: lines starting with `//` are ignored
- Blank lines permitted for readability

**Value formatting:**
- **Quoting:** Use quotes when values contain special characters (pipe, quotes, brackets). Choose single or double quotes based on content.
- **Empty values:** `''` or `""` represents empty string; blank cells indicate null for non-primitive types
- **Collections:** Lists `[1, 2, 3]`, Sets `{1, 2, 3}`, Maps `[key: value, key: value]`, nested structures supported

For complete format specification, see [TableTest documentation](https://github.com/nchaugen/tabletest).

## Formatting Behavior

This formatter applies consistent formatting with sensible defaults based on established patterns from the [IntelliJ IDEA plugin](https://github.com/nchaugen/tabletest-intellij).

### Formatting Rules

**Column alignment:**
All pipes align vertically based on the widest cell in each column. Values are left-aligned and padded with spaces.

```
Before:                          After:
Scenario|Input|Expected          Scenario   | Input | Expected
Basic case|5|10                  Basic case | 5     | 10
```

**Row alignment:**
All rows start at the same column position, creating a straight left edge regardless of varying input indentation.

```
Before:                          After:
    a | b | c                    a            | b | c
longer value | d | e             longer value | d | e
        x | y | z                x            | y | z
```

**Pipe spacing:**
Pipes always have space padding: ` | ` (space-pipe-space).

**Collection formatting:**
Spacing normalization inside collection literals:
- Space after comma: `[1,2,3]` → `[1, 2, 3]`
- Space after colon in maps: `[a:b,c:d]` → `[a: b, c: d]`
- Remove extra spaces inside brackets: `[ [] ]` → `[[]]`
- Nested structures formatted recursively: `[a:[1,2],b:[3,4]]` → `[a: [1, 2], b: [3, 4]]`

**Quote preservation:**
User's original quote choices preserved (`'1'` stays `'1'`, `"2"` stays `"2"`).

**Comments and blank lines:**
Preserved exactly as-is, including indentation.

**Indentation:**
- Standalone `.table` files: Tables start at left margin
- Java/Kotlin files: Tables indented relative to `@TableTest` annotation position
- Base indentation preserved (tabs stay tabs, spaces stay spaces)
- Additional indentation added using configured `indentStyle`

**Error handling:**
The formatter follows a **graceful degradation** policy – malformed tables, unparseable content, or empty input returns unchanged. This ensures formatting never breaks your build.

### Configuration Options

TableTest Formatter reads configuration from `.editorconfig` files following the [EditorConfig specification](https://editorconfig.org). This provides a standard, IDE-integrated way to configure formatting across your entire team.

**Supported properties:**

| Property        | Values         | Default | Description                                           |
|-----------------|----------------|---------|-------------------------------------------------------|
| `indent_style`  | `space`, `tab` | `space` | Type of indentation for additional indent beyond base |
| `indent_size`   | `0`-`N`        | `4`     | Number of indent characters to add beyond base        |

**Example `.editorconfig`:**

```ini
# Java and Kotlin files - 4 space indentation
[*.{java,kt}]
indent_style = space
indent_size = 4

# Standalone table files - no indentation
[*.table]
indent_style = space
indent_size = 0

# Alternative: 2-space indentation
[src/test/java/**/*.java]
indent_style = space
indent_size = 2
```

**How it works:**
- Place `.editorconfig` in your project root or source directories
- The formatter searches up the directory tree to find the applicable configuration
- Settings cascade: more specific `.editorconfig` files override parent directories
- Properties under `[*]` apply to all file types — you don't need file-specific sections unless you want different settings per type
- If no `.editorconfig` is found, defaults to 4 spaces
- Base indentation from source files is always preserved

**Indent behavior:**
- `indent_size = 0` – Tables align exactly with their `@TableTest` annotation
- `indent_size = N` – Adds N indent characters (spaces or tabs) beyond the base level

## Integration

### Gradle (Spotless)

TableTest formatter is natively supported in [Spotless](https://github.com/diffplug/spotless) — no extra dependencies required. Plugin version 8.3.0+ supports Java and Kotlin files (requires Java 21+ build). Version 8.4.0+ adds `.table` file support and works with Java 17+ builds.

#### Setup

```groovy
plugins {
    id 'com.diffplug.spotless' version '8.4.0'
}

spotless {
    java {
        tableTestFormatter()
        // tableTestFormatter('1.1.1') // pin a specific version
    }
    kotlin {
        tableTestFormatter()
    }
    tableTest {
        target 'src/**/*.table'
        tableTestFormatter()
    }
}
```

#### Configuration

Formatting settings are read from `.editorconfig` files. Place one in your project root:

```ini
[*.{java,kt}]
indent_style = space
indent_size = 4
```

The formatter searches up the directory tree to find the applicable configuration for each file.

#### Usage

```bash
# Check formatting (exits with error if changes needed)
./gradlew spotlessCheck

# Apply formatting
./gradlew spotlessApply
```

#### CI Integration

```yaml
# GitHub Actions example
- name: Check TableTest formatting
  run: ./gradlew spotlessCheck
```

### Maven (Spotless)

TableTest formatter is natively supported in the [Spotless Maven plugin](https://github.com/diffplug/spotless/tree/main/plugin-maven) — no extra dependencies required. Plugin version 3.3.0+ supports Java and Kotlin files (requires Java 21+ build). Version 3.4.0+ adds `.table` file support and works with Java 17+ builds.

#### Setup

Add the Spotless Maven plugin to your `pom.xml`:

```xml
<plugin>
    <groupId>com.diffplug.spotless</groupId>
    <artifactId>spotless-maven-plugin</artifactId>
    <version>3.4.0</version>
    <configuration>
        <java>
            <tableTestFormatter/>
            <!-- <tableTestFormatter><version>1.1.1</version></tableTestFormatter> pin a specific version -->
        </java>
        <kotlin>
            <tableTestFormatter/>
        </kotlin>
        <tableTest>
            <includes>
                <include>src/**/*.table</include>
            </includes>
            <tableTestFormatter/>
        </tableTest>
    </configuration>
</plugin>
```

#### Configuration

Formatting settings are read from `.editorconfig` files. Place one in your project root:

```ini
[*.{java,kt}]
indent_style = space
indent_size = 4
```

The formatter searches up the directory tree to find the applicable configuration for each file.

#### Usage

```bash
# Check formatting (exits with error if changes needed)
mvn spotless:check

# Apply formatting
mvn spotless:apply
```

#### CI Integration

```yaml
# GitHub Actions example
- name: Check TableTest formatting
  run: mvn spotless:check
```

### CLI / Manual

The tabletest-formatter-cli module provides a standalone command-line tool for formatting TableTest tables.

#### Installation

**Option 1: Download from Maven Central**

Java developers can fetch the CLI JAR using Maven:

```bash
mvn dependency:get \
  -Dartifact=org.tabletest:tabletest-formatter-cli:1.1.1:jar:shaded \
  -Dtransitive=false
```

The JAR will be downloaded to your local Maven repository:
```
~/.m2/repository/org/tabletest/tabletest-formatter-cli/1.1.1/
```

**Option 2: Download from GitHub Releases**

Download the latest `tabletest-formatter-cli-<version>.jar` from the [Releases page](https://github.com/nchaugen/tabletest-formatter/releases).

**Option 3: Build from Source**

```bash
git clone https://github.com/nchaugen/tabletest-formatter.git
cd tabletest-formatter
mvn clean install
```

The executable JAR will be at:
```
tabletest-formatter-cli/target/tabletest-formatter-cli-1.1.1.jar
```

#### Usage

**Format files in-place:**
```bash
java -jar tabletest-formatter-cli.jar <files-or-directories>
```

**Check formatting without modifying files:**
```bash
java -jar tabletest-formatter-cli.jar --check <files-or-directories>
```

**Configuration:**

Formatting settings are read from `.editorconfig` files. Create a `.editorconfig` in your project root:

```ini
[*.java]
indent_style = space
indent_size = 4

[*.kt]
indent_style = space
indent_size = 4

[*.table]
indent_style = space
indent_size = 0
```

**Command-line options:**

| Option          | Description                                           | Default |
|-----------------|-------------------------------------------------------|---------|
| `-c, --check`   | Check if files need formatting without modifying them | `false` |
| `-v, --verbose` | Print detailed output for each file                   | `false` |
| `-h, --help`    | Show help message                                     |         |
| `--version`     | Show version information                              |         |

**Examples:**

```bash
# Format all TableTest files in a project
java -jar tabletest-formatter-cli.jar src/

# Check if files need formatting (useful in CI)
java -jar tabletest-formatter-cli.jar --check src/

# Format specific files with verbose output
java -jar tabletest-formatter-cli.jar --verbose \
  src/test/java/MyTest.java \
  src/test/resources/test-data.table
```

**Exit codes:**

| Exit Code | Meaning                                                             |
|-----------|---------------------------------------------------------------------|
| `0`       | Success (check: no changes needed, apply: formatting succeeded)     |
| `1`       | Failure (check: changes needed OR errors, apply: errors occurred)   |
| `2`       | Invalid usage (incorrect command-line arguments)                    |

#### CI Integration

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

## Requirements

**Minimum:**
- **Java:** 17 or later

**Tested with:**
- **Gradle:** 8.12
- **Maven:** 3.9.9
- **Spotless Gradle Plugin:** 8.4.0
- **Spotless Maven Plugin:** 3.4.0

**Minimum Spotless versions** for native `tableTestFormatter` support:
- **Spotless Gradle 8.3.0+ / Maven 3.3.0+** — Java and Kotlin files (requires Java 21+ build)
- **Spotless Gradle 8.4.0+ / Maven 3.4.0+** — Java 17+ build support and `.table` file formatting

The formatter runs on any platform with Java 17+: Linux, macOS, Windows.

## Contributing

Contributions are welcome! See [CONTRIBUTING.md](CONTRIBUTING.md) for:
- Project structure and architecture
- Build and test instructions
- Development workflow and guidelines
- Code formatting standards

## License

Licensed under the Apache Licence, Version 2.0. See [LICENSE](LICENSE) for details.
