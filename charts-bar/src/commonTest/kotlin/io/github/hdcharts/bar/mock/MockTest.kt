package io.github.hdcharts.bar.mock

import io.github.hdcharts.core.model.ChartData
import io.github.hdcharts.core.model.toChartData

internal object MockTest {
    const val TITLE = "Title"

    val data: ChartData =
        listOf(10.0, 20.0, 30.0, 40.0).toChartData()
}
