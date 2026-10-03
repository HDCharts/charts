package io.github.hdcharts.bar.internal

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import io.github.hdcharts.core.internal.InternalChartsApi
import io.github.hdcharts.core.internal.NO_SELECTION
import io.github.hdcharts.core.internal.model.MultiChartData
import io.github.hdcharts.core.model.ChartData
import io.github.hdcharts.core.model.ChartSelection
import io.github.hdcharts.core.model.ChartValueFormatter
import io.github.hdcharts.core.model.rememberSelectionLifecycle
import io.github.hdcharts.core.style.BarChartStyle

/**
 * The plot bar and histogram share.
 *
 * [data] is validated and converted by the caller's entry, so nothing checks it again here. Bar
 * draws a single series, so it reads the caller's own model and its categories rather than needing
 * anything the model adds.
 */
@InternalChartsApi
@Composable
fun BarChartInternalPlot(
    data: MultiChartData,
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
    Box(modifier = modifier) {
        Box(modifier = if (chartTag == null) Modifier else Modifier.testTag(chartTag)) {
            BarChartImpl(
                chartData = data.data,
                title = data.title,
                style = style,
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
