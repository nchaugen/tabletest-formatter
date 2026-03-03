# Spotless Integration Samples

Standalone sample projects that demonstrate and verify the official Spotless integration
for the TableTest Formatter.

These projects use the **native Spotless DSL** (`tableTestFormatter()` / `<tableTestFormatter>`)
— no dependency on `tabletest-formatter-spotless` is required.

## Samples

| Sample | Spotless version |
|--------|-----------------|
| [`gradle-sample/`](gradle-sample/) | Gradle plugin 8.3.0 |
| [`maven-sample/`](maven-sample/) | Maven plugin 3.3.0 |

Both samples include Java and Kotlin test files with `@TableTest` annotations.

> **Note:** The official Spotless integration supports `.java` and `.kt` files only.
> Standalone `.table` file support is not yet included in Spotless.

## Prerequisites

- Java 21+
- For Gradle sample: Gradle 8.x (or use the wrapper — see below)
- For Maven sample: Maven 3.6+

## Running the Gradle sample

```bash
cd gradle-sample

# Generate Gradle wrapper (one-time)
gradle wrapper --gradle-version 8.12

# Verify already-formatted files are recognised as correct (idempotency)
./gradlew spotlessCheck

# To test the full format cycle: introduce violations, then apply
./reset-for-demo.sh
./gradlew spotlessCheck    # FAILS — violations detected
./gradlew spotlessApply    # formats the tables
./gradlew spotlessCheck    # PASSES — idempotent
```

## Running the Maven sample

```bash
cd maven-sample

# Verify already-formatted files are recognised as correct (idempotency)
mvn spotless:check

# To test the full format cycle: introduce violations, then apply
./reset-for-demo.sh
mvn spotless:check         # FAILS — violations detected
mvn spotless:apply         # formats the tables
mvn spotless:check         # PASSES — idempotent
```

## What is being tested

1. **Idempotency** — `spotlessCheck` passes on correctly-formatted files
2. **Violation detection** — `spotlessCheck` fails when tables are unformatted
3. **Formatting** — `spotlessApply` aligns table columns correctly
4. Both **Java** and **Kotlin** source files are handled
