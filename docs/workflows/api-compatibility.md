# API Compatibility

Workflows:
- `Pull Request API Compatibility` — `charts/.github/workflows/pull-request-api.yml` (pull-request orchestration)
- `API Compatibility` — `charts/.github/workflows/api-compatibility.yml` (reusable compatibility check)
- `Set API Baseline` — `charts/.github/workflows/set-api-baseline.yml` (post-merge baseline update)

## PR Compatibility Flow

```mermaid
flowchart TD
  A["PR opened, synchronized, or reopened"] --> B["Prepare PR detects code changes"]
  B --> C{"Code or build changes?"}
  C -- No --> D["Docs-only no-op: PR API Compatibility skipped"]
  C -- Yes --> E["Run API Compatibility"]
  E --> F{"Breaking API change detected?"}
  F -- No --> G["Pass: API remains compatible"]
  F -- Yes --> H{"PR has breaking-change label?"}
  H -- No --> I["Fail: add breaking-change label or restore compatibility"]
  H -- Yes --> J["Pass: breaking change is explicitly acknowledged"]

  I --> B
```

The `Pull Request API Compatibility` workflow runs for the `opened`,
`synchronize`, and `reopened` pull-request actions. Its `Prepare PR` job runs
the same code-change detector used by the core workflow and gates the
compatibility check on it. Documentation-only pull requests use the reusable
workflow's `Docs-only no-op` path and report `PR API Compatibility` as
skipped rather than running the Gradle compatibility check. The
`breaking-change` label is evaluated as policy input on pull requests that do
run the check; it allows an intentional API break but does not trigger a
separate workflow run.

The compatibility job fetches current PR labels from the GitHub API on each
attempt rather than using the original event's label snapshot. After adding
`breaking-change` for an intentional incompatibility, use **Re-run failed jobs**
or **Re-run all jobs** to evaluate it without pushing another commit.

If a breaking change is acknowledged with the `breaking-change` label and
merged, the post-merge baseline update flow below runs automatically.

## Release Audit Flow

`Release` automatically compares the pinned release source against the latest
published SemVer tag. The checked-in
`.github/api-compatibility-baseline.txt` may already have advanced after
accepted breaking-change PRs merge, so the release audit intentionally uses the
previous tag instead.

To reproduce the audit locally, run:

```bash
./gradlew apiCompatibilityCheck --no-daemon --continue -PapiCompatibilityBaselineRef=<previous-release-tag>
```

Example for a `2.3.0` release whose previous release is `2.2.0`:

```bash
./gradlew apiCompatibilityCheck --no-daemon --continue -PapiCompatibilityBaselineRef=2.2.0
```

## Post-Merge Baseline Update Flow

```mermaid
flowchart TD
  A["PR with the breaking-change label is merged to main"] --> B["Set API Baseline runs automatically"]
  B --> C["Use the merge commit as the immutable baseline"]
  C --> D["Workflow creates baseline-update PR"]
  D --> E["Review and merge baseline-update PR"]
  E --> F["Future API compatibility checks use the new baseline"]
```
