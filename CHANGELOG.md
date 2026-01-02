# TableTest Formatter Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

## [0.1.0] - 2026-01-02

### Added

- **TableTest table formatting** for Java and Kotlin files (with `@TableTest` annotations), and standalone `.table` files
- **Column alignment** with proper spacing, Unicode/emoji width handling, and collection literal formatting
- **Command-line tool** for formatting files and directories with check mode for CI integration
- **Spotless integration** (Gradle) with configurable indentation and formatting options
- **Graceful error handling** that never breaks builds when encountering malformed tables
