# Charts Release

Workflow: `charts/.github/workflows/release.yml`

```mermaid
flowchart TD
  A["workflow_dispatch"] --> B["Checkout + JDK"]
  B --> C["axion: resolve-release-version.sh"]
  C --> E["Gradle (Axion): verifyRelease"]
  E --> F{"Release tag exists?"}
  F -- Yes --> FX["Fail"]
  F -- No --> G["Checkout charts-docs"]
  G --> H{"Version exists in charts-docs registry?"}
  H -- No --> HX["Fail"]
  H -- Yes --> I["AWS OIDC creds"]
  I --> J{"Docs/static release published for version + commit?"}
  J -- No --> JX["Fail"]
  J -- Yes --> K["Check Maven/signing secrets"]
  K --> L{"Secrets complete?"}
  L -- No --> LX["Fail"]
  L -- Yes --> M["Gradle (Axion): createRelease (local tag)"]
  M --> N["Gradle (Charts): publishChartsModules (Maven Central)"]
  N --> O["Gradle (Axion): pushRelease (tag push)"]
  O --> P["Commit comment (success)"]
```

Before running this workflow, complete the
[Release Checklist](./release-checklist.md). In particular, the docs promotion
pull request must be merged because this workflow requires the release version
in the charts-docs registry.

The workflow resolves the release version from Axion. If the release tag already
exists, the workflow fails before publishing.

The workflow also requires `Docs Release Publish` to have published versioned
docs/static assets for the same release version and charts commit before Maven
publishing can proceed.
