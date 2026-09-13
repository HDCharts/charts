---
name: hdc-concurrency
description: Coroutine ownership, Compose effects, animation cancellation, Flow exposure, live chart previews, delayed selection, and concurrency tests for HDCharts.
---

# HDCharts Concurrency

Use for coroutines, flows, animations, delayed selection, scroll or zoom
effects, live updates, and shared mutable state.

## Rules

- Keep mutable flows private and expose read-only `StateFlow` or another
  read-only state surface. Use `update` for atomic state transformations.
- Tie sample ViewModel work to its owner or the collecting `LaunchedEffect`.
- Use `rememberCoroutineScope` only for user-triggered UI work that must be
  launched from a composable callback. Use `LaunchedEffect` for work owned by
  composition and let its keys cancel and restart it.
- Cancel or replace restartable jobs explicitly so live-update and preview loops
  have one producer.
- Use structured concurrency for related chart animations. Child animations
  should be cancelled together when the data or render mode changes.
- Keep infinite preview loops cancellable with `isActive` and `delay`; use
  suspending work in common code.
- Delayed selection cleanup verifies the current selection before clearing it,
  preserving newer, replaced, and programmatic selections.
- When data changes, reset or preserve selection according to the chart's
  source-index contract and publish only valid indices.
- Catch coroutine failures only with an explicit policy. Preserve cancellation
  as a distinct lifecycle outcome from chart errors and completed operations.
- Keep state mutations on the owner controlling their lifetime. Expose
  read-only flows, selections, and animation state to callers.

## Tests

- Use Compose test synchronization such as `runOnIdle`, `waitForIdle`,
  `waitUntil`, and the test main clock for asynchronous rendering.
- Test loop starts, cancellation, data replacement during animation, delayed
  callbacks, selection-holder replacement, and disabled interaction.
- Advance animation time deterministically in tests.
