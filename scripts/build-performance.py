#!/usr/bin/env python3
"""Measure HDCharts Gradle build times and storage. Manual and opt-in; CI never runs it.

  scripts/build-performance.py                                 # configuration only
  scripts/build-performance.py -s module-clean -s module-warm  # --module :charts-bar
  scripts/build-performance.py -s full-clean -s full-warm      # ciCompile, several minutes
  scripts/build-performance.py --show temp/build-performance/latest.json
  scripts/build-performance.py --compare OLD.json NEW.json

Each scenario times one ./gradlew call (wall clock, client start included), three
times by default. Clean scenarios run an untimed Gradle clean of the module or of
every project before each run, then build with --no-build-cache. The other
scenarios run their tasks once untimed first. Gradle clean deletes project build
outputs and nothing else. Storage is measured after the scenarios.

Reports go to temp/build-performance/: one timestamped JSON per run, plus
latest.json. Compare reports from the same machine, and read changes under about
15% as noise. Not measured: memory, incremental compilation, iOS/Kotlin Native,
tests and CI.
"""

import argparse
import json
import os
import platform
import re
import statistics
import subprocess
import sys
import time
from datetime import datetime
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
REPORT_DIR = ROOT / "temp" / "build-performance"
COMPILE_TASKS = ("compileKotlinJvm", "compileKotlinWasmJs", "compileAndroidMain")
SCREENSHOT_BASELINES = ":(glob)**/src/screenshotTest*/reference/**/*.png"
GIF_BASELINES = ":(glob)gif-baselines/*.gif"
# Expected to change between reports, so --compare reports them as changes rather than warnings.
PROJECT_CHANGES = ("commit", "uncommitted_changes", "gradle_projects")


def scenarios(module):
    """Scenario name -> (untimed preparation before each run, timed tasks, extra flags)."""
    module_tasks = [f"{module}:{task}" for task in COMPILE_TASKS]
    return {
        "configuration": (None, ["help"], []),
        "module-clean": ([f"{module}:clean"], module_tasks, ["--no-build-cache"]),
        "module-warm": (None, module_tasks, []),
        "full-clean": (["clean"], ["ciCompile"], ["--no-build-cache"]),
        "full-warm": (None, ["ciCompile"], []),
    }


class BuildFailed(Exception):
    pass


def run_quietly(command):
    """Return the stripped output of a command, or None if it is missing or fails."""
    try:
        result = subprocess.run(command, cwd=ROOT, capture_output=True, text=True, errors="replace")
    except OSError:
        return None
    return result.stdout.strip() if result.returncode == 0 else None


def read_text(path):
    try:
        return (ROOT / path).read_text()
    except OSError:
        return None


def search(pattern, text):
    match = re.search(pattern, text or "", re.MULTILINE)
    return match.group(1).strip() if match else None


# Measuring


def gradle(tasks, flags=()):
    command = ["./gradlew", *tasks, "--console=plain", *flags]
    command_text = " ".join(command)
    print(f"  $ {command_text}", file=sys.stderr, flush=True)
    start = time.monotonic()
    try:
        result = subprocess.run(
            command, cwd=ROOT, stdout=subprocess.PIPE, stderr=subprocess.STDOUT, text=True, errors="replace"
        )
    except OSError as error:
        raise BuildFailed(f"{command_text} could not start: {error}") from error
    seconds = round(time.monotonic() - start, 2)
    if result.returncode != 0:
        last_lines = "\n".join(result.stdout.splitlines()[-30:])
        raise BuildFailed(f"{command_text} exited with {result.returncode}\n{last_lines}")
    return seconds, result.stdout


def parse_run(seconds, output):
    # Gradle's summary, e.g. "35 actionable tasks: 8 executed, 6 from cache, 21 up-to-date".
    summary = search(r"^(\d+ actionable tasks?: .*)$", output)
    run = {"seconds": seconds, "gradle_summary": summary, "executed": None, "from_cache": None, "up_to_date": None}
    if summary:
        for key, label in (("executed", "executed"), ("from_cache", "from cache"), ("up_to_date", "up-to-date")):
            run[key] = int(search(rf"(\d+) {label}", summary) or 0)
    # With --console=plain, a task line without an outcome such as UP-TO-DATE is a task that ran.
    projects = set()
    for task_path in re.findall(r"^> Task (\S+)$", output, re.MULTILINE):
        projects.add(task_path.rpartition(":")[0] or ":")
    run["projects_with_executed_tasks"] = sorted(projects)
    return run


def measure(name, module, run_count):
    preparation, tasks, flags = scenarios(module)[name]
    print(name, file=sys.stderr)
    if not preparation:
        gradle(tasks, flags)  # untimed: start the daemon and bring outputs up to date
    runs = []
    for _ in range(run_count):
        if preparation:
            gradle(preparation)
        run = parse_run(*gradle(tasks, flags))
        runs.append(run)
        print(f"    {run['seconds']:.2f} s, {run['gradle_summary'] or 'no task summary'}", file=sys.stderr)
    return {
        "untimed_preparation": " ".join(["./gradlew", *preparation]) if preparation else None,
        "timed_command": " ".join(["./gradlew", *tasks, *flags]),
        "median_seconds": round(statistics.median(run["seconds"] for run in runs), 2),
        "runs": runs,
    }


def cpu_and_ram_gb():
    if sys.platform == "darwin":
        cpu = run_quietly(["sysctl", "-n", "machdep.cpu.brand_string"])
        memory_bytes = run_quietly(["sysctl", "-n", "hw.memsize"])
        ram_gb = round(int(memory_bytes) / 2**30) if memory_bytes and memory_bytes.isdigit() else None
        return cpu, ram_gb
    cpu = search(r"^model name\s*:\s*(.+)$", read_text("/proc/cpuinfo"))
    memory_kb = search(r"^MemTotal:\s+(\d+) kB", read_text("/proc/meminfo"))
    ram_gb = round(int(memory_kb) / 2**20) if memory_kb else None
    return cpu, ram_gb


def environment():
    gradle_version = run_quietly(["./gradlew", "--version"])
    catalog = read_text("gradle/libs.versions.toml")
    daemon_properties = read_text("gradle/gradle-daemon-jvm.properties")
    settings = read_text("settings.gradle.kts")
    git_status = run_quietly(["git", "status", "--porcelain"])
    daemon_version = search(r"^toolchainVersion=(.+)$", daemon_properties)
    daemon_vendor = search(r"^toolchainVendor=(.+)$", daemon_properties)
    cpu, ram_gb = cpu_and_ram_gb()
    return {
        "commit": run_quietly(["git", "rev-parse", "--short", "HEAD"]),
        "uncommitted_changes": None if git_status is None else git_status != "",
        "os_name": "macOS" if sys.platform == "darwin" else platform.system(),
        "os_version": platform.mac_ver()[0] or platform.release(),
        "architecture": platform.machine() or None,
        "cpu": cpu,
        "logical_cores": os.cpu_count(),
        "ram_gb": ram_gb,
        "gradle": search(r"^Gradle (\S+)", gradle_version),
        "launcher_jvm": search(r"^Launcher JVM:(.+)$", gradle_version),
        # The requirement Gradle uses to pick the daemon JVM, not the exact JVM it runs on.
        "daemon_jvm_criteria": f"Java {daemon_version}, {daemon_vendor}" if daemon_version else None,
        "java_toolchain": search(r'^java\s*=\s*"([^"]+)"', catalog),
        "kotlin": search(r'^kotlin-multiplatform\s*=\s*"([^"]+)"', catalog),
        "gradle_projects": None if settings is None else len(re.findall(r'^include\("', settings, re.MULTILINE)),
    }


def tracked_files(pathspec):
    listing = run_quietly(["git", "ls-files", "-z", "--", pathspec])
    if listing is None:
        return None
    paths = [ROOT / name for name in listing.split("\0") if name]
    sizes = [path.stat().st_size for path in paths if path.is_file()]
    return {"files": len(sizes), "mb": round(sum(sizes) / 2**20, 1)}


def build_output_usage():
    """Return MB of disk used per git-ignored build/ directory, or None when git or du fails."""
    ignored = run_quietly(["git", "ls-files", "--others", "--ignored", "--exclude-standard", "--directory"])
    if ignored is None:
        return None
    build_dirs = [path for path in ignored.splitlines() if path == "build/" or path.endswith("/build/")]
    if not build_dirs:
        return {}
    usage = run_quietly(["du", "-sk", *build_dirs])  # one du call counts hard-linked files once
    if usage is None:
        return None
    megabytes = {}
    for line in usage.splitlines():
        kilobytes, path = line.split("\t", 1)
        megabytes[path.rstrip("/")] = round(int(kilobytes) / 1024, 1)
    return megabytes


def storage():
    build_dirs = build_output_usage()
    return {
        "build_outputs_mb": None if build_dirs is None else round(sum(build_dirs.values()), 1),
        "build_outputs_mb_by_dir": build_dirs,
        "screenshot_baselines": tracked_files(SCREENSHOT_BASELINES),
        "gif_baselines": tracked_files(GIF_BASELINES),
    }


# Reporting


def show(value):
    return "unavailable" if value is None else str(value)


def same_or_each(values):
    """Return one value when every run agrees, otherwise each run's value."""
    shown = [show(value) for value in values]
    return shown[0] if len(set(shown)) == 1 else ", ".join(shown)


def print_table(rows):
    widths = [max(len(row[column]) for row in rows) for column in range(len(rows[0]))]
    for row in rows:
        cells = [cell.ljust(width) for cell, width in zip(row, widths)]
        print("  " + "  ".join(cells).rstrip())


def describe_storage(stored):
    lines = {}
    for key, label in (("screenshot_baselines", "Screenshot baselines"), ("gif_baselines", "GIF baselines")):
        counted = stored[key]
        lines[label] = "unavailable" if counted is None else f"{counted['files']} files, {counted['mb']} MB"
    build_dirs = stored["build_outputs_mb_by_dir"]
    if build_dirs is None:
        lines["Build outputs"] = "unavailable"
    else:
        lines["Build outputs"] = f"{stored['build_outputs_mb']} MB in {len(build_dirs)} build/ directories"
    return lines


def print_report(report):
    env = report["environment"]
    commit = show(env.get("commit"))
    if env.get("uncommitted_changes"):
        commit += " (uncommitted changes)"
    print(f"HDCharts build performance, {report['started']}\n")
    print_table([
        ["Commit", f"{commit}, {show(env.get('gradle_projects'))} Gradle projects"],
        ["Machine", f"{show(env.get('os_name'))} {show(env.get('os_version'))} {show(env.get('architecture'))}, "
                    f"{show(env.get('cpu'))}, {show(env.get('logical_cores'))} cores, {show(env.get('ram_gb'))} GB"],
        ["Build", f"Gradle {show(env.get('gradle'))}, Kotlin {show(env.get('kotlin'))}, "
                  f"Java toolchain {show(env.get('java_toolchain'))}"],
        ["JVMs", f"launcher {show(env.get('launcher_jvm'))}; "
                 f"daemon criteria {show(env.get('daemon_jvm_criteria'))} (not the exact runtime)"],
    ])

    rows = [["Scenario", "Median s", "Runs s", "Executed", "From cache", "Up-to-date"]]
    for name, result in report["scenarios"].items():
        runs = result["runs"]
        rows.append([
            name,
            f"{result['median_seconds']:.2f}",
            ", ".join(f"{run['seconds']:.2f}" for run in runs),
            same_or_each(run["executed"] for run in runs),
            same_or_each(run["from_cache"] for run in runs),
            same_or_each(run["up_to_date"] for run in runs),
        ])
    print()
    if len(rows) > 1:
        print_table(rows)
    else:
        print("  No scenario completed.")
    if report.get("error"):
        print(f"\n  Failed: {report['error'].splitlines()[0]}")

    print("\nStorage, measured after the scenarios")
    print_table([[label, text] for label, text in describe_storage(report["storage"]).items()])


def compare(old, new):
    old_env = old["environment"]
    new_env = new["environment"]
    print(f"Commit {show(old_env.get('commit'))} -> {show(new_env.get('commit'))}, "
          f"Gradle projects {show(old_env.get('gradle_projects'))} -> {show(new_env.get('gradle_projects'))}")
    for key in dict.fromkeys([*old_env, *new_env]):
        if key not in PROJECT_CHANGES and old_env.get(key) != new_env.get(key):
            print(f"Warning: {key} differs: {show(old_env.get(key))} -> {show(new_env.get(key))}")
    for label, report in (("old", old), ("new", new)):
        if report.get("error"):
            print(f"Warning: the {label} report recorded a failed build: {report['error'].splitlines()[0]}")

    rows = [["Scenario", "Old median s", "New median s", "Change", "Old executed", "New executed"]]
    skipped = []
    for name, before in old["scenarios"].items():
        after = new["scenarios"].get(name)
        if after is None:
            continue
        same_commands = before["timed_command"] == after["timed_command"]
        same_preparation = before["untimed_preparation"] == after["untimed_preparation"]
        if not (same_commands and same_preparation):
            skipped.append(name)
            continue
        change = ""
        if before["median_seconds"]:
            change = f"{(after['median_seconds'] - before['median_seconds']) / before['median_seconds']:+.0%}"
        rows.append([
            name,
            f"{before['median_seconds']:.2f}",
            f"{after['median_seconds']:.2f}",
            change,
            same_or_each(run["executed"] for run in before["runs"]),
            same_or_each(run["executed"] for run in after["runs"]),
        ])
    print()
    if len(rows) > 1:
        print_table(rows)
    else:
        print("  No scenario is in both reports.")
    if skipped:
        print(f"  Skipped because the commands differ: {', '.join(skipped)}")

    old_storage = describe_storage(old["storage"])
    new_storage = describe_storage(new["storage"])
    print("\nStorage")
    print_table([[label, old_storage[label], "->", new_storage[label]] for label in old_storage])


# Command line


def run_benchmarks(args):
    started = datetime.now().astimezone()
    report = {
        "started": started.isoformat(timespec="seconds"),
        "environment": environment(),
        "scenarios": {},
        "error": None,
        "storage": None,
    }
    try:
        for name in dict.fromkeys(args.scenario or ["configuration"]):
            report["scenarios"][name] = measure(name, args.module, args.runs)
    except BuildFailed as failure:
        report["error"] = str(failure)
        print(f"\n{failure}\n", file=sys.stderr)
    report["storage"] = storage()

    REPORT_DIR.mkdir(parents=True, exist_ok=True)
    text = json.dumps(report, indent=2) + "\n"
    path = REPORT_DIR / f"{started:%Y%m%d-%H%M%S}.json"
    with open(path, "x") as file:  # "x" refuses to overwrite an earlier report
        file.write(text)
    (REPORT_DIR / "latest.json").write_text(text)

    print_report(report)
    print(f"\nSaved {path.relative_to(ROOT)} and latest.json")
    return 1 if report["error"] else 0


def main():
    parser = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    parser.add_argument("-s", "--scenario", action="append", choices=list(scenarios(":module")),
                        help="repeatable, runs in the given order; default: configuration")
    parser.add_argument("--module", default=":charts-line", help="module for module-* scenarios (default %(default)s)")
    parser.add_argument("--runs", type=int, default=3, help="timed runs per scenario (default %(default)s)")
    parser.add_argument("--show", metavar="REPORT", help="print a saved report")
    parser.add_argument("--compare", nargs=2, metavar=("OLD", "NEW"), help="compare two saved reports")
    args = parser.parse_args()
    if args.runs < 1:
        parser.error("--runs must be at least 1")

    try:
        if args.show:
            print_report(json.loads(Path(args.show).read_text()))
            return 0
        if args.compare:
            old, new = (json.loads(Path(path).read_text()) for path in args.compare)
            compare(old, new)
            return 0
    except (OSError, ValueError, KeyError, TypeError) as error:
        sys.exit(f"cannot read report: {error!r}")
    return run_benchmarks(args)


if __name__ == "__main__":
    sys.exit(main())
