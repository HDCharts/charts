package io.github.hdcharts.charts.style

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.hdcharts.charts.internal.common.palette.resolvePaletteColors
import io.github.hdcharts.charts.model.ChartValueFormatter
import io.github.hdcharts.charts.model.ChartValueFormatters
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
    ) : this(color, alpha, colors.toImmutableList(), strokeWidth, bezier)

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
}

@Immutable
data class LinePointStyle(
    val color: Color,
    val size: Dp,
    val visible: Boolean,
)

@Immutable
data class LineSelectionStyle(
    val color: Color,
    val size: Dp,
    val activeSize: Dp,
    val visible: Boolean,
)

@Immutable
data class LineAxisStyle(
    val visible: Boolean,
    val color: Color,
    val lineWidth: Dp,
    val xLabels: AxisLabelStyle,
    val yLabels: AxisLabelStyle,
)

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
    ): LineChartStyle =
        LineChartStyle(chartContainerStyle, line, points, selection, axis, range, legend, zoomControlsVisible)

    @Composable
    fun line(
        color: Color = MaterialTheme.colorScheme.primary,
        alpha: Float = defaultChartAlpha(),
        colors: List<Color> = emptyList(),
        strokeWidth: Dp = 2.dp,
        bezier: Boolean = true,
    ): LineVisualStyle = LineVisualStyle(color, alpha.coerceIn(0f, 1f), colors, strokeWidth, bezier)

    @Composable
    fun points(
        color: Color = MaterialTheme.colorScheme.tertiary,
        size: Dp = 4.dp,
        visible: Boolean = false,
    ): LinePointStyle = LinePointStyle(color, size, visible)

    @Composable
    fun selection(
        color: Color = MaterialTheme.colorScheme.tertiary,
        size: Dp = 3.dp,
        activeSize: Dp = 5.dp,
        visible: Boolean = true,
    ): LineSelectionStyle = LineSelectionStyle(color, size, activeSize, visible)

    @Composable
    fun axis(
        visible: Boolean = true,
        color: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
        lineWidth: Dp = 1.dp,
        xLabels: AxisLabelStyle = xLabels(),
        yLabels: AxisLabelStyle = yLabels(),
    ): LineAxisStyle = LineAxisStyle(visible, color, lineWidth, xLabels, yLabels)

    /**
     * Returns a [LineRangeStyle] for the optional fixed Y-axis range.
     */
    fun range(
        min: Double? = null,
        max: Double? = null,
    ): LineRangeStyle = LineRangeStyle(min = min, max = max)

    @Composable
    fun xLabels(
        visible: Boolean = true,
        color: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
        size: TextUnit = 11.sp,
        count: Int = 6,
    ): AxisLabelStyle = AxisLabelStyle(visible, color, size, count)

    @Composable
    fun yLabels(
        visible: Boolean = true,
        color: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
        size: TextUnit = 11.sp,
        count: Int = 5,
    ): AxisLabelStyle = AxisLabelStyle(visible, color, size, count)

    /**
     * Returns a [LegendStyle] with the provided visibility.
     *
     * The chart renders the legend only when the data has more than one series
     * and [visible] is true. Pass `visible = false` to suppress the legend for
     * multi-series data.
     *
     * @param visible Whether the legend is visible. Defaults to true.
     */
    @Composable
    fun legend(visible: Boolean = true): LegendStyle = LegendDefaults.style(visible = visible)

    val valueFormatter: ChartValueFormatter = ChartValueFormatters.Default
    val axisValueFormatter: ChartValueFormatter =
        ChartValueFormatter { value ->
            ChartValueFormatters.Default.format(value).removeSuffix(".0")
        }
}
