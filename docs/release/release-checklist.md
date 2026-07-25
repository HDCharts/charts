# Release Checklist

Use the operator-facing workflows in `HDCharts/charts`. The charts-docs
promotion and release-note workflows are internal receivers and are not run
manually.

1. Merge all charts changes intended for the release.
2. Merge the latest automated charts-docs release-note sync pull request.
3. Run `Promote Docs` from the charts `main` branch.
4. Review and merge the generated charts-docs promotion pull request.
5. Run `Docs Release Publish` from the charts `main` branch.
6. Run `Release` from the charts `main` branch.

`Promote Docs` resolves the release version automatically, freezes
`content/snapshot` as `content/<version>`, and registers the version. The charts
release and docs release-publish workflows intentionally fail when that
registry entry is missing.

`Release` also checks that versioned docs/static assets were already published
to S3 for the same release version and charts commit. This prevents publishing
the library before the matching public docs are available.

Manual release workflows resolve their versions from Axion or `.version`; the
manual run form no longer carries generated version preview choices.
See [Versioning](./versioning.md) for the current minor-by-default policy and
the unresolved patch/hotfix release strategy.

`Release` creates the git tag locally before publishing to Maven Central and
pushes it after publishing succeeds. If the workflow fails, rerun `Release` from
GitHub after resolving the failure.

Release-note directories are already versioned and are not moved or reset
during promotion.
