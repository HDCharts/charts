package io.github.dautovicharis.charts.app.demo.stackedarea

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import io.github.dautovicharis.charts.app.ui.composable.ChartAspectRatioPreset
import io.github.dautovicharis.charts.app.ui.composable.ChartStyleItems
import io.github.dautovicharis.charts.app.ui.composable.StyleItems
import io.github.dautovicharis.charts.app.ui.composable.toChartModifier
import io.github.dautovicharis.charts.demoshared.fixtures.ChartTestStyleFixtures
import io.github.dautovicharis.charts.style.ChartViewDefaults
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
        StackedAreaChartDefaults.style(chartViewStyle = chartViewStyle(aspectRatioPreset))

    @Composable
    fun custom(aspectRatioPreset: ChartAspectRatioPreset = ChartAspectRatioPreset.Square): StyleItems =
        ChartStyleItems(
            currentStyle =
                ChartTestStyleFixtures.stackedAreaCustomStyle(
                    chartViewStyle = chartViewStyle(aspectRatioPreset),
                    seriesCount = 0,
                ),
            defaultStyle = defaultStyle(),
        )

    @Composable
    fun customStyle(
        areaColors: List<Color>,
        aspectRatioPreset: ChartAspectRatioPreset = ChartAspectRatioPreset.Square,
    ) = ChartTestStyleFixtures.stackedAreaCustomStyle(
        chartViewStyle = chartViewStyle(aspectRatioPreset),
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
    private fun chartViewStyle(aspectRatioPreset: ChartAspectRatioPreset) =
        ChartViewDefaults.style(modifierChart = aspectRatioPreset.toChartModifier())
}
