package io.github.hdcharts.charts.internal.barchart

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import io.github.hdcharts.charts.internal.InternalChartsApi
import io.github.hdcharts.charts.internal.NO_SELECTION
import io.github.hdcharts.charts.internal.common.composable.Chart
import io.github.hdcharts.charts.model.ChartData
import io.github.hdcharts.charts.model.ChartSelection
import io.github.hdcharts.charts.model.ChartValueFormatter
import io.github.hdcharts.charts.model.rememberSelectionLifecycle
import io.github.hdcharts.charts.style.BarChartStyle
import io.github.hdcharts.charts.internal.common.model.ChartData as InternalChartData

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

/**
 * Validates the [ChartSelection] index against [data] and drives the lifecycle
 * (clear on data identity change or out-of-bounds index) via
 * [rememberSelectionLifecycle]. Returns the validated index, or
 * [NO_SELECTION] if none is valid.
 */
@InternalChartsApi
@Composable
fun rememberBarSelection(
    data: ChartData,
    selection: ChartSelection,
): Int {
    val count =
        data.series
            .singleOrNull()
            ?.values
            ?.size ?: 0
    rememberSelectionLifecycle(
        selection = selection,
        data = data,
        itemCount = count,
    )
    return selection.selectedIndex?.takeIf { it in 0 until count } ?: NO_SELECTION
}
