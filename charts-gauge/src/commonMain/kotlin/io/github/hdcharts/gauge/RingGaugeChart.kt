package io.github.hdcharts.gauge

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.hdcharts.core.internal.InternalChartsApi
import io.github.hdcharts.core.internal.NO_SELECTION
import io.github.hdcharts.core.model.ChartData
import io.github.hdcharts.core.model.ChartSelection
import io.github.hdcharts.core.model.ChartValueFormatter
import io.github.hdcharts.core.model.rememberChartSelection
import io.github.hdcharts.core.model.rememberSelectionLifecycle
import io.github.hdcharts.gauge.internal.RingGaugeChartEntry

/**
 * A composable function that displays a Ring Gauge Chart.
 *
 * Each value is drawn as its own ring on a half circle, filled from the start of the style's range
 * towards its end, so several values can be compared on the same scale. The first value is the outer
 * ring. Values outside the range stop at the nearest end of the arc. Categories are optional and name
 * the rings in the legend and while they are selected; when present, there must be one per value.
 *
 * Tapping a ring selects it and shows `Category: value` in the title; tapping outside the rings
 * clears the selection. [interactionEnabled] disables tap-to-select, but programmatic selection still
 * renders. [animateOnStart] controls the initial fill, not subsequent update animations.
 *
 * @param data The chart data to display. One series of at least one finite value. Categories are
 * optional; when supplied they must match the value count. Invalid data renders the documented errors
 * instead of a chart.
 * @param modifier The modifier to be applied to the chart. Also forwarded to the error branch when
 * the data fails validation.
 * @param style The style to be applied to the chart. If not provided, the default style will be used.
 * @param title Optional chart title displayed when no ring is selected.
 * @param selectedValueFormatter Formats the selected value in the title.
 * @param axisValueFormatter Formats the range labels under the ends of the arc.
 * @param selection The hoisted selection state. Use [rememberChartSelection] for interactive charts or
 * [io.github.hdcharts.core.model.staticChartSelection] for deterministic preset selections.
 * @param interactionEnabled When `false`, disables tap-to-select.
 * @param animateOnStart When `false`, renders the chart in its final state without the initial fill
 * animation.
 */
@OptIn(InternalChartsApi::class)
@Composable
fun RingGaugeChart(
    data: ChartData,
    modifier: Modifier = Modifier,
    style: RingGaugeChartStyle = RingGaugeChartDefaults.style(),
    title: String? = null,
    selectedValueFormatter: ChartValueFormatter = RingGaugeChartDefaults.selectedValueFormatter,
    axisValueFormatter: ChartValueFormatter = RingGaugeChartDefaults.axisValueFormatter,
    selection: ChartSelection = rememberChartSelection(),
    interactionEnabled: Boolean = true,
    animateOnStart: Boolean = true,
) {
    val ringCount =
        data.series
            .singleOrNull()
            ?.values
            ?.size ?: 0
    rememberSelectionLifecycle(
        selection = selection,
        data = data,
        itemCount = ringCount,
    )
    val selectedIndex = selection.selectedIndex?.takeIf { it in 0 until ringCount } ?: NO_SELECTION

    RingGaugeChartEntry(
        data = data,
        modifier = modifier,
        style = style,
        title = title,
        selectedValueFormatter = selectedValueFormatter,
        axisValueFormatter = axisValueFormatter,
        selectedIndex = selectedIndex,
        selection = selection,
        interactionEnabled = interactionEnabled,
        animateOnStart = animateOnStart,
    )
}
