package io.github.hdcharts.sampleshared.data.impl

import io.github.hdcharts.core.model.ChartData
import io.github.hdcharts.core.model.toChartData
import io.github.hdcharts.sampleshared.data.HistogramSampleUseCase
import kotlin.random.Random

internal class DefaultHistogramSampleUseCase : HistogramSampleUseCase {
    companion object {
        private const val TITLE = "API Response Time"
        private const val INITIAL_BINS = 20
        private const val INITIAL_BIN_WIDTH_MS = 25

        // More bins than fit at the minimum bar width, even on a tablet.
        private const val DENSE_BINS = 150
        private const val DENSE_BIN_WIDTH_MS = 4
        private const val DEFAULT_POINTS = 60
        private val DEFAULT_RANGE = 0..120
    }

    override fun initialHistogramDataSet(): ChartData =
        latencyHistogram(
            bins = INITIAL_BINS,
            binWidthMs = INITIAL_BIN_WIDTH_MS,
            peakBin = 4.0,
            peak = 1_840.0,
            seed = 17,
            noise = 0.05,
        )

    override fun initialDenseHistogramDataSet(): ChartData =
        latencyHistogram(
            bins = DENSE_BINS,
            binWidthMs = DENSE_BIN_WIDTH_MS,
            peakBin = 25.0,
            peak = 310.0,
            seed = 19,
            noise = 0.08,
        )

    override fun histogramDefaultPoints(): Int = DEFAULT_POINTS

    override fun histogramDefaultRange(): IntRange = DEFAULT_RANGE

    override fun histogramDataSet(
        points: Int,
        range: IntRange,
    ): ChartData {
        val safePoints = points.coerceAtLeast(2)
        val safeRangeStart = range.first.coerceAtLeast(0)
        val safeRangeEnd = range.last.coerceAtLeast(safeRangeStart)
        val values = List(safePoints) { (safeRangeStart..safeRangeEnd).random().toDouble() }
        val labels = List(safePoints) { index -> "B${index + 1}" }
        return values.toChartData(
            categories = labels,
            seriesName = TITLE,
        )
    }

    private fun latencyHistogram(
        bins: Int,
        binWidthMs: Int,
        peakBin: Double,
        peak: Double,
        seed: Int,
        noise: Double,
    ): ChartData {
        val counts =
            SampleSignals.longTailCounts(
                count = bins,
                peakBin = peakBin,
                peak = peak,
                spread = 0.62,
                random = Random(seed),
                noise = noise,
            )
        return SampleSignals.rounded(counts).toChartData(
            categories = List(bins) { bin -> "${bin * binWidthMs}ms" },
            seriesName = TITLE,
        )
    }
}
