package io.github.nchaugen.tabletest.formatter.spotless;

import com.diffplug.spotless.FormatterStep;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for Spotless FormatterStep.
 * <p>
 * These tests verify that the formatter works correctly with real project files,
 * simulating how Spotless would use the formatter in a Maven/Gradle build.
 */
class SpotlessIntegrationTest {

    private static final String TEST_RESOURCES = "src/test/resources/integration-test-project";

    private final FormatterStep formatterStep = TableTestFormatterStep.create();

    @Test
    void shouldFormatJavaFileWithTableTestAnnotations() throws Exception {
        Path javaFile = Paths.get(TEST_RESOURCES, "src/test/java/com/example/CalculatorTest.java");
        String content = Files.readString(javaFile);

        String formatted = formatterStep.format(content, javaFile.toFile());

        // Verify tables are formatted with proper spacing
        assertThat(formatted).contains("a  | b  | sum");
        assertThat(formatted).contains("1  | 2  | 3");
        assertThat(formatted).contains("x | y | product");
        assertThat(formatted).contains("name    | age | valid");
        assertThat(formatted).contains("Alice   | 30  | true");
        assertThat(formatted).contains("Charlie | 25  | true");
    }

    @Test
    void shouldFormatKotlinFileWithTableTestAnnotations() throws Exception {
        Path kotlinFile = Paths.get(TEST_RESOURCES, "src/test/kotlin/com/example/StringUtilsTest.kt");
        String content = Files.readString(kotlinFile);

        String formatted = formatterStep.format(content, kotlinFile.toFile());

        // Verify tables are formatted with proper spacing
        assertThat(formatted).contains("input | expected");
        assertThat(formatted).contains("hello | HELLO");
        assertThat(formatted).contains("text   | reversed");
        assertThat(formatted).contains("kotlin | niltok");
    }

    @Test
    void shouldFormatStandaloneTableFile() throws Exception {
        Path tableFile = Paths.get(TEST_RESOURCES, "src/test/resources/users.table");
        String content = Files.readString(tableFile);

        String formatted = formatterStep.format(content, tableFile.toFile());

        // Verify table is formatted with proper spacing
        assertThat(formatted).contains("name    | age | city");
        assertThat(formatted).contains("Alice   | 30  | Oslo");
        assertThat(formatted).contains("Bob     | 25  | Bergen");
        assertThat(formatted).contains("Charlie | 35  | Trondheim");
    }

    @Test
    void shouldReturnNullForAlreadyFormattedFile() throws Exception {
        Path tableFile = Paths.get(TEST_RESOURCES, "src/test/resources/products.table");
        String content = Files.readString(tableFile);

        String formatted = formatterStep.format(content, tableFile.toFile());

        // Verify no changes needed (returns null per Spotless contract)
        assertThat(formatted).isNull();
    }

    @Test
    void shouldHandleMultipleAnnotationsInOneFile() throws Exception {
        Path javaFile = Paths.get(TEST_RESOURCES, "src/test/java/com/example/CalculatorTest.java");
        String content = Files.readString(javaFile);

        String formatted = formatterStep.format(content, javaFile.toFile());

        // Count occurrences of formatted table headers
        long headerCount = formatted
                .lines()
                .filter(line -> line.contains(" | ") && !line.trim().startsWith("//"))
                .count();

        // Should have at least 3 tables (testAddition, testMultiplication, testAgeValidation)
        assertThat(headerCount).isGreaterThanOrEqualTo(3);
    }

    @Test
    void shouldPreserveFileStructure() throws Exception {
        Path javaFile = Paths.get(TEST_RESOURCES, "src/test/java/com/example/CalculatorTest.java");
        String content = Files.readString(javaFile);

        String formatted = formatterStep.format(content, javaFile.toFile());

        // Verify package, imports, and class structure preserved
        assertThat(formatted).contains("package com.example;");
        assertThat(formatted).contains("import org.tabletest.TableTest;");
        assertThat(formatted).contains("class CalculatorTest {");
        assertThat(formatted).contains("void testAddition(");
        assertThat(formatted).contains("void testMultiplication(");
        assertThat(formatted).contains("void testAgeValidation(");
    }
}
