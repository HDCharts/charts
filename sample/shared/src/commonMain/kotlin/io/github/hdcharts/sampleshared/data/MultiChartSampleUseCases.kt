package io.github.hdcharts.sampleshared.data

// Sample data for the demo app (the chart view models in sample/app) and the screenshot tests
// (sample/androidApp/src/screenshotTest). `deterministic` returns the same data for the same arguments;
// `random` builds new data each time the demo refreshes.

private const val DEFAULT_MULTI_LINE_POINTS = 12

/**
 * MultiSeriesLineChartScreenshotTest and HeroChartScreenshotTest only. The multi-line demo uses
 * [LiveLatencyTimelineUseCase].
 */
interface MultiLineSampleUseCase {
    /** A year of revenue per channel: monthly for up to 12 [points], spread over days beyond that. */
    fun deterministic(points: Int = DEFAULT_MULTI_LINE_POINTS): MultiLineSampleData

    /** The chart on the charts-docs landing page. Keep it stable. */
    fun hero(): MultiLineSampleData
}

private const val DEFAULT_STACKED_BAR_POINTS = 8

interface StackedBarSampleUseCase {
    /** Quarterly revenue per product, ending in Q4 2025. Demo start data and StackedBarChartScreenshotTest. */
    fun deterministic(points: Int = DEFAULT_STACKED_BAR_POINTS): StackedBarSampleData

    /** Demo refresh. */
    fun random(
        points: Int,
        range: IntRange,
    ): StackedBarSampleData
}

private const val DEFAULT_STACKED_AREA_POINTS = 24

interface StackedAreaSampleUseCase {
    /** Monthly subscribers per plan, ending in December 2025. Demo start data and StackedAreaChartScreenshotTest. */
    fun deterministic(points: Int = DEFAULT_STACKED_AREA_POINTS): StackedAreaSampleData

    /** Demo refresh. */
    fun random(
        points: Int,
        range: IntRange,
    ): StackedAreaSampleData
}

private const val DEFAULT_RADAR_SERIES = 3

/** Series are named "Series 1", "Series 2", and so on, over six axes named "Axis 1" to "Axis 6". */
interface RadarSampleUseCase {
    /** Scores from 40 to 100. Demo start data, RadarChartScreenshotTest, and MultiSeriesRadarChartScreenshotTest. */
    fun deterministic(series: Int = DEFAULT_RADAR_SERIES): RadarSampleData

    /** Demo refresh: the series of [deterministic] with random scores. */
    fun random(series: Int = DEFAULT_RADAR_SERIES): RadarSampleData
}
