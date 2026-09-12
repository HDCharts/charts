package io.github.dautovicharis.charts.mock

import io.github.dautovicharis.charts.model.ChartSeries
import io.github.dautovicharis.charts.model.chartDataOf

internal object MockTest {
    const val TITLE = "Title"

    val data: io.github.dautovicharis.charts.model.ChartData =
        chartDataOf(
            categories = listOf("A", "B", "C", "D"),
            ChartSeries(name = "Series", values = listOf(10.0, 20.0, 30.0, 40.0)),
        )
}
