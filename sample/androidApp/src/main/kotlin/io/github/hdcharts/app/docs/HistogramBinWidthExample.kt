package io.github.hdcharts.app.docs

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import io.github.hdcharts.core.model.ChartData
import io.github.hdcharts.core.style.HistogramChartDefaults
import io.github.hdcharts.histogram.HistogramChart

// BEGIN WIKI EXAMPLE: histogram-bin-width
@Composable
fun ShowWiderRequestBins(data: ChartData) {
    HistogramChart(
        data = data,
        style =
            HistogramChartDefaults.style(
                bars = HistogramChartDefaults.bars(minBarWidth = 16.dp),
            ),
    )
}
// END WIKI EXAMPLE: histogram-bin-width
