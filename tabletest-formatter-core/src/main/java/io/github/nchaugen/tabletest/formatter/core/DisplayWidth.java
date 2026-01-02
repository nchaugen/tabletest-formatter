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
package io.github.nchaugen.tabletest.formatter.core;

/**
 * Calculates display width of Unicode characters for terminal-style formatting.
 *
 * <p>This implementation is adapted from Markus Kuhn's wcwidth.c (Unicode 5.0)
 * and follows IEEE Std 1002.1-2001 specifications for character width calculation.</p>
 *
 * <p>Display width rules:
 * <ul>
 *   <li>Regular ASCII and Latin characters: width 1</li>
 *   <li>CJK ideographic characters: width 2</li>
 *   <li>Combining characters (diacritics, etc.): width 0</li>
 *   <li>Control characters: width 0 (treated as non-printing)</li>
 * </ul>
 *
 * @see <a href="http://www.cl.cam.ac.uk/~mgk25/ucs/wcwidth.c">wcwidth.c</a>
 * @see <a href="https://github.com/termd/termd">termd project</a>
 */
public class DisplayWidth {

    /**
     * Calculates the display width of a Unicode codepoint.
     *
     * @param codePoint the Unicode codepoint
     * @return the display width: 0 for null/combining/control, 1 for regular, 2 for CJK/fullwidth
     */
    public static int ofCodePoint(int codePoint) {
        return isZeroWidth(codePoint) ? 0 : isWideCharacter(codePoint) ? 2 : 1;
    }

    /**
     * Calculates the display width of a string.
     *
     * <p><strong>Example usage:</strong>
     * <pre>
     * DisplayWidth.of("Hello")       // Returns: 5 (regular ASCII)
     * DisplayWidth.of("你好")         // Returns: 4 (2 CJK characters × 2 width each)
     * DisplayWidth.of("café")        // Returns: 4 (combining accent has width 0)
     * DisplayWidth.of("😀")          // Returns: 1 (Unicode 5.0 implementation)
     * DisplayWidth.of(null)          // Returns: 0 (null handling)
     * </pre>
     *
     * @param text the string to measure (null is treated as empty string)
     * @return the total display width, or 0 if text is null
     */
    public static int of(String text) {
        return text == null
                ? 0
                : text.codePoints().map(DisplayWidth::ofCodePoint).sum();
    }

    private static boolean isZeroWidth(int codePoint) {
        return isNullCharacter(codePoint) || isControlCharacter(codePoint) || isCombiningCharacter(codePoint);
    }

    private static boolean isNullCharacter(int codePoint) {
        return codePoint == 0;
    }

    private static boolean isControlCharacter(int codePoint) {
        return codePoint < 32 || (codePoint >= 0x7f && codePoint < 0xa0);
    }

    private static boolean isCombiningCharacter(int codePoint) {
        return binarySearchRanges(codePoint, COMBINING);
    }

    /**
     * Binary search to find if a value falls within any of the sorted, non-overlapping ranges.
     *
     * @param value the value to search for
     * @param ranges sorted array of [start, end] ranges
     * @return true if value falls within any range, false otherwise
     */
    private static boolean binarySearchRanges(int value, int[][] ranges) {
        int low = 0;
        int high = ranges.length - 1;

        while (low <= high) {
            int mid = (low + high) >>> 1;
            int[] range = ranges[mid];

            if (value < range[0]) {
                high = mid - 1;
            } else if (value > range[1]) {
                low = mid + 1;
            } else {
                return true;
            }
        }

        return false;
    }

    private static boolean isWideCharacter(int codePoint) {
        return isHangulJamo(codePoint)
                || isAngleBracket(codePoint)
                || isCjkUnifiedIdeograph(codePoint)
                || isHangulSyllable(codePoint)
                || isCjkCompatibilityIdeograph(codePoint)
                || isVerticalForm(codePoint)
                || isCjkCompatibilityForm(codePoint)
                || isFullwidthForm(codePoint)
                || isFullwidthSymbol(codePoint)
                || isCjkExtension(codePoint);
    }

    private static boolean isHangulJamo(int codePoint) {
        return codePoint >= 0x1100 && codePoint <= 0x115f;
    }

    private static boolean isAngleBracket(int codePoint) {
        return codePoint == 0x2329 || codePoint == 0x232a;
    }

    private static boolean isCjkUnifiedIdeograph(int codePoint) {
        return codePoint >= 0x2e80 && codePoint <= 0xa4cf && codePoint != 0x303f;
    }

    private static boolean isHangulSyllable(int codePoint) {
        return codePoint >= 0xac00 && codePoint <= 0xd7a3;
    }

    private static boolean isCjkCompatibilityIdeograph(int codePoint) {
        return codePoint >= 0xf900 && codePoint <= 0xfaff;
    }

    private static boolean isVerticalForm(int codePoint) {
        return codePoint >= 0xfe10 && codePoint <= 0xfe19;
    }

    private static boolean isCjkCompatibilityForm(int codePoint) {
        return codePoint >= 0xfe30 && codePoint <= 0xfe6f;
    }

    private static boolean isFullwidthForm(int codePoint) {
        return codePoint >= 0xff00 && codePoint <= 0xff60;
    }

    private static boolean isFullwidthSymbol(int codePoint) {
        return codePoint >= 0xffe0 && codePoint <= 0xffe6;
    }

    private static boolean isCjkExtension(int codePoint) {
        return (codePoint >= 0x20000 && codePoint <= 0x2fffd) || (codePoint >= 0x30000 && codePoint <= 0x3fffd);
    }

    /**
     * Sorted list of non-overlapping intervals of non-spacing characters.
     * Generated by "uniset +cat=Me +cat=Mn +cat=Cf -00AD +1160-11FF +200B c"
     */
    private static final int[][] COMBINING = {
        {0x0300, 0x036F}, {0x0483, 0x0486}, {0x0488, 0x0489},
        {0x0591, 0x05BD}, {0x05BF, 0x05BF}, {0x05C1, 0x05C2},
        {0x05C4, 0x05C5}, {0x05C7, 0x05C7}, {0x0600, 0x0603},
        {0x0610, 0x0615}, {0x064B, 0x065E}, {0x0670, 0x0670},
        {0x06D6, 0x06E4}, {0x06E7, 0x06E8}, {0x06EA, 0x06ED},
        {0x070F, 0x070F}, {0x0711, 0x0711}, {0x0730, 0x074A},
        {0x07A6, 0x07B0}, {0x07EB, 0x07F3}, {0x0901, 0x0902},
        {0x093C, 0x093C}, {0x0941, 0x0948}, {0x094D, 0x094D},
        {0x0951, 0x0954}, {0x0962, 0x0963}, {0x0981, 0x0981},
        {0x09BC, 0x09BC}, {0x09C1, 0x09C4}, {0x09CD, 0x09CD},
        {0x09E2, 0x09E3}, {0x0A01, 0x0A02}, {0x0A3C, 0x0A3C},
        {0x0A41, 0x0A42}, {0x0A47, 0x0A48}, {0x0A4B, 0x0A4D},
        {0x0A70, 0x0A71}, {0x0A81, 0x0A82}, {0x0ABC, 0x0ABC},
        {0x0AC1, 0x0AC5}, {0x0AC7, 0x0AC8}, {0x0ACD, 0x0ACD},
        {0x0AE2, 0x0AE3}, {0x0B01, 0x0B01}, {0x0B3C, 0x0B3C},
        {0x0B3F, 0x0B3F}, {0x0B41, 0x0B43}, {0x0B4D, 0x0B4D},
        {0x0B56, 0x0B56}, {0x0B82, 0x0B82}, {0x0BC0, 0x0BC0},
        {0x0BCD, 0x0BCD}, {0x0C3E, 0x0C40}, {0x0C46, 0x0C48},
        {0x0C4A, 0x0C4D}, {0x0C55, 0x0C56}, {0x0CBC, 0x0CBC},
        {0x0CBF, 0x0CBF}, {0x0CC6, 0x0CC6}, {0x0CCC, 0x0CCD},
        {0x0CE2, 0x0CE3}, {0x0D41, 0x0D43}, {0x0D4D, 0x0D4D},
        {0x0DCA, 0x0DCA}, {0x0DD2, 0x0DD4}, {0x0DD6, 0x0DD6},
        {0x0E31, 0x0E31}, {0x0E34, 0x0E3A}, {0x0E47, 0x0E4E},
        {0x0EB1, 0x0EB1}, {0x0EB4, 0x0EB9}, {0x0EBB, 0x0EBC},
        {0x0EC8, 0x0ECD}, {0x0F18, 0x0F19}, {0x0F35, 0x0F35},
        {0x0F37, 0x0F37}, {0x0F39, 0x0F39}, {0x0F71, 0x0F7E},
        {0x0F80, 0x0F84}, {0x0F86, 0x0F87}, {0x0F90, 0x0F97},
        {0x0F99, 0x0FBC}, {0x0FC6, 0x0FC6}, {0x102D, 0x1030},
        {0x1032, 0x1032}, {0x1036, 0x1037}, {0x1039, 0x1039},
        {0x1058, 0x1059}, {0x1160, 0x11FF}, {0x135F, 0x135F},
        {0x1712, 0x1714}, {0x1732, 0x1734}, {0x1752, 0x1753},
        {0x1772, 0x1773}, {0x17B4, 0x17B5}, {0x17B7, 0x17BD},
        {0x17C6, 0x17C6}, {0x17C9, 0x17D3}, {0x17DD, 0x17DD},
        {0x180B, 0x180D}, {0x18A9, 0x18A9}, {0x1920, 0x1922},
        {0x1927, 0x1928}, {0x1932, 0x1932}, {0x1939, 0x193B},
        {0x1A17, 0x1A18}, {0x1B00, 0x1B03}, {0x1B34, 0x1B34},
        {0x1B36, 0x1B3A}, {0x1B3C, 0x1B3C}, {0x1B42, 0x1B42},
        {0x1B6B, 0x1B73}, {0x1DC0, 0x1DCA}, {0x1DFE, 0x1DFF},
        {0x200B, 0x200F}, {0x202A, 0x202E}, {0x2060, 0x2063},
        {0x206A, 0x206F}, {0x20D0, 0x20EF}, {0x302A, 0x302F},
        {0x3099, 0x309A}, {0xA806, 0xA806}, {0xA80B, 0xA80B},
        {0xA825, 0xA826}, {0xFB1E, 0xFB1E}, {0xFE00, 0xFE0F},
        {0xFE20, 0xFE23}, {0xFEFF, 0xFEFF}, {0xFFF9, 0xFFFB},
        {0x10A01, 0x10A03}, {0x10A05, 0x10A06}, {0x10A0C, 0x10A0F},
        {0x10A38, 0x10A3A}, {0x10A3F, 0x10A3F}, {0x1D167, 0x1D169},
        {0x1D173, 0x1D182}, {0x1D185, 0x1D18B}, {0x1D1AA, 0x1D1AD},
        {0x1D242, 0x1D244}, {0xE0001, 0xE0001}, {0xE0020, 0xE007F},
        {0xE0100, 0xE01EF}
    };
}
