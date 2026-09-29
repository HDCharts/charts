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
| JVM Tests | `./gradlew ciTestJvm` | `jvmTest` for every chart library module. | Ubuntu, Zulu JDK 17. |
| Android Tests | `./gradlew ciTestAndroid` | Android screenshot validation, plus `connectedAndroidTest` for every chart library module except `charts-core`. | Ubuntu; API 35 `google_apis` x86_64 Nexus 6 emulator with KVM. |
| Wasm Tests | `./gradlew ciTestWeb` | `wasmJsTest` for every chart library module. | Ubuntu, Kotlin/Wasm browser tests. |
| iOS Tests | `./gradlew ciTestIos` | `iosSimulatorArm64Test` for every chart library module. | macOS, ARM64 iOS Simulator. |

The `ciTest*` tasks are CI entry points. They delegate to the platform-specific `chartsTest*`
tasks in the root build.

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

CI owns `chartsTestAndroid`, `chartsTestWasm`, `chartsTestIos`, and
`validateDocsGifBaselines` unless explicitly requested locally.
