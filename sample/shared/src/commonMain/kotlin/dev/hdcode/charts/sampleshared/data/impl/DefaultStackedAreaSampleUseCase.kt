package dev.hdcode.charts.sampleshared.data.impl

import dev.hdcode.charts.sampleshared.data.StackedAreaSampleData
import dev.hdcode.charts.sampleshared.data.StackedAreaSampleUseCase
import io.github.dautovicharis.charts.model.MultiChartDataSet
import io.github.dautovicharis.charts.model.toMultiChartDataSet

internal class DefaultStackedAreaSampleUseCase : StackedAreaSampleUseCase {
    companion object {
        private const val DEFAULT_TITLE = "Monthly Active Subscribers by Plan"
        private const val DEFAULT_PREFIX = ""
        private val REFRESH_RANGE = 100..1000
    }

    private val stackedAreaCategories = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun")
    private val stackedAreaItems =
        listOf(
            "Free Plan" to listOf(620f, 650f, 690f, 720f, 760f, 800f),
            "Standard Plan" to listOf(240f, 260f, 285f, 310f, 340f, 365f),
            "Premium Plan" to listOf(90f, 95f, 105f, 118f, 130f, 142f),
        )
    private val noCategoriesItems =
        listOf(
            "Free Plan" to listOf(540f, 600f, 660f, 720f),
            "Standard Plan" to listOf(200f, 230f, 260f, 290f),
            "Premium Plan" to listOf(80f, 92f, 105f, 118f),
        )

    override fun initialStackedAreaSample(): StackedAreaSampleData =
        StackedAreaSampleData(
            dataSet =
                stackedAreaItems.toMultiChartDataSet(
                    title = DEFAULT_TITLE,
                    prefix = DEFAULT_PREFIX,
                    categories = stackedAreaCategories,
                ),
            seriesKeys = stackedAreaItems.map { it.first },
        )

    override fun initialStackedAreaNoCategoriesDataSet(): MultiChartDataSet =
        noCategoriesItems.toMultiChartDataSet(title = "Subscriber Mix (No Time Labels)")

    override fun stackedAreaRefreshRange(): IntRange = REFRESH_RANGE

    override fun stackedAreaSample(range: IntRange): StackedAreaSampleData =
        stackedAreaSample(points = stackedAreaCategories.size, range = range)

    override fun stackedAreaSample(
        points: Int,
        range: IntRange,
    ): StackedAreaSampleData {
        val safePoints = points.coerceAtLeast(2)
        val safeRangeStart = minOf(range.first, range.last).coerceAtLeast(0)
        val safeRangeEnd = maxOf(range.first, range.last).coerceAtLeast(safeRangeStart)
        val safeRange = safeRangeStart..safeRangeEnd
        val categories = stackedAreaCategoriesFor(points = safePoints)
        val newItems =
            stackedAreaItems.map { (name, _) ->
                name to List(safePoints) { safeRange.random().toFloat() }
            }
        val dataSet =
            newItems.toMultiChartDataSet(
                prefix = DEFAULT_PREFIX,
                categories = categories,
                title = DEFAULT_TITLE,
            )
        return StackedAreaSampleData(
            dataSet = dataSet,
            seriesKeys = newItems.map { it.first },
        )
    }

    private fun stackedAreaCategoriesFor(points: Int): List<String> {
        if (points <= stackedAreaCategories.size) {
            return stackedAreaCategories.take(points)
        }
        val extraCount = points - stackedAreaCategories.size
        val extras = List(extraCount) { index -> "P${stackedAreaCategories.size + index + 1}" }
        return stackedAreaCategories + extras
    }
}
