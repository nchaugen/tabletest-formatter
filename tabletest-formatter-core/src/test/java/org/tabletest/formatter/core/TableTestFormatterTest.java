package org.tabletest.formatter.core;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.tabletest.formatter.config.Config;
import org.tabletest.formatter.config.IndentStyle;
import org.tabletest.junit.Description;
import org.tabletest.junit.TableTest;
import org.tabletest.reporter.junit.Lines;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * End-to-end assembly checks for {@link TableTestFormatter}. The individual formatting
 * rules are specified in ColumnWidthTest, RowLayoutTest, CellFormattingTest,
 * CommentAndBlankLineTest and IndentationTest.
 */
@DisplayName("Graceful degradation")
class TableTestFormatterTest {

    private final TableTestFormatter formatter = new TableTestFormatter();

    @Test
    void shouldFormatTableWithColumnAlignment() {
        String input = """
            name|age
            Alice|30
            Bob|25
            """;

        String result = formatter.format(input, "", Config.NO_INDENT);

        assertThat(result).isEqualTo("""
            name  | age
            Alice | 30
            Bob   | 25
            """);
    }

    @Test
    void shouldFormatHeaderOnlyTable() {
        String input = """
            name|age
            """;

        String result = formatter.format(input, "", Config.NO_INDENT);

        assertThat(result).isEqualTo("""
            name | age
            """);
    }

    @DisplayName("Returns input it cannot parse unchanged")
    @Description("""
            Formatting must never break a build. The formatter returns input it cannot parse as a
            well-formed table exactly as it was. An escaped quote is not part of the table grammar,
            so the formatter leaves a table holding one alone. The first row shows the contrast: the
            formatter reformats parseable input, and Formatted lines then differs from Table lines.
            """)
    @TableTest("""
        Scenario                     | Table lines                                    | Formatted lines?
        Well-formed table            | ["name|age", "Alice|30"]                       | ["name  | age", "Alice | 30"]
        Extra column in a data row   | ["name|age", "Alice|30", "Bob|25|London"]      | ["name|age", "Alice|30", "Bob|25|London"]
        Missing column in a data row | ["name|age|city", "Alice|30", "Bob|25|London"] | ["name|age|city", "Alice|30", "Bob|25|London"]
        Escaped quotes in a value    | ["name|message", 'test|"He said \\"hello\\""'] | ["name|message", 'test|"He said \\"hello\\""']
        Empty input                  | []                                             | []
        Whitespace-only input        | ["   ", "  ", "   "]                           | ["   ", "  ", "   "]
        """)
    void leavesUnparseableInputUntouched(@Lines List<String> tableLines, @Lines List<String> formattedLines) {
        String input = String.join("\n", tableLines);

        assertThat(formatter.format(input, "", Config.NO_INDENT)).isEqualTo(String.join("\n", formattedLines));
    }

    @Test
    void shouldNotIndentInputItCannotParse() {
        String input = "name|age\nAlice|30\nBob|25|London";

        String result = formatter.format(input, "", new Config(IndentStyle.SPACE, 4));

        assertThat(result).isEqualTo(input);
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
