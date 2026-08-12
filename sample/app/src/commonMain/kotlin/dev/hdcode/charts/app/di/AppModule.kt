package dev.hdcode.charts.app.di

import dev.hdcode.charts.app.ChartGalleryViewModel
import dev.hdcode.charts.app.MainViewModel
import dev.hdcode.charts.app.data.ChartPreviewUseCase
import dev.hdcode.charts.app.data.LiveLatencyTimelineUseCase
import dev.hdcode.charts.app.data.impl.DefaultChartPreviewUseCase
import dev.hdcode.charts.app.data.impl.DefaultLiveLatencyTimelineUseCase
import dev.hdcode.charts.app.demo.bar.BarChartViewModel
import dev.hdcode.charts.app.demo.histogram.HistogramChartViewModel
import dev.hdcode.charts.app.demo.line.LineChartViewModel
import dev.hdcode.charts.app.demo.multiline.MultiLineChartViewModel
import dev.hdcode.charts.app.demo.pie.PieChartViewModel
import dev.hdcode.charts.app.demo.radar.RadarChartViewModel
import dev.hdcode.charts.app.demo.stackedarea.StackedAreaChartViewModel
import dev.hdcode.charts.app.demo.stackedbar.StackedBarChartViewModel
import dev.hdcode.charts.sampleshared.data.BarSampleUseCase
import dev.hdcode.charts.sampleshared.data.HistogramSampleUseCase
import dev.hdcode.charts.sampleshared.data.LineSampleUseCase
import dev.hdcode.charts.sampleshared.data.MultiLineSampleUseCase
import dev.hdcode.charts.sampleshared.data.PieSampleUseCase
import dev.hdcode.charts.sampleshared.data.RadarSampleUseCase
import dev.hdcode.charts.sampleshared.data.StackedAreaSampleUseCase
import dev.hdcode.charts.sampleshared.data.StackedBarSampleUseCase
import dev.hdcode.charts.sampleshared.data.barSampleUseCase
import dev.hdcode.charts.sampleshared.data.histogramSampleUseCase
import dev.hdcode.charts.sampleshared.data.lineSampleUseCase
import dev.hdcode.charts.sampleshared.data.multiLineSampleUseCase
import dev.hdcode.charts.sampleshared.data.pieSampleUseCase
import dev.hdcode.charts.sampleshared.data.radarSampleUseCase
import dev.hdcode.charts.sampleshared.data.stackedAreaSampleUseCase
import dev.hdcode.charts.sampleshared.data.stackedBarSampleUseCase
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
