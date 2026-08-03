# Charts Snapshot

Workflow: `charts/.github/workflows/snapshot-release.yml`

```mermaid
flowchart TD
  A["nightly.yml schedule"] --> B["Call Snapshot Release"]
  B --> C["Detect recent changes (24h)"]
  C --> D{"Has relevant changes?"}
  D -- No --> DX["Exit"]
  D -- Yes --> E["Checkout + JDK"]
  E --> F["Gradle (Axion): currentVersion"]
  F --> G{"Is -SNAPSHOT?"}
  G -- No --> GX["Skip snapshot publish"]
  G -- Yes --> H["Pin source SHA and validate charts-docs token"]
  H --> I["Sync release notes and GIF baselines to charts-docs main"]
  I --> J["Publish API reference and demo from pinned SHA"]
  J --> K["Publish Maven snapshot from pinned SHA"]
  K --> L["Call and await shared Android build (snapshot channel)"]
  K --> M["Commit comment"]
```

`release-notes/<version>` is versioned without the `-SNAPSHOT` suffix. When that
directory exists, the workflow synchronizes it before publication so snapshot
pages use the same notes that will later accompany the release. A missing
release-note directory does not block snapshot publication; final releases
still require their versioned release notes. GIF baselines are owned by
`charts/gif-baselines` and synchronized into
`charts-docs/content/snapshot/wiki/assets`; final release promotion carries
those assets into the versioned docs directory. The workflow also writes
`charts-docs/docs-app/public/snapshot-manifest.json`; the release validation
stage requires that manifest to match the exact release source SHA. The shared
`Android Build` workflow checks out the pinned source SHA, matching `Snapshot
Release`.

`Snapshot Release` and `Android Build` are reusable workflows without manual
triggers. `nightly.yml` remains the scheduled organizer.
