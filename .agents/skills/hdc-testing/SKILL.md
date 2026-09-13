---
name: hdc-testing
description: HDCharts test design and implementation for Kotlin models, Compose interaction, rendering behavior, API contracts, platforms, and screenshots.
---

# HDCharts Testing

Use when writing or changing tests. Test the behavior owned by the changed
layer and assert the public contract.

## Test Placement

- Put portable library tests in each module's `commonTest`.
- Keep model, validation, formatting, geometry, transformation, and Compose
  behavior tests with the owning module.
- Keep Android screenshots in `sample/androidApp/src/screenshotTest`.
- Use platform-specific tests for platform-only behavior.

## Test Design

- Name tests after the observable behavior and condition.
- Prefer small Arrange, Act, Assert tests with one primary behavior.
- Use deterministic fixtures and fakes.
- Keep shared fixtures in module test support and chart fixtures local.
- Assert public output, callbacks, visible state, and errors.
- Test at the smallest layer that owns the behavior; add a Compose test across
  validation, state, rendering, or interaction boundaries.

## Data and Model Tests

Cover validation and transformation boundaries for:

- Empty and undersized inputs.
- Non-finite values and invalid ranges.
- Mismatched categories, series, and palettes.
- Invalid style dimensions and chart-specific constraints.
- Zero, negative, constant, all-zero, dense, boundary, and large finite values.
- Source-index preservation when data is compacted, grouped, scrolled, or
  zoomed.

## Compose Tests

- Use the existing Compose Multiplatform test APIs and stable `TestTags`.
- Cover initial rendering, validation errors, titles, formatters, selection
  callbacks, selection reset after data changes, and interaction-disabled
  behavior.
- Exercise chart-specific scrolling, zoom/fit controls, dense-data mapping,
  boundary hit testing, and modifier forwarding where applicable.
- Mutate state with `runOnIdle` and synchronize with `waitForIdle` or
  `waitUntil` for asynchronous rendering.
- Verify user-facing behavior with semantics and visible text. Test geometry
  through bounds and pointer input when geometry is part of the contract.
- Control animation with `mainClock`.

## Coroutine and Screenshot Tests

- Test cancellation, delayed selection, repeated interactions, data replacement
  during animation, and stale callbacks when concurrency affects behavior.
- Use stable screenshot fixtures, explicit selection, and controlled animation
  settings.
- Include default, customized, selected, dense-data, invalid-data, and
  responsive states when those states are part of the change.
- Update screenshot baselines for intentional visual changes and pair them with
  assertions that explain the behavior.
