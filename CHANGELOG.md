# TableTest Formatter Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

## [1.0.0] - 2026-02-17
### Added
- Support for reading indent style and size from .editorconfig
### Removed
- BREAKING: Removed config parameters for indent style and size, instead use .editorconfig to override defaults
### Changed
- Moved to new org.tabletest coordinates, please update your dependencies accordingly
### Fixed
- Corrected emoji display width calculation

## [0.1.0] - 2026-01-03
### Added
- **TableTest table formatting** for Java and Kotlin files (with `@TableTest` annotations), and standalone `.table` files
- **Column alignment** with proper spacing, Unicode/emoji width handling, and collection literal formatting
- **Command-line tool** for formatting files and directories with check mode for CI integration
- **Spotless integration** (Gradle) with configurable indentation and formatting options
- **Graceful error handling** that never breaks builds when encountering malformed tables
- **Indentation alignment**: Aligns tables relative to `@TableTest` annotation, preserving source indentation style (tabs/spaces) with configurable extra indentation
