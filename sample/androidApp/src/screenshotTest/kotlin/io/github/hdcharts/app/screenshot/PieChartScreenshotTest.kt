package io.github.hdcharts.app.screenshot

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.android.tools.screenshot.PreviewTest
import io.github.hdcharts.app.screenshot.shared.SCREENSHOT_PIE_SAMPLE_USE_CASE
import io.github.hdcharts.app.screenshot.shared.ScreenshotPreview
import io.github.hdcharts.app.screenshot.shared.ScreenshotSurface
import io.github.hdcharts.charts.PieChart
import io.github.hdcharts.charts.model.staticChartSelection
import io.github.hdcharts.charts.style.ChartContainerDefaults
import io.github.hdcharts.charts.style.PieChartDefaults
import io.github.hdcharts.sampleshared.fixtures.ChartTestStyleFixtures

@PreviewTest
@ScreenshotPreview
@Composable
fun PieChartDefaultPreview() {
    val sample = SCREENSHOT_PIE_SAMPLE_USE_CASE.initialPieSample()
    ScreenshotSurface {
        PieChart(
            data = sample.slices,
            title = sample.title,
        )
    }
}

@PreviewTest
@ScreenshotPreview
@Composable
fun PieChartCustomPreview() {
    val sample = SCREENSHOT_PIE_SAMPLE_USE_CASE.initialPieCustomSample()
    ScreenshotSurface {
        val slices = ChartTestStyleFixtures.pieCustomSlices(sample.slices)
        PieChart(
            data = slices,
            title = sample.title,
            style =
                ChartTestStyleFixtures.pieCustomStyle(
                    chartContainerStyle = ChartContainerDefaults.style(),
                ),
        )
    }
}

@PreviewTest
@ScreenshotPreview
@Composable
fun PieChartSelectedSlicePreview() {
    val sample = SCREENSHOT_PIE_SAMPLE_USE_CASE.initialPieSample()
    ScreenshotSurface {
        PieChart(
            data = sample.slices,
            selection = staticChartSelection(1),
        )
    }
}

@PreviewTest
@ScreenshotPreview
@Composable
fun PieChartRectangularPlotAreaPreview() {
    val sample = SCREENSHOT_PIE_SAMPLE_USE_CASE.initialPieSample()
    ScreenshotSurface {
        PieChart(
            data = sample.slices,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(350.dp)
                    .wrapContentSize(Alignment.Center),
            style =
                PieChartDefaults.style(
                    legend = PieChartDefaults.legend(visible = false),
                ),
        )
    }
}
