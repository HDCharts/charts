# sample-shared

Shared sample code for the HDCharts demo apps.

## Purpose

Stateless, app-independent sample data used by the chart demos:

- Per-chart sample data sources (`PieSampleUseCase`, `LineSampleUseCase`, `BarSampleUseCase`, `HistogramSampleUseCase`, `MultiLineSampleUseCase`, `StackedBarSampleUseCase`, `StackedAreaSampleUseCase`, `RadarSampleUseCase`)
- Live latency timeline generator (`LiveLatencyTimelineUseCase`) for the line/multi-line live preview
- Gallery preview seed and jitter (`ChartPreviewUseCase`)
- Reusable sample data models and shared UI theme (`Dimens`, `LocalChartColors`, typography, color schemes)

## Constraints

- No dependency on `sample/app` or any view-model layer.
- Depends only on the public `io.github.hdcharts.charts` model API and Compose.
- Pure logic; safe to reuse from any host (Android, JVM, iOS, Wasm).

## Usage

The host app wires implementations through factories declared in
[`SampleUseCases.kt`](src/commonMain/kotlin/io/github/hdcharts/sampleshared/data/SampleUseCases.kt)
and theme providers from the [`theme`](src/commonMain/kotlin/io/github/hdcharts/sampleshared/theme) package.
