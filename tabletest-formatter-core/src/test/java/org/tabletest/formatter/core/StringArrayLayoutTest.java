package org.tabletest.formatter.core;

import org.junit.jupiter.api.DisplayName;
import org.tabletest.formatter.config.Config;
import org.tabletest.formatter.config.IndentStyle;
import org.tabletest.junit.Description;
import org.tabletest.junit.TableTest;
import org.tabletest.junit.TypeConverter;
import org.tabletest.reporter.junit.Lines;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("String array layout")
public class StringArrayLayoutTest {

    private final SourceFileFormatter formatter = new SourceFileFormatter();

    @DisplayName("Writes each entry on its own line, closing quotes aligned")
    @Description("""
            The formatter lays out a table written as an array of string literals as a block. The
            opening brace ends its line. Every entry gets a line of its own. The closing brace and
            parenthesis end the block.

            The formatter pads an entry with trailing spaces inside the quotes, so the closing quote
            of every entry sits in the same column. That is the array form's equivalent of aligned
            pipes.

            The entries are written at the configured indent. The Indentation feature says what an
            indent does in general. An indent is written style:size.
            """)
    @TableTest("""
        Scenario                 | Source lines                                          | Configured indent | Formatted lines?
        Entries on one line      | ['@TableTest({"name|age","Alice|30","Bob|7"})']       | space:4           | ['@TableTest({', '    "name  | age",', '    "Alice | 30 ",', '    "Bob   | 7  "', '})']
        Entries already on lines | ['@TableTest({', '"name|age",', '"Alice|30"', '})']   | space:4           | ['@TableTest({', '    "name  | age",', '    "Alice | 30 "', '})']
        A single entry           | ['@TableTest({"name|age"})']                          | space:4           | ['@TableTest({', '    "name | age"', '})']
        Wide characters          | ['@TableTest({"name|width","你好|4","hello|5"})']     | space:4           | ['@TableTest({', '    "name  | width",', '    "你好  | 4    ",', '    "hello | 5    "', '})']
        An empty entry           | ['@TableTest({"a|b", "", "1|2"})']                    | space:4           | ['@TableTest({', '    "a | b",', '    "     ",', '    "1 | 2"', '})']
        Already laid out         | ['@TableTest({', '    "a | b",', '    "1 | 2"', '})'] | space:4           | ['@TableTest({', '    "a | b",', '    "1 | 2"', '})']
        No indent configured     | ['@TableTest({"name|age","Alice|30"})']               | space:0           | ['@TableTest({', '"name  | age",', '"Alice | 30 "', '})']
        """)
    void laysOutEachEntryOnItsOwnLine(
            @Lines List<String> sourceLines, Config configuredIndent, @Lines List<String> formattedLines) {
        assertThat(formatted(sourceLines, configuredIndent)).isEqualTo(formattedLines);
    }

    @DisplayName("Keeps a comment in the array, in place and unchanged")
    @Description("""
            An entry commented out to disable a scenario stays where it is, byte for byte. So does a
            note written between entries. Neither takes part in the alignment of the entries around
            it. A comment written after an entry stays on that entry's line.
            """)
    @TableTest("""
        Scenario               | Source lines                                                                   | Configured indent | Formatted lines?
        Commented-out entry    | ['@TableTest({', '"a|b",', '// "9 | 9",', '"10|2"', '})']                      | space:4           | ['@TableTest({', '    "a  | b",', '    // "9 | 9",', '    "10 | 2"', '})']
        Block-commented entry  | ['@TableTest({', '"a|b",', '/* "9 | 9", */', '"10|2"', '})']                   | space:4           | ['@TableTest({', '    "a  | b",', '    /* "9 | 9", */', '    "10 | 2"', '})']
        A note between entries | ['@TableTest({', '"name|age",', '// boundary cases below', '"Alice|30"', '})'] | space:4           | ['@TableTest({', '    "name  | age",', '    // boundary cases below', '    "Alice | 30 "', '})']
        Comment after an entry | ['@TableTest({', '"name|age", // header', '"Alice|30" // adult', '})']         | space:4           | ['@TableTest({', '    "name  | age", // header', '    "Alice | 30 " // adult', '})']
        Nothing but a comment  | ['@TableTest({ /* "a|b" */ })']                                                | space:4           | ['@TableTest({ /* "a|b" */ })']
        """)
    void preservesCommentsInPlace(
            @Lines List<String> sourceLines, Config configuredIndent, @Lines List<String> formattedLines) {
        assertThat(formatted(sourceLines, configuredIndent)).isEqualTo(formattedLines);
    }

    /** The lines the formatter produces for these lines of source at the configured indent. */
    private List<String> formatted(List<String> sourceLines, Config configuredIndent) {
        String result = formatter.format(String.join("\n", sourceLines) + "\n", configuredIndent);
        return List.of(result.stripTrailing().split("\n", -1));
    }

    @TypeConverter
    public static Config parseIndent(String value) {
        String[] parts = value.split(":");
        return new Config(IndentStyle.valueOf(parts[0].toUpperCase()), Integer.parseInt(parts[1]));
    }
}
