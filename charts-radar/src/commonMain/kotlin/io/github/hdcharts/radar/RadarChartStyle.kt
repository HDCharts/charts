package io.github.hdcharts.radar

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import io.github.hdcharts.core.internal.clampAlpha
import io.github.hdcharts.core.internal.clampGridSteps
import io.github.hdcharts.core.internal.clampSize
import io.github.hdcharts.core.internal.clampTextSize
import io.github.hdcharts.core.internal.palette.resolvePaletteColors
import io.github.hdcharts.core.style.ChartContainerDefaults
import io.github.hdcharts.core.style.ChartContainerStyle
import io.github.hdcharts.core.style.StyleDefaults
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

@Immutable
data class RadarGridStyle(
    val visible: Boolean,
    val color: Color,
    val lineWidth: Dp,
    val steps: Int,
)

@Immutable
data class RadarAxesStyle(
    val visible: Boolean,
    val lineColor: Color,
    val lineWidth: Dp,
    val labelColor: Color,
    val labelSize: TextUnit,
    val labelPadding: Dp,
    val labelVisible: Boolean,
)

@Immutable
data class RadarPolygonStyle(
    val fillVisible: Boolean,
    val fillAlpha: Float,
    val lineColor: Color,
    val lineColors: ImmutableList<Color>,
    val lineWidth: Dp,
) {
    constructor(
        fillVisible: Boolean,
        fillAlpha: Float,
        lineColor: Color,
        lineColors: List<Color>,
        lineWidth: Dp,
    ) : this(
        fillVisible = fillVisible,
        fillAlpha = fillAlpha,
        lineColor = lineColor,
        lineColors = lineColors.toImmutableList(),
        lineWidth = lineWidth,
    )

    /**
     * Returns the polygon line colors the chart draws for [seriesCount] series.
     *
     * A single series uses [lineColor]. Multiple series use [lineColors] when set, or
     * generated shades of [lineColor] when [lineColors] is empty.
     */
    fun resolveLineColors(seriesCount: Int): ImmutableList<Color> =
        resolvePaletteColors(
            baseColor = lineColor,
            colors = lineColors,
            count = seriesCount,
            singleItemUsesBase = true,
        )
}

@Immutable
data class RadarPointStyle(
    val visible: Boolean,
    val color: Color,
    val colorSameAsLine: Boolean,
    val size: Dp,
)

/**
 * Selection indicator configuration for radar charts.
 *
 * Dragging selects an axis: its points are drawn at [pointSize], and the points and axis labels of
 * the other axes are drawn at [unselectedAlpha]. Tapping a series outline focuses the
 * series: the other series are drawn at [unfocusedSeriesAlpha], and repeated taps where series
 * overlap move to the next.
 *
 * @property visible Whether selection shows on the chart. `false` draws every series, point, and label as usual.
 * @property pointSize The radius of the selected axis's points.
 * @property unselectedAlpha The alpha multiplier for the data points and labels of the other axes
 * while an axis is selected, in `0..1`. `1f` keeps them solid.
 * @property unfocusedSeriesAlpha The alpha multiplier for the other series while a series is focused,
 * in `0..1`. `1f` keeps every series solid.
 */
@Immutable
data class RadarSelectionStyle(
    val visible: Boolean,
    val pointSize: Dp,
    val unselectedAlpha: Float,
    val unfocusedSeriesAlpha: Float,
)

@Immutable
class RadarChartStyle(
    val chartContainerStyle: ChartContainerStyle,
    val grid: RadarGridStyle,
    val axes: RadarAxesStyle,
    val polygon: RadarPolygonStyle,
    val points: RadarPointStyle,
    val selection: RadarSelectionStyle,
)

object RadarChartDefaults {
    /** Returns a [RadarChartStyle] with the provided parameters or their default values. */
    @Composable
    fun style(
        chartContainerStyle: ChartContainerStyle = ChartContainerDefaults.style(),
        grid: RadarGridStyle = grid(),
        axes: RadarAxesStyle = axes(),
        polygon: RadarPolygonStyle = polygon(),
        points: RadarPointStyle = points(),
        selection: RadarSelectionStyle = selection(),
    ): RadarChartStyle =
        RadarChartStyle(
            chartContainerStyle = chartContainerStyle,
            grid = grid,
            axes = axes,
            polygon = polygon,
            points = points,
            selection = selection,
        )

    /** Returns a [RadarGridStyle] for the grid rings. */
    @Composable
    fun grid(
        visible: Boolean = true,
        color: Color = StyleDefaults.gridColor,
        lineWidth: Dp = StyleDefaults.lineWidth,
        steps: Int = StyleDefaults.gridSteps,
    ): RadarGridStyle = RadarGridStyle(visible = visible, color = color, lineWidth = lineWidth, steps = steps)

    /** Returns a [RadarAxesStyle] for the axis lines and labels. */
    @Composable
    fun axes(
        visible: Boolean = true,
        lineColor: Color = StyleDefaults.gridColor,
        lineWidth: Dp = StyleDefaults.lineWidth,
        labelColor: Color = StyleDefaults.axisLabelColor,
        labelSize: TextUnit = StyleDefaults.axisLabelSize,
        labelPadding: Dp = StyleDefaults.axisLabelPadding,
        labelVisible: Boolean = StyleDefaults.radarAxisLabelsVisible,
    ): RadarAxesStyle =
        RadarAxesStyle(
            visible = visible,
            lineColor = lineColor,
            lineWidth = lineWidth,
            labelColor = labelColor,
            labelSize = labelSize,
            labelPadding = labelPadding,
            labelVisible = labelVisible,
        )

    /** Returns a [RadarPolygonStyle] for the series polygons. */
    @Composable
    fun polygon(
        fillVisible: Boolean = true,
        fillAlpha: Float = StyleDefaults.radarFillAlpha,
        lineColor: Color = StyleDefaults.seriesColor,
        lineColors: List<Color> = emptyList(),
        lineWidth: Dp = StyleDefaults.seriesLineWidth,
    ): RadarPolygonStyle =
        RadarPolygonStyle(
            fillVisible = fillVisible,
            fillAlpha = fillAlpha,
            lineColor = lineColor,
            lineColors = lineColors,
            lineWidth = lineWidth,
        )

    /** Returns a [RadarPointStyle] for the data points. */
    @Composable
    fun points(
        visible: Boolean = StyleDefaults.pointsVisible,
        color: Color = StyleDefaults.pointColor,
        colorSameAsLine: Boolean = true,
        size: Dp = StyleDefaults.pointSize,
    ): RadarPointStyle =
        RadarPointStyle(
            visible = visible,
            color = color,
            colorSameAsLine = colorSameAsLine,
            size = size,
        )

    /** Returns a [RadarSelectionStyle] for axis selection and series focus. */
    @Composable
    fun selection(
        visible: Boolean = true,
        pointSize: Dp = StyleDefaults.selectedPointSize,
        unselectedAlpha: Float = StyleDefaults.unselectedAlpha,
        unfocusedSeriesAlpha: Float = StyleDefaults.radarUnfocusedSeriesAlpha,
    ): RadarSelectionStyle =
        RadarSelectionStyle(
            visible = visible,
            pointSize = pointSize,
            unselectedAlpha = unselectedAlpha,
            unfocusedSeriesAlpha = unfocusedSeriesAlpha,
        )
}

/** Returns [this] with alphas, sizes, and grid steps clamped to drawable values. */
internal fun RadarChartStyle.clamped(density: Density): RadarChartStyle =
    RadarChartStyle(
        chartContainerStyle = chartContainerStyle,
        grid =
            grid.copy(
                lineWidth = grid.lineWidth.clampSize(fallback = StyleDefaults.lineWidth, density = density),
                steps = grid.steps.clampGridSteps(),
            ),
        axes =
            axes.copy(
                lineWidth = axes.lineWidth.clampSize(fallback = StyleDefaults.lineWidth, density = density),
                labelSize = axes.labelSize.clampTextSize(fallback = StyleDefaults.axisLabelSize, density = density),
                labelPadding =
                    axes.labelPadding.clampSize(
                        fallback = StyleDefaults.axisLabelPadding,
                        density = density,
                    ),
            ),
        polygon =
            polygon.copy(
                fillAlpha = polygon.fillAlpha.clampAlpha(),
                lineWidth = polygon.lineWidth.clampSize(fallback = StyleDefaults.seriesLineWidth, density = density),
            ),
        points = points.copy(size = points.size.clampSize(fallback = StyleDefaults.pointSize, density = density)),
        selection =
            selection.copy(
                pointSize =
                    selection.pointSize.clampSize(fallback = StyleDefaults.selectedPointSize, density = density),
                unselectedAlpha = selection.unselectedAlpha.clampAlpha(),
                unfocusedSeriesAlpha =
                    selection.unfocusedSeriesAlpha.clampAlpha(),
            ),
    )
