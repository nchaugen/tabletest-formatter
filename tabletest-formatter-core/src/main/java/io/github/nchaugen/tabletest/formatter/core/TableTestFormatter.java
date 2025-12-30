/*
 * Copyright 2025-present Nils Christian Haugen
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.nchaugen.tabletest.formatter.core;

import io.github.nchaugen.tabletest.parser.Table;
import io.github.nchaugen.tabletest.parser.TableParser;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.joining;

/**
 * Formats TableTest tables according to consistent formatting rules.
 *
 * <p>Parses TableTest table text and applies formatting such as column alignment,
 * spacing normalization, and quote preservation.
 */
public class TableTestFormatter {

    /**
     * Formats the given TableTest table text.
     *
     * <p>If the input cannot be parsed or formatted (malformed table structure),
     * the original input is returned unchanged.
     *
     * @param tableText the raw table text to format
     * @return the formatted table text, or the original input if formatting fails
     */
    public String format(String tableText) {
        try {
            String[] lines = tableText.split("\n", -1);

            // Identify comment and blank lines
            List<Integer> commentOrBlankLines = IntStream.range(0, lines.length)
                    .filter(i -> isCommentLine(lines[i]) || isBlankLine(lines[i]))
                    .boxed()
                    .toList();

            // Format the table (parser ignores comments and blank lines)
            Table table = TableParser.parse(tableText, true);

            // Single-line tables (header only, no data rows) should be returned unchanged
            if (table.rows().isEmpty()) {
                return tableText;
            }

            String formatted = formatTable(table);

            // Add back preserved comments and blank lines
            return addBackCommentsAndBlankLines(lines, commentOrBlankLines, formatted);
        } catch (Exception e) {
            // Return input unchanged if parsing or formatting fails
            return tableText;
        }
    }

    private boolean isCommentLine(String line) {
        return line.trim().startsWith("//");
    }

    private boolean isBlankLine(String line) {
        return line.trim().isEmpty();
    }

    private String addBackCommentsAndBlankLines(
            String[] originalLines, List<Integer> commentOrBlankIndices, String formatted) {
        Iterator<String> formattedIterator =
                Arrays.stream(formatted.split("\n", -1)).iterator();

        return IntStream.range(0, originalLines.length)
                .mapToObj(i -> commentOrBlankIndices.contains(i) ? originalLines[i] : formattedIterator.next())
                .collect(joining("\n"));
    }

    /**
     * Calculates the maximum width needed for each column.
     *
     * @param tableText the raw table text
     * @return an array of column widths
     */
    public int[] calculateColumnWidths(String tableText) {
        Table table = TableParser.parse(tableText, true);
        return calculateColumnWidths(table);
    }

    private String formatTable(Table table) {
        int[] columnWidths = calculateColumnWidths(table);

        String headerRow = formatRow(table.headers(), columnWidths);

        String dataRows = table.rows().stream()
                .map(row -> formatRow(row.values(), columnWidths))
                .collect(joining("\n"));

        return headerRow + "\n" + dataRows + "\n";
    }

    private int[] calculateColumnWidths(Table table) {
        return IntStream.range(0, table.columnCount())
                .map(col -> calculateColumnWidth(table, col))
                .toArray();
    }

    private int calculateColumnWidth(Table table, int columnIndex) {
        int headerWidth = cellWidth(table.header(columnIndex));

        int maxDataWidth = table.rows().stream()
                .mapToInt(row -> cellWidth(row.value(columnIndex)))
                .max()
                .orElse(0);

        return Math.max(headerWidth, maxDataWidth);
    }

    private int cellWidth(Object cell) {
        return DisplayWidth.of(formatCell(cell));
    }

    private String formatRow(List<?> cells, int[] columnWidths) {
        return IntStream.range(0, cells.size())
                .mapToObj(i -> {
                    String value = formatCell(cells.get(i));
                    return padToColumnWidth(value, i == 0, i == cells.size() - 1, columnWidths[i]);
                })
                .collect(joining("|"));
    }

    private String padToColumnWidth(String value, boolean isFirst, boolean isLast, int columnWidth) {
        if (isFirst && isLast) {
            // Single column: no padding, no spacing
            return value;
        } else if (isFirst) {
            // First column (not last): no leading space, just pad
            return padCell(value, columnWidth);
        } else if (isLast) {
            // Last column (not first): leading space only
            return value.isEmpty() ? "" : " " + value;
        } else {
            // Middle cells: leading space + padding
            return " " + padCell(value, columnWidth);
        }
    }

    private String formatCell(Object cell) {
        return switch (cell) {
            case null -> "";
            case List<?> list -> formatList(list);
            case Set<?> set -> formatSet(set);
            case Map<?, ?> map -> formatMap(map);
            default -> cell.toString();
        };
    }

    private String formatList(List<?> list) {
        return list.isEmpty() ? "[]" : list.stream().map(this::formatCell).collect(joining(", ", "[", "]"));
    }

    private String formatSet(Set<?> set) {
        return set.isEmpty() ? "{}" : set.stream().map(this::formatCell).collect(joining(", ", "{", "}"));
    }

    private String formatMap(Map<?, ?> map) {
        return map.isEmpty()
                ? "[:]"
                : map.entrySet().stream()
                        .map(entry -> entry.getKey() + ": " + formatCell(entry.getValue()))
                        .collect(joining(", ", "[", "]"));
    }

    private String padCell(String value, int width) {
        int paddingSpaces = width + 1 - DisplayWidth.of(value);
        return value + " ".repeat(Math.max(0, paddingSpaces));
    }
}
