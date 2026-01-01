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
    private static final long serialVersionUID = 2L;

    private final int tabSize;

    /**
     * Creates a new TableTestFormatterState with default configuration (tab size of 4).
     */
    public TableTestFormatterState() {
        this(4);
    }

    /**
     * Creates a new TableTestFormatterState with specified tab size.
     *
     * @param tabSize the number of spaces a tab character should be converted to (must be positive)
     * @throws IllegalArgumentException if tabSize is less than 1
     */
    public TableTestFormatterState(int tabSize) {
        if (tabSize < 1) {
            throw new IllegalArgumentException("tabSize must be at least 1, got: " + tabSize);
        }
        this.tabSize = tabSize;
    }

    /**
     * Gets the configured tab size.
     *
     * @return the number of spaces a tab character should be converted to
     */
    public int tabSize() {
        return tabSize;
    }
}
