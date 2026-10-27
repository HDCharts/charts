---
title: Releases
order: 0
---

# How HDCharts Ships

Every change goes through the same path: a pull request is checked, merged changes are
published as a nightly snapshot, and a maintainer promotes a tested snapshot to a release.
All workflows live in [`HDCharts/charts`](https://github.com/HDCharts/charts/tree/main/.github/workflows).

```mermaid
sequenceDiagram
  actor Dev as Contributor
  actor Maintainer
  participant C as charts
  participant D as charts-docs
  participant P as Maven, CDN, GitHub
  Dev->>C: Open pull request
  C->>C: PR checks gate the merge
  Maintainer->>C: Merge to main
  loop Nightly, when code changed
    C->>D: Sync release notes, wiki, and GIFs
    C->>P: Publish snapshot
  end
  Maintainer->>C: Run Release
  C->>P: Publish release
  C->>D: Promote snapshot docs to a new version
  Note over D: Vercel deploys the docs site
```

| Stage | Workflow | What it publishes |
| --- | --- | --- |
| Pull request | `pull-request.yml`, `pull-request-api.yml`, `pull-request-gif-validation.yml` | Nothing; it gates the merge. |
| Snapshot | `nightly.yml` → `snapshot-release.yml` | Maven snapshot, snapshot API reference and demo, snapshot Android APK, snapshot docs. |
| Release | `release.yml` | Maven release and tag, versioned API reference and demo, release Android APK, versioned docs, GitHub release. |

## What validates `main`

The pull-request checks. The nightly snapshot publishes `main` as those checks left it when the
last 24 hours brought code changes, and a maintainer starts `release.yml` by hand.

That makes the pull-request checks the tests that gate every commit. See
[Pull Requests](pull-requests.md) for the jobs they run and
[Validation Matrix](validation-matrix.md) for what each one covers.

`warm-gradle-cache.yml` runs on `main` when sources, resources, or build files change and twice a
week. It runs lint, the JVM tests and the API diff, and compiles the other Linux and iOS CI targets.
Dependencies use a cache keyed by build files. Gradle task outputs use a separate cache keyed by
runner OS, architecture, and commit, so source changes refresh the outputs that pull requests
restore. Each warm job restores earlier outputs and saves the updated cache after a successful
build. Pull requests restore the latest available outputs for their platform without saving them.
Other workflows on `main` restore caches read-only. The playground job keeps its own dependency
cache, and release jobs start fresh.
