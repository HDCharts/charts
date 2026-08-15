package io.github.dautovicharis.charts

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import io.github.dautovicharis.charts.model.PieSlice
import io.github.dautovicharis.charts.style.ChartContainerDefaults
import io.github.dautovicharis.charts.style.PieChartDefaults
import io.github.dautovicharis.charts.style.PieChartStyle

private const val PIE_CHART_TITLE = "Pie Chart"
private val PIE_VALUES = listOf(32f, 21f, 24f, 14f, 9f)
private val PIE_LABELS = listOf("North", "East", "South", "West", "Other")
private val PIE_SLICES =
    PIE_LABELS.mapIndexed { index, label -> PieSlice(label = label, value = PIE_VALUES[index]) }

@Composable
private fun PieChartPreviewContent() {
    val style: PieChartStyle =
        PieChartDefaults.style(
            chartContainerStyle =
                ChartContainerDefaults.style(
                    backgroundColor = androidx.compose.material3.MaterialTheme.colorScheme.surface,
                    cornerRadius = 20.dp,
                    shadow = 15.dp,
                    innerPadding = 15.dp,
                ),
        )
    PieChart(
        data = PIE_SLICES,
        style = style,
        title = PIE_CHART_TITLE,
    )
}

@ChartsPreviewLightDark
@Composable
private fun PieChartPreview() {
    ChartsPreviewTheme {
        PieChartPreviewContent()
    }
}

@ChartsPreviewLightDark
@Composable
private fun PieChartErrorPreview() {
    ChartsPreviewTheme {
        PieChart(
            data = listOf(PieSlice(label = "Slice 1", value = 42f)),
            style = PieChartDefaults.style(),
            title = PIE_CHART_TITLE,
        )
    }
}
