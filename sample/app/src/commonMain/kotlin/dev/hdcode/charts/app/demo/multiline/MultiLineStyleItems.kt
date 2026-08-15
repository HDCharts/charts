package dev.hdcode.charts.app.demo.multiline

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import dev.hdcode.charts.app.demo.line.lineChartTableItems
import dev.hdcode.charts.app.ui.composable.ChartAspectRatioPreset
import dev.hdcode.charts.app.ui.composable.StyleItems
import dev.hdcode.charts.app.ui.composable.toChartModifier
import dev.hdcode.charts.sampleshared.fixtures.ChartTestStyleFixtures
import io.github.dautovicharis.charts.style.ChartContainerDefaults
import io.github.dautovicharis.charts.style.LineChartDefaults

object MultiLineStyleItems {
    @Composable
    fun defaultStyle(aspectRatioPreset: ChartAspectRatioPreset = ChartAspectRatioPreset.Square) =
        LineChartDefaults.style(chartContainerStyle = chartContainerStyle(aspectRatioPreset))

    @Composable
    fun customStyle(
        lineColors: List<Color>,
        aspectRatioPreset: ChartAspectRatioPreset = ChartAspectRatioPreset.Square,
    ) = ChartTestStyleFixtures.multiLineCustomStyle(
        chartContainerStyle = chartContainerStyle(aspectRatioPreset),
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
    private fun chartContainerStyle(aspectRatioPreset: ChartAspectRatioPreset) =
        ChartContainerDefaults.style(modifierChart = aspectRatioPreset.toChartModifier())
}
