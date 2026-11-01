#!/usr/bin/env python3
"""Synchronize marked Kotlin examples into wiki Markdown code blocks."""

import argparse
from dataclasses import dataclass
from pathlib import Path
import re
import sys


SOURCE_MARKER = re.compile(r"^\s*// (BEGIN|END) WIKI EXAMPLE: ([a-z0-9-]+)\s*$")
DEST_START = re.compile(
    r"^<!-- BEGIN WIKI EXAMPLE: ([^#<>]+)#([a-z0-9-]+) -->$"
)
DEST_END = re.compile(r"^<!-- END WIKI EXAMPLE: ([a-z0-9-]+) -->$")
SOURCE_MARKER_PREFIXES = ("// BEGIN WIKI EXAMPLE", "// END WIKI EXAMPLE")
DEST_MARKER_PREFIXES = ("<!-- BEGIN WIKI EXAMPLE", "<!-- END WIKI EXAMPLE")


def looks_like_marker(line: str, kind: str | None = None) -> bool:
    prefixes = SOURCE_MARKER_PREFIXES if kind == "source" else DEST_MARKER_PREFIXES
    if kind is None:
        prefixes = SOURCE_MARKER_PREFIXES + DEST_MARKER_PREFIXES
    return line.lstrip().startswith(prefixes)


class SyncError(Exception):
    """An invalid or inconsistent source/destination marker."""


@dataclass(frozen=True)
class Region:
    start: int
    end: int
    identifier: str


def find_region(lines: list[str], path: Path, kind: str, identifier: str) -> Region:
    if kind == "source":
        start_pattern = re.compile(r"^\s*// BEGIN WIKI EXAMPLE: ([a-z0-9-]+)\s*$")
        end_pattern = re.compile(r"^\s*// END WIKI EXAMPLE: ([a-z0-9-]+)\s*$")
    else:
        start_pattern = DEST_START
        end_pattern = DEST_END

    starts: dict[str, list[int]] = {}
    ends: dict[str, list[int]] = {}
    for number, line in enumerate(lines):
        if not looks_like_marker(line, kind):
            continue
        start_match = start_pattern.fullmatch(line)
        end_match = end_pattern.fullmatch(line)
        if start_match:
            marker_id = start_match.group(1) if kind == "source" else start_match.group(2)
            starts.setdefault(marker_id, []).append(number)
        elif end_match:
            ends.setdefault(end_match.group(1), []).append(number)
        else:
            raise SyncError(f"{path}:{number + 1}: malformed {kind} marker: {line.strip()}")

    for marker_id in starts.keys() | ends.keys():
        if len(starts.get(marker_id, [])) != 1 or len(ends.get(marker_id, [])) != 1:
            raise SyncError(
                f"{path}: expected exactly one {kind} start and end marker for "
                f"{marker_id!r}; found {len(starts.get(marker_id, []))} start and "
                f"{len(ends.get(marker_id, []))} end markers"
            )
        if starts[marker_id][0] >= ends[marker_id][0]:
            raise SyncError(f"{path}: {kind} end marker {marker_id!r} must follow its start marker")
    if identifier not in starts:
        raise SyncError(
            f"{path}: {kind} marker {identifier!r} is missing"
        )
    return Region(starts[identifier][0], ends[identifier][0], identifier)


def read_snippet(repo_root: Path, source_ref: str, destination: Path) -> str:
    if "#" not in source_ref:
        raise SyncError(f"{destination}: unresolved source reference {source_ref!r}; expected path#id")
    relative_path, identifier = source_ref.rsplit("#", 1)
    source_path = Path(relative_path)
    if source_path.is_absolute() or ".." in source_path.parts:
        raise SyncError(f"{destination}: ambiguous source reference {source_ref!r}")
    source = (repo_root / source_path).resolve()
    try:
        source.relative_to(repo_root.resolve())
    except ValueError as error:
        raise SyncError(f"{destination}: source reference escapes repository: {source_ref!r}") from error
    if not source.is_file():
        raise SyncError(f"{destination}: referenced source does not exist: {source_path}")

    source_lines = source.read_text(encoding="utf-8").splitlines()
    try:
        region = find_region(source_lines, source_path, "source", identifier)
    except SyncError as error:
        raise SyncError(f"{destination}: referenced source {source_path}: {error}") from error
    snippet = "\n".join(source_lines[region.start + 1 : region.end]).strip("\n")
    if not snippet.strip():
        raise SyncError(f"{source_path}: source region {identifier!r} is empty")
    return snippet


def sync_markdown(repo_root: Path, markdown: Path, check: bool) -> bool:
    original = markdown.read_bytes().decode("utf-8")
    newline = "\r\n" if "\r\n" in original else "\n"
    lines = original.splitlines()
    updated = lines.copy()
    index = 0
    changed = False
    seen_ids: set[str] = set()
    while index < len(lines):
        line = lines[index]
        if not looks_like_marker(line, "destination"):
            index += 1
            continue
        start = DEST_START.fullmatch(line)
        if not start:
            raise SyncError(f"{markdown}:{index + 1}: malformed destination marker: {line.strip()}")
        source_ref, identifier = start.groups()
        if identifier in seen_ids:
            raise SyncError(f"{markdown}:{index + 1}: duplicate destination marker {identifier!r}")
        seen_ids.add(identifier)
        end_index = index + 1
        end_region = None
        while end_index < len(lines):
            if looks_like_marker(lines[end_index], "destination"):
                end_region = DEST_END.fullmatch(lines[end_index])
                break
            end_index += 1
        if end_region is None:
            raise SyncError(f"{markdown}:{index + 1}: missing destination end marker for {identifier!r}")
        if end_region.group(1) != identifier:
            raise SyncError(
                f"{markdown}:{end_index + 1}: destination marker identifier "
                f"{end_region.group(1)!r} does not match {identifier!r}"
            )
        snippet = read_snippet(repo_root, f"{source_ref}#{identifier}", markdown)
        block = ["```kotlin", *snippet.splitlines(), "```"]
        if updated[index + 1 : end_index] != block:
            updated[index + 1 : end_index] = block
            changed = True
        index = end_index + 1

    if changed:
        if check:
            raise SyncError(f"{markdown}: managed Kotlin example is out of sync; run scripts/sync-wiki-examples.py")
        result = newline.join(updated) + (newline if original.endswith(("\n", "\r")) else "")
        markdown.write_bytes(result.encode("utf-8"))
    return changed


def sync_all(repo_root: Path, check: bool) -> int:
    markdown_files = sorted((repo_root / "docs/wiki").rglob("*.md"))
    changed = 0
    for markdown in markdown_files:
        changed += sync_markdown(repo_root, markdown, check)
    return changed


def main(argv: list[str] | None = None, repo_root: Path | None = None) -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--check", action="store_true", help="verify without modifying files")
    args = parser.parse_args(argv)
    root = repo_root or Path(__file__).resolve().parent.parent
    try:
        changed = sync_all(root, args.check)
    except (OSError, SyncError) as error:
        print(f"Wiki example sync error: {error}", file=sys.stderr)
        return 1
    if args.check:
        print("Wiki examples are in sync.")
    else:
        print(f"Synchronized wiki examples ({changed} file(s) changed).")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
