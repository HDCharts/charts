package io.github.dautovicharis.charts.style

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * A class that defines the style for a Bar Chart.
 *
 * @property modifier The modifier to be applied to the chart.
 * @property chartViewStyle The style to be applied to the chart view.
 * @property barColor The color to be used for the bars in the chart.
 * @property barColors Optional explicit colors used for individual bars.
 * @property barAlpha The alpha value applied to rendered bars.
 * @property space The space between the bars in the chart.
 * @property minValue Optional fixed minimum value for the chart scale.
 * @property maxValue Optional fixed maximum value for the chart scale.
 * @property minBarWidth The minimum width of each bar.
 * @property zoomControlsVisible Whether zoom controls are shown.
 * @property gridVisible Whether horizontal grid lines are shown.
 * @property gridSteps Number of horizontal grid intervals.
 * @property gridColor The color of grid lines.
 * @property gridLineWidth The stroke width of grid lines.
 * @property axisVisible Whether the left Y-axis line and horizontal baseline are shown.
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
 * @property selectionLineVisible Whether the selected bar indicator line is shown.
 * @property selectionLineColor The color of the selected bar indicator line.
 * @property selectionLineWidth The stroke width of the selected bar indicator line.
 */
@Immutable
class BarChartStyle(
    val modifier: Modifier,
    val chartViewStyle: ChartViewStyle,
    val barColor: Color,
    val barColors: List<Color>,
    val barAlpha: Float,
    val space: Dp,
    val minValue: Float?,
    val maxValue: Float?,
    val minBarWidth: Dp,
    val zoomControlsVisible: Boolean,
    val gridVisible: Boolean,
    val gridSteps: Int,
    val gridColor: Color,
    val gridLineWidth: Float,
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
    val selectionLineVisible: Boolean,
    val selectionLineColor: Color,
    val selectionLineWidth: Float,
) : Style {
    /**
     * Returns a list of the properties of the BarChartStyle.
     */
    override fun getProperties(): List<Pair<String, Any>> =
        listOf(
            BarChartStyle::barColor.name to barColor,
            BarChartStyle::barColors.name to barColors,
            BarChartStyle::barAlpha.name to barAlpha,
            BarChartStyle::space.name to space,
            BarChartStyle::minValue.name to (minValue ?: "auto"),
            BarChartStyle::maxValue.name to (maxValue ?: "auto"),
            BarChartStyle::minBarWidth.name to minBarWidth,
            BarChartStyle::zoomControlsVisible.name to zoomControlsVisible,
            BarChartStyle::gridVisible.name to gridVisible,
            BarChartStyle::gridSteps.name to gridSteps,
            BarChartStyle::gridColor.name to gridColor,
            BarChartStyle::gridLineWidth.name to gridLineWidth,
            BarChartStyle::axisVisible.name to axisVisible,
            BarChartStyle::axisColor.name to axisColor,
            BarChartStyle::axisLineWidth.name to axisLineWidth,
            BarChartStyle::yAxisLabelsVisible.name to yAxisLabelsVisible,
            BarChartStyle::yAxisLabelColor.name to yAxisLabelColor,
            BarChartStyle::yAxisLabelSize.name to yAxisLabelSize,
            BarChartStyle::yAxisLabelCount.name to yAxisLabelCount,
            BarChartStyle::xAxisLabelsVisible.name to xAxisLabelsVisible,
            BarChartStyle::xAxisLabelColor.name to xAxisLabelColor,
            BarChartStyle::xAxisLabelSize.name to xAxisLabelSize,
            BarChartStyle::xAxisLabelMaxCount.name to xAxisLabelMaxCount,
            BarChartStyle::selectionLineVisible.name to selectionLineVisible,
            BarChartStyle::selectionLineColor.name to selectionLineColor,
            BarChartStyle::selectionLineWidth.name to selectionLineWidth,
        )
}

/**
 * An object that provides default styles for a Bar Chart.
 */
object BarChartDefaults {
    @Composable
    private fun defaultGridColor() = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f)

    @Composable
    private fun defaultAxisColor() = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)

    @Composable
    private fun defaultXAxisLabelColor() = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)

    @Composable
    private fun defaultYAxisLabelColor() = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)

    @Composable
    private fun defaultSelectionLineColor() = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)

    /**
     * Returns a BarChartStyle with the provided parameters or their default values.
     *
     * @param barColor The color to be used for the bars in the chart. Defaults to the primary color of the MaterialTheme.
     * @param barColors Optional explicit colors used for individual bars. Defaults to an empty list.
     * @param barAlpha The alpha value applied to rendered bars. Defaults to 0.4f in light theme and 0.6f in dark theme.
     * @param space The space between the bars in the chart. Defaults to 10.dp.
     * @param minValue Optional fixed minimum value for the chart scale. Defaults to null.
     * @param maxValue Optional fixed maximum value for the chart scale. Defaults to null.
     * @param minBarWidth The minimum width of each bar. Defaults to 10.dp.
     * @param zoomControlsVisible Whether zoom controls are shown. Defaults to true.
     * @param gridVisible Whether horizontal grid lines are shown. Defaults to true.
     * @param gridSteps Number of horizontal grid intervals. Defaults to 4.
     * @param gridColor The color of grid lines. Defaults to a theme-based onSurface variant.
     * @param gridLineWidth The stroke width of grid lines. Defaults to 1f.
     * @param axisVisible Whether the left Y-axis line and horizontal baseline are shown. Defaults to true.
     * @param axisColor The color of the axes. Defaults to a theme-based onSurface variant.
     * @param axisLineWidth The stroke width of the axes. Defaults to 1f.
     * @param xAxisLabelsVisible Whether X-axis labels are shown. Defaults to true.
     * @param xAxisLabelColor The color of X-axis labels. Defaults to a theme-based onSurface variant.
     * @param xAxisLabelSize The text size of X-axis labels. Defaults to 11.sp.
     * @param xAxisLabelMaxCount Maximum number of X-axis labels to display. Defaults to 6.
     * @param selectionLineVisible Whether the selected bar indicator line is shown. Defaults to true.
     * @param selectionLineColor The color of the selected bar indicator line. Defaults to a theme-based primary variant.
     * @param selectionLineWidth The stroke width of the selected bar indicator line. Defaults to 1f.
     * @param chartViewStyle The style to be applied to the chart view. Defaults to the default style of ChartViewDefaults.
     * @param yAxisLabelsVisible Whether Y-axis labels are shown. Defaults to true.
     * @param yAxisLabelColor The color of Y-axis labels. Defaults to a theme-based onSurface variant.
     * @param yAxisLabelSize The text size of Y-axis labels. Defaults to 11.sp.
     * @param yAxisLabelCount Number of Y-axis labels. Defaults to 5.
     */
    @Composable
    fun style(
        barColor: Color = MaterialTheme.colorScheme.primary,
        barColors: List<Color> = emptyList(),
        barAlpha: Float = defaultChartAlpha(),
        space: Dp = 10.dp,
        minValue: Float? = null,
        maxValue: Float? = null,
        minBarWidth: Dp = 10.dp,
        zoomControlsVisible: Boolean = true,
        gridVisible: Boolean = true,
        gridSteps: Int = 4,
        gridColor: Color = defaultGridColor(),
        gridLineWidth: Float = 1f,
        axisVisible: Boolean = true,
        axisColor: Color = defaultAxisColor(),
        axisLineWidth: Float = 1f,
        xAxisLabelsVisible: Boolean = true,
        xAxisLabelColor: Color = defaultXAxisLabelColor(),
        xAxisLabelSize: TextUnit = 11.sp,
        xAxisLabelMaxCount: Int = 6,
        selectionLineVisible: Boolean = true,
        selectionLineColor: Color = defaultSelectionLineColor(),
        selectionLineWidth: Float = 1f,
        chartViewStyle: ChartViewStyle = ChartViewDefaults.style(),
        yAxisLabelsVisible: Boolean = true,
        yAxisLabelColor: Color = defaultYAxisLabelColor(),
        yAxisLabelSize: TextUnit = 11.sp,
        yAxisLabelCount: Int = 5,
    ): BarChartStyle {
        val modifier: Modifier = chartViewStyle.fillMaxSizeChartModifier()

        return BarChartStyle(
            modifier = modifier,
            barColor = barColor,
            barColors = barColors,
            barAlpha = barAlpha.coerceIn(0f, 1f),
            space = space,
            minValue = minValue,
            maxValue = maxValue,
            minBarWidth = minBarWidth,
            zoomControlsVisible = zoomControlsVisible,
            gridVisible = gridVisible,
            gridSteps = gridSteps,
            gridColor = gridColor,
            gridLineWidth = gridLineWidth,
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
            selectionLineVisible = selectionLineVisible,
            selectionLineColor = selectionLineColor,
            selectionLineWidth = selectionLineWidth,
            chartViewStyle = chartViewStyle,
        )
    }
}
