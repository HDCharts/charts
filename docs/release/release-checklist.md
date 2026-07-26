# Release Checklist

Use the operator-facing workflows in `HDCharts/charts`.

1. Merge all charts changes intended for the release.
2. Ensure the latest snapshot release has synchronized its release notes.
3. Run `Release` from the charts `main` branch.
4. Approve the `Release Approval` environment.

`Release` pins one charts commit, publishes its API and demo assets, publishes
Maven artifacts, synchronizes release notes, promotes the docs version, and
waits for the public docs deployment to serve the matching commit.

The version is resolved from Axion after checkout. Leave
`replace_static_assets` disabled unless intentionally replacing pre-release API
and demo objects already present in S3.
See [Versioning](./versioning.md) for the current minor-by-default policy and
the unresolved patch/hotfix release strategy.

`Release` creates the git tag locally before publishing to Maven Central and
pushes it after publishing succeeds. If a downstream verification step fails,
use **Re-run failed jobs** after resolving the failure.

Release-note directories are already versioned and are not moved or reset
during promotion.
