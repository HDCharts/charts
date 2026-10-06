package io.github.hdcharts.app.screenshot

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.android.tools.screenshot.PreviewTest
import io.github.hdcharts.app.screenshot.shared.SCREENSHOT_ANIMATE_ON_START
import io.github.hdcharts.app.screenshot.shared.SCREENSHOT_BAR_SAMPLE_USE_CASE
import io.github.hdcharts.app.screenshot.shared.ScreenshotChartSurface
import io.github.hdcharts.app.screenshot.shared.ScreenshotPreview
import io.github.hdcharts.app.screenshot.shared.categoryIndex
import io.github.hdcharts.bar.BarChart
import io.github.hdcharts.core.model.rememberChartSelection
import io.github.hdcharts.core.model.staticChartSelection
import io.github.hdcharts.core.style.BarChartDefaults
import io.github.hdcharts.core.style.ChartGradient
import io.github.hdcharts.core.style.ChartGradients
import io.github.hdcharts.core.style.GradientSpan
import io.github.hdcharts.sampleshared.theme.LocalChartColors
import io.github.hdcharts.sampleshared.theme.seriesColor

// A negative day.
private const val BAR_SELECTION_LABEL = "Mar 6"

// In the middle of the 90 days.
private const val DENSE_SELECTION_LABEL = "Apr 15"
private const val DENSE_POINTS = 90

// Wider than the data (-43 to 124) on both sides, with zero as a tick.
private const val FIXED_RANGE_MIN = -60.0
private const val FIXED_RANGE_MAX = 150.0
private const val POSITIVE_COLOR_INDEX = 6
private const val NEGATIVE_COLOR_INDEX = 3
private const val GRADIENT_START_COLOR_INDEX = 0
private const val GRADIENT_END_COLOR_INDEX = 4
private const val FADE_END_ALPHA = 0.15f

@PreviewTest
@ScreenshotPreview
@Composable
fun BarChartDefaultPreview() {
    ScreenshotChartSurface {
        val data = SCREENSHOT_BAR_SAMPLE_USE_CASE.deterministic(signed = true)
        BarChart(
            data = data,
            title = data.series.single().name,
            animateOnStart = SCREENSHOT_ANIMATE_ON_START,
        )
    }
}

@PreviewTest
@ScreenshotPreview
@Composable
fun BarChartSelectedBarPreview() {
    ScreenshotChartSurface {
        val data = SCREENSHOT_BAR_SAMPLE_USE_CASE.deterministic(signed = true)
        BarChart(
            data = data,
            title = data.series.single().name,
            animateOnStart = SCREENSHOT_ANIMATE_ON_START,
            interactionEnabled = false,
            selection = staticChartSelection(data.categoryIndex(BAR_SELECTION_LABEL)),
        )
    }
}

@PreviewTest
@ScreenshotPreview
@Composable
fun BarChartDensePreview() {
    ScreenshotChartSurface {
        val data = SCREENSHOT_BAR_SAMPLE_USE_CASE.deterministic(points = DENSE_POINTS)
        BarChart(
            data = data,
            title = data.series.single().name,
            animateOnStart = SCREENSHOT_ANIMATE_ON_START,
        )
    }
}

/**
 * In dense data, a bar shows its bucket's average, and the selection shows the source bar's value.
 * Interaction stays on so the expand toggle shows next to the selection.
 */
@PreviewTest
@ScreenshotPreview
@Composable
fun BarChartDenseSelectedBarPreview() {
    ScreenshotChartSurface {
        val data = SCREENSHOT_BAR_SAMPLE_USE_CASE.deterministic(points = DENSE_POINTS)
        BarChart(
            data = data,
            title = data.series.single().name,
            animateOnStart = SCREENSHOT_ANIMATE_ON_START,
            selection = staticChartSelection(data.categoryIndex(DENSE_SELECTION_LABEL)),
        )
    }
}

@PreviewTest
@ScreenshotPreview
@Composable
fun BarChartFixedRangePreview() {
    ScreenshotChartSurface {
        val data = SCREENSHOT_BAR_SAMPLE_USE_CASE.deterministic(signed = true)
        BarChart(
            data = data,
            title = data.series.single().name,
            style =
                BarChartDefaults.style(
                    range = BarChartDefaults.range(min = FIXED_RANGE_MIN, max = FIXED_RANGE_MAX),
                ),
            animateOnStart = SCREENSHOT_ANIMATE_ON_START,
        )
    }
}

@PreviewTest
@ScreenshotPreview
@Composable
fun BarChartHiddenGridPreview() {
    ScreenshotChartSurface {
        val data = SCREENSHOT_BAR_SAMPLE_USE_CASE.deterministic(signed = true)
        BarChart(
            data = data,
            title = data.series.single().name,
            style = BarChartDefaults.style(grid = BarChartDefaults.grid(visible = false)),
            animateOnStart = SCREENSHOT_ANIMATE_ON_START,
        )
    }
}

/** One color per bar: positive days in one color, negative days in another. */
@PreviewTest
@ScreenshotPreview
@Composable
fun BarChartBarColorsPreview() {
    ScreenshotChartSurface {
        val data = SCREENSHOT_BAR_SAMPLE_USE_CASE.deterministic(signed = true)
        val chartColors = LocalChartColors.current
        val positive = chartColors.seriesColor(POSITIVE_COLOR_INDEX)
        val negative = chartColors.seriesColor(NEGATIVE_COLOR_INDEX)
        val barColors =
            data.series
                .single()
                .values
                .map { value -> if (value < 0) negative else positive }
        BarChart(
            data = data,
            title = data.series.single().name,
            style = BarChartDefaults.style(bars = BarChartDefaults.bars(colors = barColors)),
            animateOnStart = SCREENSHOT_ANIMATE_ON_START,
        )
    }
}

/** The same gradient on every bar; bars below zero mirror it, so each runs from its end to the baseline. */
@PreviewTest
@ScreenshotPreview
@Composable
fun BarChartGradientShapePreview() {
    GradientBarChart(gradient = { colors -> ChartGradients.vertical(colors = colors, span = GradientSpan.Shape) })
}

/** One gradient across the plot: the first bar takes the start color and the last bar the end color. */
@PreviewTest
@ScreenshotPreview
@Composable
fun BarChartGradientPlotHorizontalPreview() {
    GradientBarChart(gradient = { colors -> ChartGradients.horizontal(colors = colors, span = GradientSpan.Plot) })
}

/** One gradient down the plot: taller bars reach further into the start color. */
@PreviewTest
@ScreenshotPreview
@Composable
fun BarChartGradientPlotVerticalPreview() {
    GradientBarChart(gradient = { colors -> ChartGradients.vertical(colors = colors, span = GradientSpan.Plot) })
}

/** Each bar fades from its own color toward the baseline. */
@PreviewTest
@ScreenshotPreview
@Composable
fun BarChartGradientFadePreview() {
    GradientBarChart(gradient = { ChartGradients.fade(endAlpha = FADE_END_ALPHA) })
}

/** Selection dims the other bars over their gradient. */
@PreviewTest
@ScreenshotPreview
@Composable
fun BarChartGradientSelectedBarPreview() {
    GradientBarChart(
        gradient = { colors -> ChartGradients.vertical(colors = colors, span = GradientSpan.Shape) },
        selectionLabel = BAR_SELECTION_LABEL,
    )
}

/** Builds [gradient] from two theme colors inside the surface, where the chart colors are provided. */
@Composable
private fun GradientBarChart(
    gradient: (colors: List<Color>) -> ChartGradient,
    selectionLabel: String? = null,
) {
    ScreenshotChartSurface {
        val data = SCREENSHOT_BAR_SAMPLE_USE_CASE.deterministic(signed = true)
        val chartColors = LocalChartColors.current
        val colors =
            listOf(
                chartColors.seriesColor(index = GRADIENT_START_COLOR_INDEX),
                chartColors.seriesColor(index = GRADIENT_END_COLOR_INDEX),
            )
        BarChart(
            data = data,
            title = data.series.single().name,
            style = BarChartDefaults.style(bars = BarChartDefaults.bars(gradient = gradient(colors))),
            animateOnStart = SCREENSHOT_ANIMATE_ON_START,
            interactionEnabled = selectionLabel == null,
            selection =
                if (selectionLabel == null) {
                    rememberChartSelection()
                } else {
                    staticChartSelection(index = data.categoryIndex(label = selectionLabel))
                },
        )
    }
}
