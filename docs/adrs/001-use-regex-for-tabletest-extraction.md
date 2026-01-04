# 001. Use Regex for @TableTest Extraction

**Status:** Superseded by [ADR-002](./002-use-state-machine-parser.md)

**Date:** 2025-12-29

## Context

The tabletest-formatter needs to extract `@TableTest` annotations and their text block content from Java source files to format the embedded tables. The extraction must:
- Find `@TableTest` annotations at the class scope
- Extract the text block content within the annotation
- Preserve base indentation of the annotation
- Work with both simple and fully qualified annotation names

We need a solution that:
- Can be implemented quickly to get an MVP working
- Handles the common case correctly
- Allows us to validate the formatting logic before investing in a more robust parser

## Decision

We will use a regex-based extractor to find and extract `@TableTest` annotations.

**Implementation:**
```java
Pattern.compile(
    "^([ \\t]*)@TableTest\\s*\\([^)]*?\"\"\"(.*?)\"\"\"[^)]*?\\)",
    Pattern.DOTALL | Pattern.MULTILINE
)
```

The regex:
- Captures base indentation (spaces/tabs)
- Matches `@TableTest` annotation
- Extracts text block content between `"""`
- Uses non-greedy matching to handle multiple annotations in one file

## Consequences

### Positive

- Quick to implement (~50 lines of code)
- Sufficient for initial MVP and validation
- No external dependencies
- Works for the common case (annotations at class scope)
- Enables us to focus on the core formatting logic first

### Negative

- **Cannot distinguish parsing context** - matches patterns in strings, comments, and code equally
- Limited error handling for malformed annotations
- Assumes specific whitespace patterns
- Not robust against edge cases

### Neutral

- Accepted as "good enough to start" with the understanding we may need a better solution later
- Trade-off: Development velocity now vs correctness later
- Can be replaced without affecting the core formatting logic (clean interface boundary)

## Known Limitations

The regex approach has a critical limitation that we accepted as a known trade-off:

**String Literal Blindness:**
The regex cannot distinguish between:
```java
// Real annotation in code - SHOULD extract
@TableTest("""
    x | y
    1 | 2
    """)
void test() {}

// Annotation inside string literal - SHOULD IGNORE
String example = """
    @TableTest(\"\"\"
        x | y
        \"\"\")
    """;
```

This limitation would become critical when "dogfooding" - using the formatter on its own test files.

## When This Decision Was Revisited

This decision was superseded in January 2026 when dogfooding revealed that the regex extractor would incorrectly match `@TableTest` patterns inside string literals, causing the formatter to break on its own test files. See [ADR-002](./002-use-state-machine-parser.md) for the replacement approach.

## Links

- Superseded by: [ADR-002 - Use State Machine Parser](./002-use-state-machine-parser.md)
- Implementation: `RegexTableTestExtractor.java` (deprecated but kept for reference)
