package io.github.dautovicharis.charts.style

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Immutable v3 style for [io.github.dautovicharis.charts.HistogramChart].
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
 * @property selectionLine Selection indicator configuration.
 * @property zoomControlsVisible Whether zoom controls appear in the chart header.
 */
@Stable
class HistogramChartStyle(
    val chartContainerStyle: ChartContainerStyle,
    val bars: BarBarsStyle,
    val range: BarRangeStyle,
    val grid: BarGridStyle,
    val axis: BarAxisStyle,
    val selectionLine: BarSelectionLineStyle,
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
        range: BarRangeStyle = BarChartDefaults.range(min = 0.0),
        grid: BarGridStyle = BarChartDefaults.grid(),
        axis: BarAxisStyle = BarChartDefaults.axis(),
        selectionLine: BarSelectionLineStyle = BarChartDefaults.selectionLine(),
        zoomControlsVisible: Boolean = true,
    ): HistogramChartStyle =
        HistogramChartStyle(
            chartContainerStyle = chartContainerStyle,
            bars = bars,
            range = range,
            grid = grid,
            axis = axis,
            selectionLine = selectionLine,
            zoomControlsVisible = zoomControlsVisible,
        )

    /**
     * Returns a [BarBarsStyle] with adjacent histogram bins by default.
     *
     * @param color The fallback bar color.
     * @param colors Optional explicit per-bin colors; must match bin count or be empty.
     * @param alpha The bar alpha. Defaults to `defaultChartAlpha()`.
     * @param space The spacing between bins. Defaults to 0.dp.
     * @param minBarWidth The minimum width of each bin. Defaults to 10.dp.
     */
    @Composable
    fun bars(
        color: Color = MaterialTheme.colorScheme.primary,
        colors: List<Color> = emptyList(),
        alpha: Float = defaultChartAlpha(),
        space: Dp = 0.dp,
        minBarWidth: Dp = 10.dp,
    ): BarBarsStyle =
        BarChartDefaults.bars(
            color = color,
            colors = colors,
            alpha = alpha,
            space = space,
            minBarWidth = minBarWidth,
        )
}
