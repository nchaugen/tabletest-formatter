> [!IMPORTANT]
> TableTest Reporter has new Maven coordinates: `org.tabletest:tabletest-reporter-*:1.0.0`
>
> Please update your dependencies to keep receiving updates.

# TableTest Formatter

A formatter for [TableTest](https://github.com/nchaugen/tabletest) tables that ensures consistent, readable formatting across your test suite.

**Formats tables in:**
- Java files with `@TableTest` annotations
- Kotlin files with `@TableTest` annotations
- Standalone `.table` files

**Available as:**
- **Spotless plugin** for Gradle (automatic formatting in your build)
- **CLI tool** for standalone formatting or CI integration
- **Maven integration** via exec-maven-plugin

## Key Features

- **Consistent, readable tables** – Vertically aligned columns and rows with normalized spacing
- **Context-aware** – Works in standalone `.table` files, Java text blocks, and Kotlin raw strings
- **Smart collection formatting** – Normalizes spacing in lists, sets, and maps while preserving your quote choices
- **Preserves structure** – Comments and blank lines maintained exactly as written
- **Unicode support** – Accurate width calculation for CJK characters, emojis, and special characters
- **Safe by default** – Never breaks builds; returns input unchanged on parse errors
- **Flexible integration** – Spotless plugin (Gradle), CLI tool, or Maven exec-maven-plugin

See [FEATURES.md](FEATURES.md) for comprehensive feature documentation.

## Table of Contents

- [What is TableTest Format?](#what-is-tabletest-format)
- [Formatting Behavior](#formatting-behavior)
  - [Formatting Rules](#formatting-rules)
  - [Configuration Options](#configuration-options)
- [Integration](#integration)
  - [Gradle (Spotless)](#gradle-spotless)
  - [Maven (exec-maven-plugin)](#maven-exec-maven-plugin)
  - [CLI / Manual](#cli--manual)
- [Requirements](#requirements)
- [Contributing](#contributing)
- [License](#license)

## What is TableTest Format?

TableTest uses a pipe-delimited table format for data-driven testing. Tables can appear in Java files (as multiline string parameters to `@TableTest` annotation), Kotlin files (as multiline string parameters to `@TableTest` annotation), or standalone `.table` files.

**Basic example:**

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
- If no `.editorconfig` is found, defaults to 4 spaces
- Base indentation from source files is always preserved

**Indent behavior:**
- `indent_size = 0` – Tables align exactly with their `@TableTest` annotation
- `indent_size = N` – Adds N indent characters (spaces or tabs) beyond the base level

## Integration

### Gradle (Spotless)

The tabletest-formatter-spotless module integrates with [Spotless](https://github.com/diffplug/spotless) to automatically format TableTest tables in your Gradle projects.

#### Installation

Add the formatter dependency to your `build.gradle`:

```groovy
buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath 'org.tabletest:tabletest-formatter-spotless:1.0.0'
    }
}
```

#### Setup

```groovy
import org.tabletest.formatter.spotless.TableTestFormatterStep

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

**Configuration:**

Formatting settings are read from `.editorconfig` files in your project. Create a `.editorconfig` file in your project root:

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

The formatter automatically finds and uses the applicable `.editorconfig` settings for each file being formatted.

#### Usage

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

### Maven (exec-maven-plugin)

Maven projects can use the CLI tool via [exec-maven-plugin](https://www.mojohaus.org/exec-maven-plugin/) to automatically format tables during the build. This provides build-integrated formatting until official Spotless Maven support is available.

**Note:** Official Spotless Maven integration requires contributing the formatter to the Spotless project itself, as the Spotless Maven plugin doesn't support programmatic custom steps.

#### Installation

Add the formatter CLI as a build dependency in your `pom.xml`:

```xml
<build>
    <plugins>
        <!-- Download the formatter CLI -->
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
                                <groupId>org.tabletest</groupId>
                                <artifactId>tabletest-formatter-cli</artifactId>
                                <version>1.0.0</version>
                                <classifier>shaded</classifier>
                                <type>jar</type>
                                <outputDirectory>${project.build.directory}/formatter</outputDirectory>
                            </artifactItem>
                        </artifactItems>
                    </configuration>
                </execution>
            </executions>
        </plugin>
    </plugins>
</build>
```

#### Setup

Add exec-maven-plugin to run the formatter:

```xml
<plugin>
    <groupId>org.codehaus.mojo</groupId>
    <artifactId>exec-maven-plugin</artifactId>
    <version>3.6.3</version>
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
                    <argument>${project.build.directory}/formatter/tabletest-formatter-cli-1.0.0-shaded.jar</argument>
                    <argument>${project.basedir}/src/test/java</argument>
                </arguments>
            </configuration>
        </execution>
    </executions>
</plugin>
```

**Configuration:**

Formatting settings are read from `.editorconfig` files. Create a `.editorconfig` in your project root:

```ini
[*.java]
indent_style = space
indent_size = 4

[*.table]
indent_style = space
indent_size = 0
```

**Options:**

**Build phase** – Choose when formatting runs:
- `process-test-classes` – After test compilation (recommended for apply mode)
- `verify` – During verification phase
- `validate` – Early in the build (good for check mode in CI)

**Multiple directories** – Add multiple paths as separate arguments:
```xml
<arguments>
    <argument>-jar</argument>
    <argument>${project.build.directory}/formatter/tabletest-formatter-cli-1.0.0-shaded.jar</argument>
    <argument>${project.basedir}/src/test/java</argument>
    <argument>${project.basedir}/src/main/java</argument>
    <argument>${project.basedir}/src/test/resources</argument>
</arguments>
```

**Check mode** – Fail build if formatting needed (add `--check` argument):
```xml
<arguments>
    <argument>-jar</argument>
    <argument>${project.build.directory}/formatter/tabletest-formatter-cli-1.0.0-shaded.jar</argument>
    <argument>--check</argument>
    <argument>${project.basedir}/src/test/java</argument>
</arguments>
```

#### Usage

```bash
# Apply formatting (automatic during build)
mvn clean install

# Check formatting only (with --check configuration)
mvn validate
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
            <argument>${project.build.directory}/formatter/tabletest-formatter-cli-1.0.0-shaded.jar</argument>
            <argument>--check</argument>
            <argument>${project.basedir}/src/test/java</argument>
        </arguments>
    </configuration>
</execution>
```

This will fail the build if any files need formatting, helping enforce consistent formatting in your CI pipeline.

### CLI / Manual

The tabletest-formatter-cli module provides a standalone command-line tool for formatting TableTest tables.

#### Installation

**Option 1: Download from Maven Central**

Java developers can fetch the CLI JAR using Maven:

```bash
mvn dependency:get \
  -Dartifact=org.tabletest:tabletest-formatter-cli:1.0.0:jar:shaded \
  -Dtransitive=false
```

The JAR will be downloaded to your local Maven repository:
```
~/.m2/repository/org/tabletest/tabletest-formatter-cli/1.0.0/
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
tabletest-formatter-cli/target/tabletest-formatter-cli-1.0.0.jar
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
- **Java:** 21 or later (required by tabletest-parser dependency)

**Tested with:**
- **Gradle:** 8.11.1
- **Maven:** 3.9.9
- **Spotless Gradle Plugin:** 8.1.0
- **exec-maven-plugin:** 3.6.3

**Theoretical minimums** (based on dependency requirements, not tested):
- **Gradle:** 8.1+ (required by Spotless 8.1.0) or 7.3+ (if using Spotless 8.0.0)
- **Maven:** 3.6.3+ (required by exec-maven-plugin 3.6.3)
- **Spotless Gradle Plugin:** 8.0.0+ (requires Java 17+, satisfied by our Java 21 requirement)

The formatter runs on any platform with Java 21+: Linux, macOS, Windows.

## Contributing

Contributions are welcome! See [CONTRIBUTING.md](CONTRIBUTING.md) for:
- Project structure and architecture
- Build and test instructions
- Development workflow and guidelines
- Code formatting standards

## License

Licensed under the Apache Licence, Version 2.0. See [LICENSE](LICENSE) for details.
