package io.github.hdcharts.app.screenshot

import androidx.compose.runtime.Composable
import com.android.tools.screenshot.PreviewTest
import io.github.hdcharts.app.screenshot.shared.SCREENSHOT_ANIMATE_ON_START
import io.github.hdcharts.app.screenshot.shared.ScreenshotChartSurface
import io.github.hdcharts.app.screenshot.shared.ScreenshotPreview
import io.github.hdcharts.app.screenshot.shared.screenshotLiveLatencySample
import io.github.hdcharts.charts.LiveLineChart
import io.github.hdcharts.charts.model.ChartValueFormatter
import io.github.hdcharts.charts.style.LineChartDefaults

private val MILLISECONDS =
    ChartValueFormatter { value -> LineChartDefaults.axisValueFormatter.format(value) + " ms" }

@PreviewTest
@ScreenshotPreview
@Composable
fun LiveLineChartDefaultPreview() {
    ScreenshotChartSurface {
        val sample = screenshotLiveLatencySample()
        LiveLineChart(
            data = sample.dataSet,
            title = sample.title,
            axisValueFormatter = MILLISECONDS,
            animateOnStart = SCREENSHOT_ANIMATE_ON_START,
        )
    }
}
