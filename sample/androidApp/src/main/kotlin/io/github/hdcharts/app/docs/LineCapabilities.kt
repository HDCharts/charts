package io.github.hdcharts.app.docs

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import io.github.hdcharts.core.model.ChartData
import io.github.hdcharts.core.model.rememberChartSelection
import io.github.hdcharts.core.model.toChartData
import io.github.hdcharts.core.style.ChartGradients
import io.github.hdcharts.core.style.GradientSpan
import io.github.hdcharts.line.LineChart
import io.github.hdcharts.line.LineChartDefaults
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

// BEGIN WIKI EXAMPLE: line-fixed-range
@Composable
fun CompareTemperatures(data: ChartData) {
    LineChart(
        data = data,
        style = LineChartDefaults.style(range = LineChartDefaults.range(min = 0.0, max = 40.0)),
    )
}
// END WIKI EXAMPLE: line-fixed-range

// BEGIN WIKI EXAMPLE: line-selection
@Composable
fun SelectTemperature(data: ChartData) {
    val selection = rememberChartSelection()

    LineChart(data = data, selection = selection)
    Text("Selected point index: ${selection.selectedIndex ?: "none"}")
}
// END WIKI EXAMPLE: line-selection

// BEGIN WIKI EXAMPLE: line-gradient
@Composable
fun GradientTemperature(data: ChartData) {
    val gradient =
        ChartGradients.vertical(
            colors = listOf(Color(0xFF2563EB), Color(0xFFF59E0B), Color(0xFFDC2626)),
            span = GradientSpan.Plot,
        )

    LineChart(
        data = data,
        style = LineChartDefaults.style(line = LineChartDefaults.line(gradient = gradient)),
    )
}
// END WIKI EXAMPLE: line-gradient

// BEGIN WIKI EXAMPLE: line-morphing
@Composable
fun MorphWeeklyMeasurements() {
    val weeks =
        listOf(
            listOf(42.0, 38.0, 45.0, 51.0, 47.0),
            listOf(46.0, 52.0, 48.0, 44.0, 50.0),
        )
    var weekIndex by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(2_000.milliseconds)
            weekIndex = (weekIndex + 1) % weeks.size
        }
    }

    val data =
        weeks[weekIndex].toChartData(
            categories = listOf("Mon", "Tue", "Wed", "Thu", "Fri"),
            seriesName = "Daily Measurements",
        )

    LineChart(
        data = data,
        title = "Daily Measurements",
        animateOnStart = false,
    )
}
// END WIKI EXAMPLE: line-morphing
