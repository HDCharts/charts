# Contributing to charts

Contributions are welcome.

There are many ways to contribute, including feature work, bug fixes, performance improvements, test coverage, docs updates, and tooling/CI refinements.

## Requirements
- Android Studio Panda 2 | 2025.3.2

## Project hierarchy
<pre>
HDCharts
├── charts
│   ├── core
│   │   └── :charts-core
│   ├── umbrella
│   │   └── :charts
│   ├── modules
│   │   ├── :charts-line
│   │   ├── :charts-pie
│   │   ├── :charts-bar
│   │   ├── :charts-radar
│   │   ├── :charts-stacked-bar
│   │   └── :charts-stacked-area
│   ├── BOM
│   │   └── :charts-bom
│   ├── shared demo resources
│   │   └── :charts-demo-shared
│   └── apps
│       ├── :app (common)
│       ├── :androidApp
│       └── :iosApp
├── charts-playground (<a href="https://github.com/HDCharts/charts-playground">https://github.com/HDCharts/charts-playground</a>)
├── charts-docs (<a href="https://github.com/HDCharts/charts-docs">https://github.com/HDCharts/charts-docs</a>)
└── charts-gif-recorder (<a href="https://github.com/hdcodedev/compose-gif-recorder">https://github.com/hdcodedev/compose-gif-recorder</a>)
</pre>

## Report issues
Open an issue: https://github.com/HDCharts/charts/issues

## Technologies
| Module | Technologies / Languages |
| --- | --- |
| `:charts-*` | Kotlin Multiplatform, Compose Multiplatform (Android/iOS/JVM/JS) |
| `:charts-bom` | Gradle Java Platform (BOM), Maven Publishing |
| `:app` | Kotlin Multiplatform, Compose Multiplatform (Android/iOS/JVM/JS) |
| `:androidApp` | Kotlin, Android, Jetpack Compose |
| `:iosApp` | Swift, SwiftUI, Xcode |
| `charts-playground` | Kotlin Multiplatform (JVM/JS), Compose Multiplatform, Ktor |
| `charts-docs` | Next.js, React, TypeScript, MDX |
| `charts-gif-recorder` | Kotlin (JVM), Gradle Plugin Development, Android/Jetpack Compose, KSP |

## Test types in this project
- Kotlin/JVM test runs for core and chart modules (`jvmTest` executes shared `commonTest` suites).
- Compose UI tests for chart modules (`:charts-*`, KMP Compose UI test APIs via `commonTest`).
- Android screenshot tests (baseline image validation in `:androidApp` `screenshotTest`).
- Android instrumented GIF recording scenarios via `compose-gif-recorder` in `:androidApp` (device/emulator workflow).
- Smoke compile checks (module-level compile validation).
