# Docs Promotion (Cross-Repo)

Workflow: `charts/.github/workflows/promote-docs.yml`

```mermaid
flowchart TD
  A["workflow_dispatch: charts: promote-docs.yml"] --> B["axion: resolve-release-version.sh"]
  B --> C{"CHARTS_WORKFLOW_TOKEN set?"}
  C -- No --> CX["Fail"]
  C -- Yes --> E["charts-docs: promote-docs.yml (workflow_dispatch)"]
  E --> F["promote-snapshot-to-release.sh"]
  F --> G["reset-snapshot-content.sh"]
  G --> H["test-docs-release-links-contract.sh"]
  H --> I["Build docs app (DOCS_STATIC_BASE_URL required)"]
  I --> J["Create PR with promoted docs changes"]
```

Notes:
- `promote-snapshot-to-release.sh`: copies `content/snapshot` to `content/{release_version}` and updates registry.
- `reset-snapshot-content.sh`: clears snapshot changes and resets breaking-changes baseline.
