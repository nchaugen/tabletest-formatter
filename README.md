# TableTest Formatter

A formatter for [TableTest](https://github.com/nchaugen/tabletest) tables that ensures consistent, readable formatting across your test suite.

**Available as:**
- **Spotless plugin** for Gradle (automatic formatting in your build)
- **CLI tool** for standalone formatting or CI integration

**Formats tables in:**
- Java files with `@TableTest` annotations
- Kotlin files with `@TableTest` annotations
- Standalone `.table` files

## Table of Contents

- [Installation](#installation)
- [Quick Start](#quick-start)
- [TableTest Format](#tabletest-format)
- [Formatting Rules](#formatting-rules)
- [Configuration Parameters](#configuration-parameters)
- [Usage](#usage)
  - [Spotless Integration (Gradle)](#spotless-integration-gradle)
  - [Maven Integration (exec-maven-plugin)](#maven-integration-exec-maven-plugin)
  - [Command-Line Interface](#command-line-interface)
- [Contributing](#contributing)
- [License](#license)

## Installation

### Gradle (Spotless Integration)

Add the formatter dependency to your `build.gradle`:

```groovy
buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath 'io.github.nchaugen.tabletest:tabletest-formatter-spotless:0.1.0'
    }
}
```

See [Spotless Integration](#spotless-integration-gradle) for configuration and usage.

### Command-Line Tool

**Option 1: Download from Maven Central**

Java developers can fetch the CLI JAR using Maven:

```bash
mvn dependency:get \
  -Dartifact=io.github.nchaugen.tabletest:tabletest-formatter-cli:0.1.0:jar:shaded \
  -Dtransitive=false
```

The JAR will be downloaded to your local Maven repository:
```
~/.m2/repository/io/github/nchaugen/tabletest/tabletest-formatter-cli/0.1.0/
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
tabletest-formatter-cli/target/tabletest-formatter-cli-0.1.0.jar
```

See [Command-Line Interface](#command-line-interface) for usage instructions.

## Quick Start

### Gradle (Spotless Integration)

Add to your `build.gradle`:

```groovy
import io.github.nchaugen.tabletest.formatter.spotless.TableTestFormatterStep

buildscript {
    repositories { mavenCentral() }
    dependencies {
        classpath 'io.github.nchaugen.tabletest:tabletest-formatter-spotless:0.1.0'
    }
}

plugins {
    id 'com.diffplug.spotless' version '8.1.0'
}

spotless {
    java {
        addStep(TableTestFormatterStep.create())
    }
}
```

Then run: `./gradlew spotlessApply`

### Command-Line Tool

Build the CLI tool:
```bash
mvn clean install
```

Format your files:
```bash
java -jar tabletest-formatter-cli/target/tabletest-formatter-cli-0.1.0.jar src/
```

---

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
4. **Indentation** - Tables inside Java/Kotlin files are indented relative to their `@TableTest` annotation
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

Both the CLI and Spotless integration support configurable `indentType` and `indentSize` parameters:

| Parameter | CLI Option | Spotless Method | Default | Description |
|-----------|------------|-----------------|---------|-------------|
| **Indent Type** | `--indent-type <type>` | `create(IndentType, ...)` | `space` | Type of indentation: `space` or `tab`. Base indentation from source is preserved, additional indentation uses this type |
| **Indent Size** | `--indent-size <N>` | `create(..., N)` | `4` | Number of indent characters to add (spaces or tabs depending on indent type) |

**How indentation works:**

The formatter **preserves the base indentation** from your source files (tabs stay tabs, spaces stay spaces) and adds additional indentation for table content using the specified `indentType`.

**CLI**:
- `--indent-type` specifies whether to use spaces or tabs for additional indentation (default: `space`)
- `--indent-size` specifies how many indent characters to add (default: `4`)
- Base indentation from the file is automatically detected and preserved

**Spotless**:
- Both `indentType` and `indentSize` are configurable via `create(IndentType, indentSize)`
- Tables are indented relative to their `@TableTest` annotation position in the source file
- Base indentation (tabs or spaces) is preserved from the source file
- Additional indentation is added using the specified `indentType`
- Example: `create(IndentType.SPACE, 0)` aligns tables with their annotation, `create(IndentType.SPACE, 4)` adds 4 spaces of extra indentation (default)

## Usage

### Spotless Integration (Gradle)

The tabletest-formatter-spotless module integrates with [Spotless](https://github.com/diffplug/spotless) to automatically format TableTest tables in your Gradle projects.

**Supported file types:**
- Java files (`.java`) with `@TableTest` annotations
- Kotlin files (`.kt`) with `@TableTest` annotations
- Standalone `.table` files

**Note:** Official Spotless Maven integration requires contributing the formatter to the Spotless project itself, as the Spotless Maven plugin doesn't support programmatic custom steps. Maven projects can use [exec-maven-plugin](#maven-integration-exec-maven-plugin) to run the CLI tool during builds. Gradle projects can use the formatter today via `addStep()`.

#### Gradle Configuration

Add to your `build.gradle`:

```groovy
import io.github.nchaugen.tabletest.formatter.spotless.TableTestFormatterStep

buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath 'io.github.nchaugen.tabletest:tabletest-formatter-spotless:0.1.0'
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

The Spotless integration supports two configuration parameters: **indent type** and **indent size**.

```groovy
import io.github.nchaugen.tabletest.formatter.core.IndentType

// Default configuration (space indentation, indent size: 4)
addStep(TableTestFormatterStep.create())

// Custom indent size only (indent type defaults to space)
addStep(TableTestFormatterStep.create(2))

// Custom indent type and indent size
addStep(TableTestFormatterStep.create(IndentType.SPACE, 0))  // align with annotation, no extra indent
addStep(TableTestFormatterStep.create(IndentType.SPACE, 2))  // space indent, 2-space indent size
addStep(TableTestFormatterStep.create(IndentType.TAB, 4))    // tab indent, 4-tab indent size
```

**Indent type** controls whether additional indentation uses spaces or tabs:
- **`IndentType.SPACE`** (default): Additional indentation uses spaces
- **`IndentType.TAB`**: Additional indentation uses tabs
- Base indentation from source files is always preserved (tabs stay tabs, spaces stay spaces)

**Indent size** controls additional indentation added to table content beyond the base indentation:
- **Default (4)**: Adds 4 indent characters to table content
- **Zero (0)**: Tables align exactly with their `@TableTest` annotation position
- **Other values**: Adds that many indent characters (e.g., `2` adds 2 spaces or 2 tabs depending on indent type)

**Indentation behaviour**: Tables are indented relative to their `@TableTest` annotation position in the source file. Base indentation is automatically detected and preserved. The `indentSize` parameter adds additional indentation on top of this base level using the specified `indentType`.

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

### Maven Integration (exec-maven-plugin)

Maven projects can use the CLI tool via [exec-maven-plugin](https://www.mojohaus.org/exec-maven-plugin/) to automatically format tables during the build. This provides build-integrated formatting until official Spotless Maven support is available.

#### Setup

First, ensure the CLI JAR is available. Add the formatter as a build dependency:

```xml
<build>
    <plugins>
        <!-- Build the formatter CLI first -->
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-dependency-plugin</artifactId>
            <version>3.3.0</version>
            <executions>
                <execution>
                    <id>copy-formatter-cli</id>
                    <phase>generate-resources</phase>
                    <goals>
                        <goal>copy</goal>
                    </goals>
                    <configuration>
                        <artifactItems>
                            <artifactItem>
                                <groupId>io.github.nchaugen.tabletest</groupId>
                                <artifactId>tabletest-formatter-cli</artifactId>
                                <version>0.1.0</version>
                                <classifier>shaded</classifier>
                                <type>jar</type>
                                <outputDirectory>${project.build.directory}/formatter</outputDirectory>
                            </artifactItem>
                        </artifactItems>
                    </configuration>
                </execution>
            </executions>
        </plugin>

        <!-- Format @TableTest tables automatically -->
        <plugin>
            <groupId>org.codehaus.mojo</groupId>
            <artifactId>exec-maven-plugin</artifactId>
            <version>3.5.0</version>
            <executions>
                <execution>
                    <id>format-tabletest-tables</id>
                    <phase>process-test-classes</phase>
                    <goals>
                        <goal>exec</goal>
                    </goals>
                    <configuration>
                        <executable>java</executable>
                        <arguments>
                            <argument>-jar</argument>
                            <argument>${project.build.directory}/formatter/tabletest-formatter-cli-0.1.0-shaded.jar</argument>
                            <argument>${project.basedir}/src/test/java</argument>
                        </arguments>
                    </configuration>
                </execution>
            </executions>
        </plugin>
    </plugins>
</build>
```

#### Configuration Options

**Build phase**: Choose when formatting runs:
- `process-test-classes` - After test compilation (recommended for apply mode)
- `verify` - During verification phase
- `validate` - Early in the build (good for check mode in CI)

**Format multiple directories**: Add multiple paths as separate arguments:
```xml
<arguments>
    <argument>-jar</argument>
    <argument>${project.build.directory}/formatter/tabletest-formatter-cli-0.1.0-shaded.jar</argument>
    <argument>${project.basedir}/src/test/java</argument>
    <argument>${project.basedir}/src/main/java</argument>
    <argument>${project.basedir}/src/test/resources</argument>
</arguments>
```

**Custom indentation**: Add configuration arguments:
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

**Check mode** (fail build if formatting needed): Add `--check` argument:
```xml
<arguments>
    <argument>-jar</argument>
    <argument>${project.build.directory}/formatter/tabletest-formatter-cli-0.1.0-shaded.jar</argument>
    <argument>--check</argument>
    <argument>${project.basedir}/src/test/java</argument>
</arguments>
```

#### CI Integration

For CI pipelines, use check mode in a separate execution that runs during validation:

```xml
<execution>
    <id>check-tabletest-formatting</id>
    <phase>validate</phase>
    <goals>
        <goal>exec</goal>
    </goals>
    <configuration>
        <executable>java</executable>
        <arguments>
            <argument>-jar</argument>
            <argument>${project.build.directory}/formatter/tabletest-formatter-cli-0.1.0-shaded.jar</argument>
            <argument>--check</argument>
            <argument>${project.basedir}/src/test/java</argument>
        </arguments>
    </configuration>
</execution>
```

This will fail the build if any files need formatting, helping enforce consistent formatting in your CI pipeline.

### Command-Line Interface

The tabletest-formatter-cli module provides a standalone command-line tool for formatting TableTest tables.

#### Installation

Build the CLI tool:

```bash
mvn clean install
```

The executable JAR will be located at:
```
tabletest-formatter-cli/target/tabletest-formatter-cli-0.1.0.jar
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
| `--indent-size <N>` | Number of indent characters to add (spaces or tabs) | `4` |
| `--indent-type <type>` | Type of indentation to use: `space` or `tab` | `space` |
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
java -jar tabletest-formatter-cli.jar --indent-size 2 --indent-type tab src/
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

## Contributing

Contributions are welcome! See [CONTRIBUTING.md](CONTRIBUTING.md) for:
- Project structure and architecture
- Build and test instructions
- Development workflow and guidelines
- Code formatting standards

## License

Licensed under the Apache Licence, Version 2.0. See [LICENSE](LICENSE) for details.
