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
package io.github.nchaugen.tabletest.formatter.cli;

import io.github.nchaugen.tabletest.formatter.core.SourceFileFormatter;
import io.github.nchaugen.tabletest.formatter.core.TableTestFormatter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Formats individual files containing TableTest tables.
 * Handles both standalone .table files and source files (.java, .kt) with @TableTest annotations.
 */
public class FileFormatter {

    private final TableTestFormatter tableFormatter;
    private final SourceFileFormatter sourceFormatter;

    public FileFormatter() {
        this.tableFormatter = new TableTestFormatter();
        this.sourceFormatter = new SourceFileFormatter();
    }

    /**
     * Formats a file and returns the result.
     *
     * @param file the file to format
     * @return formatting result with changed flag and formatted content
     * @throws IOException if an I/O error occurs
     */
    public FormattingResult format(Path file) throws IOException {
        return format(file, 0);
    }

    /**
     * Formats a file with specified indentation and returns the result.
     *
     * @param file       the file to format
     * @param indentSize the number of spaces per indent level (0 for no indentation)
     * @return formatting result with changed flag and formatted content
     * @throws IOException if an I/O error occurs
     */
    public FormattingResult format(Path file, int indentSize) throws IOException {
        String content = Files.readString(file);
        String fileName = file.getFileName().toString();

        if (fileName.endsWith(".table")) {
            return formatStandaloneTableFile(file, content);
        } else if (fileName.endsWith(".java") || fileName.endsWith(".kt")) {
            return formatSourceFile(file, content, indentSize);
        } else {
            return new FormattingResult(file, false, content);
        }
    }

    private FormattingResult formatStandaloneTableFile(Path file, String content) {
        String formatted = tableFormatter.format(content);
        boolean changed = !formatted.equals(content);
        return new FormattingResult(file, changed, formatted);
    }

    private FormattingResult formatSourceFile(Path file, String content, int indentSize) {
        String formatted = sourceFormatter.format(content, indentSize);
        boolean changed = !formatted.equals(content);
        return new FormattingResult(file, changed, formatted);
    }
}
