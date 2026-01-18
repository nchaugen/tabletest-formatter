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

import java.util.Objects;

/**
 * Immutable configuration provider with static values.
 *
 * <p>This implementation is typically used when configuration comes from:
 * <ul>
 *   <li>CLI arguments (e.g., {@code --indent-size=2 --indent-type=space})</li>
 *   <li>Build script parameters (e.g., Spotless configuration in build.gradle)</li>
 *   <li>Programmatic API calls with explicit configuration</li>
 * </ul>
 *
 * <p><strong>Example usage:</strong>
 * <pre>
 * // Use default configuration (4 spaces)
 * ConfigProvider config = StaticConfigProvider.DEFAULT;
 *
 * // Create custom configuration
 * ConfigProvider config = new StaticConfigProvider(IndentType.TAB, 1);
 * </pre>
 */
public record StaticConfigProvider(IndentType indentType, int indentSize) implements ConfigProvider {

    /**
     * Default configuration: 4 spaces for indentation.
     */
    public static final StaticConfigProvider DEFAULT = new StaticConfigProvider(IndentType.SPACE, 4);

    /**
     * No indentation for tables in .table files
     */
    public static final StaticConfigProvider NO_INDENT = new StaticConfigProvider(IndentType.SPACE, 0);

    /**
     * Creates a static configuration provider with validation.
     *
     * @param indentType the type of indentation (must not be null)
     * @param indentSize the number of indent characters (must be >= 0)
     * @throws NullPointerException     if indentType is null
     * @throws IllegalArgumentException if indentSize is negative
     */
    public StaticConfigProvider {
        Objects.requireNonNull(indentType, "indentType must not be null");
        if (indentSize < 0) {
            throw new IllegalArgumentException("indentSize must not be negative: " + indentSize);
        }
    }
}
