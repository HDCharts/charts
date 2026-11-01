package io.github.hdcharts.app.docs

import androidx.compose.runtime.Composable
import io.github.hdcharts.core.model.ChartData
import io.github.hdcharts.core.model.ChartValueFormatter
import io.github.hdcharts.core.model.ChartValueFormatters
import io.github.hdcharts.core.style.ChartGradients
import io.github.hdcharts.stackedbar.StackedBarChart
import io.github.hdcharts.stackedbar.StackedBarChartDefaults

// BEGIN WIKI EXAMPLE: stacked-bar-value-formatting
@Composable
fun FormattedQuarterlyRevenue(data: ChartData) {
    StackedBarChart(
        data = data,
        selectedValueFormatter = ChartValueFormatters.prefix("$"),
        axisValueFormatter =
            ChartValueFormatter { value ->
                "$" + StackedBarChartDefaults.axisValueFormatter.format(value)
            },
    )
}
// END WIKI EXAMPLE: stacked-bar-value-formatting

// BEGIN WIKI EXAMPLE: stacked-bar-fade-gradient
@Composable
fun FadedQuarterlyRevenue(data: ChartData) {
    StackedBarChart(
        data = data,
        style =
            StackedBarChartDefaults.style(
                segments =
                    StackedBarChartDefaults.segments(
                        gradient = ChartGradients.fade(endAlpha = 0.35f),
                    ),
            ),
    )
}
// END WIKI EXAMPLE: stacked-bar-fade-gradient
