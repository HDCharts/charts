package io.github.dautovicharis.charts.internal.barchart

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import io.github.dautovicharis.charts.internal.InternalChartsApi
import io.github.dautovicharis.charts.internal.NO_SELECTION
import io.github.dautovicharis.charts.internal.common.composable.Chart
import io.github.dautovicharis.charts.model.ChartData
import io.github.dautovicharis.charts.model.ChartSelection
import io.github.dautovicharis.charts.model.ChartValueFormatter
import io.github.dautovicharis.charts.style.BarChartStyle
import io.github.dautovicharis.charts.internal.common.model.ChartData as InternalChartData

/** Shared rendering boundary for already validated bar and histogram data. */
@InternalChartsApi
@Composable
fun BarChartInternalPlot(
    data: ChartData,
    title: String?,
    style: BarChartStyle,
    selection: ChartSelection,
    selectedIndex: Int,
    interactionEnabled: Boolean,
    animateOnStart: Boolean,
    aggregate: Boolean,
    valueFormatter: ChartValueFormatter,
    axisValueFormatter: ChartValueFormatter,
    modifier: Modifier = Modifier,
    chartTag: String? = null,
) {
    val chartData =
        remember(data) {
            InternalChartData(
                data.series.single().values.mapIndexed { index, value ->
                    data.categories.getOrNull(index).orEmpty() to value
                },
            )
        }
    Chart(style.chartContainerStyle, modifier) {
        Box(modifier = if (chartTag == null) Modifier else Modifier.testTag(chartTag)) {
            BarChart(
                chartData = chartData,
                title = title.orEmpty(),
                style = style.toInternal(),
                interactionEnabled = interactionEnabled,
                animateOnStart = animateOnStart,
                selectedBarIndex = selectedIndex,
                onValueChanged = { index ->
                    if (index == NO_SELECTION) selection.clear() else selection.select(index)
                },
                aggregate = aggregate,
                valueFormatter = valueFormatter,
                axisValueFormatter = axisValueFormatter,
            )
        }
    }
}

/** Keep the selection lifecycle alive across validation-error and valid plot states. */
@InternalChartsApi
@Composable
fun rememberBarSelection(
    data: ChartData,
    selection: ChartSelection,
): Int {
    var previousData by remember(selection) { mutableStateOf(data) }
    val count =
        data.series
            .singleOrNull()
            ?.values
            ?.size ?: 0
    val selectedIndex = selection.selectedIndex?.takeIf { previousData == data && it in 0 until count }
    LaunchedEffect(data, selection, selection.selectedIndex) {
        if (previousData != data || selection.selectedIndex?.let { it !in 0 until count } == true) {
            previousData = data
            selection.clear()
        }
    }
    return selectedIndex ?: NO_SELECTION
}
