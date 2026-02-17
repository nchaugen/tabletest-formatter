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

import org.ec4j.core.Cache.Caches;
import org.ec4j.core.Resource.Resources;
import org.ec4j.core.ResourceProperties;
import org.ec4j.core.ResourcePropertiesService;
import org.ec4j.core.model.Property;
import org.ec4j.core.model.PropertyType;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Objects;

/**
 * Service for looking up formatting configuration from .editorconfig files.
 *
 * <p>This service maintains a shared cache of EditorConfig files for performance.
 * It searches for .editorconfig files in the file's directory and parent directories
 * according to the EditorConfig specification.
 *
 * <p><strong>Thread safety:</strong> This class is thread-safe. Multiple threads can
 * safely call {@link #lookupConfig(Path, Config)} concurrently.
 *
 * <p><strong>Usage pattern:</strong>
 * <pre>
 * // Create service once (e.g., as static field)
 * EditorConfigProvider service = new EditorConfigProvider();
 *
 * // Lookup config for each file
 * Config config = service.lookupConfig(
 *     filePath,
 *     Config.SPACES_4
 * );
 * </pre>
 *
 * <p><strong>Supported properties:</strong>
 * <ul>
 *   <li>{@code indent_style} - mapped to {@link IndentStyle} (space/tab)</li>
 *   <li>{@code indent_size} - number of indent characters per level</li>
 * </ul>
 *
 * <p><strong>Example .editorconfig:</strong>
 * <pre>
 * [*.java]
 * indent_style = space
 * indent_size = 4
 *
 * [*.kt]
 * indent_style = tab
 * indent_size = 1
 * </pre>
 */
public final class EditorConfigProvider {

    private final ResourcePropertiesService service;

    /**
     * Creates a new EditorConfig lookup service with permanent caching.
     *
     * <p>The cache persists for the lifetime of this service instance.
     * Create one instance and reuse it across multiple lookups for best performance.
     */
    public EditorConfigProvider() {
        this.service =
                ResourcePropertiesService.builder().cache(Caches.permanent()).build();
    }

    /**
     * Looks up formatting configuration for the specified file.
     *
     * <p>Searches for .editorconfig files in the file's directory and parent
     * directories. If no .editorconfig is found or properties are not specified,
     * returns the provided defaults.
     *
     * @param filePath the path to the file being formatted (not null)
     * @param defaults the default configuration to use if no .editorconfig found (not null)
     * @return the resolved configuration (never null)
     * @throws NullPointerException if either parameter is null
     */
    public Config lookupConfig(Path filePath, Config defaults) {
        Objects.requireNonNull(filePath, "filePath must not be null");
        Objects.requireNonNull(defaults, "defaults must not be null");

        try {
            ResourceProperties properties = service.queryProperties(Resources.ofPath(filePath, StandardCharsets.UTF_8));

            IndentStyle indentStyle = parseIndentStyle(properties, defaults.indentStyle());
            int indentSize = parseIndentSize(properties, defaults.indentSize());

            return new Config(indentStyle, indentSize);
        } catch (Exception e) {
            // Graceful degradation: return defaults on any error
            return defaults;
        }
    }

    private IndentStyle parseIndentStyle(ResourceProperties properties, IndentStyle defaultValue) {
        Property indentStyleProperty = properties.getProperties().get(PropertyType.indent_style.getName());
        if (indentStyleProperty == null) {
            return defaultValue;
        }

        return switch (indentStyleProperty.getSourceValue().toLowerCase()) {
            case "tab" -> IndentStyle.TAB;
            case "space" -> IndentStyle.SPACE;
            default -> defaultValue;
        };
    }

    private int parseIndentSize(ResourceProperties properties, int defaultValue) {
        Property indentSizeProperty = properties.getProperties().get(PropertyType.indent_size.getName());
        if (indentSizeProperty == null) {
            return defaultValue;
        }

        try {
            int size = Integer.parseInt(indentSizeProperty.getSourceValue());
            return size >= 0 ? size : defaultValue;
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}
