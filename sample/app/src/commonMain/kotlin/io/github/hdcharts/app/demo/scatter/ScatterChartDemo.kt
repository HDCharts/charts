package io.github.hdcharts.app.demo.scatter

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.hdcharts.app.ui.composable.ChartDemo
import io.github.hdcharts.scatter.ScatterChart
import io.github.hdcharts.scatter.ScatterData
import io.github.hdcharts.scatter.ScatterPoint
import io.github.hdcharts.scatter.ScatterSeries

@Composable
fun ScatterChartDemo() {
    val data =
        ScatterData(
            listOf(
                ScatterSeries(
                    "North",
                    listOf(ScatterPoint(1.0, 2.5, 8.0), ScatterPoint(2.0, 4.0, 18.0), ScatterPoint(3.5, 3.2, 12.0)),
                ),
                ScatterSeries(
                    "South",
                    listOf(ScatterPoint(1.5, 1.8, 14.0), ScatterPoint(2.8, 2.7, 6.0), ScatterPoint(4.0, 4.5, 20.0)),
                ),
            ),
        )
    ChartDemo(onRefresh = {}, refreshVisible = false) {
        ScatterChart(data = data, modifier = Modifier.fillMaxSize(), title = "Regional growth")
    }
}
