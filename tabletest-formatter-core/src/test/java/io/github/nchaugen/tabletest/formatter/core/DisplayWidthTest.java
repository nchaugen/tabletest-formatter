package io.github.nchaugen.tabletest.formatter.core;

import org.junit.jupiter.api.Test;
import org.tabletest.junit.TableTest;

import static org.assertj.core.api.Assertions.assertThat;

class DisplayWidthTest {

    @TableTest("""
        Scenario                     | Text                | Expected Width
        ASCII single letter          | A                   | 1
        ASCII word                   | Hello               | 5
        ASCII numbers                | 123                 | 3
        CJK single character         | 中                  | 2
        CJK two characters           | 你好                | 4
        CJK four characters          | 你好世界            | 8
        Japanese hiragana            | こんにちは          | 10
        Japanese hiragana with kanji | こんにちは世界      | 14
        Korean short greeting        | 안녕                | 4
        Korean greeting              | 안녕하세요          | 10
        Emoji grinning face          | 😀                  | 2
        Emoji waving hand            | 👋                  | 2
        Emoji coffee                 | ☕                  | 2
        Mixed ASCII and emoji        | Hello 👋 World      | 14
        Mixed text with emoji        | Café ☕ tastes good | 19
        Scandinavian æ               | æ                   | 1
        Scandinavian ø repeated      | øøø                 | 3
        Scandinavian å repeated      | ååå                 | 3
        Accented word naïve          | naïve               | 5
        Accented word résumé         | résumé              | 6
        Greek letters with spaces    | α β γ               | 5
        Greek greeting               | Γεια σου κόσμε      | 14
        Cyrillic greeting            | Привет мир          | 10
        Arabic greeting              | مرحبا بالعالم       | 13
        Hebrew greeting              | שלום עולם           | 9
        Mathematical symbols         | ∑ ∏ ∫ √             | 7
        Box drawing characters       | ┌─┐│ │└─┘           | 9
        Currency symbols             | $€£¥₹               | 5
        Quotation marks              | «»""''—–            | 8
        Null string                  |                     | 0
        Empty string                 | ''                  | 0
        """)
    void shouldCalculateDisplayWidthOfStrings(String text, int expectedWidth) {
        assertThat(DisplayWidth.of(text)).isEqualTo(expectedWidth);
    }

    @TableTest("""
        Scenario          | Code Point | Expected Width
        ASCII letter A    | 65         | 1
        ASCII letter z    | 122        | 1
        ASCII digit 0     | 48         | 1
        CJK character 中  | 20013      | 2
        CJK character 你  | 20320      | 2
        Null character    | 0          | 0
        Newline character | 10         | 0
        Tab character     | 9          | 0
        """)
    void shouldCalculateDisplayWidthOfCodePoints(int codePoint, int expectedWidth) {
        assertThat(DisplayWidth.ofCodePoint(codePoint)).isEqualTo(expectedWidth);
    }

    @Test
    void shouldReturnZeroForNullString() {
        assertThat(DisplayWidth.of(null)).isEqualTo(0);
    }
}
