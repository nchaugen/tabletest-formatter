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
    private final TableTestExtractor extractor;

    public SourceFileFormatter() {
        this.formatter = new TableTestFormatter();
        this.extractor = new TableTestExtractor();
    }

    /**
     * Formats all TableTest tables found in a source file without indentation.
     *
     * @param content the source file content
     * @return the formatted content, or original if no changes needed
     */
    public String format(String content) {
        return format(content, 0, IndentType.SPACE);
    }

    /**
     * Formats all TableTest tables found in a source file with specified indentation.
     *
     * @param content    the source file content
     * @param indentSize the number of indent characters to add (0 for no indentation)
     * @return the formatted content, or original if no changes needed
     */
    public String format(String content, int indentSize) {
        return format(content, indentSize, IndentType.SPACE);
    }

    /**
     * Formats all TableTest tables found in a source file with specified indentation and indent type.
     *
     * @param content    the source file content
     * @param indentSize the number of indent characters to add (spaces or tabs depending on indent type, 0 for no indentation)
     * @param indentType the type of indentation to use (SPACE or TAB)
     * @return the formatted content, or original if no changes needed
     */
    public String format(String content, int indentSize, IndentType indentType) {
        List<TableMatch> matches = extractor.findAll(content);

        return matches.isEmpty() ? content : formatMatches(content, matches, indentSize, indentType);
    }

    private String formatMatches(String content, List<TableMatch> matches, int indentSize, IndentType indentType) {
        return matches.stream()
                .sorted(Comparator.comparingInt(TableMatch::tableContentStart).reversed())
                .reduce(
                        content,
                        (result, match) -> formatMatch(result, content, match, indentSize, indentType),
                        (s1, s2) -> s1);
    }

    private String formatMatch(
            String result, String originalContent, TableMatch match, int indentSize, IndentType indentType) {
        String originalTable = originalContent.substring(match.tableContentStart(), match.tableContentEnd());
        String baseIndentString = originalContent.substring(match.baseIndentStart(), match.baseIndentEnd());
        String formattedTable = formatter.format(originalTable, indentSize, baseIndentString, indentType);

        return formattedTable.equals(originalTable) ? result : replaceTableContent(result, match, formattedTable);
    }

    private String replaceTableContent(String result, TableMatch match, String formattedTable) {
        // Ensure at least one newline after opening quotes (""") for @TableTest annotations.
        // Preserves multiple newlines if present. Improves readability (Kotlin) and syntax correctness (Java).
        String replacement = formattedTable.startsWith("\n") ? formattedTable : "\n" + formattedTable;

        return result.substring(0, match.tableContentStart()) + replacement + result.substring(match.tableContentEnd());
    }
}
