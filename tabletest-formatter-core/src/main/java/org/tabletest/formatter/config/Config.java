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
package org.tabletest.formatter.config;

import java.util.Objects;

/**
 * File formatting configuration
 */
public record Config(IndentStyle indentStyle, int indentSize) {

    /**
     * Default indentation for tables in Java and Kotlin files
     */
    public static final Config SPACES_4 = new Config(IndentStyle.SPACE, 4);

    /**
     * No indentation for tables in .table files
     */
    public static final Config NO_INDENT = new Config(IndentStyle.SPACE, 0);

    /**
     * Creates a configuration with validation.
     *
     * @param indentStyle the type of indentation (must not be null)
     * @param indentSize the number of indent characters (must be >= 0)
     * @throws NullPointerException     if indentStyle is null
     * @throws IllegalArgumentException if indentSize is negative
     */
    public Config {
        Objects.requireNonNull(indentStyle, "indentStyle must not be null");
        if (indentSize < 0) {
            throw new IllegalArgumentException("indentSize must not be negative: " + indentSize);
        }
    }
}
