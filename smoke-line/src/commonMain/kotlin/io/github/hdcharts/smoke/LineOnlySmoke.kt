package io.github.hdcharts.smoke

import androidx.compose.runtime.Composable
import io.github.hdcharts.core.model.toChartData
import io.github.hdcharts.line.LineChart

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
