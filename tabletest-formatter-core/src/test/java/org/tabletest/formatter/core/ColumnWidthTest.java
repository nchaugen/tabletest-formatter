package org.tabletest.formatter.core;

import org.junit.jupiter.api.DisplayName;
import org.tabletest.junit.Description;
import org.tabletest.junit.TableTest;
import org.tabletest.reporter.junit.Lines;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Column width")
class ColumnWidthTest {

    private final TableTestFormatter formatter = new TableTestFormatter();

    @DisplayName("Widens a column to fit its widest cell")
    @Description("""
            The formatter measures a width in terminal display columns, not in characters. See the
            Display width feature. The header counts as a cell like any other. The column separator
            is not part of the width.
            """)
    @TableTest("""
        Scenario                       | Cells in column      | Width?
        Cells of differing lengths     | [a, dddd, bb]        | 4
        A header longer than its data  | [name, Alice, Bob]   | 5
        An empty cell among the values | ['', longest]        | 7
        Two-column glyphs              | [name, 中文, 日本語] | 6
        """)
    void columnIsAsWideAsItsWidestCell(List<String> cells, int width) {
        assertThat(formatter.columnWidth(cells)).isEqualTo(width);
    }

    @DisplayName("Measures each column on its own")
    @TableTest("""
        Scenario                        | Table lines                | Column widths?
        Columns of differing widths     | ["a|bb|ccc", "dddd|e|ff"]  | [4, 2, 3]
        An empty cell in one column     | ["name|value", "|longest"] | [4, 7]
        """)
    void measuresEachColumnIndependently(@Lines List<String> tableLines, List<Integer> columnWidths) {
        int[] widths = formatter.calculateColumnWidths(String.join("\n", tableLines));

        assertThat(widths)
                .containsExactly(
                        columnWidths.stream().mapToInt(Integer::intValue).toArray());
    }
}
