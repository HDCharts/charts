package dev.hdcode.charts.sampleshared.data.impl

import dev.hdcode.charts.sampleshared.data.LineSampleUseCase
import io.github.dautovicharis.charts.model.ChartData
import io.github.dautovicharis.charts.model.toChartData

internal class DefaultLineSampleUseCase : LineSampleUseCase {
    companion object {
        private const val DEFAULT_TITLE = "Daily Support Tickets"
        private val REFRESH_RANGE = 10..100
    }

    private val defaultValues = listOf(42f, 38f, 45f, 51f, 47f, 54f, 49f)
    private val defaultLabels = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")

    override fun initialLineDataSet(): ChartData =
        defaultValues
            .map { it.toDouble() }
            .toChartData(categories = defaultLabels, seriesName = DEFAULT_TITLE)

    override fun lineRefreshRange(): IntRange = REFRESH_RANGE

    override fun lineRefreshPointsCount(): Int = defaultValues.size

    override fun lineDataSet(
        range: IntRange,
        numOfPoints: IntRange,
    ): ChartData {
        val points = numOfPoints.random()
        val values = List(points) { range.random() }
        return values
            .map { it.toDouble() }
            .toChartData(categories = labelsForPoints(points), seriesName = DEFAULT_TITLE)
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
