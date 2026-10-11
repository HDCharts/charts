package io.github.hdcharts.app.screenshot

import androidx.compose.foundation.layout.RowScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.android.tools.screenshot.PreviewTest
import io.github.hdcharts.app.screenshot.shared.GradientGalleryPreview
import io.github.hdcharts.app.screenshot.shared.SCREENSHOT_ANIMATE_ON_START
import io.github.hdcharts.app.screenshot.shared.SCREENSHOT_BAR_SAMPLE_USE_CASE
import io.github.hdcharts.bar.BarChart
import io.github.hdcharts.core.model.ChartData
import io.github.hdcharts.core.style.BarChartDefaults
import io.github.hdcharts.core.style.BarChartStyle
import io.github.hdcharts.core.style.ChartGradients
import io.github.hdcharts.core.style.GradientSpan
import io.github.hdcharts.sampleshared.theme.LocalChartColors
import io.github.hdcharts.sampleshared.theme.seriesColor

/** A 2x2 landscape grid of labeled gradient examples for the bar chart. */
@PreviewTest
@GradientGalleryPreview
@Composable
fun BarChartGradientPreview() {
    val data = SCREENSHOT_BAR_SAMPLE_USE_CASE.deterministic()
    val signedData = SCREENSHOT_BAR_SAMPLE_USE_CASE.deterministic(signed = true)
    GradientGallerySurface(title = "Bar chart gradients") {
        val chartColors = LocalChartColors.current
        val gradientColors =
            listOf(
                chartColors.seriesColor(0),
                chartColors.seriesColor(2),
                chartColors.seriesColor(4),
            )
        GradientGalleryRow {
            BarGalleryExample(
                title = "Three-stop shape",
                description = "Three colors repeat inside every bar.",
                data = data,
                style =
                    BarChartDefaults.style(
                        bars =
                            BarChartDefaults.bars(
                                gradient = ChartGradients.vertical(gradientColors, GradientSpan.Shape),
                            ),
                    ),
            )
            BarGalleryExample(
                title = "Plot horizontal",
                description = "One left-to-right gradient across the plot.",
                data = data,
                style =
                    BarChartDefaults.style(
                        bars =
                            BarChartDefaults.bars(
                                gradient = ChartGradients.horizontal(gradientColors, GradientSpan.Plot),
                            ),
                    ),
            )
        }
        GradientGalleryRow {
            BarGalleryExample(
                title = "Plot vertical",
                description = "Bar height selects the visible gradient range.",
                data = data,
                style =
                    BarChartDefaults.style(
                        bars =
                            BarChartDefaults.bars(
                                gradient = ChartGradients.vertical(gradientColors, GradientSpan.Plot),
                            ),
                    ),
            )
            BarGalleryExample(
                title = "Signed fade",
                description = "Positive and negative bars mirror around zero.",
                data = signedData,
                style =
                    BarChartDefaults.style(
                        bars =
                            BarChartDefaults.bars(
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
private fun RowScope.BarGalleryExample(
    title: String,
    description: String,
    data: ChartData,
    style: BarChartStyle,
) {
    GradientGalleryExample(title = title, description = description, modifier = Modifier.weight(1f)) { chartModifier ->
        BarChart(
            data = data,
            modifier = chartModifier,
            style = style,
            animateOnStart = SCREENSHOT_ANIMATE_ON_START,
        )
    }
}
