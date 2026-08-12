package dev.hdcode.charts.app.gif

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentHeight
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
import io.github.dautovicharis.charts.BarChart
import io.github.dautovicharis.charts.HistogramChart
import io.github.dautovicharis.charts.LineChart
import io.github.dautovicharis.charts.PieChart
import io.github.dautovicharis.charts.RadarChart
import io.github.dautovicharis.charts.StackedAreaChart
import io.github.dautovicharis.charts.StackedBarChart
import io.github.hdcodedev.composegif.annotations.GifFractionPoint
import io.github.hdcodedev.composegif.annotations.GifGestureStep
import io.github.hdcodedev.composegif.annotations.GifGestureType
import io.github.hdcodedev.composegif.annotations.GifInteraction
import io.github.hdcodedev.composegif.annotations.GifInteractionTarget
import io.github.hdcodedev.composegif.annotations.GifInteractionType
import io.github.hdcodedev.composegif.annotations.GifSwipeDirection
import io.github.hdcodedev.composegif.annotations.GifSwipeDistance
import io.github.hdcodedev.composegif.annotations.GifSwipeSpeed
import io.github.hdcodedev.composegif.annotations.RecordGif

@RecordGif(
    name = "pie_default",
    interactionNodeTag = "PieChart",
    interactions = [
        GifInteraction(type = GifInteractionType.TAP, target = GifInteractionTarget.TOP, framesAfter = 14),
        GifInteraction(type = GifInteractionType.TAP, target = GifInteractionTarget.RIGHT, framesAfter = 14),
        GifInteraction(type = GifInteractionType.TAP, target = GifInteractionTarget.LEFT, framesAfter = 14),
    ],
)
@Composable
fun PieDefaultGifScenario() {
    DocsGifScene {
        PieChart(pieSampleUseCase().initialPieSample().dataSet)
    }
}

@RecordGif(
    name = "line_default",
    interactionNodeTag = "LineChartPlot",
    interactions = [
        GifInteraction(
            type = GifInteractionType.SWIPE,
            direction = GifSwipeDirection.LEFT_TO_RIGHT,
            distance = GifSwipeDistance.LONG,
            speed = GifSwipeSpeed.SLOW,
        ),
    ],
)
@Composable
fun LineDefaultGifScenario() {
    DocsGifScene {
        LineChart(lineSampleUseCase().initialLineDataSet())
    }
}

@RecordGif(
    name = "multi_line_default",
    interactionNodeTag = "LineChartPlot",
    interactions = [
        GifInteraction(
            type = GifInteractionType.SWIPE,
            direction = GifSwipeDirection.LEFT_TO_RIGHT,
            distance = GifSwipeDistance.LONG,
            speed = GifSwipeSpeed.SLOW,
        ),
        GifInteraction(type = GifInteractionType.TAP, target = GifInteractionTarget.LEFT, framesAfter = 24),
        GifInteraction(type = GifInteractionType.TAP, target = GifInteractionTarget.CENTER, framesAfter = 24),
        GifInteraction(type = GifInteractionType.TAP, target = GifInteractionTarget.RIGHT, framesAfter = 24),
    ],
)
@Composable
fun MultiLineDefaultGifScenario() {
    DocsGifScene {
        LineChart(multiLineSampleUseCase().initialMultiLineSample().dataSet)
    }
}

@RecordGif(
    name = "bar_default",
    interactionNodeTag = "BarChartPlot",
    interactions = [
        GifInteraction(type = GifInteractionType.TAP, target = GifInteractionTarget.LEFT, framesAfter = 14),
        GifInteraction(type = GifInteractionType.TAP, target = GifInteractionTarget.TOP, framesAfter = 14),
        GifInteraction(type = GifInteractionType.TAP, target = GifInteractionTarget.RIGHT, framesAfter = 14),
    ],
)
@Composable
fun BarDefaultGifScenario() {
    DocsGifScene {
        BarChart(barSampleUseCase().initialBarDataSet())
    }
}

@RecordGif(
    name = "histogram_default",
    interactionNodeTag = "HistogramChart",
    interactions = [
        GifInteraction(type = GifInteractionType.TAP, target = GifInteractionTarget.LEFT, framesAfter = 14),
        GifInteraction(type = GifInteractionType.TAP, target = GifInteractionTarget.TOP, framesAfter = 14),
        GifInteraction(type = GifInteractionType.TAP, target = GifInteractionTarget.RIGHT, framesAfter = 14),
    ],
)
@Composable
fun HistogramDefaultGifScenario() {
    DocsGifScene {
        HistogramChart(histogramSampleUseCase().initialHistogramDataSet())
    }
}

@RecordGif(
    name = "stacked_bar_default",
    interactionNodeTag = "StackedBarChartPlot",
    interactions = [
        GifInteraction(
            type = GifInteractionType.SWIPE,
            direction = GifSwipeDirection.LEFT_TO_RIGHT,
            distance = GifSwipeDistance.LONG,
            speed = GifSwipeSpeed.SLOW,
        ),
    ],
)
@Composable
fun StackedBarDefaultGifScenario() {
    DocsGifScene {
        StackedBarChart(stackedBarSampleUseCase().initialStackedBarSample().dataSet)
    }
}

@RecordGif(
    name = "stacked_area_default",
    interactionNodeTag = "StackedAreaChartPlot",
    interactions = [
        GifInteraction(
            type = GifInteractionType.SWIPE,
            direction = GifSwipeDirection.LEFT_TO_RIGHT,
            distance = GifSwipeDistance.LONG,
            speed = GifSwipeSpeed.SLOW,
        ),
        GifInteraction(type = GifInteractionType.TAP, target = GifInteractionTarget.LEFT, framesAfter = 24),
        GifInteraction(type = GifInteractionType.TAP, target = GifInteractionTarget.CENTER, framesAfter = 24),
        GifInteraction(type = GifInteractionType.TAP, target = GifInteractionTarget.RIGHT, framesAfter = 36),
    ],
)
@Composable
fun StackedAreaDefaultGifScenario() {
    DocsGifScene {
        StackedAreaChart(stackedAreaSampleUseCase().initialStackedAreaSample().dataSet)
    }
}

@RecordGif(
    name = "radar_default",
    interactionNodeTag = "RadarChart",
    gestures = [
        GifGestureStep(
            type = GifGestureType.DRAG_PATH,
            points = [
                GifFractionPoint(x = 0.5f, y = 0.2f),
                GifFractionPoint(x = 0.8f, y = 0.5f),
                GifFractionPoint(x = 0.5f, y = 0.8f),
                GifFractionPoint(x = 0.2f, y = 0.5f),
                GifFractionPoint(x = 0.5f, y = 0.2f),
            ],
            holdStartFrames = 6,
            framesPerWaypoint = 30,
            releaseFrames = 8,
        ),
    ],
)
@Composable
fun RadarDefaultGifScenario() {
    DocsGifScene {
        RadarChart(radarSampleUseCase().initialRadarDefaultDataSet())
    }
}

@Composable
private fun DocsGifScene(chartContent: @Composable () -> Unit) {
    AppTheme(theme = docsSlate, darkTheme = false, useDynamicColors = false) {
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center,
        ) {
            chartContent()
        }
    }
}
