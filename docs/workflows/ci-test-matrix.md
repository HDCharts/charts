# CI Test Matrix

Each PR test job checks the same immutable merge revision prepared by the PR workflow. The four jobs below run only when the PR contains code or build changes.

| CI job | Gradle entry task | Tests included | Runs on |
| --- | --- | --- | --- |
| JVM Tests | `./gradlew ciTestJvm` | `jvmTest` for every chart library module. | GitHub-hosted `ubuntu-latest` runner with Zulu JDK 17. |
| Android Tests | `./gradlew ciTestAndroid` | Android screenshot validation plus `connectedAndroidTest` for `charts`, `charts-bar`, `charts-line`, `charts-pie`, `charts-radar`, `charts-stacked-area`, `charts-stacked-bar`, and `charts-histogram`. | GitHub-hosted `ubuntu-latest` runner; API 35, `google_apis`, x86_64 Nexus 6 emulator with KVM acceleration. |
| Wasm Tests | `./gradlew ciTestWeb` | `wasmJsTest` for every chart library module. | GitHub-hosted `ubuntu-latest` runner in the Kotlin/Wasm browser test environment. |
| iOS Tests | `./gradlew ciTestIos` | `iosSimulatorArm64Test` for every chart library module. | GitHub-hosted `macos-15` Apple Silicon runner using the ARM64 iOS Simulator. |

The `ciTest*` tasks are CI entry points. They delegate to the platform-specific `chartsTest*` tasks defined in the root build.
