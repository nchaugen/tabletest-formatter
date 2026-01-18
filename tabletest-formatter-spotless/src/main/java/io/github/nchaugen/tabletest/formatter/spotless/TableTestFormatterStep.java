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

import com.diffplug.spotless.FormatterStep;
import io.github.nchaugen.tabletest.formatter.config.IndentType;

/**
 * Factory for creating Spotless FormatterStep instances for TableTest formatting.
 * <p>
 * This class provides a static factory method to create a FormatterStep that formats
 * TableTest tables in standalone .table files and @TableTest annotations in Java/Kotlin files.
 */
public final class TableTestFormatterStep {

    private static final String NAME = "tabletest";

    private TableTestFormatterStep() {
        // Utility class - prevent instantiation
    }

    /**
     * Creates a new FormatterStep for TableTest formatting with default space indentation and indent size of 4.
     *
     * @return a configured FormatterStep instance
     */
    public static FormatterStep create() {
        return create(4);
    }

    /**
     * Creates a new FormatterStep for TableTest formatting with specified indent size and default space indentation.
     *
     * @param indentSize the number of indent characters to add (spaces or tabs depending on indent type)
     * @return a configured FormatterStep instance
     * @throws IllegalArgumentException if indentSize is negative
     */
    public static FormatterStep create(int indentSize) {
        return create(IndentType.SPACE, indentSize);
    }

    /**
     * Creates a new FormatterStep for TableTest formatting with specified indent type and indent size.
     *
     * @param indentType the type of indentation to use (SPACE or TAB)
     * @param indentSize the number of indent characters to add (spaces or tabs depending on indent type)
     * @return a configured FormatterStep instance
     * @throws IllegalArgumentException if indentType is null or indentSize is negative
     */
    public static FormatterStep create(IndentType indentType, int indentSize) {
        TableTestFormatterState state = new TableTestFormatterState(indentType, indentSize);
        return FormatterStep.createLazy(NAME, () -> state, TableTestFormatterFunc::new);
    }
}
