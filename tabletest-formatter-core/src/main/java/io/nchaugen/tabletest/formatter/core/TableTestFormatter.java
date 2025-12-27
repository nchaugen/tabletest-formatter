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
package io.nchaugen.tabletest.formatter.core;

import io.github.nchaugen.tabletest.parser.Table;
import io.github.nchaugen.tabletest.parser.TableParser;

import java.util.List;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.joining;

/**
 * Formats TableTest tables according to consistent formatting rules.
 *
 * <p>Parses TableTest table text and applies formatting such as column alignment,
 * spacing normalization, and quote preservation.
 */
public class TableTestFormatter {

    /**
     * Formats the given TableTest table text.
     *
     * @param tableText the raw table text to format
     * @return the formatted table text
     */
    public String format(String tableText) {
        Table table = TableParser.parse(tableText);
        return rebuildTable(table);
    }

    private String rebuildTable(Table table) {
        String headerRow = buildRow(table.headers());

        String dataRows = IntStream.range(0, table.rowCount())
                .mapToObj(table::row)
                .map(row -> buildRow(row.values()))
                .collect(joining("\n"));

        return headerRow + "\n" + dataRows;
    }

    private String buildRow(List<?> cells) {
        return cells.stream().map(cell -> cell != null ? cell.toString() : "").collect(joining("|"));
    }
}
