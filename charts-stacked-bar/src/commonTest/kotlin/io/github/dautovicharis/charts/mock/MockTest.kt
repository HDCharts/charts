package io.github.dautovicharis.charts.mock

import androidx.compose.ui.graphics.Color
import io.github.dautovicharis.charts.model.ChartData
import io.github.dautovicharis.charts.model.ChartSeries
import io.github.dautovicharis.charts.model.chartDataOf

internal object MockTest {
    private val firstItem = listOf(26000.68f, 28000.34f, 32000.57f, 45000.57f)
    private val secondItem = listOf(26000.68f, 28000.34f, 32000.57f, 45000.57f)
    private val thirdItem = listOf(4000.87f, 5000.58f, 30245.81f, 135000.58f)
    private val fourthItem = listOf(1000.87f, 9000.58f, 16544.81f, 100444.87f)

    private val categories = listOf("Jan", "Feb", "Mar", "Apr")
    val colors = listOf(Color.Red, Color.Green, Color.Cyan, Color.Black)

    val stackedBarData: ChartData =
        transpose(
            rows =
                listOf(
                    "Item 1" to firstItem,
                    "Item 2" to secondItem,
                    "Item 3" to thirdItem,
                    "Item 4" to fourthItem,
                ),
            segmentNames = categories,
        )

    fun invalidStackedBarData(): ChartData =
        transpose(
            rows =
                listOf(
                    "Item 1" to firstItem,
                    "Item 2" to secondItem.dropLast(1),
                    "Item 3" to thirdItem,
                    "Item 4" to fourthItem.dropLast(1),
                ),
            segmentNames = categories,
        )

    private fun transpose(
        rows: List<Pair<String, List<Float>>>,
        segmentNames: List<String>,
    ): ChartData =
        chartDataOf(
            categories = rows.map { (barLabel, _) -> barLabel },
            *segmentNames
                .mapIndexed { segmentIndex, segmentName ->
                    ChartSeries(
                        name = segmentName,
                        values = rows.map { (_, values) -> values.getOrNull(segmentIndex)?.toDouble() ?: Double.NaN },
                    )
                }.toTypedArray(),
        )
}
