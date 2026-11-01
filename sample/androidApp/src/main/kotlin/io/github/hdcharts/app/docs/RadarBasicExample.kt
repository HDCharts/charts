package io.github.hdcharts.app.docs

// BEGIN WIKI EXAMPLE: radar-basic-usage
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.hdcharts.core.model.toChartData
import io.github.hdcharts.radar.RadarChart

internal data class ReadinessMetric(
    val category: String,
    val score: Double,
) {
    companion object {
        const val TITLE = "Platform Readiness"
        const val SERIES_NAME = "Current release"

        val sample =
            listOf(
                ReadinessMetric("Performance", 84.0),
                ReadinessMetric("Reliability", 79.0),
                ReadinessMetric("Usability", 76.0),
                ReadinessMetric("Security", 88.0),
                ReadinessMetric("Scalability", 82.0),
                ReadinessMetric("Observability", 74.0),
            )
    }
}

@Composable
fun ShowPlatformReadiness(modifier: Modifier = Modifier) {
    val data =
        ReadinessMetric.sample.toChartData(
            value = ReadinessMetric::score,
            category = ReadinessMetric::category,
            seriesName = ReadinessMetric.SERIES_NAME,
        )

    RadarChart(
        data = data,
        modifier = modifier,
        title = ReadinessMetric.TITLE,
    )
}
// END WIKI EXAMPLE: radar-basic-usage
