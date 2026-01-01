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

    @Test
    void shouldFormatWithIndentation() {
        String input = """
                class Test {
                    @TableTest(\"""
                    name|age
                    Alice|30
                    \""")
                    void test() {}
                }
                """;

        String result = formatter.format(input, 4);

        assertThat(result).isEqualTo("""
                class Test {
                    @TableTest(\"""
                        name  | age
                        Alice | 30
                        \""")
                    void test() {}
                }
                """);
    }

    @Test
    void shouldFormatWithIndentationAndExtraWhitespaceAroundAnnotation() {
        String input = """
                class Test {
                    @TableTest(  \"""
                    name|age
                    Alice|30
                    \"""  )
                    void test() {}
                }
                """;

        String result = formatter.format(input, 4);

        assertThat(result).isEqualTo("""
                class Test {
                    @TableTest(  \"""
                        name  | age
                        Alice | 30
                        \"""  )
                    void test() {}
                }
                """);
    }

    @Test
    void shouldFormatWithIndentationWhenQuotesOnSeparateLine() {
        String input = """
                class Test {
                    @TableTest(
                        \"""
                        name|age
                        Alice|30
                        \"""
                    )
                    void test() {}
                }
                """;

        String result = formatter.format(input, 4);

        assertThat(result).isEqualTo("""
                class Test {
                    @TableTest(
                        \"""
                        name  | age
                        Alice | 30
                        \"""
                    )
                    void test() {}
                }
                """);
    }

    @Test
    void shouldFormatTablesInNestedClass() {
        String input = """
                class Outer {
                    class Inner {
                        @TableTest(\"""
                        name|age
                        Alice|30
                        \""")
                        void test() {}
                    }
                }
                """;

        String result = formatter.format(input, 4);

        assertThat(result).isEqualTo("""
                class Outer {
                    class Inner {
                        @TableTest(\"""
                            name  | age
                            Alice | 30
                            \""")
                        void test() {}
                    }
                }
                """);
    }

    @Test
    void shouldFormatMultipleTablesWithDifferentIndentationLevels() {
        String input = """
                class Test {
                    @TableTest(\"""
                    name|age
                    Alice|30
                    \""")
                    void test1() {}

                    class Nested {
                        @TableTest(\"""
                        city|country
                        London|UK
                        \""")
                        void test2() {}
                    }
                }
                """;

        String result = formatter.format(input, 4);

        assertThat(result).isEqualTo("""
                class Test {
                    @TableTest(\"""
                        name  | age
                        Alice | 30
                        \""")
                    void test1() {}

                    class Nested {
                        @TableTest(\"""
                            city   | country
                            London | UK
                            \""")
                        void test2() {}
                    }
                }
                """);
    }

    @Test
    void shouldFormatDeeplyNestedStructure() {
        String input = """
                class Level1 {
                    class Level2 {
                        class Level3 {
                            @TableTest(\"""
                            x|y
                            1|2
                            \""")
                            void test() {}
                        }
                    }
                }
                """;

        String result = formatter.format(input, 4);

        assertThat(result).isEqualTo("""
                class Level1 {
                    class Level2 {
                        class Level3 {
                            @TableTest(\"""
                                x | y
                                1 | 2
                                \""")
                            void test() {}
                        }
                    }
                }
                """);
    }

    @Test
    void shouldFormatWithTwoSpaceIndentation() {
        String input = """
                class Test {
                  @TableTest(\"""
                  name|age
                  Alice|30
                  \""")
                  void test() {}
                }
                """;

        String result = formatter.format(input, 2);

        assertThat(result).isEqualTo("""
                class Test {
                  @TableTest(\"""
                    name  | age
                    Alice | 30
                    \""")
                  void test() {}
                }
                """);
    }

    @Test
    void shouldFormatWithTabIndentationNormalizedToSpaces() {
        String input = """
                class Test {
                \t@TableTest(\"""
                \tname|age
                \tAlice|30
                \t\""")
                \tvoid test() {}
                }
                """;

        String result = formatter.format(input, 4);

        // Tab characters are normalized to spaces when indentation is applied
        assertThat(result).isEqualTo("""
                class Test {
                \t@TableTest(\"""
                        name  | age
                        Alice | 30
                        \""")
                \tvoid test() {}
                }
                """);
    }

    @Test
    void shouldFormatWithMixedSpacesAndTabs() {
        String input = """
                class Test {
                \t@TableTest(\"""
                    name|age
                    Alice|30
                    \""")
                    void test() {}
                }
                """;

        String result = formatter.format(input, 4);

        assertThat(result).isEqualTo("""
                class Test {
                \t@TableTest(\"""
                        name  | age
                        Alice | 30
                        \""")
                    void test() {}
                }
                """);
    }

    @Test
    void shouldFormatKotlinRawStringWithContentOnSameLine() {
        String input = """
                class Test {
                    @TableTest(\"""name|age
                    Alice|30
                    \""")
                    fun test() {}
                }
                """;

        String result = formatter.format(input, 4);

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
    void shouldFormatKotlinWithOpeningQuotesAndContentOnSameLine() {
        String input = """
                class Test {
                    @TableTest(
                        \"""name|age
                        Alice|30
                        \"""
                    )
                    fun test() {}
                }
                """;

        String result = formatter.format(input, 4);

        assertThat(result).isEqualTo("""
                class Test {
                    @TableTest(
                        \"""
                        name  | age
                        Alice | 30
                        \"""
                    )
                    fun test() {}
                }
                """);
    }
}
