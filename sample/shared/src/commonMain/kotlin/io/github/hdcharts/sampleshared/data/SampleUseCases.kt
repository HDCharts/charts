package io.github.hdcharts.sampleshared.data

import io.github.hdcharts.sampleshared.data.impl.DefaultBarSampleUseCase
import io.github.hdcharts.sampleshared.data.impl.DefaultChartPreviewUseCase
import io.github.hdcharts.sampleshared.data.impl.DefaultHistogramSampleUseCase
import io.github.hdcharts.sampleshared.data.impl.DefaultLineSampleUseCase
import io.github.hdcharts.sampleshared.data.impl.DefaultLiveLatencyTimelineUseCase
import io.github.hdcharts.sampleshared.data.impl.DefaultMultiLineSampleUseCase
import io.github.hdcharts.sampleshared.data.impl.DefaultPieSampleUseCase
import io.github.hdcharts.sampleshared.data.impl.DefaultRadarSampleUseCase
import io.github.hdcharts.sampleshared.data.impl.DefaultStackedAreaSampleUseCase
import io.github.hdcharts.sampleshared.data.impl.DefaultStackedBarSampleUseCase

fun pieSampleUseCase(): PieSampleUseCase = DefaultPieSampleUseCase()

fun lineSampleUseCase(): LineSampleUseCase = DefaultLineSampleUseCase()

fun barSampleUseCase(): BarSampleUseCase = DefaultBarSampleUseCase()

fun histogramSampleUseCase(): HistogramSampleUseCase = DefaultHistogramSampleUseCase()

fun multiLineSampleUseCase(): MultiLineSampleUseCase = DefaultMultiLineSampleUseCase()

fun stackedBarSampleUseCase(): StackedBarSampleUseCase = DefaultStackedBarSampleUseCase()

fun stackedAreaSampleUseCase(): StackedAreaSampleUseCase = DefaultStackedAreaSampleUseCase()

fun radarSampleUseCase(): RadarSampleUseCase = DefaultRadarSampleUseCase()

fun liveLatencyTimelineUseCase(): LiveLatencyTimelineUseCase = DefaultLiveLatencyTimelineUseCase()

fun chartPreviewUseCase(): ChartPreviewUseCase = DefaultChartPreviewUseCase()
