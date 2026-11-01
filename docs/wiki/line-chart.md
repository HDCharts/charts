---
title: Line Chart
---

# Line Chart

## Overview

`LineChart` shows how one or more numeric series change across ordered categories. It derives the
vertical range from the data by default and morphs between updates that keep the same number of
series and points. Use `LiveLineChart` for a continuously advancing window where new points slide
into view.

![Line Demo](/content/{{version}}/wiki/assets/line_default.gif)

The GIF uses a longer generated series; its full recording source is
[`LineExample.kt`](https://github.com/HDCharts/charts/blob/main/sample/androidApp/src/main/kotlin/io/github/hdcharts/app/gif/docs/LineExample.kt).

## Basic Usage

<!-- BEGIN WIKI EXAMPLE: sample/androidApp/src/main/kotlin/io/github/hdcharts/app/docs/LineBasicExamples.kt#line-basic-usage -->
```kotlin
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.hdcharts.core.model.toChartData
import io.github.hdcharts.line.LineChart

internal data class DailyMeasurement(
    val day: String,
    val temperatureCelsius: Double,
) {
    companion object {
        const val SERIES_NAME = "Temperature"

        val sample =
            listOf(
                DailyMeasurement("Mon", 18.0),
                DailyMeasurement("Tue", 21.0),
                DailyMeasurement("Wed", 19.0),
                DailyMeasurement("Thu", 24.0),
                DailyMeasurement("Fri", 22.0),
            )
    }
}

@Composable
fun ShowDailyTemperatures(modifier: Modifier = Modifier) {
    val measurements = DailyMeasurement.sample
    val data =
        measurements.toChartData(
            value = { it.temperatureCelsius },
            category = { it.day },
            seriesName = DailyMeasurement.SERIES_NAME,
        )

    LineChart(
        data = data,
        modifier = modifier,
        title = "Daily High Temperature (°C)",
    )
}
```
<!-- END WIKI EXAMPLE: line-basic-usage -->

![Line chart showing daily high temperatures across five weekdays](/content/{{version}}/wiki/assets/wiki-line-basic.png)

## Capabilities

### Multiple Series

Use one named value list per series and provide one shared category list in matching order. Each
series must have the same number of values, and the category count must match that length. The
chart uses its line palette to distinguish series and shows their names in the legend. This
focused example reuses the imports from Basic Usage.

![MultiLine Demo](/content/{{version}}/wiki/assets/multi_line_default.gif)

The recording uses a longer data set. Its source is
[`MultiLineExample.kt`](https://github.com/HDCharts/charts/blob/main/sample/androidApp/src/main/kotlin/io/github/hdcharts/app/gif/docs/MultiLineExample.kt).

<!-- BEGIN WIKI EXAMPLE: sample/androidApp/src/main/kotlin/io/github/hdcharts/app/docs/LineBasicExamples.kt#line-multiple-series -->
```kotlin
@Composable
fun ShowWeeklyRevenueByChannel() {
    val weeks = listOf("W1", "W2", "W3", "W4", "W5")
    val data =
        listOf(
            "Web Store" to listOf(180.0, 310.0, 420.0, 560.0, 640.0),
            "Mobile App" to listOf(120.0, 240.0, 350.0, 430.0, 520.0),
            "Partner Sales" to listOf(60.0, 130.0, 190.0, 240.0, 300.0),
        ).toChartData(categories = weeks)

    LineChart(
        data = data,
        modifier = Modifier.fillMaxWidth().height(300.dp),
        title = "Weekly Revenue by Channel",
    )
}
```
<!-- END WIKI EXAMPLE: line-multiple-series -->

### Fixed Value Range

The default Y-axis range follows the minimum and maximum values in the data. Set either or both
bounds to compare changing measurements on a consistent scale. A `null` bound continues to follow
the data; this is useful when the measurements are meaningful relative to their observed values,
without implying a zero baseline.

![Line Range Demo](/content/{{version}}/wiki/assets/line_range.gif)

The recording uses a different dataset from the teaching example. Its source is
[`LineWithRangeExample.kt`](https://github.com/HDCharts/charts/blob/main/sample/androidApp/src/main/kotlin/io/github/hdcharts/app/gif/docs/LineWithRangeExample.kt).

<!-- BEGIN WIKI EXAMPLE: sample/androidApp/src/main/kotlin/io/github/hdcharts/app/docs/LineCapabilities.kt#line-fixed-range -->
```kotlin
@Composable
fun CompareTemperatures(data: ChartData) {
    LineChart(
        data = data,
        style = LineChartDefaults.style(range = LineChartDefaults.range(min = 0.0, max = 40.0)),
    )
}
```
<!-- END WIKI EXAMPLE: line-fixed-range -->

### Selection and Interaction

`LineChart` supports user selection and programmatic selection through `ChartSelection`. Its
`selectedIndex` is the selected source X index shared by every series; in a multi-series chart,
the selection represents that category across the aligned series. `interactionEnabled = false`
disables user controls while programmatic selection can still be displayed. This excerpt reuses
`data` from Basic Usage.

<!-- BEGIN WIKI EXAMPLE: sample/androidApp/src/main/kotlin/io/github/hdcharts/app/docs/LineCapabilities.kt#line-selection -->
```kotlin
@Composable
fun SelectTemperature(
    data: ChartData,
    modifier: Modifier = Modifier,
    selection: ChartSelection = rememberChartSelection(),
) {
    Column(modifier = modifier) {
        LineChart(
            data = data,
            modifier = Modifier.weight(1f).fillMaxWidth(),
            selection = selection,
        )
        Text("Selected point index: ${selection.selectedIndex ?: "none"}")
    }
}
```
<!-- END WIKI EXAMPLE: line-selection -->

![Line chart with Thursday selected and its value highlighted](/content/{{version}}/wiki/assets/wiki-line-selection.png)

### Gradients

Use a line gradient to color the line across the plot. This example reuses `data` from Basic Usage.
For gradient directions, spans, and stops, see the shared [Gradients guide](/{{version}}/wiki/gradients).

![Line Gradient](/content/{{version}}/wiki/assets/line_gradient.gif)

The GIF uses generated data; its source is
[`LineGradientExample.kt`](https://github.com/HDCharts/charts/blob/main/sample/androidApp/src/main/kotlin/io/github/hdcharts/app/gif/docs/LineGradientExample.kt).

<!-- BEGIN WIKI EXAMPLE: sample/androidApp/src/main/kotlin/io/github/hdcharts/app/docs/LineCapabilities.kt#line-gradient -->
```kotlin
@Composable
fun GradientTemperature(data: ChartData) {
    val gradient =
        ChartGradients.vertical(
            colors = listOf(Color(0xFF2563EB), Color(0xFFF59E0B), Color(0xFFDC2626)),
            span = GradientSpan.Plot,
        )

    LineChart(
        data = data,
        style = LineChartDefaults.style(line = LineChartDefaults.line(gradient = gradient)),
    )
}
```
<!-- END WIKI EXAMPLE: line-gradient -->

### Morphing

When new `LineChart` data keeps the same series count and the same number of points in each series,
the chart morphs between the old and new line shapes. A change to either count redraws the chart
instead. Keep categories aligned across updates so each point continues to represent the same
position in the series. Morphing transitions between datasets; it does not advance a sliding
timeline. The existing recording shows a longer, pinned-range example:
[`MorphingLineExample.kt`](https://github.com/HDCharts/charts/blob/main/sample/androidApp/src/main/kotlin/io/github/hdcharts/app/gif/docs/MorphingLineExample.kt).

![Line Morph Demo](/content/{{version}}/wiki/assets/line_morph.gif)

<!-- BEGIN WIKI EXAMPLE: sample/androidApp/src/main/kotlin/io/github/hdcharts/app/docs/LineCapabilities.kt#line-morphing -->
```kotlin
@Composable
fun MorphWeeklyMeasurements() {
    val weeks =
        listOf(
            listOf(42.0, 38.0, 45.0, 51.0, 47.0),
            listOf(46.0, 52.0, 48.0, 44.0, 50.0),
        )
    var weekIndex by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(2_000.milliseconds)
            weekIndex = (weekIndex + 1) % weeks.size
        }
    }

    val data =
        weeks[weekIndex].toChartData(
            categories = listOf("Mon", "Tue", "Wed", "Thu", "Fri"),
            seriesName = "Daily Measurements",
        )

    LineChart(
        data = data,
        title = "Daily Measurements",
        animateOnStart = false,
    )
}
```
<!-- END WIKI EXAMPLE: line-morphing -->

### Live Line Chart

`LiveLineChart` displays a rolling window of one or more aligned series. When each update removes
the oldest point and appends a new point, the plot and its category labels shift together, while
the Y-axis range follows values in the current window. Other updates with the same series and
point counts morph over `shiftDuration`; changing either count redraws immediately.

Update data at a cadence close to `shiftDuration` so each shift can finish as the next point
arrives. The example uses a fixed-size window, a deterministic repeating reading sequence, and a
500 ms update cadence. `LiveLineChart` is display-only and ignores gestures; it does not expose
`selection` or `interactionEnabled` parameters. For multiple live series, supply equally sized
named series and shared categories through the same `ChartData` structure used by `LineChart`.

![Live Line Demo](/content/{{version}}/wiki/assets/line_timeline.gif)

The GIF uses a heartbeat-shaped generator; its full source is
[`LiveLineExample.kt`](https://github.com/HDCharts/charts/blob/main/sample/androidApp/src/main/kotlin/io/github/hdcharts/app/gif/docs/LiveLineExample.kt).

<!-- BEGIN WIKI EXAMPLE: sample/androidApp/src/main/kotlin/io/github/hdcharts/app/docs/LiveLineExample.kt#live-line-basic -->
```kotlin
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.hdcharts.core.model.toChartData
import io.github.hdcharts.line.LiveLineChart
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun ShowLiveSensorReadings() {
    val readings = listOf(18.0, 18.5, 19.0, 18.7, 19.4, 20.0, 19.6, 19.1)
    val windowSize = 12
    var firstSample by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(500.milliseconds)
            firstSample++
        }
    }

    val sampleNumbers = firstSample until (firstSample + windowSize)
    val values = sampleNumbers.map { sample -> readings[sample % readings.size] }
    val categories = sampleNumbers.map { "#${it + 1}" }
    val data = values.toChartData(categories = categories, seriesName = "Sensor")

    LiveLineChart(
        data = data,
        modifier = Modifier.fillMaxWidth().height(300.dp),
        title = "Live Sensor Reading",
        shiftDuration = 500.milliseconds,
        animateOnStart = false,
    )
}
```
<!-- END WIKI EXAMPLE: live-line-basic -->

The live gradient GIF demonstrates colors progressing from older to newer points as the window
slides. Its source is
[`LiveLineGradientExample.kt`](https://github.com/HDCharts/charts/blob/main/sample/androidApp/src/main/kotlin/io/github/hdcharts/app/gif/docs/LiveLineGradientExample.kt).

![Live Line Gradient](/content/{{version}}/wiki/assets/line_timeline_gradient.gif)

## API Reference

- [LineChart](/static/api/{{version}}/charts/io.github.hdcharts.line/-line-chart.html)
- [LiveLineChart](/static/api/{{version}}/charts/io.github.hdcharts.line/-live-line-chart.html)
- [LineChartDefaults](/static/api/{{version}}/charts/io.github.hdcharts.line/-line-chart-defaults/index.html)
- [LineChartStyle](/static/api/{{version}}/charts/io.github.hdcharts.line/-line-chart-style/index.html)
