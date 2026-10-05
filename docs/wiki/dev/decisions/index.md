---
title: Decisions
order: 0
---

# Decision Log

This page records technical decisions for the project, such as changes to CI, the build, the
architecture, or the public API. Each row keeps the state before the change, the expected effect,
and, where useful, an issue to check whether it paid off. The pull request holds the details.

## Decisions

| Date | Decision | PR | Before | Effect | Revisit | Outcome |
|---|---|---|---|---|---|---|
| 2026-10-05 | Run the Android device tests on the `aosp_atd` emulator image at 280 dpi, with drawing turned back on. | [#667](https://github.com/HDCharts/charts/pull/667) | Emulator job 13m14s–13m35s (run 37209512033). | 5m10s–6m34s over 8 runs. | — | Kept. |
| 2026-10-05 | Save the Linux and macOS Gradle caches on `main` when build files change, from a job that builds the CI targets. The iOS cache adds `~/.konan`. Other workflows on `main` restore them read-only, except release jobs (next row). The playground job keeps its own cache. | [#668](https://github.com/HDCharts/charts/pull/668) | iOS job ~14 min with no cache. 6.9 GB of macOS caches that only their own PR could read. | Expected ~10 min or less, and one shared macOS cache, for PRs that keep `main`'s build files. | [#669](https://github.com/HDCharts/charts/issues/669) | — |
| 2026-10-05 | Release jobs start with no Gradle cache, so a release builds from fresh downloads and never from a stale cache. Snapshot jobs keep using the cache. | [#668](https://github.com/HDCharts/charts/pull/668) | — | Release jobs download every dependency on each run. | — | — |
