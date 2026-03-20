package io.github.dautovicharis.charts.mock

import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp
import io.github.dautovicharis.charts.internal.common.model.ChartDataType.FloatData
import io.github.dautovicharis.charts.model.ChartDataSet
import io.github.dautovicharis.charts.model.MultiChartDataSet
import io.github.dautovicharis.charts.style.ChartViewStyle
import io.github.dautovicharis.charts.style.LineChartStyle
import io.github.dautovicharis.charts.style.PieChartStyle
import io.github.dautovicharis.charts.style.StackedAreaChartStyle
import io.github.dautovicharis.charts.style.StackedBarChartStyle

internal object MockTest {
    const val TITLE = "Title"
    private val FIRST_ITEM = listOf(26000.68f, 28000.34f, 32000.57f, 45000.57f)
    private val SECOND_ITEM = listOf(26000.68f, 28000.34f, 32000.57f, 45000.57f)
    private val THIRD_ITEM = listOf(4000.87f, 5000.58f, 30245.81f, 135000.58f)
    private val FOURTH_ITEM = listOf(1000.87f, 9000.58f, 16544.81f, 100444.87f)

    private const val FIRST_ITEM_NAME = "Item 1"
    private const val SECOND_ITEM_NAME = "Item 2"
    private const val THIRD_ITEM_NAME = "Item 3"
    private const val FOURTH_ITEM_NAME = "Item 4"

    private val categories = listOf("Jan", "Feb", "Mar", "Apr")
    val colors = listOf(Color.Red, Color.Green, Color.Cyan, Color.Black)

    private val dataItems =
        listOf(
            FIRST_ITEM_NAME to FloatData(FIRST_ITEM),
            SECOND_ITEM_NAME to FloatData(SECOND_ITEM),
            THIRD_ITEM_NAME to FloatData(THIRD_ITEM),
            FOURTH_ITEM_NAME to FloatData(FOURTH_ITEM),
        )

    val dataSet =
        ChartDataSet(
            items = FloatData(listOf(10f, 20f, 30f, 40f)),
            title = TITLE,
        )

    val multiDataSet =
        MultiChartDataSet(
            items = dataItems,
            categories = categories,
            title = TITLE,
        )

    fun invalidMultiDataSet(): MultiChartDataSet {
        val items =
            listOf(
                FIRST_ITEM_NAME to FloatData(FIRST_ITEM.dropLast(1)),
                SECOND_ITEM_NAME to FloatData(SECOND_ITEM),
                THIRD_ITEM_NAME to FloatData(THIRD_ITEM.dropLast(1)),
                FOURTH_ITEM_NAME to FloatData(FOURTH_ITEM),
            )

        return MultiChartDataSet(
            items = items,
            categories = categories.dropLast(1),
            title = TITLE,
            prefix = "$",
        )
    }

    fun invalidMultiDataSet(
        index: Int,
        empty: Boolean = false,
    ): MultiChartDataSet {
        val updatedDataItems = dataItems.toMutableList()
        updatedDataItems[index] =
            if (empty) {
                updatedDataItems[index].copy(second = FloatData(emptyList()))
            } else {
                updatedDataItems[index].copy(second = FloatData(updatedDataItems[index].second.values.drop(2)))
            }

        return MultiChartDataSet(
            items = updatedDataItems,
            categories = categories,
            title = TITLE,
        )
    }

    fun invalidDataSetCategories(): MultiChartDataSet =
        MultiChartDataSet(
            items = dataItems,
            categories = categories.drop(1),
            title = TITLE,
        )

    // Mock styles
    fun mockLineChartStyle(lineColors: List<Color> = colors): LineChartStyle =
        LineChartStyle(
            modifier = Modifier.fillMaxSize(),
            pointColor = Color.Red,
            pointSize = 10f,
            pointVisible = true,
            lineColor = Color.Green,
            lineAlpha = 1f,
            lineColors = lineColors,
            bezier = true,
            dragPointSize = 7f,
            dragPointVisible = true,
            dragActivePointSize = 12f,
            dragPointColor = Color.Red,
            dragPointColorSameAsLine = true,
            pointColorSameAsLine = true,
            axisVisible = true,
            axisColor = Color.Gray,
            axisLineWidth = 1f,
            yAxisLabelsVisible = true,
            yAxisLabelColor = Color.Gray,
            yAxisLabelSize = 11.sp,
            yAxisLabelCount = 5,
            xAxisLabelsVisible = true,
            xAxisLabelColor = Color.Gray,
            xAxisLabelSize = 11.sp,
            xAxisLabelMaxCount = 6,
            zoomControlsVisible = true,
            chartViewStyle = mockChartViewStyle(),
        )

    fun mockStackedBarChartStyle(barColors: List<Color> = colors): StackedBarChartStyle =
        StackedBarChartStyle(
            modifier = Modifier.fillMaxSize(),
            barColors = barColors,
            barColor = Color.Red,
            barAlpha = 1f,
            space = Dp(10f),
            minBarWidth = Dp(10f),
            zoomControlsVisible = true,
            yAxisLabelsVisible = true,
            yAxisLabelColor = Color.Gray,
            yAxisLabelSize = 11.sp,
            yAxisLabelCount = 5,
            xAxisLabelsVisible = true,
            xAxisLabelColor = Color.Gray,
            xAxisLabelSize = 11.sp,
            xAxisLabelMaxCount = 6,
            selectionLineVisible = true,
            selectionLineColor = Color.Magenta,
            selectionLineWidth = 1f,
            chartViewStyle = mockChartViewStyle(),
        )

    fun mockStackedAreaChartStyle(
        areaColors: List<Color> = colors,
        lineColors: List<Color> = colors,
    ): StackedAreaChartStyle =
        StackedAreaChartStyle(
            modifier = Modifier.fillMaxSize(),
            areaColor = Color.Red,
            areaColors = areaColors,
            fillAlpha = 0.35f,
            lineVisible = true,
            lineColor = Color.Red,
            lineColors = lineColors,
            lineWidth = 4f,
            bezier = false,
            zoomControlsVisible = true,
            yAxisLabelsVisible = true,
            yAxisLabelColor = Color.Gray,
            yAxisLabelSize = 11.sp,
            yAxisLabelCount = 5,
            xAxisLabelsVisible = true,
            xAxisLabelColor = Color.Gray,
            xAxisLabelSize = 11.sp,
            xAxisLabelMaxCount = 6,
            chartViewStyle = mockChartViewStyle(),
        )

    fun mockBarChartStyle(barColors: List<Color> = colors): StackedBarChartStyle =
        StackedBarChartStyle(
            modifier = Modifier.fillMaxSize(),
            barColors = barColors,
            barColor = Color.Red,
            barAlpha = 1f,
            space = Dp(10f),
            minBarWidth = Dp(10f),
            zoomControlsVisible = true,
            yAxisLabelsVisible = true,
            yAxisLabelColor = Color.Gray,
            yAxisLabelSize = 11.sp,
            yAxisLabelCount = 5,
            xAxisLabelsVisible = true,
            xAxisLabelColor = Color.Gray,
            xAxisLabelSize = 11.sp,
            xAxisLabelMaxCount = 6,
            selectionLineVisible = true,
            selectionLineColor = Color.Magenta,
            selectionLineWidth = 1f,
            chartViewStyle = mockChartViewStyle(),
        )

    fun mockPieChartStyle(pieColors: List<Color> = colors): PieChartStyle =
        PieChartStyle(
            modifier = Modifier.fillMaxSize(),
            pieColors = pieColors,
            pieColor = Color.Red,
            pieAlpha = 1f,
            donutPercentage = 0.5f,
            borderColor = Color.Black,
            borderWidth = 2f,
            legendVisible = true,
            chartViewStyle = mockChartViewStyle(),
        )

    private fun mockChartViewStyle(): ChartViewStyle =
        ChartViewStyle(
            modifierMain = Modifier.fillMaxSize(),
            styleTitle = TextStyle.Default,
            modifierLegend = Modifier.fillMaxSize(),
            modifierTopTitle = Modifier.fillMaxSize(),
            innerPadding = Dp(10f),
            width = Dp.Infinity,
            backgroundColor = Color.White,
            modifierChart = Modifier.aspectRatio(1f),
        )
}
