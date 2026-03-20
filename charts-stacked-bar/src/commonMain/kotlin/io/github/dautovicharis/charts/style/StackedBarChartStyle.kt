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
 * A class that defines the style for a Stacked Bar Chart.
 *
 * @property modifier The modifier to be applied to the chart.
 * @property chartViewStyle The style to be applied to the chart container.
 * @property barColor The fallback base color used when `barColors` is empty.
 * @property barAlpha The alpha value applied to rendered bar segments.
 * @property space The space between bars in the chart.
 * @property barColors The explicit colors used for stacked segments.
 * @property minBarWidth The minimum width of each bar, used for dense/expand behavior.
 * @property zoomControlsVisible Whether zoom controls are shown in expanded dense mode.
 * @property yAxisLabelsVisible Whether Y-axis labels are shown.
 * @property yAxisLabelColor The color of Y-axis labels.
 * @property yAxisLabelSize The text size of Y-axis labels.
 * @property yAxisLabelCount Number of Y-axis labels.
 * @property xAxisLabelsVisible Whether X-axis labels are shown.
 * @property xAxisLabelColor The color of X-axis labels.
 * @property xAxisLabelSize The text size of X-axis labels.
 * @property xAxisLabelMaxCount Maximum number of X-axis labels to display.
 * @property selectionLineVisible Whether the selection indicator line is shown.
 * @property selectionLineColor The color of the selection indicator line.
 * @property selectionLineWidth The stroke width of the selection indicator line.
 */
@Immutable
class StackedBarChartStyle(
    val modifier: Modifier,
    val chartViewStyle: ChartViewStyle,
    val barColor: Color,
    val barAlpha: Float,
    val space: Dp,
    val barColors: List<Color>,
    val minBarWidth: Dp,
    val zoomControlsVisible: Boolean,
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
     * Returns a list of the properties of the StackedBarChartStyle.
     */
    override fun getProperties(): List<Pair<String, Any>> =
        listOf(
            StackedBarChartStyle::barColor.name to barColor,
            StackedBarChartStyle::barAlpha.name to barAlpha,
            StackedBarChartStyle::space.name to space,
            StackedBarChartStyle::barColors.name to barColors,
            StackedBarChartStyle::minBarWidth.name to minBarWidth,
            StackedBarChartStyle::zoomControlsVisible.name to zoomControlsVisible,
            StackedBarChartStyle::yAxisLabelsVisible.name to yAxisLabelsVisible,
            StackedBarChartStyle::yAxisLabelColor.name to yAxisLabelColor,
            StackedBarChartStyle::yAxisLabelSize.name to yAxisLabelSize,
            StackedBarChartStyle::yAxisLabelCount.name to yAxisLabelCount,
            StackedBarChartStyle::xAxisLabelsVisible.name to xAxisLabelsVisible,
            StackedBarChartStyle::xAxisLabelColor.name to xAxisLabelColor,
            StackedBarChartStyle::xAxisLabelSize.name to xAxisLabelSize,
            StackedBarChartStyle::xAxisLabelMaxCount.name to xAxisLabelMaxCount,
            StackedBarChartStyle::selectionLineVisible.name to selectionLineVisible,
            StackedBarChartStyle::selectionLineColor.name to selectionLineColor,
            StackedBarChartStyle::selectionLineWidth.name to selectionLineWidth,
        )
}

/**
 * An object that provides default styles for a Stacked Bar Chart.
 */
object StackedBarChartDefaults {
    @Composable
    private fun defaultXAxisLabelColor() = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)

    @Composable
    private fun defaultYAxisLabelColor() = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)

    @Composable
    private fun defaultSelectionLineColor() = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)

    /**
     * Returns a StackedBarChartStyle with the provided parameters or their default values.
     *
     * @param barColor The color to be used for the bars in the chart. Defaults to the primary color of the MaterialTheme.
     * @param barAlpha The alpha value applied to rendered bar segments. Defaults to 0.4f in light theme and 0.6f in dark theme.
     * @param space The space between the bars in the chart. Defaults to 10.dp.
     * @param barColors The colors to be used for the bars in the chart. Defaults to an empty list.
     * @param minBarWidth The minimum width of each bar. Defaults to 10.dp.
     * @param zoomControlsVisible Whether zoom controls are shown in expanded dense mode. Defaults to true.
     * @param yAxisLabelsVisible Whether Y-axis labels are shown. Defaults to true.
     * @param yAxisLabelColor The color of Y-axis labels. Defaults to a theme-based onSurface variant.
     * @param yAxisLabelSize The text size of Y-axis labels. Defaults to 11.sp.
     * @param yAxisLabelCount Number of Y-axis labels. Defaults to 5.
     * @param xAxisLabelsVisible Whether X-axis labels are shown. Defaults to true.
     * @param xAxisLabelColor The color of X-axis labels. Defaults to a theme-based onSurface variant.
     * @param xAxisLabelSize The text size of X-axis labels. Defaults to 11.sp.
     * @param xAxisLabelMaxCount Maximum number of X-axis labels to display. Defaults to 6.
     * @param selectionLineVisible Whether the selection indicator line is shown. Defaults to true.
     * @param selectionLineColor The color of the selection indicator line. Defaults to a theme-based primary variant.
     * @param selectionLineWidth The stroke width of the selection indicator line. Defaults to 1f.
     * @param chartViewStyle The style to be applied to the chart view. Defaults to the default style of ChartViewDefaults.
     */
    @Composable
    fun style(
        barColor: Color = MaterialTheme.colorScheme.primary,
        barAlpha: Float = defaultChartAlpha(),
        space: Dp = 10.dp,
        barColors: List<Color> = emptyList(),
        chartViewStyle: ChartViewStyle = ChartViewDefaults.style(),
        minBarWidth: Dp = 10.dp,
        zoomControlsVisible: Boolean = true,
        yAxisLabelsVisible: Boolean = true,
        yAxisLabelColor: Color = defaultYAxisLabelColor(),
        yAxisLabelSize: TextUnit = 11.sp,
        yAxisLabelCount: Int = 5,
        xAxisLabelsVisible: Boolean = true,
        xAxisLabelColor: Color = defaultXAxisLabelColor(),
        xAxisLabelSize: TextUnit = 11.sp,
        xAxisLabelMaxCount: Int = 6,
        selectionLineVisible: Boolean = true,
        selectionLineColor: Color = defaultSelectionLineColor(),
        selectionLineWidth: Float = 1f,
    ): StackedBarChartStyle {
        val modifier: Modifier = chartViewStyle.fillMaxSizeChartModifier()

        return StackedBarChartStyle(
            modifier = modifier,
            barColor = barColor,
            barAlpha = barAlpha.coerceIn(0f, 1f),
            space = space,
            barColors = barColors,
            minBarWidth = minBarWidth,
            zoomControlsVisible = zoomControlsVisible,
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
