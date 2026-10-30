package io.github.hdcharts.gauge

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import io.github.hdcharts.core.ChartsPreviewLightDark
import io.github.hdcharts.core.ChartsPreviewTheme
import io.github.hdcharts.core.model.toChartData

@ChartsPreviewLightDark
@Composable
private fun SegmentedRingGaugeChartPreview() {
    ChartsPreviewTheme {
        SegmentedRingGaugeChart(
            data = listOf(240.0, 180.0, 320.0).toChartData(categories = listOf("Carbs", "Protein", "Fat")),
            capacity = 2_000.0,
            centerSupportingLabel = "of 2000 kcal",
            style =
                SegmentedRingGaugeChartDefaults.style(
                    segments =
                        SegmentedRingGaugeChartDefaults.segments(
                            colors = listOf(Color(0xFF3B91CC), Color(0xFF1EB157), Color(0xFFE3B713)),
                        ),
                    legend = SegmentedRingGaugeChartDefaults.legend(visible = false),
                ),
        )
    }
}
