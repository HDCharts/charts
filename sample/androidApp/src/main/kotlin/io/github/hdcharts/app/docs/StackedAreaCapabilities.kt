package io.github.hdcharts.app.docs

import androidx.compose.runtime.Composable
import io.github.hdcharts.core.model.ChartData
import io.github.hdcharts.core.model.ChartValueFormatters
import io.github.hdcharts.core.style.ChartGradients
import io.github.hdcharts.stackedarea.StackedAreaChart
import io.github.hdcharts.stackedarea.StackedAreaChartDefaults

// BEGIN WIKI EXAMPLE: stacked-area-value-formatting
@Composable
fun FormattedMonthlySubscribers(data: ChartData) {
    StackedAreaChart(
        data = data,
        selectedValueFormatter = ChartValueFormatters.suffix(" subscribers"),
        axisValueFormatter = ChartValueFormatters.suffix(" total subscribers"),
    )
}
// END WIKI EXAMPLE: stacked-area-value-formatting

// BEGIN WIKI EXAMPLE: stacked-area-fade-gradient
@Composable
fun FadedMonthlySubscribers(data: ChartData) {
    StackedAreaChart(
        data = data,
        style =
            StackedAreaChartDefaults.style(
                fill =
                    StackedAreaChartDefaults.fill(
                        gradient = ChartGradients.fade(endAlpha = 0.35f),
                    ),
            ),
    )
}
// END WIKI EXAMPLE: stacked-area-fade-gradient
