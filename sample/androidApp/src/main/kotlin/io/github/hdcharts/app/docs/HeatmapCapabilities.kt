package io.github.hdcharts.app.docs

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.github.hdcharts.core.model.rememberChartSelection
import io.github.hdcharts.heatmap.HeatmapChart
import io.github.hdcharts.heatmap.HeatmapChartDefaults
import io.github.hdcharts.heatmap.HeatmapColorScale
import io.github.hdcharts.heatmap.HeatmapData
import io.github.hdcharts.heatmap.HeatmapRenderMode
import kotlin.math.roundToInt

// BEGIN WIKI EXAMPLE: heatmap-data-mapping
data class SessionCount(
    val day: String,
    val block: String,
    val sessions: Int,
)

fun sessionHeatmap(
    counts: List<SessionCount>,
    days: List<String>,
    blocks: List<String>,
): HeatmapData {
    val sessionsByCell = counts.associate { (it.day to it.block) to it.sessions.toDouble() }
    return HeatmapData(
        columns = blocks,
        rows = days,
        // A day and block with no record stays null, so it draws as a missing cell.
        values = days.map { day -> blocks.map { block -> sessionsByCell[day to block] } },
    )
}
// END WIKI EXAMPLE: heatmap-data-mapping

// BEGIN WIKI EXAMPLE: heatmap-sequential-scale
// Average CPU load in percent per region, in two-hour UTC blocks.
val regionalCpuLoad =
    HeatmapData(
        columns = listOf("00", "02", "04", "06", "08", "10", "12", "14", "16", "18", "20", "22"),
        rows = listOf("Tokyo", "Mumbai", "Frankfurt", "London", "New York", "San Francisco"),
        values =
            listOf(
                listOf(36.0, 61.0, 78.0, 72.0, 52.0, 39.0, 31.0, 18.0, 11.0, 10.0, 12.0, 19.0),
                listOf(13.0, 22.0, 41.0, 64.0, 76.0, 65.0, 45.0, 36.0, 27.0, 15.0, 11.0, 11.0),
                listOf(11.0, 10.0, 12.0, 20.0, 38.0, 64.0, 82.0, 75.0, 54.0, 40.0, 33.0, 18.0),
                listOf(13.0, 10.0, 11.0, 15.0, 26.0, 49.0, 72.0, 78.0, 62.0, 43.0, 36.0, 25.0),
                listOf(43.0, 35.0, 19.0, 11.0, 10.0, 13.0, 21.0, 40.0, 69.0, 89.0, 82.0, 58.0),
                listOf(65.0, 45.0, 37.0, 26.0, 14.0, 10.0, 11.0, 15.0, 27.0, 51.0, 75.0, 82.0),
            ),
    )

// Fixed 0..100 bounds: green, yellow, orange and red sit at 0, 33, 67 and 100 percent.
val cpuLoadScale =
    HeatmapColorScale(
        colors = listOf(Color(0xFF43A047), Color(0xFFFDD835), Color(0xFFFB8C00), Color(0xFFD32F2F)),
        min = 0.0,
        max = 100.0,
    )

@Composable
fun ShowRegionalCpuLoad(modifier: Modifier = Modifier) {
    HeatmapChart(
        data = regionalCpuLoad,
        modifier = modifier,
        title = "CPU load by region (UTC)",
        style = HeatmapChartDefaults.style(colorScale = cpuLoadScale),
        valueFormatter = { load -> "${load.roundToInt()}%" },
    )
}
// END WIKI EXAMPLE: heatmap-sequential-scale

// BEGIN WIKI EXAMPLE: heatmap-interpolated
@Composable
fun ShowInterpolatedCpuLoad(
    data: HeatmapData,
    modifier: Modifier = Modifier,
) {
    HeatmapChart(
        data = data,
        modifier = modifier,
        title = "CPU load by region (UTC)",
        style =
            HeatmapChartDefaults.style(
                renderMode = HeatmapRenderMode.Interpolated,
                colorScale = cpuLoadScale,
            ),
        valueFormatter = { load -> "${load.roundToInt()}%" },
    )
}
// END WIKI EXAMPLE: heatmap-interpolated

// BEGIN WIKI EXAMPLE: heatmap-diverging-scale
@Composable
fun ShowTemperatureAnomalies(modifier: Modifier = Modifier) {
    val anomalies =
        HeatmapData(
            columns = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun"),
            rows = listOf("Oslo", "Berlin", "Madrid"),
            values =
                listOf(
                    listOf(-2.4, -1.1, 0.6, 1.3, 0.2, -0.4),
                    listOf(-1.2, -0.4, 0.8, 1.6, 1.1, 2.0),
                    listOf(0.3, 0.9, 1.4, 2.2, 2.9, 3.4),
                ),
        )
    HeatmapChart(
        data = anomalies,
        modifier = modifier,
        title = "Temperature anomaly (°C)",
        style =
            HeatmapChartDefaults.style(
                // Symmetric bounds keep zero on the middle color.
                colorScale =
                    HeatmapChartDefaults.colorScale(
                        colors = listOf(Color(0xFF2166AC), Color(0xFFF7F7F7), Color(0xFFB2182B)),
                        min = -4.0,
                        max = 4.0,
                    ),
                cellSpacing = 4.dp,
            ),
    )
}
// END WIKI EXAMPLE: heatmap-diverging-scale

// BEGIN WIKI EXAMPLE: heatmap-selection
@Composable
fun SelectSessionBlock(
    data: HeatmapData,
    modifier: Modifier = Modifier,
) {
    // Friday 08-12 is row 4, column 2.
    val selection = rememberChartSelection(initialIndex = data.cellIndex(row = 4, column = 2))

    HeatmapChart(
        data = data,
        modifier = modifier,
        title = "Weekly sessions",
        selection = selection,
    )
}
// END WIKI EXAMPLE: heatmap-selection
