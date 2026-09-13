---
name: hdc-ux
description: HDCharts chart usability, interaction design, accessibility, responsive behavior, visual hierarchy, data communication, and demo discoverability.
---

# HDCharts UX

Use for chart behavior, interaction, visual hierarchy, accessibility,
responsive layout, defaults, examples, and demo flow.

## Rules

- Make titles, axes, categories, series, legends, selected readouts, and units
  explain the chart in user-facing language.
- Make selection immediate and pair highlights with a title, label, value, or
  legend update. Use labels, values, strokes, or shapes alongside color.
- Define selection behavior for data replacement, resize, dense mode, scrolling,
  zooming, holder replacement, and disabled interaction.
- Match touch targets, hit testing, geometry, and clipping to the visible chart.
- Expose dense-data fit, expand, zoom, and scroll behavior while preserving
  source-index meaning.
- Provide clear error and empty states, preserve the caller modifier, and keep
  invalid numeric values out of rendering.
- Design for narrow, wide, tablet, and landscape layouts. Keep long labels,
  axes, and legends readable and separated from the plot.
- Preserve semantics for titles, errors, selected readouts, scrollable plots,
  and controls. Use useful labels and touch targets.
- Use cancellable, deterministic motion that explains data changes and keeps
  selection current.
- Keep defaults legible and samples representative of default, customized,
  selected, dense, invalid, and responsive states.

## UX Validation

- Exercise small and large sizes and long labels before changing screenshots.
- Test keyboard, pointer, touch, scroll, zoom, selection, and disabled states
  on supported platforms.
- Verify error, empty, selected, and loading states through semantics and
  visible content.
- Add focused Compose interaction tests for behavioral changes.
