package io.github.nchaugen.tabletest.formatter.spotless;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class TableTestFormatterFuncTest {

    private final TableTestFormatterState state = new TableTestFormatterState();
    private final TableTestFormatterFunc formatter = new TableTestFormatterFunc(state);

    @Test
    void shouldFormatStandaloneTableFile(@TempDir Path tempDir) throws Exception {
        File file = tempDir.resolve("test.table").toFile();
        String input = """
                name|age|city
                Alice|30|Oslo
                Bob|25|Bergen
                """;

        String result = formatter.apply(input, file);

        assertThat(result).isNotNull();
        assertThat(result).contains("name  | age | city");
        assertThat(result).contains("Alice | 30  | Oslo");
        assertThat(result).contains("Bob   | 25  | Bergen");
    }

    @Test
    void shouldFormatTableInJavaFile(@TempDir Path tempDir) throws Exception {
        File file = tempDir.resolve("Test.java").toFile();
        String input = """
                class Test {
                    @TableTest(\"""
                        name|age
                        Alice|30
                        \""")
                    void test() {}
                }
                """;

        String result = formatter.apply(input, file);

        assertThat(result).isNotNull();
        assertThat(result).contains("name  | age");
        assertThat(result).contains("Alice | 30");
    }

    @Test
    void shouldFormatTableInKotlinFile(@TempDir Path tempDir) throws Exception {
        File file = tempDir.resolve("Test.kt").toFile();
        String input = """
                class Test {
                    @TableTest(\"""
                        x|y
                        1|2
                        \""")
                    fun test() {}
                }
                """;

        String result = formatter.apply(input, file);

        assertThat(result).isNotNull();
        assertThat(result).contains("x | y");
        assertThat(result).contains("1 | 2");
    }

    @Test
    void shouldHandleMultipleTablesInOneFile(@TempDir Path tempDir) throws Exception {
        File file = tempDir.resolve("Test.java").toFile();
        String input = """
                class Test {
                    @TableTest(\"""
                        a|b
                        1|2
                        \""")
                    void test1() {}

                    @TableTest(\"""
                        x|y
                        5|6
                        \""")
                    void test2() {}
                }
                """;

        String result = formatter.apply(input, file);

        assertThat(result).isNotNull();
        assertThat(result).contains("a | b");
        assertThat(result).contains("1 | 2");
        assertThat(result).contains("x | y");
        assertThat(result).contains("5 | 6");
    }

    @Test
    void shouldReturnNullWhenNoChangesNeeded(@TempDir Path tempDir) throws Exception {
        File file = tempDir.resolve("test.table").toFile();
        String input = """
                name  | age | city
                Alice | 30  | Oslo
                Bob   | 25  | Bergen
                """;

        String result = formatter.apply(input, file);

        assertThat(result).isNull();
    }

    @Test
    void shouldIgnoreNonTableTestFiles(@TempDir Path tempDir) throws Exception {
        File file = tempDir.resolve("readme.txt").toFile();
        String input = "Some random text content";

        String result = formatter.apply(input, file);

        assertThat(result).isEqualTo(input);
    }

    @Test
    void shouldHandleValueParameterInAnnotation(@TempDir Path tempDir) throws Exception {
        File file = tempDir.resolve("Test.java").toFile();
        String input = """
                class Test {
                    @TableTest(value = \"""
                        a|b
                        1|2
                        \""")
                    void test() {}
                }
                """;

        String result = formatter.apply(input, file);

        assertThat(result).isNotNull();
        assertThat(result).contains("a | b");
        assertThat(result).contains("1 | 2");
    }

    @Test
    void shouldHandleMultipleParametersInAnnotation(@TempDir Path tempDir) throws Exception {
        File file = tempDir.resolve("Test.java").toFile();
        String input = """
                class Test {
                    @TableTest(resource="data.csv", value=\"""
                        a|b
                        1|2
                        \""", encoding="UTF-8")
                    void test() {}
                }
                """;

        String result = formatter.apply(input, file);

        assertThat(result).isNotNull();
        assertThat(result).contains("a | b");
        assertThat(result).contains("1 | 2");
    }
}
