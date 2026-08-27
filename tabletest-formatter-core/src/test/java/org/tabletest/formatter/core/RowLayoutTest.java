package org.tabletest.formatter.core;

import org.junit.jupiter.api.DisplayName;
import org.tabletest.junit.Description;
import org.tabletest.junit.TableTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Cell padding and alignment")
class RowLayoutTest {

    private final TableTestFormatter formatter = new TableTestFormatter();

    @DisplayName("Pads each cell to its column width and joins with pipes")
    @Description("""
            Column widths come from the Column width feature, measured in display columns. The
            formatter pads every cell except the last to its column width plus one space before the
            pipe. It writes one space after the pipe for every cell after the first. It never pads
            the last column, so no line carries trailing spaces. The header row follows the same
            rule as a data row.
            """)
    @TableTest("""
        Scenario                             | Cells       | Column widths | Row?
        Cells that exactly fill their columns | [Alice, 30] | [5, 3]        | 'Alice | 30'
        Cells shorter than their columns      | [Bob, 7]    | [5, 3]        | 'Bob   | 7'
        Three columns                         | [a, bb, c]  | [1, 2, 1]     | 'a | bb | c'
        An empty first cell                   | ['', 30]    | [5, 3]        | '      | 30'
        An empty middle cell                  | [a, '', c]  | [1, 1, 1]     | 'a |   | c'
        An empty last cell                    | [short, ''] | [5, 3]        | 'short |'
        A single column                       | [Alice]     | [8]           | Alice
        Two-column glyphs                     | [中文, x]   | [6, 1]        | '中文   | x'
        """)
    void laysOutRow(List<String> cells, List<Integer> columnWidths, String row) {
        int[] widths = columnWidths.stream().mapToInt(Integer::intValue).toArray();

        assertThat(formatter.formatRow(cells, widths)).isEqualTo(row);
    }
}
