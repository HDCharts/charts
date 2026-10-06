package io.github.hdcharts.app.screenshot.shared

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import io.github.hdcharts.core.model.ChartData
import io.github.hdcharts.core.model.ChartValueFormatter
import io.github.hdcharts.line.LineChartDefaults
import io.github.hdcharts.sampleshared.data.MultiLineSampleData
import io.github.hdcharts.sampleshared.data.barSampleUseCase
import io.github.hdcharts.sampleshared.data.histogramSampleUseCase
import io.github.hdcharts.sampleshared.data.lineSampleUseCase
import io.github.hdcharts.sampleshared.data.liveLatencyTimelineUseCase
import io.github.hdcharts.sampleshared.data.multiLineSampleUseCase
import io.github.hdcharts.sampleshared.data.pieSampleUseCase
import io.github.hdcharts.sampleshared.data.radarSampleUseCase
import io.github.hdcharts.sampleshared.data.ringGaugeSampleUseCase
import io.github.hdcharts.sampleshared.data.stackedAreaSampleUseCase
import io.github.hdcharts.sampleshared.data.stackedBarSampleUseCase
import io.github.hdcharts.sampleshared.theme.AppTheme
import io.github.hdcharts.sampleshared.theme.docsSlate
import kotlin.random.Random

internal val ScreenshotTheme = docsSlate
internal const val SCREENSHOT_ANIMATE_ON_START = false
internal val SCREENSHOT_PIE_SAMPLE_USE_CASE = pieSampleUseCase()
internal val SCREENSHOT_LINE_SAMPLE_USE_CASE = lineSampleUseCase()
internal val SCREENSHOT_MULTI_LINE_SAMPLE_USE_CASE = multiLineSampleUseCase()
internal val SCREENSHOT_BAR_SAMPLE_USE_CASE = barSampleUseCase()
internal val SCREENSHOT_HISTOGRAM_SAMPLE_USE_CASE = histogramSampleUseCase()
internal val SCREENSHOT_STACKED_BAR_SAMPLE_USE_CASE = stackedBarSampleUseCase()
internal val SCREENSHOT_STACKED_AREA_SAMPLE_USE_CASE = stackedAreaSampleUseCase()
internal val SCREENSHOT_RADAR_SAMPLE_USE_CASE = radarSampleUseCase()
internal val SCREENSHOT_RING_GAUGE_SAMPLE_USE_CASE = ringGaugeSampleUseCase()

private const val LIVE_LINE_WINDOW_SIZE = 60
private const val LIVE_LINE_SEED = 7

/** A fixed 60-second P50/P95 latency window for live line screenshots. */
internal fun screenshotLiveLatencySample(): MultiLineSampleData {
    val useCase = liveLatencyTimelineUseCase(Random(LIVE_LINE_SEED))
    return MultiLineSampleData(
        dataSet = useCase.toMultiDataSet(useCase.createMultiWindow(windowSize = LIVE_LINE_WINDOW_SIZE)),
        seriesKeys = useCase.multiSeriesKeys,
        title = useCase.multiSeriesTitle,
    )
}

/**
 * Finds a selection by its category label, so a change to the sample data fails here instead of
 * quietly selecting a different point.
 */
internal fun ChartData.categoryIndex(label: String): Int {
    val index = categories.indexOf(label)
    check(index >= 0) { "No category \"$label\" in the sample data" }
    return index
}

/** Formats values held in thousands of dollars, such as `$552K`. */
internal val SCREENSHOT_THOUSANDS_OF_DOLLARS =
    ChartValueFormatter { value -> "$" + LineChartDefaults.axisValueFormatter.format(value) + "K" }

@Composable
internal fun ScreenshotSurface(content: @Composable () -> Unit) {
    val darkTheme = isSystemInDarkTheme()
    AppTheme(theme = ScreenshotTheme, darkTheme = darkTheme, useDynamicColors = false) {
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center,
        ) {
            content()
        }
    }
}

/**
 * Places a chart the way an app screen would: inset from the edges and no taller than it
 * is wide, so charts on a phone do not stretch to the full screen height.
 */
@Composable
internal fun ScreenshotChartSurface(content: @Composable () -> Unit) {
    ScreenshotSurface {
        BoxWithConstraints(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(16.dp),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(min(maxWidth, maxHeight)),
                contentAlignment = Alignment.Center,
            ) {
                content()
            }
        }
    }
}
