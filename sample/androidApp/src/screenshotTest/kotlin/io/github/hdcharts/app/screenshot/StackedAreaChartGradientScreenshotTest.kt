package io.github.hdcharts.app.screenshot

import androidx.compose.foundation.layout.RowScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.android.tools.screenshot.PreviewTest
import io.github.hdcharts.app.screenshot.shared.GradientGalleryPreview
import io.github.hdcharts.app.screenshot.shared.SCREENSHOT_ANIMATE_ON_START
import io.github.hdcharts.app.screenshot.shared.SCREENSHOT_STACKED_AREA_SAMPLE_USE_CASE
import io.github.hdcharts.core.model.ChartData
import io.github.hdcharts.core.style.ChartGradients
import io.github.hdcharts.core.style.GradientPoint
import io.github.hdcharts.core.style.GradientSpan
import io.github.hdcharts.sampleshared.theme.LocalChartColors
import io.github.hdcharts.sampleshared.theme.seriesColors
import io.github.hdcharts.stackedarea.StackedAreaChart
import io.github.hdcharts.stackedarea.StackedAreaChartDefaults
import io.github.hdcharts.stackedarea.StackedAreaChartStyle

/** A 2x2 landscape grid of fades that preserve stacked-area series colors. */
@PreviewTest
@GradientGalleryPreview
@Composable
fun StackedAreaChartGradientPreview() {
    val sample = SCREENSHOT_STACKED_AREA_SAMPLE_USE_CASE.deterministic()
    GradientGallerySurface(title = "Stacked area gradients") {
        val colors = LocalChartColors.current.seriesColors(sample.seriesKeys.size)
        val layerFade = ChartGradients.fade(endAlpha = GRADIENT_GALLERY_END_ALPHA, span = GradientSpan.Shape)
        val plotFade = ChartGradients.fade(endAlpha = GRADIENT_GALLERY_END_ALPHA, span = GradientSpan.Plot)
        val horizontalPlotFade =
            plotFade.copy(
                start = GradientPoint(x = 0f, y = 0.5f),
                end = GradientPoint(x = 1f, y = 0.5f),
            )
        val freeLayerFade = seriesPlotGradient(colors.first())
        GradientGalleryRow {
            StackedAreaGalleryExample(
                title = "Vertical fade in each layer",
                description = "Each layer keeps its color and fades from its highest point toward its lowest point.",
                data = sample.data,
                style =
                    StackedAreaChartDefaults.style(
                        fill =
                            StackedAreaChartDefaults.fill(
                                colors = colors,
                                gradient = layerFade,
                            ),
                    ),
            )
            StackedAreaGalleryExample(
                title = "Horizontal fade across the chart",
                description = "All series fade together from left to right, keeping their own colors.",
                data = sample.data,
                style =
                    StackedAreaChartDefaults.style(
                        fill =
                            StackedAreaChartDefaults.fill(
                                colors = colors,
                                gradient = horizontalPlotFade,
                            ),
                    ),
            )
        }
        GradientGalleryRow {
            StackedAreaGalleryExample(
                title = "Vertical fade across the chart",
                description = "Colors are strongest at the chart top and fade toward the baseline.",
                data = sample.data,
                style =
                    StackedAreaChartDefaults.style(
                        fill =
                            StackedAreaChartDefaults.fill(
                                colors = colors,
                                gradient = plotFade,
                            ),
                    ),
            )
            StackedAreaGalleryExample(
                title = "One layer fades",
                description = "Only the Free layer fades across the plot; the other layers stay solid.",
                data = sample.data,
                style =
                    StackedAreaChartDefaults.style(
                        fill =
                            StackedAreaChartDefaults.fill(
                                colors = colors,
                                seriesGradients =
                                    listOf(
                                        freeLayerFade,
                                        null,
                                    ),
                            ),
                    ),
            )
        }
    }
}

@Composable
private fun RowScope.StackedAreaGalleryExample(
    title: String,
    description: String,
    data: ChartData,
    style: StackedAreaChartStyle,
) {
    GradientGalleryExample(title = title, description = description, modifier = Modifier.weight(1f)) { chartModifier ->
        StackedAreaChart(
            data = data,
            modifier = chartModifier,
            style = style,
            animateOnStart = SCREENSHOT_ANIMATE_ON_START,
        )
    }
}
