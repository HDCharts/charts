# Release notes

This directory is the source of truth for user-facing HDCharts release notes.
Each target release has two fragment directories:

```text
release-notes/<version>/
├── changes/
└── migrations/
```

- `changes/` contains one short changeset for each public, user-facing charts
  pull request. Only put text in `release_note` when it belongs in the public
  "What's New" section. Internal release automation, CI, refactors, and
  maintenance-only changes should either omit a changeset or keep
  `release_note` empty.
- `migrations/` contains one migration fragment for each pull request with
  breaking API changes.

The target release is the value in `.version` without the `-SNAPSHOT` suffix.
When the target directory exists, `Snapshot Release` mirrors it into
`charts-docs/release-notes/<version>/` and updates the current-version pointer.
A missing target directory is allowed for snapshots because the next release
version may not have been selected yet. Final releases still require their
versioned release-note directory.

Release directories remain in this repository as history. Keeping them
versioned lets snapshot and released documentation consume the same immutable
fragments without copying or resetting release-note files during promotion.
