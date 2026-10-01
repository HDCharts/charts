package io.github.hdcharts.core.style

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit

/**
 * Immutable v3 style for [io.github.hdcharts.histogram.HistogramChart].
 *
 * Histograms render pre-binned counts in equal-width adjacent bars. Histogram defaults
 * enforce contiguous bins and a zero baseline by default; consumers can still override
 * spacing, minimum bar width, and explicit range. The chart-level selection state is a
 * top-level composable parameter, not part of this style.
 *
 * @property chartContainerStyle The shared container presentation.
 * @property bars Bar visual block. Default [HistogramChartDefaults.bars] produces adjacent
 * bins (zero spacing) and a 10.dp minimum bar width; call sites can override.
 * @property range Optional fixed Y-axis range. Defaults to a zero minimum so a bin
 * count of zero renders at the baseline.
 * @property grid Horizontal grid configuration.
 * @property axis Axis lines and X/Y label configuration.
 * @property selection Selection indicator configuration.
 * @property zoomControlsVisible Whether zoom controls appear in the chart header.
 */
@Stable
class HistogramChartStyle(
    val chartContainerStyle: ChartContainerStyle,
    val bars: BarBarsStyle,
    val range: BarRangeStyle,
    val grid: BarGridStyle,
    val axis: BarAxisStyle,
    val selection: BarSelectionStyle,
    val zoomControlsVisible: Boolean,
)

/**
 * Defaults factory for [HistogramChartStyle]. Histograms render pre-binned values
 * (one Double per bin) at equal visual widths and a zero baseline by default.
 */
object HistogramChartDefaults {
    /**
     * Returns a [HistogramChartStyle] with the provided parameters or their default values.
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
    ): HistogramChartStyle =
        HistogramChartStyle(
            chartContainerStyle = chartContainerStyle,
            bars = bars,
            range = range,
            grid = grid,
            axis = axis,
            selection = selection,
            zoomControlsVisible = zoomControlsVisible,
        )

    /**
     * Returns a [BarBarsStyle] with adjacent histogram bins by default.
     *
     * @param color The fallback bar color.
     * @param colors Optional explicit per-bin colors; must match bin count or be empty.
     * @param alpha The bar alpha. Defaults to 1f.
     * @param space The spacing between bins. Defaults to 0.dp.
     * @param minBarWidth The minimum width of each bin. Defaults to 10.dp.
     */
    @Composable
    fun bars(
        color: Color = StyleDefaults.seriesColor,
        colors: List<Color> = emptyList(),
        alpha: Float = StyleDefaults.seriesAlpha,
        space: Dp = StyleDefaults.histogramBarSpacing,
        minBarWidth: Dp = StyleDefaults.minBarWidth,
    ): BarBarsStyle =
        BarChartDefaults.bars(
            color = color,
            colors = colors,
            alpha = alpha,
            space = space,
            minBarWidth = minBarWidth,
        )

    /**
     * Returns a [BarRangeStyle] for the optional fixed Y-axis range. Defaults to a zero
     * minimum so a bin count of zero renders at the baseline.
     */
    fun range(
        min: Double? = StyleDefaults.histogramRangeMin,
        max: Double? = null,
    ): BarRangeStyle = BarChartDefaults.range(min = min, max = max)

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
        BarChartDefaults.grid(
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
        BarChartDefaults.axis(
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
        BarChartDefaults.xLabels(
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
        BarChartDefaults.yLabels(
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
        BarChartDefaults.selection(
            visible = visible,
            color = color,
            width = width,
            unselectedAlpha = unselectedAlpha,
        )
}
