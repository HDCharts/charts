package io.github.dautovicharis.charts.style

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
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
 * @property bars Bar visual block. Default [BarChartDefaults.bars] produces adjacent
 * bins (zero spacing) and a 0.dp minimum bar width; call sites can override.
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
        bars: BarBarsStyle = BarChartDefaults.bars(space = 0.dp, minBarWidth = 0.dp),
        range: BarRangeStyle = BarChartDefaults.range(min = 0f),
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
}
