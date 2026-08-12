package dev.hdcode.charts.app.demo.histogram

import androidx.compose.runtime.Composable
import dev.hdcode.charts.app.ui.composable.ChartAspectRatioPreset
import dev.hdcode.charts.app.ui.composable.ChartStyleItems
import dev.hdcode.charts.app.ui.composable.StyleItems
import dev.hdcode.charts.app.ui.composable.toChartModifier
import dev.hdcode.charts.sampleshared.fixtures.ChartTestStyleFixtures
import io.github.dautovicharis.charts.style.ChartViewDefaults
import io.github.dautovicharis.charts.style.HistogramChartDefaults

object HistogramChartStyleItems {
    @Composable
    fun default(aspectRatioPreset: ChartAspectRatioPreset = ChartAspectRatioPreset.Square): StyleItems =
        ChartStyleItems(
            currentStyle = defaultStyle(aspectRatioPreset),
            defaultStyle = defaultStyle(),
        )

    @Composable
    fun defaultStyle(aspectRatioPreset: ChartAspectRatioPreset = ChartAspectRatioPreset.Square) =
        HistogramChartDefaults.style(chartViewStyle = chartViewStyle(aspectRatioPreset))

    @Composable
    fun customStyle(
        barCount: Int,
        minValue: Float,
        maxValue: Float,
        aspectRatioPreset: ChartAspectRatioPreset = ChartAspectRatioPreset.Square,
    ) = ChartTestStyleFixtures.histogramCustomStyle(
        chartViewStyle = chartViewStyle(aspectRatioPreset),
        barCount = barCount,
        useBarColors = true,
        minValue = minValue,
        maxValue = maxValue,
    )

    @Composable
    fun custom(
        barCount: Int,
        minValue: Float,
        maxValue: Float,
        aspectRatioPreset: ChartAspectRatioPreset = ChartAspectRatioPreset.Square,
    ): StyleItems =
        ChartStyleItems(
            currentStyle =
                customStyle(
                    barCount = barCount,
                    minValue = minValue,
                    maxValue = maxValue,
                    aspectRatioPreset = aspectRatioPreset,
                ),
            defaultStyle = defaultStyle(),
        )

    @Composable
    private fun chartViewStyle(aspectRatioPreset: ChartAspectRatioPreset) =
        ChartViewDefaults.style(modifierChart = aspectRatioPreset.toChartModifier())
}
