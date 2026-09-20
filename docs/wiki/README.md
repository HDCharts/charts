# User-facing wiki content

These files are the source of truth for the public documentation wiki published at
https://charts.hdcode.dev/. Edit them here, in the same PR that changes the public API
they describe.

They are mirrored into `HDCharts/charts-docs` (`content/snapshot/wiki/`) automatically by
`.github/scripts/sync-wiki-docs.sh`, run from the snapshot-release workflow on every code
change and from the release workflow when a release is promoted. Do not edit the copies in
`charts-docs` directly — they are overwritten on the next sync.

This `README.md` is excluded from the sync and does not appear as a wiki page.

- `index.md` — landing page for a documentation version
- `getting-started.md` — installation and setup
- `*-chart.md` — one page per chart type (e.g. `line-chart.md`, `bar-chart.md`). Any file
  matching this `*-chart` suffix is picked up automatically for the wiki sidebar and the
  Agent Prompt Builder's chart picker in `charts-docs` — add a new file, nothing else to
  register.
- `customization.md` — cross-cutting styling, sizing, and interaction guide
- `migration.md` — hand-written migration overview only. Each release's breaking changes
  get their own page instead, generated from `release-notes/` at `/wiki/migration/{release}`
  in `charts-docs` — nothing here to edit when a release ships breaking changes.
