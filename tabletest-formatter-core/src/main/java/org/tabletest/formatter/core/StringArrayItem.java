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
