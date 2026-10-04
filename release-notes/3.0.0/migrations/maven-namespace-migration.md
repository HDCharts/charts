# Maven namespace migration

HDCharts has moved to the organization-owned Maven group
`io.github.hdcharts`. The personal identity `io.github.dautovicharis` no
longer receives new artifacts.

## Coordinates

| | Before (2.x) | After (3.0.0) |
|---|---|---|
| Umbrella | `io.github.dautovicharis:charts` | `io.github.hdcharts:charts` |
| Modules | `io.github.dautovicharis:charts-line` | `io.github.hdcharts:line` |
| BOM | `io.github.dautovicharis:charts-bom` | `io.github.hdcharts:bom` |

## Packages

Each module now has its own Kotlin package, named after its artifact:

| Before (2.x) | After (3.0.0) |
|---|---|
| Shared models: `io.github.dautovicharis.charts.model.*` | `io.github.hdcharts.core.model.*` |
| Shared styles: `io.github.dautovicharis.charts.style.*` | `io.github.hdcharts.core.style.*` |
| `LineChart`, `LiveLineChart`, `LineChartStyle`, `LineChartDefaults` | `io.github.hdcharts.line.*` |
| `PieChart`, `PieChartStyle`, `PieChartDefaults` | `io.github.hdcharts.pie.*` |
| `BarChart` | `io.github.hdcharts.bar.*` |
| `HistogramChart` | `io.github.hdcharts.histogram.*` |
| `StackedBarChart`, `StackedBarChartStyle`, `StackedBarChartDefaults` | `io.github.hdcharts.stackedbar.*` |
| `StackedAreaChart`, `StackedAreaChartStyle`, `StackedAreaChartDefaults` | `io.github.hdcharts.stackedarea.*` |
| `RadarChart`, `RadarChartStyle`, `RadarChartDefaults` | `io.github.hdcharts.radar.*` |

`BarChartStyle`, `BarChartDefaults`, `HistogramChartStyle`, and
`HistogramChartDefaults` are shared by the bar and histogram modules, so they
live in `io.github.hdcharts.core.style`.

Update both the dependency coordinates and the `import` statements in your
project. Removing the old imports and letting the IDE re-import each symbol
picks the new packages.

## Before

```kotlin
import io.github.dautovicharis.charts.LineChart
import io.github.dautovicharis.charts.model.toChartData

commonMain.dependencies {
    implementation("io.github.dautovicharis:charts-line:2.4.0")
}
```

## After

```kotlin
import io.github.hdcharts.core.model.toChartData
import io.github.hdcharts.line.LineChart

commonMain.dependencies {
    implementation("io.github.hdcharts:line:3.0.0")
}
```

## Upgrade path from 2.x

Consumers staying on `io.github.dautovicharis:charts-line:2.x` do not need to
change anything until they upgrade to `3.0.0`. When a consumer that pins
`io.github.dautovicharis:charts-line:3.0.0` resolves the new version, Maven
Central follows the relocation published under the old group and downloads the
artifact from `io.github.hdcharts:line:3.0.0` automatically. No manual
intervention is required during the upgrade.

The relocation applies once at the `3.0.0` release. From `3.0.1` onward, the
project only publishes under `io.github.hdcharts`. Pin the new coordinates
directly when starting a new project.
