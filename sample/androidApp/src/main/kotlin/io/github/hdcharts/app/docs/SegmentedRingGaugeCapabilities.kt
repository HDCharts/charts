package io.github.hdcharts.app.docs

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.hdcharts.core.model.ChartData
import io.github.hdcharts.core.model.rememberChartSelection
import io.github.hdcharts.gauge.SegmentedRingGaugeChart
import io.github.hdcharts.gauge.SegmentedRingGaugeChartDefaults
import io.github.hdcharts.gauge.SegmentedRingGaugeDirection
import io.github.hdcharts.gauge.SegmentedRingGaugeStartPosition

// BEGIN WIKI EXAMPLE: segmented-ring-gauge-capacity-labels
@Composable
fun CapacityAndCenterLabels(data: ChartData) {
    SegmentedRingGaugeChart(
        data = data,
        capacity = 2_000.0,
        centerSupportingLabel = "of 2,000 kcal",
        style =
            SegmentedRingGaugeChartDefaults.style(
                centerLabels =
                    SegmentedRingGaugeChartDefaults.centerLabels(
                        value = TextStyle(fontSize = 32.sp),
                        supporting = TextStyle(fontSize = 14.sp),
                        spacing = 6.dp,
                    ),
            ),
    )
}
// END WIKI EXAMPLE: segmented-ring-gauge-capacity-labels

// BEGIN WIKI EXAMPLE: segmented-ring-gauge-selection
@Composable
fun SelectableNutrition(data: ChartData) {
    val selection = rememberChartSelection()

    SegmentedRingGaugeChart(
        data = data,
        capacity = 2_000.0,
        selection = selection,
    )
}
// END WIKI EXAMPLE: segmented-ring-gauge-selection

// BEGIN WIKI EXAMPLE: segmented-ring-gauge-segment-styling
@Composable
fun StyledNutrition(data: ChartData) {
    SegmentedRingGaugeChart(
        data = data,
        capacity = 2_000.0,
        style =
            SegmentedRingGaugeChartDefaults.style(
                segments =
                    SegmentedRingGaugeChartDefaults.segments(
                        width = 24.dp,
                        spacing = 5.dp,
                        cornerRadius = 8.dp,
                    ),
                track = SegmentedRingGaugeChartDefaults.track(color = Color(0xFFE5E7EB)),
            ),
    )
}
// END WIKI EXAMPLE: segmented-ring-gauge-segment-styling

// BEGIN WIKI EXAMPLE: segmented-ring-gauge-position-direction
@Composable
fun ReversedNutrition(data: ChartData) {
    SegmentedRingGaugeChart(
        data = data,
        capacity = 2_000.0,
        style =
            SegmentedRingGaugeChartDefaults.style(
                segments =
                    SegmentedRingGaugeChartDefaults.segments(
                        startPosition = SegmentedRingGaugeStartPosition.Top,
                        direction = SegmentedRingGaugeDirection.Clockwise,
                    ),
            ),
    )
}
// END WIKI EXAMPLE: segmented-ring-gauge-position-direction
