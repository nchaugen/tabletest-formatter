# 002. Use State Machine Parser for @TableTest Extraction

**Status:** Accepted

**Date:** 2026-01-03

## Context

The initial regex-based extractor (ADR-001) worked for the common case but had a critical limitation: it could not distinguish between `@TableTest` annotations in actual code versus those inside string literals or comments.

This limitation became critical during "dogfooding" - when we tried to use the formatter on its own test files. The test files contained example code with `@TableTest` patterns inside string literals:

```java
// Real annotation - SHOULD format
@TableTest("""
    x | y
    1 | 2
    """)
void testReal() {}

// Example in string literal - SHOULD IGNORE
String example = """
    @TableTest(\"\"\"
        x | y
        \"\"\")
    """;
```

The regex extractor matched both, causing the formatter to corrupt string literals in the test files.

## Decision

We will implement a custom state machine parser (`SmartTableTestExtractor`) that tracks parsing context to distinguish real annotations from those in strings or comments.

**Implementation approach:**
- ~250 lines of pure Java code
- Tracks parsing state: `CODE`, `LINE_COMMENT`, `BLOCK_COMMENT`, `STRING`, `TEXT_BLOCK`, `CHAR_LITERAL`
- Tracks brace depth to distinguish class scope from method bodies
- Only extracts `@TableTest` annotations found in the `CODE` state at class scope
- Handles escaped quotes, nested structures, and mixed line endings
- Supports both simple (`@TableTest`) and fully qualified (`@org.tabletest.junit.TableTest`) annotation names

## Options Considered

### Option 1: JavaParser

A mature Java parsing library that provides a full Abstract Syntax Tree (AST).

**Pros:**
- Full AST available for precise extraction
- Mature, well-maintained library
- Pure Java (no native dependencies)
- ~1.2MB JAR

**Cons:**
- **Java-only** - would not work for Kotlin files
- Would still need a separate solution for Kotlin support
- Heavier dependency than needed for our use case

**Why not chosen:** We want to support both Java and Kotlin with a single implementation. JavaParser only handles Java.

### Option 2: Tree-sitter

A modern incremental parsing library used by GitHub, various editors, and static analysis tools. Supports multiple languages including Java and Kotlin.

**Pros:**
- Robust, battle-tested parser
- Multi-language support (Java and Kotlin)
- Full AST available
- Used by major tools (GitHub, Atom, Neovim)

**Cons:**
- **Native library dependencies** - requires platform-specific C bindings (`.so`, `.dylib`, `.dll`)
- **Complex multi-platform build** - must build for Linux x64, Linux ARM64, macOS x64, macOS ARM64, Windows x64
- Requires GitHub Actions matrix or cross-compilation setup
- Binary artifacts must be packaged correctly per platform
- The `java-tree-sitter` library currently only ships x64 binaries (ARM64 missing)

**Why not chosen:** The multi-platform native library complexity is disproportionate to our needs. We need a simpler solution that "just works" everywhere without build complexity.

**Reference:** Issue `tabletest-formatter-swo` documents detailed investigation of tree-sitter multi-platform options.

### Option 3: Custom State Machine Parser ✅ **CHOSEN**

A lightweight parser that does "just enough" to solve our specific problem.

**Pros:**
- **Pure Java** - no native dependencies, works on all platforms
- **Works for both Java and Kotlin** with single implementation
- **Solves the core problem** - correctly distinguishes parsing contexts
- **Maintainable** - ~250 lines, straightforward state machine logic
- **No external dependencies** beyond JDK
- **Fast** - single-pass, O(n) performance
- Successfully tested with 44 test cases covering edge cases

**Cons:**
- Not a "proper" parser - no full AST available
- Custom code to maintain (though well-tested)
- Could miss obscure edge cases a full parser would handle

**Why chosen:** This is the right level of complexity for our needs. We don't need a full AST - we just need to extract text blocks from annotations while respecting parsing context. The state machine approach is simple, maintainable, and solves the problem completely.

## Trade-offs Accepted

**Simplicity over completeness:**
- We accept that this is not a full parser
- We're building "just enough" parsing for our specific need
- Edge cases that a full parser would handle may not work, but we have good test coverage for the cases that matter

**Maintainability over "proper" solution:**
- ~250 lines of custom code to maintain
- Acceptable because it's well-tested (44 tests) and the logic is straightforward
- Much simpler to maintain than multi-platform native library builds

## Consequences

### Positive

- **Solves the dogfooding problem** - formatter now works correctly on its own test files
- **Both Java and Kotlin support** - single implementation handles both languages
- **Platform independent** - pure Java, works everywhere without build complexity
- **Well-tested** - 44 tests covering edge cases (comments, strings, escapes, nested structures)
- **Supports fully qualified annotations** - `@org.tabletest.junit.TableTest`
- **Fast** - single-pass state machine, O(n) time complexity
- **Simple deployment** - no platform-specific artifacts needed

### Negative

- **Custom parser to maintain** - not using a standard parsing library
- **Not a full AST** - cannot answer general questions about code structure
- **Potential edge cases** - might miss obscure scenarios a full parser would handle

### Neutral

- Tree-sitter investigation documented but parked (issues `tabletest-formatter-swo`, `tabletest-formatter-2pj`, `tabletest-formatter-it6`)
- Can reconsider tree-sitter later if multi-platform build becomes easier
- The "expand-migrate-contract" pattern was used: extracted `TableTestExtractor` interface, kept `RegexTableTestExtractor` alongside new `SmartTableTestExtractor`

## Implementation Details

**States tracked:**
- `CODE` - Looking for `@TableTest` annotations
- `LINE_COMMENT` - Inside `// ...` (skip to newline)
- `BLOCK_COMMENT` - Inside `/* ... */` (skip to `*/`)
- `STRING` - Inside `"..."` (handle escapes)
- `TEXT_BLOCK` - Inside `"""..."""` (extract content!)
- `CHAR_LITERAL` - Inside `'...'` (handle special chars like `'{'`)
- `LOOKING_FOR_TEXT_BLOCK` - Found `@TableTest`, searching for opening `"""`

**Critical logic:**
- Brace depth tracking distinguishes class scope (where annotations live) from method bodies (where they don't)
- Comment detection takes precedence over all other parsing
- Escape sequence handling for quotes inside text blocks
- Supports mixed line endings (CRLF, LF)

**Test coverage:**
- String literal distinction (the critical dogfooding test)
- Annotations in line comments
- Annotations in block comments
- Multiple `@TableTest` in one file
- Fully qualified annotation names
- Text blocks with escaped quotes
- Character literals with special chars (`'{'`, `'/'`, `'"'`)
- Nested classes with annotations

## Success Metrics

- ✅ Formatter successfully processes its own test files (dogfooding works)
- ✅ 44 tests passing in `SmartTableTestExtractorTest`
- ✅ 251 total tests passing across all modules
- ✅ Zero platform-specific issues
- ✅ Clean deployment (no native library concerns)

## Links

- Supersedes: [ADR-001 - Use Regex for @TableTest Extraction](./001-use-regex-for-tabletest-extraction.md)
- Implementation: `SmartTableTestExtractor.java` (~250 lines)
- Tests: `SmartTableTestExtractorTest.java` (44 test cases)
- Tree-sitter investigation: Beads issues `tabletest-formatter-swo`, `tabletest-formatter-2pj`, `tabletest-formatter-it6`
- JavaParser investigation: Beads issue `tabletest-formatter-3b9`
- Interface extraction: Beads issue `tabletest-formatter-l1z`
