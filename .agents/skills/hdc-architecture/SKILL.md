---
name: hdc-architecture
description: HDCharts module boundaries, public chart API layering, validation, internal render models, sample integration, and dependency direction.
---

# HDCharts Architecture

Use for module boundaries, public contracts, validation-to-rendering data flow,
and sample integration.

## Module Boundaries

- `charts-core` owns shared models, styles, selection, formatting, validation
  foundations, Compose primitives, and rendering utilities.
- Chart modules own public entry points, styles, validation, geometry,
  interaction, and renderers.
- `charts` owns umbrella re-exports and source aggregation. `charts-bom` owns
  published version alignment.
- `sample/shared` and `sample/app` own fixtures, view models, navigation, and
  presentation. `sample/androidApp` owns Android wiring and screenshot tests.

## Data Flow

```text
Public chart models and styles
        -> chart validation
        -> internal render models and adapters
        -> Compose layout, interaction, and drawing
        -> public selection callbacks and visible readouts
```

- Validate at the public chart boundary before conversion.
- Keep public models, styles, formatters, and selections separate from internal
  renderer data.
- Preserve source indices through dense or compact rendering.
- Keep formatting and fallback policies at the owning boundary; renderers use
  prepared values and geometry.
- Preserve the caller `Modifier` and documented error UI on validation errors.

## Dependency Direction

- Dependencies flow from chart modules to `charts-core`, from `charts` to chart
  modules, and from samples to published APIs.
- Keep platform implementations behind source-set boundaries and common APIs
  platform-neutral.
- Coordinate public API changes across implementation, tests, exports,
  compatibility, migration guidance, and release notes.
