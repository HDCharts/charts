package io.github.hdcharts.app.docs

// BEGIN WIKI EXAMPLE: gradients-custom-stops
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TileMode
import io.github.hdcharts.bar.BarChart
import io.github.hdcharts.core.model.toChartData
import io.github.hdcharts.core.style.BarChartDefaults
import io.github.hdcharts.core.style.ChartGradient
import io.github.hdcharts.core.style.GradientPoint
import io.github.hdcharts.core.style.GradientSpan
import io.github.hdcharts.core.style.GradientStop

@Composable
fun HighlightCashFlowExtremes() {
    val data =
        listOf(45.0, -12.0, 38.0, 27.0, -19.0).toChartData(
            categories = listOf("Mon", "Tue", "Wed", "Thu", "Fri"),
            seriesName = "Net cash flow",
        )
    val gradient =
        ChartGradient.Linear(
            stops =
                listOf(
                    GradientStop.Series(offset = 0f, alpha = 1f),
                    GradientStop.Fixed(offset = 0.55f, color = Color.White),
                    GradientStop.Series(offset = 1f, alpha = 0.25f),
                ),
            start = GradientPoint(x = 0f, y = 0f),
            end = GradientPoint(x = 1f, y = 1f),
            span = GradientSpan.Shape,
            tileMode = TileMode.Clamp,
        )

    BarChart(
        data = data,
        title = "Cash flow with a highlight",
        style = BarChartDefaults.style(bars = BarChartDefaults.bars(gradient = gradient)),
    )
}
// END WIKI EXAMPLE: gradients-custom-stops
