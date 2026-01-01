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
 * Immutable value object representing a matched @TableTest annotation in source code.
 *
 * @param originalText the raw table text as found in the source
 * @param startIndex   the start position in the source file
 * @param endIndex     the end position in the source file
 * @param baseIndent   the number of leading spaces before the @TableTest annotation
 */
public record TableMatch(String originalText, int startIndex, int endIndex, int baseIndent) {}
