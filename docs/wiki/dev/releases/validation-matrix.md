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
| iOS Tests | `./gradlew ciTestIos` | `iosSimulatorArm64Test` for the selected chart library modules. | macOS, ARM64 iOS Simulator. |

The `ciTest*` tasks are CI entry points. Without a selection they run the same modules as the
platform-specific `chartsTest*` tasks in the root build.

Wasm compilation remains in `ciCompile`, and `ciAssemble` builds the sample web distribution.
Full Wasm browser tests run manually; they are outside the PR checks, including full-validation
fallbacks. Compilation and packaging do not verify browser rendering or interaction.

## Check selection

`Prepare PR` runs [.github/scripts/ci-impact.py](https://github.com/HDCharts/charts/blob/main/.github/scripts/ci-impact.py) on the files the pull request changes. The script
maps each file to the Gradle project that owns it, using [settings.gradle.kts](https://github.com/HDCharts/charts/blob/main/settings.gradle.kts) and the project
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
| [API-COMPATIBILITY-BREAKS.txt](https://github.com/HDCharts/charts/blob/main/API-COMPATIBILITY-BREAKS.txt) | API compatibility |
| Root build files, `buildSrc`, `gradle/`, `.github/`, [scripts/ci-has-code-changes.sh](https://github.com/HDCharts/charts/blob/main/scripts/ci-has-code-changes.sh), or a path outside every Gradle project | Full validation |

The iOS and Android jobs start only when a selected module is a library module: `:charts`
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

The test jobs do not cover the same modules. The `chartsTest*` tasks in [build.gradle.kts](https://github.com/HDCharts/charts/blob/main/build.gradle.kts) take
their module lists from `ChartsModules` in `buildSrc`:

| Task | Modules |
| --- | --- |
| `chartsTestJvm` | `library` + `sampleTested` |
| `chartsTestWasm` (manual) | `library` |
| `chartsTestIos` | `library` |
| `chartsTestAndroidInstrumented` | `library` without `:charts-core` and `:charts` |

[ChartsModules.kt](https://github.com/HDCharts/charts/blob/main/buildSrc/src/main/kotlin/ChartsModules.kt) is where those lists change. `modulesWithoutDeviceTests` in
[build.gradle.kts](https://github.com/HDCharts/charts/blob/main/build.gradle.kts) holds the exceptions — `:charts-core` and `:charts` have no device tests — so a
new module without them needs an entry there. A new chart module otherwise joins `library` and
picks up every job above, with no edit to this page. [ci-impact.py](https://github.com/HDCharts/charts/blob/main/.github/scripts/ci-impact.py) finds it through
[settings.gradle.kts](https://github.com/HDCharts/charts/blob/main/settings.gradle.kts) and the dependencies of the `:charts` umbrella.

The `sampleTested` modules are multiplatform and hold `commonTest` sources, but only `chartsTestJvm`
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
| Intentional screenshot updates | Follow [Screenshot baselines](#screenshot-baselines). |

`chartsCheck` is a local aggregate that includes the root `build`. PRs use the narrower
`ciAssemble`, `ciCompile`, `ktlintCheck`, and `ciTest*` tasks above.

CI owns `chartsTestAndroidInstrumented`, `chartsTestIos`, and `validateDocsGifBaselines` unless
explicitly requested locally.

## Screenshot baselines

Update references only for an intentional visual change:

1. Open the affected screenshot preview in Android Studio and use its reference-image update
   action. Keep the update scoped to that preview or test.
2. Inspect every changed image and the file diff. Keep unrelated references unchanged.
3. Run the selected-class screenshot validation command from the local-validation table above.

For command-line updates, confirm the installed screenshot plugin supports the requested filter
and honors it. If it cannot limit the update, use the preview-specific IDE action.

## Manual Wasm validation

Run `./gradlew chartsTestWasm` for the full library browser suite, or
`./gradlew ciTestWeb -PciTestModules=:charts-line` for selected library modules.
The Wasm test targets, shared tests, and browser configuration remain available.

Before a release, or when changing Kotlin, Compose, or browser-specific code, explicitly run
this suite and check rendering and interaction in the sample web app. These checks are manual
and do not run automatically in the PR workflow.
