package io.github.hdcharts.stackedbar

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.hdcharts.core.internal.NO_SELECTION
import io.github.hdcharts.core.model.ChartData
import io.github.hdcharts.core.model.ChartSelection
import io.github.hdcharts.core.model.ChartValueFormatter
import io.github.hdcharts.core.model.rememberChartSelection
import io.github.hdcharts.core.model.rememberSelectionLifecycle
import io.github.hdcharts.stackedbar.internal.StackedBarChartEntry

/**
 * Displays nonnegative absolute stacks from one aligned series per segment.
 * [selectedValueFormatter] formats the selected values; [axisValueFormatter] formats Y ticks.
 */
@Composable
fun StackedBarChart(
    data: ChartData,
    modifier: Modifier = Modifier,
    style: StackedBarChartStyle = StackedBarChartDefaults.style(),
    title: String? = null,
    selection: ChartSelection = rememberChartSelection(),
    interactionEnabled: Boolean = true,
    animateOnStart: Boolean = true,
    selectedValueFormatter: ChartValueFormatter = StackedBarChartDefaults.selectedValueFormatter,
    axisValueFormatter: ChartValueFormatter = StackedBarChartDefaults.axisValueFormatter,
) {
    val pointCount =
        data.series
            .firstOrNull()
            ?.values
            ?.size ?: 0
    val selectedIndex = selection.selectedIndex?.takeIf { it in 0 until pointCount } ?: NO_SELECTION
    rememberSelectionLifecycle(
        selection = selection,
        data = data,
        itemCount = pointCount,
    )
    StackedBarChartEntry(
        data = data,
        modifier = modifier,
        style = style,
        title = title,
        selectedIndex = selectedIndex,
        interactionEnabled = interactionEnabled,
        animateOnStart = animateOnStart,
        selectedValueFormatter = selectedValueFormatter,
        axisValueFormatter = axisValueFormatter,
        onValueChanged = { index ->
            if (index == NO_SELECTION) selection.clear() else selection.select(index)
        },
    )
}
