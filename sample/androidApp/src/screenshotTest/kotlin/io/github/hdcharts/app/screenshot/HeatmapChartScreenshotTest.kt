package io.github.hdcharts.app.screenshot

import androidx.compose.runtime.Composable
import com.android.tools.screenshot.PreviewTest
import io.github.hdcharts.app.screenshot.shared.SCREENSHOT_HEATMAP_SAMPLE_USE_CASE
import io.github.hdcharts.app.screenshot.shared.ScreenshotChartSurface
import io.github.hdcharts.app.screenshot.shared.ScreenshotPreview
import io.github.hdcharts.core.model.staticChartSelection
import io.github.hdcharts.heatmap.HeatmapChart
import io.github.hdcharts.heatmap.HeatmapChartDefaults
import io.github.hdcharts.heatmap.HeatmapRenderMode
import io.github.hdcharts.sampleshared.data.HeatmapSampleColorScales
import kotlin.math.roundToInt

// Monday at 10:00, the busiest weekday hour.
private const val SELECTED_ROW = 0
private const val SELECTED_HOUR = 10

// New York at 19:00 UTC, the highest regional CPU load.
private const val PEAK_LOAD_ROW = 6
private const val PEAK_LOAD_HOUR = 19

/** Regional CPU load on the green-to-red scale, with the peak selected. Cells and Interpolated share it. */
@Composable
private fun RegionalCpuLoad(renderMode: HeatmapRenderMode) {
    val sample = SCREENSHOT_HEATMAP_SAMPLE_USE_CASE.regionalCpuLoad()
    HeatmapChart(
        data = sample.data,
        title = sample.title,
        style =
            HeatmapChartDefaults.style(
                renderMode = renderMode,
                colorScale = HeatmapSampleColorScales.cpuLoad,
            ),
        selection = staticChartSelection(sample.data.cellIndex(PEAK_LOAD_ROW, PEAK_LOAD_HOUR)),
        interactionEnabled = false,
        valueFormatter = { value -> "${value.roundToInt()}%" },
    )
}

@PreviewTest
@ScreenshotPreview
@Composable
fun HeatmapChartCellsPreview() {
    ScreenshotChartSurface {
        RegionalCpuLoad(HeatmapRenderMode.Cells)
    }
}

@PreviewTest
@ScreenshotPreview
@Composable
fun HeatmapChartInterpolatedPreview() {
    ScreenshotChartSurface {
        RegionalCpuLoad(HeatmapRenderMode.Interpolated)
    }
}

/** The theme-derived default scale, with a selected cell. */
@PreviewTest
@ScreenshotPreview
@Composable
fun HeatmapChartSelectedPreview() {
    ScreenshotChartSurface {
        val sample = SCREENSHOT_HEATMAP_SAMPLE_USE_CASE.weeklyActivity()
        HeatmapChart(
            data = sample.data,
            title = sample.title,
            selection = staticChartSelection(sample.data.cellIndex(SELECTED_ROW, SELECTED_HOUR)),
            interactionEnabled = false,
        )
    }
}

@PreviewTest
@ScreenshotPreview
@Composable
fun HeatmapChartDivergingPreview() {
    ScreenshotChartSurface {
        val sample = SCREENSHOT_HEATMAP_SAMPLE_USE_CASE.temperatureAnomalies()
        HeatmapChart(
            data = sample.data,
            title = sample.title,
            style = HeatmapChartDefaults.style(colorScale = HeatmapSampleColorScales.temperatureAnomaly),
        )
    }
}
