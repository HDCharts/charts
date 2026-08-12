package dev.hdcode.charts.sampleshared.data.impl

import dev.hdcode.charts.sampleshared.data.LineSampleUseCase
import io.github.dautovicharis.charts.model.ChartDataSet
import io.github.dautovicharis.charts.model.toChartDataSet

internal class DefaultLineSampleUseCase : LineSampleUseCase {
    companion object {
        private const val DEFAULT_TITLE = "Daily Support Tickets"
        private val REFRESH_RANGE = 10..100
    }

    private val defaultValues = listOf(42f, 38f, 45f, 51f, 47f, 54f, 49f)
    private val defaultLabels = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")

    override fun initialLineDataSet(): ChartDataSet =
        defaultValues.toChartDataSet(
            title = DEFAULT_TITLE,
            labels = defaultLabels,
        )

    override fun lineRefreshRange(): IntRange = REFRESH_RANGE

    override fun lineRefreshPointsCount(): Int = defaultValues.size

    override fun lineDataSet(
        range: IntRange,
        numOfPoints: IntRange,
    ): ChartDataSet {
        val points = numOfPoints.random()
        val values = List(points) { range.random() }
        return values.toChartDataSet(
            title = DEFAULT_TITLE,
            labels = labelsForPoints(points),
        )
    }

    private fun labelsForPoints(points: Int): List<String> {
        if (points <= defaultLabels.size) {
            return defaultLabels.take(points)
        }
        val extrasCount = points - defaultLabels.size
        val extras = List(extrasCount) { index -> "Day ${defaultLabels.size + index + 1}" }
        return defaultLabels + extras
    }
}
