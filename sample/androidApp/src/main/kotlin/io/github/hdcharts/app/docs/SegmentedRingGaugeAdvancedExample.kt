package io.github.hdcharts.app.docs

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.hdcharts.core.model.ChartValueFormatters
import io.github.hdcharts.core.model.rememberChartSelection
import io.github.hdcharts.core.model.toChartData
import io.github.hdcharts.core.style.ChartContainerDefaults
import io.github.hdcharts.gauge.SegmentedRingGaugeChart
import io.github.hdcharts.gauge.SegmentedRingGaugeChartDefaults
import io.github.hdcharts.gauge.SegmentedRingGaugeDirection
import io.github.hdcharts.gauge.SegmentedRingGaugeStartPosition

/** Demonstrates the complete chart-specific segmented ring gauge configuration. */
@Composable
fun AdvancedSegmentedRingGauge() {
    val selection = rememberChartSelection(initialIndex = 1)
    val capacity = 2_000.0

    SegmentedRingGaugeChart(
        modifier = Modifier.fillMaxWidth().height(320.dp),
        data =
            listOf(240.0, 180.0, 320.0).toChartData(
                categories = listOf("Carbs", "Protein", "Fat"),
            ),
        capacity = capacity,
        title = "Daily Nutrition",
        subtitle = "Current intake by macronutrient",
        centerSupportingLabel = "of ${capacity.toInt()} kcal",
        // Alternative fixed primary label: uncomment this and remove centerValueFormatter below.
        // centerLabel = "740 kcal",
        centerValueFormatter = ChartValueFormatters.suffix(" kcal"),
        selectedValueFormatter = ChartValueFormatters.suffix(" kcal"),
        selection = selection,
        interactionEnabled = false,
        animateOnStart = false,
        style =
            SegmentedRingGaugeChartDefaults.style(
                chartContainerStyle = ChartContainerDefaults.style(contentPadding = 16.dp),
                segments =
                    SegmentedRingGaugeChartDefaults.segments(
                        alpha = 0.95f,
                        colors =
                            listOf(
                                Color(0xFF3B91CC),
                                Color(0xFF1EB157),
                                Color(0xFFE3B713),
                            ),
                        // Alternative palette: remove colors above and uncomment baseColor.
                        // baseColor = Color(0xFF3B91CC),
                        width = 28.dp,
                        spacing = 6.dp,
                        cornerRadius = 8.dp,
                        // Any start position can be paired with either direction.
                        // Options: Top, TopRight, Right, BottomRight, Bottom, BottomLeft, Left, TopLeft.
                        startPosition = SegmentedRingGaugeStartPosition.Top,
                        // Options: Clockwise or CounterClockwise.
                        direction = SegmentedRingGaugeDirection.Clockwise,
                    ),
                track = SegmentedRingGaugeChartDefaults.track(color = Color(0xFFE5E7EB)),
                centerLabels =
                    SegmentedRingGaugeChartDefaults.centerLabels(
                        value = TextStyle(color = Color(0xFF111827), fontSize = 32.sp),
                        supporting = TextStyle(color = Color(0xFF6B7280), fontSize = 14.sp),
                        spacing = 6.dp,
                    ),
                legend = SegmentedRingGaugeChartDefaults.legend(visible = false),
            ),
    )
}
