# API Compatibility

Workflows:
- `Pull Request API Compatibility` — `charts/.github/workflows/pull-request-api.yml` (pull-request orchestration)
- `API Compatibility` — `charts/.github/workflows/api-compatibility.yml` (reusable compatibility check)

## PR Compatibility Flow

```mermaid
 flowchart TD
   A["PR opened, synchronized, or reopened"] --> B["Prepare PR detects code changes"]
   B --> C{"Code or build changes?"}
   C -- No --> D["Docs-only no-op: PR API Compatibility skipped"]
   C -- Yes --> E["Run API Compatibility"]
   E --> F{"Breaking API change detected?"}
   F -- No --> G["Pass: API remains compatible"]
   F -- Yes --> H["Fail: API compatibility gate fails with workflow-run annotation and instructions"]
   H --> I["Developer runs ./gradlew apiCompatibilityUpdateBaseline locally"]
   I --> J["Developer commits the updated API-COMPATIBILITY-BASELINE.txt in the same PR"]
   J --> B
```

The `Pull Request API Compatibility` workflow runs for the `opened`,
`synchronize`, and `reopened` pull-request actions. Its `Prepare PR` job runs
the same code-change detector used by the core workflow and gates the
compatibility check on it. Documentation-only pull requests use the reusable
workflow's `Docs-only no-op` path and report `PR API Compatibility` as
skipped rather than running the Gradle compatibility check. When the
compatibility check does detect a binary or source-incompatible public API
change, it fails the API gate and posts a workflow-run annotation that points
the developer at `./gradlew apiCompatibilityUpdateBaseline`. The developer
runs that task locally, commits the regenerated
`API-COMPATIBILITY-BASELINE.txt` in the same pull request, and pushes again.
The next run compares the new commit against the updated baseline and passes.

## Release Audit Flow

`Release` automatically compares the pinned release source against the latest
published SemVer tag. The checked-in
`API-COMPATIBILITY-BASELINE.txt` may already have advanced after
breaking-change PRs merged the updated baseline file, so the release audit
intentionally uses the previous tag instead.

To reproduce the audit locally, run:

```bash
./gradlew apiCompatibilityCheck --no-daemon --continue -PapiCompatibilityBaselineRef=<previous-release-tag>
```

Example for a `2.3.0` release whose previous release is `2.2.0`:

```bash
./gradlew apiCompatibilityCheck --no-daemon --continue -PapiCompatibilityBaselineRef=2.2.0
```

## Acknowledging an Intentional Breaking Change

1. Push the breaking change. The API compatibility workflow will fail the PR
   and post a workflow-run annotation with instructions.
2. Run `./gradlew apiCompatibilityUpdateBaseline` locally. It writes the
   current HEAD SHA into `API-COMPATIBILITY-BASELINE.txt`.
3. Commit the regenerated `API-COMPATIBILITY-BASELINE.txt` in the same
   pull request and push. The next run uses the new baseline and passes.
