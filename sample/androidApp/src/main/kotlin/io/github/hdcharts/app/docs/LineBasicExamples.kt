package io.github.hdcharts.app.docs

// BEGIN WIKI EXAMPLE: line-basic-usage
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.hdcharts.core.model.toChartData
import io.github.hdcharts.line.LineChart

data class DailyMeasurement(
    val day: String,
    val temperatureCelsius: Double,
)

@Composable
fun ShowDailyTemperatures(modifier: Modifier = Modifier) {
    val measurements =
        listOf(
            DailyMeasurement("Mon", 18.0),
            DailyMeasurement("Tue", 21.0),
            DailyMeasurement("Wed", 19.0),
            DailyMeasurement("Thu", 24.0),
            DailyMeasurement("Fri", 22.0),
        )
    val data =
        measurements.toChartData(
            value = { it.temperatureCelsius },
            category = { it.day },
            seriesName = "Temperature",
        )

    LineChart(
        data = data,
        modifier = modifier,
        title = "Daily High Temperature (°C)",
    )
}
// END WIKI EXAMPLE: line-basic-usage

// BEGIN WIKI EXAMPLE: line-multiple-series
@Composable
fun ShowWeeklyRevenueByChannel() {
    val weeks = listOf("W1", "W2", "W3", "W4", "W5")
    val data =
        listOf(
            "Web Store" to listOf(180.0, 310.0, 420.0, 560.0, 640.0),
            "Mobile App" to listOf(120.0, 240.0, 350.0, 430.0, 520.0),
            "Partner Sales" to listOf(60.0, 130.0, 190.0, 240.0, 300.0),
        ).toChartData(categories = weeks)

    LineChart(
        data = data,
        modifier = Modifier.fillMaxWidth().height(300.dp),
        title = "Weekly Revenue by Channel",
    )
}
// END WIKI EXAMPLE: line-multiple-series
