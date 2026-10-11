package io.github.hdcharts.app.screenshot

import androidx.compose.foundation.layout.RowScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.android.tools.screenshot.PreviewTest
import io.github.hdcharts.app.screenshot.shared.GradientGalleryPreview
import io.github.hdcharts.app.screenshot.shared.SCREENSHOT_ANIMATE_ON_START
import io.github.hdcharts.app.screenshot.shared.SCREENSHOT_HISTOGRAM_SAMPLE_USE_CASE
import io.github.hdcharts.core.model.ChartData
import io.github.hdcharts.core.style.ChartGradients
import io.github.hdcharts.core.style.GradientSpan
import io.github.hdcharts.core.style.HistogramChartDefaults
import io.github.hdcharts.core.style.HistogramChartStyle
import io.github.hdcharts.histogram.HistogramChart
import io.github.hdcharts.sampleshared.theme.LocalChartColors
import io.github.hdcharts.sampleshared.theme.seriesColor

/** A 2x2 landscape grid of labeled gradient examples for the histogram. */
@PreviewTest
@GradientGalleryPreview
@Composable
fun HistogramChartGradientPreview() {
    val data = SCREENSHOT_HISTOGRAM_SAMPLE_USE_CASE.deterministic()
    GradientGallerySurface(title = "Histogram gradients") {
        val chartColors = LocalChartColors.current
        val gradientColors =
            listOf(
                chartColors.seriesColor(0),
                chartColors.seriesColor(2),
                chartColors.seriesColor(4),
            )
        GradientGalleryRow {
            HistogramGalleryExample(
                title = "Three-stop shape",
                description = "Three colors repeat inside every bin.",
                data = data,
                style =
                    HistogramChartDefaults.style(
                        bars =
                            HistogramChartDefaults.bars(
                                gradient = ChartGradients.vertical(gradientColors, GradientSpan.Shape),
                            ),
                    ),
            )
            HistogramGalleryExample(
                title = "Plot horizontal",
                description = "One left-to-right gradient across the plot.",
                data = data,
                style =
                    HistogramChartDefaults.style(
                        bars =
                            HistogramChartDefaults.bars(
                                gradient = ChartGradients.horizontal(gradientColors, GradientSpan.Plot),
                            ),
                    ),
            )
        }
        GradientGalleryRow {
            HistogramGalleryExample(
                title = "Plot vertical",
                description = "Bin height selects the visible gradient range.",
                data = data,
                style =
                    HistogramChartDefaults.style(
                        bars =
                            HistogramChartDefaults.bars(
                                gradient = ChartGradients.vertical(gradientColors, GradientSpan.Plot),
                            ),
                    ),
            )
            HistogramGalleryExample(
                title = "Fade",
                description = "Each bin fades toward the baseline.",
                data = data,
                style =
                    HistogramChartDefaults.style(
                        bars =
                            HistogramChartDefaults.bars(
                                gradient =
                                    ChartGradients.fade(
                                        GRADIENT_GALLERY_END_ALPHA,
                                        GradientSpan.Shape,
                                    ),
                            ),
                    ),
            )
        }
    }
}

@Composable
private fun RowScope.HistogramGalleryExample(
    title: String,
    description: String,
    data: ChartData,
    style: HistogramChartStyle,
) {
    GradientGalleryExample(title = title, description = description, modifier = Modifier.weight(1f)) { chartModifier ->
        HistogramChart(
            data = data,
            modifier = chartModifier,
            style = style,
            animateOnStart = SCREENSHOT_ANIMATE_ON_START,
        )
    }
}
