package io.github.nchaugen.tabletest.formatter.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StaticConfigProviderTest {

    @Test
    void shouldProvideDefaultConfiguration() {
        StaticConfigProvider config = StaticConfigProvider.DEFAULT;

        assertEquals(IndentType.SPACE, config.indentType());
        assertEquals(4, config.indentSize());
    }

    @Test
    void shouldCreateConfigurationWithSpaces() {
        StaticConfigProvider config = new StaticConfigProvider(IndentType.SPACE, 2);

        assertEquals(IndentType.SPACE, config.indentType());
        assertEquals(2, config.indentSize());
    }

    @Test
    void shouldCreateConfigurationWithTabs() {
        StaticConfigProvider config = new StaticConfigProvider(IndentType.TAB, 1);

        assertEquals(IndentType.TAB, config.indentType());
        assertEquals(1, config.indentSize());
    }

    @Test
    void shouldCreateConfigurationWithZeroIndent() {
        StaticConfigProvider config = new StaticConfigProvider(IndentType.SPACE, 0);

        assertEquals(IndentType.SPACE, config.indentType());
        assertEquals(0, config.indentSize());
    }

    @Test
    void shouldRejectNullIndentType() {
        NullPointerException exception =
                assertThrows(NullPointerException.class, () -> new StaticConfigProvider(null, 4));

        assertEquals("indentType must not be null", exception.getMessage());
    }

    @Test
    void shouldRejectNegativeIndentSize() {
        IllegalArgumentException exception =
                assertThrows(IllegalArgumentException.class, () -> new StaticConfigProvider(IndentType.SPACE, -1));

        assertEquals("indentSize must not be negative: -1", exception.getMessage());
    }

    @Test
    void shouldImplementConfigProviderInterface() {
        ConfigProvider config = new StaticConfigProvider(IndentType.TAB, 2);

        assertEquals(IndentType.TAB, config.indentType());
        assertEquals(2, config.indentSize());
    }

    @Test
    void shouldSupportRecordEquality() {
        StaticConfigProvider config1 = new StaticConfigProvider(IndentType.SPACE, 4);
        StaticConfigProvider config2 = new StaticConfigProvider(IndentType.SPACE, 4);
        StaticConfigProvider config3 = new StaticConfigProvider(IndentType.TAB, 4);

        assertEquals(config1, config2);
        assertNotEquals(config1, config3);
    }

    @Test
    void shouldSupportRecordHashCode() {
        StaticConfigProvider config1 = new StaticConfigProvider(IndentType.SPACE, 4);
        StaticConfigProvider config2 = new StaticConfigProvider(IndentType.SPACE, 4);

        assertEquals(config1.hashCode(), config2.hashCode());
    }

    @Test
    void shouldSupportRecordToString() {
        StaticConfigProvider config = new StaticConfigProvider(IndentType.SPACE, 4);

        String toString = config.toString();
        assertTrue(toString.contains("StaticConfigProvider"));
        assertTrue(toString.contains("SPACE"));
        assertTrue(toString.contains("4"));
    }
}
