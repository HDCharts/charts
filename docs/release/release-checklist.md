# Release Checklist

Use the operator-facing workflows in `HDCharts/charts`. The charts-docs
promotion and release-note workflows are internal receivers and are not run
manually.

1. Merge all charts changes intended for the release.
2. Merge the latest automated charts-docs release-note sync pull request.
3. Run `Promote Docs` from the charts `main` branch.
4. Review and merge the generated charts-docs promotion pull request.
5. Run `Release` from the charts `main` branch.
6. Run `Docs Release Publish` after the charts release succeeds.

`Promote Docs` resolves the release version automatically, freezes
`content/snapshot` as `content/<version>`, and registers the version. The charts
release and docs release-publish workflows intentionally fail when that
registry entry is missing.

Release-note directories are already versioned and are not moved or reset
during promotion.
