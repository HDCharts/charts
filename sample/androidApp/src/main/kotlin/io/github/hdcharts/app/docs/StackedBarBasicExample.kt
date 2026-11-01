package io.github.hdcharts.app.docs

// BEGIN WIKI EXAMPLE: stacked-bar-basic-usage
import androidx.compose.runtime.Composable
import io.github.hdcharts.core.model.toChartData
import io.github.hdcharts.stackedbar.StackedBarChart

@Composable
fun ShowQuarterlyRevenue() {
    val data =
        listOf(
            "Online" to listOf(280.0, 520.0, 760.0, 1_080.0),
            "Retail" to listOf(480.0, 760.0, 1_050.0, 1_400.0),
            "Wholesale" to listOf(360.0, 540.0, 620.0, 720.0),
        ).toChartData(categories = listOf("Q1", "Q2", "Q3", "Q4"))

    StackedBarChart(
        data = data,
        title = "Quarterly Revenue by Channel",
    )
}
// END WIKI EXAMPLE: stacked-bar-basic-usage
