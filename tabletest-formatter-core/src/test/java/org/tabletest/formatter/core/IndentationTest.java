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

@DisplayName("Indentation")
public class IndentationTest {

    private final TableTestFormatter formatter = new TableTestFormatter();
    private final SourceFileFormatter sourceFormatter = new SourceFileFormatter();

    @DisplayName("Re-indents every table line from scratch")
    @Description("""
            The formatter strips the leading whitespace from each input line. It then re-indents the
            line with the base indent plus one level of the configured indent. The base indent is
            the indentation of the surrounding code. A blank line stays completely empty.

            The final element of Result lines is the indent the closing text block quotes are
            written at. It is empty when the indent size is zero. The next rule shows that line
            in place, with the quotes on it.

            The style may be spaces or tabs. With a tab style each level is that many tab
            characters, kept as tabs.
            """)
    @TableTest("""
        Scenario                             | Table lines                                | Base indent | Configured indent | Result lines?
        Indent size zero leaves table flush  | ["name|age", "Alice|30"]                   | ''          | space:0           | ["name  | age", "Alice | 30", ""]
        Every line indented one level        | ["name|age", "Alice|30"]                   | ''          | space:4           | ["    name  | age", "    Alice | 30", "    "]
        Base indent added beneath the level  | ["name|age", "Alice|30"]                   | '    '      | space:2           | ["      name  | age", "      Alice | 30", "      "]
        Varying input indentation normalised | ["  name|age", "      Alice|30", "Bob|25"] | ''          | space:4           | ["    name  | age", "    Alice | 30", "    Bob   | 25", "    "]
        Header-only table                    | ["name|age"]                               | ''          | space:4           | ["    name | age", "    "]
        Comment lines indented too           | ["name|age", "// note", "Alice|30"]        | ''          | space:2           | ["  name  | age", "  // note", "  Alice | 30", "  "]
        Blank lines never indented           | ["name|age", "Alice|30", "", "Bob|25"]     | ''          | space:2           | ["  name  | age", "  Alice | 30", "", "  Bob   | 25", "  "]
        Tab style indents with a tab         | ["name|age", "Alice|30"]                   | ''          | tab:1             | ["\tname  | age", "\tAlice | 30", "\t"]
        Tab size sets tabs per level         | ["name|age", "Alice|30"]                   | ''          | tab:2             | ["\t\tname  | age", "\t\tAlice | 30", "\t\t"]
        """)
    void appliesIndentation(
            @Lines List<String> tableLines, String baseIndent, Config indent, @Lines List<String> resultLines) {
        String result = formatter.format(String.join("\n", tableLines) + "\n", baseIndent, indent);

        assertThat(result).isEqualTo(String.join("\n", resultLines));
    }

    @DisplayName("Starts the indent from the annotation's own line")
    @Description("""
            A table written in a source text sits inside an annotation. The formatter indents every
            line of the block — the table lines and the closing quotes alike — to the indentation of
            the annotation's own line plus one level of the configured indent.

            The annotation's own indentation is reproduced exactly as written, whatever mix of tabs
            and spaces it holds, and the configured level is added after it in the configured style.

            Source lines hold the block as it was written. Formatted lines hold what the formatter
            wrote back. The rule above says what one level of indent is; this one says where the
            level starts from.
            """)
    @TableTest("""
        Scenario                       | Source lines                                                                     | Configured indent | Formatted lines?
        Annotation at the left margin  | ['@TableTest(\"""', 'name|age', 'Alice|30', '\""")']                             | space:4           | ['@TableTest(\"""', '    name  | age', '    Alice | 30', '    \""")']
        Annotation indented one level  | ['    @TableTest(\"""', '    name|age', '    Alice|30', '    \""")']             | space:4           | ['    @TableTest(\"""', '        name  | age', '        Alice | 30', '        \""")']
        Tab indentation kept as a tab  | ['\t@TableTest(\"""', '\tname|age', '\tAlice|30', '\t\""")']                   | space:1           | ['\t@TableTest(\"""', '\t name  | age', '\t Alice | 30', '\t \""")']
        Tab style added after a space  | ['    @TableTest(\"""', '    name|age', '    Alice|30', '    \""")']             | tab:1             | ['    @TableTest(\"""', '    \tname  | age', '    \tAlice | 30', '    \t\""")']
        Indent size zero stays flush   | ['@TableTest(\"""', 'name|age', 'Alice|30', '\""")']                             | space:0           | ['@TableTest(\"""', 'name  | age', 'Alice | 30', '\""")']
        """)
    void startsTheIndentFromTheAnnotationLine(
            @Lines List<String> sourceLines, Config configuredIndent, @Lines List<String> formattedLines) {
        assertThat(formatted(sourceLines, configuredIndent)).isEqualTo(formattedLines);
    }

    /** The lines the source formatter produces for these lines of source at the configured indent. */
    private List<String> formatted(List<String> sourceLines, Config configuredIndent) {
        String result = sourceFormatter.format(String.join("\n", sourceLines) + "\n", configuredIndent);
        return List.of(result.stripTrailing().split("\n", -1));
    }

    @TypeConverter
    public static Config parseIndent(String value) {
        String[] parts = value.split(":");
        return new Config(IndentStyle.valueOf(parts[0].toUpperCase()), Integer.parseInt(parts[1]));
    }
}
