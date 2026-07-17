package org.tabletest.formatter.core;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.tabletest.formatter.config.Config;
import org.tabletest.junit.Description;
import org.tabletest.junit.TableTest;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Cell value formatting rules")
class CellFormattingTest {

    private final TableTestFormatter formatter = new TableTestFormatter();

    @DisplayName("Collection values are normalised")
    @Description("""
            Inside a cell, list, set and map values are rewritten with one space after
            each comma and colon and no spaces directly inside the brackets, recursively
            through nested collections. Quoted keys keep their quotes.
            """)
    @TableTest("""
            Scenario                              | Value                   | Formatted?
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
    void normalisesCollectionValues(String value, String formatted) {
        assertThat(formatSingleCell(value)).isEqualTo(formatted);
    }

    @DisplayName("Quoted values are preserved as written")
    @Description("""
            The formatter never rewrites quoting: a quoted value keeps its quote style,
            pipes inside quotes do not split the cell, and backslash sequences are kept
            literally. Values that mix both quote styles, use unmatched quotes, or hold
            quoted list elements cannot be written inside this table (they would break
            the table's own parsing) and are covered by the plain tests below.
            """)
    @TableTest("""
            Scenario                  | Value      | Formatted?
            Single-quoted value       | "'value'"  | "'value'"
            Double-quoted value       | '"value"'  | '"value"'
            Pipe inside quotes        | "'a|b'"    | "'a|b'"
            Invalid backslash escape  | \\uZZZZ    | \\uZZZZ
            """)
    void preservesQuotedValues(String value, String formatted) {
        assertThat(formatSingleCell(value)).isEqualTo(formatted);
    }

    @Test
    void shouldPreserveMixedQuoteStylesWithinCell() {
        String input = """
            name|message
            test|'He said "hello"'
            """;

        String result = formatter.format(input, "", Config.NO_INDENT);

        assertThat(result).isEqualTo("""
            name | message
            test | 'He said "hello"'
            """);
    }

    @Test
    void shouldPreserveUnmatchedSingleQuote() {
        String input = """
            name|value
            test|'unclosed
            """;

        String result = formatter.format(input, "", Config.NO_INDENT);

        assertThat(result).isEqualTo("""
            name | value
            test | 'unclosed
            """);
    }

    @Test
    void shouldPreserveUnmatchedDoubleQuote() {
        String input = """
            name|value
            test|"unclosed
            """;

        String result = formatter.format(input, "", Config.NO_INDENT);

        assertThat(result).isEqualTo("""
            name | value
            test | "unclosed
            """);
    }

    @Test
    void shouldPreserveQuotedElementsInsideCollections() {
        String input = """
            col
            [unquoted, 'with|pipe', "with]bracket"]
            """;

        String result = formatter.format(input, "", Config.NO_INDENT);

        assertThat(result).isEqualTo("""
            col
            [unquoted, 'with|pipe', "with]bracket"]
            """);
    }

    @Test
    void shouldPadQuotedValuesLikeAnyOtherValue() {
        String input = """
            short|long
            'a'|'longer'
            """;

        String result = formatter.format(input, "", Config.NO_INDENT);

        assertThat(result).isEqualTo("""
            short | long
            'a'   | 'longer'
            """);
    }

    private String formatSingleCell(String value) {
        String result = formatter.format("value\n" + value, "", Config.NO_INDENT);
        return result.split("\n")[1];
    }
}
