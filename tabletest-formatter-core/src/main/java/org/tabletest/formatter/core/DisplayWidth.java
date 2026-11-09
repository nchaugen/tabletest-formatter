/*
 * Copyright 2025-present Nils Christian Haugen
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.tabletest.formatter.core;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Measures how many terminal columns a text takes, by Unicode data, as the TableTest format
 * specifies it (SPEC 4.8).
 *
 * <p>The text is measured one visible character at a time: one extended grapheme cluster, such
 * as a letter and its accent, a flag, or emoji joined into one picture. A character takes:
 * <ul>
 *   <li>2 columns when a code point in it is East Asian wide or fullwidth, or has emoji
 *       presentation, or when U+FE0F (the emoji presentation selector) follows a code point in it;</li>
 *   <li>0 columns when every code point in it is a control character, a format character, a
 *       combining mark, or a Hangul vowel or final consonant that joins a syllable;</li>
 *   <li>1 column otherwise.</li>
 * </ul>
 */
public class DisplayWidth {

    private static final int EMOJI_PRESENTATION_SELECTOR = 0xFE0F;

    /** One extended grapheme cluster: what a reader sees as one character. */
    private static final Pattern CHARACTER = Pattern.compile("\\X");

    /**
     * Calculates the display width of one code point on its own.
     *
     * @param codePoint the Unicode code point
     * @return 0, 1 or 2
     */
    public static int ofCodePoint(int codePoint) {
        return ofCharacter(new int[] {codePoint});
    }

    /**
     * Calculates the display width of a string.
     *
     * <p><strong>Example usage:</strong>
     * <pre>
     * DisplayWidth.of("Hello")   // Returns: 5
     * DisplayWidth.of("你好")     // Returns: 4 (two wide characters)
     * DisplayWidth.of("café")    // Returns: 4 (an accent adds no column)
     * DisplayWidth.of("🇳🇴")      // Returns: 2 (a flag is one character)
     * DisplayWidth.of(null)      // Returns: 0
     * </pre>
     *
     * @param text the string to measure (null is treated as an empty string)
     * @return the total display width, or 0 if text is null
     */
    public static int of(String text) {
        if (text == null) {
            return 0;
        }
        Matcher characters = CHARACTER.matcher(text);
        return characters
                .results()
                .mapToInt(
                        character -> ofCharacter(character.group().codePoints().toArray()))
                .sum();
    }

    private static int ofCharacter(int[] codePoints) {
        if (Arrays.stream(codePoints).anyMatch(DisplayWidth::isWide) || hasEmojiSelector(codePoints)) {
            return 2;
        }
        return Arrays.stream(codePoints).allMatch(DisplayWidth::drawsNoGlyph) ? 0 : 1;
    }

    private static boolean isWide(int codePoint) {
        return WideCodePoints.contains(codePoint);
    }

    private static boolean hasEmojiSelector(int[] codePoints) {
        return Arrays.stream(codePoints).skip(1).anyMatch(codePoint -> codePoint == EMOJI_PRESENTATION_SELECTOR);
    }

    private static boolean drawsNoGlyph(int codePoint) {
        return switch (Character.getType(codePoint)) {
            case Character.CONTROL, Character.FORMAT, Character.NON_SPACING_MARK, Character.ENCLOSING_MARK -> true;
            default -> isJoiningHangulLetter(codePoint);
        };
    }

    /** A Hangul vowel or final consonant (Hangul_Syllable_Type V or T), which joins a syllable. */
    private static boolean isJoiningHangulLetter(int codePoint) {
        return (codePoint >= 0x1160 && codePoint <= 0x11FF) || (codePoint >= 0xD7B0 && codePoint <= 0xD7FF);
    }
}
