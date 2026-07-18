package org.tabletest.formatter.core;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.tabletest.formatter.config.Config;
import org.tabletest.formatter.config.IndentStyle;
import org.tabletest.junit.Description;
import org.tabletest.junit.TableTest;
import org.tabletest.junit.TypeConverterSources;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * End-to-end assembly checks for {@link TableTestFormatter}. The individual formatting
 * rules are specified in ColumnWidthTest, RowLayoutTest, CellFormattingTest,
 * CommentAndBlankLineTest and IndentationTest.
 */
@DisplayName("Graceful degradation")
@TypeConverterSources(IndentationTest.class)
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

    @DisplayName("Unparseable input is returned unchanged")
    @Description("""
            Formatting must never break a build: input that cannot be parsed as a
            well-formed table is returned exactly as it was, whatever indent is
            configured. Escaped quotes are not part of the table grammar, so tables
            containing them are left alone. The well-formed row shows the contrast:
            parseable input is reformatted.
            """)
    @TableTest("""
        Scenario                     | Table lines                                    | Configured indent      | Unchanged?
        Well-formed table            | ["name|age", "Alice|30"]                       | {'space:0', 'space:4'} | false
        Extra column in a data row   | ["name|age", "Alice|30", "Bob|25|London"]      | {'space:0', 'space:4'} | true
        Missing column in a data row | ["name|age|city", "Alice|30", "Bob|25|London"] | {'space:0', 'space:4'} | true
        Escaped quotes in a value    | ["name|message", 'test|"He said \\"hello\\""'] | {'space:0', 'space:4'} | true
        Empty input                  | []                                             | {'space:0', 'space:4'} | true
        Whitespace-only input        | ["   ", "  ", "   "]                           | {'space:0', 'space:4'} | true
        """)
    void leavesUnparseableInputUntouched(List<String> tableLines, Config indent, boolean unchanged) {
        String input = String.join("\n", tableLines);

        assertThat(formatter.format(input, "", indent).equals(input)).isEqualTo(unchanged);
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
