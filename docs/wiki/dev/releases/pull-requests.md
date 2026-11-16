---
title: Pull Requests
order: 1
---

# Pull Requests

Pull requests run independent workflows for core checks, API compatibility, and opt-in GIF
validation. Each checks the event's immutable merge commit (`github.sha`) and cancels its own older
run when a new event starts it.

| Workflow | Purpose |
|---|---|
| [`pull-request.yml`](https://github.com/HDCharts/charts/blob/main/.github/workflows/pull-request.yml) | Selects core checks, runs CI-script self-tests, and reports the core gate |
| [`pull-request-api.yml`](https://github.com/HDCharts/charts/blob/main/.github/workflows/pull-request-api.yml) | Selects and runs the API check, then reports its gate |
| [`pull-request-gif-validation.yml`](https://github.com/HDCharts/charts/blob/main/.github/workflows/pull-request-gif-validation.yml) | Checks the GIF-validation label and reports its gate |

## Check selection

`Prepare PR` uses [.github/scripts/ci-impact.py](https://github.com/HDCharts/charts/blob/main/.github/scripts/ci-impact.py) to choose checks and modules from the changed files.
Its job summary records the decision. See [Validation Matrix](validation-matrix.md) for the rules,
coverage, and local commands.

Changes limited to documentation, release notes, agent guidance, or repository metadata select no
build or platform tests. CI-script self-tests still run. An unselected reusable check runs its
successful `No-op` job. GitHub also shows the inactive path as skipped.

## Merge gates

The `protect main` ruleset requires these final checks:

| Gate | Pass condition |
|---|---|
| `PR Core Checks` | `Prepare PR` and all selected core checks succeed |
| `PR API Compatibility` | Preparation succeeds and any selected API check succeeds |
| `PR GIF Baseline Validation` | Label lookup succeeds and GIF validation succeeds when opted in |

Each gate reports even when its underlying checks are unselected. [API
Compatibility](api-compatibility.md) explains the API baseline and intentional breaks.

## GIF validation

Add `run-gif-validation` to record and compare the docs GIF scenarios. Every label change starts a
new GIF workflow run, so adding an unrelated label during validation restarts it.

The recording configuration lives in [validate-gifs.yml](https://github.com/HDCharts/charts/blob/main/.github/workflows/validate-gifs.yml) and [sample/androidApp/build.gradle.kts](https://github.com/HDCharts/charts/blob/main/sample/androidApp/build.gradle.kts).
CI owns device-based recording unless local execution is explicitly requested.

## Checkout depth

Jobs that select changes or resolve a release version need full history. Build and test jobs check
out only the merge commit and may use Axion's fallback snapshot version. [Versioning](versioning.md)
describes version resolution; each workflow owns its checkout configuration.

## Fork security

Pull-request workflows execute contributor code with read-only permissions on `pull_request`.
Keep build and test steps on that event. `pull_request_target` has write access.

## Troubleshooting

- **Merge is blocked:** compare the required names in `protect main` with the reported checks.
- **A test fails:** open its `PR Test` job and download the HTML or XML test reports.
- **API compatibility fails:** follow [Acknowledging a breaking
  change](api-compatibility.md#acknowledging-a-breaking-change). Acknowledgments cover intended API
  breaks; other Gradle errors still need to be fixed.
