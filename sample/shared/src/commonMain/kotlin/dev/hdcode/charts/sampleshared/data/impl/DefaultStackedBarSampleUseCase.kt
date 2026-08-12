package dev.hdcode.charts.sampleshared.data.impl

import dev.hdcode.charts.sampleshared.data.StackedBarSampleData
import dev.hdcode.charts.sampleshared.data.StackedBarSampleUseCase
import io.github.dautovicharis.charts.model.MultiChartDataSet
import io.github.dautovicharis.charts.model.toMultiChartDataSet

internal class DefaultStackedBarSampleUseCase : StackedBarSampleUseCase {
    companion object {
        private const val DEFAULT_TITLE = "Quarterly Revenue by Region"
        private const val DEFAULT_PREFIX = "$"
        private val REFRESH_RANGE = 100..1000
    }

    private val stackedCategories = listOf("Q1", "Q2", "Q3", "Q4")
    private val stackedItems =
        listOf(
            "North America" to listOf(320f, 340f, 360f, 390f),
            "Europe" to listOf(210f, 230f, 245f, 260f),
            "Asia Pacific" to listOf(180f, 205f, 225f, 250f),
        )
    private val noCategoriesItems =
        listOf(
            "Online" to listOf(220f, 260f, 300f),
            "Retail" to listOf(180f, 210f, 240f),
            "Enterprise" to listOf(140f, 160f, 190f),
        )

    override fun initialStackedBarSample(): StackedBarSampleData =
        StackedBarSampleData(
            dataSet =
                stackedItems.toMultiChartDataSet(
                    title = DEFAULT_TITLE,
                    prefix = DEFAULT_PREFIX,
                    categories = stackedCategories,
                ),
            segmentKeys = stackedCategories,
        )

    override fun initialStackedBarNoCategoriesDataSet(): MultiChartDataSet =
        noCategoriesItems.toMultiChartDataSet(title = "Revenue Streams (No Period Labels)")

    override fun stackedBarRefreshRange(): IntRange = REFRESH_RANGE

    override fun stackedBarSample(range: IntRange): StackedBarSampleData =
        stackedBarSample(points = stackedItems.size, range = range)

    override fun stackedBarSample(
        points: Int,
        range: IntRange,
    ): StackedBarSampleData {
        val safePoints = points.coerceAtLeast(1)
        val safeRangeStart = minOf(range.first, range.last)
        val safeRangeEnd = maxOf(range.first, range.last)
        val safeRange = safeRangeStart..safeRangeEnd
        val newItems =
            List(safePoints) { index ->
                stackedBarLabel(index) to List(stackedCategories.size) { safeRange.random().toFloat() }
            }
        val dataSet =
            newItems.toMultiChartDataSet(
                title = DEFAULT_TITLE,
                prefix = DEFAULT_PREFIX,
                categories = stackedCategories,
            )
        return StackedBarSampleData(
            dataSet = dataSet,
            segmentKeys = stackedCategories,
        )
    }

    private fun stackedBarLabel(index: Int): String = stackedItems.getOrNull(index)?.first ?: "Region ${index + 1}"
}
