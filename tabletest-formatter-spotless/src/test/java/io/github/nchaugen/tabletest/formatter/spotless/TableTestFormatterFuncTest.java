package io.github.nchaugen.tabletest.formatter.spotless;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests Spotless-specific behaviour of TableTestFormatterFunc.
 * Formatting correctness is tested in SourceFileFormatterTest.
 */
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
    void shouldStoreConfiguredTabSize() {
        TableTestFormatterState state2 = new TableTestFormatterState(2);
        assertThat(state2.tabSize()).isEqualTo(2);

        TableTestFormatterState state8 = new TableTestFormatterState(8);
        assertThat(state8.tabSize()).isEqualTo(8);
    }

    @Test
    void shouldUseDefaultTabSizeWhenNotSpecified() {
        TableTestFormatterState defaultState = new TableTestFormatterState();
        assertThat(defaultState.tabSize()).isEqualTo(4);
    }

    @Test
    void shouldPassTabSizeToFormatter(@TempDir Path tempDir) throws Exception {
        // Verify that the formatter can be created with custom tab size and formats successfully
        TableTestFormatterState customState = new TableTestFormatterState(2);
        TableTestFormatterFunc customFormatter = new TableTestFormatterFunc(customState);

        File file = tempDir.resolve("Test.java").toFile();
        String input = """
                class Test {
                    @TableTest(\"""
                    x|y
                    1|2
                    \""")
                    void test() {}
                }
                """;

        String result = customFormatter.apply(input, file);

        // Should format successfully with custom tab size
        assertThat(result).isNotNull();
        assertThat(result).contains("x | y");
        assertThat(result).contains("1 | 2");
    }
}
