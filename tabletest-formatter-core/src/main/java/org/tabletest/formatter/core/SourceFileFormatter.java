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

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.stream.Collectors.joining;

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

        // Extract string values from array content
        List<String> entries = extractStringEntries(arrayContent);
        if (entries.isEmpty()) {
            return result;
        }

        // Format as plain table text using existing formatter logic
        String tableText = String.join("\n", entries);
        String formattedTable = formatter.format(tableText, "", Config.NO_INDENT);

        // Split into lines and pad for aligned closing quotes
        String[] formattedLines = formattedTable.split("\n", -1);
        // Remove trailing empty element from split
        String[] lines = Arrays.stream(formattedLines).filter(l -> !l.isEmpty()).toArray(String[]::new);

        int maxWidth = Arrays.stream(lines).mapToInt(String::length).max().orElse(0);

        // Build indented string array
        String indent = config.indentSize() > 0
                ? baseIndentString + config.indentStyle().repeat(config.indentSize())
                : baseIndentString;

        String replacement = Arrays.stream(lines)
                .map(line -> indent + "\"" + padRight(line, maxWidth) + "\"")
                .collect(joining(",\n"));

        String formatted = "\n" + replacement + "\n" + baseIndentString;

        String original = arrayContent;
        if (formatted.equals(original)) {
            return result;
        }

        return result.substring(0, match.tableContentStart()) + formatted + result.substring(match.tableContentEnd());
    }

    private static final Pattern STRING_ENTRY_PATTERN = Pattern.compile("\"((?:[^\"\\\\]|\\\\.)*)\"");

    private List<String> extractStringEntries(String arrayContent) {
        Matcher matcher = STRING_ENTRY_PATTERN.matcher(arrayContent);
        List<String> entries = new java.util.ArrayList<>();
        while (matcher.find()) {
            entries.add(matcher.group(1));
        }
        return entries;
    }

    private String padRight(String value, int width) {
        int padding = width - value.length();
        return padding > 0 ? value + " ".repeat(padding) : value;
    }
}
