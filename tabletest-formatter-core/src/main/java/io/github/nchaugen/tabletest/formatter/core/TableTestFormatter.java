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
import java.util.Objects;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.joining;

/**
 * Formats TableTest tables according to consistent formatting rules.
 *
 * <p>Parses TableTest table text and applies formatting such as column alignment,
 * spacing normalization, and quote preservation.
 *
 * <h2>Error Handling Strategy</h2>
 * <p>This formatter follows a <strong>graceful degradation</strong> policy:
 * <ul>
 *   <li>If table parsing fails (malformed structure, mismatched columns, etc.),
 *       the original input is returned unchanged</li>
 *   <li>This ensures formatting never breaks a build due to table syntax errors</li>
 *   <li>Parse exceptions ({@code TableTestParseException}) are caught internally</li>
 * </ul>
 *
 * <p><strong>Exceptions that propagate to caller:</strong>
 * <ul>
 *   <li>{@link NullPointerException} - if required parameters are null</li>
 *   <li>{@link IllegalArgumentException} - if indent parameters are negative</li>
 * </ul>
 */
public class TableTestFormatter {

    /**
     * Width allocated for spacing between columns (trailing space after cell value).
     * This accounts for the space character that appears before the pipe separator,
     * ensuring proper visual separation between columns in the formatted table.
     */
    private static final int COLUMN_SEPARATOR_WIDTH = 1;

    private final CellFormatter cellFormatter = new CellFormatter();

    /**
     * Formats the given TableTest table text without indentation.
     *
     * <p><strong>Graceful Degradation:</strong> If the input cannot be parsed
     * (malformed table structure, mismatched columns, invalid syntax), the original
     * input is returned unchanged. This ensures formatting never breaks a build.
     *
     * <p><strong>Examples of inputs returned unchanged:</strong>
     * <ul>
     *   <li>Empty strings or whitespace-only input</li>
     *   <li>Tables with mismatched column counts between rows</li>
     *   <li>Invalid quote escaping that the parser cannot handle</li>
     *   <li>Any input that throws {@code TableTestParseException} during parsing</li>
     * </ul>
     *
     * <p><strong>Example usage:</strong>
     * <pre>
     * var formatter = new TableTestFormatter();
     * var input = "name|age\nAlice|30\nBob|25";
     * var formatted = formatter.format(input);
     * // Returns:
     * // "name  | age\n"
     * // "Alice | 30\n"
     * // "Bob   | 25\n"
     * </pre>
     *
     * @param tableText the raw table text to format (must not be null)
     * @return the formatted table text, or the original input if parsing/formatting fails
     * @throws NullPointerException if tableText is null
     */
    public String format(String tableText) {
        Objects.requireNonNull(tableText, "tableText must not be null");
        return format(tableText, 0, 0);
    }

    /**
     * Formats the given TableTest table text with indentation.
     *
     * <p><strong>Graceful Degradation:</strong> If the input cannot be parsed
     * (malformed table structure, mismatched columns, invalid syntax), the original
     * input is returned unchanged. This ensures formatting never breaks a build.
     *
     * <p>When {@code indentSize > 0}, the input is normalized (stripped and trimmed)
     * before formatting, and trailing indentation is added for closing quote alignment.
     *
     * <p><strong>Example usage:</strong>
     * <pre>
     * var formatter = new TableTestFormatter();
     * var input = "name|age\nAlice|30";
     * var formatted = formatter.format(input, 4, 8);
     * // Returns (with 8 spaces base indent + 4 spaces per level):
     * //             "            name  | age\n"
     * //             "            Alice | 30\n"
     * //             "            "
     * </pre>
     *
     * @param tableText  the raw table text to format (must not be null)
     * @param indentSize the number of spaces per indent level (must be >= 0, typically 2 or 4)
     * @param baseIndent the base indentation level in spaces (must be >= 0)
     * @return the formatted table text with proper indentation, or the original input if parsing/formatting fails
     * @throws NullPointerException if tableText is null
     * @throws IllegalArgumentException if indentSize or baseIndent is negative
     */
    public String format(String tableText, int indentSize, int baseIndent) {
        Objects.requireNonNull(tableText, "tableText must not be null");
        if (indentSize < 0) {
            throw new IllegalArgumentException("indentSize must not be negative: " + indentSize);
        }
        if (baseIndent < 0) {
            throw new IllegalArgumentException("baseIndent must not be negative: " + baseIndent);
        }

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
     * <p><strong>Note:</strong> This method does NOT follow graceful degradation.
     * If parsing fails, the parser exception will propagate to the caller.
     *
     * @param tableText the raw table text
     * @return an array of column widths
     * @throws io.github.nchaugen.tabletest.parser.TableTestParseException if table cannot be parsed
     * @throws NullPointerException if tableText is null
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
        int paddingSpaces = width + COLUMN_SEPARATOR_WIDTH - DisplayWidth.of(value);
        return value + " ".repeat(Math.max(0, paddingSpaces));
    }
}
