---
name: hdc-kotlin
description: Kotlin and Kotlin Multiplatform source sets, public API design, immutable chart models, visibility, dependencies, and formatting for HDCharts.
---

# HDCharts Kotlin

Use for Kotlin, Kotlin Multiplatform, public API, and implementation decisions
in the library and sample applications.

## Rules

- Put portable library and sample production code in `commonMain`; put portable
  tests in `commonTest`.
- Keep platform APIs out of common code. Use the existing `androidMain`,
  `jvmMain`, `appleMain`, and `webMain` source sets when platform behavior is
  genuinely required.
- Prefer common APIs over `expect`/`actual`; use platform source sets for
  behavior that requires platform code.
- Prefer immutable data classes, immutable collection snapshots, exhaustive
  sealed hierarchies, and non-null values unless absence has real meaning.
- Preserve the public numeric contract: migrated chart data uses `Double`, and
  values must be validated before they reach internal render models.
- Keep public models, styles, selections, formatters, and composables in their
  published module; keep adapters, geometry, validation helpers, and renderer
  state under `internal` packages unless they are intentionally public API.
- Use `internal` visibility for implementation details and keep `@Composable`
  entry points small enough to validate inputs before renderer conversion.
- Add dependencies through `gradle/libs.versions.toml` and use type-safe
  `projects.*` accessors for project dependencies.
- Follow the repository Kotlin style and run `./gradlew ktlintCheck`; use
  `./gradlew ktlintFormat` for formatter-owned violations.
- Add compatibility overloads, duplicate models, or platform forks only for a
  concrete API or runtime requirement.
