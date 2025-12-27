#!/bin/bash
# Install custom git hooks from git-hooks/ directory
# Run this BEFORE installing beads (bd init)

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
HOOKS_SOURCE="$PROJECT_ROOT/git-hooks"
HOOKS_TARGET="$PROJECT_ROOT/.git/hooks"

# Colours
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m'

echo "Installing custom git hooks..."
echo ""

# Check if git-hooks directory exists
if [ ! -d "$HOOKS_SOURCE" ]; then
    echo -e "${RED}✗ Error: git-hooks/ directory not found${NC}"
    echo "Expected location: $HOOKS_SOURCE"
    exit 1
fi

# Check if .git directory exists
if [ ! -d "$HOOKS_TARGET" ]; then
    echo -e "${RED}✗ Error: .git/hooks/ directory not found${NC}"
    echo "Run 'git init' first to initialize the repository"
    exit 1
fi

# Install each hook file
for hook in pre-commit pre-push commit-msg; do
    if [ -f "$HOOKS_SOURCE/$hook" ]; then
        cp "$HOOKS_SOURCE/$hook" "$HOOKS_TARGET/$hook"
        chmod +x "$HOOKS_TARGET/$hook"
        echo -e "${GREEN}✓${NC} Installed $hook"
    else
        echo -e "${YELLOW}⚠${NC} Skipped $hook (not found in git-hooks/)"
    fi
done

echo ""
echo -e "${GREEN}Git hooks installed successfully!${NC}"
echo ""
echo "What the hooks do:"
echo "  • pre-commit: Formats Java code (Spotless), runs build and tests"
echo "  • pre-push: Protects against force-push to main/master, runs full test suite"
echo "  • commit-msg: Validates conventional commit format"
echo ""
echo "Next steps:"
echo "  1. Install beads (if using): Run 'bd init' and select 'merge' for git hooks"
echo "  2. Make your first commit"
echo ""
echo "To skip hooks when needed:"
echo "  git commit --no-verify"
echo "  git push --no-verify"
