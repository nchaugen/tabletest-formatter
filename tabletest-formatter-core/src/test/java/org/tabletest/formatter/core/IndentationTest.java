package org.tabletest.formatter.core;

import org.junit.jupiter.api.DisplayName;
import org.tabletest.formatter.config.Config;
import org.tabletest.formatter.config.IndentStyle;
import org.tabletest.junit.Description;
import org.tabletest.junit.TableTest;
import org.tabletest.junit.TypeConverter;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Indentation rules")
public class IndentationTest {

    private final TableTestFormatter formatter = new TableTestFormatter();

    @DisplayName("Tables are re-indented from scratch")
    @Description("""
            With a configured indent (style:size), input lines are stripped of their own
            leading whitespace and re-indented with the base indent (the indentation of
            the surrounding code) plus one level of the configured indent. Blank lines
            stay completely empty. The final element of Result lines shows what ends the
            block: a bare indent that aligns the closing text-block quotes, or nothing
            when the indent size is zero.
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
        """)
    void appliesIndentation(List<String> tableLines, String baseIndent, Config indent, List<String> resultLines) {
        String result = formatter.format(String.join("\n", tableLines) + "\n", baseIndent, indent);

        assertThat(result).isEqualTo(String.join("\n", resultLines));
    }

    @TypeConverter
    public static Config parseIndent(String value) {
        String[] parts = value.split(":");
        return new Config(IndentStyle.valueOf(parts[0].toUpperCase()), Integer.parseInt(parts[1]));
    }
}
