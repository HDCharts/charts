#!/usr/bin/env python3
"""Check which pull request checks ci-impact.py selects for small change sets."""

import importlib.util
from pathlib import Path
import sys
import tempfile
import unittest
from unittest.mock import patch


spec = importlib.util.spec_from_file_location(
    "ci_impact", Path(__file__).resolve().parent.parent / "ci-impact.py"
)
ci_impact = importlib.util.module_from_spec(spec)
# Dataclasses resolve their annotations through sys.modules.
sys.modules[spec.name] = ci_impact
spec.loader.exec_module(ci_impact)

ROOT = ci_impact.ROOT
LINE = "charts-line/src/commonMain/kotlin/io/github/hdcharts/line/LineChart.kt"
PIE = "charts-pie/src/commonMain/kotlin/io/github/hdcharts/pie/PieChart.kt"
RADAR = "charts-radar/src/commonMain/kotlin/io/github/hdcharts/radar/RadarChart.kt"
BAR = "charts-bar/src/commonMain/kotlin/io/github/hdcharts/bar/BarChart.kt"
CORE = "charts-core/src/commonMain/kotlin/io/github/hdcharts/core/model/ChartData.kt"
SCREENSHOT_SOURCES = "sample/androidApp/src/screenshotTest/kotlin/io/github/hdcharts/app/screenshot"
SCREENSHOT_REFERENCES = "sample/androidApp/src/screenshotTestDebug/reference/io/github/hdcharts/app/screenshot"


def plan(*paths):
    return ci_impact.classify_paths(ROOT, list(paths))


def screenshot(name):
    return f"io.github.hdcharts.app.screenshot.{name}Kt"


def selected_checks(result):
    return {check for check, runs in result.checks.items() if runs}


def write_files(root, files):
    for path, text in files.items():
        file = root / path
        file.parent.mkdir(parents=True, exist_ok=True)
        file.write_text(text, encoding="utf-8")


class ChangeSelectionTest(unittest.TestCase):
    def test_documentation_only_change_runs_no_checks(self):
        result = plan("README.md", "docs/wiki/line-chart.md", "release-notes/3.2.0/changes/example.md")

        self.assertEqual("none", result.mode)
        self.assertEqual(set(), selected_checks(result))

    def test_line_change_selects_line_and_its_consumers(self):
        result = plan(LINE)

        self.assertEqual("selective", result.mode)
        self.assertEqual({":charts-line"}, result.changed)
        self.assertLessEqual(
            {":charts-line", ":charts", ":sample-shared", ":app", ":androidApp", ":smoke-line"}, result.test_modules
        )
        self.assertFalse(result.test_modules & {":charts-core", ":charts-pie", ":charts-radar", ":charts-heatmap"})
        self.assertEqual(set(ci_impact.CHECKS), selected_checks(result))
        self.assertIn(screenshot("LineChartScreenshotTest"), result.screenshots.classes)
        self.assertIn(screenshot("WikiChartScreenshotTest"), result.screenshots.classes)
        self.assertNotIn(screenshot("PieChartScreenshotTest"), result.screenshots.classes)
        self.assertNotIn(screenshot("StackedBarChartScreenshotTest"), result.screenshots.classes)

    def test_pie_and_radar_changes_select_the_union(self):
        result = plan(PIE, RADAR)

        self.assertEqual({":charts-pie", ":charts-radar"}, result.changed)
        self.assertFalse(result.test_modules & {":charts-line", ":charts-bar"})
        classes = set(result.screenshots.classes)
        self.assertLessEqual(
            {
                screenshot("PieChartScreenshotTest"),
                screenshot("RadarChartScreenshotTest"),
                screenshot("MultiSeriesRadarChartScreenshotTest"),
            },
            classes,
        )
        self.assertNotIn(screenshot("LineChartScreenshotTest"), classes)

    def test_bar_change_selects_histogram_which_depends_on_it(self):
        result = plan(BAR)

        self.assertIn(":charts-histogram", result.test_modules)
        self.assertIn(screenshot("HistogramChartScreenshotTest"), result.screenshots.classes)

    def test_gauge_change_selects_both_gauge_charts(self):
        result = plan("charts-gauge/src/commonMain/kotlin/io/github/hdcharts/gauge/RingGaugeChart.kt")

        self.assertIn(":charts-gauge", result.test_modules)
        self.assertLessEqual(
            {screenshot("RingGaugeChartScreenshotTest"), screenshot("SegmentedRingGaugeChartScreenshotTest")},
            set(result.screenshots.classes),
        )

    def test_core_change_selects_every_library_module_and_screenshot(self):
        result = plan(CORE)

        self.assertLessEqual(ci_impact.Repo(ROOT).library, result.test_modules)
        self.assertTrue(result.screenshots.run)
        self.assertEqual([], result.screenshots.classes)

    def test_build_logic_selects_full_validation(self):
        for path in (
            "build.gradle.kts",
            "settings.gradle.kts",
            "gradle.properties",
            "gradle/libs.versions.toml",
            "buildSrc/src/main/kotlin/ChartsModules.kt",
            "karma.config.d/timeout.js",
        ):
            with self.subTest(path=path):
                result = plan(path)

                self.assertEqual("full", result.mode)
                self.assertEqual(set(ci_impact.CHECKS), selected_checks(result))
                self.assertEqual("", ci_impact.github_outputs(result)["test_modules"])

    def test_chart_test_change_runs_only_that_modules_tests(self):
        result = plan("charts-bar/src/jvmTest/kotlin/io/github/hdcharts/bar/internal/BarChartScrollFrameTest.kt")

        self.assertEqual(set(), result.changed)
        self.assertEqual({":charts-bar"}, result.test_modules)
        self.assertEqual({"lint", "jvm", "android", "wasm", "ios"}, selected_checks(result))

    def test_chart_screenshot_change_selects_only_its_class(self):
        result = plan(f"{SCREENSHOT_SOURCES}/PieChartScreenshotTest.kt")

        self.assertEqual([screenshot("PieChartScreenshotTest")], result.screenshots.classes)
        self.assertEqual({"lint", "screenshot"}, selected_checks(result))

    def test_reference_image_change_selects_its_class(self):
        result = plan(f"{SCREENSHOT_REFERENCES}/RadarChartScreenshotTestKt/RadarChartDefaultPreview_Light_5e637cf0_0.png")

        self.assertEqual([screenshot("RadarChartScreenshotTest")], result.screenshots.classes)
        self.assertEqual({"screenshot"}, selected_checks(result))

    def test_shared_screenshot_inputs_select_every_screenshot(self):
        for path in (
            f"{SCREENSHOT_SOURCES}/shared/ScreenshotFixtures.kt",
            f"{SCREENSHOT_SOURCES}/GradientScreenshotSupport.kt",
            "sample/shared/src/commonMain/kotlin/io/github/hdcharts/sampleshared/theme/AppTheme.kt",
            "sample/androidApp/build.gradle.kts",
            "sample/androidApp/src/main/res/values/strings.xml",
        ):
            with self.subTest(path=path):
                result = plan(path)

                self.assertTrue(result.screenshots.run)
                self.assertEqual([], result.screenshots.classes)
                self.assertEqual("", ci_impact.github_outputs(result)["screenshot_tests"])

    def test_docs_example_change_selects_screenshots_that_import_it(self):
        result = plan("sample/androidApp/src/main/kotlin/io/github/hdcharts/app/docs/BarBasicExample.kt")

        self.assertIn(screenshot("WikiChartScreenshotTest"), result.screenshots.classes)
        self.assertNotIn(screenshot("BarChartScreenshotTest"), result.screenshots.classes)
        self.assertEqual({"lint", "build", "screenshot"}, selected_checks(result))

    def test_sample_change_skips_library_platform_tests(self):
        result = plan("sample/api/src/commonMain/kotlin/io/github/hdcharts/sampleapi/SampleApiModule.kt")

        self.assertEqual({":sample-api", ":app", ":androidApp"}, result.test_modules)
        self.assertFalse(result.checks["api"] or result.checks["android"] or result.checks["wasm"] or result.checks["ios"])
        self.assertTrue(result.checks["jvm"])

    def test_unknown_module_selects_full_validation(self):
        result = plan("charts-funnel/src/commonMain/kotlin/io/github/hdcharts/funnel/FunnelChart.kt")

        self.assertEqual("full", result.mode)

    def test_deleted_and_renamed_files_select_every_owning_module(self):
        # git diff --no-renames lists a move as a deletion plus an addition.
        result = plan(
            "charts-pie/src/commonMain/kotlin/io/github/hdcharts/pie/Moved.kt",
            "charts-line/src/commonMain/kotlin/io/github/hdcharts/line/Moved.kt",
            "charts-scatter/src/commonMain/kotlin/io/github/hdcharts/scatter/Deleted.kt",
        )

        self.assertEqual({":charts-pie", ":charts-line", ":charts-scatter"}, result.changed)

    def test_removed_module_selects_full_validation(self):
        result = plan("settings.gradle.kts", "charts-scatter/src/commonMain/kotlin/io/github/hdcharts/scatter/ScatterChart.kt")

        self.assertEqual("full", result.mode)

    def test_removed_screenshot_source_selects_every_screenshot(self):
        result = plan(f"{SCREENSHOT_SOURCES}/RemovedChartScreenshotTest.kt")

        self.assertTrue(result.screenshots.run)
        self.assertEqual([], result.screenshots.classes)

    def test_missing_or_invalid_revisions_select_full_validation(self):
        for base, head in ((None, None), ("", "HEAD"), ("0" * 40, "f" * 40)):
            with self.subTest(base=base, head=head):
                result = ci_impact.decide(ROOT, base, head)

                self.assertEqual("full", result.mode)
                self.assertTrue(result.fallback)

    def test_classifier_error_selects_full_validation(self):
        with patch.object(ci_impact, "plan_changes", side_effect=RuntimeError("broken")):
            result = plan(LINE)

        self.assertEqual("full", result.mode)
        self.assertTrue(result.fallback)
        self.assertIn("Full validation as a fallback", ci_impact.render_summary(result))

    def test_selection_code_changes_select_full_validation(self):
        for path in (
            ".github/scripts/ci-impact.py",
            ".github/scripts/tests/test_ci_impact.py",
            ".github/workflows/test.yml",
            "scripts/ci-has-code-changes.sh",
        ):
            with self.subTest(path=path):
                self.assertEqual("full", plan(path).mode)

    def test_lint_and_api_inputs_run_only_their_check(self):
        self.assertEqual({"lint"}, selected_checks(plan(".editorconfig")))
        self.assertEqual({"api"}, selected_checks(plan("API-COMPATIBILITY-BREAKS.txt")))

    def test_kdoc_only_change_runs_no_checks(self):
        kdoc_check = type("KdocCheck", (), {"is_kdoc_only": staticmethod(lambda base, head, path: True)})
        with patch.object(ci_impact, "load_kdoc_check", return_value=kdoc_check):
            result = ci_impact.classify_paths(ROOT, [LINE], "base", "head")

        self.assertEqual("none", result.mode)

    def test_outputs_pass_the_selection_to_gradle(self):
        outputs = ci_impact.github_outputs(plan(LINE))
        modules = outputs["test_modules"].split(",")

        self.assertEqual("selective", outputs["mode"])
        self.assertEqual(sorted(modules), modules)
        self.assertLessEqual({":charts-line", ":charts"}, set(modules))
        self.assertNotIn(":charts-pie", modules)
        self.assertIn(screenshot("LineChartScreenshotTest"), outputs["screenshot_tests"].split(","))
        self.assertEqual("true", outputs["ios"])

    def test_summary_names_skipped_scopes(self):
        summary = ci_impact.render_summary(plan(LINE))

        self.assertIn("**Decision:** Selected validation.", summary)
        self.assertIn("**Skipped:** library tests for", summary)
        self.assertIn("`:charts-pie`", summary)


class GraphTest(unittest.TestCase):
    def test_parses_includes_directories_and_dependencies(self):
        projects = ci_impact.parse_projects(
            'include(":charts-core")\n'
            'include(":charts-line", ":app")\n'
            '// include(":charts-old")\n'
            'project(":app").projectDir = file("sample/app")\n'
        )
        dependencies = ci_impact.parse_dependencies(
            "api(projects.chartsCore)\n"
            'implementation(project(":charts-line"))\n'
            '// implementation(project(":app"))\n',
            projects,
        )

        self.assertEqual({":charts-core": "charts-core", ":charts-line": "charts-line", ":app": "sample/app"}, projects)
        self.assertEqual({":charts-core", ":charts-line"}, dependencies)

    def test_graph_lists_projects_dependencies_and_library(self):
        lines = set(ci_impact.graph_lines(ci_impact.Repo(ROOT)))

        self.assertIn("project :app sample/app", lines)
        self.assertIn("edge :charts-histogram :charts-bar", lines)
        self.assertIn("library :charts-line", lines)
        self.assertNotIn("library :sample-shared", lines)

    def test_screenshot_file_with_a_shared_declaration_is_not_a_plain_test(self):
        preview = '@PreviewTest\n@Preview(\n    name = "Light",\n)\n@Composable\nfun APreview() {}\n'
        plain = preview + "\nprivate fun helper() = 1\n"

        self.assertTrue(ci_impact.declares_only_previews(plain))
        self.assertFalse(ci_impact.declares_only_previews(plain + "\ninternal fun sharedHelper() = 1\n"))
        self.assertFalse(ci_impact.declares_only_previews(plain + "\ninternal const val SHARED = 1\n"))


class SyntheticRepositoryTest(unittest.TestCase):
    FILES = {
        "settings.gradle.kts": (
            'include(":charts", ":charts-core", ":charts-line", ":androidApp")\n'
            'project(":androidApp").projectDir = file("sample/androidApp")\n'
        ),
        "charts/build.gradle.kts": "api(projects.chartsCore)\napi(projects.chartsLine)\n",
        "charts-line/build.gradle.kts": "api(projects.chartsCore)\n",
        "charts-core/src/commonMain/kotlin/Core.kt": "package io.github.hdcharts.core\n",
        "charts-line/src/commonMain/kotlin/Line.kt": "package io.github.hdcharts.line\n",
        "sample/androidApp/build.gradle.kts": 'implementation(project(":charts"))\n',
        "sample/androidApp/src/main/kotlin/App.kt": "package io.github.hdcharts.app\n",
        "sample/androidApp/src/screenshotTest/kotlin/TextScreenshotTest.kt": (
            "package io.github.hdcharts.app.screenshot\n\n@PreviewTest\n@Composable\nfun TextPreview() = Text()\n"
        ),
    }
    SCREENSHOT = "sample/androidApp/src/screenshotTest/kotlin/LineScreenshotTest.kt"
    PREVIEW = "@PreviewTest\n@Composable\nfun LinePreview() = LineChart()\n"

    def plan_with_screenshot(self, source, *changed):
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            write_files(root, {**self.FILES, self.SCREENSHOT: source})
            return ci_impact.classify_paths(root, list(changed))

    def test_selects_the_screenshot_class_that_imports_a_changed_chart(self):
        source = "package io.github.hdcharts.app.screenshot\n\nimport io.github.hdcharts.line.LineChart\n\n" + self.PREVIEW

        result = self.plan_with_screenshot(source, "charts-line/src/commonMain/kotlin/Line.kt")

        self.assertEqual(["io.github.hdcharts.app.screenshot.LineScreenshotTestKt"], result.screenshots.classes)

    def test_custom_class_name_selects_every_screenshot(self):
        source = (
            '@file:JvmName("LineScreenshots")\n\npackage io.github.hdcharts.app.screenshot\n\n'
            "import io.github.hdcharts.line.LineChart\n\n" + self.PREVIEW
        )

        result = self.plan_with_screenshot(source, "charts-line/src/commonMain/kotlin/Line.kt")

        self.assertTrue(result.screenshots.run)
        self.assertEqual([], result.screenshots.classes)

    def test_unknown_project_package_ties_a_screenshot_to_every_change(self):
        source = "package io.github.hdcharts.app.screenshot\n\nimport io.github.hdcharts.funnel.FunnelChart\n\n" + self.PREVIEW

        result = self.plan_with_screenshot(source, "sample/androidApp/src/main/kotlin/App.kt")

        self.assertEqual(["io.github.hdcharts.app.screenshot.LineScreenshotTestKt"], result.screenshots.classes)


if __name__ == "__main__":
    unittest.main()
