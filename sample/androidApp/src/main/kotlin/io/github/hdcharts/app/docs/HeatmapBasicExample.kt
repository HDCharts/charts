package io.github.hdcharts.app.docs

// BEGIN WIKI EXAMPLE: heatmap-basic-usage
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.hdcharts.heatmap.HeatmapChart
import io.github.hdcharts.heatmap.HeatmapData

val weeklySessions =
    HeatmapData(
        columns = listOf("00-04", "04-08", "08-12", "12-16", "16-20", "20-24"),
        rows = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"),
        values =
            listOf(
                listOf(3.0, 19.0, 76.0, 22.0, 32.0, 32.0),
                listOf(4.0, 21.0, 71.0, 24.0, 35.0, 38.0),
                listOf(4.0, 20.0, 65.0, 28.0, 38.0, 45.0),
                listOf(5.0, 18.0, 58.0, 32.0, 41.0, 53.0),
                listOf(5.0, 15.0, 49.0, 36.0, 44.0, 60.0),
                listOf(6.0, 12.0, 40.0, 38.0, 47.0, 68.0),
                // No data was collected during Sunday night maintenance, so that cell is missing.
                listOf(null, 10.0, 30.0, 37.0, 50.0, 76.0),
            ),
    )

@Composable
fun ShowWeeklySessions(modifier: Modifier = Modifier) {
    HeatmapChart(data = weeklySessions, modifier = modifier, title = "Weekly sessions")
}
// END WIKI EXAMPLE: heatmap-basic-usage
