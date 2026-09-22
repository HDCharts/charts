package io.github.hdcharts.app.gif

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import io.github.hdcharts.app.gif.docs.LiveSensorReadingViewModel
import io.github.hdcharts.app.gif.docs.MorphingLineViewModel
import io.github.hdcharts.app.gif.docs.ShowBar
import io.github.hdcharts.app.gif.docs.ShowHistogram
import io.github.hdcharts.app.gif.docs.ShowLine
import io.github.hdcharts.app.gif.docs.ShowLineWithRange
import io.github.hdcharts.app.gif.docs.ShowLiveLine
import io.github.hdcharts.app.gif.docs.ShowMorphingLine
import io.github.hdcharts.app.gif.docs.ShowMultiLine
import io.github.hdcharts.app.gif.docs.ShowPie
import io.github.hdcharts.app.gif.docs.ShowRadar
import io.github.hdcharts.app.gif.docs.ShowStackedArea
import io.github.hdcharts.app.gif.docs.ShowStackedBar
import io.github.hdcharts.sampleshared.theme.AppTheme
import io.github.hdcharts.sampleshared.theme.docsSlate
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
    DocsGifScene { ShowPie() }
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
    DocsGifScene { ShowLine() }
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
    DocsGifScene { ShowMultiLine() }
}

@RecordGif(
    name = "line_range",
    interactionNodeTag = "LineChartPlot",
    interactions = [
        GifInteraction(type = GifInteractionType.TAP, target = GifInteractionTarget.LEFT, framesAfter = 14),
        GifInteraction(type = GifInteractionType.TAP, target = GifInteractionTarget.CENTER, framesAfter = 14),
        GifInteraction(type = GifInteractionType.TAP, target = GifInteractionTarget.RIGHT, framesAfter = 14),
    ],
)
@Composable
fun LineRangeGifScenario() {
    DocsGifScene { ShowLineWithRange() }
}

@RecordGif(
    name = "line_morph",
    fps = 25,
)
@Composable
fun LineMorphGifScenario() {
    DocsGifScene {
        // Injects a composition-bound scope so the ticker respects the recorder's virtual frame clock.
        val scope = rememberCoroutineScope()
        val viewModel = remember { MorphingLineViewModel(tickerScope = scope) }
        ShowMorphingLine(viewModel = viewModel)
    }
}

@RecordGif(
    name = "line_timeline",
    durationMs = 1500,
    fps = 25,
)
@Composable
fun LineTimelineGifScenario() {
    DocsGifScene {
        // Injects a composition-bound scope so the ticker respects the recorder's virtual frame clock.
        val scope = rememberCoroutineScope()
        val viewModel = remember { LiveSensorReadingViewModel(tickerScope = scope) }
        ShowLiveLine(viewModel = viewModel)
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
    DocsGifScene { ShowBar() }
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
    DocsGifScene { ShowHistogram() }
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
    DocsGifScene { ShowStackedBar() }
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
    DocsGifScene { ShowStackedArea() }
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
    DocsGifScene { ShowRadar() }
}

@Composable
private fun DocsGifScene(chartContent: @Composable () -> Unit) {
    AppTheme(theme = docsSlate, darkTheme = false, useDynamicColors = false) {
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center,
        ) {
            chartContent()
        }
    }
}
