package io.github.hdcharts.charts.smoke

import androidx.compose.runtime.Composable
import io.github.hdcharts.charts.LineChart
import io.github.hdcharts.charts.model.toChartData

val lineOnlySmokeDataSet =
    listOf(10.0, 20.0, 15.0).toChartData(
        categories = listOf("A", "B", "C"),
        seriesName = "Smoke",
    )

@Composable
fun LineOnlySmokeChart() {
    LineChart(
        data = lineOnlySmokeDataSet,
        interactionEnabled = false,
        animateOnStart = false,
    )
}
