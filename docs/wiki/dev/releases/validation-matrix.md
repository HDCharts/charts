---
title: Validation Matrix
order: 2
---

# Validation Matrix

Each pull-request test job checks the same merge commit prepared by `Prepare PR`. The jobs run
only when the pull request changes code or build files. Workflow:
[`test.yml`](https://github.com/HDCharts/charts/blob/main/.github/workflows/test.yml)

| CI job | Gradle entry task | Tests included | Runs on |
| --- | --- | --- | --- |
| JVM Tests | `./gradlew ciTestJvm` | `jvmTest` for the library modules, plus `:sample-shared` and `:app`. | Ubuntu, Zulu JDK 17. |
| Android Instrumented Tests 1/2 and 2/2 | `./gradlew ciTestAndroidInstrumented -PandroidTestShard=<1\|2>` | `connectedAndroidTest` for every chart library module except `charts-core` and `charts`, split across two emulator jobs. | Ubuntu; API 35 `aosp_atd` x86_64 Nexus 6 emulator at 280 dpi with KVM. |
| Screenshot Tests | `./gradlew ciTestScreenshot` | `:androidApp:validateDebugScreenshotTest`. | Ubuntu, Zulu JDK 17. |
| Wasm Tests 1/2 and 2/2 | `./gradlew ciTestWeb -PwasmTestShard=<1\|2>` | `wasmJsTest` for every chart library module, split across two jobs. | Ubuntu, Kotlin/Wasm browser tests in headless Chrome. |
| iOS Tests | `./gradlew ciTestIos` | `iosSimulatorArm64Test` for every chart library module. | macOS, ARM64 iOS Simulator. |

The `ciTest*` tasks are CI entry points. They delegate to the platform-specific `chartsTest*`
tasks in the root build.

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
picks up every job above, with no edit to this page.

The two sample modules are multiplatform and hold `commonTest` sources, but only `chartsTestJvm`
wires them. Their shared tests run in the JVM job and in no other job, so a platform difference in
the sample code reaches CI on the JVM alone.

## Local Validation Selection

Use the smallest applicable command:

| Scope | Command |
| --- | --- |
| One chart module | `./gradlew :charts-<module>:jvmTest` |
| Cross-module or `charts-core` | `./gradlew chartsTestJvm` |
| Kotlin or build logic | `./gradlew ktlintCheck` |
| Compile gate | `./gradlew ciCompile` |
| Repository checks | `./gradlew chartsCheck` |
| Public API compatibility | `./gradlew apiCompatibilityCheck` |
| Playground compile (library or `sample/shared` changes) | `../charts-playground/gradlew -p ../charts-playground -DchartsLocalPath="$PWD" ciCompile` |
| Compose or screenshots | `./gradlew :androidApp:validateDebugScreenshotTest` |
| Intentional screenshot updates | `./gradlew updateScreenshots` |

The playground command expects `charts-playground` next to this checkout. From a git worktree,
replace both `../charts-playground` paths with the path to your playground checkout.

Two rows are local only:

- **`chartsCheck`** is never invoked by a workflow. Pull requests are validated by the narrower
  `ciAssemble`, `ciCompile`, `ktlintCheck`, and `ciTest*` tasks above, which are not equivalent to
  the root `build` that `chartsCheck` depends on. Whether `chartsCheck` becomes a merge gate is
  undecided.
- **The playground compile** has no pull-request gate either. The playground is built during
  snapshot and release publishing, and the pull request template asks contributors to run the
  command above when library or sample code changes.

CI owns `chartsTestAndroidInstrumented`, `chartsTestWasm`, `chartsTestIos`, and
`validateDocsGifBaselines` unless explicitly requested locally.
