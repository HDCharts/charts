package io.github.hdcharts.app.docs

// BEGIN WIKI EXAMPLE: ring-gauge-basic-usage
import androidx.compose.runtime.Composable
import io.github.hdcharts.core.model.toChartData
import io.github.hdcharts.gauge.RingGaugeChart

private data class TeamProgress(
    val metric: String,
    val percentComplete: Double,
)

@Composable
fun ShowTeamProgress() {
    val progress =
        listOf(
            TeamProgress("Design", 82.0),
            TeamProgress("Implementation", 64.0),
            TeamProgress("Testing", 47.0),
        )
    val data =
        progress.toChartData(
            value = { it.percentComplete },
            category = { it.metric },
        )

    RingGaugeChart(data = data, title = "Release Progress")
}
// END WIKI EXAMPLE: ring-gauge-basic-usage
