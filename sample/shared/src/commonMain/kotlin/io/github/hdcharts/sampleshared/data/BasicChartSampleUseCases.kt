package io.github.hdcharts.sampleshared.data

import io.github.hdcharts.core.model.ChartData

// Sample data for the demo app (the chart view models in sample/app) and the screenshot tests
// (sample/androidApp/src/screenshotTest). `deterministic` returns the same data for the same arguments;
// `random` builds new data each time the demo refreshes.

private const val DEFAULT_PIE_SLICES = 6

/** Slices are named "Segment 1", "Segment 2", and so on. */
interface PieSampleUseCase {
    /** Values n, n-1, ..., 1 for n [slices]. Demo start data and PieChartScreenshotTest. */
    fun deterministic(slices: Int = DEFAULT_PIE_SLICES): PieSampleData

    /** Demo refresh: the slices of [deterministic] with random values. */
    fun random(slices: Int = DEFAULT_PIE_SLICES): PieSampleData
}

private const val DEFAULT_LINE_POINTS = 30

/** LineChartScreenshotTest only. The line demo uses [LiveLatencyTimelineUseCase]. */
interface LineSampleUseCase {
    /** Daily values with a yearly season, or monthly values crossing zero when [signed]. */
    fun deterministic(
        points: Int = DEFAULT_LINE_POINTS,
        signed: Boolean = false,
    ): ChartData
}

private const val DEFAULT_BAR_POINTS = 12

interface BarSampleUseCase {
    /** Daily values, crossing zero when [signed]. BarChartScreenshotTest. */
    fun deterministic(
        points: Int = DEFAULT_BAR_POINTS,
        signed: Boolean = false,
    ): ChartData

    /** Demo: start data and refresh. */
    fun random(
        points: Int,
        range: IntRange,
    ): ChartData
}

private const val DEFAULT_HISTOGRAM_BINS = 20

interface HistogramSampleUseCase {
    /** Response times from 0 to 500ms in [bins] equal bins. HistogramChartScreenshotTest. */
    fun deterministic(bins: Int = DEFAULT_HISTOGRAM_BINS): ChartData

    /** Demo: start data and refresh. */
    fun random(
        points: Int,
        range: IntRange,
    ): ChartData
}

private const val DEFAULT_RINGS = 3

/** RingGaugeChartScreenshotTest only, until the gauge has a demo. Rings are named "Ring 1", "Ring 2", and so on. */
interface RingGaugeSampleUseCase {
    /**
     * A 0 to 100 range, or a -20 to 40 range when [signed]: the first ring past its end and the third
     * below zero.
     */
    fun deterministic(
        rings: Int = DEFAULT_RINGS,
        signed: Boolean = false,
    ): RingGaugeSampleData
}
