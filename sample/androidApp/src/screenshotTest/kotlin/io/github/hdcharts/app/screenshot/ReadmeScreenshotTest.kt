package io.github.hdcharts.app.screenshot

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.android.tools.screenshot.PreviewTest
import io.github.hdcharts.app.screenshot.shared.SCREENSHOT_ANIMATE_ON_START
import io.github.hdcharts.app.screenshot.shared.SCREENSHOT_BAR_SAMPLE_USE_CASE
import io.github.hdcharts.app.screenshot.shared.SCREENSHOT_HISTOGRAM_SAMPLE_USE_CASE
import io.github.hdcharts.app.screenshot.shared.SCREENSHOT_MULTI_LINE_SAMPLE_USE_CASE
import io.github.hdcharts.app.screenshot.shared.SCREENSHOT_PIE_SAMPLE_USE_CASE
import io.github.hdcharts.app.screenshot.shared.SCREENSHOT_RADAR_SAMPLE_USE_CASE
import io.github.hdcharts.app.screenshot.shared.SCREENSHOT_STACKED_AREA_SAMPLE_USE_CASE
import io.github.hdcharts.app.screenshot.shared.SCREENSHOT_THOUSANDS_OF_DOLLARS
import io.github.hdcharts.app.screenshot.shared.ScreenshotSurface
import io.github.hdcharts.app.screenshot.shared.categoryIndex
import io.github.hdcharts.bar.BarChart
import io.github.hdcharts.core.model.staticChartSelection
import io.github.hdcharts.core.style.BarChartDefaults
import io.github.hdcharts.core.style.ChartGradients
import io.github.hdcharts.core.style.GradientSpan
import io.github.hdcharts.core.style.HistogramChartDefaults
import io.github.hdcharts.histogram.HistogramChart
import io.github.hdcharts.line.LineChart
import io.github.hdcharts.line.LineChartDefaults
import io.github.hdcharts.pie.PieChart
import io.github.hdcharts.pie.PieChartDefaults
import io.github.hdcharts.radar.RadarChart
import io.github.hdcharts.radar.RadarChartDefaults
import io.github.hdcharts.sampleshared.theme.LocalChartColors
import io.github.hdcharts.sampleshared.theme.seriesColor
import io.github.hdcharts.sampleshared.theme.seriesColors
import io.github.hdcharts.stackedarea.StackedAreaChart
import io.github.hdcharts.stackedarea.StackedAreaChartDefaults

private const val README_DEVICE = "spec:width=1280dp,height=800dp,dpi=240"
private const val LINE_SELECTION_LABEL = "Oct"
private const val PIE_SELECTION_INDEX = 1
private const val DONUT_HOLE_PERCENTAGE = 58f
private val DISTINCT_SERIES_ORDER = listOf(0, 3, 6, 4)

private val ChartSpacing = 24.dp

/** README feature image; the docs screenshots page skips it by preview name. */
@PreviewTest
@Preview(name = "README light", device = README_DEVICE, uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "README dark", device = README_DEVICE, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun ReadmeShowcasePreview() {
    ScreenshotSurface {
        val colorScheme = MaterialTheme.colorScheme
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(Brush.linearGradient(listOf(colorScheme.background, colorScheme.primaryContainer)))
                    .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(ChartSpacing),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().weight(1f),
                horizontalArrangement = Arrangement.spacedBy(ChartSpacing),
            ) {
                Box(modifier = Modifier.weight(1f).fillMaxHeight()) { HistogramShowcase() }
                Box(modifier = Modifier.weight(1f).fillMaxHeight()) { StackedAreaShowcase() }
                Box(modifier = Modifier.weight(1f).fillMaxHeight()) { SignedBarShowcase() }
            }
            Row(
                modifier = Modifier.fillMaxWidth().weight(1f),
                horizontalArrangement = Arrangement.spacedBy(ChartSpacing),
            ) {
                Box(modifier = Modifier.weight(1f).fillMaxHeight()) { DonutShowcase() }
                Box(modifier = Modifier.weight(1f).fillMaxHeight()) { MultiLineShowcase() }
                Box(modifier = Modifier.weight(1f).fillMaxHeight()) { RadarShowcase() }
            }
        }
    }
}

// Palette colors far apart, so neighbouring lines and polygons stay easy to tell apart.
@Composable
private fun distinctSeriesColors(count: Int): List<Color> {
    val chartColors = LocalChartColors.current
    return List(count) { index -> chartColors.seriesColor(DISTINCT_SERIES_ORDER[index % DISTINCT_SERIES_ORDER.size]) }
}

@Composable
private fun DonutShowcase() {
    val sample = SCREENSHOT_PIE_SAMPLE_USE_CASE.deterministic()
    val sliceColors = LocalChartColors.current.seriesColors(sample.data.categories.size)
    PieChart(
        data = sample.data,
        modifier = Modifier.fillMaxSize(),
        title = sample.title,
        style =
            PieChartDefaults.style(
                donut = PieChartDefaults.donut(holePercentage = DONUT_HOLE_PERCENTAGE),
                slices = PieChartDefaults.slices(colors = sliceColors),
                legend = PieChartDefaults.legend(visible = false),
            ),
        selection = staticChartSelection(PIE_SELECTION_INDEX),
        interactionEnabled = false,
        animateOnStart = SCREENSHOT_ANIMATE_ON_START,
    )
}

@Composable
private fun MultiLineShowcase() {
    val sample = SCREENSHOT_MULTI_LINE_SAMPLE_USE_CASE.deterministic()
    val lineColors = distinctSeriesColors(sample.seriesKeys.size)
    LineChart(
        data = sample.dataSet,
        modifier = Modifier.fillMaxSize(),
        title = sample.title,
        style = LineChartDefaults.style(line = LineChartDefaults.line(colors = lineColors)),
        selectedValueFormatter = SCREENSHOT_THOUSANDS_OF_DOLLARS,
        axisValueFormatter = SCREENSHOT_THOUSANDS_OF_DOLLARS,
        selection = staticChartSelection(sample.dataSet.categoryIndex(LINE_SELECTION_LABEL)),
        interactionEnabled = false,
        animateOnStart = SCREENSHOT_ANIMATE_ON_START,
    )
}

@Composable
private fun RadarShowcase() {
    val sample = SCREENSHOT_RADAR_SAMPLE_USE_CASE.deterministic()
    RadarChart(
        data = sample.data,
        modifier = Modifier.fillMaxSize(),
        title = sample.title,
        style =
            RadarChartDefaults.style(
                // Docs Slate dark leaves outlineVariant unset, so the default web is faint; see #613.
                grid = RadarChartDefaults.grid(color = MaterialTheme.colorScheme.outline),
                axes = RadarChartDefaults.axes(lineColor = MaterialTheme.colorScheme.outline),
                polygon = RadarChartDefaults.polygon(lineColors = distinctSeriesColors(sample.seriesKeys.size)),
                legend = RadarChartDefaults.legend(visible = false),
            ),
        animateOnStart = SCREENSHOT_ANIMATE_ON_START,
    )
}

@Composable
private fun HistogramShowcase() {
    val gradientColors = galleryGradientColors()
    val data = SCREENSHOT_HISTOGRAM_SAMPLE_USE_CASE.deterministic()
    HistogramChart(
        data = data,
        modifier = Modifier.fillMaxSize(),
        title = data.series.single().name,
        style =
            HistogramChartDefaults.style(
                bars =
                    HistogramChartDefaults.bars(
                        gradient = ChartGradients.vertical(colors = gradientColors, span = GradientSpan.Plot),
                    ),
                zoomControlsVisible = false,
            ),
        animateOnStart = SCREENSHOT_ANIMATE_ON_START,
    )
}

@Composable
private fun StackedAreaShowcase() {
    val sample = SCREENSHOT_STACKED_AREA_SAMPLE_USE_CASE.deterministic()
    StackedAreaChart(
        data = sample.data,
        modifier = Modifier.fillMaxSize(),
        title = sample.title,
        style =
            StackedAreaChartDefaults.style(
                fill =
                    StackedAreaChartDefaults.fill(
                        colors = LocalChartColors.current.seriesColors(sample.seriesKeys.size),
                        bezier = true,
                        gradient = ChartGradients.fade(endAlpha = GRADIENT_GALLERY_END_ALPHA),
                    ),
            ),
        animateOnStart = SCREENSHOT_ANIMATE_ON_START,
    )
}

@Composable
private fun SignedBarShowcase() {
    val chartColors = LocalChartColors.current
    // One gradient across the plot, from green at the top to pink at the bottom.
    val greenToPink = listOf(chartColors.seriesColor(7), chartColors.seriesColor(5), chartColors.seriesColor(3))
    val data = SCREENSHOT_BAR_SAMPLE_USE_CASE.deterministic(signed = true)
    BarChart(
        data = data,
        modifier = Modifier.fillMaxSize(),
        title = data.series.single().name,
        style =
            BarChartDefaults.style(
                bars =
                    BarChartDefaults.bars(
                        gradient = ChartGradients.vertical(colors = greenToPink, span = GradientSpan.Plot),
                    ),
            ),
        animateOnStart = SCREENSHOT_ANIMATE_ON_START,
    )
}
