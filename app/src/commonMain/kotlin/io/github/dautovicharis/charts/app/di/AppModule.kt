package io.github.dautovicharis.charts.app.di

import io.github.dautovicharis.charts.app.ChartGalleryViewModel
import io.github.dautovicharis.charts.app.MainViewModel
import io.github.dautovicharis.charts.app.data.ChartPreviewUseCase
import io.github.dautovicharis.charts.app.data.LiveLatencyTimelineUseCase
import io.github.dautovicharis.charts.app.data.impl.DefaultChartPreviewUseCase
import io.github.dautovicharis.charts.app.data.impl.DefaultLiveLatencyTimelineUseCase
import io.github.dautovicharis.charts.app.demo.bar.BarChartViewModel
import io.github.dautovicharis.charts.app.demo.histogram.HistogramChartViewModel
import io.github.dautovicharis.charts.app.demo.line.LineChartViewModel
import io.github.dautovicharis.charts.app.demo.multiline.MultiLineChartViewModel
import io.github.dautovicharis.charts.app.demo.pie.PieChartViewModel
import io.github.dautovicharis.charts.app.demo.radar.RadarChartViewModel
import io.github.dautovicharis.charts.app.demo.stackedarea.StackedAreaChartViewModel
import io.github.dautovicharis.charts.app.demo.stackedbar.StackedBarChartViewModel
import io.github.dautovicharis.charts.demoshared.data.BarSampleUseCase
import io.github.dautovicharis.charts.demoshared.data.HistogramSampleUseCase
import io.github.dautovicharis.charts.demoshared.data.LineSampleUseCase
import io.github.dautovicharis.charts.demoshared.data.MultiLineSampleUseCase
import io.github.dautovicharis.charts.demoshared.data.PieSampleUseCase
import io.github.dautovicharis.charts.demoshared.data.RadarSampleUseCase
import io.github.dautovicharis.charts.demoshared.data.StackedAreaSampleUseCase
import io.github.dautovicharis.charts.demoshared.data.StackedBarSampleUseCase
import io.github.dautovicharis.charts.demoshared.data.barSampleUseCase
import io.github.dautovicharis.charts.demoshared.data.histogramSampleUseCase
import io.github.dautovicharis.charts.demoshared.data.lineSampleUseCase
import io.github.dautovicharis.charts.demoshared.data.multiLineSampleUseCase
import io.github.dautovicharis.charts.demoshared.data.pieSampleUseCase
import io.github.dautovicharis.charts.demoshared.data.radarSampleUseCase
import io.github.dautovicharis.charts.demoshared.data.stackedAreaSampleUseCase
import io.github.dautovicharis.charts.demoshared.data.stackedBarSampleUseCase
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
