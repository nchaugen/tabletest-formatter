package org.tabletest.formatter.core;

import org.junit.jupiter.api.Test;
import org.tabletest.formatter.config.Config;
import org.tabletest.formatter.config.IndentStyle;
import org.tabletest.junit.TableTest;

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

    @TableTest("""
        Scenario                              | Input                   | Formatted?
        Normalize spacing in lists            | "[1,2,3]"               | "[1, 2, 3]"
        Remove extra spaces inside brackets   | "[ [] ]"                | "[[]]"
        Format nested lists                   | "[[1,2],[3,4]]"         | "[[1, 2], [3, 4]]"
        Format empty lists                    | "[]"                    | "[]"
        Normalize spacing in maps             | "[a:1,b:2]"             | "[a: 1, b: 2]"
        Format single-quoted keys             | "['[a]':1,'b:b':2]"     | "['[a]': 1, 'b:b': 2]"
        Format double-quoted keys             | '[",a,":1,"b|b":2]'     | '[",a,": 1, "b|b": 2]'
        Format empty maps                     | "[:]"                   | "[:]"
        Normalize spacing in sets             | "{1,2,3}"               | "{1, 2, 3}"
        Format set with nested list           | "{[1,2]}"               | "{[1, 2]}"
        Format empty sets                     | "{}"                    | "{}"
        Format list of maps                   | "[[a:1],[b:2]]"         | "[[a: 1], [b: 2]]"
        Format nested collections recursively | "[a:[1,2],b:[3,4]]"     | "[a: [1, 2], b: [3, 4]]"
        Format deeply nested collections      | "[a:{[1,2]},b:{[3,4]}]" | "[a: {[1, 2]}, b: {[3, 4]}]"
        """)
    void shouldFormatCollectionInCell(String input, String formatted) {
        var tableInput = "value\n" + input;

        Objects.requireNonNull(tableInput, "tableText must not be null");
        var result = formatter.format(tableInput, "", Config.NO_INDENT);

        var lines = result.split("\n");
        assertThat(lines[1]).isEqualTo(formatted);
    }

    @Test
    void shouldPreserveCommentLines() {
        var input = """
            name|age
            // This is a comment
            Alice|30
            Bob|25
            """;

        Objects.requireNonNull(input, "tableText must not be null");
        var result = formatter.format(input, "", Config.NO_INDENT);

        assertThat(result).isEqualTo("""
            name  | age
            // This is a comment
            Alice | 30
            Bob   | 25
            """);
    }

    @Test
    void shouldPreserveBlankLines() {
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
    void shouldPreserveCommentsAndBlankLinesTogether() {
        var input = """
            name|age
            // First group
            Alice|30

            // Second group
            Bob|25
            """;

        Objects.requireNonNull(input, "tableText must not be null");
        var result = formatter.format(input, "", Config.NO_INDENT);

        assertThat(result).isEqualTo("""
            name  | age
            // First group
            Alice | 30

            // Second group
            Bob   | 25
            """);
    }

    @Test
    void shouldPreserveSingleQuotes() {
        var input = """
            col
            'value'
            """;

        Objects.requireNonNull(input, "tableText must not be null");
        var result = formatter.format(input, "", Config.NO_INDENT);

        assertThat(result).isEqualTo("""
            col
            'value'
            """);
    }

    @Test
    void shouldPreserveDoubleQuotes() {
        var input = """
            col
            "value"
            """;

        Objects.requireNonNull(input, "tableText must not be null");
        var result = formatter.format(input, "", Config.NO_INDENT);

        assertThat(result).isEqualTo("""
            col
            "value"
            """);
    }

    @Test
    void shouldPreserveMixedQuoteStyles() {
        var input = """
            col1|col2|col3
            'single'|"double"|unquoted
            """;

        Objects.requireNonNull(input, "tableText must not be null");
        var result = formatter.format(input, "", Config.NO_INDENT);

        assertThat(result).isEqualTo("""
            col1     | col2     | col3
            'single' | "double" | unquoted
            """);
    }

    @Test
    void shouldPreserveQuotesWithPadding() {
        var input = """
            short|long
            'a'|'longer'
            """;

        Objects.requireNonNull(input, "tableText must not be null");
        var result = formatter.format(input, "", Config.NO_INDENT);

        assertThat(result).isEqualTo("""
            short | long
            'a'   | 'longer'
            """);
    }

    @Test
    void shouldPreserveQuotesWithPipeInside() {
        var input = """
            col1|col2
            'a|b'|'c'
            """;

        Objects.requireNonNull(input, "tableText must not be null");
        var result = formatter.format(input, "", Config.NO_INDENT);

        assertThat(result).isEqualTo("""
            col1  | col2
            'a|b' | 'c'
            """);
    }

    @Test
    void shouldPreserveQuotesInListWithSpecialCharacters() {
        var input = """
            col
            [unquoted, 'with|pipe', "with]bracket"]
            """;

        Objects.requireNonNull(input, "tableText must not be null");
        var result = formatter.format(input, "", Config.NO_INDENT);

        assertThat(result).isEqualTo("""
            col
            [unquoted, 'with|pipe', "with]bracket"]
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
    void shouldHandleUnmatchedSingleQuote() {
        var input = """
            name|value
            test|'unclosed
            """;

        Objects.requireNonNull(input, "tableText must not be null");
        var result = formatter.format(input, "", Config.NO_INDENT);

        assertThat(result).isEqualTo("""
            name | value
            test | 'unclosed
            """);
    }

    @Test
    void shouldHandleUnmatchedDoubleQuote() {
        var input = """
            name|value
            test|"unclosed
            """;

        Objects.requireNonNull(input, "tableText must not be null");
        var result = formatter.format(input, "", Config.NO_INDENT);

        assertThat(result).isEqualTo("""
            name | value
            test | "unclosed
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
    void shouldHandleInvalidUnicodeSequence() {
        var input = """
            name|value
            test|\\uZZZZ
            """;

        Objects.requireNonNull(input, "tableText must not be null");
        var result = formatter.format(input, "", Config.NO_INDENT);

        assertThat(result).isEqualTo("""
            name | value
            test | \\uZZZZ
            """);
    }

    @Test
    void shouldHandleMixedQuotesWithinCell() {
        var input = """
            name|message
            test|'He said "hello"'
            """;

        Objects.requireNonNull(input, "tableText must not be null");
        var result = formatter.format(input, "", Config.NO_INDENT);

        assertThat(result).isEqualTo("""
            name | message
            test | 'He said "hello"'
            """);
    }

    @Test
    void shouldNotIndentWhenIndentSizeIsZero() {
        var input = """
            name|age
            Alice|30
            Bob|25
            """;

        var result = formatter.format(input, "", new Config(IndentStyle.SPACE, 0));

        assertThat(result).isEqualTo("""
            name  | age
            Alice | 30
            Bob   | 25
            """);
    }

    @Test
    void shouldIndentAllLinesIncludingTrailingIndentForClosingQuote() {
        var input = """
            name|age
            Alice|30
            Bob|25
            """;

        var result = formatter.format(input, "", new Config(IndentStyle.SPACE, 4));

        assertThat(result).isEqualTo("""
            name  | age
            Alice | 30
            Bob   | 25
            """.indent(4) + " ".repeat(4));
    }

    @Test
    void shouldCombineBaseIndentAndIndentSize() {
        var input = """
            name|age
            Alice|30
            """;

        var result = formatter.format(input, "    ", new Config(IndentStyle.SPACE, 2));

        assertThat(result).isEqualTo("""
            name  | age
            Alice | 30
            """.indent(6) + " ".repeat(6));
    }

    @Test
    void shouldNormalizeVaryingInputIndentation() {
        var input = """
              name|age
                    Alice|30
            Bob|25
            """;

        var result = formatter.format(input, "", new Config(IndentStyle.SPACE, 4));

        assertThat(result).isEqualTo("""
            name  | age
            Alice | 30
            Bob   | 25
            """.indent(4) + " ".repeat(4));
    }

    @Test
    void shouldReturnEmptyTableUnchangedWithIndentation() {
        var input = "";

        var result = formatter.format(input, "", new Config(IndentStyle.SPACE, 4));

        assertThat(result).isEmpty();
    }

    @Test
    void shouldFormatHeaderOnlyTableWithIndentation() {
        var input = """
            name|age
            """;

        var result = formatter.format(input, "", new Config(IndentStyle.SPACE, 4));

        assertThat(result).isEqualTo("""
            name | age
            """.indent(4) + " ".repeat(4));
    }

    @Test
    void shouldIndentComments() {
        var input = """
            name|age
            // This is a comment
            Alice|30
            Bob|25
            """;

        var result = formatter.format(input, "", new Config(IndentStyle.SPACE, 2));

        assertThat(result).isEqualTo("""
            name  | age
            // This is a comment
            Alice | 30
            Bob   | 25
            """.indent(2) + " ".repeat(2));
    }

    @Test
    void shouldLeaveBlankLinesEmptyWhenIndenting() {
        var input = """
            name|age
            Alice|30

            Bob|25
            """;

        var result = formatter.format(input, "", new Config(IndentStyle.SPACE, 2));

        assertThat(result).isEqualTo("  name  | age\n" + "  Alice | 30\n" + "\n" + "  Bob   | 25\n" + "  ");
    }

    @Test
    void shouldIndentCommentsButLeaveBlankLinesEmpty() {
        var input = """
            name|age
            // First group
            Alice|30

            // Second group
            Bob|25
            """;

        var result = formatter.format(input, "", new Config(IndentStyle.SPACE, 4));

        assertThat(result)
                .isEqualTo("    name  | age\n"
                        + "    // First group\n"
                        + "    Alice | 30\n"
                        + "\n"
                        + "    // Second group\n"
                        + "    Bob   | 25\n"
                        + "    ");
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
