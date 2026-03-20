package io.github.dautovicharis.charts.app.demo.stackedbar

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import io.github.dautovicharis.charts.app.ui.composable.ChartAspectRatioPreset
import io.github.dautovicharis.charts.app.ui.composable.ChartStyleItems
import io.github.dautovicharis.charts.app.ui.composable.StyleItems
import io.github.dautovicharis.charts.app.ui.composable.toChartModifier
import io.github.dautovicharis.charts.demoshared.fixtures.ChartTestStyleFixtures
import io.github.dautovicharis.charts.style.ChartViewDefaults
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
        StackedBarChartDefaults.style(chartViewStyle = chartViewStyle(aspectRatioPreset))

    @Composable
    fun customStyle(
        barColors: List<Color>,
        aspectRatioPreset: ChartAspectRatioPreset = ChartAspectRatioPreset.Square,
    ) = ChartTestStyleFixtures.stackedBarCustomStyle(
        chartViewStyle = chartViewStyle(aspectRatioPreset),
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
    private fun chartViewStyle(aspectRatioPreset: ChartAspectRatioPreset) =
        ChartViewDefaults.style(modifierChart = aspectRatioPreset.toChartModifier())
}
