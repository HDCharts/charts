package io.github.dautovicharis.charts.mock

import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp
import io.github.dautovicharis.charts.internal.common.model.ChartDataType.FloatData
import io.github.dautovicharis.charts.internal.linechart.LineChartInternalStyle
import io.github.dautovicharis.charts.model.ChartData
import io.github.dautovicharis.charts.model.ChartDataSet
import io.github.dautovicharis.charts.model.ChartSeries
import io.github.dautovicharis.charts.model.MultiChartDataSet
import io.github.dautovicharis.charts.model.chartDataOf
import io.github.dautovicharis.charts.style.AxisLabelStyle
import io.github.dautovicharis.charts.style.ChartContainerStyle
import io.github.dautovicharis.charts.style.RadarAxesStyle
import io.github.dautovicharis.charts.style.RadarCategoryStyle
import io.github.dautovicharis.charts.style.RadarChartStyle
import io.github.dautovicharis.charts.style.RadarGridStyle
import io.github.dautovicharis.charts.style.RadarPointStyle
import io.github.dautovicharis.charts.style.RadarPolygonStyle
import io.github.dautovicharis.charts.style.StackedAreaAxisStyle
import io.github.dautovicharis.charts.style.StackedAreaBoundaryStyle
import io.github.dautovicharis.charts.style.StackedAreaChartStyle
import io.github.dautovicharis.charts.style.StackedAreaFillStyle
import io.github.dautovicharis.charts.style.StackedAreaSelectionStyle
import io.github.dautovicharis.charts.style.StackedBarAxisStyle
import io.github.dautovicharis.charts.style.StackedBarChartStyle
import io.github.dautovicharis.charts.style.StackedBarLayoutStyle
import io.github.dautovicharis.charts.style.StackedBarSegmentStyle
import io.github.dautovicharis.charts.style.StackedBarSelectionStyle
import kotlinx.collections.immutable.toImmutableList

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
    val colorsAsymmetric =
        listOf(Color.Red, Color.Green, Color.Cyan, Color.Black, Color.Blue, Color.Yellow)

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

    val asymmetricMultiDataSet =
        MultiChartDataSet(
            items =
                listOf(
                    FIRST_ITEM_NAME to FloatData(FIRST_ITEM + 5f),
                    SECOND_ITEM_NAME to FloatData(SECOND_ITEM + 5f),
                    THIRD_ITEM_NAME to FloatData(THIRD_ITEM + 5f),
                ),
            categories = categories + "May",
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

    // ChartData helpers for stacked-area tests
    val multiDataSetItems: ChartData =
        chartDataOf(
            categories = categories,
            ChartSeries(name = FIRST_ITEM_NAME, values = FIRST_ITEM.map { it.toDouble() }),
            ChartSeries(name = SECOND_ITEM_NAME, values = SECOND_ITEM.map { it.toDouble() }),
            ChartSeries(name = THIRD_ITEM_NAME, values = THIRD_ITEM.map { it.toDouble() }),
            ChartSeries(name = FOURTH_ITEM_NAME, values = FOURTH_ITEM.map { it.toDouble() }),
        )

    fun invalidMultiDataSetItemsCategories(): ChartData =
        chartDataOf(
            categories = categories.drop(1),
            ChartSeries(name = FIRST_ITEM_NAME, values = FIRST_ITEM.map { it.toDouble() }),
            ChartSeries(name = SECOND_ITEM_NAME, values = SECOND_ITEM.map { it.toDouble() }),
            ChartSeries(name = THIRD_ITEM_NAME, values = THIRD_ITEM.map { it.toDouble() }),
            ChartSeries(name = FOURTH_ITEM_NAME, values = FOURTH_ITEM.map { it.toDouble() }),
        )

    fun invalidRaggedMultiDataSetItems(index: Int): ChartData {
        val seriesList =
            listOf(FIRST_ITEM, SECOND_ITEM, THIRD_ITEM, FOURTH_ITEM).mapIndexed { i, values ->
                val name =
                    listOf(
                        FIRST_ITEM_NAME,
                        SECOND_ITEM_NAME,
                        THIRD_ITEM_NAME,
                        FOURTH_ITEM_NAME,
                    )[i]
                val seriesValues =
                    if (i == index) {
                        values.drop(2).map { it.toDouble() }
                    } else {
                        values.map { it.toDouble() }
                    }
                ChartSeries(name = name, values = seriesValues)
            }
        return chartDataOf(
            categories = categories,
            *seriesList.toTypedArray(),
        )
    }

    fun invalidSinglePointMultiDataSetItems(index: Int): ChartData =
        chartDataOf(
            categories = listOf("Jan"),
            ChartSeries(name = FIRST_ITEM_NAME, values = listOf(FIRST_ITEM.first().toDouble())),
            ChartSeries(name = SECOND_ITEM_NAME, values = SECOND_ITEM.map { it.toDouble() }),
            ChartSeries(name = THIRD_ITEM_NAME, values = THIRD_ITEM.map { it.toDouble() }),
            ChartSeries(name = FOURTH_ITEM_NAME, values = FOURTH_ITEM.map { it.toDouble() }),
        )

    // Mock styles
    fun mockLineChartStyle(lineColors: List<Color> = colors): LineChartInternalStyle =
        LineChartInternalStyle(
            modifier = Modifier.fillMaxSize(),
            chartContainerStyle = mockChartContainerStyle(),
            dragPointColorSameAsLine = true,
            pointColorSameAsLine = true,
            pointColor = Color.Red,
            pointVisible = true,
            pointSize = 10f,
            lineColor = Color.Green,
            lineAlpha = 1f,
            lineColors = lineColors,
            bezier = true,
            lineStrokeWidth = 1f,
            dragPointSize = 7f,
            dragPointVisible = true,
            dragActivePointSize = 12f,
            dragPointColor = Color.Red,
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
        )

    fun mockStackedBarChartStyle(barColors: List<Color> = colors): StackedBarChartStyle =
        StackedBarChartStyle(
            chartContainerStyle = mockChartContainerStyle(),
            segments = StackedBarSegmentStyle(color = Color.Red, colors = barColors, alpha = 1f),
            layout = StackedBarLayoutStyle(space = Dp(10f), minBarWidth = Dp(10f)),
            axis =
                StackedBarAxisStyle(
                    xLabels = AxisLabelStyle(visible = true, color = Color.Gray, size = 11.sp, count = 6),
                    yLabels = AxisLabelStyle(visible = true, color = Color.Gray, size = 11.sp, count = 5),
                ),
            selection = StackedBarSelectionStyle(visible = true, color = Color.Magenta, width = Dp(1f)),
            zoomControlsVisible = true,
        )

    fun mockStackedAreaChartStyle(
        areaColors: List<Color> = colors,
        lineColors: List<Color> = colors,
    ): StackedAreaChartStyle =
        StackedAreaChartStyle(
            chartContainerStyle = mockChartContainerStyle(),
            fill =
                StackedAreaFillStyle(
                    color = Color.Red,
                    colors = areaColors,
                    alpha = 0.35f,
                ),
            boundary =
                StackedAreaBoundaryStyle(
                    visible = true,
                    color = Color.Red,
                    colors = lineColors,
                    width = Dp(4f),
                    bezier = false,
                ),
            axis =
                StackedAreaAxisStyle(
                    xLabels = AxisLabelStyle(visible = true, color = Color.Gray, size = 11.sp, count = 6),
                    yLabels = AxisLabelStyle(visible = true, color = Color.Gray, size = 11.sp, count = 5),
                ),
            selection =
                StackedAreaSelectionStyle(
                    visible = true,
                    color = Color.Gray,
                    width = Dp(1f),
                ),
            zoomControlsVisible = true,
        )

    fun mockBarChartStyle(barColors: List<Color> = colors): StackedBarChartStyle =
        StackedBarChartStyle(
            chartContainerStyle = mockChartContainerStyle(),
            segments = StackedBarSegmentStyle(color = Color.Red, colors = barColors, alpha = 1f),
            layout = StackedBarLayoutStyle(space = Dp(10f), minBarWidth = Dp(10f)),
            axis =
                StackedBarAxisStyle(
                    xLabels = AxisLabelStyle(visible = true, color = Color.Gray, size = 11.sp, count = 6),
                    yLabels = AxisLabelStyle(visible = true, color = Color.Gray, size = 11.sp, count = 5),
                ),
            selection = StackedBarSelectionStyle(visible = true, color = Color.Magenta, width = Dp(1f)),
            zoomControlsVisible = true,
        )

    fun mockRadarChartStyle(lineColors: List<Color> = colors): RadarChartStyle =
        RadarChartStyle(
            chartContainerStyle = mockChartContainerStyle(),
            grid =
                RadarGridStyle(
                    visible = true,
                    color = Color.Gray,
                    lineWidth = 1f,
                    steps = 4,
                ),
            axes =
                RadarAxesStyle(
                    visible = true,
                    lineColor = Color.Gray,
                    lineWidth = 1f,
                    labelColor = Color.Gray,
                    labelSize = 11.sp,
                    labelPadding = 4f,
                    labelVisible = true,
                ),
            polygon =
                RadarPolygonStyle(
                    fillVisible = true,
                    fillAlpha = 0.3f,
                    lineColor = Color.Green,
                    lineColors = lineColors,
                    lineWidth = 2f,
                ),
            points =
                RadarPointStyle(
                    visible = true,
                    color = Color.Red,
                    colorSameAsLine = true,
                    size = 8f,
                ),
            categories =
                RadarCategoryStyle(
                    legendVisible = true,
                    pinsVisible = true,
                    colors = colors.toImmutableList(),
                    pinSize = 4f,
                ),
        )

    private fun mockChartContainerStyle(): ChartContainerStyle =
        ChartContainerStyle(
            modifierMain = Modifier.fillMaxSize(),
            styleTitle = TextStyle.Default,
            modifierLegend = Modifier.fillMaxSize(),
            modifierTopTitle = Modifier.fillMaxSize(),
            innerPadding = Dp(10f),
            modifierChart = Modifier.aspectRatio(1f),
        )
}
