package io.github.hdcharts.histogram

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import io.github.hdcharts.bar.internal.BarChartInternalPlot
import io.github.hdcharts.bar.internal.rememberBarSelection
import io.github.hdcharts.core.internal.TestTags
import io.github.hdcharts.core.internal.axis.validateAxisLabels
import io.github.hdcharts.core.internal.composable.ChartErrors
import io.github.hdcharts.core.internal.validateRange
import io.github.hdcharts.core.internal.validateSingleSeries
import io.github.hdcharts.core.model.ChartData
import io.github.hdcharts.core.model.ChartSelection
import io.github.hdcharts.core.model.ChartValueFormatter
import io.github.hdcharts.core.model.rememberChartSelection
import io.github.hdcharts.core.style.BarChartDefaults
import io.github.hdcharts.core.style.BarChartStyle
import io.github.hdcharts.core.style.HistogramChartDefaults
import io.github.hdcharts.core.style.HistogramChartStyle
import io.github.hdcharts.core.style.clamped
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
                selection = style.selection,
                zoomControlsVisible = style.zoomControlsVisible,
            )
        }
    val density = LocalDensity.current
    val selectedIndex = rememberBarSelection(data, selection)
    val errors =
        remember(data, style, density) {
            validateSingleSeries(data = data, colorCount = style.bars.colors.size, allowNegative = false) +
                validateRange(min = style.range.min, max = style.range.max) +
                validateAxisLabels(style.axis.xLabels, style.axis.yLabels, density)
        }
    val drawStyle = remember(barStyle, density) { barStyle.clamped(density) }
    if (errors.isNotEmpty()) {
        ChartErrors(style.chartContainerStyle, errors.toImmutableList(), modifier)
    } else {
        BarChartInternalPlot(
            data = data,
            title = title,
            style = drawStyle,
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
