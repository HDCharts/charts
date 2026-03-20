package io.github.dautovicharis.charts.style

import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
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
 * A class that defines the style for a Radar Chart.
 *
 * @property modifier The modifier to be applied to the chart.
 * @property chartViewStyle The style to be applied to the chart view.
 * @property gridColor The color of the grid lines.
 * @property gridLineWidth The width of the grid lines.
 * @property gridSteps The number of grid rings.
 * @property gridVisible A boolean indicating whether the grid is visible.
 * @property axisLineColor The color of the axis lines.
 * @property axisLineWidth The width of the axis lines.
 * @property axisVisible A boolean indicating whether axis lines are visible.
 * @property axisLabelColor The color of the axis labels.
 * @property axisLabelSize The size of the axis labels.
 * @property axisLabelPadding The padding between the chart and axis labels.
 * @property axisLabelVisible A boolean indicating whether axis labels are visible.
 * @property categoryLegendVisible A boolean indicating whether category legend items are visible.
 * @property categoryColors The colors used for category pins and legend items.
 * @property categoryPinSize The size of the category pins.
 * @property categoryPinsVisible A boolean indicating whether category pins are visible.
 * @property pointColorSameAsLine A boolean indicating whether the point color matches the line color.
 * @property pointColor The color of the points.
 * @property pointSize The size of the points.
 * @property pointVisible A boolean indicating whether points are visible.
 * @property lineColor The default color of the radar lines.
 * @property lineColors The colors of the radar lines.
 * @property lineWidth The width of the radar lines.
 * @property fillAlpha The alpha value for the filled area under the radar lines.
 * @property fillVisible A boolean indicating whether the filled area is visible.
 */
@Immutable
class RadarChartStyle(
    val modifier: Modifier,
    val chartViewStyle: ChartViewStyle,
    val gridColor: Color,
    val gridLineWidth: Float,
    val gridSteps: Int,
    val gridVisible: Boolean,
    val axisLineColor: Color,
    val axisLineWidth: Float,
    val axisVisible: Boolean,
    val axisLabelColor: Color,
    val axisLabelSize: TextUnit,
    val axisLabelPadding: Dp,
    val axisLabelVisible: Boolean,
    val categoryLegendVisible: Boolean,
    val categoryColors: List<Color>,
    val categoryPinSize: Float,
    val categoryPinsVisible: Boolean,
    val pointColorSameAsLine: Boolean,
    val pointColor: Color,
    val pointSize: Float,
    val pointVisible: Boolean,
    val lineColor: Color,
    val lineColors: List<Color>,
    val lineWidth: Float,
    val fillAlpha: Float,
    val fillVisible: Boolean,
) : Style {
    /**
     * Returns a list of the properties of the RadarChartStyle.
     */
    override fun getProperties(): List<Pair<String, Any>> =
        listOf(
            RadarChartStyle::gridColor.name to gridColor,
            RadarChartStyle::gridLineWidth.name to gridLineWidth,
            RadarChartStyle::gridSteps.name to gridSteps,
            RadarChartStyle::gridVisible.name to gridVisible,
            RadarChartStyle::axisLineColor.name to axisLineColor,
            RadarChartStyle::axisLineWidth.name to axisLineWidth,
            RadarChartStyle::axisVisible.name to axisVisible,
            RadarChartStyle::axisLabelColor.name to axisLabelColor,
            RadarChartStyle::axisLabelSize.name to axisLabelSize,
            RadarChartStyle::axisLabelPadding.name to axisLabelPadding,
            RadarChartStyle::axisLabelVisible.name to axisLabelVisible,
            RadarChartStyle::categoryLegendVisible.name to categoryLegendVisible,
            RadarChartStyle::categoryColors.name to categoryColors,
            RadarChartStyle::categoryPinSize.name to categoryPinSize,
            RadarChartStyle::categoryPinsVisible.name to categoryPinsVisible,
            RadarChartStyle::pointColor.name to pointColor,
            RadarChartStyle::pointSize.name to pointSize,
            RadarChartStyle::pointVisible.name to pointVisible,
            RadarChartStyle::lineColor.name to lineColor,
            RadarChartStyle::lineColors.name to lineColors,
            RadarChartStyle::lineWidth.name to lineWidth,
            RadarChartStyle::fillAlpha.name to fillAlpha,
            RadarChartStyle::fillVisible.name to fillVisible,
        )
}

/**
 * An object that provides default styles for a Radar Chart.
 */
object RadarChartDefaults {
    @Composable
    private fun defaultGridColor() = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.22f)

    @Composable
    private fun defaultAxisLineColor() = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)

    @Composable
    private fun defaultAxisLabelColor() = MaterialTheme.colorScheme.onSurface

    @Composable
    private fun defaultPointColor() = MaterialTheme.colorScheme.tertiary

    /**
     * Returns a RadarChartStyle with the provided parameters or their default values.
     */
    @Composable
    fun style(
        gridColor: Color = defaultGridColor(),
        gridLineWidth: Float = 1f,
        gridSteps: Int = 5,
        gridVisible: Boolean = true,
        axisLineColor: Color = defaultAxisLineColor(),
        axisLineWidth: Float = 1f,
        axisVisible: Boolean = true,
        axisLabelColor: Color = defaultAxisLabelColor(),
        axisLabelSize: TextUnit = 12.sp,
        axisLabelPadding: Dp = 8.dp,
        axisLabelVisible: Boolean = false,
        categoryLegendVisible: Boolean = true,
        categoryColors: List<Color> = emptyList(),
        categoryPinSize: Float = Float.NaN,
        categoryPinsVisible: Boolean = true,
        pointColor: Color = defaultPointColor(),
        pointSize: Float = 9f,
        pointVisible: Boolean = true,
        lineColor: Color = MaterialTheme.colorScheme.primary,
        lineColors: List<Color> = emptyList(),
        lineWidth: Float = 3f,
        fillAlpha: Float = defaultChartAlpha(light = 0.25f, dark = 0.2f),
        fillVisible: Boolean = true,
        chartViewStyle: ChartViewStyle = ChartViewDefaults.style(),
    ): RadarChartStyle {
        val modifier: Modifier =
            Modifier
                .wrapContentSize()
                .padding(chartViewStyle.innerPadding)
                .aspectRatio(1f)

        val pointColorSameAsLine = pointColor == defaultPointColor()
        val resolvedCategoryPinSize =
            if (categoryPinSize.isNaN()) {
                pointSize * 0.7f
            } else {
                categoryPinSize
            }

        return RadarChartStyle(
            modifier = modifier,
            chartViewStyle = chartViewStyle,
            gridColor = gridColor,
            gridLineWidth = gridLineWidth,
            gridSteps = gridSteps,
            gridVisible = gridVisible,
            axisLineColor = axisLineColor,
            axisLineWidth = axisLineWidth,
            axisVisible = axisVisible,
            axisLabelColor = axisLabelColor,
            axisLabelSize = axisLabelSize,
            axisLabelPadding = axisLabelPadding,
            axisLabelVisible = axisLabelVisible,
            categoryLegendVisible = categoryLegendVisible,
            categoryColors = categoryColors,
            categoryPinSize = resolvedCategoryPinSize,
            categoryPinsVisible = categoryPinsVisible,
            pointColorSameAsLine = pointColorSameAsLine,
            pointColor = pointColor,
            pointSize = pointSize,
            pointVisible = pointVisible,
            lineColor = lineColor,
            lineColors = lineColors,
            lineWidth = lineWidth,
            fillAlpha = fillAlpha,
            fillVisible = fillVisible,
        )
    }
}
