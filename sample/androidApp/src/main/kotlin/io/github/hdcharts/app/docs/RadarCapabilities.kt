package io.github.hdcharts.app.docs

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.github.hdcharts.core.model.ChartData
import io.github.hdcharts.core.model.rememberChartSelection
import io.github.hdcharts.core.model.toChartData
import io.github.hdcharts.radar.RadarChart
import io.github.hdcharts.radar.RadarChartDefaults

// BEGIN WIKI EXAMPLE: radar-multiple-series
@Composable
fun ComparePlatformReleases() {
    val categories = listOf("Performance", "Reliability", "Usability", "Security", "Scalability", "Observability")
    val data =
        listOf(
            "Current release" to listOf(84.0, 79.0, 76.0, 88.0, 82.0, 74.0),
            "Previous release" to listOf(75.0, 82.0, 71.0, 80.0, 76.0, 68.0),
        ).toChartData(categories = categories)

    RadarChart(data = data, title = "Platform Readiness by Release")
}
// END WIKI EXAMPLE: radar-multiple-series

// BEGIN WIKI EXAMPLE: radar-category-selection
@Composable
fun SelectReadinessCategory(data: ChartData) {
    val categorySelection = rememberChartSelection()

    RadarChart(
        data = data,
        selection = categorySelection,
    )
}
// END WIKI EXAMPLE: radar-category-selection

// BEGIN WIKI EXAMPLE: radar-series-selection
@Composable
fun FocusReadinessSeries(data: ChartData) {
    val seriesSelection = rememberChartSelection()

    RadarChart(
        data = data,
        seriesSelection = seriesSelection,
    )
}
// END WIKI EXAMPLE: radar-series-selection

// BEGIN WIKI EXAMPLE: radar-styling
@Composable
fun StyledReadinessRadar(data: ChartData) {
    RadarChart(
        data = data,
        style =
            RadarChartDefaults.style(
                grid = RadarChartDefaults.grid(steps = 4),
                axes = RadarChartDefaults.axes(labelPadding = 10.dp),
                polygon =
                    RadarChartDefaults.polygon(
                        lineColors = listOf(Color(0xFF2563EB), Color(0xFFDB2777)),
                        fillVisible = true,
                        fillAlpha = 0.16f,
                    ),
            ),
    )
}
// END WIKI EXAMPLE: radar-styling
