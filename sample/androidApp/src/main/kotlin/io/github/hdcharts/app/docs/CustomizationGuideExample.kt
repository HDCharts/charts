package io.github.hdcharts.app.docs

// BEGIN WIKI EXAMPLE: customization-styled-cash-flow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.hdcharts.bar.BarChart
import io.github.hdcharts.core.model.ChartValueFormatters
import io.github.hdcharts.core.model.toChartData
import io.github.hdcharts.core.style.AxesDefaults
import io.github.hdcharts.core.style.AxisLinesDefaults
import io.github.hdcharts.core.style.BarChartDefaults
import io.github.hdcharts.core.style.ChartContainerDefaults
import io.github.hdcharts.core.style.GridDefaults

@Composable
fun StyledCashFlowChart() {
    val data =
        listOf(45.0, -12.0, 38.0, 27.0, -19.0).toChartData(
            categories = listOf("Mon", "Tue", "Wed", "Thu", "Fri"),
            seriesName = "Net cash flow",
        )
    val axes =
        AxesDefaults.style(
            lines = AxisLinesDefaults.style(color = Color(0xFF64748B)),
            grid = GridDefaults.style(color = Color(0xFFCBD5E1)),
            xLabels = BarChartDefaults.xLabels(color = Color(0xFF475569), size = 12.sp),
            yLabels = BarChartDefaults.yLabels(color = Color(0xFF475569), size = 12.sp),
        )

    BarChart(
        data = data,
        modifier = Modifier.fillMaxWidth().height(280.dp),
        title = "Daily net cash flow",
        subtitle = "Revenue minus costs · 14–18 October",
        style =
            BarChartDefaults.style(
                bars = BarChartDefaults.bars(color = Color(0xFF0F766E)),
                axes = axes,
                chartContainerStyle =
                    ChartContainerDefaults.style(
                        styleSubtitle = ChartContainerDefaults.style().styleSubtitle.copy(fontSize = 16.sp),
                        contentPadding = 12.dp,
                    ),
            ),
        selectedValueFormatter = ChartValueFormatters.prefix("$"),
    )
}
// END WIKI EXAMPLE: customization-styled-cash-flow
