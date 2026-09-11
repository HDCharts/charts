package dev.hdcode.charts.sampleshared.data.impl

import dev.hdcode.charts.sampleshared.data.BarSampleUseCase
import io.github.dautovicharis.charts.model.ChartData
import io.github.dautovicharis.charts.model.toChartData

internal class DefaultBarSampleUseCase : BarSampleUseCase {
    companion object {
        private const val DEFAULT_TITLE = "Daily Net Cash Flow"
        private const val DEFAULT_POINTS = 120
        private val DEFAULT_RANGE = -100..100
    }

    override fun initialBarDataSet(): ChartData =
        listOf(45.0, -12.0, 38.0, 27.0, -19.0, 42.0, 31.0).toChartData(
            categories = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"),
            seriesName = DEFAULT_TITLE,
        )

    override fun barDefaultPoints(): Int = DEFAULT_POINTS

    override fun barDefaultRange(): IntRange = DEFAULT_RANGE

    override fun barDataSet(
        points: Int,
        range: IntRange,
    ): ChartData {
        val safePoints = points.coerceAtLeast(2)
        val values = List(safePoints) { range.random().toDouble() }
        val labels = List(safePoints) { index -> (index + 1).toString() }
        return values.toChartData(
            categories = labels,
            seriesName = DEFAULT_TITLE,
        )
    }
}
