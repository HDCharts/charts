package io.github.hdcharts.app.docs

// BEGIN WIKI EXAMPLE: live-line-basic
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.hdcharts.core.model.toChartData
import io.github.hdcharts.line.LiveLineChart
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun ShowLiveSensorReadings() {
    val readings = listOf(18.0, 18.5, 19.0, 18.7, 19.4, 20.0, 19.6, 19.1)
    val windowSize = 12
    var firstSample by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(500.milliseconds)
            firstSample++
        }
    }

    val sampleNumbers = firstSample until (firstSample + windowSize)
    val values = sampleNumbers.map { sample -> readings[sample % readings.size] }
    val categories = sampleNumbers.map { "#${it + 1}" }
    val data = values.toChartData(categories = categories, seriesName = "Sensor")

    LiveLineChart(
        data = data,
        modifier = Modifier.fillMaxWidth().height(300.dp),
        title = "Live Sensor Reading",
        shiftDuration = 500.milliseconds,
        animateOnStart = false,
    )
}
// END WIKI EXAMPLE: live-line-basic
