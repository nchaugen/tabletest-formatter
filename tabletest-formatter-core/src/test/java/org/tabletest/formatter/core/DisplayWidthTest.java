package org.tabletest.formatter.core;

import org.junit.jupiter.api.DisplayName;
import org.tabletest.junit.Description;
import org.tabletest.junit.TableTest;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Display width rules")
@Description("""
        Alignment is measured in terminal display columns, not in characters. These
        rules define how wide a value renders in a monospaced font; the Column width
        and Cell padding rules build on them.
        """)
class DisplayWidthTest {

    @DisplayName("Each code point is zero, one or two columns wide")
    @Description("""
            Typical Latin characters occupy one display column. CJK characters occupy
            two. Control characters (null, newline, tab) occupy none.
            """)
    @TableTest("""
        Scenario          | Code point | Width?
        ASCII letter A    | 65         | 1
        ASCII letter z    | 122        | 1
        ASCII digit 0     | 48         | 1
        CJK character 中  | 20013      | 2
        CJK character 你  | 20320      | 2
        Null character    | 0          | 0
        Newline character | 10         | 0
        Tab character     | 9          | 0
        """)
    void measuresCodePointWidth(int codePoint, int width) {
        assertThat(DisplayWidth.ofCodePoint(codePoint)).isEqualTo(width);
    }

    @DisplayName("String width is the sum of its code point widths")
    @Description("""
            Emoji and CJK render two columns wide, so mixed-script strings are wider
            than their character count. A null or empty string has width zero.
            """)
    @TableTest("""
        Scenario                     | Text                | Width?
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
    void measuresStringWidth(String text, int width) {
        assertThat(DisplayWidth.of(text)).isEqualTo(width);
    }
}
