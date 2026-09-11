package io.github.dautovicharis.charts.smoke

import androidx.compose.runtime.Composable
import io.github.dautovicharis.charts.LineChart
import io.github.dautovicharis.charts.model.toChartData

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
