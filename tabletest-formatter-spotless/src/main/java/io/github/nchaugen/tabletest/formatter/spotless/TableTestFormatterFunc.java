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

import com.diffplug.spotless.FormatterFunc;
import io.github.nchaugen.tabletest.formatter.config.Config;
import io.github.nchaugen.tabletest.formatter.config.EditorConfigProvider;
import io.github.nchaugen.tabletest.formatter.core.SourceFileFormatter;
import io.github.nchaugen.tabletest.formatter.core.TableTestFormatter;

import java.io.File;
import java.util.Objects;

/**
 * Spotless formatter function for TableTest tables.
 *
 * <p>Handles formatting of:
 * <ul>
 *   <li>Standalone .table files - formats entire file content</li>
 *   <li>Java/Kotlin files - extracts and formats @TableTest annotations</li>
 *   <li>Other files - returns unchanged</li>
 * </ul>
 *
 * <p><strong>Configuration:</strong> Reads formatting settings from .editorconfig files.
 * Searches for .editorconfig in the file's directory and parent directories.
 *
 * <h2>Error Handling</h2>
 * <p>Inherits graceful degradation from {@link TableTestFormatter}:
 * <ul>
 *   <li>Malformed tables return original input unchanged</li>
 *   <li>Parse failures never break the build</li>
 *   <li>Files with no @TableTest annotations are unchanged</li>
 *   <li>Missing .editorconfig files fall back to defaults</li>
 * </ul>
 *
 * <p>The {@code applyWithFile} method declares {@code throws Exception} per
 * Spotless API contract, but in practice only propagates exceptions for
 * programming errors (null inputs), not formatting failures.
 */
public final class TableTestFormatterFunc implements FormatterFunc.NeedsFile {

    // Shared across all instances for EditorConfig caching
    private static final EditorConfigProvider CONFIG_PROVIDER = new EditorConfigProvider();

    private final TableTestFormatter tableFormatter;
    private final SourceFileFormatter sourceFormatter;

    /**
     * Creates a new formatter function.
     */
    public TableTestFormatterFunc() {
        this.tableFormatter = new TableTestFormatter();
        this.sourceFormatter = new SourceFileFormatter();
    }

    /**
     * Applies formatting to the file content based on file extension.
     *
     * @param rawUnix the file content in Unix format (LF line endings)
     * @param file the file being formatted
     * @return the formatted content, or null if no changes were made
     * @throws Exception per Spotless API contract (in practice, only for programming errors)
     */
    @Override
    public String applyWithFile(String rawUnix, File file) throws Exception {
        String fileName = file.getName();

        if (fileName.endsWith(".table")) {
            return formatStandaloneTableFile(rawUnix, file);
        } else if (fileName.endsWith(".java") || fileName.endsWith(".kt")) {
            return formatSourceFile(rawUnix, file);
        } else {
            return rawUnix;
        }
    }

    private String formatStandaloneTableFile(String content, File file) {
        Objects.requireNonNull(content, "content must not be null");

        Config config = CONFIG_PROVIDER.lookupConfig(file.toPath(), Config.NO_INDENT);

        String formatted = tableFormatter.format(content, "", config);
        return formatted.equals(content) ? null : formatted;
    }

    private String formatSourceFile(String content, File file) {
        Config config = CONFIG_PROVIDER.lookupConfig(file.toPath(), Config.SPACES_4);

        String formatted = sourceFormatter.format(content, config);
        return formatted.equals(content) ? null : formatted;
    }
}
