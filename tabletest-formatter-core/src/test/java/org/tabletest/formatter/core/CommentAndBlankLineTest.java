package org.tabletest.formatter.core;

import org.junit.jupiter.api.DisplayName;
import org.tabletest.formatter.config.Config;
import org.tabletest.junit.Description;
import org.tabletest.junit.TableTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Comment and blank line rules")
class CommentAndBlankLineTest {

    private final TableTestFormatter formatter = new TableTestFormatter();

    @DisplayName("Which lines count as comments or blanks")
    @Description("""
            A comment line starts with // after any leading whitespace; a blank line
            contains nothing but whitespace. A // marker after cell content does not
            make the line a comment.
            """)
    @TableTest("""
        Scenario                    | Line          | Comment line? | Blank line?
        Comment line                | '// note'     | true          | false
        Comment with leading spaces | '  // note'   | true          | false
        Empty line                  | ''            | false         | true
        Whitespace-only line        | '   '         | false         | true
        Data row                    | 'Alice|30'    | false         | false
        Comment marker mid-line     | 'Alice // 30' | false         | false
        """)
    void classifiesLine(String line, boolean commentLine, boolean blankLine) {
        assertThat(formatter.isCommentLine(line)).isEqualTo(commentLine);
        assertThat(formatter.isBlankLine(line)).isEqualTo(blankLine);
    }

    @DisplayName("Comment and blank lines keep their place and content")
    @Description("""
            Comment and blank lines take no part in column alignment: the surrounding
            rows are formatted as one table and the preserved lines are re-inserted at
            their original positions, byte for byte.
            """)
    @TableTest("""
        Scenario                     | Table lines                                                     | Formatted?
        Comment between rows         | ["name|age", "// note", "Alice|30"]                             | ["name  | age", "// note", "Alice | 30"]
        Blank line between rows      | ["name|age", "Alice|30", "", "Bob|25"]                          | ["name  | age", "Alice | 30", "", "Bob   | 25"]
        Comments and blanks together | ["name|age", "// First", "Alice|30", "", "// Second", "Bob|25"] | ["name  | age", "// First", "Alice | 30", "", "// Second", "Bob   | 25"]
        """)
    void preservesCommentAndBlankLinesInPlace(List<String> tableLines, List<String> formattedLines) {
        String result = formatter.format(String.join("\n", tableLines) + "\n", "", Config.NO_INDENT);

        assertThat(result).isEqualTo(String.join("\n", formattedLines) + "\n");
    }
}
