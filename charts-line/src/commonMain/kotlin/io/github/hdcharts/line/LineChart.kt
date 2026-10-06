package io.github.hdcharts.line

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.hdcharts.core.internal.InternalChartsApi
import io.github.hdcharts.core.internal.NO_SELECTION
import io.github.hdcharts.core.internal.selectedLegendValues
import io.github.hdcharts.core.internal.selectedTitle
import io.github.hdcharts.core.model.ChartData
import io.github.hdcharts.core.model.ChartSelection
import io.github.hdcharts.core.model.ChartValueFormatter
import io.github.hdcharts.core.model.rememberChartSelection
import io.github.hdcharts.core.model.rememberSelectionLifecycle
import io.github.hdcharts.line.internal.LineChartEntry
import io.github.hdcharts.line.internal.LineChartImpl

/**
 * Displays one or more aligned indexed series. A single public entry point handles both
 * single- and multi-line data. Selection always refers to a source X index shared by all series.
 * In compact mode, user interaction selects the middle source index represented by a bucket;
 * programmatic source selection highlights the bucket containing that index.
 *
 * When [data] changes and keeps the number of points and series, the line morphs from its previous
 * shape; changes to either count redraw immediately. Use [LiveLineChart] for a continuously
 * updating window that slides as new points arrive.
 *
 * [interactionEnabled] disables all user controls and returns dense data to the fit view, but
 * programmatic selection still renders.
 */
@OptIn(InternalChartsApi::class)
@Composable
fun LineChart(
    data: ChartData,
    modifier: Modifier = Modifier,
    style: LineChartStyle = LineChartDefaults.style(),
    title: String? = null,
    selection: ChartSelection = rememberChartSelection(),
    interactionEnabled: Boolean = true,
    animateOnStart: Boolean = true,
    valueFormatter: ChartValueFormatter = LineChartDefaults.valueFormatter,
    axisValueFormatter: ChartValueFormatter = LineChartDefaults.axisValueFormatter,
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
    LineChartEntry(
        data = data,
        modifier = modifier,
        style = style,
        title = title,
    ) { internalData, drawStyle ->
        LineChartImpl(
            data = internalData,
            modifier = modifier,
            style = drawStyle,
            interactionEnabled = interactionEnabled,
            animateOnStart = animateOnStart,
            selectedPointIndex = selectedIndex,
            onValueChanged = { index ->
                if (index == NO_SELECTION) selection.clear() else selection.select(index)
            },
            axisValueFormatter = axisValueFormatter,
            legendLabels =
                selectedLegendValues(
                    data = data,
                    selectedIndex = selectedIndex,
                    valueFormatter = valueFormatter,
                ),
            selectedTitle =
                selectedTitle(
                    data = data,
                    selectedIndex = selectedIndex,
                    title = title,
                    valueFormatter = valueFormatter,
                ),
        )
    }
}
