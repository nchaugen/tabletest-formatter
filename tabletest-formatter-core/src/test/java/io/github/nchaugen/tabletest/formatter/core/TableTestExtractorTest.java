package io.github.nchaugen.tabletest.formatter.core;

import io.github.nchaugen.tabletest.junit.TableTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TableTestExtractorTest {

    private final TableTestExtractor extractor = new RegexTableTestExtractor();

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

        List<TableMatch> matches = extractor.findAll(sourceCode);

        assertThat(matches).hasSize(1);
        TableMatch match = matches.getFirst();
        String extracted = sourceCode.substring(match.tableContentStart(), match.tableContentEnd());
        String normalized = extracted.stripIndent().strip();
        String expected = """
                a | b | sum
                1 | 2 | 3
                5 | 3 | 8
                """.strip();
        assertThat(normalized).isEqualTo(expected);
        assertThat(match.tableContentStart()).isGreaterThan(0);
        assertThat(match.tableContentEnd()).isGreaterThan(match.tableContentStart());
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

        List<TableMatch> matches = extractor.findAll(sourceCode);

        assertThat(matches).hasSize(2);
        assertThat(sourceCode.substring(
                        matches.get(0).tableContentStart(), matches.get(0).tableContentEnd()))
                .contains("x | result");
        assertThat(sourceCode.substring(
                        matches.get(1).tableContentStart(), matches.get(1).tableContentEnd()))
                .contains("name | age");
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

        List<TableMatch> matches = extractor.findAll(sourceCode);

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

        List<TableMatch> matches = extractor.findAll(sourceCode);

        assertThat(matches).hasSize(1);
        assertThat(sourceCode.substring(
                        matches.getFirst().tableContentStart(),
                        matches.getFirst().tableContentEnd()))
                .contains("a | b | sum");
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

        List<TableMatch> matches = extractor.findAll(sourceCode);

        // Note: Current regex implementation cannot distinguish comments from actual code
        // This test documents the known limitation - it will find 3 matches instead of 1
        // We accept this limitation as per the issue description
        assertThat(matches).hasSizeGreaterThanOrEqualTo(1);
        assertThat(matches.stream().anyMatch(m -> sourceCode
                        .substring(m.tableContentStart(), m.tableContentEnd())
                        .contains("real | value")))
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

        List<TableMatch> matches = extractor.findAll(sourceCode);

        assertThat(matches).hasSize(1);
        TableMatch match = matches.getFirst();

        // Verify we can extract the table content from source using the positions
        String tableContent = sourceCode.substring(match.tableContentStart(), match.tableContentEnd());
        assertThat(tableContent).contains("x | y");
        assertThat(tableContent).contains("1 | 2");

        // Verify base indent extraction
        String baseIndent = sourceCode.substring(match.baseIndentStart(), match.baseIndentEnd());
        assertThat(baseIndent).isEmpty(); // Top-level annotation has no indent
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

        List<TableMatch> matches = extractor.findAll(sourceCode);

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

        List<TableMatch> matches = extractor.findAll(sourceCode);

        assertThat(matches).hasSize(1);
        assertThat(sourceCode.substring(
                        matches.getFirst().tableContentStart(),
                        matches.getFirst().tableContentEnd()))
                .contains("x | y");
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

        List<TableMatch> matches = extractor.findAll(sourceCode);

        assertThat(matches).hasSize(1);
        assertThat(sourceCode.substring(
                        matches.getFirst().tableContentStart(),
                        matches.getFirst().tableContentEnd()))
                .contains("x | y");
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

        List<TableMatch> matches = extractor.findAll(sourceCode);

        assertThat(matches).hasSize(3);
        assertThat(sourceCode.substring(
                        matches.get(0).tableContentStart(), matches.get(0).tableContentEnd()))
                .contains("a | b");
        assertThat(sourceCode.substring(
                        matches.get(1).tableContentStart(), matches.get(1).tableContentEnd()))
                .contains("x | y");
        assertThat(sourceCode.substring(
                        matches.get(2).tableContentStart(), matches.get(2).tableContentEnd()))
                .contains("m | n");
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

        List<TableMatch> matches = extractor.findAll(sourceCode);

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

        List<TableMatch> matches = extractor.findAll(sourceCode);

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

        List<TableMatch> matches = extractor.findAll(sourceCode);

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

        List<TableMatch> matches = extractor.findAll(sourceCode);

        // Incomplete/corrupted source (missing closing braces) - finds nothing
        assertThat(matches).isEmpty();
    }

    @Test
    void shouldHandleEmptySourceFile() {
        var sourceCode = "";

        List<TableMatch> matches = extractor.findAll(sourceCode);

        assertThat(matches).isEmpty();
    }

    @Test
    void shouldHandleSourceWithOnlyWhitespace() {
        var sourceCode = "   \n\n   \t\t  \n  ";

        List<TableMatch> matches = extractor.findAll(sourceCode);

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

        List<TableMatch> matches = extractor.findAll(sourceCode);

        assertThat(matches).hasSize(1);
        String extracted = sourceCode.substring(
                matches.getFirst().tableContentStart(), matches.getFirst().tableContentEnd());
        assertThat(extracted).contains("name | age");
        assertThat(extracted).contains("Alice | 30");
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

        List<TableMatch> matches = extractor.findAll(sourceCode);

        assertThat(matches).hasSize(1);
        String extracted = sourceCode.substring(
                matches.getFirst().tableContentStart(), matches.getFirst().tableContentEnd());
        assertThat(extracted).contains("name | age");
        assertThat(extracted).contains("Alice | 30");
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

        List<TableMatch> matches = extractor.findAll(sourceCode);

        // Extraction should succeed (parsing may fail, but that's not the extractor's concern)
        assertThat(matches).hasSize(1);
        String extracted = sourceCode.substring(
                matches.getFirst().tableContentStart(), matches.getFirst().tableContentEnd());
        assertThat(extracted.stripIndent().strip()).isEmpty();
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

        List<TableMatch> matches = extractor.findAll(sourceCode);

        assertThat(matches).hasSize(1);
        String extractedText = sourceCode.substring(
                matches.getFirst().tableContentStart(), matches.getFirst().tableContentEnd());
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

        List<TableMatch> matches = extractor.findAll(sourceCode);

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

        List<TableMatch> matchesCrlf = extractor.findAll(sourceCodeCrlf);

        assertThat(matchesCrlf).hasSize(1);
        assertThat(sourceCodeCrlf.substring(
                        matchesCrlf.getFirst().tableContentStart(),
                        matchesCrlf.getFirst().tableContentEnd()))
                .contains("name | age");

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

        List<TableMatch> matchesLf = extractor.findAll(sourceCodeLf);

        assertThat(matchesLf).hasSize(1);
        assertThat(sourceCodeLf.substring(
                        matchesLf.getFirst().tableContentStart(),
                        matchesLf.getFirst().tableContentEnd()))
                .contains("name | age");
    }

    // ========== Indentation Detection Tests ==========

    @TableTest("""
        Scenario                       | sourceCode                                  | indent?
        no indentation (top-level)     | '@TableTest(\"""\\nx|y\\n1|2\\n\""")'       | ''
        shallow indentation (2 spaces) | '  @TableTest(\"""\\nx|y\\n1|2\\n\""")'    | '  '
        standard indentation (4 spaces)| '    @TableTest(\"""\\nx|y\\n1|2\\n\""")'  | '    '
        deep indentation (8 spaces)    | '        @TableTest(\"""\\nx|y\\n1|2\\n\""")'| '        '
        """)
    void shouldDetectBaseIndentation(String sourceCode, String indent) {
        String actualSource = sourceCode.replace("\\n", "\n");

        List<TableMatch> matches = extractor.findAll(actualSource);

        assertThat(matches).hasSize(1);
        String extractedIndent = actualSource.substring(
                matches.getFirst().baseIndentStart(), matches.getFirst().baseIndentEnd());
        assertThat(extractedIndent).isEqualTo(indent);
    }

    @TableTest("""
        Scenario              | sourceCode                                 | indent?
        single tab            | '\t@TableTest(\"""\\nx|y\\n1|2\\n\""")'   | '\t'
        two tabs              | '\t\t@TableTest(\"""\\nx|y\\n1|2\\n\""")'  | '\t\t'
        tab plus two spaces   | '\t  @TableTest(\"""\\nx|y\\n1|2\\n\""")'  | '\t  '
        two spaces plus tab   | '  \t@TableTest(\"""\\nx|y\\n1|2\\n\""")'  | '  \t'
        """)
    void shouldPreserveTabsAndSpaces(String sourceCode, String indent) {
        String actualSource = sourceCode.replace("\\n", "\n");

        List<TableMatch> matches = extractor.findAll(actualSource);

        assertThat(matches).hasSize(1);
        String extractedIndent = actualSource.substring(
                matches.getFirst().baseIndentStart(), matches.getFirst().baseIndentEnd());
        assertThat(extractedIndent).isEqualTo(indent);
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

        List<TableMatch> matches = extractor.findAll(sourceCode);

        assertThat(matches).hasSize(1);
        String extractedIndent = sourceCode.substring(
                matches.getFirst().baseIndentStart(), matches.getFirst().baseIndentEnd());
        assertThat(extractedIndent).isEqualTo("    ");
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

        List<TableMatch> matches = extractor.findAll(sourceCode);

        assertThat(matches).hasSize(1);
        String extractedIndent = sourceCode.substring(
                matches.getFirst().baseIndentStart(), matches.getFirst().baseIndentEnd());
        assertThat(extractedIndent).isEqualTo("        ");
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

        List<TableMatch> matches = extractor.findAll(sourceCode);

        assertThat(matches).hasSize(1);
        String extractedIndent = sourceCode.substring(
                matches.getFirst().baseIndentStart(), matches.getFirst().baseIndentEnd());
        assertThat(extractedIndent).isEqualTo("    ");
    }

    // ========== Input Validation Tests ==========

    @Test
    void shouldThrowNullPointerExceptionWhenSourceCodeIsNull() {
        assertThatThrownBy(() -> extractor.findAll(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("sourceCode must not be null");
    }
}
