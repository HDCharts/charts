package io.github.dautovicharis.charts.mock

import io.github.dautovicharis.charts.model.ChartSeries
import io.github.dautovicharis.charts.model.chartDataOf

internal object MockTest {
    private val firstItem = listOf(26000.68, 28000.34, 32000.57, 45000.57)
    private val secondItem = listOf(26000.68, 28000.34, 32000.57, 45000.57)
    private val thirdItem = listOf(4000.87, 5000.58, 30245.81, 135000.58)
    private val fourthItem = listOf(1000.87, 9000.58, 16544.81, 100444.87)

    val multiDataSet =
        chartDataOf(
            categories = listOf("Jan", "Feb", "Mar", "Apr"),
            ChartSeries(name = "Item 1", values = firstItem),
            ChartSeries(name = "Item 2", values = secondItem),
            ChartSeries(name = "Item 3", values = thirdItem),
            ChartSeries(name = "Item 4", values = fourthItem),
        )
}
