package io.github.hdcharts.core.style

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import io.github.hdcharts.core.internal.InternalChartsApi
import io.github.hdcharts.core.internal.clampAlpha
import io.github.hdcharts.core.internal.clampGridSteps
import io.github.hdcharts.core.internal.clampSize
import io.github.hdcharts.core.model.ChartValueFormatter
import io.github.hdcharts.core.model.ChartValueFormatters

/**
 * Immutable v3 style for vertical [io.github.hdcharts.bar.BarChart].
 *
 * Configuration is grouped into [bars], [range], [grid], [axis], and [selection]
 * blocks. The chart-level selection state is a top-level composable parameter, not part
 * of this style. Compose with [BarChartDefaults.style] for theme-aware defaults.
 *
 * @property chartContainerStyle The shared container presentation.
 * @property bars Bar visual block (colors, spacing, minimum width).
 * @property range Optional fixed Y-axis range.
 * @property grid Horizontal grid configuration.
 * @property axis Axis lines and X/Y label configuration.
 * @property selection Selection indicator configuration.
 * @property zoomControlsVisible Whether zoom controls appear in the chart header.
 */
@Stable
class BarChartStyle(
    val chartContainerStyle: ChartContainerStyle,
    val bars: BarBarsStyle,
    val range: BarRangeStyle,
    val grid: BarGridStyle,
    val axis: BarAxisStyle,
    val selection: BarSelectionStyle,
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
        selection: BarSelectionStyle = selection(),
        zoomControlsVisible: Boolean = true,
    ): BarChartStyle =
        BarChartStyle(
            chartContainerStyle = chartContainerStyle,
            bars = bars,
            range = range,
            grid = grid,
            axis = axis,
            selection = selection,
            zoomControlsVisible = zoomControlsVisible,
        )

    /**
     * Returns a [BarBarsStyle] with bar-aware defaults.
     *
     * @param color The fallback bar color.
     * @param colors Optional explicit per-bar colors; must match bar count or be empty.
     * @param alpha The bar alpha. Defaults to 1f.
     * @param space The spacing between bars. Defaults to 10.dp.
     * @param minBarWidth The minimum width of each bar. Defaults to 10.dp.
     */
    @Composable
    fun bars(
        color: Color = StyleDefaults.seriesColor,
        colors: List<Color> = emptyList(),
        alpha: Float = StyleDefaults.seriesAlpha,
        space: Dp = StyleDefaults.barSpacing,
        minBarWidth: Dp = StyleDefaults.minBarWidth,
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
     */
    @Composable
    fun grid(
        visible: Boolean = true,
        steps: Int = StyleDefaults.gridSteps,
        color: Color = StyleDefaults.gridColor,
        lineWidth: Dp = StyleDefaults.lineWidth,
    ): BarGridStyle =
        BarGridStyle(
            visible = visible,
            steps = steps,
            color = color,
            lineWidth = lineWidth,
        )

    /**
     * Returns a [BarAxisStyle] for axis and label configuration.
     */
    @Composable
    fun axis(
        visible: Boolean = true,
        color: Color = StyleDefaults.axisColor,
        lineWidth: Dp = StyleDefaults.lineWidth,
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
        color: Color = StyleDefaults.axisLabelColor,
        size: TextUnit = StyleDefaults.axisLabelSize,
        maxCount: Int? = null,
    ): AxisLabelStyle =
        AxisLabelStyle(
            visible = visible,
            color = color,
            size = size,
            maxCount = maxCount,
        )

    /**
     * Returns an [AxisLabelStyle] for Y-axis labels.
     */
    @Composable
    fun yLabels(
        visible: Boolean = true,
        color: Color = StyleDefaults.axisLabelColor,
        size: TextUnit = StyleDefaults.axisLabelSize,
        maxCount: Int? = null,
    ): AxisLabelStyle =
        AxisLabelStyle(
            visible = visible,
            color = color,
            size = size,
            maxCount = maxCount,
        )

    /**
     * Returns a [BarSelectionStyle] for the selection indicator.
     */
    @Composable
    fun selection(
        visible: Boolean = true,
        color: Color = StyleDefaults.selectionColor,
        width: Dp = StyleDefaults.lineWidth,
        unselectedAlpha: Float = StyleDefaults.unselectedAlpha,
    ): BarSelectionStyle =
        BarSelectionStyle(
            visible = visible,
            color = color,
            width = width,
            unselectedAlpha = unselectedAlpha,
        )
}

/** Returns [this] with alphas, sizes, and grid steps clamped to drawable values. */
@InternalChartsApi
fun BarChartStyle.clamped(density: Density): BarChartStyle =
    BarChartStyle(
        chartContainerStyle = chartContainerStyle,
        bars =
            bars.copy(
                alpha = bars.alpha.clampAlpha(),
                space = bars.space.clampSize(fallback = StyleDefaults.barSpacing, density = density),
                minBarWidth = bars.minBarWidth.clampSize(fallback = StyleDefaults.minBarWidth, density = density),
            ),
        range = range,
        grid =
            grid.copy(
                steps = grid.steps.clampGridSteps(),
                lineWidth = grid.lineWidth.clampSize(fallback = StyleDefaults.lineWidth, density = density),
            ),
        axis = axis.copy(lineWidth = axis.lineWidth.clampSize(fallback = StyleDefaults.lineWidth, density = density)),
        selection =
            selection.copy(
                width = selection.width.clampSize(fallback = StyleDefaults.lineWidth, density = density),
                unselectedAlpha = selection.unselectedAlpha.clampAlpha(),
            ),
        zoomControlsVisible = zoomControlsVisible,
    )
