---
title: Line
---

# Line Chart

`LineChart` renders single or multi-series line data. It has one composable with two
render modes, selected via `renderMode`.

![Line Demo](/content/{{version}}/wiki/assets/line_default.gif)

## Basic Usage

The example below is minimal and runs as written. The GIF above uses a longer generated
series; its full source is in
[`LineExample.kt`](https://github.com/HDCharts/charts/blob/main/sample/androidApp/src/main/kotlin/io/github/hdcharts/app/gif/docs/LineExample.kt).

```kotlin
@Composable
fun ShowLine() {
    val data = listOf(30.0, 45.0, 38.0, 52.0, 61.0, 49.0, 58.0).toChartData(
        categories = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"),
        seriesName = "Daily Support Tickets",
    )

    LineChart(data = data, title = "Daily Support Tickets")
}
```

## Multi-Series

Pass more than one named series to plot multiple lines on the same chart.

![MultiLine Demo](/content/{{version}}/wiki/assets/multi_line_default.gif)

Full source behind the GIF:
[`MultiLineExample.kt`](https://github.com/HDCharts/charts/blob/main/sample/androidApp/src/main/kotlin/io/github/hdcharts/app/gif/docs/MultiLineExample.kt).

```kotlin
@Composable
fun ShowMultiLine() {
    val data = listOf(
        "Web Store" to listOf(180.0, 310.0, 420.0, 560.0, 640.0, 720.0),
        "Mobile App" to listOf(120.0, 240.0, 350.0, 430.0, 520.0, 580.0),
        "Partner Sales" to listOf(60.0, 130.0, 190.0, 240.0, 300.0, 340.0),
    ).toChartData(
        categories = listOf("W1", "W2", "W3", "W4", "W5", "W6"),
    )

    LineChart(
        data = data,
        title = "Weekly Revenue by Channel",
        valueFormatter = ChartValueFormatters.prefix("$"),
    )
}
```

## Fixed Y-Axis Range

By default the Y-axis is derived from the data, which can make small fluctuations look
exaggerated when the axis doesn't start at zero. Set `range` on `LineChartDefaults.style()`
to pin `min`, `max`, or both, independently of one another; whichever bound you leave `null`
keeps deriving from the data.

![Line Range Demo](/content/{{version}}/wiki/assets/line_range.gif)

Full source behind the GIF:
[`LineWithRangeExample.kt`](https://github.com/HDCharts/charts/blob/main/sample/androidApp/src/main/kotlin/io/github/hdcharts/app/gif/docs/LineWithRangeExample.kt).

```kotlin
@Composable
fun ShowLineWithRange() {
    val data = listOf(62.0, 58.0, 65.0, 60.0, 67.0, 59.0, 63.0).toChartData(
        categories = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"),
        seriesName = "Daily Support Tickets",
    )

    LineChart(
        data = data,
        title = "Daily Support Tickets",
        style = LineChartDefaults.style(
            range = LineChartDefaults.range(min = 0.0, max = 100.0),
        ),
    )
}
```

## Render Modes

`LineChart` has one composable with two render modes, selected via `renderMode`.

### Morph

The default mode. When `data` changes, the line animates smoothly from its old shape to
the new one. This is the right choice for most charts, including ones with large point
counts that support zoom and scroll.

![Line Morph Demo](/content/{{version}}/wiki/assets/line_morph.gif)

Hand `LineChart` new `data` and it animates from the previous shape. The example below
cycles three weeks of the same metric on a timer, with the range pinned so the axis
doesn't jump between weeks — see [Fixed Y-Axis Range](#fixed-y-axis-range) above. Full
source behind the GIF:
[`MorphingLineExample.kt`](https://github.com/HDCharts/charts/blob/main/sample/androidApp/src/main/kotlin/io/github/hdcharts/app/gif/docs/MorphingLineExample.kt).

```kotlin
@Composable
fun ShowMorphingLine() {
    val weeks = listOf(
        listOf(42.0, 38.0, 45.0, 51.0, 47.0, 54.0, 49.0),
        listOf(46.0, 52.0, 68.0, 74.0, 61.0, 50.0, 44.0),
        listOf(58.0, 55.0, 49.0, 43.0, 39.0, 31.0, 28.0),
    )
    var index by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(1400.milliseconds)
            index = (index + 1) % weeks.size
        }
    }

    LineChart(
        data = weeks[index].toChartData(
            categories = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"),
            seriesName = "Daily Support Tickets",
        ),
        title = "Daily Support Tickets",
        style = LineChartDefaults.style(
            range = LineChartDefaults.range(min = 0.0, max = 80.0),
        ),
        animateOnStart = false,
        renderMode = LineChartRenderMode.Morph,
    )
}
```

### Timeline

Built for live, continuously updating data. Instead of morphing, new points slide the whole
chart horizontally like a rolling window, and the Y-axis rescales automatically as old
values leave the window. Interaction (selection, gestures) is disabled in this mode.
`animationDuration` sets the update speed — how long each new point takes to slide into
place — and should match how often `data` actually changes.

![Line Timeline Demo](/content/{{version}}/wiki/assets/line_timeline.gif)

Keep a fixed-size window of values and replace it on every tick. Full source behind the
GIF, including its heartbeat-shaped reading generator, is in
[`LiveLineExample.kt`](https://github.com/HDCharts/charts/blob/main/sample/androidApp/src/main/kotlin/io/github/hdcharts/app/gif/docs/LiveLineExample.kt).

```kotlin
@Composable
fun ShowLiveLine() {
    var values by remember { mutableStateOf(List(120) { 18.0 }) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(150.milliseconds)
            values = values.drop(1) + nextReading()
        }
    }

    LineChart(
        data = values.toChartData(seriesName = "Live Sensor Reading"),
        title = "Live Sensor Reading",
        animateOnStart = false,
        renderMode = LineChartRenderMode.Timeline,
        animationDuration = 120.milliseconds,
    )
}
```
