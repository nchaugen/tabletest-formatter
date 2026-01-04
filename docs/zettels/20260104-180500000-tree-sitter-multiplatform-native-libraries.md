# 20260104-180500000 Tree-Sitter Multi-Platform Native Library Complexity

#troubleshooting #tree_sitter #native_libraries #build #gotcha

Tree-sitter requires platform-specific native libraries (C bindings), making multi-platform Java distribution significantly more complex than pure-Java solutions.

## Problem

The `java-tree-sitter` library uses JNI (Java Native Interface) to call into tree-sitter's C library. This means you need platform-specific binary files:
- Linux x64: `libjava-tree-sitter.so`
- Linux ARM64: `libjava-tree-sitter.so` (different binary!)
- macOS x64: `libjava-tree-sitter.dylib`
- macOS ARM64: `libjava-tree-sitter.dylib` (different binary!)
- Windows x64: `java-tree-sitter.dll`

## Symptoms

**On Apple Silicon (ARM64 Mac):**
```
java.lang.UnsatisfiedLinkError:
  /path/to/libjava-tree-sitter.dylib:
  dlopen(): file was built for x86_64 which is not the architecture being run (arm64)
```

**Root cause:** The published `java-tree-sitter` JAR (as of Jan 2026) only includes x86_64 binaries, not ARM64.

## Why This Is Hard

### Building Native Libraries

You need a C compiler toolchain for each target platform:
- macOS requires Xcode Command Line Tools
- Linux requires gcc/clang
- Windows requires MSVC or MinGW
- Cross-compilation is possible but complex

The `java-tree-sitter` project provides a Python build script:
```bash
python build.py -s Darwin -a arm64    # macOS ARM64
python build.py -s Darwin -a x86_64   # macOS Intel
python build.py -s Linux -a x86_64    # Linux x64
```

But you need to run this on each platform (or set up cross-compilation).

### GitHub Actions Multi-Platform Build

To create a JAR that works everywhere, you need:
1. **Matrix build** across multiple runners:
   - `macos-13` (x64)
   - `macos-14` (ARM64)
   - `ubuntu-latest` (x64)
   - `windows-latest` (x64)
2. **Build native library** on each platform
3. **Combine all binaries** into one multi-platform JAR
4. **Custom loader** that detects OS/architecture and extracts the right binary

This is ~100+ lines of GitHub Actions YAML plus custom loader code.

## Solution: KTreeSitter Approach

The KTreeSitter project (Kotlin tree-sitter bindings) successfully solved this:

1. **Multi-platform JAR structure:**
```
lib/
  linux/
    aarch64/libktreesitter.so
    x64/libktreesitter.so
  macos/
    aarch64/libktreesitter.dylib
    x64/libktreesitter.dylib
  windows/
    x64/ktreesitter.dll
```

2. **Custom NativeUtils loader:**
- Detects OS via `System.getProperty("os.name")`
- Detects architecture via `System.getProperty("os.arch")`
- Maps to path: `lib/{os}/{arch}/lib{name}.{ext}`
- Extracts matching library to temp dir and loads it

3. **Published to Maven Central** successfully

This approach works but requires:
- ~150 lines of custom loader code
- GitHub Actions workflow to build all platforms
- Maintenance when upstream updates

## Our Decision

For tabletest-formatter, we decided tree-sitter was **too complex for our needs**. Instead, we implemented a custom state machine parser (~250 lines) that:
- Works on all platforms (pure Java)
- No build complexity
- No native dependencies

See [ADR-002](../adrs/002-use-state-machine-parser.md) for full rationale.

## When Tree-Sitter Is Worth It

Use tree-sitter when:
- ✅ You need robust parsing for complex language features
- ✅ You need full AST for multiple use cases
- ✅ Multi-platform build infrastructure already exists
- ✅ You're building an IDE, linter, or analysis tool

**For simple extraction tasks**, a custom parser may be simpler and more maintainable.

## Related

- [[20260104-180000000-dogfooding-problem-regex-parser]] - Why we needed better parsing
- [[../adrs/002-use-state-machine-parser]] - Why we chose state machine over tree-sitter

## References

- Upstream issue: https://github.com/seart-group/java-tree-sitter/issues/101
- KTreeSitter (proven solution): https://github.com/tree-sitter/kotlin-tree-sitter
- KTreeSitter Maven artifact: https://repo1.maven.org/maven2/io/github/tree-sitter/ktreesitter-jvm/
- Beads investigation: `tabletest-formatter-swo`
