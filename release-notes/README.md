# Release notes

This directory is the source of truth for user-facing HDCharts release notes.
Each target release has two fragment directories:

```text
release-notes/<version>/
├── changes/
└── migrations/
```

- `changes/` contains one short changeset for each user-facing charts pull
  request.
- `migrations/` contains one migration fragment for each pull request with
  breaking API changes.

The target release is the value in `.version` without the `-SNAPSHOT` suffix.
After changes under `release-notes/` merge to `main`, the
`Sync Release Notes` workflow dispatches the charts-docs synchronization
workflow. That workflow mirrors the complete version directory into
`charts-docs/release-notes/<version>/`, updates its current-version pointer,
and opens or updates a charts-docs pull request.

Release directories remain in this repository as history. Keeping them
versioned lets snapshot and released documentation consume the same immutable
fragments without copying or resetting release-note files during promotion.
