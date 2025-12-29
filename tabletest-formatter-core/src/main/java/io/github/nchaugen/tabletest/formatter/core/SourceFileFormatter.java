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
     * Formats all TableTest tables found in a source file.
     *
     * @param content the source file content
     * @return the formatted content, or original if no changes needed
     */
    public String format(String content) {
        List<TableMatch> matches = TableTestExtractor.findAll(content);

        return matches.isEmpty() ? content : formatMatches(content, matches);
    }

    private String formatMatches(String content, List<TableMatch> matches) {
        return matches.stream()
                .sorted(Comparator.comparingInt(TableMatch::startIndex).reversed())
                .reduce(content, (result, match) -> formatMatch(result, content, match), (s1, s2) -> s1);
    }

    private String formatMatch(String result, String originalContent, TableMatch match) {
        String originalTable = match.originalText();
        String formattedTable = formatter.format(originalTable);

        return formattedTable.equals(originalTable)
                ? result
                : replaceTableContent(result, originalContent, match, formattedTable);
    }

    private String replaceTableContent(String result, String originalContent, TableMatch match, String formattedTable) {
        String matchText = originalContent.substring(match.startIndex(), match.endIndex());
        int tableContentStart = matchText.indexOf("\"\"\"") + 3;
        int tableContentEnd = matchText.lastIndexOf("\"\"\"");

        int actualStart = match.startIndex() + tableContentStart;
        int actualEnd = match.startIndex() + tableContentEnd;

        return result.substring(0, actualStart) + formattedTable + result.substring(actualEnd);
    }
}
