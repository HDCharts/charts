package io.github.dautovicharis.charts.style

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.dautovicharis.charts.model.ChartValueFormatter
import io.github.dautovicharis.charts.model.ChartValueFormatters

/**
 * Immutable v3 style for vertical [io.github.dautovicharis.charts.BarChart].
 *
 * Configuration is grouped into [bars], [range], [grid], [axis], and [selectionLine]
 * blocks. The chart-level selection state is a top-level composable parameter, not part
 * of this style. Compose with [BarChartDefaults.style] for theme-aware defaults.
 *
 * @property chartContainerStyle The shared container presentation.
 * @property bars Bar visual block (colors, spacing, minimum width).
 * @property range Optional fixed Y-axis range.
 * @property grid Horizontal grid configuration.
 * @property axis Axis lines and X/Y label configuration.
 * @property selectionLine Selection indicator configuration.
 * @property zoomControlsVisible Whether zoom controls appear in the chart header.
 */
@Stable
class BarChartStyle(
    val chartContainerStyle: ChartContainerStyle,
    val bars: BarBarsStyle,
    val range: BarRangeStyle,
    val grid: BarGridStyle,
    val axis: BarAxisStyle,
    val selectionLine: BarSelectionLineStyle,
    val zoomControlsVisible: Boolean,
)

/**
 * Defaults factory for [BarChartStyle]. All parameters have theme-aware defaults.
 */
object BarChartDefaults {
    /** Default formatter for chart value readouts. */
    val valueFormatter: ChartValueFormatter = ChartValueFormatters.Default

    /** Default axis formatter, omitting only the terminal `.0` on whole values. */
    val axisValueFormatter: ChartValueFormatter =
        ChartValueFormatter { ChartValueFormatters.Default.format(it).removeSuffix(".0") }

    /**
     * Returns a [BarChartStyle] with the provided parameters or their default values.
     */
    @Composable
    fun style(
        chartContainerStyle: ChartContainerStyle = ChartContainerDefaults.style(),
        bars: BarBarsStyle = bars(),
        range: BarRangeStyle = range(),
        grid: BarGridStyle = grid(),
        axis: BarAxisStyle = axis(),
        selectionLine: BarSelectionLineStyle = selectionLine(),
        zoomControlsVisible: Boolean = true,
    ): BarChartStyle =
        BarChartStyle(
            chartContainerStyle = chartContainerStyle,
            bars = bars,
            range = range,
            grid = grid,
            axis = axis,
            selectionLine = selectionLine,
            zoomControlsVisible = zoomControlsVisible,
        )

    /**
     * Returns a [BarBarsStyle] with bar-aware defaults.
     *
     * @param color The fallback bar color.
     * @param colors Optional explicit per-bar colors; must match bar count or be empty.
     * @param alpha The bar alpha. Defaults to `defaultChartAlpha()`.
     * @param space The spacing between bars. Defaults to 10.dp.
     * @param minBarWidth The minimum width of each bar. Defaults to 10.dp.
     */
    @Composable
    fun bars(
        color: Color = MaterialTheme.colorScheme.primary,
        colors: List<Color> = emptyList(),
        alpha: Float = defaultChartAlpha(),
        space: Dp = 10.dp,
        minBarWidth: Dp = 10.dp,
    ): BarBarsStyle =
        BarBarsStyle(
            color = color,
            colors = colors,
            alpha = alpha,
            space = space,
            minBarWidth = minBarWidth,
        )

    /**
     * Returns a [BarRangeStyle] for the optional fixed Y-axis range.
     */
    fun range(
        min: Double? = null,
        max: Double? = null,
    ): BarRangeStyle = BarRangeStyle(min = min, max = max)

    /**
     * Returns a [BarGridStyle] for horizontal grid configuration.
     * The default stroke width preserves one physical pixel at the current density.
     */
    @Composable
    fun grid(
        visible: Boolean = true,
        steps: Int = 4,
        color: Color =
            androidx.compose.material3.MaterialTheme.colorScheme.onSurface
                .copy(alpha = 0.15f),
        lineWidth: Dp = with(LocalDensity.current) { 1f.toDp() },
    ): BarGridStyle =
        BarGridStyle(
            visible = visible,
            steps = steps,
            color = color,
            lineWidth = lineWidth,
        )

    /**
     * Returns a [BarAxisStyle] for axis and label configuration.
     * The default stroke width preserves one physical pixel at the current density.
     */
    @Composable
    fun axis(
        visible: Boolean = true,
        color: Color =
            androidx.compose.material3.MaterialTheme.colorScheme.onSurface
                .copy(alpha = 0.3f),
        lineWidth: Dp = with(LocalDensity.current) { 1f.toDp() },
        xLabels: AxisLabelStyle = xLabels(),
        yLabels: AxisLabelStyle = yLabels(),
    ): BarAxisStyle =
        BarAxisStyle(
            visible = visible,
            color = color,
            lineWidth = lineWidth,
            xLabels = xLabels,
            yLabels = yLabels,
        )

    /**
     * Returns an [AxisLabelStyle] for X-axis labels.
     */
    @Composable
    fun xLabels(
        visible: Boolean = true,
        color: Color =
            androidx.compose.material3.MaterialTheme.colorScheme.onSurface
                .copy(alpha = 0.75f),
        size: androidx.compose.ui.unit.TextUnit = 11.sp,
        count: Int = 6,
    ): AxisLabelStyle =
        AxisLabelStyle(
            visible = visible,
            color = color,
            size = size,
            count = count,
        )

    /**
     * Returns an [AxisLabelStyle] for Y-axis labels.
     */
    @Composable
    fun yLabels(
        visible: Boolean = true,
        color: Color =
            androidx.compose.material3.MaterialTheme.colorScheme.onSurface
                .copy(alpha = 0.75f),
        size: androidx.compose.ui.unit.TextUnit = 11.sp,
        count: Int = 5,
    ): AxisLabelStyle =
        AxisLabelStyle(
            visible = visible,
            color = color,
            size = size,
            count = count,
        )

    /**
     * Returns a [BarSelectionLineStyle] for the selection indicator.
     * The default stroke width preserves one physical pixel at the current density.
     */
    @Composable
    fun selectionLine(
        visible: Boolean = true,
        color: Color =
            androidx.compose.material3.MaterialTheme.colorScheme.primary
                .copy(alpha = 0.6f),
        width: Dp = with(LocalDensity.current) { 1f.toDp() },
    ): BarSelectionLineStyle =
        BarSelectionLineStyle(
            visible = visible,
            color = color,
            width = width,
        )
}
