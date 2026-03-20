package io.github.dautovicharis.charts.mock

import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import io.github.dautovicharis.charts.model.ChartDataSet
import io.github.dautovicharis.charts.model.toChartDataSet
import io.github.dautovicharis.charts.style.ChartViewStyle
import io.github.dautovicharis.charts.style.PieChartStyle

internal object MockTest {
    const val TITLE = "Title"

    val colors = listOf(Color.Red, Color.Green, Color.Cyan, Color.Black)

    val dataSet: ChartDataSet =
        listOf(10f, 20f, 30f, 40f).toChartDataSet(
            title = TITLE,
        )

    fun mockPieChartStyle(pieColors: List<Color> = colors): PieChartStyle =
        PieChartStyle(
            modifier = Modifier.fillMaxSize(),
            pieColors = pieColors,
            pieColor = Color.Red,
            pieAlpha = 1f,
            donutPercentage = 0.5f,
            borderColor = Color.Black,
            borderWidth = 2f,
            legendVisible = true,
            chartViewStyle = mockChartViewStyle(),
        )

    private fun mockChartViewStyle(): ChartViewStyle =
        ChartViewStyle(
            modifierMain = Modifier.fillMaxSize(),
            styleTitle = TextStyle.Default,
            modifierLegend = Modifier.fillMaxSize(),
            modifierTopTitle = Modifier.fillMaxSize(),
            innerPadding = Dp(10f),
            width = Dp.Infinity,
            backgroundColor = Color.White,
            modifierChart = Modifier.aspectRatio(1f),
        )
}
