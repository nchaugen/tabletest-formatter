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

import io.github.nchaugen.tabletest.formatter.core.IndentType;

import java.io.Serial;
import java.io.Serializable;

/**
 * Immutable state holder for TableTest formatter configuration.
 * <p>
 * Implements Serializable to support Spotless caching.
 */
public final class TableTestFormatterState implements Serializable {

    @Serial
    private static final long serialVersionUID = 4L;

    private final IndentType indentType;
    private final int indentSize;

    /**
     * Creates a new TableTestFormatterState with default configuration (space indentation, indent size of 4).
     */
    public TableTestFormatterState() {
        this(IndentType.SPACE, 4);
    }

    /**
     * Creates a new TableTestFormatterState with specified indent size and default space indentation.
     *
     * @param indentSize the number of indent characters to add (must be non-negative)
     * @throws IllegalArgumentException if indentSize is negative
     */
    public TableTestFormatterState(int indentSize) {
        this(IndentType.SPACE, indentSize);
    }

    /**
     * Creates a new TableTestFormatterState with specified indent type and indent size.
     *
     * @param indentType the type of indentation to use (SPACE or TAB)
     * @param indentSize the number of indent characters to add (spaces or tabs depending on indent type, must be non-negative)
     * @throws IllegalArgumentException if indentType is null or indentSize is negative
     */
    public TableTestFormatterState(IndentType indentType, int indentSize) {
        if (indentType == null) {
            throw new IllegalArgumentException("indentType must not be null");
        }
        if (indentSize < 0) {
            throw new IllegalArgumentException("indentSize must not be negative, got: " + indentSize);
        }
        this.indentType = indentType;
        this.indentSize = indentSize;
    }

    /**
     * Gets the configured indent type.
     *
     * @return the type of indentation to preserve (SPACE or TAB)
     */
    public IndentType indentType() {
        return indentType;
    }

    /**
     * Gets the configured indent size.
     *
     * @return the number of indent characters to add (spaces or tabs depending on indent type)
     */
    public int indentSize() {
        return indentSize;
    }
}
