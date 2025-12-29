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
}
