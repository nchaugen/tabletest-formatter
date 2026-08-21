package org.tabletest.formatter.core;

import org.junit.jupiter.api.DisplayName;
import org.tabletest.formatter.config.Config;
import org.tabletest.junit.Description;
import org.tabletest.junit.TableTest;
import org.tabletest.reporter.junit.Lines;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Comments and blank lines")
class CommentAndBlankLineTest {

    private final TableTestFormatter formatter = new TableTestFormatter();

    @DisplayName("Counts a line as a comment or a blank")
    @Description("""
            A comment line starts with // after any leading whitespace. A blank line holds nothing
            but whitespace. A // marker after cell content does not make the line a comment.
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

    @DisplayName("Keeps a comment or blank line in place, with its text")
    @Description("""
            A comment line and a blank line take no part in column alignment. The formatter formats
            the rows around them as one table. It then puts the kept lines back at their original
            positions, byte for byte.
            """)
    @TableTest("""
        Scenario                     | Table lines                                                     | Formatted?
        Comment between rows         | ["name|age", "// note", "Alice|30"]                             | ["name  | age", "// note", "Alice | 30"]
        Blank line between rows      | ["name|age", "Alice|30", "", "Bob|25"]                          | ["name  | age", "Alice | 30", "", "Bob   | 25"]
        Comments and blanks together | ["name|age", "// First", "Alice|30", "", "// Second", "Bob|25"] | ["name  | age", "// First", "Alice | 30", "", "// Second", "Bob   | 25"]
        """)
    void preservesCommentAndBlankLinesInPlace(@Lines List<String> tableLines, @Lines List<String> formattedLines) {
        String result = formatter.format(String.join("\n", tableLines) + "\n", "", Config.NO_INDENT);

        assertThat(result).isEqualTo(String.join("\n", formattedLines) + "\n");
    }
}
