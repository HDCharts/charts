---
title: Release
order: 6
---

# Release

Workflow: [`release.yml`](https://github.com/HDCharts/charts/blob/main/.github/workflows/release.yml)

```mermaid
sequenceDiagram
  actor Maintainer
  participant R as Release
  participant D as charts-docs
  participant W as Docs site
  participant CDN as S3 + CloudFront
  participant M as Maven Central
  participant GH as GitHub
  Maintainer->>R: Run on main
  R->>R: Pin source SHA, resolve version, audit API
  R->>D: Check snapshot and docs provenance
  R->>Maintainer: Wait for Release Approval
  Maintainer->>R: Approve
  R->>CDN: Publish API reference and demo
  R->>M: Publish release
  R->>GH: Push release tag
  R->>CDN: Build and upload release Android APK
  R->>D: Sync release notes and promote snapshot docs
  D->>W: Vercel deploys the new version
  R->>W: Wait until the release manifest matches
  R->>GH: Publish GitHub release and comment on commit
```

Before running this workflow, complete the
[Release Checklist](release-checklist.md).

Every job builds from the source SHA pinned in the first step, so all published artifacts come
from the same commit.

## Validation

- The release version comes from Axion. If the release tag already exists, the workflow fails
  before publishing anything.
- The release commit must already have a published snapshot: the snapshot manifest in
  `charts-docs` has to match the release version and source SHA.
- If `charts-docs` already has docs for this version, they must come from the same source SHA.
- The API audit runs `apiCompatibilityCheck` and only warns. Breaks are enforced on pull
  requests; see [API Compatibility](api-compatibility.md).

## Publishing

`Release` is the only publishing workflow you start by hand. It waits for the protected
`Release Approval` environment before anything is published.

The optional `replace_static_assets` input replaces pre-release API and demo files for the
resolved version. Leave it off for normal releases.

After the Android build, the workflow promotes the snapshot docs in `charts-docs` to a new
versioned docs folder, syncs the release notes, and waits until the public site serves that exact
docs commit. The GitHub release is published last, with highlights built from the release notes.
