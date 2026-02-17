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
package org.tabletest.formatter.spotless;

import com.diffplug.spotless.FormatterStep;

/**
 * Factory for creating Spotless FormatterStep instances for TableTest formatting.
 *
 * <p>This formatter reads configuration from .editorconfig files rather than accepting
 * parameters. Place a .editorconfig file in your project root or source directories:
 *
 * <pre>
 * [*.java]
 * indent_style = space
 * indent_size = 4
 *
 * [*.kt]
 * indent_style = space
 * indent_size = 4
 *
 * [*.table]
 * indent_style = space
 * indent_size = 0
 * </pre>
 *
 * <p>See <a href="https://editorconfig.org">editorconfig.org</a> for specification.
 */
public final class TableTestFormatterStep {

    private static final String NAME = "tabletest";

    private TableTestFormatterStep() {
        // Utility class - prevent instantiation
    }

    /**
     * Creates a new FormatterStep for TableTest formatting.
     *
     * <p>Formatting settings are read from .editorconfig files. If no .editorconfig
     * is found, defaults to 4 spaces for Java/Kotlin files and no indentation for
     * .table files.
     *
     * @return a configured FormatterStep instance
     */
    public static FormatterStep create() {
        // Use empty string as state (config comes from .editorconfig files)
        return FormatterStep.create(NAME, "", state -> new TableTestFormatterFunc());
    }
}
