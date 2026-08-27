package org.tabletest.formatter.core;

import org.junit.jupiter.api.DisplayName;
import org.tabletest.junit.Description;
import org.tabletest.junit.TableTest;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Display width")
@Description("""
        The formatter measures alignment in terminal display columns, not in characters. These
        rules say how wide a value renders in a monospaced font. The Column width feature and
        the Cell padding and alignment feature build on them.
        """)
class DisplayWidthTest {

    @DisplayName("Measures a code point as zero, one, or two columns")
    @Description("""
            A code point renders at one of three widths, and its script decides which. An ordinary
            letter, digit or sign takes one column whatever alphabet it belongs to. A glyph drawn on
            a square body takes two. A code point that puts no glyph of its own on the line takes
            none, and there are two kinds: a control character, and a mark that combines with the
            letter before it.

            A code point is written as a number, because the two zero-width kinds have no glyph to
            write. Each row carries one of each script or kind its width covers:

            One column — A, z, 0, æ, «, alpha, Cyrillic pe, Arabic meem, Hebrew shin, a box-drawing
            corner, the euro sign, the summation sign.
            Two columns — the ideograph for middle, the ideograph for you, the hiragana ko, the
            Hangul syllable guk, a grinning face, fullwidth A.
            No columns — the null character, a line feed, a tab, a combining acute accent, a
            combining diaeresis.
            """)
    @TableTest("""
        Scenario                                 | Code point                                       | Width?
        An ordinary letter, digit or sign        | {65, 122, 48, 230, 171, 945, 1055, 1605, 1513, 9484, 8364, 8721} | 1
        A glyph drawn on a square body           | {20013, 20320, 12371, 44397, 128512, 65313}      | 2
        A control character or a combining mark  | {0, 10, 9, 769, 776}                             | 0
        """)
    void measuresCodePointWidth(int codePoint, int width) {
        assertThat(DisplayWidth.ofCodePoint(codePoint)).isEqualTo(width);
    }

    @DisplayName("Adds the code point widths to measure a string")
    @Description("""
            A string is as wide as its code points together, so a string of square-bodied glyphs is
            twice as wide as its character count and a mixed string is wider than its count without
            being twice it. The rule above says what each code point contributes.

            A null string and an empty string are both nothing to draw, and the formatter treats
            them alike rather than rejecting the null.
            """)
    @TableTest("""
        Scenario                        | Text           | Width?
        Every glyph one column          | Hello          | 5
        Every glyph two columns         | 你好世界       | 8
        One-column and two-column mixed | Hello 👋 World | 14
        A null string                   |                | 0
        An empty string                 | ''             | 0
        """)
    void measuresStringWidth(String text, int width) {
        assertThat(DisplayWidth.of(text)).isEqualTo(width);
    }
}
