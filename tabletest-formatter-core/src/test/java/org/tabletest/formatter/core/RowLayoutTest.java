package org.tabletest.formatter.core;

import org.junit.jupiter.api.DisplayName;
import org.tabletest.junit.Description;
import org.tabletest.junit.TableTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Cell padding and alignment")
class RowLayoutTest {

    private final TableTestFormatter formatter = new TableTestFormatter();

    @DisplayName("Cells are padded to their column width and joined with pipes")
    @Description("""
            Column widths come from the Column width rules and are measured in display
            columns. Every cell except the last is padded to its column width plus one
            space before the pipe; every cell after the first gets one space after the
            pipe. The last column is never padded, so no line carries trailing spaces.
            The header row is laid out by the same rule as data rows.
            """)
    @TableTest("""
        Scenario                            | Cells       | Column widths | Row?
        Cells padded to their column width  | [Alice, 30] | [5, 3]        | 'Alice | 30'
        Last column never padded            | [Bob, 7]    | [5, 3]        | 'Bob   | 7'
        Three columns joined with pipes     | [a, bb, c]  | [1, 2, 1]     | 'a | bb | c'
        Empty first cell filled with spaces | ['', 30]    | [5, 3]        | '      | 30'
        Empty middle cell                   | [a, '', c]  | [1, 1, 1]     | 'a |   | c'
        Empty last cell leaves pipe bare    | [short, ''] | [5, 3]        | 'short |'
        Single column gets no padding       | [Alice]     | [8]           | Alice
        CJK padded by display width         | [中文, x]   | [6, 1]        | '中文   | x'
        Emoji padded by display width       | [😀, ok]    | [2, 2]        | '😀 | ok'
        """)
    void laysOutRow(List<String> cells, List<Integer> columnWidths, String row) {
        int[] widths = columnWidths.stream().mapToInt(Integer::intValue).toArray();

        assertThat(formatter.formatRow(cells, widths)).isEqualTo(row);
    }
}
