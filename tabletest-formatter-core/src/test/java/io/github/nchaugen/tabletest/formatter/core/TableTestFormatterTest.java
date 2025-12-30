package io.github.nchaugen.tabletest.formatter.core;

import io.github.nchaugen.tabletest.junit.TableTest;
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
    void shouldFormatTableWithMultipleEmptyCells() {
        var input = """
                col1|col2|col3|col4
                ||value|
                """;

        var result = formatter.format(input);

        assertThat(result).isEqualTo("""
                col1 | col2 | col3  | col4
                     |      | value |
                """);
    }

    @Test
    void shouldFormatTableWithEmptyCellsInWideColumns() {
        var input = """
                name|age
                |longest
                short|
                """;

        var result = formatter.format(input);

        assertThat(result).isEqualTo("""
                name  | age
                      | longest
                short |
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

    @Test
    void shouldFormatTableWithCjkCharacters() {
        var input = """
                name|greeting
                中文|你好
                日本語|こんにちは
                """;

        var result = formatter.format(input);

        assertThat(result).isEqualTo("""
                name   | greeting
                中文   | 你好
                日本語 | こんにちは
                """);
    }

    @Test
    void shouldFormatTableWithMixedUnicodeContent() {
        var input = """
                language|text
                Greek|Γεια
                Cyrillic|Привет
                """;

        var result = formatter.format(input);

        assertThat(result).isEqualTo("""
                language | text
                Greek    | Γεια
                Cyrillic | Привет
                """);
    }

    @Test
    void shouldCalculateWidthsForCjkCharacters() {
        var input = """
                name|greeting
                中文|你好
                日本語|こんにちは
                """;

        int[] widths = formatter.calculateColumnWidths(input);

        assertThat(widths).containsExactly(6, 10);
    }

    @Test
    void shouldFormatTableWithEmojis() {
        var input = """
                col|emoji
                test|😀
                """;

        var result = formatter.format(input);

        assertThat(result).isEqualTo("""
                col  | emoji
                test | 😀
                """);
    }

    @TableTest("""
        Scenario                              | Input                   | Formatted?
        Normalize spacing in lists            | "[1,2,3]"               | "[1, 2, 3]"
        Remove extra spaces inside brackets   | "[ [] ]"                | "[[]]"
        Format nested lists                   | "[[1,2],[3,4]]"         | "[[1, 2], [3, 4]]"
        Format empty lists                    | "[]"                    | "[]"
        Normalize spacing in maps             | "[a:1,b:2]"             | "[a: 1, b: 2]"
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

        var result = formatter.format(tableInput);

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

        var result = formatter.format(input);

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

        var result = formatter.format(input);

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

        var result = formatter.format(input);

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

        var result = formatter.format(input);

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

        var result = formatter.format(input);

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

        var result = formatter.format(input);

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

        var result = formatter.format(input);

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

        var result = formatter.format(input);

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

        var result = formatter.format(input);

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

        var result = formatter.format(input);

        assertThat(result).isEqualTo(input);
    }

    @Test
    void shouldReturnUnchangedWhenFewerColumnsInDataRow() {
        var input = """
                name|age|city
                Alice|30
                Bob|25|London
                """;

        var result = formatter.format(input);

        assertThat(result).isEqualTo(input);
    }

    @Test
    void shouldFormatSingleColumnTable() {
        var input = """
                name
                Alice
                Bob
                """;

        var result = formatter.format(input);

        assertThat(result).isEqualTo("""
                name
                Alice
                Bob
                """);
    }

    @Test
    void shouldReturnUnchangedWhenEmpty() {
        var input = "";

        var result = formatter.format(input);

        assertThat(result).isEqualTo(input);
    }

    @Test
    void shouldReturnUnchangedWhenOnlyWhitespace() {
        var input = "   \n  \n   ";

        var result = formatter.format(input);

        assertThat(result).isEqualTo(input);
    }

    @Test
    void shouldReturnUnchangedWhenSingleLine() {
        var input = "name|age";

        var result = formatter.format(input);

        assertThat(result).isEqualTo(input);
    }

    @Test
    void shouldHandleUnmatchedSingleQuote() {
        var input = """
                name|value
                test|'unclosed
                """;

        var result = formatter.format(input);

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

        var result = formatter.format(input);

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

        var result = formatter.format(input);

        // Parser cannot handle escaped quotes, returns unchanged
        assertThat(result).isEqualTo(input);
    }

    @Test
    void shouldHandleInvalidUnicodeSequence() {
        var input = """
                name|value
                test|\\uZZZZ
                """;

        var result = formatter.format(input);

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

        var result = formatter.format(input);

        assertThat(result).isEqualTo("""
                name | message
                test | 'He said "hello"'
                """);
    }
}
