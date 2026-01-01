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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility for finding @TableTest annotations in Java/Kotlin source files.
 * <p>
 * Uses regex-based pattern matching to locate @TableTest annotations with text block parameters.
 * This approach handles the majority of real-world cases where a single @TableTest annotation
 * is present per test method. Supports both direct text block parameters and named value parameters,
 * optionally combined with other parameters like resource and encoding.
 *
 * <h2>Error Handling</h2>
 * <p>This extractor follows a <strong>graceful approach</strong>:
 * <ul>
 *   <li>If no @TableTest annotations are found, returns an empty list</li>
 *   <li>Malformed annotations are silently skipped (regex won't match)</li>
 *   <li>Only throws exceptions for programming errors (null input)</li>
 * </ul>
 *
 * <h2>Known Limitations</h2>
 * <ul>
 *   <li>May not handle complex nested structures or nested parentheses correctly</li>
 *   <li>Cannot distinguish between @TableTest in comments vs actual code</li>
 * </ul>
 */
public class TableTestExtractor {

    private static final Pattern TABLE_TEST_PATTERN = Pattern.compile(
            "^([ \\t]*)@TableTest\\s*\\([^)]*?\"\"\"(.*?)\"\"\"[^)]*?\\)", Pattern.DOTALL | Pattern.MULTILINE);

    /**
     * Finds all @TableTest annotations in the provided source code.
     *
     * <p>If no annotations are found or the source contains malformed annotations
     * that don't match the expected pattern, returns an empty list. This method
     * never throws exceptions due to malformed input—only for programming errors.
     *
     * @param sourceCode the Java or Kotlin source code to search (must not be null)
     * @return list of all @TableTest matches found (empty if none found), with position information
     * @throws NullPointerException if sourceCode is null
     */
    public static List<TableMatch> findAll(String sourceCode) {
        Objects.requireNonNull(sourceCode, "sourceCode must not be null");
        List<TableMatch> matches = new ArrayList<>();
        Matcher matcher = TABLE_TEST_PATTERN.matcher(sourceCode);

        while (matcher.find()) {
            String leadingWhitespace = matcher.group(1);
            String tableText = matcher.group(2);
            // Adjust startIndex to point to @ symbol (skip leading whitespace)
            int startIndex = matcher.start() + leadingWhitespace.length();
            int endIndex = matcher.end();
            int baseIndent = calculateBaseIndent(leadingWhitespace);
            matches.add(new TableMatch(tableText, startIndex, endIndex, baseIndent));
        }

        return matches;
    }

    /**
     * Calculates the indent size from leading whitespace, converting tabs to 4 spaces.
     *
     * @param whitespace the leading whitespace string
     * @return the equivalent number of spaces
     */
    private static int calculateBaseIndent(String whitespace) {
        return whitespace.replaceAll("\t", "    ").length();
    }
}
