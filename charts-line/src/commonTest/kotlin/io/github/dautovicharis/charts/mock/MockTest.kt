package io.github.dautovicharis.charts.mock

import androidx.compose.ui.graphics.Color
import io.github.dautovicharis.charts.model.ChartData
import io.github.dautovicharis.charts.model.ChartSeries
import io.github.dautovicharis.charts.model.chartDataOf
import io.github.dautovicharis.charts.model.toChartData

internal object MockTest {
    const val TITLE = "Title"

    private val firstItem = listOf(26000.68f, 28000.34f, 32000.57f, 45000.57f)
    private val secondItem = listOf(26000.68f, 28000.34f, 32000.57f, 45000.57f)
    private val thirdItem = listOf(4000.87f, 5000.58f, 30245.81f, 135000.58f)
    private val fourthItem = listOf(1000.87f, 9000.58f, 16544.81f, 100444.87f)

    private val categories = listOf("Jan", "Feb", "Mar", "Apr")
    val colors = listOf(Color.Red, Color.Green, Color.Cyan, Color.Black)

    val dataSet: ChartData =
        listOf(10f, 20f, 30f, 40f)
            .map { it.toDouble() }
            .toChartData(seriesName = TITLE)

    val multiDataSet: ChartData =
        chartDataOf(
            categories = categories,
            *arrayOf(
                ChartSeries(name = "Item 1", values = firstItem.map { it.toDouble() }),
                ChartSeries(name = "Item 2", values = secondItem.map { it.toDouble() }),
                ChartSeries(name = "Item 3", values = thirdItem.map { it.toDouble() }),
                ChartSeries(name = "Item 4", values = fourthItem.map { it.toDouble() }),
            ),
        )

    fun invalidMultiDataSet(): ChartData =
        chartDataOf(
            categories = categories.dropLast(1),
            *arrayOf(
                ChartSeries(name = "Item 1", values = firstItem.dropLast(1).map { it.toDouble() }),
                ChartSeries(name = "Item 2", values = secondItem.map { it.toDouble() }),
                ChartSeries(name = "Item 3", values = thirdItem.dropLast(1).map { it.toDouble() }),
                ChartSeries(name = "Item 4", values = fourthItem.map { it.toDouble() }),
            ),
        )
}
