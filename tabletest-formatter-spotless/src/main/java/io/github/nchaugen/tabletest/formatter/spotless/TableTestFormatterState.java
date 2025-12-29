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
 * Currently empty but provides extensibility for future configuration options.
 * Implements Serializable to support Spotless caching.
 */
public final class TableTestFormatterState implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Creates a new TableTestFormatterState with default configuration.
     */
    public TableTestFormatterState() {
        // Empty for now - future configuration options can be added here
    }
}
