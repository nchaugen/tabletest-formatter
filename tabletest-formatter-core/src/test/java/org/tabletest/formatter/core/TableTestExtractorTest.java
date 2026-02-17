package org.tabletest.formatter.core;

import org.junit.jupiter.api.Test;
import org.tabletest.junit.TableTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TableTestExtractorTest {

    private final TableTestExtractor extractor = new TableTestExtractor();

    @Test
    void shouldExtractSingleTableFromJavaFile() {
        String sourceCode = """
                package com.example;

                import org.tabletest.TableTest;

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
    void shouldIgnoreTableTestInStringLiteral() {
        String sourceCode = """
                package com.example;

                import org.tabletest.TableTest;

                class ExampleTest {
                    void demonstrateUsage() {
                        // This is just example code in a string - should NOT be extracted
                        String example = \"""
                            @TableTest(\\\"""
                                x | y
                                1 | 2
                                \\\""")
                            void exampleTest() {}
                            \""";
                    }

                    // This is a REAL annotation - SHOULD be extracted
                    @TableTest(\"""
                        real | data
                        3 | 4
                        \""")
                    void actualTest(int real, int data) {
                        assertThat(real + 1).isEqualTo(data);
                    }
                }
                """;

        List<TableMatch> matches = extractor.findAll(sourceCode);

        // Should find exactly 1 match (the real annotation, not the one in the string)
        assertThat(matches).hasSize(1);
        String extracted = sourceCode.substring(
                matches.getFirst().tableContentStart(), matches.getFirst().tableContentEnd());
        String normalized = extracted.stripIndent().strip();
        assertThat(normalized).contains("real | data");
        assertThat(normalized).doesNotContain("x | y");
    }

    @Test
    void shouldExtractMultipleTablesFromOneFile() {
        String sourceCode = """
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
        String sourceCode = """
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
    void shouldIgnoreTableTestInComments() {
        String sourceCode = """
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

        // Should only find the real annotation, not the ones in comments
        assertThat(matches).hasSize(1);
        assertThat(sourceCode.substring(
                        matches.getFirst().tableContentStart(),
                        matches.getFirst().tableContentEnd()))
                .contains("real | value");
    }

    @Test
    void shouldHandleWhitespaceVariations() {
        String sourceCode = """
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
        String sourceCode = """
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
        String sourceCode = """
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
    void shouldHandleEmptySourceFile() {
        String sourceCode = "";

        List<TableMatch> matches = extractor.findAll(sourceCode);

        assertThat(matches).isEmpty();
    }

    @Test
    void shouldHandleSourceWithOnlyWhitespace() {
        String sourceCode = "   \n\n   \t\t  \n  ";

        List<TableMatch> matches = extractor.findAll(sourceCode);

        assertThat(matches).isEmpty();
    }

    @Test
    void shouldThrowNullPointerExceptionWhenSourceCodeIsNull() {
        assertThatThrownBy(() -> extractor.findAll(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("sourceCode must not be null");
    }

    @Test
    void shouldIgnoreRegularStringsAndOnlyExtractTextBlocks() {
        String sourceCode = """
                class Test {
                    // Regular string with escape sequences - should NOT be extracted
                    @TableTest("a | b\\n1 | 2")
                    void test1() {}

                    // Text block - SHOULD be extracted
                    @TableTest(\"""
                        x | y
                        3 | 4
                        \""")
                    void test2() {}

                    // Another regular string - should NOT be extracted
                    @TableTest(value = "name | age\\nAlice | 30")
                    void test3() {}
                }
                """;

        List<TableMatch> matches = extractor.findAll(sourceCode);

        // Should only find the text block (test2), not the regular strings (test1, test3)
        assertThat(matches).hasSize(1);
        assertThat(sourceCode.substring(
                        matches.getFirst().tableContentStart(),
                        matches.getFirst().tableContentEnd()))
                .contains("x | y")
                .contains("3 | 4");
    }

    @Test
    void shouldHandleMultipleParameters() {
        String sourceCode = """
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
                }
                """;

        List<TableMatch> matches = extractor.findAll(sourceCode);

        assertThat(matches).hasSize(2);
        assertThat(sourceCode.substring(
                        matches.get(0).tableContentStart(), matches.get(0).tableContentEnd()))
                .contains("a | b");
        assertThat(sourceCode.substring(
                        matches.get(1).tableContentStart(), matches.get(1).tableContentEnd()))
                .contains("x | y");
    }

    @Test
    void shouldDetectBaseIndentation() {
        String sourceCode = """
                    @TableTest(\"""
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
        String sourceCode = """
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
    void shouldHandleCharLiteralsWithSpecialChars() {
        String sourceCode = """
                class Test {
                    char quote = '"';
                    char brace = '{';
                    char slash = '/';

                    @TableTest(\"""
                        x | y
                        1 | 2
                        \""")
                    void test() {}
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
    void shouldHandleInlineComments() {
        String sourceCode = """
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
    void shouldHandleBlockCommentsInAnnotation() {
        String sourceCode = """
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
    void shouldHandleMultipleAnnotationsOnSameLine() {
        String sourceCode = """
                class Test {
                    @Test @TableTest(\"""
                        x | y
                        1 | 2
                        \""")
                    void test1() {}

                    @Disabled @Test @TableTest(\"""
                        a | b
                        3 | 4
                        \""")
                    void test2() {}
                }
                """;

        List<TableMatch> matches = extractor.findAll(sourceCode);

        assertThat(matches).hasSize(2);

        // Verify base indentation is extracted correctly (should be just whitespace, not including @Test)
        String indent1 = sourceCode.substring(
                matches.get(0).baseIndentStart(), matches.get(0).baseIndentEnd());
        String indent2 = sourceCode.substring(
                matches.get(1).baseIndentStart(), matches.get(1).baseIndentEnd());

        assertThat(indent1).isEqualTo("    ");
        assertThat(indent2).isEqualTo("    ");
    }

    // ========== Kotlin Support Tests ==========

    @Test
    void shouldExtractFromKotlinFile() {
        String sourceCode = """
                import org.tabletest.TableTest

                class CalculatorTest {
                    @TableTest(\"""
                        a | b | sum
                        1 | 2 | 3
                        5 | 3 | 8
                        \""")
                    fun testAddition(a: Int, b: Int, sum: Int) {
                        assertThat(a + b).isEqualTo(sum)
                    }
                }
                """;

        List<TableMatch> matches = extractor.findAll(sourceCode);

        assertThat(matches).hasSize(1);
        String extracted = sourceCode.substring(
                matches.getFirst().tableContentStart(), matches.getFirst().tableContentEnd());
        String normalized = extracted.stripIndent().strip();
        assertThat(normalized).contains("a | b | sum");
    }

    @Test
    void shouldExtractMultipleTablesFromKotlinFile() {
        String sourceCode = """
                class MyTest {
                    @TableTest(\"""
                        x | result
                        1 | 2
                        \""")
                    fun test1(x: Int, result: Int) {}

                    @TableTest(\"""
                        name | age
                        Alice | 30
                        Bob | 25
                        \""")
                    fun test2(name: String, age: Int) {}
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
    void shouldHandleKotlinProperties() {
        String sourceCode = """
                class Test {
                    val testData = "some data"

                    @TableTest(\"""
                        x | y
                        1 | 2
                        \""")
                    fun testMethod(x: Int, y: Int) {
                        assertThat(x + 1).isEqualTo(y)
                    }
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
    void shouldHandleKotlinNestedClasses() {
        String sourceCode = """
                class Outer {
                    inner class Inner {
                        @TableTest(\"""
                            x | y
                            1 | 2
                            \""")
                        fun test() {}
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
    void shouldHandleKotlinCompanionObject() {
        String sourceCode = """
                class Test {
                    companion object {
                        @TableTest(\"""
                            x | y
                            1 | 2
                            \""")
                        fun testStatic(x: Int, y: Int) {}
                    }
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
    void shouldIgnoreKotlinStringTemplates() {
        String sourceCode = """
                class Test {
                    val example = "@TableTest(\\"\\"\\"\\nx|y\\n1|2\\n\\"\\"\\")"

                    @TableTest(\"""
                        real | data
                        3 | 4
                        \""")
                    fun actualTest(real: Int, data: Int) {}
                }
                """;

        List<TableMatch> matches = extractor.findAll(sourceCode);

        // Should only find the real annotation, not the one in the string
        assertThat(matches).hasSize(1);
        assertThat(sourceCode.substring(
                        matches.getFirst().tableContentStart(),
                        matches.getFirst().tableContentEnd()))
                .contains("real | data");
    }

    // ========== Additional Edge Cases and Robustness Tests ==========

    @Test
    void shouldCaptureCorrectPositions() {
        String sourceCode = """
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
    void shouldHandleInvalidAnnotationSyntax() {
        String sourceCode = """
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
        String sourceCode = """
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
        String sourceCode = """
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
        String sourceCode = """
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
    void shouldHandleEmptyTableContent() {
        String sourceCode = """
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
        String sourceCode = """
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
    void shouldMatchFullyQualifiedDeprecatedAnnotation() {
        String sourceCode = """
                class Test {
                    @io.github.nchaugen.tabletest.junit.TableTest(\"""
                        name | age
                        Alice | 30
                        \""")
                    void test1() {}
                }
                """;

        List<TableMatch> matches = extractor.findAll(sourceCode);

        // Fully qualified annotations are supported - matches on last identifier component
        assertThat(matches).hasSize(1);
        assertThat(sourceCode.substring(
                        matches.getFirst().tableContentStart(),
                        matches.getFirst().tableContentEnd()))
                .contains("name | age");
    }

    @Test
    void shouldMatchFullyQualifiedAnnotation() {
        String sourceCode = """
                class Test {
                    @org.tabletest.junit.TableTest(\"""
                        name | age
                        Alice | 30
                        \""")
                    void test1() {}
                }
                """;

        List<TableMatch> matches = extractor.findAll(sourceCode);

        // Fully qualified annotations are supported - matches on last identifier component
        assertThat(matches).hasSize(1);
        assertThat(sourceCode.substring(
                        matches.getFirst().tableContentStart(),
                        matches.getFirst().tableContentEnd()))
                .contains("name | age");
    }

    @Test
    void shouldMatchAnyTableTestAnnotationRegardlessOfPackage() {
        String sourceCode = """
                class Test {
                    // Known limitation: matches on simple name "TableTest", not full package
                    @com.example.different.TableTest(\"""
                        x | y
                        1 | 2
                        \""")
                    void test1() {}
                }
                """;

        List<TableMatch> matches = extractor.findAll(sourceCode);

        // Acceptable limitation: any annotation ending with .TableTest will be extracted
        // This is unlikely to cause issues in practice since @TableTest is relatively unique
        assertThat(matches).hasSize(1);
        assertThat(sourceCode.substring(
                        matches.getFirst().tableContentStart(),
                        matches.getFirst().tableContentEnd()))
                .contains("x | y");
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

    @Test
    void shouldDetectIndentationWithOpeningQuotesOnSeparateLine() {
        String sourceCode = """
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
    void shouldIgnoreWhitespaceAroundAnnotationWhenDetectingIndent() {
        String sourceCode = """
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

    @TableTest("""
        Scenario                        | sourceCode                                    | indent?
        no indentation (top-level)      | '@TableTest(\"""\\nx|y\\n1|2\\n\""")'         | ''
        shallow indentation (2 spaces)  | '  @TableTest(\"""\\nx|y\\n1|2\\n\""")'       | '  '
        standard indentation (4 spaces) | '    @TableTest(\"""\\nx|y\\n1|2\\n\""")'     | '    '
        deep indentation (8 spaces)     | '        @TableTest(\"""\\nx|y\\n1|2\\n\""")' | '        '
        """)
    void shouldDetectBaseIndentationVariations(String sourceCode, String indent) {
        String actualSource = sourceCode.replace("\\n", "\n");

        List<TableMatch> matches = extractor.findAll(actualSource);

        assertThat(matches).hasSize(1);
        String extractedIndent = actualSource.substring(
                matches.getFirst().baseIndentStart(), matches.getFirst().baseIndentEnd());
        assertThat(extractedIndent).isEqualTo(indent);
    }

    @TableTest("""
        Scenario            | sourceCode                                | indent?
        single tab          | '\t@TableTest(\"""\\nx|y\\n1|2\\n\""")'   | '\t'
        two tabs            | '\t\t@TableTest(\"""\\nx|y\\n1|2\\n\""")' | '\t\t'
        tab plus two spaces | '\t  @TableTest(\"""\\nx|y\\n1|2\\n\""")' | '\t  '
        two spaces plus tab | '  \t@TableTest(\"""\\nx|y\\n1|2\\n\""")' | '  \t'
        """)
    void shouldPreserveTabsAndSpacesInIndentation(String sourceCode, String indent) {
        String actualSource = sourceCode.replace("\\n", "\n");

        List<TableMatch> matches = extractor.findAll(actualSource);

        assertThat(matches).hasSize(1);
        String extractedIndent = actualSource.substring(
                matches.getFirst().baseIndentStart(), matches.getFirst().baseIndentEnd());
        assertThat(extractedIndent).isEqualTo(indent);
    }

    // ========== Escaped Quotes Tests ==========

    @Test
    void shouldIgnoreTableTestWithEscapedQuotesInTextBlock() {
        // Regression test for dogfooding bug: @TableTest(\""" ... \""") inside a text block
        // should be treated as content, not as a real annotation
        String sourceCode = """
                class Test {
                    void testMethod() {
                        var fixture = \"""
                            class Example {
                                @TableTest(\\\"""
                                    name | age
                                    Alice | 30
                                    \\\""")
                                void exampleTest() {}
                            }
                            \""";
                    }
                }
                """;

        List<TableMatch> matches = extractor.findAll(sourceCode);

        // Should NOT find any matches - the @TableTest is inside a text block (fixture)
        assertThat(matches).isEmpty();
    }

    @Test
    void shouldIgnoreEmptyTableTestWithEscapedQuotesInTextBlock() {
        // Specific case from TableTestExtractorTest.shouldHandleEmptyTableContent
        String sourceCode = """
                class Test {
                    void testMethod() {
                        var fixture = \"""
                            class Example {
                                @TableTest(\\\"""
                                    \\\""")
                                void exampleTest() {}
                            }
                            \""";
                    }
                }
                """;

        List<TableMatch> matches = extractor.findAll(sourceCode);

        // Should NOT find any matches - the @TableTest is inside a text block (fixture)
        assertThat(matches).isEmpty();
    }

    @Test
    void shouldDistinguishBetweenEscapedAndRealTableTest() {
        // Test with both: @TableTest inside text block (escaped) AND real @TableTest
        String sourceCode = """
                class Test {
                    void testMethod() {
                        var fixture = \"""
                            @TableTest(\\\"""
                                fake | data
                                1 | 2
                                \\\""")
                            \""";
                    }

                    @TableTest(\"""
                        real | data
                        3 | 4
                        \""")
                    void actualTest(int real, int data) {}
                }
                """;

        List<TableMatch> matches = extractor.findAll(sourceCode);

        // Should find exactly 1 match (the real annotation, not the one in the text block)
        assertThat(matches).hasSize(1);
        String extracted = sourceCode.substring(
                matches.getFirst().tableContentStart(), matches.getFirst().tableContentEnd());
        String normalized = extracted.stripIndent().strip();
        assertThat(normalized).contains("real | data");
        assertThat(normalized).doesNotContain("fake | data");
    }
}
