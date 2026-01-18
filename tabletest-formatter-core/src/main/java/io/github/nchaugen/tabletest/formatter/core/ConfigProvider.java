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

/**
 * Provides formatting configuration for TableTest tables.
 *
 * <p>This interface allows different sources of configuration (static values,
 * EditorConfig files, IDE settings, etc.) to be used interchangeably during
 * formatting operations.
 *
 * <p>Implementations should provide immutable configuration values that remain
 * constant for the lifetime of the provider instance.
 *
 * <p><strong>Example implementations:</strong>
 * <ul>
 *   <li>{@code StaticConfigProvider} - hardcoded values (CLI args, build scripts)</li>
 *   <li>{@code EditorConfigProvider} - reads from .editorconfig files</li>
 *   <li>{@code MergedConfigProvider} - combines multiple providers with precedence</li>
 * </ul>
 */
public interface ConfigProvider {

    /**
     * Returns the type of indentation to use when formatting.
     *
     * @return the indent type (SPACE or TAB)
     */
    IndentType indentType();

    /**
     * Returns the number of indent characters to add for each indentation level.
     *
     * @return the indent size (typically 2 or 4 for spaces, 1 for tabs)
     */
    int indentSize();
}
