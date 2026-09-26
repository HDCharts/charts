---
title: Workflow Security
order: 8
---

# Workflow Security

The release workflows use three GitHub environments. Each one allows only `main`, and
administrators cannot bypass them.

| Environment | Purpose |
|---|---|
| `Release Approval` | Approval before a release publishes anything. |
| `Production` | Release publishing and docs promotion. |
| `Snapshot` | Snapshot publishing. |

`dautovicharis` or `hdcodedev` can approve a release.

## Repository Policy

Changes to `main` go through a pull request. The ruleset allows squash merge only, requires
resolved review conversations, and requires the three [merge gates](pull-requests.md#merge-gates).
It does not require an approving review.

A tag ruleset blocks updating or deleting any tag except `snapshot`, so a pushed release tag is
permanent.

Dependabot keeps GitHub Actions versions current.
