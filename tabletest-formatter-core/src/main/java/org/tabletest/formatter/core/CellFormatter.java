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
package org.tabletest.formatter.core;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.stream.Collectors.joining;

/**
 * Formats cell values for display in TableTest tables.
 *
 * <p>Converts various object types (primitives, collections, maps) to their
 * string representation according to TableTest formatting conventions.
 */
class CellFormatter {

    String formatCell(Object cell) {
        return switch (cell) {
            case null -> "";
            case List<?> list -> formatList(list);
            case Set<?> set -> formatSet(set);
            case Map<?, ?> map -> formatMap(map);
            default -> cell.toString();
        };
    }

    private String formatList(List<?> list) {
        return list.isEmpty() ? "[]" : list.stream().map(this::formatCell).collect(joining(", ", "[", "]"));
    }

    private String formatSet(Set<?> set) {
        return set.isEmpty() ? "{}" : set.stream().map(this::formatCell).collect(joining(", ", "{", "}"));
    }

    private String formatMap(Map<?, ?> map) {
        return map.isEmpty()
                ? "[:]"
                : map.entrySet().stream()
                        .map(entry -> entry.getKey() + ": " + formatCell(entry.getValue()))
                        .collect(joining(", ", "[", "]"));
    }
}
