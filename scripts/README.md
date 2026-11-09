# Development Scripts

## Git Hooks

### Installation

```bash
bash scripts/install-git-hooks.sh
```

### What the hooks do

**Pre-commit hook** (runs on `git commit`):
- Syncs beads changes
- Build and run unit tests (`mvn clean install`)
- Takes ~10 seconds

**Pre-push hook** (runs on `git push`):
- Syncs beads changes
- Runs full compatibility test suite
- Takes ~30 seconds

### Skipping hooks

If you need to skip the hooks temporarily:

```bash
# Skip pre-commit
git commit --no-verify

# Skip pre-push
git push --no-verify
```

**Note:** Only skip hooks if you're certain your changes don't break anything. CI will still catch issues.

### Manual testing

To run compatibility tests manually:

```bash
cd compatibility-tests
bash run-tests.sh
```

To test a specific compatibility project:

```bash
cd compatibility-tests/spring-boot-latest
bash test.sh
```

## Unicode width data

`generate-wide-code-points.py` prints the table in `WideCodePoints.java`: the code points that
draw two columns on their own (East Asian Width W or F, or Emoji_Presentation). Run it with a
Python whose Unicode version matches the `emoji-data.txt` you give it:

```bash
curl -sO https://www.unicode.org/Public/17.0.0/ucd/emoji/emoji-data.txt
python3 scripts/generate-wide-code-points.py emoji-data.txt
```
