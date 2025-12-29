package io.github.nchaugen.tabletest.formatter.cli;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class FileDiscoveryTest {

    @Test
    void shouldDiscoverSingleTableFile(@TempDir Path tempDir) throws IOException {
        Path tableFile = tempDir.resolve("test.table");
        Files.writeString(tableFile, "name|age\nAlice|30\n");

        FileDiscovery discovery = new FileDiscovery();
        List<Path> discovered = discovery.discover(List.of(tableFile));

        assertThat(discovered).containsExactly(tableFile);
    }

    @Test
    void shouldDiscoverJavaAndKotlinFiles(@TempDir Path tempDir) throws IOException {
        Path javaFile = tempDir.resolve("Test.java");
        Path kotlinFile = tempDir.resolve("Test.kt");
        Path tableFile = tempDir.resolve("data.table");

        Files.writeString(javaFile, "public class Test {}");
        Files.writeString(kotlinFile, "class Test");
        Files.writeString(tableFile, "name|age\n");

        FileDiscovery discovery = new FileDiscovery();
        List<Path> discovered = discovery.discover(List.of(tempDir));

        assertThat(discovered).hasSize(3).containsExactlyInAnyOrder(javaFile, kotlinFile, tableFile);
    }

    @Test
    void shouldRecursivelyDiscoverFilesInDirectory(@TempDir Path tempDir) throws IOException {
        Path rootTable = tempDir.resolve("root.table");
        Path subDir = tempDir.resolve("sub");
        Files.createDirectory(subDir);
        Path nestedJava = subDir.resolve("Nested.java");
        Path deepDir = subDir.resolve("deep");
        Files.createDirectory(deepDir);
        Path deepKotlin = deepDir.resolve("Deep.kt");

        Files.writeString(rootTable, "data");
        Files.writeString(nestedJava, "class Nested {}");
        Files.writeString(deepKotlin, "class Deep");

        FileDiscovery discovery = new FileDiscovery();
        List<Path> discovered = discovery.discover(List.of(tempDir));

        assertThat(discovered).hasSize(3).containsExactlyInAnyOrder(rootTable, nestedJava, deepKotlin);
    }

    @Test
    void shouldFilterUnsupportedExtensions(@TempDir Path tempDir) throws IOException {
        Path tableFile = tempDir.resolve("test.table");
        Path javaFile = tempDir.resolve("Test.java");
        Path txtFile = tempDir.resolve("readme.txt");
        Path xmlFile = tempDir.resolve("config.xml");
        Path noExtension = tempDir.resolve("Makefile");

        Files.writeString(tableFile, "data");
        Files.writeString(javaFile, "class Test {}");
        Files.writeString(txtFile, "readme");
        Files.writeString(xmlFile, "<config/>");
        Files.writeString(noExtension, "all:");

        FileDiscovery discovery = new FileDiscovery();
        List<Path> discovered = discovery.discover(List.of(tempDir));

        assertThat(discovered).hasSize(2).containsExactlyInAnyOrder(tableFile, javaFile);
    }

    @Test
    void shouldSortFilesForDeterministicOutput(@TempDir Path tempDir) throws IOException {
        Path file1 = tempDir.resolve("zebra.table");
        Path file2 = tempDir.resolve("alpha.java");
        Path file3 = tempDir.resolve("beta.kt");

        Files.writeString(file1, "data");
        Files.writeString(file2, "class Alpha {}");
        Files.writeString(file3, "class Beta");

        FileDiscovery discovery = new FileDiscovery();
        List<Path> discovered = discovery.discover(List.of(tempDir));

        assertThat(discovered).containsExactly(file2, file3, file1);
    }

    @Test
    void shouldHandleMultipleInputPaths(@TempDir Path tempDir) throws IOException {
        Path dir1 = tempDir.resolve("dir1");
        Path dir2 = tempDir.resolve("dir2");
        Files.createDirectory(dir1);
        Files.createDirectory(dir2);

        Path file1 = dir1.resolve("file1.table");
        Path file2 = dir2.resolve("file2.java");
        Files.writeString(file1, "data");
        Files.writeString(file2, "class Test {}");

        FileDiscovery discovery = new FileDiscovery();
        List<Path> discovered = discovery.discover(List.of(dir1, dir2));

        assertThat(discovered).hasSize(2).containsExactlyInAnyOrder(file1, file2);
    }

    @Test
    void shouldHandleMixOfFilesAndDirectories(@TempDir Path tempDir) throws IOException {
        Path explicitFile = tempDir.resolve("explicit.table");
        Files.writeString(explicitFile, "data");

        Path subDir = tempDir.resolve("sub");
        Files.createDirectory(subDir);
        Path discoveredFile = subDir.resolve("discovered.java");
        Files.writeString(discoveredFile, "class Test {}");

        FileDiscovery discovery = new FileDiscovery();
        List<Path> discovered = discovery.discover(List.of(explicitFile, subDir));

        assertThat(discovered).hasSize(2).containsExactlyInAnyOrder(explicitFile, discoveredFile);
    }
}
