package io.github.hdcharts.app.docs

// BEGIN WIKI EXAMPLE: bar-basic-usage
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.hdcharts.bar.BarChart
import io.github.hdcharts.core.model.toChartData

private data class DailyCashFlow(
    val day: String,
    val amount: Double,
)

@Composable
fun ShowDailyCashFlow() {
    val cashFlow =
        listOf(
            DailyCashFlow("Mon", 45.0),
            DailyCashFlow("Tue", -12.0),
            DailyCashFlow("Wed", 38.0),
            DailyCashFlow("Thu", 27.0),
            DailyCashFlow("Fri", -19.0),
        )
    val data =
        cashFlow.toChartData(
            value = { it.amount },
            category = { it.day },
            seriesName = "Net cash flow",
        )

    BarChart(
        data = data,
        modifier = Modifier.fillMaxWidth().height(280.dp),
        title = "Daily Net Cash Flow",
    )
}
// END WIKI EXAMPLE: bar-basic-usage
