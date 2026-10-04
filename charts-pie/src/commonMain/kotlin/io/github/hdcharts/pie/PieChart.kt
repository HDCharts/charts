package io.github.hdcharts.pie

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import io.github.hdcharts.core.internal.InternalChartsApi
import io.github.hdcharts.core.model.ChartData
import io.github.hdcharts.core.model.ChartSelection
import io.github.hdcharts.core.model.SelectionLifetime
import io.github.hdcharts.core.model.rememberChartSelection
import io.github.hdcharts.core.model.rememberSelectionLifecycle
import io.github.hdcharts.pie.internal.PieChartEntry

internal const val PIE_SELECTION_AUTO_DESELECT_TIMEOUT_MS = 3000L

/**
 * A composable function that displays a Pie Chart.
 *
 * A pie is one series whose values are the slices. Categories are optional and name the slices in
 * the legend and while they are selected; when present, there must be one per value. The slice
 * colors come from the style, either as an explicit palette or as shades generated from its base
 * color.
 *
 * [interactionEnabled] disables user controls (tap-to-select and the auto-deselect timeout), but
 * programmatic selection still renders. [animateOnStart] controls the initial reveal, not subsequent
 * update animations.
 *
 * @param data The chart data to display. One series of at least two nonnegative finite values.
 * Categories are optional; when supplied they must match the value count. Invalid data renders the
 * documented errors instead of a chart.
 * @param modifier The modifier to be applied to the chart. Also forwarded to the error
 * branch when the data fails validation.
 * @param style The style to be applied to the chart. If not provided, the default style will be used.
 * @param title Optional chart title displayed when no slice is selected.
 * @param selection The hoisted selection state. Use [rememberChartSelection] for interactive
 *   charts or [io.github.hdcharts.core.model.staticChartSelection] for deterministic
 *   preset selections.
 * @param interactionEnabled When `false`, disables tap-to-select and the auto-deselect timeout.
 * @param animateOnStart When `false`, renders the chart in its final state without the
 *   initial reveal animation.
 */
@OptIn(InternalChartsApi::class)
@Composable
fun PieChart(
    data: ChartData,
    modifier: Modifier = Modifier,
    style: PieChartStyle = PieChartDefaults.style(),
    title: String? = null,
    selection: ChartSelection = rememberChartSelection(),
    interactionEnabled: Boolean = true,
    animateOnStart: Boolean = true,
) {
    var interactionNonce by remember(data, selection) { mutableStateOf<Long?>(null) }
    val sliceCount =
        data.series
            .firstOrNull()
            ?.values
            ?.size ?: 0
    rememberSelectionLifecycle(
        selection = selection,
        data = data,
        itemCount = sliceCount,
        lifetime =
            if (interactionEnabled) {
                SelectionLifetime.AutoDeselect(PIE_SELECTION_AUTO_DESELECT_TIMEOUT_MS)
            } else {
                SelectionLifetime.Persistent
            },
        autoDeselectTrigger = interactionNonce,
    )

    PieChartEntry(
        data = data,
        modifier = modifier,
        style = style,
        title = title,
        selection = selection,
        interactionEnabled = interactionEnabled,
        animateOnStart = animateOnStart,
        onSelectionInteraction = {
            interactionNonce = (interactionNonce ?: 0L) + 1L
        },
    )
}
