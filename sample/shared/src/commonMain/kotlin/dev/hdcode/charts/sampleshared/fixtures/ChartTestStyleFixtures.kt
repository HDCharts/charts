package dev.hdcode.charts.sampleshared.fixtures

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import dev.hdcode.charts.sampleshared.theme.LocalChartColors
import dev.hdcode.charts.sampleshared.theme.seriesColor
import dev.hdcode.charts.sampleshared.theme.seriesColors
import io.github.dautovicharis.charts.model.PieSlice
import io.github.dautovicharis.charts.style.BarChartDefaults
import io.github.dautovicharis.charts.style.BarChartStyle
import io.github.dautovicharis.charts.style.ChartContainerStyle
import io.github.dautovicharis.charts.style.HistogramChartDefaults
import io.github.dautovicharis.charts.style.HistogramChartStyle
import io.github.dautovicharis.charts.style.LineChartDefaults
import io.github.dautovicharis.charts.style.LineChartStyle
import io.github.dautovicharis.charts.style.PieChartDefaults
import io.github.dautovicharis.charts.style.PieChartStyle
import io.github.dautovicharis.charts.style.RadarChartDefaults
import io.github.dautovicharis.charts.style.RadarChartStyle
import io.github.dautovicharis.charts.style.StackedAreaChartDefaults
import io.github.dautovicharis.charts.style.StackedAreaChartStyle
import io.github.dautovicharis.charts.style.StackedBarChartDefaults
import io.github.dautovicharis.charts.style.StackedBarChartStyle

/**
 * Shared custom style fixtures used by:
 * - `sample/app/src/commonMain/kotlin/dev/hdcode/charts/app/demo/...`
 * - `sample/androidApp/src/screenshotTest/kotlin/.../ChartScreenshotTest.kt`
 */
object ChartTestStyleFixtures {
    @Composable
    fun pieCustomStyle(chartContainerStyle: ChartContainerStyle): PieChartStyle =
        PieChartDefaults.style(
            chartContainerStyle = chartContainerStyle,
            donut = PieChartDefaults.donut(holePercentage = 40f),
            border =
                PieChartDefaults.border(
                    color = MaterialTheme.colorScheme.surface,
                    width = 5f,
                ),
            legend = PieChartDefaults.legend(visible = true),
        )

    /**
     * Applies the shared chart palette onto the custom pie slices so the preview renders
     * the same colors as before, with the color living on the [PieSlice] data rather than
     * the style object.
     */
    @Composable
    fun pieCustomSlices(slices: List<PieSlice>): List<PieSlice> {
        val chartColors = LocalChartColors.current
        val palette = chartColors.seriesColors(slices.size)
        return slices.mapIndexed { index, slice ->
            slice.copy(color = palette.getOrElse(index) { slice.color ?: palette.first() })
        }
    }

    @Composable
    fun lineCustomStyle(chartContainerStyle: ChartContainerStyle): LineChartStyle {
        val chartColors = LocalChartColors.current
        return LineChartDefaults.style(
            chartContainerStyle = chartContainerStyle,
            lineColor = chartColors.seriesColor(1),
            pointSize = 9f,
            bezier = false,
            dragPointVisible = true,
            pointVisible = true,
            dragPointColor = chartColors.selection,
            dragPointSize = 8f,
            dragActivePointSize = 10f,
        )
    }

    @Composable
    fun multiLineCustomStyle(
        chartContainerStyle: ChartContainerStyle,
        seriesCount: Int,
    ): LineChartStyle {
        val chartColors = LocalChartColors.current
        return LineChartDefaults.style(
            chartContainerStyle = chartContainerStyle,
            lineColors = chartColors.seriesColors(seriesCount),
            bezier = false,
            pointVisible = true,
            dragPointVisible = false,
            pointColor = chartColors.highlight,
            dragPointColor = chartColors.selection,
        )
    }

    @Composable
    fun barCustomStyle(
        chartContainerStyle: ChartContainerStyle,
        barCount: Int = 1,
        useBarColors: Boolean = false,
        minValue: Float? = null,
        maxValue: Float? = null,
    ): BarChartStyle {
        val chartColors = LocalChartColors.current
        val barColors =
            if (useBarColors) {
                chartColors.seriesColors(barCount.coerceAtLeast(1))
            } else {
                emptyList()
            }
        return BarChartDefaults.style(
            chartContainerStyle = chartContainerStyle,
            barColor = chartColors.seriesColor(4),
            barColors = barColors,
            minValue = minValue,
            maxValue = maxValue,
            gridColor = chartColors.gridLine,
            axisColor = chartColors.axisLine,
            xAxisLabelColor = chartColors.axisLabel,
            selectionLineVisible = true,
            selectionLineColor = chartColors.selection,
            selectionLineWidth = 2f,
        )
    }

    @Composable
    fun histogramCustomStyle(
        chartContainerStyle: ChartContainerStyle,
        barCount: Int = 1,
        useBarColors: Boolean = false,
        minValue: Float? = 0f,
        maxValue: Float? = null,
    ): HistogramChartStyle {
        val chartColors = LocalChartColors.current
        val barColors =
            if (useBarColors) {
                chartColors.seriesColors(barCount.coerceAtLeast(1))
            } else {
                emptyList()
            }
        return HistogramChartDefaults.style(
            chartContainerStyle = chartContainerStyle,
            barColor = chartColors.seriesColor(4),
            barColors = barColors,
            minValue = minValue,
            maxValue = maxValue,
            gridColor = chartColors.gridLine,
            axisColor = chartColors.axisLine,
            xAxisLabelColor = chartColors.axisLabel,
            selectionLineVisible = true,
            selectionLineColor = chartColors.selection,
            selectionLineWidth = 2f,
        )
    }

    @Composable
    fun stackedBarCustomStyle(
        chartContainerStyle: ChartContainerStyle,
        segmentCount: Int,
    ): StackedBarChartStyle {
        val chartColors = LocalChartColors.current
        return StackedBarChartDefaults.style(
            chartContainerStyle = chartContainerStyle,
            barColors = chartColors.seriesColors(segmentCount),
            space = 8.dp,
            zoomControlsVisible = true,
            xAxisLabelColor = chartColors.axisLabel,
            yAxisLabelColor = chartColors.axisLabel,
            selectionLineVisible = true,
            selectionLineColor = chartColors.selection,
            selectionLineWidth = 2f,
        )
    }

    @Composable
    fun stackedAreaCustomStyle(
        chartContainerStyle: ChartContainerStyle,
        seriesCount: Int,
    ): StackedAreaChartStyle {
        val chartColors = LocalChartColors.current
        val colors = chartColors.seriesColors(seriesCount)
        return StackedAreaChartDefaults.style(
            chartContainerStyle = chartContainerStyle,
            areaColors = colors,
            lineColors = colors,
            fillAlpha = 0.3f,
            lineVisible = true,
            lineWidth = 3.5f,
            bezier = false,
            zoomControlsVisible = true,
            xAxisLabelColor = chartColors.axisLabel,
            yAxisLabelColor = chartColors.axisLabel,
        )
    }

    @Composable
    fun radarCustomStyle(
        chartContainerStyle: ChartContainerStyle,
        seriesKeys: List<String>,
    ): RadarChartStyle {
        val chartColors = LocalChartColors.current
        return RadarChartDefaults.style(
            chartContainerStyle = chartContainerStyle,
            lineColors = chartColors.seriesColors(seriesKeys),
            lineWidth = 3.5f,
            pointColor = chartColors.highlight,
            pointSize = 5f,
            gridSteps = 6,
            gridLineWidth = 1.4f,
            axisLineColor = chartColors.axisLine,
            axisLineWidth = 1.2f,
            axisLabelColor = chartColors.axisLabel,
            fillAlpha = 0.2f,
            categoryLegendVisible = false,
        )
    }
}
