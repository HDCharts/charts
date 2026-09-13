package dev.hdcode.charts.sampleshared.data.impl

import dev.hdcode.charts.sampleshared.data.StackedAreaSampleData
import dev.hdcode.charts.sampleshared.data.StackedAreaSampleUseCase
import io.github.dautovicharis.charts.model.ChartData
import io.github.dautovicharis.charts.model.toChartData

internal class DefaultStackedAreaSampleUseCase : StackedAreaSampleUseCase {
    companion object {
        private const val DEFAULT_TITLE = "Monthly Active Subscribers by Plan"
        private val REFRESH_RANGE = 100..1000
    }

    private val stackedAreaCategories = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun")
    private val stackedAreaSeries =
        listOf(
            "Free Plan" to listOf(620.0, 650.0, 690.0, 720.0, 760.0, 800.0),
            "Standard Plan" to listOf(240.0, 260.0, 285.0, 310.0, 340.0, 365.0),
            "Premium Plan" to listOf(90.0, 95.0, 105.0, 118.0, 130.0, 142.0),
        )
    private val noCategoriesSeries =
        listOf(
            "Free Plan" to listOf(540.0, 600.0, 660.0, 720.0),
            "Standard Plan" to listOf(200.0, 230.0, 260.0, 290.0),
            "Premium Plan" to listOf(80.0, 92.0, 105.0, 118.0),
        )

    override fun initialStackedAreaSample(): StackedAreaSampleData =
        StackedAreaSampleData(
            data = stackedAreaData(stackedAreaSeries, stackedAreaCategories),
            seriesKeys = stackedAreaSeries.map { it.first },
            title = DEFAULT_TITLE,
        )

    override fun initialStackedAreaNoCategoriesData(): StackedAreaSampleData =
        StackedAreaSampleData(
            data = stackedAreaData(noCategoriesSeries, emptyList()),
            seriesKeys = noCategoriesSeries.map { it.first },
            title = "Subscriber Mix (No Time Labels)",
        )

    override fun stackedAreaRefreshRange(): IntRange = REFRESH_RANGE

    override fun stackedAreaSample(range: IntRange): StackedAreaSampleData =
        stackedAreaSample(points = stackedAreaSeries.size, range = range)

    override fun stackedAreaSample(
        points: Int,
        range: IntRange,
    ): StackedAreaSampleData {
        val safePoints = points.coerceAtLeast(2)
        val safeRangeStart = minOf(range.first, range.last).coerceAtLeast(0)
        val safeRangeEnd = maxOf(range.first, range.last).coerceAtLeast(safeRangeStart)
        val safeRange = safeRangeStart..safeRangeEnd
        val categories = stackedAreaCategoriesFor(points = safePoints)
        val newSeries =
            stackedAreaSeries.map { (name, _) ->
                name to List(safePoints) { safeRange.random().toDouble() }
            }
        return StackedAreaSampleData(
            data = stackedAreaData(newSeries, categories),
            seriesKeys = newSeries.map { it.first },
            title = DEFAULT_TITLE,
        )
    }

    private fun stackedAreaData(
        series: List<Pair<String, List<Double>>>,
        categories: List<String>,
    ): ChartData = series.toChartData(categories = categories)

    private fun stackedAreaCategoriesFor(points: Int): List<String> {
        if (points <= stackedAreaCategories.size) {
            return stackedAreaCategories.take(points)
        }
        val extraCount = points - stackedAreaCategories.size
        val extras = List(extraCount) { index -> "P${stackedAreaCategories.size + index + 1}" }
        return stackedAreaCategories + extras
    }
}
