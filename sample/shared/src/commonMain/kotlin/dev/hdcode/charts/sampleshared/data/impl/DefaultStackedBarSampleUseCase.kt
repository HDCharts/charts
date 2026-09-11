package dev.hdcode.charts.sampleshared.data.impl

import dev.hdcode.charts.sampleshared.data.StackedBarSampleData
import dev.hdcode.charts.sampleshared.data.StackedBarSampleUseCase
import io.github.dautovicharis.charts.model.ChartData
import io.github.dautovicharis.charts.model.ChartSeries
import io.github.dautovicharis.charts.model.chartDataOf

internal class DefaultStackedBarSampleUseCase : StackedBarSampleUseCase {
    companion object {
        private const val DEFAULT_TITLE = "Quarterly Revenue by Region"
        private val REFRESH_RANGE = 100..1000
    }

    private val stackedCategories = listOf("Q1", "Q2", "Q3", "Q4")
    private val stackedItems =
        listOf(
            "North America" to listOf(320.0, 340.0, 360.0, 390.0),
            "Europe" to listOf(210.0, 230.0, 245.0, 260.0),
            "Asia Pacific" to listOf(180.0, 205.0, 225.0, 250.0),
        )
    private val noCategoriesItems =
        listOf(
            "Online" to listOf(220.0, 260.0, 300.0),
            "Retail" to listOf(180.0, 210.0, 240.0),
            "Enterprise" to listOf(140.0, 160.0, 190.0),
        )

    override fun initialStackedBarSample(): StackedBarSampleData =
        StackedBarSampleData(
            dataSet =
                stackedBarData(
                    rows = stackedItems,
                    segmentNames = stackedCategories,
                ),
            segmentKeys = stackedCategories,
            title = DEFAULT_TITLE,
        )

    override fun initialStackedBarNoCategoriesDataSet(): StackedBarSampleData =
        StackedBarSampleData(
            dataSet = stackedBarData(noCategoriesItems),
            segmentKeys = emptyList(),
            title = "Revenue Streams (No Period Labels)",
        )

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
                stackedBarLabel(index) to List(stackedCategories.size) { safeRange.random().toDouble() }
            }
        val dataSet = stackedBarData(rows = newItems, segmentNames = stackedCategories)
        return StackedBarSampleData(
            dataSet = dataSet,
            segmentKeys = stackedCategories,
            title = DEFAULT_TITLE,
        )
    }

    private fun stackedBarLabel(index: Int): String = stackedItems.getOrNull(index)?.first ?: "Region ${index + 1}"

    private fun stackedBarData(
        rows: List<Pair<String, List<Double>>>,
        segmentNames: List<String>? = null,
        categories: List<String>? = null,
    ): ChartData {
        val segmentCount = rows.maxOfOrNull { row -> row.second.size } ?: 0
        return chartDataOf(
            categories = categories ?: rows.map { row -> row.first },
            *List(segmentCount) { segmentIndex ->
                ChartSeries(
                    name = segmentNames?.getOrNull(segmentIndex),
                    values = rows.map { row -> row.second.getOrNull(segmentIndex)?.toDouble() ?: Double.NaN },
                )
            }.toTypedArray(),
        )
    }
}
