package io.github.hdcharts.app.di

import io.github.hdcharts.app.ChartGalleryViewModel
import io.github.hdcharts.app.MainViewModel
import io.github.hdcharts.app.data.ChartPreviewUseCase
import io.github.hdcharts.app.data.LiveLatencyTimelineUseCase
import io.github.hdcharts.app.data.impl.DefaultChartPreviewUseCase
import io.github.hdcharts.app.data.impl.DefaultLiveLatencyTimelineUseCase
import io.github.hdcharts.app.demo.bar.BarChartViewModel
import io.github.hdcharts.app.demo.histogram.HistogramChartViewModel
import io.github.hdcharts.app.demo.line.LineChartViewModel
import io.github.hdcharts.app.demo.multiline.MultiLineChartViewModel
import io.github.hdcharts.app.demo.pie.PieChartViewModel
import io.github.hdcharts.app.demo.radar.RadarChartViewModel
import io.github.hdcharts.app.demo.stackedarea.StackedAreaChartViewModel
import io.github.hdcharts.app.demo.stackedbar.StackedBarChartViewModel
import io.github.hdcharts.sampleshared.data.BarSampleUseCase
import io.github.hdcharts.sampleshared.data.HistogramSampleUseCase
import io.github.hdcharts.sampleshared.data.LineSampleUseCase
import io.github.hdcharts.sampleshared.data.MultiLineSampleUseCase
import io.github.hdcharts.sampleshared.data.PieSampleUseCase
import io.github.hdcharts.sampleshared.data.RadarSampleUseCase
import io.github.hdcharts.sampleshared.data.StackedAreaSampleUseCase
import io.github.hdcharts.sampleshared.data.StackedBarSampleUseCase
import io.github.hdcharts.sampleshared.data.barSampleUseCase
import io.github.hdcharts.sampleshared.data.histogramSampleUseCase
import io.github.hdcharts.sampleshared.data.lineSampleUseCase
import io.github.hdcharts.sampleshared.data.multiLineSampleUseCase
import io.github.hdcharts.sampleshared.data.pieSampleUseCase
import io.github.hdcharts.sampleshared.data.radarSampleUseCase
import io.github.hdcharts.sampleshared.data.stackedAreaSampleUseCase
import io.github.hdcharts.sampleshared.data.stackedBarSampleUseCase
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val appModule =
    module {
        single<ChartPreviewUseCase> { DefaultChartPreviewUseCase() }
        single<LiveLatencyTimelineUseCase> { DefaultLiveLatencyTimelineUseCase() }
        single<PieSampleUseCase> { pieSampleUseCase() }
        single<LineSampleUseCase> { lineSampleUseCase() }
        single<MultiLineSampleUseCase> { multiLineSampleUseCase() }
        single<BarSampleUseCase> { barSampleUseCase() }
        single<HistogramSampleUseCase> { histogramSampleUseCase() }
        single<StackedBarSampleUseCase> { stackedBarSampleUseCase() }
        single<StackedAreaSampleUseCase> { stackedAreaSampleUseCase() }
        single<RadarSampleUseCase> { radarSampleUseCase() }
        viewModel { PieChartViewModel(get()) }
        viewModel { ChartGalleryViewModel(get()) }
        viewModel { MainViewModel() }
        viewModel { LineChartViewModel(get()) }
        viewModel { MultiLineChartViewModel(get()) }
        viewModel { BarChartViewModel(get()) }
        viewModel { HistogramChartViewModel(get()) }
        viewModel { StackedBarChartViewModel(get()) }
        viewModel { StackedAreaChartViewModel(get()) }
        viewModel { RadarChartViewModel(get()) }
    }
