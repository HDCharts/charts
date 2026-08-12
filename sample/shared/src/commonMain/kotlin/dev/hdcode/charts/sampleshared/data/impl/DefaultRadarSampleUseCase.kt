package dev.hdcode.charts.sampleshared.data.impl

import dev.hdcode.charts.sampleshared.data.RadarCustomSampleData
import dev.hdcode.charts.sampleshared.data.RadarSampleData
import dev.hdcode.charts.sampleshared.data.RadarSampleUseCase
import io.github.dautovicharis.charts.model.ChartDataSet
import io.github.dautovicharis.charts.model.MultiChartDataSet
import io.github.dautovicharis.charts.model.toChartDataSet
import io.github.dautovicharis.charts.model.toMultiChartDataSet

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
    private val radarDefaultValues = listOf(84f, 79f, 76f, 88f, 82f, 74f)
    private val radarBasicItems =
        listOf(
            "Release 2.2" to listOf(80f, 75f, 72f, 85f, 79f, 70f),
            "Release 2.3" to listOf(86f, 82f, 78f, 89f, 84f, 77f),
        )
    private val radarInitialItems =
        listOf(
            "Android App" to listOf(88f, 81f, 79f, 90f, 83f, 76f),
            "iOS App" to listOf(84f, 86f, 82f, 88f, 80f, 79f),
            "Web App" to listOf(78f, 74f, 85f, 83f, 88f, 84f),
        )
    private val radarEdgeValues = listOf(40f, 55f, 100f, 45f, 70f, 60f)

    override fun initialRadarSample(): RadarSampleData =
        RadarSampleData(
            basicDataSet =
                radarBasicItems.toMultiChartDataSet(
                    title = DEFAULT_TITLE,
                    categories = radarCategories,
                ),
            customDataSet =
                radarInitialItems.toMultiChartDataSet(
                    title = DEFAULT_TITLE,
                    categories = radarCategories,
                ),
            seriesKeys = radarInitialItems.map { it.first },
        )

    override fun initialRadarDefaultDataSet(): ChartDataSet =
        radarDefaultValues.toChartDataSet(
            title = DEFAULT_TITLE,
            labels = radarCategories,
        )

    override fun initialRadarEdgeDataSet(): ChartDataSet =
        radarEdgeValues.toChartDataSet(
            title = "Stress-Test Profile",
            labels = radarCategories,
        )

    override fun initialRadarMultiNoCategoriesDataSet(): MultiChartDataSet =
        radarInitialItems.toMultiChartDataSet(title = DEFAULT_TITLE)

    override fun radarRefreshRange(): IntRange = REFRESH_RANGE

    override fun radarDefaultDataSet(range: IntRange): ChartDataSet {
        val min = range.first.toFloat()
        val max = range.last.toFloat()
        val newValues =
            radarDefaultValues.map { base ->
                (base + (-10..10).random()).coerceIn(min, max)
            }
        return newValues.toChartDataSet(
            title = DEFAULT_TITLE,
            labels = radarCategories,
        )
    }

    override fun radarBasicDataSet(range: IntRange): MultiChartDataSet {
        val min = range.first.toFloat()
        val max = range.last.toFloat()
        val newItems =
            radarBasicItems.map { (name, values) ->
                name to values.map { base -> (base + (-10..10).random()).coerceIn(min, max) }
            }
        return newItems.toMultiChartDataSet(
            title = DEFAULT_TITLE,
            categories = radarCategories,
        )
    }

    override fun radarCustomSample(range: IntRange): RadarCustomSampleData {
        val newItems =
            radarInitialItems.map { (name, values) ->
                name to values.map { range.random().toFloat() }
            }
        val dataSet =
            newItems.toMultiChartDataSet(
                title = DEFAULT_TITLE,
                categories = radarCategories,
            )
        return RadarCustomSampleData(
            dataSet = dataSet,
            seriesKeys = newItems.map { it.first },
        )
    }
}
