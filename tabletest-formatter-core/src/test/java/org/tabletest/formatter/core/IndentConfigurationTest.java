package org.tabletest.formatter.core;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.io.TempDir;
import org.tabletest.formatter.config.Config;
import org.tabletest.formatter.config.EditorConfigProvider;
import org.tabletest.formatter.config.IndentStyle;
import org.tabletest.junit.Description;
import org.tabletest.junit.TableTest;
import org.tabletest.junit.TypeConverter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Lives in the core test package, not beside {@link EditorConfigProvider} in
 * org.tabletest.formatter.config, so the published spec stays one flat list of features in
 * pipeline order. A published rule in a second package moves the report root up and silently
 * un-matches every name in the reading order — see
 * products/formatter/gotchas/a-second-published-package-restructures-the-spec.md. The test is
 * black-box against the public lookupConfig, so the package it sits in is free to choose.
 */
@DisplayName("Indent configuration")
public class IndentConfigurationTest {

    @TempDir
    Path projectRoot;

    private final EditorConfigProvider provider = new EditorConfigProvider();

    @DisplayName("The indent comes from the nearest .editorconfig, or from the caller's default")
    @Description("""
            The formatter carries no indent setting of its own: it asks EditorConfig, the
            convention the surrounding project already states. The search starts in the source
            file's own directory and walks up, and the indent_style and indent_size it finds
            become the style and size the Indentation rules apply. Anything the search cannot
            use — no file, a file it cannot read, or one whose sections do not cover this kind
            of file — leaves the caller's default in force, so a broken .editorconfig never
            fails a build. Each row writes the file it shows into a temporary project holding
            one Java source file. An indent is written style:size.
            """)
    @TableTest("""
        Scenario                    | Config file lines                                                     | Sits     | Caller's default | Indent used?
        Two-space Java indent       | ['root = true', '[*.java]', 'indent_style = space', 'indent_size = 2'] | BESIDE   | space:4          | space:2
        Tabs, one per level         | ['root = true', '[*.java]', 'indent_style = tab', 'indent_size = 1']   | BESIDE   | space:4          | tab:1
        Setting from a parent       | ['root = true', '[*.java]', 'indent_style = tab', 'indent_size = 1']   | ANCESTOR | space:4          | tab:1
        A section for other files   | ['root = true', '[*.kt]', 'indent_style = tab', 'indent_size = 1']     | BESIDE   | space:4          | space:4
        No .editorconfig at all     | []                                                                    | NOWHERE  | space:4          | space:4
        A file that cannot be read  | ['this is not valid editorconfig', '[unclosed section']               | BESIDE   | space:4          | space:4
        Caller asking for no indent | []                                                                    | NOWHERE  | space:0          | space:0
        """)
    void resolvesTheIndentForASourceFile(
            List<String> configFileLines, ConfigFileLocation sits, Config callersDefault, Config indentUsed)
            throws IOException {

        Path sourceFile = sourceFileIn(sits, configFileLines);

        assertThat(provider.lookupConfig(sourceFile, callersDefault)).isEqualTo(indentUsed);
    }

    /** Where a project keeps the .editorconfig that governs a source file. */
    enum ConfigFileLocation {
        BESIDE,
        ANCESTOR,
        NOWHERE
    }

    /**
     * Writes a Java source file into the temporary project, with the given .editorconfig lines
     * in the place named. The project root always carries a bare stopper, so the search cannot
     * walk out into the real filesystem and read whatever this machine happens to have.
     */
    private Path sourceFileIn(ConfigFileLocation sits, List<String> configFileLines) throws IOException {
        Files.writeString(projectRoot.resolve(".editorconfig"), "root = true\n");
        Path module = Files.createDirectories(projectRoot.resolve("module"));
        Path sourceDir = Files.createDirectories(module.resolve("src"));

        switch (sits) {
            case BESIDE -> writeConfigFile(sourceDir, configFileLines);
            case ANCESTOR -> writeConfigFile(module, configFileLines);
            case NOWHERE -> {}
        }
        return Files.writeString(sourceDir.resolve("Test.java"), "class Test {}\n");
    }

    private void writeConfigFile(Path directory, List<String> lines) throws IOException {
        Files.writeString(directory.resolve(".editorconfig"), String.join("\n", lines) + "\n");
    }

    @TypeConverter
    public static Config parseIndent(String value) {
        String[] parts = value.split(":");
        return new Config(IndentStyle.valueOf(parts[0].toUpperCase()), Integer.parseInt(parts[1]));
    }
}
