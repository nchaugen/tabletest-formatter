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
package org.tabletest.formatter.cli;

import org.tabletest.formatter.config.Config;
import org.tabletest.formatter.config.EditorConfigProvider;
import org.tabletest.formatter.core.SourceFileFormatter;
import org.tabletest.formatter.core.TableTestFormatter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

/**
 * Formats individual files containing TableTest tables.
 *
 * <p>Handles both standalone .table files and source files (.java, .kt) with @TableTest annotations.
 *
 * <p><strong>Configuration:</strong> Reads formatting settings from .editorconfig files.
 * Searches for .editorconfig in the file's directory and parent directories. If no
 * .editorconfig is found, uses sensible defaults (4 spaces for source files, no indentation
 * for .table files).
 */
public class FileFormatter {

    private final EditorConfigProvider configProvider;
    private final TableTestFormatter tableFormatter;
    private final SourceFileFormatter sourceFormatter;

    public FileFormatter() {
        this.configProvider = new EditorConfigProvider();
        this.tableFormatter = new TableTestFormatter();
        this.sourceFormatter = new SourceFileFormatter();
    }

    /**
     * Formats a file using configuration from .editorconfig files.
     *
     * @param file the file to format
     * @return formatting result with changed flag and formatted content
     * @throws IOException if an I/O error occurs
     */
    public FormattingResult format(Path file) throws IOException {
        String content = Files.readString(file);
        String fileName = file.getFileName().toString();

        if (fileName.endsWith(".table")) {
            return formatStandaloneTableFile(file, content);
        } else if (fileName.endsWith(".java") || fileName.endsWith(".kt")) {
            return formatSourceFile(file, content);
        } else {
            return new FormattingResult(file, false, content);
        }
    }

    private FormattingResult formatStandaloneTableFile(Path file, String content) {
        Objects.requireNonNull(content, "content must not be null");

        Config config = configProvider.lookupConfig(file, Config.NO_INDENT);

        String formatted = tableFormatter.format(content, "", config);
        boolean changed = !formatted.equals(content);
        return new FormattingResult(file, changed, formatted);
    }

    private FormattingResult formatSourceFile(Path file, String content) {
        Config config = configProvider.lookupConfig(file, Config.SPACES_4);

        String formatted = sourceFormatter.format(content, config);
        boolean changed = !formatted.equals(content);
        return new FormattingResult(file, changed, formatted);
    }
}
