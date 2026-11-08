#!/usr/bin/env python3
"""Choose the pull request checks a change needs.

Prepare PR runs this before any Gradle job starts. Each changed file maps to the
Gradle project that owns it. An implementation change also selects every project
that depends on it, and screenshot test classes are matched to the projects they
import. A file the script cannot place selects full validation, and so does any
error. `./gradlew ciCheckImpactGraph` fails when Gradle has a project or project
dependency that this script does not see.

Usage:
  ci-impact.py <base-sha> <head-sha>   classify a pull request diff
  ci-impact.py --files <path>...       classify the listed paths
  ci-impact.py --print-graph           print the graph that ciCheckImpactGraph compares
"""

from __future__ import annotations

import argparse
import importlib.util
import json
import os
import re
import subprocess
from dataclasses import dataclass, field
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
CHANGE_FILTER = ROOT / "scripts" / "ci-has-code-changes.sh"
KDOC_CHECK = Path(__file__).resolve().parent / "ci-kdoc-only-change.py"

CHECKS = ("lint", "build", "api", "jvm", "android", "wasm", "ios", "screenshot")

# The umbrella artifact depends on every chart module, so the library is the
# umbrella plus everything it depends on.
UMBRELLA = ":charts"
SCREENSHOT_PROJECT = ":androidApp"
SCREENSHOT_SOURCES = "src/screenshotTest/kotlin/"
SCREENSHOT_REFERENCES = "src/screenshotTestDebug/reference/"
# Every screenshot uses the theme and sample data from these projects. They also
# depend on the umbrella, so following their dependencies would tie every
# screenshot to every chart. A direct change to either selects every screenshot.
SCREENSHOT_SHARED_PROJECTS = frozenset({":charts", ":sample-shared"})
# Test source sets that the JVM job never runs.
NON_JVM_TEST_SOURCE_SETS = frozenset(
    {"androidDeviceTest", "androidInstrumentedTest", "androidTest", "screenshotTest", "screenshotTestDebug"}
)
MAX_LISTED_FILES = 50

PROJECT_PATH = re.compile(r"(?::[A-Za-z0-9_.-]+)+")
CLASS_NAME = re.compile(r"[A-Za-z_$][\w$]*(?:\.[A-Za-z_$][\w$]*)*")
LINE_COMMENT = re.compile(r"^\s*//.*$", re.M)
PACKAGE = re.compile(r"^package\s+([\w.]+)", re.M)
DECLARATION = re.compile(
    r"(?P<annotations>(?:@[\w.]+(?:\([^)]*\))?\s+)*)(?P<modifiers>(?:[a-z]+\s+)*)"
    r"(?P<kind>fun|val|var|class|object|interface|typealias)\b"
)


def is_test_source_set(name: str) -> bool:
    return name == "test" or "Test" in name


def camel_case(name: str) -> str:
    head, *tail = re.split(r"[-_]", name)
    return head + "".join(part[:1].upper() + part[1:] for part in tail)


def read_text(path: Path) -> str:
    return path.read_text(encoding="utf-8") if path.is_file() else ""


def closure(start: set[str], edges: dict[str, set[str]]) -> set[str]:
    """Returns every node reachable from start, excluding start itself unless it is reached again."""
    seen: set[str] = set()
    pending = list(start)
    while pending:
        for node in edges.get(pending.pop(), ()):
            if node not in seen:
                seen.add(node)
                pending.append(node)
    return seen


def parse_projects(settings: str) -> dict[str, str]:
    """Maps each project path included by settings.gradle.kts to its directory."""
    text = LINE_COMMENT.sub("", settings)
    projects: dict[str, str] = {}
    for arguments in re.findall(r"\binclude\s*\(([^)]*)\)", text):
        for name in re.findall(r'"([^"]+)"', arguments):
            path = name if name.startswith(":") else f":{name}"
            projects[path] = path[1:].replace(":", "/")
    for path, directory in re.findall(
        r'project\(\s*"(:[^"]+)"\s*\)\s*\.projectDir\s*=\s*file\(\s*"([^"]+)"\s*\)', text
    ):
        if path in projects:
            projects[path] = directory.strip("/")
    return projects


def parse_dependencies(build_script: str, projects: dict[str, str]) -> set[str]:
    """Returns the projects a build script references through project(...) or projects.* accessors."""
    text = LINE_COMMENT.sub("", build_script)
    accessors = {".".join(camel_case(part) for part in path[1:].split(":")): path for path in projects}
    found = {path for path in re.findall(r'\bproject\(\s*(?:path\s*=\s*)?"(:[^"]*)"', text) if path in projects}
    for chain in re.findall(r"\bprojects\.([A-Za-z0-9_.]+)", text):
        parts = chain.split(".")
        for end in range(len(parts), 0, -1):
            path = accessors.get(".".join(parts[:end]))
            if path:
                found.add(path)
                break
    return found


def common_package(packages: set[str]) -> str:
    split = [package.split(".") for package in packages]
    prefix = []
    for segments in zip(*split):
        if len(set(segments)) != 1:
            break
        prefix.append(segments[0])
    return ".".join(prefix)


def declares_only_previews(text: str) -> bool:
    """True when the only top-level declarations other files can use are @PreviewTest functions."""
    lines = text.splitlines()
    for number, line in enumerate(lines):
        match = DECLARATION.match(line)
        if not match or "private" in match.group("modifiers").split():
            continue
        annotations = [match.group("annotations")]
        for previous in reversed(lines[:number]):
            if not previous.strip() or previous.startswith("}"):
                break
            annotations.append(previous)
        if match.group("kind") == "fun" and "@PreviewTest" in "\n".join(annotations):
            continue
        return False
    return True


@dataclass
class ScreenshotSource:
    path: str
    is_test: bool
    # None for support files and for tests whose class name the script cannot derive.
    test_class: str | None
    # True when the file declares code other screenshot files can use.
    shares_code: bool
    # Projects whose code the file references.
    imports: set[str]


class Repo:
    """The Gradle projects, project dependencies and screenshot tests of a checkout."""

    def __init__(self, root: Path):
        self.root = root
        self.projects = parse_projects(read_text(root / "settings.gradle.kts"))
        self.dependencies = {
            path: parse_dependencies(read_text(root / directory / "build.gradle.kts"), self.projects) - {path}
            for path, directory in self.projects.items()
        }
        self.dependents: dict[str, set[str]] = {}
        for path, dependencies in self.dependencies.items():
            for dependency in dependencies:
                self.dependents.setdefault(dependency, set()).add(path)
        self.library = (
            {UMBRELLA} | closure({UMBRELLA}, self.dependencies) if UMBRELLA in self.projects else set()
        )
        self._jvm_tested: set[str] | None = None
        self._screenshots: dict[str, ScreenshotSource] | None = None

    def owner(self, path: str) -> tuple[str, str] | None:
        """Returns the project that owns a path and the path inside the project directory."""
        matches = [
            (len(directory), project)
            for project, directory in self.projects.items()
            if path.startswith(directory + "/")
        ]
        if not matches:
            return None
        project = max(matches)[1]
        return project, path[len(self.projects[project]) + 1 :]

    def source_sets(self, project: str) -> list[Path]:
        source = self.root / self.projects[project] / "src"
        return sorted(child for child in source.iterdir() if child.is_dir()) if source.is_dir() else []

    def jvm_tested(self) -> set[str]:
        """Projects with a test source set the JVM job can run."""
        if self._jvm_tested is None:
            self._jvm_tested = {
                project
                for project in self.projects
                if any(
                    is_test_source_set(source_set.name) and source_set.name not in NON_JVM_TEST_SOURCE_SETS
                    for source_set in self.source_sets(project)
                )
            }
        return self._jvm_tested

    def packages(self) -> dict[str, set[str]]:
        """Maps each package declared in a production source set to the projects that declare it."""
        index: dict[str, set[str]] = {}
        for project in self.projects:
            for source_set in self.source_sets(project):
                if is_test_source_set(source_set.name):
                    continue
                for file in source_set.rglob("*.kt"):
                    match = PACKAGE.search(read_text(file))
                    if match:
                        index.setdefault(match.group(1), set()).add(project)
        return index

    def screenshots(self) -> dict[str, ScreenshotSource]:
        """Returns the screenshot test sources keyed by repository path."""
        if self._screenshots is None:
            self._screenshots = self._load_screenshots()
        return self._screenshots

    def _load_screenshots(self) -> dict[str, ScreenshotSource]:
        directory = self.projects.get(SCREENSHOT_PROJECT)
        base = self.root / directory / SCREENSHOT_SOURCES if directory is not None else None
        if base is None or not base.is_dir():
            return {}
        texts = {file: read_text(file) for file in sorted(base.rglob("*.kt"))}
        own_packages = {match.group(1) for text in texts.values() if (match := PACKAGE.search(text))}
        index = self.packages()
        namespace = common_package(set(index))
        sources = {}
        for file, text in texts.items():
            match = PACKAGE.search(text)
            package = f"{match.group(1)}." if match else ""
            is_test = "@PreviewTest" in text
            named = "@file:JvmName" not in text
            test_class = f"{package}{file.stem[:1].upper()}{file.stem[1:]}Kt" if is_test and named else None
            path = file.relative_to(self.root).as_posix()
            sources[path] = ScreenshotSource(
                path=path,
                is_test=is_test,
                test_class=test_class,
                shares_code=not (is_test and named and declares_only_previews(text)),
                imports=self._referenced_projects(text, index, namespace, own_packages),
            )
        return sources

    def _referenced_projects(
        self, text: str, index: dict[str, set[str]], namespace: str, own_packages: set[str]
    ) -> set[str]:
        if not namespace:
            return set(self.projects)
        found: set[str] = set()
        for name in re.findall(rf"\b{re.escape(namespace)}(?:\.\w+)+", text):
            if any(name == package or name.startswith(f"{package}.") for package in own_packages):
                continue
            parts = name.split(".")
            for end in range(len(parts), 0, -1):
                owners = index.get(".".join(parts[:end]))
                if owners:
                    found |= owners
                    break
            else:
                # An unknown package could come from any project.
                return set(self.projects)
        return found

    def reference_class(self, path: str) -> str | None:
        """Returns the test class a reference image belongs to, from its directory."""
        prefix = f"{self.projects[SCREENSHOT_PROJECT]}/{SCREENSHOT_REFERENCES}"
        parts = path[len(prefix) :].split("/")
        return ".".join(parts[:-1]) if len(parts) > 1 else None


@dataclass(frozen=True)
class Change:
    path: str
    # full, lint, api, main, test, screenshot-source or screenshot-reference
    kind: str
    project: str | None = None
    detail: str = ""


def classify(repo: Repo, path: str) -> Change:
    if path == ".editorconfig":
        return Change(path, "lint", detail="ktlint settings")
    if path == "API-COMPATIBILITY-BREAKS.txt":
        return Change(path, "api", detail="acknowledged API breaks")
    owner = repo.owner(path)
    if owner is None:
        if path.startswith(".github/"):
            reason = "CI configuration"
        elif path.startswith("scripts/"):
            reason = "CI script"
        elif "/" not in path or path.startswith(("buildSrc/", "gradle/")):
            reason = "build logic"
        else:
            reason = "a path outside every Gradle project"
        return Change(path, "full", detail=reason)
    project, relative = owner
    if project == SCREENSHOT_PROJECT and relative.startswith(SCREENSHOT_SOURCES):
        return Change(path, "screenshot-source", project, "screenshot test source")
    if project == SCREENSHOT_PROJECT and relative.startswith(SCREENSHOT_REFERENCES):
        return Change(path, "screenshot-reference", project, "screenshot reference image")
    parts = relative.split("/")
    if len(parts) > 2 and parts[0] == "src":
        if is_test_source_set(parts[1]):
            return Change(path, "test", project, f"`{project}` {parts[1]} tests")
        return Change(path, "main", project, f"`{project}` {parts[1]}")
    return Change(path, "main", project, f"`{project}` build or project file")


@dataclass
class ScreenshotPlan:
    run: bool = False
    # Empty while run is true: every class runs.
    classes: list[str] = field(default_factory=list)
    reason: str = ""
    total: int = 0


def select_screenshots(repo: Repo, changes: list[Change], changed: set[str], affected: set[str]) -> ScreenshotPlan:
    sources = repo.screenshots()
    tests = [source for source in sources.values() if source.is_test]
    total = len(tests)

    def every(reason: str) -> ScreenshotPlan:
        return ScreenshotPlan(True, [], reason, total)

    shared = sorted(changed & SCREENSHOT_SHARED_PROJECTS)
    if shared:
        return every(f"`{shared[0]}` changed, and every screenshot uses it")
    known_classes = {source.test_class for source in tests}
    selected: set[str] = set()
    for change in changes:
        if change.kind == "main" and change.project == SCREENSHOT_PROJECT and not change.path.endswith(".kt"):
            return every(f"`{change.path}` is part of the app every screenshot renders in")
        if change.kind == "screenshot-source":
            source = sources.get(change.path)
            if source is None:
                return every(f"`{change.path}` was removed or renamed")
            if source.shares_code or source.test_class is None:
                return every(f"`{change.path}` holds code that other screenshot tests can use")
            selected.add(source.test_class)
        elif change.kind == "screenshot-reference":
            test_class = repo.reference_class(change.path)
            if test_class not in known_classes:
                return every(f"`{change.path}` belongs to no screenshot test class")
            selected.add(test_class)

    shared_imports = set().union(*(source.imports for source in sources.values() if source.shares_code))
    impact = affected - SCREENSHOT_SHARED_PROJECTS
    for source in tests:
        if (source.imports | shared_imports) & impact:
            if source.test_class is None:
                return every(f"`{source.path}` sets its own class name with @file:JvmName")
            selected.add(source.test_class)
    if not selected:
        return ScreenshotPlan(False, [], "", total)
    if selected == known_classes:
        return every("every screenshot class renders an affected module")
    return ScreenshotPlan(True, sorted(selected), "", total)


@dataclass
class Plan:
    # none, selective or full
    mode: str
    reason: str = ""
    fallback: bool = False
    files: list[tuple[str, str]] = field(default_factory=list)
    changed: set[str] = field(default_factory=set)
    affected: set[str] = field(default_factory=set)
    test_modules: set[str] = field(default_factory=set)
    checks: dict[str, bool] = field(default_factory=dict)
    screenshots: ScreenshotPlan = field(default_factory=ScreenshotPlan)
    library: set[str] = field(default_factory=set)
    jvm_tested: set[str] = field(default_factory=set)

    @staticmethod
    def full(reason: str, files: list[tuple[str, str]] = (), fallback: bool = False) -> Plan:
        return Plan("full", reason, fallback, list(files), checks={check: True for check in CHECKS})

    @staticmethod
    def none(files: list[tuple[str, str]] = ()) -> Plan:
        return Plan("none", files=list(files), checks={check: False for check in CHECKS})


def plan_changes(repo: Repo, paths: list[str], notes: list[tuple[str, str]] = ()) -> Plan:
    changes = [classify(repo, path) for path in paths]
    files = [(change.path, change.detail) for change in changes] + list(notes)
    if not changes:
        return Plan.none(files)
    unowned = [change for change in changes if change.kind == "full"]
    if unowned:
        return Plan.full(f"{unowned[0].detail} changed (`{unowned[0].path}`)", files)
    if not repo.library:
        return Plan.full(f"the umbrella project `{UMBRELLA}` was not found", files, fallback=True)

    changed = {change.project for change in changes if change.kind == "main"}
    tested = {change.project for change in changes if change.kind == "test"}
    affected = changed | closure(changed, repo.dependents)
    test_modules = affected | tested
    screenshots = select_screenshots(repo, changes, changed, affected)
    if not all(PROJECT_PATH.fullmatch(module) for module in test_modules) or not all(
        CLASS_NAME.fullmatch(name) for name in screenshots.classes
    ):
        return Plan.full("a module or class name has unexpected characters", files, fallback=True)

    library_tests = bool(test_modules & repo.library)
    checks = {
        "lint": any(change.kind not in ("api", "screenshot-reference") for change in changes),
        "build": bool(changed),
        "api": bool(changed & repo.library) or any(change.kind == "api" for change in changes),
        "jvm": bool(test_modules & repo.jvm_tested()),
        "android": library_tests,
        "wasm": library_tests,
        "ios": library_tests,
        "screenshot": screenshots.run,
    }
    return Plan(
        mode="selective",
        files=files,
        changed=changed,
        affected=affected,
        test_modules=test_modules,
        checks=checks,
        screenshots=screenshots,
        library=repo.library,
        jvm_tested=repo.jvm_tested(),
    )


def filter_ignored(paths: list[str]) -> list[str]:
    """Drops documentation and metadata paths with the rules in scripts/ci-has-code-changes.sh."""
    if any("\n" in path for path in paths):
        raise ValueError("a changed path contains a line break")
    result = subprocess.run(
        ["bash", str(CHANGE_FILTER), "--filter", "core"],
        input="".join(f"{path}\n" for path in paths),
        capture_output=True,
        text=True,
        check=True,
    )
    return [line for line in result.stdout.splitlines() if line]


def load_kdoc_check():
    spec = importlib.util.spec_from_file_location("ci_kdoc_only_change", KDOC_CHECK)
    module = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(module)
    return module


def classify_paths(root: Path, paths: list[str], base: str | None = None, head: str | None = None) -> Plan:
    """Plans checks for changed paths. Skips KDoc-only Kotlin edits when both revisions are given."""
    try:
        code = filter_ignored(paths)
        notes = [(path, "documentation or metadata") for path in paths if path not in set(code)]
        if base and head:
            kdoc_check = load_kdoc_check()
            kdoc_only = [
                path for path in code if path.endswith((".kt", ".kts")) and kdoc_check.is_kdoc_only(base, head, path)
            ]
            notes += [(path, "KDoc only") for path in kdoc_only]
            code = [path for path in code if path not in set(kdoc_only)]
        return plan_changes(Repo(root), code, notes)
    except Exception as error:  # noqa: BLE001 - an unexpected failure must select full validation.
        return Plan.full(f"the change impact could not be determined ({type(error).__name__}: {error})", fallback=True)


def git(root: Path, *arguments: str) -> str:
    return subprocess.run(
        ["git", "-C", str(root), *arguments], capture_output=True, text=True, check=True
    ).stdout


def decide(root: Path, base: str | None, head: str | None) -> Plan:
    """Plans checks for the commits a pull request adds on top of its merge base."""
    try:
        if not base or not head:
            raise ValueError("the base or head revision is missing")
        merge_base = git(root, "merge-base", base, head).strip()
        diff = git(root, "-c", "core.quotePath=false", "diff", "--name-only", "--no-renames", "-z", f"{base}...{head}")
    except (OSError, subprocess.CalledProcessError, ValueError) as error:
        detail = error.stderr.strip() if isinstance(error, subprocess.CalledProcessError) else str(error)
        return Plan.full(f"the changed files could not be listed ({detail})", fallback=True)
    return classify_paths(root, [path for path in diff.split("\0") if path], merge_base, head)


def graph_lines(repo: Repo) -> list[str]:
    lines = [f"project {path} {directory}" for path, directory in repo.projects.items()]
    lines += [f"edge {path} {dependency}" for path, dependencies in repo.dependencies.items() for dependency in dependencies]
    lines += [f"library {path}" for path in repo.library]
    return sorted(lines)


def github_outputs(plan: Plan) -> dict[str, str]:
    selective = plan.mode == "selective"
    outputs = {"mode": plan.mode}
    outputs.update({check: "true" if plan.checks[check] else "false" for check in CHECKS})
    # Empty lists select every module and every screenshot class.
    outputs["test_modules"] = ",".join(sorted(plan.test_modules)) if selective else ""
    outputs["screenshot_tests"] = ",".join(plan.screenshots.classes) if selective else ""
    return outputs


def names(values) -> str:
    return ", ".join(f"`{value}`" for value in sorted(values))


def short_class(name: str) -> str:
    return name.rsplit(".", 1)[-1].removesuffix("Kt")


def check_rows(plan: Plan) -> list[tuple[str, str, str]]:
    full = plan.mode == "full"
    library = plan.test_modules & plan.library
    screenshots = plan.screenshots
    if full:
        screenshot_scope = "All classes"
    elif not screenshots.classes:
        screenshot_scope = f"All {screenshots.total} classes: {screenshots.reason}" if screenshots.run else ""
    else:
        screenshot_scope = (
            f"{len(screenshots.classes)} of {screenshots.total} classes: "
            + ", ".join(short_class(name) for name in screenshots.classes)
        )
    return [
        ("Lint", "lint", "All Kotlin sources and the CI impact graph"),
        ("Compile, Assemble", "build", "All CI targets"),
        ("API compatibility", "api", "Published modules"),
        ("JVM tests", "jvm", "All modules and buildSrc" if full else names(plan.test_modules & plan.jvm_tested)),
        (
            "Android instrumented tests",
            "android",
            "All modules with device tests" if full else f"{names(library)}, where each shard has device tests",
        ),
        ("Wasm tests", "wasm", "All library modules" if full else names(library)),
        ("iOS tests", "ios", "All library modules" if full else names(library)),
        ("Screenshot tests", "screenshot", screenshot_scope),
    ]


def render_summary(plan: Plan) -> str:
    lines = ["## CI change impact", ""]
    if plan.mode == "none":
        lines.append("**Decision:** No checks needed. Every changed file is documentation, metadata or a KDoc edit.")
    elif plan.mode == "full":
        label = "Full validation as a fallback" if plan.fallback else "Full validation"
        lines.append(f"**Decision:** {label}, because {plan.reason}.")
    else:
        lines += [
            "**Decision:** Selected validation.",
            "",
            f"**Changed modules:** {names(plan.changed) or 'none, only tests or screenshots changed'}",
            "",
            f"**Affected modules:** {names(plan.affected) or 'none'}",
        ]
    lines += ["", "| Check | Runs | Scope |", "|---|---|---|"]
    for label, check, scope in check_rows(plan):
        runs = plan.checks[check]
        lines.append(f"| {label} | {'yes' if runs else 'no'} | {scope if runs else ''} |")
    if plan.mode == "selective":
        skipped = plan.library - plan.test_modules
        if skipped:
            lines += ["", f"**Skipped:** library tests for {names(skipped)}."]
        screenshots = plan.screenshots
        if screenshots.classes:
            lines += ["", f"**Skipped:** {screenshots.total - len(screenshots.classes)} screenshot classes that render no affected module."]
    if plan.files:
        listed = [f"- `{path.replace('`', chr(39))}`: {detail}" for path, detail in plan.files[:MAX_LISTED_FILES]]
        if len(plan.files) > MAX_LISTED_FILES:
            listed.append(f"- and {len(plan.files) - MAX_LISTED_FILES} more")
        lines += ["", f"<details><summary>Changed files ({len(plan.files)})</summary>", "", *listed, "", "</details>"]
    return "\n".join(lines) + "\n"


def append_to_github_file(variable: str, text: str) -> None:
    path = os.environ.get(variable)
    if path:
        with open(path, "a", encoding="utf-8") as file:
            file.write(text)


def main(argv: list[str] | None = None) -> int:
    parser = argparse.ArgumentParser(description="Choose the pull request checks a change needs.")
    parser.add_argument("base", nargs="?", help="pull request base commit")
    parser.add_argument("head", nargs="?", help="pull request head commit")
    parser.add_argument("--files", nargs="*", metavar="PATH", help="classify these repository paths")
    parser.add_argument("--print-graph", action="store_true", help="print the projects and dependencies this script sees")
    parser.add_argument("--json", action="store_true", help="print the GitHub outputs as JSON")
    args = parser.parse_args(argv)

    if args.print_graph:
        print("\n".join(graph_lines(Repo(ROOT))))
        return 0
    plan = classify_paths(ROOT, args.files) if args.files is not None else decide(ROOT, args.base, args.head)
    outputs = github_outputs(plan)
    summary = render_summary(plan)
    append_to_github_file("GITHUB_OUTPUT", "".join(f"{key}={value}\n" for key, value in outputs.items()))
    append_to_github_file("GITHUB_STEP_SUMMARY", summary)
    print(json.dumps(outputs, indent=2) if args.json else summary)
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
