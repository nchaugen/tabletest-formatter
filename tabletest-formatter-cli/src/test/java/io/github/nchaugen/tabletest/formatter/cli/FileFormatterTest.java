package io.github.nchaugen.tabletest.formatter.cli;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class FileFormatterTest {

    private final FileFormatter formatter = new FileFormatter();

    @Test
    void shouldFormatStandaloneTableFile(@TempDir Path tempDir) throws IOException {
        Path tableFile = tempDir.resolve("test.table");
        String unformatted = """
                name|age|city
                Alice|30|London
                Bob|25|Paris
                """;
        Files.writeString(tableFile, unformatted);

        FormattingResult result = formatter.format(tableFile);

        assertThat(result.changed()).isTrue();
        assertThat(result.file()).isEqualTo(tableFile);
        assertThat(result.formattedContent()).isEqualTo("""
                        name  | age | city
                        Alice | 30  | London
                        Bob   | 25  | Paris
                        """);
    }

    @Test
    void shouldFormatJavaFileWithSingleTable(@TempDir Path tempDir) throws IOException {
        Path javaFile = tempDir.resolve("Test.java");
        String unformatted = """
                public class Test {
                    @TableTest(\"""
                    name|age
                    Alice|30
                    \""")
                    void test() {}
                }
                """;
        Files.writeString(javaFile, unformatted);

        FormattingResult result = formatter.format(javaFile);

        assertThat(result.changed()).isTrue();
        assertThat(result.formattedContent()).isEqualTo("""
                public class Test {
                    @TableTest(\"""
                name  | age
                Alice | 30
                    \""")
                    void test() {}
                }
                """);
    }

    @Test
    void shouldFormatJavaFileWithMultipleTables(@TempDir Path tempDir) throws IOException {
        Path javaFile = tempDir.resolve("Test.java");
        String unformatted = """
                public class Test {
                    @TableTest(\"""
                    name|age
                    Alice|30
                    \""")
                    void test1() {}

                    @TableTest(\"""
                    city|country
                    London|UK
                    \""")
                    void test2() {}
                }
                """;
        Files.writeString(javaFile, unformatted);

        FormattingResult result = formatter.format(javaFile);

        assertThat(result.changed()).isTrue();
        assertThat(result.formattedContent()).isEqualTo("""
                public class Test {
                    @TableTest(\"""
                name  | age
                Alice | 30
                    \""")
                    void test1() {}

                    @TableTest(\"""
                city   | country
                London | UK
                    \""")
                    void test2() {}
                }
                """);
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

        FormattingResult result = formatter.format(kotlinFile);

        assertThat(result.changed()).isTrue();
        assertThat(result.formattedContent()).isEqualTo("""
                class Test {
                    @TableTest(\"""
                name  | age
                Alice | 30
                    \""")
                    fun test() {}
                }
                """);
    }

    @Test
    void shouldReturnUnchangedWhenAlreadyFormatted(@TempDir Path tempDir) throws IOException {
        Path tableFile = tempDir.resolve("test.table");
        String alreadyFormatted = """
                name  | age | city
                Alice | 30  | London
                Bob   | 25  | Paris
                """;
        Files.writeString(tableFile, alreadyFormatted);

        FormattingResult result = formatter.format(tableFile);

        assertThat(result.changed()).isFalse();
        assertThat(result.formattedContent()).isEqualTo(alreadyFormatted);
    }

    @Test
    void shouldHandleFileWithNoTables(@TempDir Path tempDir) throws IOException {
        Path javaFile = tempDir.resolve("Test.java");
        String noTables = """
                public class Test {
                    void test() {
                        System.out.println("Hello");
                    }
                }
                """;
        Files.writeString(javaFile, noTables);

        FormattingResult result = formatter.format(javaFile);

        assertThat(result.changed()).isFalse();
        assertThat(result.formattedContent()).isEqualTo(noTables);
    }
}
