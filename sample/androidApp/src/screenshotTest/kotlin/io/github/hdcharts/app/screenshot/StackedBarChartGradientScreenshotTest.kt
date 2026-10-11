package io.github.hdcharts.app.screenshot

import androidx.compose.foundation.layout.RowScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.android.tools.screenshot.PreviewTest
import io.github.hdcharts.app.screenshot.shared.GradientGalleryPreview
import io.github.hdcharts.app.screenshot.shared.SCREENSHOT_ANIMATE_ON_START
import io.github.hdcharts.app.screenshot.shared.SCREENSHOT_STACKED_BAR_SAMPLE_USE_CASE
import io.github.hdcharts.core.model.ChartData
import io.github.hdcharts.core.style.ChartGradients
import io.github.hdcharts.core.style.GradientPoint
import io.github.hdcharts.core.style.GradientSpan
import io.github.hdcharts.sampleshared.theme.LocalChartColors
import io.github.hdcharts.sampleshared.theme.seriesColors
import io.github.hdcharts.stackedbar.StackedBarChart
import io.github.hdcharts.stackedbar.StackedBarChartDefaults
import io.github.hdcharts.stackedbar.StackedBarChartStyle

/** A 2x2 landscape grid of fades that preserve stacked-bar series colors. */
@PreviewTest
@GradientGalleryPreview
@Composable
fun StackedBarChartGradientPreview() {
    val sample = SCREENSHOT_STACKED_BAR_SAMPLE_USE_CASE.deterministic()
    GradientGallerySurface(title = "Stacked bar gradients") {
        val colors = LocalChartColors.current.seriesColors(sample.segmentKeys.size)
        val segmentFade = ChartGradients.fade(endAlpha = GRADIENT_GALLERY_END_ALPHA, span = GradientSpan.Shape)
        val plotFade = ChartGradients.fade(endAlpha = GRADIENT_GALLERY_END_ALPHA, span = GradientSpan.Plot)
        val horizontalPlotFade =
            plotFade.copy(
                start = GradientPoint(x = 0f, y = 0.5f),
                end = GradientPoint(x = 1f, y = 0.5f),
            )
        val horizontalSegmentFade = horizontalPlotFade.copy(span = GradientSpan.Shape)
        GradientGalleryRow {
            StackedBarGalleryExample(
                title = "Vertical fade in each segment",
                description = "Each segment keeps its color and fades from top to bottom.",
                data = sample.dataSet,
                style =
                    StackedBarChartDefaults.style(
                        segments =
                            StackedBarChartDefaults.segments(
                                colors = colors,
                                gradient = segmentFade,
                            ),
                    ),
            )
            StackedBarGalleryExample(
                title = "Horizontal fade across the chart",
                description = "All series fade together from left to right, keeping their own colors.",
                data = sample.dataSet,
                style =
                    StackedBarChartDefaults.style(
                        segments =
                            StackedBarChartDefaults.segments(
                                colors = colors,
                                gradient = horizontalPlotFade,
                            ),
                    ),
            )
        }
        GradientGalleryRow {
            StackedBarGalleryExample(
                title = "Vertical fade across the chart",
                description = "Colors are strongest at the chart top and fade toward the baseline.",
                data = sample.dataSet,
                style =
                    StackedBarChartDefaults.style(
                        segments =
                            StackedBarChartDefaults.segments(
                                colors = colors,
                                gradient = plotFade,
                            ),
                    ),
            )
            StackedBarGalleryExample(
                title = "Horizontal fade in each segment",
                description = "The left-to-right fade starts again in every bar, keeping each series color.",
                data = sample.dataSet,
                style =
                    StackedBarChartDefaults.style(
                        segments =
                            StackedBarChartDefaults.segments(
                                colors = colors,
                                gradient = horizontalSegmentFade,
                            ),
                    ),
            )
        }
    }
}

@Composable
private fun RowScope.StackedBarGalleryExample(
    title: String,
    description: String,
    data: ChartData,
    style: StackedBarChartStyle,
) {
    GradientGalleryExample(title = title, description = description, modifier = Modifier.weight(1f)) { chartModifier ->
        StackedBarChart(
            data = data,
            modifier = chartModifier,
            style = style,
            animateOnStart = SCREENSHOT_ANIMATE_ON_START,
        )
    }
}
