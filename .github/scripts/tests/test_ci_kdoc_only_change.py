#!/usr/bin/env python3
"""Check which Kotlin edits may skip CI."""

import importlib.util
from pathlib import Path
import unittest
from unittest.mock import patch


spec = importlib.util.spec_from_file_location(
    "kdoc_change", Path(__file__).resolve().parent.parent / "ci-kdoc-only-change.py"
)
kdoc_change = importlib.util.module_from_spec(spec)
spec.loader.exec_module(kdoc_change)


class KdocOnlyChangeTest(unittest.TestCase):
    def test_classifies_kotlin_edits(self):
        cases = [
            (
                "KDoc body text",
                "/**\n * Old description\n */\nfun value() = 1",
                "/**\n * New description\n */\nfun value() = 1",
                True,
            ),
            (
                "KDoc body line added",
                "/**\n * Description\n */\nfun value() = 1",
                "/**\n * Description\n * More detail\n */\nfun value() = 1",
                True,
            ),
            (
                "function body and KDoc changed",
                "/**\n * Old description\n */\nfun value() = 1",
                "/**\n * New description\n */\nfun value() = 2",
                False,
            ),
            (
                "closing delimiter moved to expose a function",
                "/**\n * Description\nfun value() = 1\n */",
                "/**\n * Description\n */\nfun value() = 1",
                False,
            ),
            (
                "unclosed nested comment added",
                "/**\n * Description\n */\nfun value() = 1",
                "/**\n * Description /*\n */\nfun value() = 1",
                False,
            ),
            (
                "comment-looking text inside a raw string",
                'val text = """\n/**\n * Old text\n */\n"""',
                'val text = """\n/**\n * New text\n */\n"""',
                False,
            ),
        ]
        for name, before, after, expected in cases:
            with self.subTest(name=name):
                with patch.object(
                    kdoc_change,
                    "read_lines",
                    side_effect=[before.splitlines(), after.splitlines()],
                ):
                    self.assertEqual(
                        expected, kdoc_change.is_kdoc_only("base", "head", "Example.kt")
                    )


if __name__ == "__main__":
    unittest.main()
