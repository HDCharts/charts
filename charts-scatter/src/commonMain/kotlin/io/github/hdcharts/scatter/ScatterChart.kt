package io.github.hdcharts.scatter

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.hdcharts.core.model.ChartSelection
import io.github.hdcharts.core.model.ChartValueFormatter
import io.github.hdcharts.core.model.rememberChartSelection
import io.github.hdcharts.scatter.internal.ScatterChartEntry

/** Draws points by numeric X/Y coordinates. Set [ScatterPoint.size] to encode bubble area. */
@Composable
fun ScatterChart(
    data: ScatterData,
    modifier: Modifier = Modifier,
    style: ScatterChartStyle = ScatterChartDefaults.style(),
    title: String? = null,
    selection: ChartSelection = rememberChartSelection(),
    interactionEnabled: Boolean = true,
    xValueFormatter: ChartValueFormatter = ScatterChartDefaults.axisValueFormatter,
    yValueFormatter: ChartValueFormatter = xValueFormatter,
    sizeValueFormatter: ChartValueFormatter = xValueFormatter,
) {
    ScatterChartEntry(
        data = data,
        modifier = modifier,
        style = style,
        title = title,
        selection = selection,
        interactionEnabled = interactionEnabled,
        xValueFormatter = xValueFormatter,
        yValueFormatter = yValueFormatter,
        sizeValueFormatter = sizeValueFormatter,
    )
}
