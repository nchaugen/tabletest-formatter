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

/**
 * Extracts @TableTest annotations using a custom "just enough" parser.
 * <p>
 * Uses a state machine to track code context (comments, strings, method scope)
 * to reliably distinguish real annotations from ones inside string literals.
 * This solves the key limitation of regex-based extraction.
 */
public class TableTestExtractor {

    private enum State {
        CODE, // Normal code - look for @TableTest
        LINE_COMMENT, // Inside // comment
        BLOCK_COMMENT, // Inside /* */ comment
        STRING, // Inside "..." string
        TEXT_BLOCK, // Inside """...""" text block
        CHAR_LITERAL, // Inside '...' char literal
        LOOKING_FOR_TEXT_BLOCK // After @TableTest, looking for text block
    }

    public List<TableMatch> findAll(String sourceCode) {
        if (sourceCode == null) {
            throw new NullPointerException("sourceCode must not be null");
        }

        List<TableMatch> matches = new ArrayList<>();
        State state = State.CODE;
        State returnState = State.CODE; // State to return to after comments/strings
        int braceDepth = 0;
        int i = 0;

        // Track current annotation extraction
        int baseIndentStart = -1;
        int baseIndentEnd = -1;
        int tableContentStart = -1;

        while (i < sourceCode.length()) {
            char c = sourceCode.charAt(i);
            char next = peek(sourceCode, i + 1);
            char nextNext = peek(sourceCode, i + 2);

            // State transitions based on current state
            switch (state) {
                case CODE:
                    // Check for comment starts (highest priority)
                    if (c == '/' && next == '/') {
                        returnState = State.CODE;
                        state = State.LINE_COMMENT;
                        i += 2;
                        continue;
                    }
                    if (c == '/' && next == '*') {
                        returnState = State.CODE;
                        state = State.BLOCK_COMMENT;
                        i += 2;
                        continue;
                    }

                    // Check for string/char literals
                    if (c == '"' && next == '"' && nextNext == '"') {
                        state = State.TEXT_BLOCK;
                        i += 3;
                        continue;
                    }
                    if (c == '"') {
                        returnState = State.CODE;
                        state = State.STRING;
                        i++;
                        continue;
                    }
                    if (c == '\'') {
                        returnState = State.CODE;
                        state = State.CHAR_LITERAL;
                        i++;
                        continue;
                    }

                    // Track brace depth
                    if (c == '{') {
                        braceDepth++;
                    } else if (c == '}') {
                        braceDepth--;
                    }

                    // Look for @TableTest (or @fully.qualified.TableTest) at class scope
                    if (c == '@' && isTableTestAnnotation(sourceCode, i)) {
                        // Found @TableTest annotation - extract base indentation (only leading whitespace)
                        baseIndentStart = findLineStart(sourceCode, i);
                        // Find end of leading whitespace (stop at first non-whitespace character)
                        int j = baseIndentStart;
                        while (j < i && Character.isWhitespace(sourceCode.charAt(j))) {
                            j++;
                        }
                        baseIndentEnd = j;
                        // Switch to looking for text block (will skip comments/strings naturally)
                        state = State.LOOKING_FOR_TEXT_BLOCK;
                    }
                    break;

                case LOOKING_FOR_TEXT_BLOCK:
                    // After @TableTest, looking for opening """ while handling comments/strings
                    // Check for comment starts (handle like CODE state)
                    if (c == '/' && next == '/') {
                        returnState = State.LOOKING_FOR_TEXT_BLOCK;
                        state = State.LINE_COMMENT;
                        i += 2;
                        continue;
                    }
                    if (c == '/' && next == '*') {
                        returnState = State.LOOKING_FOR_TEXT_BLOCK;
                        state = State.BLOCK_COMMENT;
                        i += 2;
                        continue;
                    }

                    // Check for string/char literals (skip them)
                    if (c == '"' && next == '"' && nextNext == '"') {
                        // Found opening """ - start extracting
                        tableContentStart = i + 3;
                        state = State.TEXT_BLOCK;
                        i += 2; // Will increment by 1 at end of loop = +3 total
                        continue;
                    }
                    if (c == '"') {
                        returnState = State.LOOKING_FOR_TEXT_BLOCK;
                        state = State.STRING;
                        i++;
                        continue;
                    }
                    if (c == '\'') {
                        returnState = State.LOOKING_FOR_TEXT_BLOCK;
                        state = State.CHAR_LITERAL;
                        i++;
                        continue;
                    }

                    // If we hit something that's not part of annotation syntax, give up
                    if (c == ';' || c == '{') {
                        // End of annotation without finding text block
                        returnState = State.CODE;
                        state = State.CODE;
                        baseIndentStart = -1;
                        baseIndentEnd = -1;
                    }
                    break;

                case LINE_COMMENT:
                    if (c == '\n') {
                        state = returnState;
                    }
                    break;

                case BLOCK_COMMENT:
                    if (c == '*' && next == '/') {
                        state = returnState;
                        i += 2;
                        continue;
                    }
                    break;

                case STRING:
                    if (c == '\\') {
                        // Skip escaped character
                        i += 2;
                        continue;
                    }
                    if (c == '"') {
                        state = returnState;
                    }
                    break;

                case TEXT_BLOCK:
                    // Check for closing """ (not escaped)
                    if (c == '"' && next == '"' && nextNext == '"' && !isEscaped(sourceCode, i)) {
                        // Found end of text block
                        if (tableContentStart != -1) {
                            // We were extracting - record the match
                            int tableContentEnd = i;
                            matches.add(
                                    new TableMatch(tableContentStart, tableContentEnd, baseIndentStart, baseIndentEnd));

                            // Reset tracking
                            baseIndentStart = -1;
                            baseIndentEnd = -1;
                            tableContentStart = -1;
                        }
                        state = State.CODE;
                        i += 3;
                        continue;
                    }
                    break;

                case CHAR_LITERAL:
                    if (c == '\\') {
                        // Skip escaped character
                        i += 2;
                        continue;
                    }
                    if (c == '\'') {
                        state = returnState;
                    }
                    break;
            }

            i++;
        }

        return matches;
    }

    private char peek(String source, int index) {
        return index < source.length() ? source.charAt(index) : '\0';
    }

    private boolean matches(String source, int pos, String target) {
        if (pos + target.length() > source.length()) {
            return false;
        }
        return source.startsWith(target, pos);
    }

    private int findLineStart(String source, int pos) {
        int i = pos - 1;
        while (i >= 0 && source.charAt(i) != '\n') {
            i--;
        }
        return i + 1; // Position after newline (or 0 if at start)
    }

    /**
     * Checks if the character sequence starting at pos is a TableTest annotation.
     * Matches both @TableTest and @fully.qualified.TableTest by checking if the
     * last identifier component (after the last dot) is "TableTest".
     *
     * @param source the source code
     * @param pos position of the '@' character
     * @return true if this is a TableTest annotation
     */
    private boolean isTableTestAnnotation(String source, int pos) {
        if (pos >= source.length() || source.charAt(pos) != '@') {
            return false;
        }

        // Read the full annotation name (could be qualified like io.github.nchaugen.tabletest.junit.TableTest)
        int i = pos + 1;
        int lastDotPos = -1;

        // Read identifier with dots until we hit non-identifier character
        while (i < source.length()) {
            char c = source.charAt(i);
            if (Character.isJavaIdentifierPart(c)) {
                i++;
            } else if (c == '.') {
                lastDotPos = i;
                i++;
            } else {
                // End of annotation name
                break;
            }
        }

        // Extract the last component (after the last dot, or entire name if no dot)
        int lastComponentStart = lastDotPos == -1 ? pos + 1 : lastDotPos + 1;
        String lastComponent = source.substring(lastComponentStart, i);

        return lastComponent.equals("TableTest");
    }

    /**
     * Checks if the character at the given position is escaped by a backslash.
     * A character is escaped if it's preceded by an odd number of backslashes.
     * (Even number means the backslashes themselves are escaped.)
     *
     * @param source the source code
     * @param pos position to check
     * @return true if the character at pos is escaped
     */
    private boolean isEscaped(String source, int pos) {
        if (pos == 0) {
            return false;
        }

        // Count consecutive backslashes before this position
        int backslashCount = 0;
        int i = pos - 1;
        while (i >= 0 && source.charAt(i) == '\\') {
            backslashCount++;
            i--;
        }

        // Odd number of backslashes means the character is escaped
        return backslashCount % 2 == 1;
    }
}
