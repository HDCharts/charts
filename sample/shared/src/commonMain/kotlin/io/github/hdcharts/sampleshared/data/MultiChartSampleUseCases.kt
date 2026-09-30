package io.github.hdcharts.sampleshared.data

import io.github.hdcharts.core.model.ChartData

// Sample data for the demo app (the chart view models in sample/app) and the screenshot tests
// (sample/androidApp/src/screenshotTest). Functions starting with `initial` return the same data on
// every call; the others build random data each time the demo refreshes.

/**
 * MultiSeriesLineChartScreenshotTest and HeroChartScreenshotTest only. The multi-line demo uses
 * [LiveLatencyTimelineUseCase].
 */
interface MultiLineSampleUseCase {
    fun initialMultiLineSample(): MultiLineSampleData

    fun initialMultiLineNoCategoriesSample(): MultiLineSampleData

    fun initialDenseMultiLineSample(): MultiLineSampleData

    /** The chart on the charts-docs landing page. Keep it stable. */
    fun initialHeroSample(): MultiLineSampleData
}

interface StackedBarSampleUseCase {
    /** Demo start data and StackedBarChartScreenshotTest. */
    fun initialStackedBarSample(): StackedBarSampleData

    /** StackedBarChartScreenshotTest. */
    fun initialDenseStackedBarSample(): StackedBarSampleData

    /** StackedBarChartScreenshotTest. */
    fun initialStackedBarNoCategoriesDataSet(): StackedBarSampleData

    /** Demo refresh. */
    fun stackedBarRefreshRange(): IntRange

    /** Demo refresh. */
    fun stackedBarSample(
        points: Int,
        range: IntRange,
    ): StackedBarSampleData
}

interface StackedAreaSampleUseCase {
    /** Demo start data and StackedAreaChartScreenshotTest. */
    fun initialStackedAreaSample(): StackedAreaSampleData

    /** StackedAreaChartScreenshotTest. */
    fun initialDenseStackedAreaSample(): StackedAreaSampleData

    /** StackedAreaChartScreenshotTest. */
    fun initialStackedAreaNoCategoriesData(): StackedAreaSampleData

    /** Demo refresh. */
    fun stackedAreaRefreshRange(): IntRange

    /** Demo refresh. */
    fun stackedAreaSample(
        points: Int,
        range: IntRange,
    ): StackedAreaSampleData
}

interface RadarSampleUseCase {
    /** Demo start data and MultiSeriesRadarChartScreenshotTest. */
    fun initialRadarSample(): RadarSampleData

    /** MultiSeriesRadarChartScreenshotTest. */
    fun initialRadarNoCategoriesSample(): RadarSampleData

    /** RadarChartScreenshotTest. */
    fun initialSingleSeriesRadarData(): ChartData

    /** Demo refresh. */
    fun radarRefreshRange(): IntRange

    /** Demo refresh. */
    fun radarSample(range: IntRange): RadarSampleData
}
