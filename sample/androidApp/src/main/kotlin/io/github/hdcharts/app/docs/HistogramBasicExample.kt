package io.github.hdcharts.app.docs

// BEGIN WIKI EXAMPLE: histogram-basic-usage
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.hdcharts.core.model.toChartData
import io.github.hdcharts.histogram.HistogramChart

private data class RequestDurationBin(
    val interval: String,
    val requestCount: Double,
)

@Composable
fun ShowRequestDurationDistribution() {
    // Counts have already been grouped into these equal-width intervals.
    val bins =
        listOf(
            RequestDurationBin("0–25 ms", 4.0),
            RequestDurationBin("25–50 ms", 18.0),
            RequestDurationBin("50–75 ms", 47.0),
            RequestDurationBin("75–100 ms", 72.0),
            RequestDurationBin("100–125 ms", 58.0),
            RequestDurationBin("125–150 ms", 31.0),
        )
    val data =
        bins.toChartData(
            value = { it.requestCount },
            category = { it.interval },
            seriesName = "Requests",
        )

    HistogramChart(
        data = data,
        modifier = Modifier.fillMaxWidth().height(280.dp),
        title = "Request Duration",
    )
}
// END WIKI EXAMPLE: histogram-basic-usage
