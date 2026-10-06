package io.github.hdcharts.sampleshared.data.impl

import io.github.hdcharts.core.model.ChartData
import io.github.hdcharts.core.model.toChartData
import io.github.hdcharts.sampleshared.data.HistogramSampleUseCase
import kotlin.random.Random

internal class DefaultHistogramSampleUseCase : HistogramSampleUseCase {
    companion object {
        private const val TITLE = "API Response Time"
        private const val SPAN_MS = 500

        // At 20 bins: the peak in bin 4 holds 1,840 requests.
        private const val PEAK_SHARE = 0.2
        private const val PEAK_TOTAL = 1_840.0 * 20
    }

    override fun deterministic(bins: Int): ChartData {
        val counts =
            SampleSignals.longTailCounts(
                count = bins,
                peakBin = bins * PEAK_SHARE,
                peak = PEAK_TOTAL / bins,
                spread = 0.62,
                random = Random(17),
                noise = 0.05,
            )
        return SampleSignals.rounded(counts).toChartData(
            categories = List(bins) { bin -> "${bin * SPAN_MS / bins}ms" },
            seriesName = TITLE,
        )
    }

    override fun random(
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
}
