package io.github.nchaugen.tabletest.formatter.core;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TableTestFormatterTest {

    private final TableTestFormatter formatter = new TableTestFormatter();

    @Test
    void shouldFormatTableWithColumnAlignment() {
        var input = """
                name|age
                Alice|30
                Bob|25
                """;

        var result = formatter.format(input);

        assertThat(result).isEqualTo("""
                name  | age
                Alice | 30
                Bob   | 25
                """);
    }

    @Test
    void shouldFormatTableWithVaryingColumnWidths() {
        var input = """
                a|bb|ccc
                dddd|e|ff
                """;

        var result = formatter.format(input);

        assertThat(result).isEqualTo("""
                a    | bb | ccc
                dddd | e  | ff
                """);
    }

    @Test
    void shouldFormatTableWithEmptyCells() {
        var input = """
                a|b|c
                1||3
                """;

        var result = formatter.format(input);

        assertThat(result).isEqualTo("""
                a | b | c
                1 |   | 3
                """);
    }

    @Test
    void shouldCalculateColumnWidthsBasedOnWidestCell() {
        var input = """
                a|bb|ccc
                dddd|e|ff
                """;

        int[] widths = formatter.calculateColumnWidths(input);

        assertThat(widths).containsExactly(4, 2, 3);
    }

    @Test
    void shouldCalculateColumnWidthsIncludingEmptyCells() {
        var input = """
                name|value
                |longest
                """;

        int[] widths = formatter.calculateColumnWidths(input);

        assertThat(widths).containsExactly(4, 7);
    }
}
