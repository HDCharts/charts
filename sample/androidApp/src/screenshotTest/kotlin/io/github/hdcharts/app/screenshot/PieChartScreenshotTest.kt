package io.github.hdcharts.app.screenshot

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.android.tools.screenshot.PreviewTest
import io.github.hdcharts.app.screenshot.shared.SCREENSHOT_ANIMATE_ON_START
import io.github.hdcharts.app.screenshot.shared.SCREENSHOT_PIE_SAMPLE_USE_CASE
import io.github.hdcharts.app.screenshot.shared.ScreenshotChartSurface
import io.github.hdcharts.app.screenshot.shared.ScreenshotPreview
import io.github.hdcharts.core.model.staticChartSelection
import io.github.hdcharts.pie.PieChart
import io.github.hdcharts.pie.PieChartDefaults
import io.github.hdcharts.sampleshared.theme.LocalChartColors
import io.github.hdcharts.sampleshared.theme.seriesColor
import io.github.hdcharts.sampleshared.theme.seriesColors

private const val PIE_SELECTION_INDEX = 1
private const val DONUT_HOLE_PERCENTAGE = 55f
private const val BASE_COLOR_INDEX = 1

@PreviewTest
@ScreenshotPreview
@Composable
fun PieChartDefaultPreview() {
    val sample = SCREENSHOT_PIE_SAMPLE_USE_CASE.initialPieSample()
    ScreenshotChartSurface {
        PieChart(
            data = sample.data,
            title = sample.title,
            animateOnStart = SCREENSHOT_ANIMATE_ON_START,
        )
    }
}

@PreviewTest
@ScreenshotPreview
@Composable
fun PieChartSelectedSlicePreview() {
    val sample = SCREENSHOT_PIE_SAMPLE_USE_CASE.initialPieSample()
    ScreenshotChartSurface {
        PieChart(
            data = sample.data,
            title = sample.title,
            animateOnStart = SCREENSHOT_ANIMATE_ON_START,
            interactionEnabled = false,
            selection = staticChartSelection(PIE_SELECTION_INDEX),
        )
    }
}

@PreviewTest
@ScreenshotPreview
@Composable
fun PieChartManySlicesPreview() {
    val sample = SCREENSHOT_PIE_SAMPLE_USE_CASE.initialManySlicesPieSample()
    ScreenshotChartSurface {
        PieChart(
            data = sample.data,
            title = sample.title,
            animateOnStart = SCREENSHOT_ANIMATE_ON_START,
        )
    }
}

@PreviewTest
@ScreenshotPreview
@Composable
fun PieChartDonutPreview() {
    val sample = SCREENSHOT_PIE_SAMPLE_USE_CASE.initialPieSample()
    ScreenshotChartSurface {
        PieChart(
            data = sample.data,
            title = sample.title,
            style = PieChartDefaults.style(donut = PieChartDefaults.donut(holePercentage = DONUT_HOLE_PERCENTAGE)),
            animateOnStart = SCREENSHOT_ANIMATE_ON_START,
        )
    }
}

/** A full-width chart with no legend keeps the pie centered, not pinned to the start edge. */
@PreviewTest
@ScreenshotPreview
@Composable
fun PieChartHiddenLegendPreview() {
    val sample = SCREENSHOT_PIE_SAMPLE_USE_CASE.initialPieSample()
    ScreenshotChartSurface {
        PieChart(
            data = sample.data,
            modifier = Modifier.fillMaxWidth(),
            title = sample.title,
            style = PieChartDefaults.style(legend = PieChartDefaults.legend(visible = false)),
            animateOnStart = SCREENSHOT_ANIMATE_ON_START,
        )
    }
}

/** With no palette set, slices get shades generated from the style's base color. */
@PreviewTest
@ScreenshotPreview
@Composable
fun PieChartBaseColorPreview() {
    val sample = SCREENSHOT_PIE_SAMPLE_USE_CASE.initialPieSample()
    ScreenshotChartSurface {
        val baseColor = LocalChartColors.current.seriesColor(BASE_COLOR_INDEX)
        PieChart(
            data = sample.data,
            title = sample.title,
            style = PieChartDefaults.style(slices = PieChartDefaults.slices(baseColor = baseColor)),
            animateOnStart = SCREENSHOT_ANIMATE_ON_START,
        )
    }
}

/** A palette set on the style is drawn in slice order, one color per slice. */
@PreviewTest
@ScreenshotPreview
@Composable
fun PieChartSliceColorsPreview() {
    val sample = SCREENSHOT_PIE_SAMPLE_USE_CASE.initialPieSample()
    ScreenshotChartSurface {
        val colors = LocalChartColors.current.seriesColors(sample.data.categories.size)
        PieChart(
            data = sample.data,
            title = sample.title,
            style = PieChartDefaults.style(slices = PieChartDefaults.slices(colors = colors)),
            animateOnStart = SCREENSHOT_ANIMATE_ON_START,
        )
    }
}
