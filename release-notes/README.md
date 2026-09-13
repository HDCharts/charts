# Release notes

This directory is the source of truth for user-facing HDCharts release notes.
Each target release has two fragment directories:

```text
release-notes/<version>/
├── changes/
└── migrations/
```

- `changes/` contains concise release notes for coherent public, user-facing
  topics. Merge related API, behavior, and hardening work into one note when it
  describes one user outcome. Only put text in `release_note` when it belongs
  in the public "What's New" section. Internal release automation, CI,
  refactors, and maintenance-only changes should omit a release note.
- `migrations/` contains one concise migration document for each coherent
  breaking API topic. Merge related changes into the topic's canonical file;
  do not create separate fragments for expected cleanup within the same API
  transition.

The target release is the current Axion-resolved snapshot version (the
`currentVersion` value without the `-SNAPSHOT` suffix). When the target directory
exists, `Snapshot Release` mirrors it into
`charts-docs/release-notes/<version>/` and updates the current-version pointer.
A missing target directory is allowed for snapshots because the next release
version may not have been selected yet. Final releases still require their
versioned release-note directory.

Release directories remain in this repository as history. Keeping them
versioned lets snapshot and released documentation consume the same immutable
fragments without copying or resetting release-note files during promotion.
