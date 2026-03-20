package io.github.dautovicharis.charts.style

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

/**
 * A class that defines the style for a Stacked Area Chart.
 *
 * @property modifier The modifier to be applied to the chart.
 * @property chartViewStyle The style to be applied to the chart container.
 * @property areaColor The fallback fill color used when `areaColors` is empty.
 * @property areaColors The explicit fill colors used for stacked areas.
 * @property fillAlpha The alpha value applied to area fills.
 * @property lineVisible Whether boundary lines are shown on top of filled areas.
 * @property lineColor The fallback boundary line color when `lineColors` is empty.
 * @property lineColors The explicit boundary line colors.
 * @property lineWidth The stroke width of boundary lines.
 * @property bezier Whether curved lines are used for area boundaries.
 * @property zoomControlsVisible Whether zoom controls are shown in expanded dense mode.
 * @property yAxisLabelsVisible Whether Y-axis labels are shown.
 * @property yAxisLabelColor The color of Y-axis labels.
 * @property yAxisLabelSize The text size of Y-axis labels.
 * @property yAxisLabelCount Number of Y-axis labels.
 * @property xAxisLabelsVisible Whether X-axis labels are shown.
 * @property xAxisLabelColor The color of X-axis labels.
 * @property xAxisLabelSize The text size of X-axis labels.
 * @property xAxisLabelMaxCount Maximum number of X-axis labels to display.
 */
@Immutable
class StackedAreaChartStyle(
    val modifier: Modifier,
    val chartViewStyle: ChartViewStyle,
    val areaColor: Color,
    val areaColors: List<Color>,
    val fillAlpha: Float,
    val lineVisible: Boolean,
    val lineColor: Color,
    val lineColors: List<Color>,
    val lineWidth: Float,
    val bezier: Boolean,
    val zoomControlsVisible: Boolean,
    val yAxisLabelsVisible: Boolean,
    val yAxisLabelColor: Color,
    val yAxisLabelSize: TextUnit,
    val yAxisLabelCount: Int,
    val xAxisLabelsVisible: Boolean,
    val xAxisLabelColor: Color,
    val xAxisLabelSize: TextUnit,
    val xAxisLabelMaxCount: Int,
) : Style {
    override fun getProperties(): List<Pair<String, Any>> =
        listOf(
            StackedAreaChartStyle::areaColor.name to areaColor,
            StackedAreaChartStyle::areaColors.name to areaColors,
            StackedAreaChartStyle::fillAlpha.name to fillAlpha,
            StackedAreaChartStyle::lineVisible.name to lineVisible,
            StackedAreaChartStyle::lineColor.name to lineColor,
            StackedAreaChartStyle::lineColors.name to lineColors,
            StackedAreaChartStyle::lineWidth.name to lineWidth,
            StackedAreaChartStyle::bezier.name to bezier,
            StackedAreaChartStyle::zoomControlsVisible.name to zoomControlsVisible,
            StackedAreaChartStyle::yAxisLabelsVisible.name to yAxisLabelsVisible,
            StackedAreaChartStyle::yAxisLabelColor.name to yAxisLabelColor,
            StackedAreaChartStyle::yAxisLabelSize.name to yAxisLabelSize,
            StackedAreaChartStyle::yAxisLabelCount.name to yAxisLabelCount,
            StackedAreaChartStyle::xAxisLabelsVisible.name to xAxisLabelsVisible,
            StackedAreaChartStyle::xAxisLabelColor.name to xAxisLabelColor,
            StackedAreaChartStyle::xAxisLabelSize.name to xAxisLabelSize,
            StackedAreaChartStyle::xAxisLabelMaxCount.name to xAxisLabelMaxCount,
        )
}

object StackedAreaChartDefaults {
    @Composable
    private fun defaultXAxisLabelColor() = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)

    @Composable
    private fun defaultYAxisLabelColor() = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)

    /**
     * Returns a StackedAreaChartStyle with the provided parameters or their default values.
     *
     * @param areaColor The fallback fill color used when `areaColors` is empty. Defaults to the primary theme color.
     * @param areaColors The explicit fill colors used for stacked areas. Defaults to an empty list.
     * @param fillAlpha The alpha value applied to area fills. Defaults to 0.4f in light theme and 0.6f in dark theme.
     * @param lineVisible Whether boundary lines are shown on top of filled areas. Defaults to true.
     * @param lineColor The fallback boundary line color used when `lineColors` is empty. Defaults to the primary theme color.
     * @param lineColors The explicit boundary line colors. Defaults to an empty list.
     * @param lineWidth The stroke width of boundary lines. Defaults to 4f.
     * @param bezier Whether curved lines are used for area boundaries. Defaults to false.
     * @param chartViewStyle The style to be applied to the chart view. Defaults to `ChartViewDefaults.style()`.
     * @param zoomControlsVisible Whether zoom controls are shown in expanded dense mode. Defaults to true.
     * @param yAxisLabelsVisible Whether Y-axis labels are shown. Defaults to true.
     * @param yAxisLabelColor The color of Y-axis labels. Defaults to a theme-based onSurface variant.
     * @param yAxisLabelSize The text size of Y-axis labels. Defaults to 11.sp.
     * @param yAxisLabelCount Number of Y-axis labels. Defaults to 5.
     * @param xAxisLabelsVisible Whether X-axis labels are shown. Defaults to true.
     * @param xAxisLabelColor The color of X-axis labels. Defaults to a theme-based onSurface variant.
     * @param xAxisLabelSize The text size of X-axis labels. Defaults to 11.sp.
     * @param xAxisLabelMaxCount Maximum number of X-axis labels to display. Defaults to 6.
     */
    @Composable
    fun style(
        areaColor: Color = MaterialTheme.colorScheme.primary,
        areaColors: List<Color> = emptyList(),
        fillAlpha: Float = defaultChartAlpha(),
        lineVisible: Boolean = true,
        lineColor: Color = MaterialTheme.colorScheme.primary,
        lineColors: List<Color> = emptyList(),
        lineWidth: Float = 4f,
        bezier: Boolean = false,
        chartViewStyle: ChartViewStyle = ChartViewDefaults.style(),
        zoomControlsVisible: Boolean = true,
        yAxisLabelsVisible: Boolean = true,
        yAxisLabelColor: Color = defaultYAxisLabelColor(),
        yAxisLabelSize: TextUnit = 11.sp,
        yAxisLabelCount: Int = 5,
        xAxisLabelsVisible: Boolean = true,
        xAxisLabelColor: Color = defaultXAxisLabelColor(),
        xAxisLabelSize: TextUnit = 11.sp,
        xAxisLabelMaxCount: Int = 6,
    ): StackedAreaChartStyle {
        val modifier: Modifier = chartViewStyle.fillMaxSizeChartModifier()

        return StackedAreaChartStyle(
            modifier = modifier,
            chartViewStyle = chartViewStyle,
            areaColor = areaColor,
            areaColors = areaColors,
            fillAlpha = fillAlpha.coerceIn(0f, 1f),
            lineVisible = lineVisible,
            lineColor = lineColor,
            lineColors = lineColors,
            lineWidth = lineWidth.coerceAtLeast(0f),
            bezier = bezier,
            zoomControlsVisible = zoomControlsVisible,
            yAxisLabelsVisible = yAxisLabelsVisible,
            yAxisLabelColor = yAxisLabelColor,
            yAxisLabelSize = yAxisLabelSize,
            yAxisLabelCount = yAxisLabelCount,
            xAxisLabelsVisible = xAxisLabelsVisible,
            xAxisLabelColor = xAxisLabelColor,
            xAxisLabelSize = xAxisLabelSize,
            xAxisLabelMaxCount = xAxisLabelMaxCount,
        )
    }
}
