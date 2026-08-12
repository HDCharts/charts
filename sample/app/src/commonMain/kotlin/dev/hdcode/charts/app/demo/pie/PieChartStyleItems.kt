package dev.hdcode.charts.app.demo.pie

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import dev.hdcode.charts.app.ui.composable.ChartStyleItems
import dev.hdcode.charts.app.ui.composable.StyleItems
import dev.hdcode.charts.sampleshared.fixtures.ChartTestStyleFixtures
import io.github.dautovicharis.charts.style.ChartViewDefaults
import io.github.dautovicharis.charts.style.PieChartDefaults

object PieChartStyleItems {
    @Composable
    fun customStyle(pieColors: List<Color>) =
        ChartTestStyleFixtures.pieCustomStyle(
            chartViewStyle = ChartViewDefaults.style(),
            segmentCount = pieColors.size,
        )

    @Composable
    fun custom(pieColors: List<Color>): StyleItems =
        ChartStyleItems(
            currentStyle = customStyle(pieColors),
            defaultStyle = PieChartDefaults.style(),
        )
}
