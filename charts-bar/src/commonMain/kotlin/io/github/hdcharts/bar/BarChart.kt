package io.github.hdcharts.bar

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.hdcharts.bar.internal.BarChartEntry
import io.github.hdcharts.bar.internal.rememberBarSelection
import io.github.hdcharts.core.model.ChartData
import io.github.hdcharts.core.model.ChartSelection
import io.github.hdcharts.core.model.ChartValueFormatter
import io.github.hdcharts.core.model.rememberChartSelection
import io.github.hdcharts.core.style.BarChartDefaults
import io.github.hdcharts.core.style.BarChartStyle

/**
 * Displays one indexed series of finite Double values as vertical bars.
 *
 * [modifier] applies to both the chart and validation errors. [title] is optional and
 * independent of category labels. Empty categories hide X labels; selected readouts
 * then show only the formatted value. [selection] always identifies a source bar,
 * including when compact mode displays bucket averages. Tapping a bucket selects its
 * middle source bar; selecting the same bucket again clears selection.
 *
 * Replacing [data] clears selection; resizing or changing density does not. Disabling
 * [interactionEnabled] disables all user controls, but programmatic selection still renders.
 * [animateOnStart] controls initial reveal, not subsequent update animations.
 * [valueFormatter] formats selected raw values; [axisValueFormatter] formats Y ticks.
 */
@Composable
fun BarChart(
    data: ChartData,
    modifier: Modifier = Modifier,
    style: BarChartStyle = BarChartDefaults.style(),
    title: String? = null,
    selection: ChartSelection = rememberChartSelection(),
    interactionEnabled: Boolean = true,
    animateOnStart: Boolean = true,
    valueFormatter: ChartValueFormatter = BarChartDefaults.valueFormatter,
    axisValueFormatter: ChartValueFormatter = BarChartDefaults.axisValueFormatter,
) {
    val selectedIndex = rememberBarSelection(data, selection)
    BarChartEntry(
        data = data,
        modifier = modifier,
        style = style,
        title = title,
        selection = selection,
        selectedIndex = selectedIndex,
        interactionEnabled = interactionEnabled,
        animateOnStart = animateOnStart,
        valueFormatter = valueFormatter,
        axisValueFormatter = axisValueFormatter,
    )
}
