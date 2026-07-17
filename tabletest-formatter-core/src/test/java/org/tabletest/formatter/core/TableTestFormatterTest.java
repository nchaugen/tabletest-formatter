package org.tabletest.formatter.core;

import org.junit.jupiter.api.Test;
import org.tabletest.formatter.config.Config;
import org.tabletest.formatter.config.IndentStyle;

import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TableTestFormatterTest {

    private final TableTestFormatter formatter = new TableTestFormatter();

    @Test
    void shouldFormatTableWithColumnAlignment() {
        var input = """
            name|age
            Alice|30
            Bob|25
            """;

        Objects.requireNonNull(input, "tableText must not be null");
        var result = formatter.format(input, "", Config.NO_INDENT);

        assertThat(result).isEqualTo("""
            name  | age
            Alice | 30
            Bob   | 25
            """);
    }

    @Test
    void shouldReturnUnchangedWhenMismatchedColumnCounts() {
        var input = """
            name|age
            Alice|30
            Bob|25|London
            """;

        Objects.requireNonNull(input, "tableText must not be null");
        var result = formatter.format(input, "", Config.NO_INDENT);

        assertThat(result).isEqualTo(input);
    }

    @Test
    void shouldReturnUnchangedWhenFewerColumnsInDataRow() {
        var input = """
            name|age|city
            Alice|30
            Bob|25|London
            """;

        Objects.requireNonNull(input, "tableText must not be null");
        var result = formatter.format(input, "", Config.NO_INDENT);

        assertThat(result).isEqualTo(input);
    }

    @Test
    void shouldReturnUnchangedWhenEmpty() {
        var input = "";

        Objects.requireNonNull(input, "tableText must not be null");
        var result = formatter.format(input, "", Config.NO_INDENT);

        assertThat(result).isEqualTo(input);
    }

    @Test
    void shouldReturnUnchangedWhenOnlyWhitespace() {
        var input = "   \n  \n   ";

        Objects.requireNonNull(input, "tableText must not be null");
        var result = formatter.format(input, "", Config.NO_INDENT);

        assertThat(result).isEqualTo(input);
    }

    @Test
    void shouldFormatHeaderOnlyTableWithoutIndentation() {
        var input = """
            name|age
            """;

        Objects.requireNonNull(input, "tableText must not be null");
        var result = formatter.format(input, "", Config.NO_INDENT);

        assertThat(result).isEqualTo("""
            name | age
            """);
    }

    @Test
    void shouldReturnUnchangedWhenEscapedQuotesInValue() {
        var input = """
            name|message
            test|"He said \\"hello\\""
            """;

        Objects.requireNonNull(input, "tableText must not be null");
        var result = formatter.format(input, "", Config.NO_INDENT);

        // Parser cannot handle escaped quotes, returns unchanged
        assertThat(result).isEqualTo(input);
    }

    @Test
    void shouldReturnEmptyTableUnchangedWithIndentation() {
        var input = "";

        var result = formatter.format(input, "", new Config(IndentStyle.SPACE, 4));

        assertThat(result).isEmpty();
    }

    // ========== Input Validation Tests ==========

    @Test
    void shouldThrowNullPointerExceptionWhenTableTextIsNull() {
        assertThatThrownBy(() -> formatter.format(null, "", Config.NO_INDENT))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("tableText must not be null");
    }

    @Test
    void shouldThrowNullPointerExceptionWhenBaseIndentStringIsNull() {
        String input = "name|age\nAlice|30\n";

        assertThatThrownBy(() -> formatter.format(input, null, new Config(IndentStyle.SPACE, 4)))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("baseIndentString must not be null");
    }

    @Test
    void shouldThrowNullPointerExceptionWhenConfigIsNull() {
        String input = "name|age\nAlice|30\n";

        assertThatThrownBy(() -> formatter.format(input, "", null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("config must not be null");
    }
}
