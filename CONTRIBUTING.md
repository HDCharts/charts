# Contributing to charts

Contributions are welcome.

There are many ways to contribute, including feature work, bug fixes, performance improvements, test coverage, docs updates, and tooling/CI refinements.

## Requirements

Use Android Studio with support for the Android Gradle Plugin version in
[gradle/libs.versions.toml](gradle/libs.versions.toml).

## Project layout

| Location | Purpose |
|---|---|
| `charts-core/` | Shared models, validation, styles, and rendering helpers |
| `charts-*/` | Chart modules |
| `charts/` | Umbrella artifact |
| `charts-bom/` | Bill of Materials |
| `sample/` | Shared sample data, API clients, and platform apps |
| `docs/wiki/` | Chart guides and internal developer documentation |
| `release-notes/` | Versioned release notes and migrations |

[settings.gradle.kts](settings.gradle.kts) declares Gradle projects and their directories.
[ChartsModules.kt](buildSrc/src/main/kotlin/ChartsModules.kt) groups modules for publishing and
validation.

Related repositories:

- [charts-docs](https://github.com/HDCharts/charts-docs): documentation site.
- [compose-gif-recorder](https://github.com/hdcodedev/compose-gif-recorder): GIF recording tools.

## Report issues
Open an issue: https://github.com/HDCharts/charts/issues

## AI-assisted contributions
See [AI-Assisted Contributions Policy](AI_POLICY.md).

## Technical decisions
See why things are built the way they are: https://charts.hdcode.dev/dev/decisions

## Technologies
| Module | Technologies / Languages |
| --- | --- |
| `:charts-*` | Kotlin Multiplatform, Compose Multiplatform (Android/iOS/JVM/Wasm) |
| `:charts-bom` | Gradle Java Platform (BOM), Maven Publishing |
| `:app` | Kotlin Multiplatform, Compose Multiplatform (Android/iOS/JVM/Wasm) |
| `:sample-api` | Kotlin Multiplatform, Ktor, kotlinx.serialization, Koin |
| `:androidApp` | Kotlin, Android, Jetpack Compose |
| `:iosApp` | Swift, SwiftUI, Xcode |
| `charts-docs` | Next.js, React, TypeScript, MDX |
| `charts-gif-recorder` | Kotlin (JVM), Gradle Plugin Development, Android/Jetpack Compose, KSP |

## Test types in this project
- Kotlin/JVM test runs for core and chart modules (`jvmTest` executes shared `commonTest` suites).
- Compose UI tests for chart modules (`:charts-*`, KMP Compose UI test APIs via `commonTest`).
- Android instrumented tests for chart modules (`connectedAndroidTest` runs the shared `commonTest` suites on an emulator).
- Android screenshot tests (baseline image validation in `:androidApp` `screenshotTest`).
- Android instrumented GIF recording scenarios via `compose-gif-recorder` in `:androidApp` (device/emulator workflow).
- Smoke compile checks (module-level compile validation).
