package io.github.hdcharts.app.docs

// BEGIN WIKI EXAMPLE: segmented-ring-gauge-basic-usage
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import io.github.hdcharts.core.model.toChartData
import io.github.hdcharts.gauge.SegmentedRingGaugeChart
import io.github.hdcharts.gauge.SegmentedRingGaugeChartDefaults

private data class Nutrient(
    val name: String,
    val calories: Double,
    val color: Color,
)

@Composable
fun CalorieProgress() {
    val capacity = 2_000.0
    val nutrients =
        listOf(
            Nutrient("Carbohydrates", 240.0, Color(0xFF3B91CC)),
            Nutrient("Protein", 180.0, Color(0xFF1EB157)),
            Nutrient("Fat", 320.0, Color(0xFFE3B713)),
        )

    SegmentedRingGaugeChart(
        data =
            nutrients.toChartData(
                value = { it.calories },
                category = { it.name },
            ),
        capacity = capacity,
        centerSupportingLabel = "of ${capacity.toInt()} kcal",
        style =
            SegmentedRingGaugeChartDefaults.style(
                segments =
                    SegmentedRingGaugeChartDefaults.segments(
                        colors = nutrients.map { it.color },
                    ),
            ),
    )
}
// END WIKI EXAMPLE: segmented-ring-gauge-basic-usage
