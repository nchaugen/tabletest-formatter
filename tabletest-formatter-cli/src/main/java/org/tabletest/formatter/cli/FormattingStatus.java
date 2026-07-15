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

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Aggregates formatting results across multiple files.
 * Tracks how many files were checked, which files were changed, and which files failed.
 */
public class FormattingStatus {

    private int filesChecked = 0;
    private int filesChanged = 0;
    private final List<Path> changedFiles = new ArrayList<>();
    private final List<Path> failedFiles = new ArrayList<>();

    /**
     * Adds a formatting result to this status.
     *
     * @param result the result to add
     */
    public void addResult(FormattingResult result) {
        filesChecked++;
        if (result.changed()) {
            filesChanged++;
            changedFiles.add(result.file());
        }
    }

    /**
     * Records a file that could not be formatted.
     *
     * @param file the file that failed
     */
    public void addFailure(Path file) {
        filesChecked++;
        failedFiles.add(file);
    }

    /**
     * Returns whether any files were changed.
     *
     * @return true if at least one file was changed
     */
    public boolean hasChanges() {
        return filesChanged > 0;
    }

    /**
     * Returns the number of files checked.
     *
     * @return the number of files checked
     */
    public int filesChecked() {
        return filesChecked;
    }

    /**
     * Returns the number of files changed.
     *
     * @return the number of files changed
     */
    public int filesChanged() {
        return filesChanged;
    }

    /**
     * Returns the list of files that were changed.
     *
     * @return immutable list of changed files
     */
    public List<Path> changedFiles() {
        return List.copyOf(changedFiles);
    }

    /**
     * Returns whether any files failed to format.
     *
     * @return true if at least one file failed
     */
    public boolean hasFailures() {
        return !failedFiles.isEmpty();
    }

    /**
     * Returns the number of files that failed to format.
     *
     * @return the number of failed files
     */
    public int filesFailed() {
        return failedFiles.size();
    }

    /**
     * Returns the list of files that failed to format.
     *
     * @return immutable list of failed files
     */
    public List<Path> failedFiles() {
        return List.copyOf(failedFiles);
    }
}
