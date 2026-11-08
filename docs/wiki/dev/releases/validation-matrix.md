---
title: Validation Matrix
order: 2
---

# Validation Matrix

Each pull-request test job checks the same merge commit prepared by `Prepare PR`. `Prepare PR`
also chooses which jobs run and which modules they test; see [Check selection](#check-selection).
Workflow: [`test.yml`](https://github.com/HDCharts/charts/blob/main/.github/workflows/test.yml)

| CI job | Gradle entry task | Tests included | Runs on |
| --- | --- | --- | --- |
| JVM Tests | `./gradlew ciTestJvm` | `jvmTest` for the selected library modules and `:sample-shared`, `:sample-api`, `:app`. `buildSrc` unit tests run with full validation. | Ubuntu, Zulu JDK 17. |
| Android Instrumented Tests 1/2 and 2/2 | `./gradlew ciTestAndroidInstrumented -PandroidTestShard=<1\|2>` | `connectedAndroidTest` for the selected chart library modules except `charts-core` and `charts`, split across two emulator jobs. A shard with no selected module skips the emulator. | Ubuntu; API 35 `aosp_atd` x86_64 Nexus 6 emulator at 280 dpi with KVM. |
| Screenshot Tests | `./gradlew ciTestScreenshot` | `:androidApp:validateDebugScreenshotTest` for the selected screenshot classes. | Ubuntu, Zulu JDK 17. |
| Wasm Tests | `./gradlew ciTestWeb` | `wasmJsTest` for the selected chart library modules. | Ubuntu, Kotlin/Wasm browser tests. |
| iOS Tests | `./gradlew ciTestIos` | `iosSimulatorArm64Test` for the selected chart library modules. | macOS, ARM64 iOS Simulator. |

The `ciTest*` tasks are CI entry points. Without a selection they run the same modules as the
platform-specific `chartsTest*` tasks in the root build.

## Check selection

`Prepare PR` runs `.github/scripts/ci-impact.py` on the files the pull request changes. The script
maps each file to the Gradle project that owns it, using `settings.gradle.kts` and the project
dependencies in the build scripts, and writes its decision to the job summary.

| Change | Checks |
| --- | --- |
| Documentation, release notes, agent guidance, repository metadata, or KDoc only | None |
| Implementation or build file of a module | Tests of that module and of every project that depends on it, lint, compile, assemble, and API compatibility for library modules |
| Test sources of a module | That module's tests and lint |
| `charts-core` implementation | Tests of every chart module and every sample that uses one, and every screenshot class |
| A screenshot test class | That screenshot class and lint |
| A screenshot reference image | That screenshot class only |
| Shared screenshot code, `:sample-shared`, the `:charts` umbrella, or `:androidApp` resources and build file | Every screenshot class |
| `.editorconfig` | Lint |
| `API-COMPATIBILITY-BREAKS.txt` | API compatibility |
| Root build files, `buildSrc`, `gradle/`, `.github/`, `scripts/ci-has-code-changes.sh`, or a path outside every Gradle project | Full validation |

The Wasm, iOS, and Android jobs start only when a selected module is a library module: `:charts`
and everything it depends on. Each Android shard runs a Gradle dry run first and starts the
emulator only when the shard holds a selected module with device tests.

Screenshot classes follow their imports. A class that imports `io.github.hdcharts.line` runs when
`charts-line` or one of its dependencies changes. A class that imports app code, such as the wiki
examples, runs for every library change. `:sample-shared` and the umbrella depend on every chart,
so only a direct change to them runs every screenshot class.

Full validation also runs when the script cannot list the changed files or fails, and the job
summary says so. `./gradlew ciCheckImpactGraph` in the Lint job fails when the script misses a
Gradle project, project dependency, or `ChartsModules.library` module, so a dependency the script
cannot read fails the pull request that adds it.

## Module coverage

The test jobs do not cover the same modules. The `chartsTest*` tasks in `build.gradle.kts` take
their module lists from `ChartsModules` in `buildSrc`:

| Task | Modules |
| --- | --- |
| `chartsTestJvm` | `library` + `sampleTested` |
| `chartsTestWasm` | `library` |
| `chartsTestIos` | `library` |
| `chartsTestAndroidInstrumented` | `library` without `:charts-core` and `:charts` |

`ChartsModules.kt` is where those lists change. `modulesWithoutDeviceTests` in
`build.gradle.kts` holds the exceptions — `:charts-core` and `:charts` have no device tests — so a
new module without them needs an entry there. A new chart module otherwise joins `library` and
picks up every job above, with no edit to this page. `ci-impact.py` finds it through
`settings.gradle.kts` and the dependencies of the `:charts` umbrella.

The three sample modules are multiplatform and hold `commonTest` sources, but only `chartsTestJvm`
wires them. Their shared tests run in the JVM job and in no other job, so a platform difference in
the sample code reaches CI on the JVM alone.

## Local Validation Selection

Use the smallest applicable command:

| Scope | Command |
| --- | --- |
| Checks CI selects for some files | `python3 .github/scripts/ci-impact.py --files <path>...` |
| CI tests for selected modules | `./gradlew ciTestJvm -PciTestModules=:charts-line,:charts` |
| CI screenshot validation for selected classes | `./gradlew ciTestScreenshot -PciScreenshotTests=io.github.hdcharts.app.screenshot.LineChartScreenshotTestKt` |
| CI selection logic | `python3 -B .github/scripts/tests/test_ci_impact.py` |
| One chart module | `./gradlew :charts-<module>:jvmTest` |
| Cross-module or `charts-core` | `./gradlew chartsTestJvm` |
| Kotlin or build logic | `./gradlew ktlintCheck` |
| Compile gate | `./gradlew ciCompile` |
| Repository checks | `./gradlew chartsCheck` |
| Public API compatibility | `./gradlew apiCompatibilityCheck` |
| Compose or screenshots | `./gradlew :androidApp:validateDebugScreenshotTest` |
| Intentional screenshot updates | `./gradlew updateScreenshots` |

`chartsCheck` is local only and is never invoked by a workflow. Pull requests are validated by
the narrower `ciAssemble`, `ciCompile`, `ktlintCheck`, and `ciTest*` tasks above, which are not
equivalent to the root `build` that `chartsCheck` depends on. Whether `chartsCheck` becomes a
merge gate is undecided.

CI owns `chartsTestAndroidInstrumented`, `chartsTestWasm`, `chartsTestIos`, and
`validateDocsGifBaselines` unless explicitly requested locally.
