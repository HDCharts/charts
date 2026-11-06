package io.github.hdcharts.waterfall

import androidx.compose.runtime.Composable
import io.github.hdcharts.core.ChartsPreviewLightDark
import io.github.hdcharts.core.ChartsPreviewTheme

@ChartsPreviewLightDark
@Composable
private fun WaterfallChartPreview() {
    ChartsPreviewTheme {
        WaterfallChart(
            data =
                WaterfallData(
                    listOf(
                        WaterfallEntry.Change("Sales", 100.0),
                        WaterfallEntry.Change("Costs", -60.0),
                        WaterfallEntry.Total("Profit"),
                    ),
                ),
            title = "Revenue to profit",
        )
    }
}
