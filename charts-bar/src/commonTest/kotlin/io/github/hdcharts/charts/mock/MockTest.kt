package io.github.hdcharts.charts.mock

import io.github.hdcharts.charts.model.ChartData
import io.github.hdcharts.charts.model.toChartData

internal object MockTest {
    const val TITLE = "Title"

    val data: ChartData =
        listOf(10.0, 20.0, 30.0, 40.0).toChartData()
}
