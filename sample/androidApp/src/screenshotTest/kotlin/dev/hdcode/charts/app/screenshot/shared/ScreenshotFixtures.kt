package dev.hdcode.charts.app.screenshot.shared

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import dev.hdcode.charts.sampleshared.data.barSampleUseCase
import dev.hdcode.charts.sampleshared.data.histogramSampleUseCase
import dev.hdcode.charts.sampleshared.data.lineSampleUseCase
import dev.hdcode.charts.sampleshared.data.multiLineSampleUseCase
import dev.hdcode.charts.sampleshared.data.pieSampleUseCase
import dev.hdcode.charts.sampleshared.data.radarSampleUseCase
import dev.hdcode.charts.sampleshared.data.stackedAreaSampleUseCase
import dev.hdcode.charts.sampleshared.data.stackedBarSampleUseCase
import dev.hdcode.charts.sampleshared.theme.AppTheme
import dev.hdcode.charts.sampleshared.theme.docsSlate

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
