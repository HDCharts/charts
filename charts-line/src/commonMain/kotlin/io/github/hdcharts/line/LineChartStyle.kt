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
import io.github.hdcharts.core.style.AxesDefaults
import io.github.hdcharts.core.style.AxesStyle
import io.github.hdcharts.core.style.AxisLabelStyle
import io.github.hdcharts.core.style.ChartContainerDefaults
import io.github.hdcharts.core.style.ChartContainerStyle
import io.github.hdcharts.core.style.ChartGradient
import io.github.hdcharts.core.style.LegendDefaults
import io.github.hdcharts.core.style.LegendStyle
import io.github.hdcharts.core.style.StyleDefaults
import io.github.hdcharts.core.style.clamp
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

/**
 * Style of the series lines.
 *
 * @property color Color used for a single series and to generate a multi-series palette.
 * @property colors Optional multi-series palette. When set, its count must match the series count.
 * A single series uses [color], even when a one-color palette is supplied.
 * @property strokeWidth Line stroke width in density-independent pixels.
 * @property bezier Whether points are joined by smooth curves rather than straight segments.
 * @property alpha The line alpha. Replaces the line colors' alpha, and multiplies a gradient's own alphas.
 * @property gradient Shared gradient that paints every line. Its span picks one gradient per line or
 * one across the plot. `null` draws solid lines.
 */
@Immutable
data class LineVisualStyle(
    val color: Color,
    val alpha: Float,
    val colors: ImmutableList<Color>,
    val strokeWidth: Dp,
    val bezier: Boolean,
    val gradient: ChartGradient?,
) {
    /** Copies the supplied list into an immutable list. */
    constructor(
        color: Color,
        alpha: Float,
        colors: List<Color>,
        strokeWidth: Dp,
        bezier: Boolean,
        gradient: ChartGradient? = null,
    ) : this(
        color = color,
        alpha = alpha,
        colors = colors.toImmutableList(),
        strokeWidth = strokeWidth,
        bezier = bezier,
        gradient = gradient,
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
     * Returns this block with [alpha], [strokeWidth], and [gradient] clamped. Names every field instead of
     * using `copy`, so a field added to the constructor fails to compile here until it is dealt with.
     */
    internal fun clamp(density: Density) =
        LineVisualStyle(
            color = color,
            alpha = alpha.clampAlpha(),
            colors = colors,
            strokeWidth = strokeWidth.clampSize(fallback = StyleDefaults.seriesLineWidth, density = density),
            bezier = bezier,
            gradient = gradient?.clamp(),
        )
}

/**
 * Markers drawn at the source points of each line.
 *
 * @property color Marker color.
 * @property size Marker radius in density-independent pixels.
 * @property visible Whether markers are drawn at rest. Selection markers use [LineSelectionStyle].
 */
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
 * [LinePointStyle.visible] is `true`.
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

/**
 * Optional fixed Y-axis range for line charts.
 *
 * Null bounds use the data range. Set bounds must be finite.
 * If both are set and `min >= max`, the chart uses the data range.
 * If only one is set, the other is clamped to keep `min <= max`.
 *
 * @property min Optional minimum value.
 * @property max Optional maximum value.
 */
@Immutable
data class LineRangeStyle(
    val min: Double?,
    val max: Double?,
)

/**
 * Presentation for single- and multi-line charts.
 *
 * @property chartContainerStyle Shared content spacing and title and subtitle text styles.
 * @property line Series colors, strokes, interpolation, and gradient.
 * @property points Markers drawn at rest.
 * @property selection Selection line and marker appearance.
 * @property axes Axis lines, grid, and X/Y labels.
 * @property range Optional fixed Y-axis bounds.
 * @property legend Series legend visibility.
 * @property zoomControlsVisible Whether zoom controls appear when interaction and scrolling are enabled.
 */
@Immutable
class LineChartStyle(
    val chartContainerStyle: ChartContainerStyle,
    val line: LineVisualStyle,
    val points: LinePointStyle,
    val selection: LineSelectionStyle,
    val axes: AxesStyle,
    val range: LineRangeStyle,
    val legend: LegendStyle,
    val zoomControlsVisible: Boolean,
)

/** Theme-aware style factories and default value formatters for [LineChartStyle]. */
object LineChartDefaults {
    /** Returns a [LineChartStyle] with the provided parameters or their default values. */
    @Composable
    fun style(
        chartContainerStyle: ChartContainerStyle = ChartContainerDefaults.style(),
        line: LineVisualStyle = line(),
        points: LinePointStyle = points(),
        selection: LineSelectionStyle = selection(),
        axes: AxesStyle = AxesDefaults.style(),
        range: LineRangeStyle = range(),
        legend: LegendStyle = legend(),
        zoomControlsVisible: Boolean = true,
    ) = LineChartStyle(
        chartContainerStyle = chartContainerStyle,
        line = line,
        points = points,
        selection = selection,
        axes = axes,
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
        gradient: ChartGradient? = null,
    ) = LineVisualStyle(
        color = color,
        alpha = alpha,
        colors = colors,
        strokeWidth = strokeWidth,
        bezier = bezier,
        gradient = gradient,
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
        visible: Boolean = StyleDefaults.axisLabelsVisible,
        color: Color = StyleDefaults.axisLabelColor,
        size: TextUnit = StyleDefaults.axisLabelSize,
        maxCount: Int? = StyleDefaults.axisLabelMaxCount,
    ): AxisLabelStyle = AxisLabelStyle(visible = visible, color = color, size = size, maxCount = maxCount)

    /** Returns an [AxisLabelStyle] for Y-axis labels. */
    @Composable
    fun yLabels(
        visible: Boolean = StyleDefaults.axisLabelsVisible,
        color: Color = StyleDefaults.axisLabelColor,
        size: TextUnit = StyleDefaults.axisLabelSize,
        maxCount: Int? = StyleDefaults.axisLabelMaxCount,
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

    /** Default formatter for selected values in the title and legend. */
    val selectedValueFormatter: ChartValueFormatter = StyleDefaults.selectedValueFormatter

    /** Default axis formatter, omitting only the terminal `.0` on whole values. */
    val axisValueFormatter: ChartValueFormatter = StyleDefaults.axisValueFormatter
}

/** Returns [this] with alphas and sizes clamped to drawable values. */
internal fun LineChartStyle.clamp(density: Density) =
    LineChartStyle(
        chartContainerStyle = chartContainerStyle,
        line = line.clamp(density),
        points = points.clamp(density),
        selection = selection.clamp(density),
        axes = axes.clamp(density),
        range = range,
        legend = legend,
        zoomControlsVisible = zoomControlsVisible,
    )
