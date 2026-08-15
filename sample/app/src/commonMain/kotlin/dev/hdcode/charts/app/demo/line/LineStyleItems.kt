package dev.hdcode.charts.app.demo.line

import androidx.compose.runtime.Composable
import dev.hdcode.charts.app.ui.composable.ChartAspectRatioPreset
import dev.hdcode.charts.app.ui.composable.ChartStyleItems
import dev.hdcode.charts.app.ui.composable.StyleItems
import dev.hdcode.charts.app.ui.composable.toChartModifier
import dev.hdcode.charts.sampleshared.fixtures.ChartTestStyleFixtures
import io.github.dautovicharis.charts.style.ChartContainerDefaults
import io.github.dautovicharis.charts.style.LineChartDefaults
import io.github.dautovicharis.charts.style.LineChartStyle

object LineChartStyleItems {
    @Composable
    fun defaultStyle(aspectRatioPreset: ChartAspectRatioPreset = ChartAspectRatioPreset.Square) =
        LineChartDefaults.style(chartContainerStyle = chartContainerStyle(aspectRatioPreset))

    @Composable
    fun customStyle(aspectRatioPreset: ChartAspectRatioPreset = ChartAspectRatioPreset.Square) =
        ChartTestStyleFixtures.lineCustomStyle(chartContainerStyle = chartContainerStyle(aspectRatioPreset))

    @Composable
    fun custom(aspectRatioPreset: ChartAspectRatioPreset = ChartAspectRatioPreset.Square): StyleItems =
        lineChartTableItems(
            currentStyle = customStyle(aspectRatioPreset),
            defaultStyle = defaultStyle(),
        )

    @Composable
    private fun chartContainerStyle(aspectRatioPreset: ChartAspectRatioPreset) =
        ChartContainerDefaults.style(modifierChart = aspectRatioPreset.toChartModifier())
}

@Composable
fun lineChartTableItems(
    currentStyle: LineChartStyle,
    defaultStyle: LineChartStyle = LineChartDefaults.style(),
): StyleItems =
    ChartStyleItems(
        currentStyle = currentStyle,
        defaultStyle = defaultStyle,
    )
