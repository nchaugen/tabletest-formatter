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

import java.util.Comparator;
import java.util.List;

/**
 * Formats source files containing @TableTest annotations.
 * Processes matches in reverse order to avoid offset tracking.
 */
public class SourceFileFormatter {

    private final TableTestFormatter formatter;

    public SourceFileFormatter() {
        this.formatter = new TableTestFormatter();
    }

    /**
     * Formats all TableTest tables found in a source file without indentation.
     *
     * @param content the source file content
     * @return the formatted content, or original if no changes needed
     */
    public String format(String content) {
        return format(content, 0, 4);
    }

    /**
     * Formats all TableTest tables found in a source file with specified indentation.
     *
     * @param content    the source file content
     * @param indentSize the number of spaces per indent level (0 for no indentation)
     * @return the formatted content, or original if no changes needed
     */
    public String format(String content, int indentSize) {
        return format(content, indentSize, 4);
    }

    /**
     * Formats all TableTest tables found in a source file with specified indentation and tab size.
     *
     * @param content    the source file content
     * @param indentSize the number of spaces per indent level (0 for no indentation)
     * @param tabSize    the number of spaces a tab character should be converted to
     * @return the formatted content, or original if no changes needed
     */
    public String format(String content, int indentSize, int tabSize) {
        List<TableMatch> matches = TableTestExtractor.findAll(content, tabSize);

        return matches.isEmpty() ? content : formatMatches(content, matches, indentSize);
    }

    private String formatMatches(String content, List<TableMatch> matches, int indentSize) {
        return matches.stream()
                .sorted(Comparator.comparingInt(TableMatch::startIndex).reversed())
                .reduce(content, (result, match) -> formatMatch(result, content, match, indentSize), (s1, s2) -> s1);
    }

    private String formatMatch(String result, String originalContent, TableMatch match, int indentSize) {
        String originalTable = match.originalText();
        String formattedTable = formatter.format(originalTable, indentSize, match.baseIndent());

        return formattedTable.equals(originalTable)
                ? result
                : replaceTableContent(result, originalContent, match, formattedTable, indentSize);
    }

    private String replaceTableContent(
            String result, String originalContent, TableMatch match, String formattedTable, int indentSize) {
        String matchText = originalContent.substring(match.startIndex(), match.endIndex());
        int tableContentStart = matchText.indexOf("\"\"\"") + 3;
        int tableContentEnd = matchText.lastIndexOf("\"\"\"");

        int actualStart = match.startIndex() + tableContentStart;
        int actualEnd = match.startIndex() + tableContentEnd;

        // When using indentation, add leading newline for text block formatting
        String replacement = indentSize > 0 ? "\n" + formattedTable : formattedTable;

        // Replace the table content (formatted table includes closing quote indentation)
        return result.substring(0, actualStart) + replacement + result.substring(actualEnd);
    }
}
