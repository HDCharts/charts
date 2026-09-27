package io.github.hdcharts.sampleshared.data

import io.github.hdcharts.charts.model.ChartData

// Sample data for the demo app (the chart view models in sample/app) and the screenshot tests
// (sample/androidApp/src/screenshotTest). Functions starting with `initial` return the same data on
// every call; the others build random data each time the demo refreshes.

interface PieSampleUseCase {
    /** Demo start data and PieChartScreenshotTest. */
    fun initialPieSample(): PieSampleData

    /** PieChartScreenshotTest only: a long tail of small slices. */
    fun initialManySlicesPieSample(): PieSampleData

    /** Demo refresh. */
    fun pieRefreshRange(): IntRange

    /** Demo refresh. */
    fun pieSample(
        range: IntRange,
        numOfPoints: IntRange,
    ): PieSampleData
}

/** LineChartScreenshotTest only. The line demo uses [LiveLatencyTimelineUseCase]. */
interface LineSampleUseCase {
    fun initialLineDataSet(): ChartData

    fun initialDenseLineDataSet(): ChartData

    /** Values below and above zero. */
    fun initialSignedLineDataSet(): ChartData
}

interface BarSampleUseCase {
    /** BarChartScreenshotTest. */
    fun initialBarDataSet(): ChartData

    /** BarChartScreenshotTest. */
    fun initialDenseBarDataSet(): ChartData

    /** Demo: start data and refresh. */
    fun barDefaultPoints(): Int

    /** Demo: start data and refresh. */
    fun barDefaultRange(): IntRange

    /** Demo: start data and refresh. */
    fun barDataSet(
        points: Int,
        range: IntRange,
    ): ChartData
}

interface HistogramSampleUseCase {
    /** HistogramChartScreenshotTest. */
    fun initialHistogramDataSet(): ChartData

    /** HistogramChartScreenshotTest. */
    fun initialDenseHistogramDataSet(): ChartData

    /** Demo: start data and refresh. */
    fun histogramDefaultPoints(): Int

    /** Demo: start data and refresh. */
    fun histogramDefaultRange(): IntRange

    /** Demo: start data and refresh. */
    fun histogramDataSet(
        points: Int,
        range: IntRange,
    ): ChartData
}
