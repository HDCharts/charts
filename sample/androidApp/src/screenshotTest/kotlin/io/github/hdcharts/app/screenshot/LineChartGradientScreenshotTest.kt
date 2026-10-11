package io.github.hdcharts.app.screenshot

import androidx.compose.foundation.layout.RowScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.android.tools.screenshot.PreviewTest
import io.github.hdcharts.app.screenshot.shared.GradientGalleryPreview
import io.github.hdcharts.app.screenshot.shared.SCREENSHOT_ANIMATE_ON_START
import io.github.hdcharts.app.screenshot.shared.SCREENSHOT_LINE_SAMPLE_USE_CASE
import io.github.hdcharts.core.model.ChartData
import io.github.hdcharts.core.style.ChartGradients
import io.github.hdcharts.core.style.GradientSpan
import io.github.hdcharts.line.LineChart
import io.github.hdcharts.line.LineChartDefaults
import io.github.hdcharts.line.LineChartStyle
import io.github.hdcharts.sampleshared.theme.LocalChartColors
import io.github.hdcharts.sampleshared.theme.seriesColor

/** A single-series line colored by value. */
@PreviewTest
@GradientGalleryPreview
@Composable
fun LineChartGradientPreview() {
    val data = SCREENSHOT_LINE_SAMPLE_USE_CASE.deterministic()
    GradientGallerySurface(title = "Line chart gradients") {
        val chartColors = LocalChartColors.current
        val gradientColors = listOf(chartColors.seriesColor(0), chartColors.seriesColor(2), chartColors.seriesColor(4))
        val byValue = ChartGradients.vertical(colors = gradientColors, span = GradientSpan.Plot)
        val byValueStyle = LineChartDefaults.style(line = LineChartDefaults.line(gradient = byValue))

        GradientGalleryRow {
            LineGalleryExample(
                title = "Color by value",
                description = "High values reach the top color; low values take the bottom one.",
                data = data,
                style = byValueStyle,
            )
        }
    }
}

@Composable
private fun RowScope.LineGalleryExample(
    title: String,
    description: String,
    data: ChartData,
    style: LineChartStyle,
) {
    GradientGalleryExample(title = title, description = description, modifier = Modifier.weight(1f)) { chartModifier ->
        LineChart(
            data = data,
            modifier = chartModifier,
            style = style,
            animateOnStart = SCREENSHOT_ANIMATE_ON_START,
        )
    }
}
