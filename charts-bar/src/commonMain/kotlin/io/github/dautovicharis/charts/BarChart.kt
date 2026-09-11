package io.github.dautovicharis.charts

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import io.github.dautovicharis.charts.internal.barchart.BarChartInternalPlot
import io.github.dautovicharis.charts.internal.barchart.rememberBarSelection
import io.github.dautovicharis.charts.internal.common.composable.ChartErrors
import io.github.dautovicharis.charts.internal.validateBarData
import io.github.dautovicharis.charts.internal.validateBarStyle
import io.github.dautovicharis.charts.model.ChartData
import io.github.dautovicharis.charts.model.ChartSelection
import io.github.dautovicharis.charts.model.ChartValueFormatter
import io.github.dautovicharis.charts.model.rememberChartSelection
import io.github.dautovicharis.charts.style.BarChartDefaults
import io.github.dautovicharis.charts.style.BarChartStyle
import kotlinx.collections.immutable.toImmutableList

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
    val density = LocalDensity.current
    val selectedIndex = rememberBarSelection(data, selection)
    val errors =
        remember(data, style, density) {
            validateBarData(data, style.bars.colors.size) + validateBarStyle(style, density)
        }
    if (errors.isNotEmpty()) {
        ChartErrors(style.chartContainerStyle, errors.toImmutableList(), modifier)
    } else {
        BarChartInternalPlot(
            data = data,
            title = title,
            style = style,
            selection = selection,
            selectedIndex = selectedIndex,
            interactionEnabled = interactionEnabled,
            animateOnStart = animateOnStart,
            aggregate = true,
            valueFormatter = valueFormatter,
            axisValueFormatter = axisValueFormatter,
            modifier = modifier,
        )
    }
}
