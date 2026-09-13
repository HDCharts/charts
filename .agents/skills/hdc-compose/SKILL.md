---
name: hdc-compose
description: Compose Multiplatform chart composables, state hoisting, drawing, interaction, previews, semantics, and screenshot behavior for HDCharts.
---

# HDCharts Compose

Use for chart composables, Compose rendering, interaction, animation wiring,
sample UI, previews, and screenshot-covered changes.

## Rules

- Keep public composables responsible for validation, selection wiring, and
  conversion to internal render data. Keep drawing and geometry internal.
- Every reusable public composable accepts `modifier: Modifier = Modifier` and
  applies it to the outer chart or error container. Preserve the caller modifier
  on validation-error paths too.
- Hoist durable selection through `ChartSelection` or the existing chart
  selection factory. Programmatic selection and user interaction must follow
  the documented `interactionEnabled` behavior.
- Key `remember` values with every input that can change them.
- Use `LaunchedEffect` for lifecycle-bound selection reset, scroll correction,
  and animation work. Key effects by the data, style, mode, or interaction input
  they actually observe.
- Render validated and prepared values. Keep validation, aggregation,
  source-index mapping, numeric fallback, and formatting outside drawing lambdas.
- Keep density conversions and geometry calculations explicit. Hit testing must
  use the same drawn bounds, clipping, inset, and coordinate system as the
  visible chart.
- When interaction is disabled, disable gestures, scrolling, zoom, fit/expand
  controls, and delayed user-selection cleanup together.
- Use stable test tags and semantics already defined in the chart internals;
  preserve meaningful title, error, scroll, and selection semantics.
- Keep previews in the module's preview files and use deterministic data,
  selection, animation settings, and styles for screenshot-covered states.
- For intentional visual changes, update screenshot references and run
  `./gradlew :androidApp:validateDebugScreenshotTest`.
