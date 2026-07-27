# Charts Release

Workflow: `charts/.github/workflows/release.yml`

```mermaid
flowchart TD
  A["workflow_dispatch on main"] --> B["Pin source SHA + resolve version"]
  B --> C["Verify release readiness + audit API against previous tag"]
  C --> D["Approve Release environment"]
  D --> E["Publish API + demo static assets"]
  E --> F["Publish Maven + push tag"]
  F --> G["Build and publish release Android APK"]
  G --> H["Sync notes + promote charts-docs main"]
  H --> I["Verify public docs deployment"]
  I --> J["Commit release announcement"]
```

Before running this workflow, complete the
[Release Checklist](./release-checklist.md). The workflow coordinates docs
promotion and static publication itself from the pinned charts commit.

The workflow resolves the release version from Axion. If the release tag already
exists, the workflow fails before publishing.

`Release` is the only manually runnable publishing workflow. Its optional
`replace_static_assets` input replaces pre-release API and demo objects for the
resolved version; leave it disabled for normal releases. The workflow must pass
the protected `Release Approval` environment before any production publication
begins.
