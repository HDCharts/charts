package io.github.hdcharts.app.screenshot

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.android.tools.screenshot.PreviewTest
import io.github.hdcharts.app.screenshot.shared.SCREENSHOT_ANIMATE_ON_START
import io.github.hdcharts.app.screenshot.shared.SCREENSHOT_STACKED_AREA_SAMPLE_USE_CASE
import io.github.hdcharts.app.screenshot.shared.ScreenshotChartSurface
import io.github.hdcharts.app.screenshot.shared.ScreenshotPreview
import io.github.hdcharts.app.screenshot.shared.categoryIndex
import io.github.hdcharts.core.model.ChartData
import io.github.hdcharts.core.model.ChartValueFormatter
import io.github.hdcharts.core.model.rememberChartSelection
import io.github.hdcharts.core.model.staticChartSelection
import io.github.hdcharts.core.style.ChartGradient
import io.github.hdcharts.core.style.ChartGradients
import io.github.hdcharts.sampleshared.theme.LocalChartColors
import io.github.hdcharts.sampleshared.theme.seriesColors
import io.github.hdcharts.stackedarea.StackedAreaChart
import io.github.hdcharts.stackedarea.StackedAreaChartDefaults
import java.util.Locale
import kotlin.math.roundToInt

// As Pro closes in on Starter.
private const val STACKED_AREA_SELECTION_LABEL = "Sep '25"

private const val DENSE_SELECTION_LABEL = "Jan '25"

// Six years of months.
private const val DENSE_POINTS = 72
private const val GRADIENT_END_ALPHA = 0.15f

// Whole subscribers with a thousands separator, such as `4,213`.
private val SUBSCRIBERS = ChartValueFormatter { value -> String.format(Locale.US, "%,d", value.roundToInt()) }

@PreviewTest
@ScreenshotPreview
@Composable
fun StackedAreaChartDefaultPreview() {
    ScreenshotChartSurface {
        val sample = SCREENSHOT_STACKED_AREA_SAMPLE_USE_CASE.deterministic()
        StackedAreaChart(
            data = sample.data,
            title = sample.title,
            animateOnStart = SCREENSHOT_ANIMATE_ON_START,
        )
    }
}

@PreviewTest
@ScreenshotPreview
@Composable
fun StackedAreaChartSelectedPointPreview() {
    ScreenshotChartSurface {
        val sample = SCREENSHOT_STACKED_AREA_SAMPLE_USE_CASE.deterministic()
        StackedAreaChart(
            data = sample.data,
            title = STACKED_AREA_SELECTION_LABEL,
            animateOnStart = SCREENSHOT_ANIMATE_ON_START,
            interactionEnabled = false,
            selection = staticChartSelection(sample.data.categoryIndex(STACKED_AREA_SELECTION_LABEL)),
            selectedValueFormatter = SUBSCRIBERS,
        )
    }
}

/** One series needs no legend, so the title shows the selected value. */
@PreviewTest
@ScreenshotPreview
@Composable
fun StackedAreaChartSingleSeriesSelectedPointPreview() {
    ScreenshotChartSurface {
        val sample = SCREENSHOT_STACKED_AREA_SAMPLE_USE_CASE.deterministic()
        val data =
            ChartData(
                categories = sample.data.categories,
                series = sample.data.series.take(1),
            )
        StackedAreaChart(
            data = data,
            title = sample.title,
            animateOnStart = SCREENSHOT_ANIMATE_ON_START,
            interactionEnabled = false,
            selection = staticChartSelection(data.categoryIndex(STACKED_AREA_SELECTION_LABEL)),
            selectedValueFormatter = SUBSCRIBERS,
        )
    }
}

@PreviewTest
@ScreenshotPreview
@Composable
fun StackedAreaChartHiddenLegendPreview() {
    ScreenshotChartSurface {
        val sample = SCREENSHOT_STACKED_AREA_SAMPLE_USE_CASE.deterministic()
        StackedAreaChart(
            data = sample.data,
            title = sample.title,
            style = StackedAreaChartDefaults.style(legend = StackedAreaChartDefaults.legend(visible = false)),
            animateOnStart = SCREENSHOT_ANIMATE_ON_START,
        )
    }
}

@PreviewTest
@ScreenshotPreview
@Composable
fun StackedAreaChartNoCategoriesPreview() {
    ScreenshotChartSurface {
        val sample = SCREENSHOT_STACKED_AREA_SAMPLE_USE_CASE.deterministic()
        StackedAreaChart(
            data = ChartData(categories = emptyList(), series = sample.data.series),
            title = sample.title,
            animateOnStart = SCREENSHOT_ANIMATE_ON_START,
        )
    }
}

@PreviewTest
@ScreenshotPreview
@Composable
fun StackedAreaChartDensePreview() {
    ScreenshotChartSurface {
        val sample = SCREENSHOT_STACKED_AREA_SAMPLE_USE_CASE.deterministic(points = DENSE_POINTS)
        StackedAreaChart(
            data = sample.data,
            title = sample.title,
            animateOnStart = SCREENSHOT_ANIMATE_ON_START,
        )
    }
}

/**
 * In dense data, selecting a point highlights the bucket that contains it.
 * Interaction stays on so the expand toggle shows next to the selection.
 */
@PreviewTest
@ScreenshotPreview
@Composable
fun StackedAreaChartDenseSelectedPointPreview() {
    ScreenshotChartSurface {
        val sample = SCREENSHOT_STACKED_AREA_SAMPLE_USE_CASE.deterministic(points = DENSE_POINTS)
        StackedAreaChart(
            data = sample.data,
            title = DENSE_SELECTION_LABEL,
            animateOnStart = SCREENSHOT_ANIMATE_ON_START,
            selection = staticChartSelection(sample.data.categoryIndex(DENSE_SELECTION_LABEL)),
        )
    }
}

@PreviewTest
@ScreenshotPreview
@Composable
fun StackedAreaChartSeriesColorsPreview() {
    ScreenshotChartSurface {
        val sample = SCREENSHOT_STACKED_AREA_SAMPLE_USE_CASE.deterministic()
        val colors = LocalChartColors.current.seriesColors(sample.seriesKeys.size)
        StackedAreaChart(
            data = sample.data,
            title = sample.title,
            style =
                StackedAreaChartDefaults.style(
                    fill = StackedAreaChartDefaults.fill(colors = colors),
                ),
            animateOnStart = SCREENSHOT_ANIMATE_ON_START,
        )
    }
}

/** Each area layer shows the whole series-color fade. */
@PreviewTest
@ScreenshotPreview
@Composable
fun StackedAreaChartGradientShapePreview() {
    StackedAreaGradientChart(gradient = ChartGradients.fade(endAlpha = GRADIENT_END_ALPHA))
}

/** Each area layer can use a different color ramp that starts at its legend color. */
@PreviewTest
@ScreenshotPreview
@Composable
fun StackedAreaChartSeriesGradientsPreview() {
    ScreenshotChartSurface {
        val sample = SCREENSHOT_STACKED_AREA_SAMPLE_USE_CASE.deterministic()
        val seriesColors =
            listOf(Color(0xFF087E72), Color(0xFFC26919), Color(0xFF6951A1), Color(0xFF3E7456))
        StackedAreaChart(
            data = sample.data,
            title = sample.title,
            style =
                StackedAreaChartDefaults.style(
                    fill =
                        StackedAreaChartDefaults.fill(
                            colors = seriesColors,
                            seriesGradients =
                                listOf(
                                    ChartGradients.vertical(colors = listOf(seriesColors[0], Color(0xFF72C8B6))),
                                    ChartGradients.vertical(colors = listOf(seriesColors[1], Color(0xFFF0C078))),
                                    ChartGradients.vertical(colors = listOf(seriesColors[2], Color(0xFFB8A5E0))),
                                    ChartGradients.vertical(colors = listOf(seriesColors[3], Color(0xFFA0D1B0))),
                                ),
                        ),
                ),
            animateOnStart = SCREENSHOT_ANIMATE_ON_START,
        )
    }
}

/** Selection dims the other area layers over their gradients. */
@PreviewTest
@ScreenshotPreview
@Composable
fun StackedAreaChartGradientSelectedPointPreview() {
    StackedAreaGradientChart(
        gradient = ChartGradients.fade(endAlpha = GRADIENT_END_ALPHA),
        selectionLabel = STACKED_AREA_SELECTION_LABEL,
    )
}

@Composable
private fun StackedAreaGradientChart(
    gradient: ChartGradient,
    selectionLabel: String? = null,
) {
    ScreenshotChartSurface {
        val sample = SCREENSHOT_STACKED_AREA_SAMPLE_USE_CASE.deterministic()
        StackedAreaChart(
            data = sample.data,
            title = selectionLabel ?: sample.title,
            style =
                StackedAreaChartDefaults.style(
                    fill = StackedAreaChartDefaults.fill(gradient = gradient),
                ),
            animateOnStart = SCREENSHOT_ANIMATE_ON_START,
            interactionEnabled = selectionLabel == null,
            selection =
                if (selectionLabel == null) {
                    rememberChartSelection()
                } else {
                    staticChartSelection(sample.data.categoryIndex(selectionLabel))
                },
        )
    }
}
