package io.github.hdcharts.charts

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.hdcharts.charts.internal.NO_SELECTION
import io.github.hdcharts.charts.internal.linechart.LineChartEntry
import io.github.hdcharts.charts.internal.linechart.LineChartImpl
import io.github.hdcharts.charts.model.ChartData
import io.github.hdcharts.charts.model.ChartSelection
import io.github.hdcharts.charts.model.ChartValueFormatter
import io.github.hdcharts.charts.model.rememberChartSelection
import io.github.hdcharts.charts.model.rememberSelectionLifecycle
import io.github.hdcharts.charts.style.LineChartDefaults
import io.github.hdcharts.charts.style.LineChartStyle
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList

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
    val selectedCategory = data.categories.getOrNull(selectedIndex)?.takeIf(String::isNotBlank)
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
    ) { internalData ->
        LineChartImpl(
            data = internalData,
            modifier = modifier,
            style = style,
            interactionEnabled = interactionEnabled,
            animateOnStart = animateOnStart,
            selectedPointIndex = selectedIndex,
            onValueChanged = { index ->
                if (index == NO_SELECTION) selection.clear() else selection.select(index)
            },
            axisValueFormatter = axisValueFormatter,
            legendLabels =
                if (selectedIndex != NO_SELECTION && data.series.size > 1 && data.categories.isNotEmpty()) {
                    data.series.map { valueFormatter.format(it.values[selectedIndex]) }.toImmutableList()
                } else {
                    persistentListOf()
                },
            selectedTitle =
                if (selectedIndex == NO_SELECTION) {
                    null
                } else if (data.series.size == 1) {
                    val value = valueFormatter.format(data.series.single().values[selectedIndex])
                    selectedCategory?.let { "$it: $value" } ?: value
                } else {
                    selectedCategory
                },
        )
    }
}
