package io.github.hdcharts.line

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import io.github.hdcharts.core.internal.clampAlpha
import io.github.hdcharts.core.internal.clampSize
import io.github.hdcharts.core.internal.palette.resolvePaletteColors
import io.github.hdcharts.core.model.ChartValueFormatter
import io.github.hdcharts.core.model.ChartValueFormatters
import io.github.hdcharts.core.style.AxisLabelStyle
import io.github.hdcharts.core.style.ChartContainerDefaults
import io.github.hdcharts.core.style.ChartContainerStyle
import io.github.hdcharts.core.style.LegendDefaults
import io.github.hdcharts.core.style.LegendStyle
import io.github.hdcharts.core.style.StyleDefaults
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

@Immutable
data class LineVisualStyle(
    val color: Color,
    val alpha: Float,
    val colors: ImmutableList<Color>,
    val strokeWidth: Dp,
    val bezier: Boolean,
) {
    constructor(
        color: Color,
        alpha: Float,
        colors: List<Color>,
        strokeWidth: Dp,
        bezier: Boolean,
    ) : this(
        color = color,
        alpha = alpha,
        colors = colors.toImmutableList(),
        strokeWidth = strokeWidth,
        bezier = bezier,
    )

    /**
     * Returns the line colors the chart draws for [seriesCount] series, before [alpha] is applied.
     *
     * A single series uses [color]. Multiple series use [colors] when set, or generated
     * shades of [color] when [colors] is empty.
     */
    fun resolveColors(seriesCount: Int): ImmutableList<Color> =
        resolvePaletteColors(
            baseColor = color,
            colors = colors,
            count = seriesCount,
            singleItemUsesBase = true,
        )

    /**
     * Returns this block with [alpha] and [strokeWidth] clamped. Names every field instead of using
     * `copy`, so a field added to the constructor fails to compile here until it is dealt with.
     */
    internal fun clamp(density: Density) =
        LineVisualStyle(
            color = color,
            alpha = alpha.clampAlpha(),
            colors = colors,
            strokeWidth = strokeWidth.clampSize(fallback = StyleDefaults.seriesLineWidth, density = density),
            bezier = bezier,
        )
}

@Immutable
data class LinePointStyle(
    val color: Color,
    val size: Dp,
    val visible: Boolean,
) {
    /**
     * Returns this block with [size] clamped. Names every field instead of using `copy`, so a field
     * added to the constructor fails to compile here until it is dealt with.
     */
    internal fun clamp(density: Density) =
        LinePointStyle(
            color = color,
            size = size.clampSize(fallback = StyleDefaults.pointSize, density = density),
            visible = visible,
        )
}

/**
 * Selection configuration for line charts.
 *
 * @property visible Whether the selection indicator is visible. `false` keeps the selected point drawn when
 * [LinePointsStyle.visible] is `true`.
 * @property color The selection line color.
 * @property width The selection line stroke width in density-independent pixels.
 * @property markerColor The marker color.
 * @property markerSize The radius of the marker that follows the touch along the line.
 * @property pointSize The radius of the marker on each series' selected point.
 */
@Immutable
data class LineSelectionStyle(
    val visible: Boolean,
    val color: Color,
    val width: Dp,
    val markerColor: Color,
    val markerSize: Dp,
    val pointSize: Dp,
) {
    /**
     * Returns this block with [width], [markerSize] and [pointSize] clamped. Names every field instead
     * of using `copy`, so a field added to the constructor fails to compile here until it is dealt
     * with.
     */
    internal fun clamp(density: Density) =
        LineSelectionStyle(
            visible = visible,
            color = color,
            width = width.clampSize(fallback = StyleDefaults.lineWidth, density = density),
            markerColor = markerColor,
            markerSize = markerSize.clampSize(fallback = StyleDefaults.lineSelectionMarkerSize, density = density),
            pointSize = pointSize.clampSize(fallback = StyleDefaults.selectedPointSize, density = density),
        )
}

@Immutable
data class LineAxisStyle(
    val visible: Boolean,
    val color: Color,
    val lineWidth: Dp,
    val xLabels: AxisLabelStyle,
    val yLabels: AxisLabelStyle,
) {
    /**
     * Returns this block with [lineWidth] clamped. Names every field instead of using `copy`, so a
     * field added to the constructor fails to compile here until it is dealt with.
     *
     * The label styles pass through: an undrawable label size is a validation error, not something
     * to clamp.
     */
    internal fun clamp(density: Density) =
        LineAxisStyle(
            visible = visible,
            color = color,
            lineWidth = lineWidth.clampSize(fallback = StyleDefaults.lineWidth, density = density),
            xLabels = xLabels,
            yLabels = yLabels,
        )
}

/**
 * Optional fixed Y-axis range for line charts.
 *
 * `null` for either bound means the chart derives that bound from data, so [min] and [max]
 * can be set independently. Explicit bounds must be finite. If the resolved range is equal
 * or reversed, both bounds fall back to the data-derived domain.
 *
 * @property min Optional minimum value.
 * @property max Optional maximum value.
 */
@Immutable
data class LineRangeStyle(
    val min: Double?,
    val max: Double?,
)

/** Grouped, Compose-friendly v3 style for single- and multi-line charts. */
@Immutable
class LineChartStyle(
    val chartContainerStyle: ChartContainerStyle,
    val line: LineVisualStyle,
    val points: LinePointStyle,
    val selection: LineSelectionStyle,
    val axis: LineAxisStyle,
    val range: LineRangeStyle,
    val legend: LegendStyle,
    val zoomControlsVisible: Boolean,
)

object LineChartDefaults {
    /** Returns a [LineChartStyle] with the provided parameters or their default values. */
    @Composable
    fun style(
        chartContainerStyle: ChartContainerStyle = ChartContainerDefaults.style(),
        line: LineVisualStyle = line(),
        points: LinePointStyle = points(),
        selection: LineSelectionStyle = selection(),
        axis: LineAxisStyle = axis(),
        range: LineRangeStyle = range(),
        legend: LegendStyle = legend(),
        zoomControlsVisible: Boolean = true,
    ) = LineChartStyle(
        chartContainerStyle = chartContainerStyle,
        line = line,
        points = points,
        selection = selection,
        axis = axis,
        range = range,
        legend = legend,
        zoomControlsVisible = zoomControlsVisible,
    )

    /** Returns a [LineVisualStyle] for the series lines. */
    @Composable
    fun line(
        color: Color = StyleDefaults.seriesColor,
        alpha: Float = StyleDefaults.seriesAlpha,
        colors: List<Color> = emptyList(),
        strokeWidth: Dp = StyleDefaults.seriesLineWidth,
        bezier: Boolean = true,
    ) = LineVisualStyle(
        color = color,
        alpha = alpha,
        colors = colors,
        strokeWidth = strokeWidth,
        bezier = bezier,
    )

    /** Returns a [LinePointStyle] for the data point markers. */
    @Composable
    fun points(
        color: Color = StyleDefaults.pointColor,
        size: Dp = StyleDefaults.pointSize,
        visible: Boolean = StyleDefaults.pointsVisible,
    ): LinePointStyle = LinePointStyle(color = color, size = size, visible = visible)

    /** Returns a [LineSelectionStyle] for the selection line and markers. */
    @Composable
    fun selection(
        visible: Boolean = true,
        color: Color = StyleDefaults.selectionColor,
        width: Dp = StyleDefaults.lineWidth,
        markerColor: Color = StyleDefaults.pointColor,
        markerSize: Dp = StyleDefaults.lineSelectionMarkerSize,
        pointSize: Dp = StyleDefaults.selectedPointSize,
    ) = LineSelectionStyle(
        visible = visible,
        color = color,
        width = width,
        markerColor = markerColor,
        markerSize = markerSize,
        pointSize = pointSize,
    )

    /** Returns a [LineAxisStyle] for axis lines and labels. */
    @Composable
    fun axis(
        visible: Boolean = true,
        color: Color = StyleDefaults.axisColor,
        lineWidth: Dp = StyleDefaults.lineWidth,
        xLabels: AxisLabelStyle = xLabels(),
        yLabels: AxisLabelStyle = yLabels(),
    ) = LineAxisStyle(
        visible = visible,
        color = color,
        lineWidth = lineWidth,
        xLabels = xLabels,
        yLabels = yLabels,
    )

    /**
     * Returns a [LineRangeStyle] for the optional fixed Y-axis range.
     */
    fun range(
        min: Double? = null,
        max: Double? = null,
    ): LineRangeStyle = LineRangeStyle(min = min, max = max)

    /** Returns an [AxisLabelStyle] for X-axis labels. */
    @Composable
    fun xLabels(
        visible: Boolean = true,
        color: Color = StyleDefaults.axisLabelColor,
        size: TextUnit = StyleDefaults.axisLabelSize,
        maxCount: Int? = null,
    ): AxisLabelStyle = AxisLabelStyle(visible = visible, color = color, size = size, maxCount = maxCount)

    /** Returns an [AxisLabelStyle] for Y-axis labels. */
    @Composable
    fun yLabels(
        visible: Boolean = true,
        color: Color = StyleDefaults.axisLabelColor,
        size: TextUnit = StyleDefaults.axisLabelSize,
        maxCount: Int? = null,
    ): AxisLabelStyle = AxisLabelStyle(visible = visible, color = color, size = size, maxCount = maxCount)

    /**
     * Returns a [LegendStyle] with the provided visibility.
     *
     * The chart renders the legend only when the data has more than one series,
     * at least one series has a name, and [visible] is true. Pass `visible = false`
     * to suppress the legend for multi-series data.
     *
     * @param visible Whether the legend is visible. Defaults to true.
     */
    @Composable
    fun legend(visible: Boolean = StyleDefaults.legendVisible): LegendStyle = LegendDefaults.style(visible = visible)

    /** Default formatter for chart value readouts. */
    val valueFormatter: ChartValueFormatter = ChartValueFormatters.Default

    /** Default axis formatter, omitting only the terminal `.0` on whole values. */
    val axisValueFormatter: ChartValueFormatter =
        ChartValueFormatter { value ->
            ChartValueFormatters.Default.format(value).removeSuffix(".0")
        }
}

/** Returns [this] with alphas and sizes clamped to drawable values. */
internal fun LineChartStyle.clamp(density: Density) =
    LineChartStyle(
        chartContainerStyle = chartContainerStyle,
        line = line.clamp(density),
        points = points.clamp(density),
        selection = selection.clamp(density),
        axis = axis.clamp(density),
        range = range,
        legend = legend,
        zoomControlsVisible = zoomControlsVisible,
    )
