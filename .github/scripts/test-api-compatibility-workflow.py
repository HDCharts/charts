#!/usr/bin/env python3
"""A non-break Gradle failure must not be reported as an API break.

Executes the enforcement step's literal run block. Before the fix, every
nonzero Gradle exit printed apiCompatibilityAcknowledgeBreaks instructions.
"""

import os
import subprocess
import sys
import tempfile
from pathlib import Path

WORKFLOW = Path(__file__).resolve().parent.parent / "workflows" / "api-compatibility.yml"
STEP = "Enforce API compatibility policy"
ACK = "apiCompatibilityAcknowledgeBreaks"
BUILD_FAILURE = (
    "API compatibility check failed during the Gradle build or check. "
    "API compatibility was not established. See api-compatibility.log for the Gradle error."
)


def die(message):
    print(f"error: {message}", file=sys.stderr)
    raise SystemExit(1)


def step_section(text):
    marker = f"- name: {STEP}\n"
    start = text.find(marker)
    if start < 0:
        die(f"workflow step not found: {STEP}")
    lines = text[start:].splitlines()
    step_indent = len(lines[0]) - len(lines[0].lstrip(" "))
    section = [lines[0]]
    for line in lines[1:]:
        if line.strip() and len(line) - len(line.lstrip(" ")) <= step_indent:
            break
        section.append(line)
    return "\n".join(section) + "\n"


def extract_run(section):
    lines = section.splitlines()
    run_at = next((i for i, line in enumerate(lines) if line.strip() == "run: |"), None)
    if run_at is None:
        die(f"run: | block not found for step: {STEP}")
    run_indent = len(lines[run_at]) - len(lines[run_at].lstrip(" "))
    body = []
    body_indent = None
    for line in lines[run_at + 1 :]:
        if not line.strip():
            body.append("")
            continue
        indent = len(line) - len(line.lstrip(" "))
        if indent <= run_indent:
            break
        if body_indent is None:
            body_indent = indent
        if indent < body_indent:
            die(f"failed to extract run block for {STEP}")
        body.append(line[body_indent:])
    while body and body[-1] == "":
        body.pop()
    script = "\n".join(body) + "\n"
    if not script.startswith("set -euo pipefail\n"):
        die(f"failed to extract run block for {STEP}")
    return script


def main():
    if not WORKFLOW.is_file():
        die(f"workflow not found: {WORKFLOW}")
    section = step_section(WORKFLOW.read_text(encoding="utf-8"))
    head, _, _ = section.partition("run: |")
    for key in ("EXIT_CODE", "ANY_CHANGE_DETECTED", "INSTRUCTION"):
        if f"{key}:" not in head:
            die(f"enforcement step does not pass {key}")
    script = extract_run(section)

    with tempfile.TemporaryDirectory(prefix="api-compat-workflow-") as directory:
        script_path = Path(directory) / "enforce.sh"
        script_path.write_text(script, encoding="utf-8")
        env = {
            "PATH": os.environ.get("PATH", "/usr/bin:/bin"),
            "HOME": os.environ.get("HOME", "/tmp"),
            "EXIT_CODE": "1",
            "ANY_CHANGE_DETECTED": "false",
            "INSTRUCTION": "",
        }
        proc = subprocess.run(
            ["bash", "--noprofile", "--norc", str(script_path)],
            env=env,
            text=True,
            capture_output=True,
            encoding="utf-8",
        )

    stdout = proc.stdout.strip()
    name = "nonzero Gradle exit without a detected API break"
    if proc.returncode != 1 or proc.stderr != "" or ACK in stdout or stdout != BUILD_FAILURE:
        print(
            f"FAIL: {name} (expected exit 1 and {BUILD_FAILURE!r}, "
            f"got exit {proc.returncode}, stdout {stdout!r}, stderr {proc.stderr!r})",
            file=sys.stderr,
        )
        return 1
    print(f"PASS: {name}")
    return 0


if __name__ == "__main__":
    sys.exit(main())
