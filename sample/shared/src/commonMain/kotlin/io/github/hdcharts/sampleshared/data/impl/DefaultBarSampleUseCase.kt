package io.github.hdcharts.sampleshared.data.impl

import io.github.hdcharts.charts.model.ChartData
import io.github.hdcharts.charts.model.toChartData
import io.github.hdcharts.sampleshared.data.BarSampleUseCase
import kotlin.random.Random

internal class DefaultBarSampleUseCase : BarSampleUseCase {
    companion object {
        private const val TITLE = "Net Cash Flow (\$K)"
        private const val DENSE_DAYS = 90
        private const val MARCH = 2
        private const val DEFAULT_POINTS = 120
        private val DEFAULT_RANGE = -100..100
    }

    override fun initialBarDataSet(): ChartData =
        listOf(42.0, -18.0, 27.0, 61.0, 35.0, -9.0, -26.0, 14.0, 48.0, 72.0, 55.0, 96.0).toChartData(
            categories = SampleLabels.months(12),
            seriesName = TITLE,
        )

    override fun initialDenseBarDataSet(): ChartData {
        val values =
            SampleSignals.trend(
                count = DENSE_DAYS,
                start = 180.0,
                end = 310.0,
                random = Random(31),
                cycleAmplitude = 38.0,
                noise = 22.0,
            )
        return SampleSignals
            .rounded(values)
            .toChartData(categories = SampleLabels.days(DENSE_DAYS, startMonth = MARCH), seriesName = TITLE)
    }

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
            seriesName = TITLE,
        )
    }
}
