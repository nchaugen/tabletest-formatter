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
package org.tabletest.formatter.core;

import org.tabletest.formatter.config.Config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * Formats source files containing @TableTest annotations.
 * Processes matches in reverse order to avoid offset tracking.
 */
public class SourceFileFormatter {

    private final TableTestFormatter formatter;
    private final TableTestExtractor extractor;
    private final StringArrayContentParser arrayContentParser;

    public SourceFileFormatter() {
        this.formatter = new TableTestFormatter();
        this.extractor = new TableTestExtractor();
        this.arrayContentParser = new StringArrayContentParser();
    }

    /**
     * Formats all TableTest tables found in a source file using the provided configuration.
     *
     * @param content the source file content
     * @param config  the formatting configuration
     * @return the formatted content, or original if no changes needed
     */
    public String format(String content, Config config) {
        List<TableMatch> matches = extractor.findAll(content);

        return matches.isEmpty() ? content : formatMatches(content, matches, config);
    }

    private String formatMatches(String content, List<TableMatch> matches, Config config) {
        return matches.stream()
                .sorted(Comparator.comparingInt(TableMatch::tableContentStart).reversed())
                .reduce(content, (result, match) -> formatMatch(result, content, match, config), (s1, s2) -> s1);
    }

    private String formatMatch(String result, String originalContent, TableMatch match, Config config) {
        return switch (match.matchType()) {
            case TEXT_BLOCK -> formatTextBlockMatch(result, originalContent, match, config);
            case STRING_ARRAY -> formatStringArrayMatch(result, originalContent, match, config);
        };
    }

    private String formatTextBlockMatch(String result, String originalContent, TableMatch match, Config config) {
        String originalTable = originalContent.substring(match.tableContentStart(), match.tableContentEnd());
        String baseIndentString = originalContent.substring(match.baseIndentStart(), match.baseIndentEnd());
        String formattedTable = formatter.format(originalTable, baseIndentString, config);

        if (formattedTable.equals(originalTable)) {
            return result;
        }

        // Ensure at least one newline after opening quotes (""") for @TableTest annotations.
        // Preserves multiple newlines if present. Improves readability (Kotlin) and syntax correctness (Java).
        String replacement = formattedTable.startsWith("\n") ? formattedTable : "\n" + formattedTable;
        return result.substring(0, match.tableContentStart()) + replacement + result.substring(match.tableContentEnd());
    }

    private String formatStringArrayMatch(String result, String originalContent, TableMatch match, Config config) {
        String arrayContent = originalContent.substring(match.tableContentStart(), match.tableContentEnd());
        String baseIndentString = originalContent.substring(match.baseIndentStart(), match.baseIndentEnd());

        List<StringArrayItem> items = arrayContentParser.parse(arrayContent);
        List<String> entryValues = items.stream()
                .filter(StringArrayItem.Entry.class::isInstance)
                .map(item -> ((StringArrayItem.Entry) item).value())
                .toList();
        if (entryValues.isEmpty()) {
            return result;
        }

        // Format as plain table text using existing formatter logic
        String tableText = String.join("\n", entryValues);
        String formattedTable = formatter.format(tableText, "", Config.NO_INDENT);

        List<String> formattedEntries = splitIntoEntryLines(formattedTable);
        if (formattedEntries.size() != entryValues.size()) {
            // Graceful degradation: formatted lines no longer map one-to-one onto entries
            return result;
        }

        // Build indented string array
        String indent = config.indentSize() > 0
                ? baseIndentString + config.indentStyle().repeat(config.indentSize())
                : baseIndentString;

        String formatted = "\n" + renderArrayLines(items, formattedEntries, indent) + "\n" + baseIndentString;

        if (formatted.equals(arrayContent)) {
            return result;
        }

        return result.substring(0, match.tableContentStart()) + formatted + result.substring(match.tableContentEnd());
    }

    private List<String> splitIntoEntryLines(String formattedTable) {
        List<String> lines = new ArrayList<>(Arrays.asList(formattedTable.split("\n", -1)));
        // Remove trailing empty element from split (artifact of the trailing newline)
        if (!lines.isEmpty() && lines.get(lines.size() - 1).isEmpty()) {
            lines.remove(lines.size() - 1);
        }
        return lines;
    }

    /**
     * Rebuilds the array lines in source order: each entry padded for aligned closing
     * quotes, comments kept in place — on their own line, or appended to the preceding
     * line when they did not start their source line.
     */
    private String renderArrayLines(List<StringArrayItem> items, List<String> formattedEntries, String indent) {
        int maxWidth =
                formattedEntries.stream().mapToInt(DisplayWidth::of).max().orElse(0);

        List<String> lines = new ArrayList<>();
        int entryIndex = 0;
        for (StringArrayItem item : items) {
            if (item instanceof StringArrayItem.Entry) {
                String cell = "\"" + padRight(formattedEntries.get(entryIndex), maxWidth) + "\"";
                entryIndex++;
                lines.add(indent + cell + (entryIndex < formattedEntries.size() ? "," : ""));
            } else if (item instanceof StringArrayItem.Comment comment) {
                if (comment.startsLine() || lines.isEmpty()) {
                    lines.add(indent + comment.text());
                } else {
                    lines.set(lines.size() - 1, lines.get(lines.size() - 1) + " " + comment.text());
                }
            }
        }
        return String.join("\n", lines);
    }

    private String padRight(String value, int width) {
        int padding = width - DisplayWidth.of(value);
        return padding > 0 ? value + " ".repeat(padding) : value;
    }
}
