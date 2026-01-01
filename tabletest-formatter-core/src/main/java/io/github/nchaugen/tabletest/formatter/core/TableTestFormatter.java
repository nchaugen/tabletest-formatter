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
import java.util.List;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.joining;

/**
 * Formats TableTest tables according to consistent formatting rules.
 *
 * <p>Parses TableTest table text and applies formatting such as column alignment,
 * spacing normalization, and quote preservation.
 */
public class TableTestFormatter {

    private final CellFormatter cellFormatter = new CellFormatter();

    /**
     * Formats the given TableTest table text without indentation.
     *
     * <p>If the input cannot be parsed or formatted (malformed table structure),
     * the original input is returned unchanged.
     *
     * @param tableText the raw table text to format
     * @return the formatted table text, or the original input if formatting fails
     */
    public String format(String tableText) {
        return format(tableText, 0, 0);
    }

    /**
     * Formats the given TableTest table text with indentation.
     *
     * <p>If the input cannot be parsed or formatted (malformed table structure),
     * the original input is returned unchanged.
     *
     * @param tableText  the raw table text to format
     * @param indentSize the number of spaces per indent level (typically 2 or 4)
     * @param baseIndent the base indentation level (spaces before @TableTest annotation)
     * @return the formatted table text with proper indentation, or the original input if formatting fails
     */
    public String format(String tableText, int indentSize, int baseIndent) {
        try {
            // Strip and normalize whitespace structure when using indentation
            String input = indentSize > 0 ? tableText.strip() : tableText;

            // Trim lines when indentation will be applied to normalize spacing
            String[] lines = indentSize > 0
                    ? Arrays.stream(input.split("\n", -1)).map(String::trim).toArray(String[]::new)
                    : input.split("\n", -1);

            // Identify comment and blank lines
            List<Integer> commentOrBlankLines = IntStream.range(0, lines.length)
                    .filter(i -> isCommentLine(lines[i]) || isBlankLine(lines[i]))
                    .boxed()
                    .toList();

            // Format the table (parser ignores comments and blank lines)
            Table table = TableParser.parse(input, true);

            String formatted = formatTable(table);

            // Add back preserved comments and blank lines
            String withComments = addBackCommentsAndBlankLines(lines, commentOrBlankLines, formatted);

            // Apply indentation as final step if requested
            return indentSize > 0 ? applyIndentation(withComments, indentSize, baseIndent) : withComments;
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
        String[] formattedLines = formatted.split("\n", -1);

        List<String> result = new java.util.ArrayList<>();
        int formattedIndex = 0;

        // Merge original lines (with comments/blanks) with formatted lines
        for (int i = 0; i < originalLines.length; i++) {
            if (commentOrBlankIndices.contains(i)) {
                // Preserve comment or blank line from original
                result.add(originalLines[i]);
            } else {
                // Use formatted table line
                result.add(formattedLines[formattedIndex++]);
            }
        }

        // Add any remaining formatted lines
        // Skip final empty string (artifact from split with trailing newline)
        while (formattedIndex < formattedLines.length) {
            if (formattedIndex == formattedLines.length - 1 && formattedLines[formattedIndex].isEmpty()) {
                break;
            }
            result.add(formattedLines[formattedIndex++]);
        }

        return String.join("\n", result);
    }

    private String applyIndentation(String formatted, int indentSize, int baseIndent) {
        String indent = " ".repeat(baseIndent + indentSize);
        String[] lines = formatted.split("\n", -1);

        // Apply indent to each line and add trailing indent for closing quote alignment
        return Arrays.stream(lines).map(line -> indent + line).collect(joining("\n")) + "\n" + indent;
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

        return table.rows().isEmpty() ? headerRow + "\n" : headerRow + "\n" + dataRows + "\n";
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
        return DisplayWidth.of(cellFormatter.formatCell(cell));
    }

    private String formatRow(List<?> cells, int[] columnWidths) {
        return IntStream.range(0, cells.size())
                .mapToObj(i -> {
                    String value = cellFormatter.formatCell(cells.get(i));
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

    private String padCell(String value, int width) {
        int paddingSpaces = width + 1 - DisplayWidth.of(value);
        return value + " ".repeat(Math.max(0, paddingSpaces));
    }
}
