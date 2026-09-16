package io.github.hdcharts.app.gif

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import io.github.hdcharts.app.gif.DocsGifScenariosData.STACKED_AREA_TITLE
import io.github.hdcharts.app.gif.DocsGifScenariosData.STACKED_BAR_TITLE
import io.github.hdcharts.charts.BarChart
import io.github.hdcharts.charts.HistogramChart
import io.github.hdcharts.charts.LineChart
import io.github.hdcharts.charts.PieChart
import io.github.hdcharts.charts.RadarChart
import io.github.hdcharts.charts.StackedAreaChart
import io.github.hdcharts.charts.StackedBarChart
import io.github.hdcharts.charts.model.ChartValueFormatters
import io.github.hdcharts.charts.model.toChartData
import io.github.hdcharts.sampleshared.data.pieSampleUseCase
import io.github.hdcharts.sampleshared.data.radarSampleUseCase
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
    val sample = pieSampleUseCase().initialPieSample()
    DocsGifScene {
        PieChart(
            data = sample.slices,
            title = sample.title,
        )
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
        val scenario = DocsGifScenariosData.line()
        LineChart(
            data =
                DocsGifScenariosData.buildSingleSeries(
                    scenario,
                    DocsGifScenariosData.LINE_TITLE,
                ),
            title = DocsGifScenariosData.LINE_TITLE,
        )
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
        val scenario = DocsGifScenariosData.multiLine()
        LineChart(
            data = scenario.items.toChartData(categories = scenario.categories),
            title = DocsGifScenariosData.MULTI_LINE_TITLE,
            valueFormatter = ChartValueFormatters.prefix("$"),
        )
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
        val scenario = DocsGifScenariosData.bar()
        BarChart(
            data =
                DocsGifScenariosData.buildSingleSeries(
                    scenario,
                    DocsGifScenariosData.BAR_TITLE,
                ),
            title = DocsGifScenariosData.BAR_TITLE,
        )
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
        val scenario = DocsGifScenariosData.histogram()
        HistogramChart(
            data =
                DocsGifScenariosData.buildSingleSeries(
                    scenario,
                    DocsGifScenariosData.HISTOGRAM_TITLE,
                ),
            title = DocsGifScenariosData.HISTOGRAM_TITLE,
        )
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
        val scenario = DocsGifScenariosData.stackedBar()
        StackedBarChart(
            data = scenario.items.toChartData(categories = scenario.categories),
            title = STACKED_BAR_TITLE,
        )
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
        val scenario = DocsGifScenariosData.stackedArea()
        StackedAreaChart(
            data = scenario.items.toChartData(categories = scenario.categories),
            title = STACKED_AREA_TITLE,
        )
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
        val data = radarSampleUseCase().initialRadarDefaultData()
        RadarChart(
            data = data,
            title = data.series.single().name,
        )
    }
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
