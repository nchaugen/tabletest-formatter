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
package org.tabletest.formatter.config;

/**
 * Represents the type of indentation used in source code.
 */
public enum IndentStyle {
    /**
     * Indentation using space characters.
     */
    SPACE(" "),

    /**
     * Indentation using tab characters.
     */
    TAB("\t");

    private final String character;

    IndentStyle(String character) {
        this.character = character;
    }

    /**
     * Repeats the indentation character the specified number of times.
     *
     * @param count the number of times to repeat the indentation character
     * @return the indentation string repeated count times
     */
    public String repeat(int count) {
        return character.repeat(count);
    }
}
