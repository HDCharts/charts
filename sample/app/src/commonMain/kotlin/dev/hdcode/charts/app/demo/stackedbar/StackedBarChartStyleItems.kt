package dev.hdcode.charts.app.demo.stackedbar

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import dev.hdcode.charts.app.ui.composable.ChartAspectRatioPreset
import dev.hdcode.charts.app.ui.composable.ChartStyleItems
import dev.hdcode.charts.app.ui.composable.StyleItems
import dev.hdcode.charts.app.ui.composable.toChartModifier
import dev.hdcode.charts.sampleshared.fixtures.ChartTestStyleFixtures
import io.github.dautovicharis.charts.style.ChartContainerDefaults
import io.github.dautovicharis.charts.style.StackedBarChartDefaults

object StackedBarChartStyleItems {
    @Composable
    fun default(aspectRatioPreset: ChartAspectRatioPreset = ChartAspectRatioPreset.Square): StyleItems =
        ChartStyleItems(
            currentStyle = defaultStyle(aspectRatioPreset),
            defaultStyle = defaultStyle(),
        )

    @Composable
    fun defaultStyle(aspectRatioPreset: ChartAspectRatioPreset = ChartAspectRatioPreset.Square) =
        StackedBarChartDefaults.style(chartContainerStyle = chartContainerStyle(aspectRatioPreset))

    @Composable
    fun customStyle(
        barColors: List<Color>,
        aspectRatioPreset: ChartAspectRatioPreset = ChartAspectRatioPreset.Square,
    ) = ChartTestStyleFixtures.stackedBarCustomStyle(
        chartContainerStyle = chartContainerStyle(aspectRatioPreset),
        segmentCount = barColors.size,
    )

    @Composable
    fun custom(
        barColors: List<Color>,
        aspectRatioPreset: ChartAspectRatioPreset = ChartAspectRatioPreset.Square,
    ): StyleItems =
        ChartStyleItems(
            currentStyle = customStyle(barColors, aspectRatioPreset),
            defaultStyle = defaultStyle(),
        )

    @Composable
    private fun chartContainerStyle(aspectRatioPreset: ChartAspectRatioPreset) =
        ChartContainerDefaults.style(modifierChart = aspectRatioPreset.toChartModifier())
}
