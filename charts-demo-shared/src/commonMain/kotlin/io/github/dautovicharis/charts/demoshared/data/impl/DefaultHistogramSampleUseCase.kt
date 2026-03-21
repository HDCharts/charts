package io.github.dautovicharis.charts.demoshared.data.impl

import io.github.dautovicharis.charts.demoshared.data.HistogramSampleUseCase
import io.github.dautovicharis.charts.model.ChartDataSet
import io.github.dautovicharis.charts.model.toChartDataSet

internal class DefaultHistogramSampleUseCase : HistogramSampleUseCase {
    companion object {
        private const val DEFAULT_TITLE = "Request Duration Distribution"
        private const val DEFAULT_POINTS = 60
        private val DEFAULT_RANGE = 0..120
    }

    override fun initialHistogramDataSet(): ChartDataSet =
        listOf(3f, 6f, 11f, 16f, 14f, 9f, 5f).toChartDataSet(
            title = DEFAULT_TITLE,
            labels = listOf("0-50ms", "50-100ms", "100-150ms", "150-200ms", "200-250ms", "250-300ms", "300ms+"),
        )

    override fun histogramDefaultPoints(): Int = DEFAULT_POINTS

    override fun histogramDefaultRange(): IntRange = DEFAULT_RANGE

    override fun histogramDataSet(
        points: Int,
        range: IntRange,
    ): ChartDataSet {
        val safePoints = points.coerceAtLeast(2)
        val safeRangeStart = range.first.coerceAtLeast(0)
        val safeRangeEnd = range.last.coerceAtLeast(safeRangeStart)
        val values = List(safePoints) { (safeRangeStart..safeRangeEnd).random().toFloat() }
        val labels = List(safePoints) { index -> "B${index + 1}" }
        return values.toChartDataSet(
            title = DEFAULT_TITLE,
            labels = labels,
        )
    }
}
