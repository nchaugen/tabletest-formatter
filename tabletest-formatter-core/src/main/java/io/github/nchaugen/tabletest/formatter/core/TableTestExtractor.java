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

import java.util.List;

/**
 * Extracts @TableTest annotations from source code.
 * <p>
 * Implementations may use different parsing strategies (regex, AST, tree-sitter)
 * to locate @TableTest annotations with their table content.
 */
public interface TableTestExtractor {

    /**
     * Finds all @TableTest annotations in the provided source code.
     *
     * @param sourceCode the Java or Kotlin source code to search (must not be null)
     * @return list of all @TableTest matches found (empty if none found), with position information
     * @throws NullPointerException if sourceCode is null
     */
    List<TableMatch> findAll(String sourceCode);
}
