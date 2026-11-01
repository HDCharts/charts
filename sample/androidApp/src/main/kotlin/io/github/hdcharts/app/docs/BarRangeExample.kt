package io.github.hdcharts.app.docs

import androidx.compose.runtime.Composable
import io.github.hdcharts.bar.BarChart
import io.github.hdcharts.core.model.ChartData
import io.github.hdcharts.core.style.BarChartDefaults

// BEGIN WIKI EXAMPLE: bar-fixed-range
@Composable
fun CompareCashFlow(data: ChartData) {
    BarChart(
        data = data,
        style =
            BarChartDefaults.style(
                range = BarChartDefaults.range(min = -50.0, max = 50.0),
            ),
    )
}
// END WIKI EXAMPLE: bar-fixed-range
