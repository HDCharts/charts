package io.github.dautovicharis.charts.app.demo.line

import androidx.compose.runtime.Composable
import io.github.dautovicharis.charts.app.ui.composable.ChartAspectRatioPreset
import io.github.dautovicharis.charts.app.ui.composable.ChartStyleItems
import io.github.dautovicharis.charts.app.ui.composable.StyleItems
import io.github.dautovicharis.charts.app.ui.composable.toChartModifier
import io.github.dautovicharis.charts.demoshared.fixtures.ChartTestStyleFixtures
import io.github.dautovicharis.charts.style.ChartViewDefaults
import io.github.dautovicharis.charts.style.LineChartDefaults
import io.github.dautovicharis.charts.style.LineChartStyle

object LineChartStyleItems {
    @Composable
    fun defaultStyle(aspectRatioPreset: ChartAspectRatioPreset = ChartAspectRatioPreset.Square) =
        LineChartDefaults.style(chartViewStyle = chartViewStyle(aspectRatioPreset))

    @Composable
    fun customStyle(aspectRatioPreset: ChartAspectRatioPreset = ChartAspectRatioPreset.Square) =
        ChartTestStyleFixtures.lineCustomStyle(chartViewStyle = chartViewStyle(aspectRatioPreset))

    @Composable
    fun custom(aspectRatioPreset: ChartAspectRatioPreset = ChartAspectRatioPreset.Square): StyleItems =
        lineChartTableItems(
            currentStyle = customStyle(aspectRatioPreset),
            defaultStyle = defaultStyle(),
        )

    @Composable
    private fun chartViewStyle(aspectRatioPreset: ChartAspectRatioPreset) =
        ChartViewDefaults.style(modifierChart = aspectRatioPreset.toChartModifier())
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
