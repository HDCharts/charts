package dev.hdcode.charts.sampleshared.data.impl

import dev.hdcode.charts.sampleshared.data.HistogramSampleUseCase
import io.github.dautovicharis.charts.model.ChartData
import io.github.dautovicharis.charts.model.toChartData

internal class DefaultHistogramSampleUseCase : HistogramSampleUseCase {
    companion object {
        private const val DEFAULT_TITLE = "Request Duration Distribution"
        private const val DEFAULT_POINTS = 60
        private val DEFAULT_RANGE = 0..120
    }

    override fun initialHistogramDataSet(): ChartData =
        listOf(3.0, 6.0, 11.0, 16.0, 14.0, 9.0, 5.0).toChartData(
            categories = listOf("0-50ms", "50-100ms", "100-150ms", "150-200ms", "200-250ms", "250-300ms", "300ms+"),
            seriesName = DEFAULT_TITLE,
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
            seriesName = DEFAULT_TITLE,
        )
    }
}
