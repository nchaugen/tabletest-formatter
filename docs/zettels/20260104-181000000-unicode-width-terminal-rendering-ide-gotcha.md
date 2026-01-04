# 20260104-181000000 Unicode Width Terminal Rendering vs IDE Display

#gotcha #unicode #terminal #rendering #cjk

IDE source editors may display CJK/Unicode text as misaligned even when the formatting is correct for actual terminals, because IDE fonts don't always render Unicode with true monospace widths.

## Context

When formatting tables with Unicode characters (CJK characters, emojis, etc.) for terminal display, we need to calculate display width correctly to align columns properly. A simple character count doesn't work because:
- ASCII characters are 1 column wide
- CJK characters (Chinese, Japanese, Korean) are 2 columns wide
- Combining characters are 0 columns wide
- Emojis vary (often 2 columns)

## The Gotcha

**IntelliJ IDEA's source editor may show CJK text as misaligned even when the table formatting is correct.**

Example - this looks misaligned in the IDE:
```
name    | age | city
Alice   | 30  | 東京
Bob     | 25  | Seoul
陳大文  | 40  | 台北
```

But when you run it in an actual terminal, it displays perfectly aligned!

## Why It Happens

**Our formatter uses the wcwidth algorithm** (IEEE Std 1002.1-2001):
- Based on Unicode 5.0 (2007) specifications
- Accurately calculates terminal display widths
- CJK characters = 2 columns
- Combining marks = 0 columns

**IDE editors use their own font rendering:**
- Font selection affects character widths
- Some fonts don't render CJK as true double-width
- IDE may use proportional spacing for readability
- What you see in the editor ≠ what terminal displays

## How to Verify

Always test Unicode formatting in actual terminals, not just the IDE editor:

```bash
# Output to terminal
cat src/test/java/YourTest.java

# Or pipe through the formatter
./tabletest-formatter format src/test/java/YourTest.java | cat
```

**Don't "fix" alignment based on IDE appearance** - trust the wcwidth calculation for terminal output.

## Implementation Details

Our `DisplayWidth` class:
- Adapted wcwidth.c from termd project (Apache 2.0)
- Binary search on 129 sorted combining character ranges
- CJK width detection via Unicode block checks
- Based on Unicode 5.0 (2007)

**Modern emojis caveat:** Unicode 5.0 predates most emoji standardization, so modern emojis may appear as width 1 in our implementation but render as width 2 in some terminals. This is a known limitation.

## Key Format Rule

Separator format: `"| "` (pipe-space), NOT `" | "` (space-pipe-space)

This is important for consistent column alignment across all character types.

## Related

- Implementation: `DisplayWidth.java` (~150 lines)
- Test coverage: `DisplayWidthTest.java`
- wcwidth reference: IEEE Std 1002.1-2001

## References

- wcwidth implementation: Adapted from termd project (Apache 2.0)
- IEEE Std 1002.1-2001 specification
- Unicode 5.0 character width specifications
