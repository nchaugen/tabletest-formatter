package org.tabletest.formatter.core;

/**
 * An item found inside a @TableTest string-array initializer: a string entry or a comment.
 *
 * <p>Items appear in source order, so a formatter can rebuild the array with
 * comments kept in their original positions.
 */
sealed interface StringArrayItem {

    /**
     * Whether this item is the first item on its source line.
     * A comment that does not start its line belongs inline after the preceding item.
     */
    boolean startsLine();

    /**
     * A string literal entry.
     *
     * @param value      the raw text between the quotes, escape sequences untouched
     * @param startsLine whether this entry is the first item on its source line
     */
    record Entry(String value, boolean startsLine) implements StringArrayItem {}

    /**
     * A line or block comment.
     *
     * @param text       the full comment text including the comment markers
     * @param startsLine whether this comment is the first item on its source line
     */
    record Comment(String text, boolean startsLine) implements StringArrayItem {}
}
