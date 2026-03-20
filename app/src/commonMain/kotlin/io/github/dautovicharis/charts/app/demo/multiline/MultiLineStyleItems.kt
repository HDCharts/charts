package io.github.dautovicharis.charts.app.demo.multiline

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import io.github.dautovicharis.charts.app.demo.line.lineChartTableItems
import io.github.dautovicharis.charts.app.ui.composable.ChartAspectRatioPreset
import io.github.dautovicharis.charts.app.ui.composable.StyleItems
import io.github.dautovicharis.charts.app.ui.composable.toChartModifier
import io.github.dautovicharis.charts.demoshared.fixtures.ChartTestStyleFixtures
import io.github.dautovicharis.charts.style.ChartViewDefaults
import io.github.dautovicharis.charts.style.LineChartDefaults

object MultiLineStyleItems {
    @Composable
    fun defaultStyle(aspectRatioPreset: ChartAspectRatioPreset = ChartAspectRatioPreset.Square) =
        LineChartDefaults.style(chartViewStyle = chartViewStyle(aspectRatioPreset))

    @Composable
    fun customStyle(
        lineColors: List<Color>,
        aspectRatioPreset: ChartAspectRatioPreset = ChartAspectRatioPreset.Square,
    ) = ChartTestStyleFixtures.multiLineCustomStyle(
        chartViewStyle = chartViewStyle(aspectRatioPreset),
        seriesCount = lineColors.size,
    )

    @Composable
    fun custom(
        lineColors: List<Color>,
        aspectRatioPreset: ChartAspectRatioPreset = ChartAspectRatioPreset.Square,
    ): StyleItems =
        lineChartTableItems(
            currentStyle = customStyle(lineColors, aspectRatioPreset),
            defaultStyle = defaultStyle(),
        )

    @Composable
    private fun chartViewStyle(aspectRatioPreset: ChartAspectRatioPreset) =
        ChartViewDefaults.style(modifierChart = aspectRatioPreset.toChartModifier())
}
