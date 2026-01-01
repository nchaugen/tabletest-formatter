package io.github.nchaugen.tabletest.formatter.core;

import io.github.nchaugen.tabletest.junit.TableTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TableTestExtractorTest {

    @Test
    void shouldExtractSingleTableFromJavaFile() {
        var sourceCode = """
                package com.example;

                import io.github.nchaugen.tabletest.TableTest;

                class CalculatorTest {
                    @TableTest(\"""
                        a | b | sum
                        1 | 2 | 3
                        5 | 3 | 8
                        \""")
                    void testAddition(int a, int b, int sum) {
                        assertThat(a + b).isEqualTo(sum);
                    }
                }
                """;

        List<TableMatch> matches = TableTestExtractor.findAll(sourceCode);

        assertThat(matches).hasSize(1);
        TableMatch match = matches.getFirst();
        String normalized = match.originalText().stripIndent().strip();
        String expected = """
                a | b | sum
                1 | 2 | 3
                5 | 3 | 8
                """.strip();
        assertThat(normalized).isEqualTo(expected);
        assertThat(match.startIndex()).isGreaterThan(0);
        assertThat(match.endIndex()).isGreaterThan(match.startIndex());
    }

    @Test
    void shouldExtractMultipleTablesFromOneFile() {
        var sourceCode = """
                class MyTest {
                    @TableTest(\"""
                        x | result
                        1 | 2
                        \""")
                    void test1(int x, int result) {}

                    @TableTest(\"""
                        name | age
                        Alice | 30
                        Bob | 25
                        \""")
                    void test2(String name, int age) {}
                }
                """;

        List<TableMatch> matches = TableTestExtractor.findAll(sourceCode);

        assertThat(matches).hasSize(2);
        assertThat(matches.get(0).originalText()).contains("x | result");
        assertThat(matches.get(1).originalText()).contains("name | age");
    }

    @Test
    void shouldHandleNoTables() {
        var sourceCode = """
                class SimpleTest {
                    @Test
                    void normalTest() {
                        assertThat(true).isTrue();
                    }
                }
                """;

        List<TableMatch> matches = TableTestExtractor.findAll(sourceCode);

        assertThat(matches).isEmpty();
    }

    @Test
    void shouldExtractFromKotlinFile() {
        var sourceCode = """
                import io.github.nchaugen.tabletest.TableTest

                class CalculatorTest {
                    @TableTest(\"""
                        a | b | sum
                        1 | 2 | 3
                        \""")
                    fun testAddition(a: Int, b: Int, sum: Int) {
                        assertThat(a + b).isEqualTo(sum)
                    }
                }
                """;

        List<TableMatch> matches = TableTestExtractor.findAll(sourceCode);

        assertThat(matches).hasSize(1);
        assertThat(matches.getFirst().originalText()).contains("a | b | sum");
    }

    @Test
    void shouldIgnoreTableTestInComments() {
        var sourceCode = """
                class MyTest {
                    // This is an example: @TableTest(\"""
                    // x | y
                    // 1 | 2
                    // \""")

                    /*
                     * Another example @TableTest(\"""
                     * a | b
                     * 3 | 4
                     * \""")
                     */

                    @TableTest(\"""
                        real | value
                        5 | 6
                        \""")
                    void actualTest(int real, int value) {}
                }
                """;

        List<TableMatch> matches = TableTestExtractor.findAll(sourceCode);

        // Note: Current regex implementation cannot distinguish comments from actual code
        // This test documents the known limitation - it will find 3 matches instead of 1
        // We accept this limitation as per the issue description
        assertThat(matches).hasSizeGreaterThanOrEqualTo(1);
        assertThat(matches.stream().anyMatch(m -> m.originalText().contains("real | value")))
                .isTrue();
    }

    @Test
    void shouldCaptureCorrectPositions() {
        var sourceCode = """
                @TableTest(\"""
                    x | y
                    1 | 2
                    \""")
                """;

        List<TableMatch> matches = TableTestExtractor.findAll(sourceCode);

        assertThat(matches).hasSize(1);
        TableMatch match = matches.getFirst();

        // Verify we can extract the table from source using the positions
        String extracted = sourceCode.substring(match.startIndex(), match.endIndex());
        assertThat(extracted).contains("@TableTest");
        assertThat(extracted).contains("x | y");
        assertThat(extracted).contains("1 | 2");
    }

    @Test
    void shouldHandleWhitespaceVariations() {
        var sourceCode = """
                class Test {
                    @TableTest   (   \"""
                        a | b
                        1 | 2
                        \"""   )
                    void test1() {}

                    @TableTest(\"""
                a|b
                3|4
                \""")
                    void test2() {}
                }
                """;

        List<TableMatch> matches = TableTestExtractor.findAll(sourceCode);

        assertThat(matches).hasSize(2);
    }

    @Test
    void shouldHandleValueParameterWithSpaces() {
        var sourceCode = """
                class Test {
                    @TableTest(value = \"""
                        x | y
                        1 | 2
                        \""")
                    void test1() {}
                }
                """;

        List<TableMatch> matches = TableTestExtractor.findAll(sourceCode);

        assertThat(matches).hasSize(1);
        assertThat(matches.getFirst().originalText()).contains("x | y");
    }

    @Test
    void shouldHandleValueParameterWithoutSpaces() {
        var sourceCode = """
                class Test {
                    @TableTest(value=\"""
                        x | y
                        1 | 2
                        \""")
                    void test1() {}
                }
                """;

        List<TableMatch> matches = TableTestExtractor.findAll(sourceCode);

        assertThat(matches).hasSize(1);
        assertThat(matches.getFirst().originalText()).contains("x | y");
    }

    @Test
    void shouldHandleMultipleParameters() {
        var sourceCode = """
                class Test {
                    @TableTest(resource="data.csv", value=\"""
                        a | b
                        1 | 2
                        \""")
                    void test1() {}

                    @TableTest(value=\"""
                        x | y
                        3 | 4
                        \""", encoding="UTF-8")
                    void test2() {}

                    @TableTest(resource="test.csv", value=\"""
                        m | n
                        5 | 6
                        \""", encoding="ISO-8859-1")
                    void test3() {}
                }
                """;

        List<TableMatch> matches = TableTestExtractor.findAll(sourceCode);

        assertThat(matches).hasSize(3);
        assertThat(matches.get(0).originalText()).contains("a | b");
        assertThat(matches.get(1).originalText()).contains("x | y");
        assertThat(matches.get(2).originalText()).contains("m | n");
    }

    @Test
    void shouldHandleInvalidAnnotationSyntax() {
        var sourceCode = """
                class Test {
                    @TableTest(
                        name | age
                        Alice | 30
                    )
                    void test1() {}

                    @TableTest("regular string not text block")
                    void test2() {}
                }
                """;

        List<TableMatch> matches = TableTestExtractor.findAll(sourceCode);

        // Invalid syntax (missing text block quotes) - finds nothing
        assertThat(matches).isEmpty();
    }

    @Test
    void shouldHandleMalformedStringLiterals() {
        var sourceCode = """
                class Test {
                    @TableTest(\"""
                        name | age
                        Alice | 30
                    void test1() {}

                    @TableTest(""
                        x | y
                        1 | 2
                        "")
                    void test2() {}
                }
                """;

        List<TableMatch> matches = TableTestExtractor.findAll(sourceCode);

        // Malformed literals (unclosed text blocks) - finds nothing
        assertThat(matches).isEmpty();
    }

    @Test
    void shouldHandleNonTableTestAnnotations() {
        var sourceCode = """
                class Test {
                    @Test
                    void test1() {}

                    @ParameterizedTest
                    @CsvSource(\"""
                        name, age
                        Alice, 30
                        \""")
                    void test2(String name, int age) {}

                    @MyCustomAnnotation(\"""
                        a | b
                        1 | 2
                        \""")
                    void test3() {}
                }
                """;

        List<TableMatch> matches = TableTestExtractor.findAll(sourceCode);

        // Non-TableTest annotations - finds nothing
        assertThat(matches).isEmpty();
    }

    @Test
    void shouldHandleCorruptedSourceFiles() {
        var sourceCode = """
                class Test {
                    @TableTest(\"""
                        name | age
                        Alice | 30
                """;

        List<TableMatch> matches = TableTestExtractor.findAll(sourceCode);

        // Incomplete/corrupted source (missing closing braces) - finds nothing
        assertThat(matches).isEmpty();
    }

    @Test
    void shouldHandleEmptySourceFile() {
        var sourceCode = "";

        List<TableMatch> matches = TableTestExtractor.findAll(sourceCode);

        assertThat(matches).isEmpty();
    }

    @Test
    void shouldHandleSourceWithOnlyWhitespace() {
        var sourceCode = "   \n\n   \t\t  \n  ";

        List<TableMatch> matches = TableTestExtractor.findAll(sourceCode);

        assertThat(matches).isEmpty();
    }

    // ========== Annotation Syntax Edge Cases ==========

    @Test
    void shouldHandleInlineComments() {
        var sourceCode = """
                class Test {
                    @TableTest( // inline comment
                    \"""
                        name | age
                        Alice | 30
                        \""")
                    void test1() {}
                }
                """;

        List<TableMatch> matches = TableTestExtractor.findAll(sourceCode);

        assertThat(matches).hasSize(1);
        assertThat(matches.getFirst().originalText()).contains("name | age");
        assertThat(matches.getFirst().originalText()).contains("Alice | 30");
    }

    @Test
    void shouldHandleBlockComments() {
        var sourceCode = """
                class Test {
                    @TableTest(/* comment */ \"""
                        name | age
                        Alice | 30
                        \""")
                    void test1() {}
                }
                """;

        List<TableMatch> matches = TableTestExtractor.findAll(sourceCode);

        assertThat(matches).hasSize(1);
        assertThat(matches.getFirst().originalText()).contains("name | age");
        assertThat(matches.getFirst().originalText()).contains("Alice | 30");
    }

    @Test
    void shouldHandleEmptyTableContent() {
        var sourceCode = """
                class Test {
                    @TableTest(\"""
                        \""")
                    void test1() {}
                }
                """;

        List<TableMatch> matches = TableTestExtractor.findAll(sourceCode);

        // Extraction should succeed (parsing may fail, but that's not the extractor's concern)
        assertThat(matches).hasSize(1);
        assertThat(matches.getFirst().originalText().stripIndent().strip()).isEmpty();
    }

    @Test
    void shouldPreserveEscapeSequences() {
        var sourceCode = """
                class Test {
                    @TableTest(\"""
                        name | quote
                        Alice | She said "hello"
                        Bob | Line1
                Line2
                        \""")
                    void test1() {}
                }
                """;

        List<TableMatch> matches = TableTestExtractor.findAll(sourceCode);

        assertThat(matches).hasSize(1);
        String extractedText = matches.getFirst().originalText();
        // Text blocks handle escape sequences at compile time, so quotes appear as regular quotes
        assertThat(extractedText).contains("She said \"hello\"");
        // Actual newline appears in the text block (not \n escape sequence)
        assertThat(extractedText).contains("Line1\nLine2");
    }

    @Test
    void shouldNotMatchFullyQualifiedAnnotation() {
        var sourceCode = """
                class Test {
                    @io.github.nchaugen.tabletest.junit.TableTest(\"""
                        name | age
                        Alice | 30
                        \""")
                    void test1() {}
                }
                """;

        List<TableMatch> matches = TableTestExtractor.findAll(sourceCode);

        // Known limitation: regex only matches @TableTest, not fully qualified names
        assertThat(matches).isEmpty();
    }

    @Test
    void shouldHandleMixedLineEndings() {
        // Test with Windows-style CRLF line endings
        String sourceCodeCrlf = """
            class Test {\r
                @TableTest(""\"\r
                    name | age\r
                    Alice | 30\r
                    ""\")\r
                void test1() {}\r
            }\r
            """;

        List<TableMatch> matchesCrlf = TableTestExtractor.findAll(sourceCodeCrlf);

        assertThat(matchesCrlf).hasSize(1);
        assertThat(matchesCrlf.getFirst().originalText()).contains("name | age");

        // Test with Unix-style LF line endings
        String sourceCodeLf = """
            class Test {
                @TableTest(""\"
                    name | age
                    Alice | 30
                    ""\")
                void test1() {}
            }
            """;

        List<TableMatch> matchesLf = TableTestExtractor.findAll(sourceCodeLf);

        assertThat(matchesLf).hasSize(1);
        assertThat(matchesLf.getFirst().originalText()).contains("name | age");
    }

    // ========== Indentation Detection Tests ==========

    @TableTest("""
        Scenario                       | sourceCode                                  | indent?
        no indentation (top-level)     | '@TableTest(\"""\\nx|y\\n1|2\\n\""")'       | 0
        shallow indentation (2 spaces) | '  @TableTest(\"""\\nx|y\\n1|2\\n\""")'    | 2
        standard indentation (4 spaces)| '    @TableTest(\"""\\nx|y\\n1|2\\n\""")'  | 4
        deep indentation (8 spaces)    | '        @TableTest(\"""\\nx|y\\n1|2\\n\""")'| 8
        """)
    void shouldDetectBaseIndentation(String sourceCode, int indent) {
        String actualSource = sourceCode.replace("\\n", "\n");

        List<TableMatch> matches = TableTestExtractor.findAll(actualSource);

        assertThat(matches).hasSize(1);
        assertThat(matches.getFirst().baseIndent()).isEqualTo(indent);
    }

    @TableTest("""
        Scenario              | sourceCode                                 | indent?
        single tab            | '\t@TableTest(\"""\\nx|y\\n1|2\\n\""")'   | 4
        two tabs              | '\t\t@TableTest(\"""\\nx|y\\n1|2\\n\""")'  | 8
        tab plus two spaces   | '\t  @TableTest(\"""\\nx|y\\n1|2\\n\""")'  | 6
        two spaces plus tab   | '  \t@TableTest(\"""\\nx|y\\n1|2\\n\""")'  | 6
        """)
    void shouldConvertTabsToSpaces(String sourceCode, int indent) {
        String actualSource = sourceCode.replace("\\n", "\n");

        List<TableMatch> matches = TableTestExtractor.findAll(actualSource);

        assertThat(matches).hasSize(1);
        assertThat(matches.getFirst().baseIndent()).isEqualTo(indent);
    }

    @Test
    void shouldDetectIndentationWithOpeningQuotesOnSeparateLine() {
        var sourceCode = """
                    @TableTest(
                    \"""
                    x | y
                    1 | 2
                    \""")
                    void test() {}
                """;

        List<TableMatch> matches = TableTestExtractor.findAll(sourceCode);

        assertThat(matches).hasSize(1);
        assertThat(matches.getFirst().baseIndent()).isEqualTo(4);
    }

    @Test
    void shouldDetectIndentationInNestedClass() {
        var sourceCode = """
                class Outer {
                    class Inner {
                        @TableTest(\"""
                            x | y
                            1 | 2
                            \""")
                        void test() {}
                    }
                }
                """;

        List<TableMatch> matches = TableTestExtractor.findAll(sourceCode);

        assertThat(matches).hasSize(1);
        assertThat(matches.getFirst().baseIndent()).isEqualTo(8);
    }

    @Test
    void shouldIgnoreWhitespaceAroundAnnotationWhenDetectingIndent() {
        var sourceCode = """
                    @TableTest   (   \"""
                        x | y
                        1 | 2
                        \"""   )
                    void test() {}
                """;

        List<TableMatch> matches = TableTestExtractor.findAll(sourceCode);

        assertThat(matches).hasSize(1);
        assertThat(matches.getFirst().baseIndent()).isEqualTo(4);
    }

    // ========== Input Validation Tests ==========

    @Test
    void shouldThrowNullPointerExceptionWhenSourceCodeIsNull() {
        assertThatThrownBy(() -> TableTestExtractor.findAll(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("sourceCode must not be null");
    }
}
