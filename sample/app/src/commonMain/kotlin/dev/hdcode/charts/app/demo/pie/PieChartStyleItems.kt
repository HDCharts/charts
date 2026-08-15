package dev.hdcode.charts.app.demo.pie

import androidx.compose.runtime.Composable
import dev.hdcode.charts.app.ui.composable.ChartStyleItems
import dev.hdcode.charts.app.ui.composable.StyleItems
import dev.hdcode.charts.sampleshared.fixtures.ChartTestStyleFixtures
import io.github.dautovicharis.charts.style.ChartContainerDefaults
import io.github.dautovicharis.charts.style.PieChartDefaults

object PieChartStyleItems {
    @Composable
    fun customStyle() =
        ChartTestStyleFixtures.pieCustomStyle(
            chartContainerStyle = ChartContainerDefaults.style(),
        )

    @Composable
    fun custom(): StyleItems =
        ChartStyleItems(
            currentStyle = customStyle(),
            defaultStyle = PieChartDefaults.style(),
        )
}
