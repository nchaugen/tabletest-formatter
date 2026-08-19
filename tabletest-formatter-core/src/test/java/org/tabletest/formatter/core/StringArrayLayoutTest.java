package org.tabletest.formatter.core;

import org.junit.jupiter.api.DisplayName;
import org.tabletest.formatter.config.Config;
import org.tabletest.junit.Description;
import org.tabletest.junit.TableTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("String array layout")
class StringArrayLayoutTest {

    private final SourceFileFormatter formatter = new SourceFileFormatter();

    @DisplayName("Each entry is written on its own line, padded so the closing quotes align")
    @Description("""
            A table written as an array of string literals is laid out as a block: the opening
            brace ends its line, every entry gets a line of its own, and the closing brace and
            parenthesis end the block. Entries are padded with trailing spaces inside the
            quotes, so the closing quote of every entry sits in the same column — the array
            form's equivalent of aligned pipes. Every row below is formatted with a four-space
            indent; what that indent does is the Indentation rules feature.
            """)
    @TableTest("""
        Scenario                 | Source lines                                              | Formatted lines?
        Entries on one line      | ['@TableTest({"name|age","Alice|30","Bob|7"})']            | ['@TableTest({', '    "name  | age",', '    "Alice | 30 ",', '    "Bob   | 7  "', '})']
        Entries already on lines | ['@TableTest({', '"name|age",', '"Alice|30"', '})']        | ['@TableTest({', '    "name  | age",', '    "Alice | 30 "', '})']
        A single entry           | ['@TableTest({"name|age"})']                               | ['@TableTest({', '    "name | age"', '})']
        Wide characters          | ['@TableTest({"name|width","你好|4","hello|5"})']          | ['@TableTest({', '    "name  | width",', '    "你好  | 4    ",', '    "hello | 5    "', '})']
        An empty entry           | ['@TableTest({"a|b", "", "1|2"})']                         | ['@TableTest({', '    "a | b",', '    "     ",', '    "1 | 2"', '})']
        Already laid out         | ['@TableTest({', '    "a | b",', '    "1 | 2"', '})']      | ['@TableTest({', '    "a | b",', '    "1 | 2"', '})']
        """)
    void laysOutEachEntryOnItsOwnLine(List<String> sourceLines, List<String> formattedLines) {
        assertThat(formatted(sourceLines)).isEqualTo(formattedLines);
    }

    @DisplayName("Comments in the array keep their place and their text")
    @Description("""
            Entries commented out to disable a scenario, and notes written between entries, are
            reproduced byte for byte where they stand — they take no part in the alignment of
            the entries around them. A comment written after an entry stays on that entry's
            line.
            """)
    @TableTest("""
        Scenario               | Source lines                                                             | Formatted lines?
        Commented-out entry    | ['@TableTest({', '"a|b",', '// "9 | 9",', '"10|2"', '})']                 | ['@TableTest({', '    "a  | b",', '    // "9 | 9",', '    "10 | 2"', '})']
        Block-commented entry  | ['@TableTest({', '"a|b",', '/* "9 | 9", */', '"10|2"', '})']              | ['@TableTest({', '    "a  | b",', '    /* "9 | 9", */', '    "10 | 2"', '})']
        A note between entries | ['@TableTest({', '"name|age",', '// boundary cases below', '"Alice|30"', '})'] | ['@TableTest({', '    "name  | age",', '    // boundary cases below', '    "Alice | 30 "', '})']
        Comment after an entry | ['@TableTest({', '"name|age", // header', '"Alice|30" // adult', '})']    | ['@TableTest({', '    "name  | age", // header', '    "Alice | 30 " // adult', '})']
        Nothing but a comment  | ['@TableTest({ /* "a|b" */ })']                                           | ['@TableTest({ /* "a|b" */ })']
        """)
    void preservesCommentsInPlace(List<String> sourceLines, List<String> formattedLines) {
        assertThat(formatted(sourceLines)).isEqualTo(formattedLines);
    }

    /** The lines the formatter produces for these lines of source, at a four-space indent. */
    private List<String> formatted(List<String> sourceLines) {
        String result = formatter.format(String.join("\n", sourceLines) + "\n", Config.SPACES_4);
        return List.of(result.stripTrailing().split("\n", -1));
    }
}
