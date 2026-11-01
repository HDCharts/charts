#!/usr/bin/env python3
"""Focused tests for the wiki example synchronizer."""

import importlib.util
import io
from pathlib import Path
import sys
import tempfile
import unittest
from contextlib import redirect_stderr, redirect_stdout


ROOT = Path(__file__).resolve().parents[3]
SPEC = importlib.util.spec_from_file_location(
    "sync_wiki_examples", ROOT / "scripts/sync-wiki-examples.py"
)
sync = importlib.util.module_from_spec(SPEC)
sys.modules[SPEC.name] = sync
SPEC.loader.exec_module(sync)


class SyncWikiExamplesTest(unittest.TestCase):
    def setUp(self):
        self.temp = tempfile.TemporaryDirectory()
        self.root = Path(self.temp.name)
        self.source = self.root / "sample/Pie.kt"
        self.markdown = self.root / "docs/wiki/pie.md"
        self.source.parent.mkdir(parents=True)
        self.markdown.parent.mkdir(parents=True)
        self.source.write_text(
            "package sample\n"
            "// BEGIN WIKI EXAMPLE: basic\n"
            "import sample.PieChart\n\n"
            "@Composable\nfun ShowPie() = PieChart()\n"
            "// END WIKI EXAMPLE: basic\n",
            encoding="utf-8",
        )
        self.markdown.write_text(
            "# Pie\n\nKeep this text.\n\n"
            "<!-- BEGIN WIKI EXAMPLE: sample/Pie.kt#basic -->\n"
            "```kotlin\nold()\n```\n"
            "<!-- END WIKI EXAMPLE: basic -->\n\nKeep this too.\n",
            encoding="utf-8",
        )

    def tearDown(self):
        self.temp.cleanup()

    def run_sync(self, check=False):
        output = io.StringIO()
        with redirect_stderr(output), redirect_stdout(output):
            result = sync.main(["--check"] if check else [], repo_root=self.root)
        self.last_output = output.getvalue()
        return result

    def test_synchronizes_snippet_and_preserves_unmanaged_markdown(self):
        self.assertEqual(0, self.run_sync())
        content = self.markdown.read_text(encoding="utf-8")
        self.assertIn("Keep this text.", content)
        self.assertIn("Keep this too.", content)
        self.assertIn("```kotlin\nimport sample.PieChart\n\n@Composable\nfun ShowPie() = PieChart()\n```", content)
        self.assertNotIn("package sample", content)

    def test_clean_check_passes(self):
        self.run_sync()
        self.assertEqual(0, self.run_sync(check=True))

    def test_stale_markdown_fails_check_without_modifying_file(self):
        before = self.markdown.read_bytes()
        self.assertEqual(1, self.run_sync(check=True))
        self.assertEqual(before, self.markdown.read_bytes())

    def test_missing_source_marker_is_reported(self):
        self.source.write_text("package sample\n", encoding="utf-8")
        self.assertEqual(1, self.run_sync())
        self.assertIn(str(self.markdown), self.last_output)
        self.assertIn("sample/Pie.kt", self.last_output)
        self.assertIn("source marker 'basic' is missing", self.last_output)

    def test_duplicate_and_malformed_source_markers_are_rejected(self):
        self.source.write_text(
            "// BEGIN WIKI EXAMPLE: basic\none\n// END WIKI EXAMPLE: basic\n"
            "// BEGIN WIKI EXAMPLE: basic\ntwo\n// END WIKI EXAMPLE: basic\n",
            encoding="utf-8",
        )
        self.assertEqual(1, self.run_sync())
        self.assertIn("expected exactly one source start and end marker", self.last_output)
        self.source.write_text(
            "// BEGIN WIKI EXAMPLE basic\ncontent\n// END WIKI EXAMPLE: basic\n",
            encoding="utf-8",
        )
        self.assertEqual(1, self.run_sync())
        self.assertIn("malformed source marker", self.last_output)

    def test_multiple_regions_preserve_markdown_and_check_drift(self):
        self.source.write_text(
            "package sample\n"
            "// BEGIN WIKI EXAMPLE: basic\n"
            "import sample.PieChart\n\n"
            "fun Basic() {\n    val data = listOf(1, 2, 3)\n    PieChart(data)\n}\n"
            "// END WIKI EXAMPLE: basic\n"
            "// BEGIN WIKI EXAMPLE: palette\nfun Palette() = PieChart(style = style)\n"
            "// END WIKI EXAMPLE: palette\n",
            encoding="utf-8",
        )
        self.markdown.write_text(
            "# Pie\n\nBefore first.\n"
            "<!-- BEGIN WIKI EXAMPLE: sample/Pie.kt#basic -->\n"
            "```kotlin\nold()\n```\n"
            "<!-- END WIKI EXAMPLE: basic -->\n"
            "Between regions.\n"
            "<!-- BEGIN WIKI EXAMPLE: sample/Pie.kt#palette -->\n"
            "```kotlin\nold one\nold two\nold three\n```\n"
            "<!-- END WIKI EXAMPLE: palette -->\n"
            "After second.\n",
            encoding="utf-8",
        )
        self.assertEqual(0, self.run_sync())
        expected = (
            "# Pie\n\nBefore first.\n"
            "<!-- BEGIN WIKI EXAMPLE: sample/Pie.kt#basic -->\n"
            "```kotlin\nimport sample.PieChart\n\n"
            "fun Basic() {\n    val data = listOf(1, 2, 3)\n    PieChart(data)\n}\n```\n"
            "<!-- END WIKI EXAMPLE: basic -->\n"
            "Between regions.\n"
            "<!-- BEGIN WIKI EXAMPLE: sample/Pie.kt#palette -->\n"
            "```kotlin\nfun Palette() = PieChart(style = style)\n```\n"
            "<!-- END WIKI EXAMPLE: palette -->\n"
            "After second.\n"
        )
        self.assertEqual(expected, self.markdown.read_text(encoding="utf-8"))

        self.assertEqual(0, self.run_sync())
        self.assertEqual(expected, self.markdown.read_text(encoding="utf-8"))
        self.assertEqual(0, self.run_sync(check=True))

        drifted = expected.replace("fun Palette()", "fun StalePalette()")
        self.markdown.write_text(drifted, encoding="utf-8")
        before_check = self.markdown.read_bytes()
        self.assertEqual(1, self.run_sync(check=True))
        self.assertEqual(before_check, self.markdown.read_bytes())

    def test_duplicate_or_malformed_destination_markers_are_rejected(self):
        duplicate = (
            "<!-- BEGIN WIKI EXAMPLE: sample/Pie.kt#basic -->\n```kotlin\n```\n"
            "<!-- END WIKI EXAMPLE: basic -->\n\n"
        )
        self.markdown.write_text(duplicate + duplicate, encoding="utf-8")
        self.assertEqual(1, self.run_sync())
        self.markdown.write_text(
            "<!-- BEGIN WIKI EXAMPLE sample/Pie.kt#basic -->\n"
            "```kotlin\n```\n<!-- END WIKI EXAMPLE: basic -->\n",
            encoding="utf-8",
        )
        self.assertEqual(1, self.run_sync())
        self.assertIn("malformed destination marker", self.last_output)

    def test_missing_referenced_source_is_reported(self):
        self.markdown.write_text(
            "<!-- BEGIN WIKI EXAMPLE: sample/Missing.kt#basic -->\n"
            "```kotlin\n```\n<!-- END WIKI EXAMPLE: basic -->\n",
            encoding="utf-8",
        )
        self.assertEqual(1, self.run_sync())
        self.assertIn("referenced source does not exist: sample/Missing.kt", self.last_output)

    def test_running_sync_twice_is_idempotent(self):
        self.run_sync()
        first = self.markdown.read_bytes()
        self.run_sync()
        self.assertEqual(first, self.markdown.read_bytes())


if __name__ == "__main__":
    unittest.main()
