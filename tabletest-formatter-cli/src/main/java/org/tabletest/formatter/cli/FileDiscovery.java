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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

/**
 * Discovers files with supported extensions (.table, .java, .kt) from given paths.
 * Recursively traverses directories and returns a sorted list of matching files.
 */
public class FileDiscovery {

    private static final Set<String> SUPPORTED_EXTENSIONS = Set.of(".table", ".java", ".kt");

    /**
     * Discovers all files with supported extensions from the given input paths.
     * If a path is a file, it is included if it has a supported extension.
     * If a path is a directory, it is recursively traversed.
     *
     * @param inputPaths paths to files or directories to discover
     * @return sorted list of discovered files with supported extensions
     * @throws IOException if an I/O error occurs during discovery
     */
    public List<Path> discover(List<Path> inputPaths) throws IOException {
        return inputPaths.stream()
                .flatMap(this::discoverFromPath)
                .filter(this::hasSupportedExtension)
                .sorted()
                .collect(toList());
    }

    private Stream<Path> discoverFromPath(Path path) {
        try {
            return Files.isDirectory(path) ? walkDirectory(path) : Stream.of(path);
        } catch (IOException e) {
            throw new RuntimeException("Failed to discover files from: " + path, e);
        }
    }

    private Stream<Path> walkDirectory(Path directory) throws IOException {
        try (Stream<Path> paths = Files.walk(directory)) {
            return paths.filter(Files::isRegularFile).toList().stream();
        }
    }

    private boolean hasSupportedExtension(Path path) {
        String fileName = path.getFileName().toString();
        return SUPPORTED_EXTENSIONS.stream().anyMatch(fileName::endsWith);
    }
}
