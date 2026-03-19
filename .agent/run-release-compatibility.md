# Check Release Compatibility

Use this flow to generate concise, end-user migration notes for snapshot release docs.

## Workflow

1. Resolve baseline version:
   - If `baseline_version` is provided, use it.
   - Otherwise use latest SemVer tag:
     - `git tag --sort=-version:refname | grep -E '^[0-9]+\.[0-9]+\.[0-9]+$' | head -n 1`
2. Run:
   - `git fetch --tags`
   - `./gradlew apiCompatibilityCheck -PapiCompatibilityBaselineVersion=<baseline_version> --no-daemon --continue`
3. Read reports:
   - `build/reports/api-compatibility/*.md`
4. Write output:
   - Use `charts_docs_root=../charts-docs` (charts-docs is always a sibling of the `charts` repo).
   - Ensure `<charts_docs_root>/content/snapshot/` exists.
   - File: `<charts_docs_root>/content/snapshot/breaking-changes.md`
   - Template: `.agent/templates/migration-guide.md.tpl`
   - If no breaking modules are reported, write `No call-site updates required.`
   - If breaking modules exist, add one `<module>` section per breaking module.
5. Content rules:
   - Follow `.agent/templates/migration-guide.md.tpl`.
