package io.github.hdcharts.app.docs

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.github.hdcharts.core.model.ChartData
import io.github.hdcharts.core.model.ChartValueFormatter
import io.github.hdcharts.core.model.ChartValueFormatters
import io.github.hdcharts.core.model.rememberChartSelection
import io.github.hdcharts.core.model.toChartData
import io.github.hdcharts.gauge.RingGaugeChart
import io.github.hdcharts.gauge.RingGaugeChartDefaults

// BEGIN WIKI EXAMPLE: ring-gauge-range
@Composable
fun ShowTemperatureRange() {
    val data =
        listOf(-25.0, 12.0, 48.0).toChartData(
            categories = listOf("Below range", "In range", "Above range"),
        )

    RingGaugeChart(
        data = data,
        style =
            RingGaugeChartDefaults.style(
                range = RingGaugeChartDefaults.range(min = -20.0, max = 40.0),
            ),
        selectedValueFormatter = ChartValueFormatters.suffix("°C"),
        axisValueFormatter =
            ChartValueFormatter { value ->
                RingGaugeChartDefaults.axisValueFormatter.format(value) + "°C"
            },
    )
}
// END WIKI EXAMPLE: ring-gauge-range

// BEGIN WIKI EXAMPLE: ring-gauge-selection
@Composable
fun SelectProgressRing(data: ChartData) {
    val selection = rememberChartSelection()

    RingGaugeChart(data = data, selection = selection)
}
// END WIKI EXAMPLE: ring-gauge-selection

// BEGIN WIKI EXAMPLE: ring-gauge-styling
@Composable
fun StyledTeamProgress(data: ChartData) {
    RingGaugeChart(
        data = data,
        style =
            RingGaugeChartDefaults.style(
                rings =
                    RingGaugeChartDefaults.rings(
                        colors = listOf(Color(0xFF2563EB), Color(0xFF0F766E), Color(0xFFDB2777)),
                        width = 18.dp,
                        spacing = 6.dp,
                    ),
                track = RingGaugeChartDefaults.track(color = Color(0xFFE5E7EB)),
            ),
    )
}
// END WIKI EXAMPLE: ring-gauge-styling
