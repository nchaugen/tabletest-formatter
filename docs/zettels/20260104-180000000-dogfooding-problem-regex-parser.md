# 20260104-180000000 The Dogfooding Problem: Why Regex Failed for TableTest Extraction

#gotcha #parsing #dogfooding #regex

Regex-based annotation extraction fails when formatting the formatter's own test files because regex cannot distinguish between real annotations in code and example annotations inside string literals.

## Context

When building the tabletest-formatter, we initially used a regex pattern to extract `@TableTest` annotations from source files. This worked fine for basic cases and allowed us to validate the core formatting logic quickly.

The problem emerged when we tried to "dogfood" - use the formatter on its own test files.

## The Gotcha

Test files for the formatter naturally contain examples of `@TableTest` annotations inside string literals:

```java
// Real annotation - SHOULD extract and format
@TableTest("""
    name | age
    Alice | 30
    """)
void testFormatting() {}

// Example in documentation string - SHOULD IGNORE
String example = """
    Example usage:
    @TableTest(\"""
        name | age
        Alice | 30
        \""")
    void test() {}
    """;
```

**The regex matched BOTH patterns**, causing the formatter to:
1. Extract the example from the string literal
2. Format it
3. Replace it back into the source
4. **Corrupt the string literal content**

## Why It Happens

Regex operates purely on pattern matching without understanding parsing context:
- Cannot distinguish between CODE state and STRING state
- Cannot track whether we're inside a string literal, comment, or actual code
- Sees only characters and patterns, not semantic meaning

The regex `@TableTest\s*\([^)]*?"""(.*?)"""[^)]*?\)` will match anywhere it finds this pattern, regardless of context.

## How We Solved It

Replaced regex with a custom state machine parser (`SmartTableTestExtractor`) that:
- Tracks parsing state: `CODE`, `STRING`, `LINE_COMMENT`, `BLOCK_COMMENT`, `TEXT_BLOCK`
- Only extracts `@TableTest` when in the `CODE` state
- Properly handles escaped quotes and nested structures
- ~250 lines of pure Java, well-tested with 44 test cases

See [ADR-002](../adrs/002-use-state-machine-parser.md) for the full decision rationale.

## Lesson Learned

**Dogfooding reveals blind spots early.**

When building formatters, linters, or code transformation tools:
1. Test on your own codebase as soon as possible
2. Your test files will naturally contain edge cases (examples in strings, commented-out code)
3. These edge cases often reveal fundamental limitations in your approach
4. Better to discover this early (MVP stage) than after users adopt the tool

The regex approach was still the right choice initially - it let us validate the formatting logic quickly. But dogfooding revealed when it was time to invest in a proper solution.

## Related

- [[20260104-180500000-state-machine-vs-full-parser]] - Why state machine over full parser
- [[../adrs/002-use-state-machine-parser]] - ADR documenting the solution

## References

- Original regex implementation: `RegexTableTestExtractor.java` (deprecated)
- State machine implementation: `SmartTableTestExtractor.java`
- Critical test case: `SmartTableTestExtractorTest.shouldDistinguishRealAnnotationsFromStringLiterals()`
