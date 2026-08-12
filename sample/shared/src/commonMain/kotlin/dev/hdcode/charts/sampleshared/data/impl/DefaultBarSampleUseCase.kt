package dev.hdcode.charts.sampleshared.data.impl

import dev.hdcode.charts.sampleshared.data.BarSampleUseCase
import io.github.dautovicharis.charts.model.ChartDataSet
import io.github.dautovicharis.charts.model.toChartDataSet

internal class DefaultBarSampleUseCase : BarSampleUseCase {
    companion object {
        private const val DEFAULT_TITLE = "Daily Net Cash Flow"
        private const val DEFAULT_POINTS = 120
        private val DEFAULT_RANGE = -100..100
    }

    override fun initialBarDataSet(): ChartDataSet =
        listOf(45f, -12f, 38f, 27f, -19f, 42f, 31f).toChartDataSet(
            title = DEFAULT_TITLE,
            labels = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"),
        )

    override fun barDefaultPoints(): Int = DEFAULT_POINTS

    override fun barDefaultRange(): IntRange = DEFAULT_RANGE

    override fun barDataSet(
        points: Int,
        range: IntRange,
    ): ChartDataSet {
        val safePoints = points.coerceAtLeast(2)
        val values = List(safePoints) { range.random().toFloat() }
        val labels = List(safePoints) { index -> (index + 1).toString() }
        return values.toChartDataSet(
            title = DEFAULT_TITLE,
            labels = labels,
        )
    }
}
