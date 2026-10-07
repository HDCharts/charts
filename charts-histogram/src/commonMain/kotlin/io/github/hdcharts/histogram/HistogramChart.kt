package io.github.hdcharts.histogram

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import io.github.hdcharts.bar.internal.rememberBarSelection
import io.github.hdcharts.core.model.ChartData
import io.github.hdcharts.core.model.ChartSelection
import io.github.hdcharts.core.model.ChartValueFormatter
import io.github.hdcharts.core.model.rememberChartSelection
import io.github.hdcharts.core.style.BarChartDefaults
import io.github.hdcharts.core.style.BarChartStyle
import io.github.hdcharts.core.style.HistogramChartDefaults
import io.github.hdcharts.core.style.HistogramChartStyle
import io.github.hdcharts.histogram.internal.HistogramChartEntry

/**
 * Displays one series of precomputed, finite, nonnegative bin heights at equal visual widths.
 *
 * [modifier] applies to both the chart and validation errors. [title] is optional and
 * independent of category labels. Empty categories hide X labels; selected readouts
 * then show only the formatted value. [selection] always identifies a source bar.
 * Tapping a bar selects it; selecting the same bar again clears selection.
 *
 * Replacing [data] clears selection; resizing or changing density does not. Disabling
 * [interactionEnabled] disables all user controls, but programmatic selection still renders.
 * [animateOnStart] controls initial reveal, not subsequent update animations.
 * [selectedValueFormatter] formats selected raw values; [axisValueFormatter] formats Y ticks.
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
    selectedValueFormatter: ChartValueFormatter = BarChartDefaults.selectedValueFormatter,
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
    val selectedIndex = rememberBarSelection(data, selection)
    HistogramChartEntry(
        data = data,
        modifier = modifier,
        style = barStyle,
        title = title,
        selection = selection,
        selectedIndex = selectedIndex,
        interactionEnabled = interactionEnabled,
        animateOnStart = animateOnStart,
        selectedValueFormatter = selectedValueFormatter,
        axisValueFormatter = axisValueFormatter,
    )
}
