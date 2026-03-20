package io.github.dautovicharis.charts.style

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

/**
 * A class that defines the style for a Line Chart.
 *
 * @property modifier The modifier to be applied to the chart.
 * @property chartViewStyle The style to be applied to the chart view.
 * @property dragPointColorSameAsLine A boolean indicating whether the color of the drag point is the same as the line color.
 * @property pointColorSameAsLine A boolean indicating whether the color of the point is the same as the line color.
 * @property pointColor The color of the points on the line chart.
 * @property pointVisible A boolean indicating whether the points on the line chart are visible.
 * @property pointSize The size of the points on the line chart.
 * @property lineColor The color of the line in the line chart.
 * @property lineAlpha The alpha value applied to rendered line colors.
 * @property lineColors The colors of the lines in the line chart.
 * @property bezier A boolean indicating whether the line chart should be drawn with bezier curves.
 * @property dragPointSize The size of the drag point on the line chart.
 * @property dragPointVisible A boolean indicating whether the drag point on the line chart is visible.
 * @property dragActivePointSize The size of the active drag point on the line chart.
 * @property dragPointColor The color of the drag point on the line chart.
 * @property axisVisible Whether chart axes are shown.
 * @property axisColor The color of the axes.
 * @property axisLineWidth The stroke width of the axes.
 * @property yAxisLabelsVisible Whether Y-axis labels are shown.
 * @property yAxisLabelColor The color of Y-axis labels.
 * @property yAxisLabelSize The text size of Y-axis labels.
 * @property yAxisLabelCount Number of Y-axis labels.
 * @property xAxisLabelsVisible Whether X-axis labels are shown.
 * @property xAxisLabelColor The color of X-axis labels.
 * @property xAxisLabelSize The text size of X-axis labels.
 * @property xAxisLabelMaxCount Maximum number of X-axis labels to display.
 * @property zoomControlsVisible Whether dense-mode zoom controls are shown.
 */
@Immutable
class LineChartStyle(
    val modifier: Modifier,
    val chartViewStyle: ChartViewStyle,
    val dragPointColorSameAsLine: Boolean,
    val pointColorSameAsLine: Boolean,
    val pointColor: Color,
    val pointVisible: Boolean,
    val pointSize: Float,
    val lineColor: Color,
    val lineAlpha: Float,
    val lineColors: List<Color>,
    val bezier: Boolean,
    val dragPointSize: Float,
    val dragPointVisible: Boolean,
    val dragActivePointSize: Float,
    val dragPointColor: Color,
    val axisVisible: Boolean,
    val axisColor: Color,
    val axisLineWidth: Float,
    val yAxisLabelsVisible: Boolean,
    val yAxisLabelColor: Color,
    val yAxisLabelSize: TextUnit,
    val yAxisLabelCount: Int,
    val xAxisLabelsVisible: Boolean,
    val xAxisLabelColor: Color,
    val xAxisLabelSize: TextUnit,
    val xAxisLabelMaxCount: Int,
    val zoomControlsVisible: Boolean,
) : Style {
    /**
     * Returns a list of the properties of the LineChartStyle.
     */
    override fun getProperties(): List<Pair<String, Any>> =
        listOf(
            LineChartStyle::pointColor.name to pointColor,
            LineChartStyle::pointVisible.name to pointVisible,
            LineChartStyle::pointSize.name to pointSize,
            LineChartStyle::lineColor.name to lineColor,
            LineChartStyle::lineAlpha.name to lineAlpha,
            LineChartStyle::lineColors.name to lineColors,
            LineChartStyle::bezier.name to bezier,
            LineChartStyle::dragPointSize.name to dragPointSize,
            LineChartStyle::dragPointVisible.name to dragPointVisible,
            LineChartStyle::dragActivePointSize.name to dragActivePointSize,
            LineChartStyle::dragPointColor.name to dragPointColor,
            LineChartStyle::axisVisible.name to axisVisible,
            LineChartStyle::axisColor.name to axisColor,
            LineChartStyle::axisLineWidth.name to axisLineWidth,
            LineChartStyle::yAxisLabelsVisible.name to yAxisLabelsVisible,
            LineChartStyle::yAxisLabelColor.name to yAxisLabelColor,
            LineChartStyle::yAxisLabelSize.name to yAxisLabelSize,
            LineChartStyle::yAxisLabelCount.name to yAxisLabelCount,
            LineChartStyle::xAxisLabelsVisible.name to xAxisLabelsVisible,
            LineChartStyle::xAxisLabelColor.name to xAxisLabelColor,
            LineChartStyle::xAxisLabelSize.name to xAxisLabelSize,
            LineChartStyle::xAxisLabelMaxCount.name to xAxisLabelMaxCount,
            LineChartStyle::zoomControlsVisible.name to zoomControlsVisible,
        )
}

/**
 * An object that provides default styles for a Line Chart.
 */
object LineChartDefaults {
    /**
     * Returns the default color for points on a Line Chart.
     */
    @Composable
    private fun defaultPointColor() = MaterialTheme.colorScheme.tertiary

    /**
     * Returns the default color for drag points on a Line Chart.
     */
    @Composable
    private fun defaultDragPointColor() = MaterialTheme.colorScheme.tertiary

    @Composable
    private fun defaultAxisColor() = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)

    @Composable
    private fun defaultXAxisLabelColor() = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)

    @Composable
    private fun defaultYAxisLabelColor() = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)

    /**
     * Returns a LineChartStyle with the provided parameters or their default values.
     *
     * @param pointColor The color of the points on the line chart. Defaults to the tertiary color of the MaterialTheme.
     * @param pointSize The size of the points on the line chart. Defaults to 9f.
     * @param pointVisible A boolean indicating whether the points on the line chart are visible. Defaults to false.
     * @param lineColor The color of the line in the line chart. Defaults to the primary color of the MaterialTheme.
     * @param lineAlpha The alpha value applied to rendered line colors. Defaults to 0.4f in light theme and 0.6f in dark theme.
     * @param lineColors The colors of the lines in the line chart. Defaults to an empty list.
     * @param bezier A boolean indicating whether the line chart should be drawn with bezier curves. Defaults to true.
     * @param dragPointSize The size of the drag point on the line chart. Defaults to 7f.
     * @param dragPointVisible A boolean indicating whether the drag point on the line chart is visible. Defaults to true.
     * @param dragActivePointSize The size of the active drag point on the line chart. Defaults to 12f.
     * @param dragPointColor The color of the drag point on the line chart. Defaults to the tertiary color of the MaterialTheme.
     * @param axisVisible Whether chart axes are shown. Defaults to true.
     * @param axisColor The color of the axes. Defaults to a theme-based onSurface variant.
     * @param axisLineWidth The stroke width of the axes. Defaults to 1f.
     * @param yAxisLabelsVisible Whether Y-axis labels are shown. Defaults to true.
     * @param yAxisLabelColor The color of Y-axis labels. Defaults to a theme-based onSurface variant.
     * @param yAxisLabelSize The text size of Y-axis labels. Defaults to 11.sp.
     * @param yAxisLabelCount Number of Y-axis labels. Defaults to 5.
     * @param xAxisLabelsVisible Whether X-axis labels are shown. Defaults to true.
     * @param xAxisLabelColor The color of X-axis labels. Defaults to a theme-based onSurface variant.
     * @param xAxisLabelSize The text size of X-axis labels. Defaults to 11.sp.
     * @param xAxisLabelMaxCount Maximum number of X-axis labels to display. Defaults to 6.
     * @param zoomControlsVisible Whether dense-mode zoom controls are shown. Defaults to true.
     * @param chartViewStyle The style to be applied to the chart view. Defaults to the default style of ChartViewDefaults.
     *
     * Dense zoom/scroll properties are applied in morph mode and ignored in timeline mode.
     */
    @Composable
    fun style(
        pointColor: Color = defaultPointColor(),
        pointSize: Float = 9f,
        pointVisible: Boolean = false,
        lineColor: Color = MaterialTheme.colorScheme.primary,
        lineAlpha: Float = defaultChartAlpha(),
        lineColors: List<Color> = emptyList(),
        bezier: Boolean = true,
        dragPointSize: Float = 7f,
        dragPointVisible: Boolean = true,
        dragActivePointSize: Float = 12f,
        dragPointColor: Color = defaultDragPointColor(),
        axisVisible: Boolean = true,
        axisColor: Color = defaultAxisColor(),
        axisLineWidth: Float = 1f,
        yAxisLabelsVisible: Boolean = true,
        yAxisLabelColor: Color = defaultYAxisLabelColor(),
        yAxisLabelSize: TextUnit = 11.sp,
        yAxisLabelCount: Int = 5,
        xAxisLabelsVisible: Boolean = true,
        xAxisLabelColor: Color = defaultXAxisLabelColor(),
        xAxisLabelSize: TextUnit = 11.sp,
        xAxisLabelMaxCount: Int = 6,
        zoomControlsVisible: Boolean = true,
        chartViewStyle: ChartViewStyle = ChartViewDefaults.style(),
    ): LineChartStyle {
        val modifier: Modifier = chartViewStyle.wrapContentChartModifier()

        val pointColorSameAsLine = pointColor == defaultPointColor()
        val dragPointColorSameAsLine = pointColor == defaultDragPointColor()

        return LineChartStyle(
            modifier = modifier,
            pointColor = pointColor,
            lineColor = lineColor,
            lineAlpha = lineAlpha.coerceIn(0f, 1f),
            bezier = bezier,
            pointVisible = pointVisible,
            lineColors = lineColors,
            dragPointSize = dragPointSize,
            dragPointVisible = dragPointVisible,
            pointSize = pointSize,
            dragActivePointSize = dragActivePointSize,
            pointColorSameAsLine = pointColorSameAsLine,
            dragPointColor = dragPointColor,
            dragPointColorSameAsLine = dragPointColorSameAsLine,
            axisVisible = axisVisible,
            axisColor = axisColor,
            axisLineWidth = axisLineWidth,
            yAxisLabelsVisible = yAxisLabelsVisible,
            yAxisLabelColor = yAxisLabelColor,
            yAxisLabelSize = yAxisLabelSize,
            yAxisLabelCount = yAxisLabelCount,
            xAxisLabelsVisible = xAxisLabelsVisible,
            xAxisLabelColor = xAxisLabelColor,
            xAxisLabelSize = xAxisLabelSize,
            xAxisLabelMaxCount = xAxisLabelMaxCount,
            zoomControlsVisible = zoomControlsVisible,
            chartViewStyle = chartViewStyle,
        )
    }
}
