package dev.hdcode.charts.sampleshared.data.impl

import dev.hdcode.charts.sampleshared.data.RadarCustomSampleData
import dev.hdcode.charts.sampleshared.data.RadarSampleData
import dev.hdcode.charts.sampleshared.data.RadarSampleUseCase
import io.github.dautovicharis.charts.model.ChartData
import io.github.dautovicharis.charts.model.ChartSeries
import io.github.dautovicharis.charts.model.chartDataOf
import io.github.dautovicharis.charts.model.toChartData

internal class DefaultRadarSampleUseCase : RadarSampleUseCase {
    companion object {
        private const val DEFAULT_TITLE = "Platform Readiness Score"
        private val REFRESH_RANGE = 30..100
    }

    private val radarCategories =
        listOf(
            "Performance",
            "Reliability",
            "Usability",
            "Security",
            "Scalability",
            "Observability",
        )
    private val radarDefaultValues = listOf(84.0, 79.0, 76.0, 88.0, 82.0, 74.0)
    private val radarBasicItems =
        listOf(
            "Release 2.2" to listOf(80.0, 75.0, 72.0, 85.0, 79.0, 70.0),
            "Release 2.3" to listOf(86.0, 82.0, 78.0, 89.0, 84.0, 77.0),
        )
    private val radarInitialItems =
        listOf(
            "Android App" to listOf(88.0, 81.0, 79.0, 90.0, 83.0, 76.0),
            "iOS App" to listOf(84.0, 86.0, 82.0, 88.0, 80.0, 79.0),
            "Web App" to listOf(78.0, 74.0, 85.0, 83.0, 88.0, 84.0),
        )
    private val radarEdgeValues = listOf(40.0, 55.0, 100.0, 45.0, 70.0, 60.0)

    override fun initialRadarSample(): RadarSampleData =
        RadarSampleData(
            basicData =
                radarBasicItems.toChartData(categories = radarCategories),
            customData =
                radarInitialItems.toChartData(categories = radarCategories),
            seriesKeys = radarInitialItems.map { it.first },
            title = DEFAULT_TITLE,
        )

    override fun initialRadarDefaultData(): ChartData =
        chartDataOf(
            categories = radarCategories,
            ChartSeries(name = "Current", values = radarDefaultValues),
        )

    override fun initialRadarEdgeData(): ChartData =
        chartDataOf(
            categories = radarCategories,
            ChartSeries(name = "Edge", values = radarEdgeValues),
        )

    override fun initialRadarMultiNoCategoriesData(): ChartData = radarInitialItems.toChartData()

    override fun radarRefreshRange(): IntRange = REFRESH_RANGE

    override fun radarDefaultData(range: IntRange): ChartData {
        val min = range.first.toDouble()
        val max = range.last.toDouble()
        val newValues =
            radarDefaultValues.map { base ->
                (base + (-10..10).random()).coerceIn(min, max)
            }
        return chartDataOf(
            categories = radarCategories,
            ChartSeries(name = "Current", values = newValues),
        )
    }

    override fun radarBasicData(range: IntRange): ChartData {
        val min = range.first.toDouble()
        val max = range.last.toDouble()
        val newItems =
            radarBasicItems.map { (name, values) ->
                name to values.map { base -> (base + (-10..10).random()).coerceIn(min, max) }
            }
        return newItems.toChartData(categories = radarCategories)
    }

    override fun radarCustomSample(range: IntRange): RadarCustomSampleData {
        val newItems =
            radarInitialItems.map { (name, values) ->
                name to values.map { range.random().toDouble() }
            }
        val data =
            newItems.toChartData(categories = radarCategories)
        return RadarCustomSampleData(
            data = data,
            seriesKeys = newItems.map { it.first },
        )
    }
}
