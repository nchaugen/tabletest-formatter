package io.github.nchaugen.tabletest.formatter.cli;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import picocli.CommandLine;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * End-to-end integration tests for the CLI.
 * Tests actual file system operations and CLI execution.
 */
class CliIntegrationTest {

    private static final String UNFORMATTED_DIR = "cli-test-data/unformatted";
    private static final String FORMATTED_DIR = "cli-test-data/formatted";

    @Test
    void shouldFormatUnformattedFiles(@TempDir Path tempDir) throws IOException, URISyntaxException {
        // Given: unformatted file in temp directory
        Path testFile = tempDir.resolve("SimpleTest.java");
        copyUnformattedFile(testFile);

        // When: applying formatting
        int exitCode = executeCliApplyMode(tempDir);

        // Then: file is formatted and exit code is 0
        assertThat(exitCode).isZero();
        assertThat(actualContent(testFile)).isEqualTo(expectedContent(testFile));
    }

    @Test
    void shouldDetectAlreadyFormattedFiles(@TempDir Path tempDir) throws IOException, URISyntaxException {
        // Given: already formatted file
        Path testFile = tempDir.resolve("SimpleTest.java");
        copyFormattedFile(testFile);

        // When: running in check mode
        int exitCode = executeCliCheckMode(tempDir);

        // Then: exit code is 0 (no changes needed)
        assertThat(exitCode).isZero();

        // And: file is unchanged
        assertThat(actualContent(testFile)).isEqualTo(expectedContent(testFile));
    }

    @Test
    void shouldDetectUnformattedFilesInCheckMode(@TempDir Path tempDir) throws IOException, URISyntaxException {
        // Given: unformatted file
        Path testFile = tempDir.resolve("SimpleTest.java");
        copyUnformattedFile(testFile);
        String originalContent = actualContent(testFile);

        // When: running in check mode
        int exitCode = executeCliCheckMode(tempDir);

        // Then: exit code is 1 (changes needed)
        assertThat(exitCode).isEqualTo(1);

        // And: file is unchanged
        assertThat(actualContent(testFile)).isEqualTo(originalContent);
    }

    @Test
    void shouldHandleMultipleTablesInOneFile(@TempDir Path tempDir) throws IOException, URISyntaxException {
        // Given: Kotlin file with multiple @TableTest annotations
        Path testFile = tempDir.resolve("MultiTableTest.kt");
        copyUnformattedFile(testFile);

        // When: applying formatting
        int exitCode = executeCliApplyMode(tempDir);

        // Then: all tables are formatted
        assertThat(exitCode).isZero();
        assertThat(actualContent(testFile)).isEqualTo(expectedContent(testFile));
    }

    @Test
    void shouldFormatMixedFileTypes(@TempDir Path tempDir) throws IOException, URISyntaxException {
        // Given: directory with .java, .kt, and .table files
        Path javaFile = tempDir.resolve("SimpleTest.java");
        Path kotlinFile = tempDir.resolve("MultiTableTest.kt");
        Path tableFile = tempDir.resolve("data.table");
        copyUnformattedFile(javaFile);
        copyUnformattedFile(kotlinFile);
        copyUnformattedFile(tableFile);

        // When: formatting directory
        int exitCode = executeCliApplyMode(tempDir);

        // Then: all files are formatted
        assertThat(exitCode).isZero();
        assertThat(actualContent(javaFile)).isEqualTo(expectedContent(javaFile));
        assertThat(actualContent(kotlinFile)).isEqualTo(expectedContent(kotlinFile));
        assertThat(actualContent(tableFile)).isEqualTo(expectedContent(tableFile));
    }

    @Test
    void shouldHandleNestedDirectories(@TempDir Path tempDir) throws IOException, URISyntaxException {
        // Given: nested directory structure
        Path subDir = tempDir.resolve("src/test/java");
        Files.createDirectories(subDir);
        Path nestedFile = subDir.resolve("SimpleTest.java");
        Path rootFile = tempDir.resolve("data.table");
        copyUnformattedFile(nestedFile);
        copyUnformattedFile(rootFile);

        // When: formatting root directory
        int exitCode = executeCliApplyMode(tempDir);

        // Then: files in all subdirectories are formatted
        assertThat(exitCode).isZero();
        assertThat(actualContent(nestedFile)).isEqualTo(expectedContent(nestedFile));
        assertThat(actualContent(rootFile)).isEqualTo(expectedContent(rootFile));
    }

    @Test
    void shouldHandleDeepIndentation(@TempDir Path tempDir) throws IOException, URISyntaxException {
        // Given: file with deeply nested classes (12+ spaces)
        Path testFile = tempDir.resolve("DeepIndentationTest.java");
        copyUnformattedFile(testFile);

        // When: applying formatting
        int exitCode = executeCliApplyMode(tempDir);

        // Then: file is formatted with correct deep indentation
        assertThat(exitCode).isZero();
        assertThat(actualContent(testFile)).isEqualTo(expectedContent(testFile));
    }

    @Test
    void shouldHandleTabIndentation(@TempDir Path tempDir) throws IOException, URISyntaxException {
        // Given: file with tab-based indentation
        Path testFile = tempDir.resolve("TabIndentationTest.java");
        copyUnformattedFile(testFile);

        // When: applying formatting
        int exitCode = executeCliApplyMode(tempDir);

        // Then: tabs preserved in base indentation, spaces added as indent characters
        assertThat(exitCode).isZero();
        assertThat(actualContent(testFile)).isEqualTo(expectedContent(testFile));
    }

    @Test
    void shouldHandleShallowIndentation(@TempDir Path tempDir) throws IOException, URISyntaxException {
        // Given: file with 2-space indentation
        Path testFile = tempDir.resolve("ShallowIndentationTest.java");
        copyUnformattedFile(testFile);

        // When: applying formatting
        int exitCode = executeCliApplyMode(tempDir);

        // Then: file is formatted with 2-space base indentation
        assertThat(exitCode).isZero();
        assertThat(actualContent(testFile)).isEqualTo(expectedContent(testFile));
    }

    @Test
    void shouldHandleVariedWhitespace(@TempDir Path tempDir) throws IOException, URISyntaxException {
        // Given: file with extra whitespace and quotes on separate lines
        Path testFile = tempDir.resolve("VariedWhitespaceTest.java");
        copyUnformattedFile(testFile);

        // When: applying formatting
        int exitCode = executeCliApplyMode(tempDir);

        // Then: file is formatted while preserving whitespace variations
        assertThat(exitCode).isZero();
        assertThat(actualContent(testFile)).isEqualTo(expectedContent(testFile));
    }

    @Test
    void shouldHandleNamedParameters(@TempDir Path tempDir) throws IOException, URISyntaxException {
        // Given: file with named parameter syntax (value = ...)
        Path testFile = tempDir.resolve("NamedParameterTest.java");
        copyUnformattedFile(testFile);

        // When: applying formatting
        int exitCode = executeCliApplyMode(tempDir);

        // Then: file is formatted correctly with named parameters
        assertThat(exitCode).isZero();
        assertThat(actualContent(testFile)).isEqualTo(expectedContent(testFile));
    }

    @Test
    void shouldHandleNestedKotlinClasses(@TempDir Path tempDir) throws IOException, URISyntaxException {
        // Given: Kotlin file with nested classes
        Path testFile = tempDir.resolve("NestedClassTest.kt");
        copyUnformattedFile(testFile);

        // When: applying formatting
        int exitCode = executeCliApplyMode(tempDir);

        // Then: file is formatted with correct indentation for each nesting level
        assertThat(exitCode).isZero();
        assertThat(actualContent(testFile)).isEqualTo(expectedContent(testFile));
    }

    @Test
    void shouldRespectIndentSizeOverride(@TempDir Path tempDir) throws IOException, URISyntaxException {
        // Given: unformatted file
        Path testFile = tempDir.resolve("SimpleTest.java");
        copyUnformattedFile(testFile);

        // When: formatting with indent-size=2
        int exitCode = new CommandLine(new TableTestFormatterCli()).execute("--indent-size=2", testFile.toString());

        // Then: file is formatted with 2-space indentation
        assertThat(exitCode).isZero();
        String content = actualContent(testFile);

        // Verify indentation is 2 spaces (baseIndent=4 + indentSize=2 = 6 total)
        assertThat(content).contains("      name  | age | city"); // 6 spaces
        assertThat(content).contains("      Alice | 30  | New York");
        assertThat(content).contains("      \"\"\""); // Closing quote also at 6 spaces
    }

    private int executeCliApplyMode(Path path) {
        return new CommandLine(new TableTestFormatterCli()).execute(path.toString());
    }

    private int executeCliCheckMode(Path path) {
        return new CommandLine(new TableTestFormatterCli()).execute("--check", path.toString());
    }

    private String expectedContent(Path file) throws URISyntaxException, IOException {
        return readTestFile(FORMATTED_DIR, file.getFileName().toString());
    }

    private void copyUnformattedFile(Path destination) throws IOException, URISyntaxException {
        String fileName = destination.getFileName().toString();
        copyTestFile(UNFORMATTED_DIR, fileName, destination);
    }

    private void copyFormattedFile(Path destination) throws IOException, URISyntaxException {
        String fileName = destination.getFileName().toString();
        copyTestFile(FORMATTED_DIR, fileName, destination);
    }

    private String actualContent(Path file) throws IOException {
        return Files.readString(file);
    }

    private void copyTestFile(String sourceDir, String fileName, Path destination)
            throws IOException, URISyntaxException {
        String content = readTestFile(sourceDir, fileName);
        Files.writeString(destination, content);
    }

    private String readTestFile(String directory, String fileName) throws URISyntaxException, IOException {
        Path resourcePath =
                Paths.get(Objects.requireNonNull(getClass().getClassLoader().getResource(directory + "/" + fileName))
                        .toURI());
        return Files.readString(resourcePath);
    }
}
