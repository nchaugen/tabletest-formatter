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
import io.github.nchaugen.tabletest.formatter.core.SourceFileFormatter;
import io.github.nchaugen.tabletest.formatter.core.TableTestFormatter;

import java.io.File;

/**
 * Spotless formatter function for TableTest tables.
 * <p>
 * Handles formatting of:
 * <ul>
 *   <li>Standalone .table files - formats entire file content</li>
 *   <li>Java/Kotlin files - extracts and formats @TableTest annotations</li>
 *   <li>Other files - returns unchanged</li>
 * </ul>
 */
public final class TableTestFormatterFunc implements FormatterFunc.NeedsFile {

    private final TableTestFormatterState state;
    private final TableTestFormatter tableFormatter;
    private final SourceFileFormatter sourceFormatter;

    /**
     * Creates a new formatter function with the given state.
     *
     * @param state the formatter configuration state
     */
    public TableTestFormatterFunc(TableTestFormatterState state) {
        this.state = state;
        this.tableFormatter = new TableTestFormatter();
        this.sourceFormatter = new SourceFileFormatter();
    }

    @Override
    public String applyWithFile(String rawUnix, File file) throws Exception {
        String fileName = file.getName();

        if (fileName.endsWith(".table")) {
            return formatStandaloneTableFile(rawUnix);
        } else if (fileName.endsWith(".java") || fileName.endsWith(".kt")) {
            return formatSourceFile(rawUnix);
        } else {
            return rawUnix;
        }
    }

    private String formatStandaloneTableFile(String content) {
        String formatted = tableFormatter.format(content);
        return formatted.equals(content) ? null : formatted;
    }

    private String formatSourceFile(String content) {
        String formatted = sourceFormatter.format(content);
        return formatted.equals(content) ? null : formatted;
    }
}
