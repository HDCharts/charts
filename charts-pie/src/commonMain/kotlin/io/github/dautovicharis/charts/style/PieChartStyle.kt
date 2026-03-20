package io.github.dautovicharis.charts.style

import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import io.github.dautovicharis.charts.internal.DONUT_MAX_PERCENTAGE
import io.github.dautovicharis.charts.internal.DONUT_MIN_PERCENTAGE

/**
 * A class that defines the style for a Pie Chart.
 *
 * @property modifier The modifier to be applied to the chart.
 * @property chartViewStyle The style to be applied to the chart view.
 * @property donutPercentage The percentage of the chart that should be a donut hole. Must be between DONUT_MIN_PERCENTAGE and DONUT_MAX_PERCENTAGE.
 * @property pieColors The colors to be used for the slices in the pie chart.
 * @property pieColor The color to be used for the pie chart if pieColors is empty.
 * @property pieAlpha The alpha value applied to rendered pie slices.
 * @property borderColor The color of the border around the pie chart.
 * @property borderWidth The width of the border around the pie chart.
 * @property legendVisible A boolean indicating whether the legend is visible.
 */
@Immutable
class PieChartStyle(
    val modifier: Modifier,
    val chartViewStyle: ChartViewStyle,
    val donutPercentage: Float,
    var pieColors: List<Color>,
    val pieColor: Color,
    val pieAlpha: Float,
    val borderColor: Color,
    val borderWidth: Float,
    val legendVisible: Boolean,
) : Style {
    /**
     * Returns a list of the properties of the PieChartStyle.
     */
    override fun getProperties(): List<Pair<String, Any>> =
        listOf(
            PieChartStyle::donutPercentage.name to donutPercentage,
            PieChartStyle::pieColors.name to pieColors,
            PieChartStyle::pieColor.name to pieColor,
            PieChartStyle::pieAlpha.name to pieAlpha,
            PieChartStyle::borderColor.name to borderColor,
            PieChartStyle::borderWidth.name to borderWidth,
            PieChartStyle::legendVisible.name to legendVisible,
        )
}

/**
 * An object that provides default styles for a Pie Chart.
 */
object PieChartDefaults {
    /**
     * Returns a PieChartStyle with the provided parameters or their default values.
     *
     * @param pieColor The color to be used for the pie chart if pieColors is empty. Defaults to the primary color of the MaterialTheme.
     * @param pieColors The colors to be used for the slices in the pie chart. Defaults to an empty list.
     * @param pieAlpha The alpha value applied to rendered pie slices. Defaults to 0.4f in light theme and 0.6f in dark theme.
     * @param borderColor The color of the border around the pie chart. Defaults to the surface color of the MaterialTheme.
     * @param donutPercentage The percentage of the chart that should be a donut hole. Defaults to 0f.
     * @param borderWidth The width of the border around the pie chart. Defaults to 3f.
     * @param legendVisible A boolean indicating whether the legend is visible. Defaults to true.
     * @param chartViewStyle The style to be applied to the chart view. Defaults to the default style of ChartViewDefaults.
     */
    @Composable
    fun style(
        pieColor: Color = MaterialTheme.colorScheme.primary,
        pieColors: List<Color> = emptyList(),
        pieAlpha: Float = defaultChartAlpha(),
        borderColor: Color = MaterialTheme.colorScheme.surface,
        donutPercentage: Float = 0f,
        borderWidth: Float = 3f,
        legendVisible: Boolean = true,
        chartViewStyle: ChartViewStyle = ChartViewDefaults.style(),
    ): PieChartStyle =
        PieChartStyle(
            modifier =
                Modifier
                    .wrapContentSize()
                    .padding(chartViewStyle.innerPadding)
                    .aspectRatio(1f),
            donutPercentage =
                donutPercentage.coerceIn(
                    DONUT_MIN_PERCENTAGE,
                    DONUT_MAX_PERCENTAGE,
                ),
            pieColors = pieColors,
            pieColor = pieColor,
            pieAlpha = pieAlpha.coerceIn(0f, 1f),
            borderColor = borderColor,
            borderWidth = borderWidth,
            legendVisible = legendVisible,
            chartViewStyle = chartViewStyle,
        )
}
