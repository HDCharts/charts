---
title: Pipeline and Stage Ownership
order: 1
---

# Pipeline and Stage Ownership

A chart prepares its input, measures its plot, then animates and draws. Keep each operation with the
code that owns its inputs.

| Responsibility | Owner | What it does |
|---|---|---|
| Input preparation | Chart entry | Validates data, converts it when needed, and clamps the style at the current density |
| Layout | Chart content and layout helpers | Measures the plot, chooses density, plans axes, and checks canvas bounds |
| Animation | Chart state used by content | Holds transitions between datasets, reveals, and live-window shifts |
| Drawing and interaction | Canvas and gesture handlers | Reads animation progress, draws visible marks, and maps pointer positions to public data |

## Input preparation

Indexed `ChartData` uses `ChartEntry` in `charts-core`, configured by a chart's `ChartSpec`.
A chart with another data model owns its validation and preparation in its entry. Both paths show
invalid input through `ChartErrors` and pass valid input with a clamped style to content.

Resolve selection against public data before conversion. Convert only when the renderer needs a
different shape; stacked bar, for example, transposes series into bars.

[Entry Seam and Chart Policy](entry-seam.md) describes the shared entry contract. [Validation
Errors](validation-errors.md) and [Style Clamping](style-clamping.md) own the input rules.

## Layout, animation, and drawing

Work that needs a measured size belongs below the entry: compact aggregation, axis layout, canvas
limits, and pointer geometry. Remember expensive calculations against the inputs they read.

Read animation progress where marks are drawn or labels are placed. Keep reusable drawing buffers
with the chart state so each frame can reuse them. A live window also needs slide state to move
points and labels together.

Use the caller's formatter for readouts. A formatter default belongs in the chart's `Defaults`.

See [Rendering and Animation](rendering.md), [X-Axis Labels](x-axis-labels.md), and
[Y-Axis Labels](y-axis-labels.md) for the shared rendering helpers.

## Choosing an owner

Ask which inputs the work needs:

- Public data and style: prepare or remember it at the entry when the result is useful to content.
- Measured size: compute it in content or a layout helper.
- Time or pointer position: read it in the animation, placement, drawing, or gesture code that uses it.

Data-only calculations can still live in content when they belong to its renderer. Domain and
normalization helpers currently do. Before moving them, check their dependencies and the number of
passes over the data. Validation establishes finiteness; conversion can rely on it.

Keep clamps that handle viewport limits, caller-chosen ranges, and intermediate animation states.
For example, line normalization clips values outside a caller's fixed range, and morph buffers can
have different lengths during a transition.

## Adding a chart

Choose the entry path for its data model and reuse shared layout, palette, validation, and drawing
helpers where their contracts fit. Bar and histogram illustrate a shared plot with separate input
policies. Document a new ownership rule or meaningful exception where it belongs, and cover the
chart's contract in its tests.
