package io.github.hdcharts.sampleshared.data.impl

import io.github.hdcharts.core.model.ChartData
import io.github.hdcharts.core.model.toChartData
import io.github.hdcharts.sampleshared.data.BarSampleUseCase
import kotlin.random.Random

internal class DefaultBarSampleUseCase : BarSampleUseCase {
    companion object {
        private const val TITLE = "Net Cash Flow (\$K)"
        private const val MARCH = 2
    }

    override fun deterministic(
        points: Int,
        signed: Boolean,
    ): ChartData {
        val values =
            SampleSignals.trend(
                count = points,
                start = if (signed) -40.0 else 180.0,
                end = if (signed) 90.0 else 310.0,
                random = Random(31),
                cycleAmplitude = 38.0,
                noise = 22.0,
            )
        return SampleSignals
            .rounded(values)
            .toChartData(categories = SampleLabels.days(points, startMonth = MARCH), seriesName = TITLE)
    }

    override fun random(
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
