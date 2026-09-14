package io.github.hdcharts.charts.mock

import io.github.hdcharts.charts.model.ChartSeries
import io.github.hdcharts.charts.model.chartDataOf

internal object MockTest {
    const val TITLE = "Title"

    val data: io.github.hdcharts.charts.model.ChartData =
        chartDataOf(
            categories = listOf("A", "B", "C", "D"),
            ChartSeries(name = "Series", values = listOf(10.0, 20.0, 30.0, 40.0)),
        )
}
