package io.github.hdcharts.app.docs

// BEGIN WIKI EXAMPLE: radar-basic-usage
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.hdcharts.core.model.toChartData
import io.github.hdcharts.radar.RadarChart

private data class ReadinessMetric(
    val category: String,
    val score: Double,
)

@Composable
fun ShowPlatformReadiness(modifier: Modifier = Modifier) {
    val metrics =
        listOf(
            ReadinessMetric("Performance", 84.0),
            ReadinessMetric("Reliability", 79.0),
            ReadinessMetric("Usability", 76.0),
            ReadinessMetric("Security", 88.0),
            ReadinessMetric("Scalability", 82.0),
            ReadinessMetric("Observability", 74.0),
        )
    val data =
        metrics.toChartData(
            value = { it.score },
            category = { it.category },
            seriesName = "Current release",
        )

    RadarChart(
        data = data,
        modifier = modifier,
        title = "Platform Readiness",
    )
}
// END WIKI EXAMPLE: radar-basic-usage
