package io.github.hdcharts.app.docs

// BEGIN WIKI EXAMPLE: bar-basic-usage
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.hdcharts.bar.BarChart
import io.github.hdcharts.core.model.toChartData

internal data class DailyCashFlow(
    val day: String,
    val amount: Double,
) {
    companion object {
        const val TITLE = "Daily Net Cash Flow"
        const val SERIES_NAME = "Net cash flow"

        val sample =
            listOf(
                DailyCashFlow("Mon", 45.0),
                DailyCashFlow("Tue", -12.0),
                DailyCashFlow("Wed", 38.0),
                DailyCashFlow("Thu", 27.0),
                DailyCashFlow("Fri", -19.0),
            )
    }
}

@Composable
fun ShowDailyCashFlow() {
    val data =
        DailyCashFlow.sample.toChartData(
            value = DailyCashFlow::amount,
            category = DailyCashFlow::day,
            seriesName = DailyCashFlow.SERIES_NAME,
        )

    BarChart(
        data = data,
        modifier = Modifier.fillMaxWidth().height(280.dp),
        title = DailyCashFlow.TITLE,
    )
}
// END WIKI EXAMPLE: bar-basic-usage
