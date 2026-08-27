package org.tabletest.formatter.core;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.tabletest.formatter.config.Config;
import org.tabletest.junit.Description;
import org.tabletest.junit.TableTest;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Cell value formatting")
class CellFormattingTest {

    private final TableTestFormatter formatter = new TableTestFormatter();

    @DisplayName("Normalises a collection value")
    @Description("""
            Inside a cell, the formatter rewrites a list, a set, and a map. It writes one space
            after each comma and each colon. It writes no space directly inside a bracket. It does
            the same through a nested collection. A quoted key keeps its quotes.
            """)
    @TableTest("""
        Scenario                        | Value                   | Formatted?
        A list                          | "[1,2,3]"               | "[1, 2, 3]"
        A map                           | "[a:1,b:2]"             | "[a: 1, b: 2]"
        A set                           | "{1,2,3}"               | "{1, 2, 3}"
        An empty list                   | "[]"                    | "[]"
        An empty map                    | "[:]"                   | "[:]"
        An empty set                    | "{}"                    | "{}"
        Spaces just inside the brackets | "[ [] ]"                | "[[]]"
        A single-quoted key             | "['[a]':1,'b:b':2]"     | "['[a]': 1, 'b:b': 2]"
        A double-quoted key             | '[",a,":1,"b|b":2]'     | '[",a,": 1, "b|b": 2]'
        A list inside a list            | "[[1,2],[3,4]]"         | "[[1, 2], [3, 4]]"
        A list inside a set             | "{[1,2]}"               | "{[1, 2]}"
        A map inside a list             | "[[a:1],[b:2]]"         | "[[a: 1], [b: 2]]"
        A list inside a map             | "[a:[1,2],b:[3,4]]"     | "[a: [1, 2], b: [3, 4]]"
        Three levels of nesting         | "[a:{[1,2]},b:{[3,4]}]" | "[a: {[1, 2]}, b: {[3, 4]}]"
        """)
    void normalisesCollectionValues(String value, String formatted) {
        assertThat(formatSingleCell(value)).isEqualTo(formatted);
    }

    @DisplayName("Preserves a quoted value as written")
    @Description("""
            The formatter never rewrites quoting. A quoted value keeps its quote style. A pipe
            inside quotes does not split the cell. A backslash sequence stays literal.

            Three kinds of value have no row here, because each one breaks the parsing of this
            table. They are a value that mixes both quote styles, a value with unmatched quotes,
            and a value that holds quoted list elements. The plain tests below cover all three.
            """)
    @TableTest("""
        Scenario                 | Value     | Formatted?
        Single-quoted value      | "'value'" | "'value'"
        Double-quoted value      | '"value"' | '"value"'
        Pipe inside quotes       | "'a|b'"   | "'a|b'"
        Invalid backslash escape | \\uZZZZ   | \\uZZZZ
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
