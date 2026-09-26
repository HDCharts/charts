<p align="center">
  <img
    src="./readme-assets/hdcharts-logo-light.svg#gh-light-mode-only"
    alt="HDCharts logo"
    align="center"
    width="300"
  />
  <img
    src="./readme-assets/hdcharts-logo-dark.svg#gh-dark-mode-only"
    alt="HDCharts logo"
    align="center"
    width="300"
  />
</p>

<p align="center">
  <a href="https://central.sonatype.com/artifact/io.github.hdcharts/charts/overview">
    <img src="https://img.shields.io/maven-central/v/io.github.hdcharts/charts.svg?label=Maven%20Central" />
  </a>
  <a href="https://central.sonatype.com/repository/maven-snapshots/io/github/hdcharts/charts/maven-metadata.xml">
    <img src="https://img.shields.io/maven-metadata/v?metadataUrl=https%3A%2F%2Fcentral.sonatype.com%2Frepository%2Fmaven-snapshots%2Fio%2Fgithub%2Fhdcharts%2Fcharts%2Fmaven-metadata.xml&label=Snapshots&color=4285F4" />
  </a>
  <img src="https://img.shields.io/badge/Compose_Multiplatform-1.11.1-4285F4?logo=jetpackcompose" />
  <img src="https://img.shields.io/badge/Kotlin_Multiplatform-2.4.10-0095D5?logo=kotlin" />
  <img src="https://img.shields.io/badge/AGP-9.3.1-2E7D32?logo=android" />
</p>

<p align="center">
  A Kotlin Multiplatform chart library built with Jetpack Compose.
</p>

<p align="center">
  <img width="893" alt="demo-light" src="https://github.com/user-attachments/assets/9bd7fc79-93c2-434a-a70c-d3a3a6dc9db4" />
</p>

---

## 📚 Documentation
https://charts.hdcode.dev/

## 🟢 Production Demo
https://charts.hdcode.dev/demo

## ✨ Snapshot Demo
https://charts.hdcode.dev/demo/snapshot/

## 🏀 Playground
https://charts.hdcode.dev/playground

## Get Started

```kotlin
dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
}
```


### All chart types

Use the umbrella artifact when you want all chart types with the simplest setup.

```kotlin
commonMain.dependencies {
    implementation("io.github.hdcharts:charts:<version>")
}
```

### Individual chart modules

Use independent modules when you want only specific chart types and smaller dependency footprint.

```kotlin
commonMain.dependencies {
    implementation("io.github.hdcharts:line:<version>")
    implementation("io.github.hdcharts:pie:<version>")
    implementation("io.github.hdcharts:bar:<version>")
    implementation("io.github.hdcharts:histogram:<version>")
    implementation("io.github.hdcharts:stacked-bar:<version>")
    implementation("io.github.hdcharts:stacked-area:<version>")
    implementation("io.github.hdcharts:radar:<version>")
}
```

### BOM

Use BOM for version alignment where Gradle platforms are supported.
For KMP `commonMain`, keep explicit versions as shown above.

```kotlin
dependencies {
    implementation(platform("io.github.hdcharts:bom:<version>"))
    implementation("io.github.hdcharts:line")
    implementation("io.github.hdcharts:pie")
    implementation("io.github.hdcharts:bar")
    implementation("io.github.hdcharts:histogram")
    implementation("io.github.hdcharts:stacked-bar")
    implementation("io.github.hdcharts:stacked-area")
    implementation("io.github.hdcharts:radar")
}
```

## Example

```kotlin
@Composable
fun BasicLineChart() {
    val values = listOf(42.0, 38.0, 45.0, 51.0, 47.0, 54.0, 49.0)
    val labels = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")

    val dataSet = values.toChartData(
        categories = labels,
    )

    LineChart(
        data = dataSet,
        modifier = Modifier
            .fillMaxWidth()
            .height(260.dp),
    )
}
```

The chart `modifier` controls its size and placement. Surrounding chrome stays
with the consumer:

```kotlin
Card {
    LineChart(
        data = dataSet,
        modifier = Modifier
            .fillMaxWidth()
            .height(260.dp),
    )
}
```

<p align="center">
  <img src="docs/content/snapshot/wiki/assets/line_default.gif" alt="Basic line chart example" width="400" />
</p>

## v3 API

### Sizing

The chart composable's `modifier` is the only sizing control. A bounded `modifier` becomes a
rectangular plot; a one-axis bounded `modifier` derives a square fallback; a fully unbounded
`modifier` falls back to a 200.dp default.

```kotlin
LineChart(
    data = dataSet,
    modifier = Modifier
        .fillMaxWidth()
        .height(260.dp),
)
```

### Surrounding chrome

Background, shadow, shape, and outer spacing are not applied by the chart. Wrap the chart in
any Compose surface to add chrome.

```kotlin
Card(modifier = Modifier.padding(16.dp)) {
    LineChart(
        data = dataSet,
        modifier = Modifier.fillMaxWidth().height(260.dp),
    )
}
```

### Content spacing

`ChartContainerDefaults.style()` returns a `ChartContainerStyle` with `styleTitle` and
`contentPadding`. Set `contentPadding` to change the internal spacing reserved for axes,
title, and legend.

```kotlin
LineChart(
    data = dataSet,
    modifier = Modifier.fillMaxWidth().height(260.dp),
    style = LineChartDefaults.style(
        chartContainerStyle = ChartContainerDefaults.style(contentPadding = 8.dp),
    ),
)
```

### Style per chart

Each chart module exposes its own `*Defaults.style()` factory. Use named arguments to customize
only the parts you care about. The example below customizes line color, axis, points, and zoom
controls; everything else keeps its default.

```kotlin
LineChart(
    data = dataSet,
    title = "Daily Support Tickets",
    modifier = Modifier.fillMaxWidth().height(260.dp),
    style = LineChartDefaults.style(
        chartContainerStyle = ChartContainerDefaults.style(),
        line = LineVisualStyle(
            color = Color(0xFF1E88E5),
            alpha = 1f,
            colors = emptyList(),
            strokeWidth = 2.dp,
            bezier = true,
        ),
        points = LinePointStyle(color = Color(0xFF1E88E5), size = 4.dp, visible = true),
        axis = LineAxisStyle(
            visible = true,
            color = Color.Gray,
            lineWidth = 1.dp,
            yLabels = AxisLabelStyle(visible = true, color = Color.Gray, size = 12.sp, count = 5),
            xLabels = AxisLabelStyle(visible = true, color = Color.Gray, size = 12.sp, count = 7),
        ),
        zoomControlsVisible = true,
    ),
)
```

### Fixed Y-axis range

By default the Y-axis is derived from the data, which can make small fluctuations look
exaggerated when the axis doesn't start at zero. Set `range` on `*Defaults.style()` to pin
`min`, `max`, or both, independently of one another; whichever bound you leave `null` keeps
deriving from the data.

```kotlin
LineChart(
    data = dataSet,
    modifier = Modifier.fillMaxWidth().height(260.dp),
    style = LineChartDefaults.style(
        range = LineChartDefaults.range(min = 0.0, max = 100.0),
    ),
)
```

`BarChart` exposes the same `range` block on `BarChartDefaults.style()`.

### Selection

Use `rememberChartSelection()` to hoist selection. The chart calls back into it; you read
`selectedIndex` to drive external UI such as legends or summary cards.

```kotlin
val selection = rememberChartSelection()
val values = listOf(42.0, 38.0, 45.0, 51.0, 47.0, 54.0, 49.0)

LineChart(
    data = values.toChartData(),
    modifier = Modifier.fillMaxWidth().height(260.dp),
    selection = selection,
)

val selected = values.getOrNull(selection.selectedIndex ?: 0)
Text("Selected: $selected")
```

### Responsive layout

Compose the chart with any layout, including `Row`, `Column`, `LazyColumn`, or `BoxWithConstraints`.
A bounded `modifier` is always honored; an unbounded one falls back to the documented defaults.

```kotlin
Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
    LineChart(
        data = lineData,
        modifier = Modifier.fillMaxWidth().height(220.dp),
    )
    Spacer(modifier = Modifier.height(16.dp))
    BarChart(
        data = barData,
        modifier = Modifier.fillMaxWidth().height(220.dp),
    )
}
```

### Animation and interaction

`animateOnStart` controls the initial reveal animation; `interactionEnabled` disables gestures,
selection, and zoom controls together. Disable interaction to embed a chart inside a tappable
card without conflicts.

```kotlin
LineChart(
    data = dataSet,
    modifier = Modifier.fillMaxWidth().height(260.dp),
    animateOnStart = true,
    interactionEnabled = false,
)
```

## Contributing
See [CONTRIBUTING.md](CONTRIBUTING.md) for contribution guidelines.

## License
[MIT](LICENSE)