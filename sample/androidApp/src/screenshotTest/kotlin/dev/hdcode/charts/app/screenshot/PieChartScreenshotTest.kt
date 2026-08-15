package dev.hdcode.charts.app.screenshot

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.android.tools.screenshot.PreviewTest
import dev.hdcode.charts.app.screenshot.shared.SCREENSHOT_PIE_SAMPLE_USE_CASE
import dev.hdcode.charts.app.screenshot.shared.ScreenshotPreview
import dev.hdcode.charts.app.screenshot.shared.ScreenshotSurface
import dev.hdcode.charts.sampleshared.fixtures.ChartTestStyleFixtures
import io.github.dautovicharis.charts.PieChart
import io.github.dautovicharis.charts.model.staticChartSelection
import io.github.dautovicharis.charts.style.ChartContainerDefaults
import io.github.dautovicharis.charts.style.PieChartDefaults

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
            style =
                PieChartDefaults.style(
                    selection = staticChartSelection(1),
                ),
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
            modifier = Modifier.fillMaxWidth().height(350.dp),
            style =
                PieChartDefaults.style(
                    legend = PieChartDefaults.legend(visible = false),
                ),
        )
    }
}
