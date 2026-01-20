package io.github.nchaugen.tabletest.formatter.config;

import org.junit.jupiter.api.Test;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EditorConfigProviderTest {

    private final EditorConfigProvider service = new EditorConfigProvider();

    @Test
    void shouldLoadBasicEditorConfig() throws Exception {
        Path testFile = getTestResourcePath("editorconfig-test-data/basic/Test.java");
        Config config = service.lookupConfig(testFile, Config.SPACES_4);

        assertThat(config.indentStyle()).isEqualTo(IndentStyle.SPACE);
        assertThat(config.indentSize()).isEqualTo(2);
    }

    @Test
    void shouldLoadEditorConfigFromParentDirectory() throws Exception {
        Path testFile = getTestResourcePath("editorconfig-test-data/nested/src/main/java/Test.java");
        Config config = service.lookupConfig(testFile, Config.SPACES_4);

        assertThat(config.indentStyle()).isEqualTo(IndentStyle.TAB);
        assertThat(config.indentSize()).isEqualTo(1);
    }

    @Test
    void shouldFallbackToDefaultsWhenEditorConfigMissing() throws Exception {
        Path testFile = getTestResourcePath("editorconfig-test-data/missing/Test.java");
        Config config = service.lookupConfig(testFile, Config.SPACES_4);

        assertThat(config.indentStyle()).isEqualTo(IndentStyle.SPACE);
        assertThat(config.indentSize()).isEqualTo(4);
    }

    @Test
    void shouldFallbackToDefaultsWhenEditorConfigMalformed() throws Exception {
        Path testFile = getTestResourcePath("editorconfig-test-data/malformed/Test.java");
        Config config = service.lookupConfig(testFile, Config.SPACES_4);

        assertThat(config.indentStyle()).isEqualTo(IndentStyle.SPACE);
        assertThat(config.indentSize()).isEqualTo(4);
    }

    @Test
    void shouldUseDifferentDefaultsForTableFiles() throws Exception {
        Path testFile = getTestResourcePath("editorconfig-test-data/missing/Test.java");
        Config config = service.lookupConfig(testFile, Config.NO_INDENT);

        assertThat(config.indentStyle()).isEqualTo(IndentStyle.SPACE);
        assertThat(config.indentSize()).isEqualTo(0);
    }

    @Test
    void shouldThrowNullPointerExceptionWhenFilePathIsNull() {
        assertThatThrownBy(() -> service.lookupConfig(null, Config.SPACES_4))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("filePath must not be null");
    }

    @Test
    void shouldThrowNullPointerExceptionWhenDefaultsIsNull() throws Exception {
        Path testFile = getTestResourcePath("editorconfig-test-data/basic/Test.java");

        assertThatThrownBy(() -> service.lookupConfig(testFile, null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("defaults must not be null");
    }

    private Path getTestResourcePath(String relativePath) throws URISyntaxException {
        return Paths.get(getClass().getClassLoader().getResource(relativePath).toURI());
    }
}
