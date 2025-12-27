package io.nchaugen.tabletest.formatter.core;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TableTestFormatterTest {

    private final TableTestFormatter formatter = new TableTestFormatter();

    @Test
    void shouldParseAndRebuildSimpleTableWithoutFormatting() {
        var input = "a|b\n1|2";

        var result = formatter.format(input);

        assertThat(result).isEqualTo(input);
    }

    @Test
    void shouldHandleTableWithMultipleRows() {
        var input = "name|age\nAlice|30\nBob|25\nCarol|35";

        var result = formatter.format(input);

        assertThat(result).isEqualTo(input);
    }

    @Test
    void shouldHandleTableWithEmptyCell() {
        var input = "a|b|c\n1||3";

        var result = formatter.format(input);

        assertThat(result).isEqualTo(input);
    }

    @Test
    void shouldHandleTableWithMoreColumns() {
        var input = "col1|col2|col3|col4\nval1|val2|val3|val4";

        var result = formatter.format(input);

        assertThat(result).isEqualTo(input);
    }
}
