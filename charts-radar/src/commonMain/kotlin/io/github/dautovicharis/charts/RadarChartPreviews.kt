package io.github.dautovicharis.charts

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import io.github.dautovicharis.charts.model.ChartSeries
import io.github.dautovicharis.charts.model.chartDataOf
import io.github.dautovicharis.charts.style.RadarChartDefaults

private const val RADAR_CHART_TITLE = "Radar Chart"

private val RADAR_CATEGORIES =
    listOf("Speed", "Strength", "Agility", "Stamina", "Skill", "Luck")

private val RADAR_MULTI_VALUES =
    listOf(
        ChartSeries(
            name = "Falcon",
            values = listOf(78.0, 62.0, 90.0, 55.0, 70.0, 80.0),
        ),
        ChartSeries(
            name = "Tiger",
            values = listOf(65.0, 88.0, 60.0, 82.0, 55.0, 68.0),
        ),
    )

private val RADAR_SINGLE_VALUES =
    listOf(
        ChartSeries(
            name = "Falcon",
            values = listOf(78.0, 62.0, 90.0, 55.0, 70.0, 80.0),
        ),
    )

@Composable
private fun RadarChartMultiSeriesPreviewContent(
    categoryLegendVisible: Boolean = true,
    categoryPinsVisible: Boolean = true,
) {
    val style =
        RadarChartDefaults.style(
            polygon =
                RadarChartDefaults.polygon(
                    lineColors =
                        listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.tertiary,
                        ),
                ),
            categories =
                RadarChartDefaults.categories(
                    legendVisible = categoryLegendVisible,
                    pinsVisible = categoryPinsVisible,
                ),
        )

    RadarChart(
        data =
            chartDataOf(
                categories = RADAR_CATEGORIES,
                *RADAR_MULTI_VALUES.toTypedArray(),
            ),
        title = RADAR_CHART_TITLE,
        style = style,
    )
}

@Composable
private fun RadarChartSingleSeriesPreviewContent() {
    RadarChart(
        data =
            chartDataOf(
                categories = RADAR_CATEGORIES,
                *RADAR_SINGLE_VALUES.toTypedArray(),
            ),
        title = RADAR_CHART_TITLE,
        style =
            RadarChartDefaults.style(
                polygon =
                    RadarChartDefaults.polygon(lineColors = listOf(MaterialTheme.colorScheme.primary)),
            ),
    )
}

@ChartsPreviewLightDark
@Composable
private fun RadarChartMultiPreview() {
    ChartsPreviewTheme {
        RadarChartMultiSeriesPreviewContent()
    }
}

@ChartsPreviewLightDark
@Composable
private fun RadarChartSinglePreview() {
    ChartsPreviewTheme {
        RadarChartSingleSeriesPreviewContent()
    }
}

@ChartsPreviewLightDark
@Composable
private fun RadarChartHiddenLegendPreview() {
    ChartsPreviewTheme {
        RadarChartMultiSeriesPreviewContent(
            categoryLegendVisible = false,
            categoryPinsVisible = false,
        )
    }
}

@ChartsPreviewLightDark
@Composable
private fun RadarChartErrorPreview() {
    ChartsPreviewTheme {
        RadarChart(
            data =
                chartDataOf(
                    categories = listOf("A", "B"),
                    ChartSeries(name = "Series", values = listOf(10.0, 12.0)),
                ),
            title = RADAR_CHART_TITLE,
            style = RadarChartDefaults.style(),
        )
    }
}
