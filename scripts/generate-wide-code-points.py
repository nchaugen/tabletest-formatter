#!/usr/bin/env python3
"""Prints the Java ranges of the code points that draw two columns on their own: East Asian
Width W (wide) or F (fullwidth), or the property Emoji_Presentation.

East Asian Width comes from the Unicode data of the Python that runs this script. Emoji
presentation comes from emoji-data.txt of the same Unicode version. Paste the output into
WideCodePoints.java.

    curl -sO https://www.unicode.org/Public/<version>/ucd/emoji/emoji-data.txt
    python3 scripts/generate-wide-code-points.py emoji-data.txt
"""
import re
import sys
import unicodedata

wide = {cp for cp in range(sys.maxunicode + 1) if unicodedata.east_asian_width(chr(cp)) in ("W", "F")}

emoji_version = None
for line in open(sys.argv[1], encoding="utf-8"):
    version = re.match(r"# Version: (\S+)", line)
    if version:
        emoji_version = version.group(1)
    fields = line.split("#")[0].split(";")
    if len(fields) == 2 and fields[1].strip() == "Emoji_Presentation":
        first, _, last = fields[0].strip().partition("..")
        wide.update(range(int(first, 16), int(last or first, 16) + 1))

if not unicodedata.unidata_version.startswith(emoji_version or "?"):
    sys.exit(f"Unicode {unicodedata.unidata_version} in Python, but emoji data {emoji_version}")

ranges = []
for cp in sorted(wide):
    if ranges and ranges[-1][1] == cp - 1:
        ranges[-1][1] = cp
    else:
        ranges.append([cp, cp])

print(f"        // Unicode {unicodedata.unidata_version}, {len(ranges)} ranges")
for start in range(0, len(ranges), 4):
    print("        " + " ".join(f"{{0x{a:04X}, 0x{b:04X}}}," for a, b in ranges[start:start + 4]))
