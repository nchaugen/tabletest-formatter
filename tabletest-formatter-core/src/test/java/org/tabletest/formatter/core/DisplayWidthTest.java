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
            A code point renders at one of three widths, by its Unicode properties. An ordinary
            letter, digit or sign takes one column, in any alphabet, and so does a symbol that
            Unicode draws as text by default. A glyph drawn on a square body takes two columns: an
            East Asian wide or fullwidth character, or an emoji that Unicode draws as a picture. A
            code point that draws no glyph of its own takes none: a control character, a format
            character, a mark that combines with the letter before it, and a Hangul vowel or final
            consonant that joins a syllable.

            Each row holds the code point as a number, because the zero-width kinds have no glyph
            to write. Each row carries one code point per script or kind of its width:

            One column: A, z, 0, æ, «, alpha, Cyrillic pe, Arabic meem, Hebrew shin, a box-drawing
            corner, the euro sign, the summation sign, the sun symbol.
            Two columns: the ideograph for middle, the ideograph for you, the hiragana ko, the
            Hangul syllable guk, a grinning face, fullwidth A, a Tangut ideograph.
            No columns: the null character, a line feed, a tab, a combining acute accent, a
            combining diaeresis, a soft hyphen, the zero-width joiner, the Hangul vowel a.
            """)
    @TableTest("""
        Scenario                                    | Code point                                                             | Width?
        An ordinary letter, digit or sign           | {65, 122, 48, 230, 171, 945, 1055, 1605, 1513, 9484, 8364, 8721, 9728} | 1
        A glyph drawn on a square body              | {20013, 20320, 12371, 44397, 128512, 65313, 94208}                     | 2
        A code point that draws no glyph of its own | {0, 10, 9, 769, 776, 173, 8205, 4449}                                  | 0
        """)
    void measuresCodePointWidth(int codePoint, int width) {
        assertThat(DisplayWidth.ofCodePoint(codePoint)).isEqualTo(width);
    }

    @DisplayName("Measures a string one visible character at a time")
    @Description("""
            A string is as wide as the characters a reader sees in it. A visible character can be
            several code points: a letter and its accent, a flag, emoji joined into one picture, an
            emoji with a skin tone, a symbol followed by the emoji selector, a Hangul syllable
            written as its letters. Each such character takes the columns of the widest kind it
            holds, once, so a joined emoji takes two columns, as a terminal draws it.

            A null string and an empty string both draw nothing. The formatter measures both as
            zero, and rejects neither.
            """)
    @TableTest("""
        Scenario                             | Text           | Width?
        Every glyph one column               | Hello          | 5
        Every glyph two columns              | 你好世界       | 8
        One-column and two-column mixed      | Hello 👋 World | 14
        A letter and a combining accent      | é              | 1
        A symbol with the emoji selector     | ☀️             | 2
        A flag                               | 🇳🇴             | 2
        Emoji joined into one picture        | 👨‍👩‍👧             | 2
        An emoji with a skin tone            | 👍🏽             | 2
        A keycap                             | 1️⃣             | 2
        A Hangul syllable written as letters | 한             | 2
        A null string                        |                | 0
        An empty string                      | ''             | 0
        """)
    void measuresStringWidth(String text, int width) {
        assertThat(DisplayWidth.of(text)).isEqualTo(width);
    }
}
