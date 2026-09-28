#!/usr/bin/env python3
"""Print the release note for one trusted-node image version from the changelog.

Usage: trusted-node-notes.py <CHANGELOG.md> <version> [locale]

Changelog layout: a `## <version>` heading per release; the text under it is the English note,
optional `### <locale>` subsections (es_ES, de_DE, ...) hold translations. Paragraphs are joined
into one line because the store manifests take a single string. Prints nothing (exit 0) when the
version or locale is absent, so callers can fall back to a generated note.
"""
import re
import sys


def extract(text: str, version: str, locale: str | None) -> str:
    lines = text.splitlines()
    section: list[str] = []
    inside = False
    for line in lines:
        if line.startswith("## "):
            if inside:
                break
            inside = line[3:].strip().split()[:1] == [version]
            continue
        if inside:
            section.append(line)
    if not section:
        return ""
    wanted: list[str] = []
    current = None  # None = English text before the first ### heading
    for line in section:
        m = re.match(r"^### +(\S+)", line)
        if m:
            current = m.group(1)
            continue
        if current == locale:
            wanted.append(line)
    return " ".join(" ".join(wanted).split())


def main() -> int:
    if len(sys.argv) not in (3, 4):
        print(__doc__, file=sys.stderr)
        return 2
    path, version = sys.argv[1], sys.argv[2]
    locale = sys.argv[3] if len(sys.argv) == 4 else None
    with open(path, encoding="utf-8-sig") as f:  # -sig: drop a BOM so the note starts with its text
        note = extract(f.read(), version, locale)
    if note:
        print(note)
    return 0


if __name__ == "__main__":
    sys.exit(main())
