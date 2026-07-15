package org.tabletest.formatter.core;

import java.util.ArrayList;
import java.util.List;

/**
 * Parses the content of a @TableTest string-array initializer into its ordered
 * string entries and comments.
 *
 * <p>Comment-aware: a string inside a comment is part of the comment text, never
 * an entry, so commented-out rows are not mistaken for live table rows.
 */
final class StringArrayContentParser {

    List<StringArrayItem> parse(String arrayContent) {
        List<StringArrayItem> items = new ArrayList<>();
        boolean firstOnLine = true;
        int i = 0;

        while (i < arrayContent.length()) {
            char c = arrayContent.charAt(i);
            char next = peek(arrayContent, i + 1);

            if (c == '"') {
                int end = findStringEnd(arrayContent, i + 1);
                items.add(new StringArrayItem.Entry(arrayContent.substring(i + 1, end), firstOnLine));
                firstOnLine = false;
                i = end + 1;
            } else if (c == '/' && next == '/') {
                int end = findLineEnd(arrayContent, i);
                items.add(new StringArrayItem.Comment(arrayContent.substring(i, end), firstOnLine));
                firstOnLine = false;
                i = end;
            } else if (c == '/' && next == '*') {
                int end = findBlockCommentEnd(arrayContent, i + 2);
                items.add(new StringArrayItem.Comment(arrayContent.substring(i, end), firstOnLine));
                firstOnLine = false;
                i = end;
            } else {
                if (c == '\n') {
                    firstOnLine = true;
                }
                i++;
            }
        }

        return List.copyOf(items);
    }

    private char peek(String content, int index) {
        return index < content.length() ? content.charAt(index) : '\0';
    }

    /**
     * Finds the closing quote of a string literal, honouring backslash escapes.
     * Returns the content length if the string is unterminated.
     */
    private int findStringEnd(String content, int from) {
        int i = from;
        while (i < content.length()) {
            char c = content.charAt(i);
            if (c == '\\') {
                i += 2;
            } else if (c == '"') {
                return i;
            } else {
                i++;
            }
        }
        return content.length();
    }

    private int findLineEnd(String content, int from) {
        int newline = content.indexOf('\n', from);
        return newline == -1 ? content.length() : newline;
    }

    private int findBlockCommentEnd(String content, int from) {
        int close = content.indexOf("*/", from);
        return close == -1 ? content.length() : close + 2;
    }
}
