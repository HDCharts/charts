package io.github.hdcharts.app.docs

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import io.github.hdcharts.core.model.ChartData
import io.github.hdcharts.core.model.ChartSelection
import io.github.hdcharts.core.model.ChartValueFormatter
import io.github.hdcharts.core.model.rememberChartSelection
import io.github.hdcharts.pie.PieChart
import io.github.hdcharts.pie.PieChartDefaults
import kotlin.math.roundToInt

// BEGIN WIKI EXAMPLE: pie-slice-colors
@Composable
fun ShowPieWithSliceColors(data: ChartData) {
    val colors = listOf(Color.Red, Color.Green, Color.Blue, Color.Yellow, Color.Magenta)

    PieChart(
        data = data,
        style =
            PieChartDefaults.style(
                slices = PieChartDefaults.slices(colors = colors),
            ),
    )
}
// END WIKI EXAMPLE: pie-slice-colors

// BEGIN WIKI EXAMPLE: pie-selected-share
@Composable
fun ShowPieWithRoundedShare(
    data: ChartData,
    selection: ChartSelection = rememberChartSelection(),
) {
    PieChart(
        data = data,
        selection = selection,
        selectedValueFormatter = ChartValueFormatter { share -> "${share.roundToInt()}%" },
    )
}
// END WIKI EXAMPLE: pie-selected-share
