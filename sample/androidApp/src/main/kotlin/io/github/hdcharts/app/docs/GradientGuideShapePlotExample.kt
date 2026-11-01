package io.github.hdcharts.app.docs

// BEGIN WIKI EXAMPLE: gradients-shape-and-plot
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import io.github.hdcharts.bar.BarChart
import io.github.hdcharts.core.model.toChartData
import io.github.hdcharts.core.style.BarChartDefaults
import io.github.hdcharts.core.style.ChartGradients
import io.github.hdcharts.core.style.GradientSpan

@Composable
fun CompareShapeAndPlotGradients() {
    val data =
        listOf(45.0, -12.0, 38.0, 27.0, -19.0).toChartData(
            categories = listOf("Mon", "Tue", "Wed", "Thu", "Fri"),
            seriesName = "Net cash flow",
        )
    val colors = listOf(Color(0xFF1E89E6), Color(0xFF9B4EDD), Color(0xFFEA7A2E))

    Column {
        BarChart(
            data = data,
            title = "Every bar repeats the gradient",
            style =
                BarChartDefaults.style(
                    bars = BarChartDefaults.bars(gradient = ChartGradients.vertical(colors)),
                ),
        )
        BarChart(
            data = data,
            title = "One gradient spans the plot",
            style =
                BarChartDefaults.style(
                    bars =
                        BarChartDefaults.bars(
                            gradient = ChartGradients.horizontal(colors, span = GradientSpan.Plot),
                        ),
                ),
        )
    }
}
// END WIKI EXAMPLE: gradients-shape-and-plot
