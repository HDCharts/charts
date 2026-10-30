---
title: Segmented Ring Gauge
---

# Segmented Ring Gauge Chart

`SegmentedRingGaugeChart` shows how several contributions fill one fixed capacity. It differs from
a pie chart: the colored segments can occupy only part of the ring, and the remaining capacity stays
visible as its own rounded track segment.

![Segmented Ring Gauge Demo](/content/{{version}}/wiki/assets/segmented_ring_gauge_default.gif)

Add the existing `gauge` module:

```kotlin
implementation("io.github.hdcharts:gauge:<version>")
```

```kotlin
@Composable
fun CalorieProgress() {
    SegmentedRingGaugeChart(
        data = listOf(240.0, 180.0, 320.0).toChartData(
            categories = listOf("Carbs", "Protein", "Fat"),
        ),
        capacity = 2_000.0,
        centerSupportingLabel = "of 2000 kcal",
        style = SegmentedRingGaugeChartDefaults.style(
            segments = SegmentedRingGaugeChartDefaults.segments(
                colors = listOf(Color(0xFF3B91CC), Color(0xFF1EB157), Color(0xFFE3B713)),
            ),
            legend = SegmentedRingGaugeChartDefaults.legend(visible = false),
        ),
    )
}
```

The complete advanced example is in
[`SegmentedRingGaugeAdvancedExample.kt`](https://github.com/HDCharts/charts/blob/main/sample/androidApp/src/main/kotlin/io/github/hdcharts/app/docs/SegmentedRingGaugeAdvancedExample.kt).

The chart calculates the centre value from its segments (`740` in this example) unless you pass a
custom `centerLabel`. Use `centerSupportingLabel` for context such as the capacity and unit.

## Advanced example

Use the chart-specific style groups for the ring geometry, track, centre labels, and legend. The
example also shows hoisted selection and value formatters.

The centre has two text lines. `centerLabel`, or `centerValueFormatter` when `centerLabel` is
omitted, controls the primary line. `centerSupportingLabel` supplies the secondary line as
ready-to-display text, while `centerLabels` styles both lines. `selectedValueFormatter` formats the
selected segment in the chart title; it does not format either centre line.

Use `centerLabel` for a fixed or custom primary readout that is not calculated from the segment
total. Uncomment it in the example and remove `centerValueFormatter` when you want to provide that
text yourself. Keep `centerValueFormatter` when the primary readout should update with the data.

```kotlin
@Composable
fun AdvancedSegmentedRingGauge() {
    val selection = rememberChartSelection(initialIndex = 1)
    val capacity = 2_000.0

    SegmentedRingGaugeChart(
        modifier = Modifier.fillMaxWidth().height(320.dp),
        data = listOf(240.0, 180.0, 320.0).toChartData(
            categories = listOf("Carbs", "Protein", "Fat"),
        ),
        capacity = capacity,
        title = "Daily Nutrition",
        subtitle = "Current intake by macronutrient",
        centerSupportingLabel = "of ${capacity.toInt()} kcal",
        // Alternative fixed primary label: uncomment this and remove centerValueFormatter below.
        // centerLabel = "740 kcal",
        centerValueFormatter = ChartValueFormatters.suffix(" kcal"),
        selectedValueFormatter = ChartValueFormatters.suffix(" kcal"),
        selection = selection,
        interactionEnabled = false,
        animateOnStart = false,
        style =
            SegmentedRingGaugeChartDefaults.style(
                chartContainerStyle = ChartContainerDefaults.style(contentPadding = 16.dp),
                segments =
                    SegmentedRingGaugeChartDefaults.segments(
                        alpha = 0.95f,
                        colors =
                            listOf(
                                Color(0xFF3B91CC),
                                Color(0xFF1EB157),
                                Color(0xFFE3B713),
                            ),
                        // Alternative palette: remove colors above and uncomment baseColor.
                        // baseColor = Color(0xFF3B91CC),
                        width = 28.dp,
                        spacing = 6.dp,
                        cornerRadius = 8.dp,
                        // Any start position can be paired with either direction.
                        // Options: Top, TopRight, Right, BottomRight, Bottom, BottomLeft, Left, TopLeft.
                        startPosition = SegmentedRingGaugeStartPosition.Top,
                        // Options: Clockwise or CounterClockwise.
                        direction = SegmentedRingGaugeDirection.Clockwise,
                    ),
                track = SegmentedRingGaugeChartDefaults.track(color = Color(0xFFE5E7EB)),
                centerLabels =
                    SegmentedRingGaugeChartDefaults.centerLabels(
                        value = TextStyle(color = Color(0xFF111827), fontSize = 32.sp),
                        supporting = TextStyle(color = Color(0xFF6B7280), fontSize = 14.sp),
                        spacing = 6.dp,
                    ),
                legend = SegmentedRingGaugeChartDefaults.legend(visible = false),
            ),
    )
}
```

## Input and selection

Pass exactly one series of non-negative values. Its total must be finite and no greater than the
finite, positive `capacity`; invalid input renders the standard chart error UI. Categories are
optional, but when supplied they name the segments in the legend and in the selected title.

Tap a visible segment to select it. The selected segment scales up and the title reads
`Category: value`. User selections clear automatically after three seconds; tapping the same or
another segment renews the timeout. Tap the track or the hole to clear the selection. Set
`interactionEnabled = false` for a static display with persistent programmatic selection.

## Styling

`segments` controls the palette, ring width, opacity, and angular space between segments. The track
represents only unused capacity, and `centerLabels` styles the value and supporting text. Its
`width`, `spacing`, `cornerRadius`, `startPosition`, and `direction` parameters control the complete
segment silhouette and placement.
