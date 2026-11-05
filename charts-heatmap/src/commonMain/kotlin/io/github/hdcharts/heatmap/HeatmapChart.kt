package io.github.hdcharts.heatmap

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.hdcharts.core.model.ChartSelection
import io.github.hdcharts.core.model.ChartValueFormatter
import io.github.hdcharts.core.model.rememberChartSelection
import io.github.hdcharts.heatmap.internal.HeatmapChartEntry

/**
 * Draws a matrix of values on two category axes, colored by [HeatmapChartStyle.colorScale].
 *
 * [HeatmapChartStyle.renderMode] draws discrete cells by default, or a color field interpolated from
 * the cell values. Both modes share the data, color scale, legend, and selection.
 *
 * @param selection Selection by cell index, `row * columns.size + column`; see [HeatmapData.cellIndex].
 * Missing cells cannot be selected. In both modes, the selection reports the cell's own value, never
 * an interpolated one.
 * @param valueFormatter Formats the legend bounds and the selected value.
 */
@Composable
fun HeatmapChart(
    data: HeatmapData,
    modifier: Modifier = Modifier,
    style: HeatmapChartStyle = HeatmapChartDefaults.style(),
    title: String? = null,
    selection: ChartSelection = rememberChartSelection(),
    interactionEnabled: Boolean = true,
    animateOnStart: Boolean = true,
    valueFormatter: ChartValueFormatter = HeatmapChartDefaults.valueFormatter,
) {
    HeatmapChartEntry(
        data = data,
        modifier = modifier,
        style = style,
        title = title,
        selection = selection,
        interactionEnabled = interactionEnabled,
        animateOnStart = animateOnStart,
        valueFormatter = valueFormatter,
    )
}
