package io.github.nchaugen.tabletest.formatter.core;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SourceFileFormatterTest {

    private final SourceFileFormatter formatter = new SourceFileFormatter();

    @Test
    void shouldFormatSingleTable() {
        String input = """
                public class Test {
                    @TableTest(\"""
                    name|age
                    Alice|30
                    \""")
                    void test() {}
                }
                """;

        String result = formatter.format(input);

        assertThat(result).isEqualTo("""
                public class Test {
                    @TableTest(\"""
                name  | age
                Alice | 30
                    \""")
                    void test() {}
                }
                """);
    }

    @Test
    void shouldFormatMultipleTables() {
        String input = """
                public class Test {
                    @TableTest(\"""
                    name|age
                    Alice|30
                    \""")
                    void test1() {}

                    @TableTest(\"""
                    city|country
                    London|UK
                    \""")
                    void test2() {}
                }
                """;

        String result = formatter.format(input);

        assertThat(result).isEqualTo("""
                public class Test {
                    @TableTest(\"""
                name  | age
                Alice | 30
                    \""")
                    void test1() {}

                    @TableTest(\"""
                city   | country
                London | UK
                    \""")
                    void test2() {}
                }
                """);
    }

    @Test
    void shouldReturnUnchangedWhenNoTables() {
        String input = """
                public class Test {
                    void test() {
                        System.out.println("Hello");
                    }
                }
                """;

        String result = formatter.format(input);

        assertThat(result).isEqualTo(input);
    }

    @Test
    void shouldReturnUnchangedWhenAlreadyFormatted() {
        String input = """
                public class Test {
                    @TableTest(\"""
                name  | age
                Alice | 30
                    \""")
                    void test() {}
                }
                """;

        String result = formatter.format(input);

        assertThat(result).isEqualTo(input);
    }

    @Test
    void shouldFormatKotlinFile() {
        String input = """
                class Test {
                    @TableTest(\"""
                    name|age
                    Alice|30
                    \""")
                    fun test() {}
                }
                """;

        String result = formatter.format(input);

        assertThat(result).isEqualTo("""
                class Test {
                    @TableTest(\"""
                name  | age
                Alice | 30
                    \""")
                    fun test() {}
                }
                """);
    }

    @Test
    void shouldHandleTablesWithDifferentWidths() {
        String input = """
                class Test {
                    @TableTest(\"""
                    a|bb|ccc
                    dddd|e|ff
                    \""")
                    void test() {}
                }
                """;

        String result = formatter.format(input);

        assertThat(result).isEqualTo("""
                class Test {
                    @TableTest(\"""
                a    | bb | ccc
                dddd | e  | ff
                    \""")
                    void test() {}
                }
                """);
    }
}
