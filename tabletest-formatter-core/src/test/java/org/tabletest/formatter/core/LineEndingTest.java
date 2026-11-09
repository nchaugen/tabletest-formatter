package org.tabletest.formatter.core;

import org.junit.jupiter.api.DisplayName;
import org.tabletest.formatter.config.Config;
import org.tabletest.junit.Description;
import org.tabletest.junit.TableTest;
import org.tabletest.reporter.junit.Lines;

import java.util.List;

import static java.util.stream.Collectors.joining;
import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Line endings")
class LineEndingTest {

    private final TableTestFormatter formatter = new TableTestFormatter();

    @DisplayName("Keeps the line ending of each line")
    @Description("""
            A file with CRLF line endings keeps them, line by line, so formatting changes only
            whitespace inside the rows. A table cell cannot hold a carriage return, so the rows
            below write it as \\r.
            """)
    @TableTest("""
        Scenario                  | Table lines                            | Formatted lines?
        LF endings                | ["a|b", "x|y", ""]                     | ["a | b", "x | y", ""]
        CRLF endings              | ["a|b\\r", "x|y\\r", ""]               | ["a | b\\r", "x | y\\r", ""]
        CRLF after a comment line | ["a|b\\r", "// note\\r", "x|y\\r", ""] | ["a | b\\r", "// note\\r", "x | y\\r", ""]
        Endings that differ       | ["a|b\\r", "x|y", "z|w\\r"]            | ["a | b\\r", "x | y", "z | w\\r"]
        """)
    void keepsTheLineEndingOfEachLine(@Lines List<String> tableLines, @Lines List<String> formattedLines) {
        assertThat(formatter.format(text(tableLines), "", Config.NO_INDENT)).isEqualTo(text(formattedLines));
    }

    /** The lines joined with line feeds, with each written {@code \r} turned into a carriage return. */
    private static String text(List<String> lines) {
        return lines.stream().map(line -> line.replace("\\r", "\r")).collect(joining("\n"));
    }
}
