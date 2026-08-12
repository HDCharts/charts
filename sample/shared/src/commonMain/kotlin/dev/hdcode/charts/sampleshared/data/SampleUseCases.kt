package dev.hdcode.charts.sampleshared.data

import dev.hdcode.charts.sampleshared.data.impl.DefaultBarSampleUseCase
import dev.hdcode.charts.sampleshared.data.impl.DefaultHistogramSampleUseCase
import dev.hdcode.charts.sampleshared.data.impl.DefaultLineSampleUseCase
import dev.hdcode.charts.sampleshared.data.impl.DefaultMultiLineSampleUseCase
import dev.hdcode.charts.sampleshared.data.impl.DefaultPieSampleUseCase
import dev.hdcode.charts.sampleshared.data.impl.DefaultRadarSampleUseCase
import dev.hdcode.charts.sampleshared.data.impl.DefaultStackedAreaSampleUseCase
import dev.hdcode.charts.sampleshared.data.impl.DefaultStackedBarSampleUseCase

fun pieSampleUseCase(): PieSampleUseCase = DefaultPieSampleUseCase()

fun lineSampleUseCase(): LineSampleUseCase = DefaultLineSampleUseCase()

fun barSampleUseCase(): BarSampleUseCase = DefaultBarSampleUseCase()

fun histogramSampleUseCase(): HistogramSampleUseCase = DefaultHistogramSampleUseCase()

fun multiLineSampleUseCase(): MultiLineSampleUseCase = DefaultMultiLineSampleUseCase()

fun stackedBarSampleUseCase(): StackedBarSampleUseCase = DefaultStackedBarSampleUseCase()

fun stackedAreaSampleUseCase(): StackedAreaSampleUseCase = DefaultStackedAreaSampleUseCase()

fun radarSampleUseCase(): RadarSampleUseCase = DefaultRadarSampleUseCase()
