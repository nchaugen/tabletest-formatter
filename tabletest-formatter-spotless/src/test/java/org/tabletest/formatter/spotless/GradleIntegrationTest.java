package org.tabletest.formatter.spotless;

import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.gradle.testkit.runner.TaskOutcome.SUCCESS;

/**
 * Integration tests for Gradle integration using Gradle TestKit.
 * <p>
 * These tests verify that the TableTest formatter works correctly when
 * invoked through Spotless in a Gradle build.
 */
class GradleIntegrationTest {

    private static final String TEST_PROJECT = "src/test/resources/integration-test-project";

    @TempDir
    Path testProjectDir;

    Path testKitDir;

    @BeforeEach
    void setUp() throws IOException {
        testKitDir = createTestKitDir();
        copyTestProject();
    }

    @Test
    void shouldFormatFilesWithSpotlessApply() throws IOException {
        BuildResult result = runGradle("spotlessApply");

        assertThat(result.task(":spotlessApply")).isNotNull().satisfies(task -> assertThat(task.getOutcome())
                .isEqualTo(SUCCESS));

        // Verify Java file is formatted
        Path javaFile = testProjectDir.resolve("src/test/java/com/example/CalculatorTest.java");
        String javaContent = Files.readString(javaFile);
        assertThat(javaContent).contains("a  | b  | sum");
        assertThat(javaContent).contains("1  | 2  | 3");

        // Verify Kotlin file is formatted
        Path kotlinFile = testProjectDir.resolve("src/test/kotlin/com/example/StringUtilsTest.kt");
        String kotlinContent = Files.readString(kotlinFile);
        assertThat(kotlinContent).contains("input | expected");
        assertThat(kotlinContent).contains("hello | HELLO");

        // Verify .table file is formatted
        Path tableFile = testProjectDir.resolve("src/test/resources/users.table");
        String tableContent = Files.readString(tableFile);
        assertThat(tableContent).contains("name    | age | city");
        assertThat(tableContent).contains("Alice   | 30  | Oslo");
    }

    @Test
    void shouldPassSpotlessCheckForAlreadyFormattedFiles() {
        // First format the files
        runGradle("spotlessApply");

        // Then verify spotlessCheck passes
        BuildResult result = runGradle("spotlessCheck");

        assertThat(result.task(":spotlessCheck")).isNotNull().satisfies(task -> assertThat(task.getOutcome())
                .isEqualTo(SUCCESS));
    }

    @Test
    void shouldFormatMultipleTablesInOneFile() throws IOException {
        runGradle("spotlessApply");

        Path javaFile = testProjectDir.resolve("src/test/java/com/example/CalculatorTest.java");
        String content = Files.readString(javaFile);

        // Verify all three tables are formatted
        assertThat(content).contains("a  | b  | sum"); // testAddition
        assertThat(content).contains("x | y | product"); // testMultiplication
        assertThat(content).contains("name    | age | valid"); // testAgeValidation
    }

    @Test
    void shouldHandleUnicodeCharactersCorrectly() throws IOException {
        // Create a test file with Unicode content
        Path unicodeFile = testProjectDir.resolve("src/test/resources/unicode.table");
        Files.writeString(unicodeFile, """
            name|greeting
            世界|Hello World
            日本|こんにちは
            """);

        runGradle("spotlessApply");

        String content = Files.readString(unicodeFile);
        assertThat(content).contains("name | greeting");
        assertThat(content).contains("世界 | Hello World");
        assertThat(content).contains("日本 | こんにちは");
    }

    private BuildResult runGradle(String... tasks) {
        List<String> arguments = Stream.concat(Stream.of("-Dorg.gradle.daemon=false"), Stream.of(tasks))
                .toList();
        return GradleRunner.create()
                .withProjectDir(testProjectDir.toFile())
                .withTestKitDir(testKitDir.toFile())
                .withGradleVersion("8.11.1")
                .withArguments(arguments)
                .build();
    }

    private Path createTestKitDir() throws IOException {
        Path baseDir = Paths.get("").toAbsolutePath().resolve("target/gradle-testkit");
        Files.createDirectories(baseDir);
        return Files.createTempDirectory(baseDir, "testkit-");
    }

    private void copyTestProject() throws IOException {
        Path source = Paths.get(TEST_PROJECT);
        try (Stream<Path> stream = Files.walk(source)) {
            stream.forEach(sourcePath -> {
                try {
                    Path targetPath = testProjectDir.resolve(source.relativize(sourcePath));
                    if (Files.isDirectory(sourcePath)) {
                        Files.createDirectories(targetPath);
                    } else {
                        Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
                    }
                } catch (IOException e) {
                    throw new RuntimeException("Failed to copy test project", e);
                }
            });
        }
    }
}
