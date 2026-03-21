package io.github.dautovicharis.charts.demoshared.data

import io.github.dautovicharis.charts.demoshared.data.impl.DefaultBarSampleUseCase
import io.github.dautovicharis.charts.demoshared.data.impl.DefaultHistogramSampleUseCase
import io.github.dautovicharis.charts.demoshared.data.impl.DefaultLineSampleUseCase
import io.github.dautovicharis.charts.demoshared.data.impl.DefaultMultiLineSampleUseCase
import io.github.dautovicharis.charts.demoshared.data.impl.DefaultPieSampleUseCase
import io.github.dautovicharis.charts.demoshared.data.impl.DefaultRadarSampleUseCase
import io.github.dautovicharis.charts.demoshared.data.impl.DefaultStackedAreaSampleUseCase
import io.github.dautovicharis.charts.demoshared.data.impl.DefaultStackedBarSampleUseCase

fun pieSampleUseCase(): PieSampleUseCase = DefaultPieSampleUseCase()

fun lineSampleUseCase(): LineSampleUseCase = DefaultLineSampleUseCase()

fun barSampleUseCase(): BarSampleUseCase = DefaultBarSampleUseCase()

fun histogramSampleUseCase(): HistogramSampleUseCase = DefaultHistogramSampleUseCase()

fun multiLineSampleUseCase(): MultiLineSampleUseCase = DefaultMultiLineSampleUseCase()

fun stackedBarSampleUseCase(): StackedBarSampleUseCase = DefaultStackedBarSampleUseCase()

fun stackedAreaSampleUseCase(): StackedAreaSampleUseCase = DefaultStackedAreaSampleUseCase()

fun radarSampleUseCase(): RadarSampleUseCase = DefaultRadarSampleUseCase()
