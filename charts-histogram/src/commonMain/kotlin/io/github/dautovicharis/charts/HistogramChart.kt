package io.github.dautovicharis.charts

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import io.github.dautovicharis.charts.internal.TestTags
import io.github.dautovicharis.charts.internal.barchart.BarChartInternalPlot
import io.github.dautovicharis.charts.internal.barchart.rememberBarSelection
import io.github.dautovicharis.charts.internal.common.composable.ChartErrors
import io.github.dautovicharis.charts.internal.validateBarStyle
import io.github.dautovicharis.charts.internal.validateHistogramData
import io.github.dautovicharis.charts.model.ChartData
import io.github.dautovicharis.charts.model.ChartSelection
import io.github.dautovicharis.charts.model.ChartValueFormatter
import io.github.dautovicharis.charts.model.rememberChartSelection
import io.github.dautovicharis.charts.style.BarChartDefaults
import io.github.dautovicharis.charts.style.BarChartStyle
import io.github.dautovicharis.charts.style.HistogramChartDefaults
import io.github.dautovicharis.charts.style.HistogramChartStyle
import kotlinx.collections.immutable.toImmutableList

/**
 * Displays one series of precomputed, finite, nonnegative bin heights at equal visual widths.
 * Fractional heights and arbitrary category labels (including open-ended intervals) are supported.
 * Fit mode preserves every bin, even at subpixel widths; it never averages or merges bins.
 * Expanded mode provides minimum-width scrolling. Defaults use adjacent bins and a zero baseline.
 *
 * [selection] identifies a source bin. Replacing [data] clears selection; resizing and density
 * changes preserve it. [interactionEnabled] gates all user controls, not programmatic selection.
 * [modifier] applies to valid and error states. [title] does not generate category labels.
 * [animateOnStart] controls initial reveal only. [valueFormatter] formats selected raw heights,
 * independently of [axisValueFormatter] for Y ticks.
 */
@Composable
fun HistogramChart(
    data: ChartData,
    modifier: Modifier = Modifier,
    style: HistogramChartStyle = HistogramChartDefaults.style(),
    title: String? = null,
    selection: ChartSelection = rememberChartSelection(),
    interactionEnabled: Boolean = true,
    animateOnStart: Boolean = true,
    valueFormatter: ChartValueFormatter = BarChartDefaults.valueFormatter,
    axisValueFormatter: ChartValueFormatter = BarChartDefaults.axisValueFormatter,
) {
    val barStyle =
        remember(style) {
            BarChartStyle(
                chartContainerStyle = style.chartContainerStyle,
                bars = style.bars,
                range = style.range,
                grid = style.grid,
                axis = style.axis,
                selectionLine = style.selectionLine,
                zoomControlsVisible = style.zoomControlsVisible,
            )
        }
    val density = LocalDensity.current
    val selectedIndex = rememberBarSelection(data, selection)
    val errors =
        remember(data, style, density) {
            validateHistogramData(data, style.bars.colors.size) + validateBarStyle(barStyle, density)
        }
    if (errors.isNotEmpty()) {
        ChartErrors(style.chartContainerStyle, errors.toImmutableList(), modifier)
    } else {
        BarChartInternalPlot(
            data = data,
            title = title,
            style = barStyle,
            selection = selection,
            selectedIndex = selectedIndex,
            interactionEnabled = interactionEnabled,
            animateOnStart = animateOnStart,
            aggregate = false,
            valueFormatter = valueFormatter,
            axisValueFormatter = axisValueFormatter,
            modifier = modifier,
            chartTag = TestTags.HISTOGRAM_CHART,
        )
    }
}
