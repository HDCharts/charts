package io.github.hdcharts.sampleshared.data.impl

import io.github.hdcharts.charts.model.ChartData
import io.github.hdcharts.charts.model.ChartSeries
import io.github.hdcharts.charts.model.chartDataOf
import io.github.hdcharts.charts.model.toChartData
import io.github.hdcharts.sampleshared.data.RadarSampleData
import io.github.hdcharts.sampleshared.data.RadarSampleUseCase

internal class DefaultRadarSampleUseCase : RadarSampleUseCase {
    companion object {
        private const val TITLE = "Platform Readiness Score"
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

    // Scores out of 100. The radar scales from the lowest to the highest value in the data,
    // so the scores spread wide enough for low and high to read at a glance.
    private val radarSingleSeriesValues = listOf(92.0, 64.0, 78.0, 97.0, 48.0, 71.0)
    private val radarItems =
        listOf(
            "Android App" to listOf(91.0, 72.0, 68.0, 85.0, 88.0, 54.0),
            "iOS App" to listOf(84.0, 93.0, 90.0, 94.0, 61.0, 48.0),
            "Web App" to listOf(62.0, 70.0, 86.0, 73.0, 95.0, 90.0),
        )

    override fun initialRadarSample(): RadarSampleData = radarSample(radarItems, radarCategories)

    override fun initialRadarNoCategoriesSample(): RadarSampleData = radarSample(radarItems, emptyList())

    override fun initialSingleSeriesRadarData(): ChartData =
        chartDataOf(
            categories = radarCategories,
            ChartSeries(name = TITLE, values = radarSingleSeriesValues),
        )

    override fun radarRefreshRange(): IntRange = REFRESH_RANGE

    override fun radarSample(range: IntRange): RadarSampleData =
        radarSample(
            items = radarItems.map { (name, values) -> name to values.map { range.random().toDouble() } },
            categories = radarCategories,
        )

    private fun radarSample(
        items: List<Pair<String, List<Double>>>,
        categories: List<String>,
    ): RadarSampleData =
        RadarSampleData(
            data = items.toChartData(categories = categories),
            seriesKeys = items.map { it.first },
            title = TITLE,
        )
}
