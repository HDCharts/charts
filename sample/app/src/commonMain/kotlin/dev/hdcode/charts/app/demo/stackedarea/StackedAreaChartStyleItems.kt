package dev.hdcode.charts.app.demo.stackedarea

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import dev.hdcode.charts.app.ui.composable.ChartAspectRatioPreset
import dev.hdcode.charts.app.ui.composable.ChartStyleItems
import dev.hdcode.charts.app.ui.composable.StyleItems
import dev.hdcode.charts.app.ui.composable.toChartModifier
import dev.hdcode.charts.sampleshared.fixtures.ChartTestStyleFixtures
import io.github.dautovicharis.charts.style.ChartContainerDefaults
import io.github.dautovicharis.charts.style.StackedAreaChartDefaults

object StackedAreaChartStyleItems {
    @Composable
    fun default(aspectRatioPreset: ChartAspectRatioPreset = ChartAspectRatioPreset.Square): StyleItems =
        ChartStyleItems(
            currentStyle = defaultStyle(aspectRatioPreset),
            defaultStyle = defaultStyle(),
        )

    @Composable
    fun defaultStyle(aspectRatioPreset: ChartAspectRatioPreset = ChartAspectRatioPreset.Square) =
        StackedAreaChartDefaults.style(chartContainerStyle = chartContainerStyle(aspectRatioPreset))

    @Composable
    fun custom(aspectRatioPreset: ChartAspectRatioPreset = ChartAspectRatioPreset.Square): StyleItems =
        ChartStyleItems(
            currentStyle =
                ChartTestStyleFixtures.stackedAreaCustomStyle(
                    chartContainerStyle = chartContainerStyle(aspectRatioPreset),
                    seriesCount = 0,
                ),
            defaultStyle = defaultStyle(),
        )

    @Composable
    fun customStyle(
        areaColors: List<Color>,
        aspectRatioPreset: ChartAspectRatioPreset = ChartAspectRatioPreset.Square,
    ) = ChartTestStyleFixtures.stackedAreaCustomStyle(
        chartContainerStyle = chartContainerStyle(aspectRatioPreset),
        seriesCount = areaColors.size,
    )

    @Composable
    fun custom(
        areaColors: List<Color>,
        aspectRatioPreset: ChartAspectRatioPreset = ChartAspectRatioPreset.Square,
    ): StyleItems =
        ChartStyleItems(
            currentStyle = customStyle(areaColors, aspectRatioPreset),
            defaultStyle = defaultStyle(),
        )

    @Composable
    private fun chartContainerStyle(aspectRatioPreset: ChartAspectRatioPreset) =
        ChartContainerDefaults.style(modifierChart = aspectRatioPreset.toChartModifier())
}
