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
package io.github.nchaugen.tabletest.formatter.spotless;

import java.io.Serial;
import java.io.Serializable;

/**
 * Immutable state holder for TableTest formatter configuration.
 * <p>
 * Implements Serializable to support Spotless caching.
 */
public final class TableTestFormatterState implements Serializable {

    @Serial
    private static final long serialVersionUID = 3L;

    private final int tabSize;
    private final int indentSize;

    /**
     * Creates a new TableTestFormatterState with default configuration (tab size of 4, indent size of 4).
     */
    public TableTestFormatterState() {
        this(4, 4);
    }

    /**
     * Creates a new TableTestFormatterState with specified tab size and default indent size of 4.
     *
     * @param tabSize the number of spaces a tab character should be converted to (must be positive)
     * @throws IllegalArgumentException if tabSize is less than 1
     */
    public TableTestFormatterState(int tabSize) {
        this(tabSize, 4);
    }

    /**
     * Creates a new TableTestFormatterState with specified tab size and indent size.
     *
     * @param tabSize    the number of spaces a tab character should be converted to (must be positive)
     * @param indentSize the number of spaces per indent level (must be non-negative)
     * @throws IllegalArgumentException if tabSize is less than 1 or indentSize is negative
     */
    public TableTestFormatterState(int tabSize, int indentSize) {
        if (tabSize < 1) {
            throw new IllegalArgumentException("tabSize must be at least 1, got: " + tabSize);
        }
        if (indentSize < 0) {
            throw new IllegalArgumentException("indentSize must not be negative, got: " + indentSize);
        }
        this.tabSize = tabSize;
        this.indentSize = indentSize;
    }

    /**
     * Gets the configured tab size.
     *
     * @return the number of spaces a tab character should be converted to
     */
    public int tabSize() {
        return tabSize;
    }

    /**
     * Gets the configured indent size.
     *
     * @return the number of spaces per indent level
     */
    public int indentSize() {
        return indentSize;
    }
}
