package io.github.nchaugen.tabletest.formatter.cli;

import io.github.nchaugen.tabletest.formatter.config.IndentType;
import io.github.nchaugen.tabletest.formatter.config.StaticConfigProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class FileFormatterTest {

    private final FileFormatter formatter = new FileFormatter();

    @Test
    void shouldFormatTableFile(@TempDir Path tempDir) throws IOException {
        Path tableFile = tempDir.resolve("test.table");
        String unformatted = """
                name|age
                Alice|30
                """;
        Files.writeString(tableFile, unformatted);

        FormattingResult result = formatter.format(tableFile, StaticConfigProvider.NO_INDENT);

        assertThat(result.changed()).isTrue();
        assertThat(result.file()).isEqualTo(tableFile);
        assertThat(result.formattedContent()).contains("name  | age");
        assertThat(result.formattedContent()).contains("Alice | 30");
    }

    @Test
    void shouldFormatJavaFile(@TempDir Path tempDir) throws IOException {
        Path javaFile = tempDir.resolve("Test.java");
        String unformatted = """
                class Test {
                    @TableTest(\"""
                    name|age
                    Alice|30
                    \""")
                    void test() {}
                }
                """;
        Files.writeString(javaFile, unformatted);

        FormattingResult result = formatter.format(javaFile, StaticConfigProvider.DEFAULT);

        assertThat(result.changed()).isTrue();
        assertThat(result.file()).isEqualTo(javaFile);
        assertThat(result.formattedContent()).contains("name  | age");
        assertThat(result.formattedContent()).contains("Alice | 30");
    }

    @Test
    void shouldFormatKotlinFile(@TempDir Path tempDir) throws IOException {
        Path kotlinFile = tempDir.resolve("Test.kt");
        String unformatted = """
                class Test {
                    @TableTest(\"""
                    name|age
                    Alice|30
                    \""")
                    fun test() {}
                }
                """;
        Files.writeString(kotlinFile, unformatted);

        FormattingResult result = formatter.format(kotlinFile, StaticConfigProvider.DEFAULT);

        assertThat(result.changed()).isTrue();
        assertThat(result.file()).isEqualTo(kotlinFile);
        assertThat(result.formattedContent()).contains("name  | age");
        assertThat(result.formattedContent()).contains("Alice | 30");
    }

    @Test
    void shouldReturnUnchangedWhenAlreadyFormatted(@TempDir Path tempDir) throws IOException {
        Path tableFile = tempDir.resolve("test.table");
        String alreadyFormatted = """
                name  | age
                Alice | 30
                """;
        Files.writeString(tableFile, alreadyFormatted);

        FormattingResult result = formatter.format(tableFile, StaticConfigProvider.NO_INDENT);

        assertThat(result.changed()).isFalse();
        assertThat(result.formattedContent()).isEqualTo(alreadyFormatted);
    }

    @Test
    void shouldHandleFileWithNoTables(@TempDir Path tempDir) throws IOException {
        Path javaFile = tempDir.resolve("Test.java");
        String noTables = """
                class Test {
                    void test() {
                        System.out.println("Hello");
                    }
                }
                """;
        Files.writeString(javaFile, noTables);

        FormattingResult result = formatter.format(javaFile, StaticConfigProvider.DEFAULT);

        assertThat(result.changed()).isFalse();
        assertThat(result.formattedContent()).isEqualTo(noTables);
    }

    @Test
    void shouldReturnUnchangedForUnsupportedFileType(@TempDir Path tempDir) throws IOException {
        Path textFile = tempDir.resolve("readme.txt");
        String content = """
                name|age
                This text file contains pipes | but should not be formatted
                """;
        Files.writeString(textFile, content);

        FormattingResult result = formatter.format(textFile, StaticConfigProvider.NO_INDENT);

        assertThat(result.changed()).isFalse();
        assertThat(result.file()).isEqualTo(textFile);
        assertThat(result.formattedContent()).isEqualTo(content);
    }

    @Test
    void shouldApplyIndentationToSourceFiles(@TempDir Path tempDir) throws IOException {
        Path javaFile = tempDir.resolve("Test.java");
        String unformatted = """
                class Test {
                    @TableTest(\"""
                    name|age
                    Alice|30
                    \""")
                    void test() {}
                }
                """;
        Files.writeString(javaFile, unformatted);

        FormattingResult result = formatter.format(javaFile, new StaticConfigProvider(IndentType.SPACE, 2));

        assertThat(result.changed()).isTrue();
        assertThat(result.file()).isEqualTo(javaFile);
        assertThat(result.formattedContent()).isEqualTo("""
                class Test {
                    @TableTest(\"""
                      name  | age
                      Alice | 30
                      \""")
                    void test() {}
                }
                """);
    }
}
