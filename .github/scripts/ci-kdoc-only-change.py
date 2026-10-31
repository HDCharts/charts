#!/usr/bin/env python3
"""Return success when every changed line in a Kotlin file is KDoc."""

from __future__ import annotations

import difflib
import re
import subprocess
import sys


def read_lines(revision: str, path: str) -> list[str] | None:
    # Missing files are expected when a Kotlin file was added or deleted.
    result = subprocess.run(
        ["git", "show", f"{revision}:{path}"],
        stdout=subprocess.PIPE,
        stderr=subprocess.DEVNULL,
        check=False,
    )
    if result.returncode:
        return None
    try:
        return result.stdout.decode("utf-8").splitlines()
    except UnicodeDecodeError:
        return None


def kdoc_lines(lines: list[str]) -> list[bool]:
    # Mark lines inside KDoc blocks; unrecognized changes stay on the CI path.
    flags = []
    inside = False
    for line in lines:
        if not inside:
            inside = bool(re.match(r"\s*/\*\*", line))
        is_kdoc = inside
        if inside and "*/" in line:
            inside = False
            if line.split("*/", 1)[1].strip():
                is_kdoc = False
        flags.append(is_kdoc)
    return flags


def is_kdoc_only(base: str, head: str, path: str) -> bool:
    # Skip checks only when every changed line is marked as KDoc on both sides.
    before = read_lines(base, path) or []
    after = read_lines(head, path) or []
    if before == after:
        return False
    # Avoid treating comment-looking lines inside Kotlin raw strings as KDoc.
    if any('"""' in line for line in before + after):
        return False

    before_flags = kdoc_lines(before)
    after_flags = kdoc_lines(after)
    changed = False
    for tag, start, end, new_start, new_end in difflib.SequenceMatcher(
        a=before, b=after, autojunk=False
    ).get_opcodes():
        if tag == "equal":
            continue
        changed = True
        # Delimiter edits can turn unchanged lines into executable code.
        if any(
            "/*" in line or "*/" in line
            for line in before[start:end] + after[new_start:new_end]
        ):
            return False
        if not all(before_flags[start:end]) or not all(after_flags[new_start:new_end]):
            return False
    return changed


if __name__ == "__main__":
    if len(sys.argv) != 4:
        raise SystemExit(2)
    raise SystemExit(0 if is_kdoc_only(*sys.argv[1:]) else 1)
