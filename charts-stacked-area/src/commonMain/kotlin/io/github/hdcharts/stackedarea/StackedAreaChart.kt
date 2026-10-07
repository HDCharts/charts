package io.github.hdcharts.stackedarea

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.hdcharts.core.internal.NO_SELECTION
import io.github.hdcharts.core.model.ChartData
import io.github.hdcharts.core.model.ChartSelection
import io.github.hdcharts.core.model.ChartValueFormatter
import io.github.hdcharts.core.model.rememberChartSelection
import io.github.hdcharts.core.model.rememberSelectionLifecycle
import io.github.hdcharts.stackedarea.internal.StackedAreaChartEntry

/**
 * Displays absolute stacked areas from one or more aligned series sharing common X categories.
 * [selectedValueFormatter] formats the selected values; [axisValueFormatter] formats Y ticks.
 */
@Composable
fun StackedAreaChart(
    data: ChartData,
    modifier: Modifier = Modifier,
    style: StackedAreaChartStyle = StackedAreaChartDefaults.style(),
    title: String? = null,
    selection: ChartSelection = rememberChartSelection(),
    interactionEnabled: Boolean = true,
    animateOnStart: Boolean = true,
    selectedValueFormatter: ChartValueFormatter = StackedAreaChartDefaults.selectedValueFormatter,
    axisValueFormatter: ChartValueFormatter = StackedAreaChartDefaults.axisValueFormatter,
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
    StackedAreaChartEntry(
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
