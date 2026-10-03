package io.github.hdcharts.radar

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.hdcharts.core.internal.InternalChartsApi
import io.github.hdcharts.core.internal.NO_SELECTION
import io.github.hdcharts.core.model.ChartData
import io.github.hdcharts.core.model.ChartSelection
import io.github.hdcharts.core.model.rememberChartSelection
import io.github.hdcharts.core.model.rememberSelectionLifecycle
import io.github.hdcharts.radar.internal.RadarChartEntry

/**
 * Displays one or more radar polygons sharing common axes.
 *
 * [selection] holds the axis selected by dragging, and a tap clears it. [seriesSelection] holds the
 * series focused by tapping its outline.
 */
@OptIn(InternalChartsApi::class)
@Composable
fun RadarChart(
    data: ChartData,
    modifier: Modifier = Modifier,
    style: RadarChartStyle = RadarChartDefaults.style(),
    title: String? = null,
    selection: ChartSelection = rememberChartSelection(),
    seriesSelection: ChartSelection = rememberChartSelection(),
    interactionEnabled: Boolean = true,
    animateOnStart: Boolean = true,
) {
    val axisCount =
        data.series
            .firstOrNull()
            ?.values
            ?.size ?: 0
    val selectedIndex = selection.selectedIndex?.takeIf { it in 0 until axisCount } ?: NO_SELECTION
    rememberSelectionLifecycle(
        selection = selection,
        data = data,
        itemCount = axisCount,
    )
    val hasMultipleSeries = data.series.size > 1
    val focusedSeriesIndex =
        seriesSelection.selectedIndex?.takeIf { hasMultipleSeries && it in data.series.indices } ?: NO_SELECTION
    rememberSelectionLifecycle(
        selection = seriesSelection,
        data = data,
        itemCount = if (hasMultipleSeries) data.series.size else 0,
    )
    RadarChartEntry(
        data = data,
        modifier = modifier,
        style = style,
        title = title,
        selectedIndex = selectedIndex,
        focusedSeriesIndex = focusedSeriesIndex,
        selection = selection,
        seriesSelection = seriesSelection,
        interactionEnabled = interactionEnabled,
        animateOnStart = animateOnStart,
    )
}
