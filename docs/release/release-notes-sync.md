# Release Notes Sync

Workflows:

- `charts/.github/workflows/sync-release-notes.yml`
- `charts-docs/.github/workflows/sync-release-notes.yml`

```mermaid
flowchart TD
  A["Merge release-notes changes to charts main"] --> B["charts: Sync Release Notes"]
  B --> C["Resolve target release from .version"]
  C --> D["Dispatch charts-docs with charts commit SHA"]
  D --> E["Checkout charts at the exact source SHA"]
  E --> F["Mirror release-notes/version and update current pointer"]
  F --> G["Validate docs contracts"]
  G --> H["Create or update charts-docs sync PR"]
```

The charts workflow runs automatically when `release-notes/**` changes on
`main`. Run it manually after workflow maintenance, after recovering from a
failed dispatch, or when charts-docs needs to be reconciled with the current
charts source without changing a release-note file.

The charts-docs workflow is an internal `repository_dispatch` receiver on its
default branch. Manual retries and reconciliation always start from the charts
workflow.

Before promoting snapshot docs to a release, merge the latest release-note sync
pull request so the released docs can resolve every merged charts change.

The docs app reads the mirrored fragments directly. Snapshot pages follow the
current-version pointer until that version appears in the release registry;
released pages use their matching version directory. Promotion does not copy or
reset release-note files.
