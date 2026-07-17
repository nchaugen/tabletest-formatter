package org.tabletest.formatter.core;

import org.junit.jupiter.api.Test;
import org.tabletest.formatter.config.Config;
import org.tabletest.formatter.config.IndentStyle;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * End-to-end assembly checks for {@link SourceFileFormatter}: annotation extraction,
 * splicing, idempotence, Java/Kotlin text-block forms and the string-array form. The
 * table formatting rules themselves are specified in ColumnWidthTest, RowLayoutTest,
 * CellFormattingTest, CommentAndBlankLineTest and IndentationTest.
 */
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

        String result = formatter.format(input, Config.NO_INDENT);

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

        String result = formatter.format(input, Config.NO_INDENT);

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

        String result = formatter.format(input, Config.NO_INDENT);

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

        String result = formatter.format(input, Config.NO_INDENT);

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

        String result = formatter.format(input, Config.NO_INDENT);

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

        String result = formatter.format(input, Config.SPACES_4);

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

        String result = formatter.format(input, Config.SPACES_4);

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

        String result = formatter.format(input, Config.SPACES_4);

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

        String result = formatter.format(input, Config.SPACES_4);

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
    void shouldPreserveTabIndentation() {
        String input = """
                class Test {
                \t@TableTest(\"""
                \tname|age
                \tAlice|30
                \t\""")
                \tvoid test() {}
                }
                """;

        String result = formatter.format(input, new Config(IndentStyle.SPACE, 1));

        // Tab characters in base indentation are preserved, 1 space added as indent character
        assertThat(result).isEqualTo("""
                class Test {
                \t@TableTest(\"""
                \t name  | age
                \t Alice | 30
                \t \""")
                \tvoid test() {}
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

        String result = formatter.format(input, Config.SPACES_4);

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

        String result = formatter.format(input, Config.SPACES_4);

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

    @Test
    void shouldPreserveNewlineWhenAlreadyPresentWithZeroIndentation() {
        String input = """
                @TableTest(\"""
                name|age
                Alice|30
                \""")
                void test() {}
                """;

        String result = formatter.format(input, Config.NO_INDENT);

        // The newline should be preserved (not removed)
        assertThat(result).isEqualTo("""
                @TableTest(\"""
                name  | age
                Alice | 30
                \""")
                void test() {}
                """);
    }

    @Test
    void shouldAddNewlineAfterOpeningQuotesEvenWithZeroIndentationKotlin() {
        String input = """
                @TableTest(\"""name|age
                Alice|30
                \""")
                fun test() {}
                """;

        String result = formatter.format(input, Config.NO_INDENT);

        // Should add newline for readability (Kotlin) even with indentSize=0
        assertThat(result).isEqualTo("""
                @TableTest(\"""
                name  | age
                Alice | 30
                \""")
                fun test() {}
                """);
    }

    // ========== String Array Tests ==========

    @Test
    void shouldFormatStringArray() {
        String input = """
                class Test {
                    @TableTest({"name|age","Alice|30","Bob|7"})
                    void test() {}
                }
                """;

        String result = formatter.format(input, Config.SPACES_4);

        assertThat(result).isEqualTo("""
                class Test {
                    @TableTest({
                        "name  | age",
                        "Alice | 30 ",
                        "Bob   | 7  "
                    })
                    void test() {}
                }
                """);
    }

    @Test
    void shouldFormatStringArrayWithNoIndent() {
        String input = """
                @TableTest({"name|age","Alice|30"})
                void test() {}
                """;

        String result = formatter.format(input, Config.NO_INDENT);

        assertThat(result).isEqualTo("""
                @TableTest({
                "name  | age",
                "Alice | 30 "
                })
                void test() {}
                """);
    }

    @Test
    void shouldFormatStringArrayIdempotently() {
        String input = """
                class Test {
                    @TableTest({
                        "name  | age",
                        "Alice | 30 ",
                        "Bob   | 7  "
                    })
                    void test() {}
                }
                """;

        String result = formatter.format(input, Config.SPACES_4);

        assertThat(result).isEqualTo(input);
    }

    @Test
    void shouldFormatStringArrayInNestedClass() {
        String input = """
                class Outer {
                    class Inner {
                        @TableTest({"name|age","Alice|30"})
                        void test() {}
                    }
                }
                """;

        String result = formatter.format(input, Config.SPACES_4);

        assertThat(result).isEqualTo("""
                class Outer {
                    class Inner {
                        @TableTest({
                            "name  | age",
                            "Alice | 30 "
                        })
                        void test() {}
                    }
                }
                """);
    }

    @Test
    void shouldFormatMixedTextBlockAndStringArray() {
        String input = """
                class Test {
                    @TableTest(\"""
                    name|age
                    Alice|30
                    \""")
                    void test1() {}

                    @TableTest({"city|country","London|UK"})
                    void test2() {}
                }
                """;

        String result = formatter.format(input, Config.SPACES_4);

        assertThat(result).isEqualTo("""
                class Test {
                    @TableTest(\"""
                        name  | age
                        Alice | 30
                        \""")
                    void test1() {}

                    @TableTest({
                        "city   | country",
                        "London | UK     "
                    })
                    void test2() {}
                }
                """);
    }

    @Test
    void shouldFormatSingleEntryStringArray() {
        String input = """
                class Test {
                    @TableTest({"name|age"})
                    void test() {}
                }
                """;

        String result = formatter.format(input, Config.SPACES_4);

        assertThat(result).isEqualTo("""
                class Test {
                    @TableTest({
                        "name | age"
                    })
                    void test() {}
                }
                """);
    }

    @Test
    void shouldFormatStringArrayWithEntriesOnSeparateLines() {
        String input = """
                class Test {
                    @TableTest({
                        "name|age",
                        "Alice|30"
                    })
                    void test() {}
                }
                """;

        String result = formatter.format(input, Config.SPACES_4);

        assertThat(result).isEqualTo("""
                class Test {
                    @TableTest({
                        "name  | age",
                        "Alice | 30 "
                    })
                    void test() {}
                }
                """);
    }

    @Test
    void shouldPreserveCommentedOutEntryInStringArray() {
        String input = """
                class Test {
                    @TableTest({
                        "a|b",
                        // "9 | 9",
                        "10|2"
                    })
                    void test() {}
                }
                """;

        String result = formatter.format(input, Config.SPACES_4);

        assertThat(result).isEqualTo("""
                class Test {
                    @TableTest({
                        "a  | b",
                        // "9 | 9",
                        "10 | 2"
                    })
                    void test() {}
                }
                """);
    }

    @Test
    void shouldPreserveBlockCommentedEntryInStringArray() {
        String input = """
                class Test {
                    @TableTest({
                        "a|b",
                        /* "9 | 9", */
                        "10|2"
                    })
                    void test() {}
                }
                """;

        String result = formatter.format(input, Config.SPACES_4);

        assertThat(result).isEqualTo("""
                class Test {
                    @TableTest({
                        "a  | b",
                        /* "9 | 9", */
                        "10 | 2"
                    })
                    void test() {}
                }
                """);
    }

    @Test
    void shouldPreserveStandaloneCommentInStringArray() {
        String input = """
                class Test {
                    @TableTest({
                        "name|age",
                        // boundary cases below
                        "Alice|30"
                    })
                    void test() {}
                }
                """;

        String result = formatter.format(input, Config.SPACES_4);

        assertThat(result).isEqualTo("""
                class Test {
                    @TableTest({
                        "name  | age",
                        // boundary cases below
                        "Alice | 30 "
                    })
                    void test() {}
                }
                """);
    }

    @Test
    void shouldKeepInlineCommentAttachedToEntryInStringArray() {
        String input = """
                class Test {
                    @TableTest({
                        "name|age", // header
                        "Alice|30" // adult
                    })
                    void test() {}
                }
                """;

        String result = formatter.format(input, Config.SPACES_4);

        assertThat(result).isEqualTo("""
                class Test {
                    @TableTest({
                        "name  | age", // header
                        "Alice | 30 " // adult
                    })
                    void test() {}
                }
                """);
    }

    @Test
    void shouldPreserveEmptyEntryInStringArrayAsBlankRow() {
        String input = """
                class Test {
                    @TableTest({"a|b", "", "1|2"})
                    void test() {}
                }
                """;

        String result = formatter.format(input, Config.SPACES_4);

        assertThat(result).isEqualTo("""
                class Test {
                    @TableTest({
                        "a | b",
                        "     ",
                        "1 | 2"
                    })
                    void test() {}
                }
                """);
    }

    @Test
    void shouldAlignClosingQuotesByDisplayWidthInStringArray() {
        String input = """
                class Test {
                    @TableTest({"name|width","你好|4","hello|5"})
                    void test() {}
                }
                """;

        String result = formatter.format(input, Config.SPACES_4);

        assertThat(result).isEqualTo("""
                class Test {
                    @TableTest({
                        "name  | width",
                        "你好  | 4    ",
                        "hello | 5    "
                    })
                    void test() {}
                }
                """);
    }

    @Test
    void shouldLeaveCommentsOnlyStringArrayUnchanged() {
        String input = """
                class Test {
                    @TableTest({ /* "a|b" */ })
                    void test() {}
                }
                """;

        String result = formatter.format(input, Config.SPACES_4);

        assertThat(result).isEqualTo(input);
    }

    @Test
    void shouldFormatStringArrayWithCommentsIdempotently() {
        String input = """
                class Test {
                    @TableTest({
                        "a  | b",
                        // "9 | 9",
                        "10 | 2", // inline note
                        "3  | 4"
                    })
                    void test() {}
                }
                """;

        String result = formatter.format(input, Config.SPACES_4);

        assertThat(result).isEqualTo(input);
    }

    // ========== Text Block Tests (continued) ==========

    @Test
    void shouldPreserveMultipleNewlinesAfterOpeningQuotes() {
        String input = """
                @TableTest(\"""

                name|age
                Alice|30
                \""")
                void test() {}
                """;

        String result = formatter.format(input, Config.NO_INDENT);

        // Multiple newlines should be preserved
        assertThat(result).isEqualTo("""
                @TableTest(\"""

                name  | age
                Alice | 30
                \""")
                void test() {}
                """);
    }
}
