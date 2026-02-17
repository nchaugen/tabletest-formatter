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

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * Command-line interface for formatting TableTest tables.
 *
 * <p>Formats TableTest tables in:
 * <ul>
 *   <li>Standalone .table files</li>
 *   <li>Java files with @TableTest annotations</li>
 *   <li>Kotlin files with @TableTest annotations</li>
 * </ul>
 *
 * <p><strong>Configuration:</strong> Reads formatting settings from .editorconfig files.
 * Place a .editorconfig file in your project root or source directories:
 * <pre>
 * [*.java]
 * indent_style = space
 * indent_size = 4
 *
 * [*.table]
 * indent_style = space
 * indent_size = 0
 * </pre>
 *
 * <p>Exit codes:
 * <ul>
 *   <li>0: Success (check: no changes needed, apply: formatting succeeded)</li>
 *   <li>1: Failure (check: changes needed OR errors, apply: errors)</li>
 *   <li>2: Invalid usage (handled by picocli)</li>
 * </ul>
 */
@Command(
        name = "tabletest-format",
        description = "Format TableTest tables in Java/Kotlin/.table files",
        mixinStandardHelpOptions = true,
        version = "0.1.0-SNAPSHOT")
public class TableTestFormatterCli implements Callable<Integer> {

    @Parameters(paramLabel = "PATH", arity = "1..*", description = "Files or directories to format")
    private List<Path> paths;

    @Option(
            names = {"-c", "--check"},
            description = "Check if files need formatting without modifying them")
    private boolean checkMode = false;

    @Option(
            names = {"-v", "--verbose"},
            description = "Print detailed output for each file")
    private boolean verbose = false;

    private final FileDiscovery fileDiscovery;
    private final FileFormatter fileFormatter;

    public TableTestFormatterCli() {
        this.fileDiscovery = new FileDiscovery();
        this.fileFormatter = new FileFormatter();
    }

    // Package-private constructor for testing
    TableTestFormatterCli(FileDiscovery fileDiscovery, FileFormatter fileFormatter) {
        this.fileDiscovery = fileDiscovery;
        this.fileFormatter = fileFormatter;
    }

    @Override
    public Integer call() {
        try {
            List<Path> files = fileDiscovery.discover(paths);

            if (files.isEmpty()) {
                System.out.println("No files found to format");
                return 0;
            }

            FormattingStatus status = formatFiles(files);

            printSummary(status);

            return determineExitCode(status);

        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
            return 1;
        }
    }

    private FormattingStatus formatFiles(List<Path> files) throws IOException {
        FormattingStatus status = new FormattingStatus();

        for (Path file : files) {
            FormattingResult result = fileFormatter.format(file);
            status.addResult(result);

            if (verbose) {
                printFileStatus(result);
            }

            if (!checkMode && result.changed()) {
                writeFormattedContent(file, result.formattedContent());
            }
        }

        return status;
    }

    private void printFileStatus(FormattingResult result) {
        String statusMessage = result.changed() ? "needs formatting" : "already formatted";
        System.out.println(result.file() + " - " + statusMessage);
    }

    private void writeFormattedContent(Path file, String content) throws IOException {
        Path tempFile = Files.createTempFile(file.getParent(), ".tabletest-format-", ".tmp");
        try {
            Files.writeString(tempFile, content);
            Files.move(tempFile, file, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException e) {
            Files.deleteIfExists(tempFile);
            throw new IOException("Failed to write formatted content to " + file, e);
        }
    }

    private void printSummary(FormattingStatus status) {
        String mode = checkMode ? "Checked" : "Formatted";
        System.out.println(mode + " " + status.filesChecked() + " files");

        if (status.hasChanges()) {
            if (checkMode) {
                System.out.println(status.filesChanged() + " files need formatting:");
                status.changedFiles().forEach(file -> System.out.println("  " + file));
            } else {
                System.out.println(status.filesChanged() + " files were reformatted");
            }
        } else {
            System.out.println("All files are already formatted");
        }
    }

    private int determineExitCode(FormattingStatus status) {
        return status.hasChanges() && checkMode ? 1 : 0;
    }

    public static void main(String[] args) {
        System.exit(new CommandLine(new TableTestFormatterCli()).execute(args));
    }
}
