package io.github.hdcharts.line.internal

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Density
import io.github.hdcharts.core.internal.AxisInputs
import io.github.hdcharts.core.internal.ChartEntry
import io.github.hdcharts.core.internal.ChartPolicy
import io.github.hdcharts.core.internal.ChartSpec
import io.github.hdcharts.core.internal.ChartValidationInputs
import io.github.hdcharts.core.internal.InternalChartsApi
import io.github.hdcharts.core.internal.RangeInputs
import io.github.hdcharts.core.internal.SeriesRule
import io.github.hdcharts.core.internal.ValidationErrors
import io.github.hdcharts.core.internal.model.ChartRenderData
import io.github.hdcharts.core.model.ChartData
import io.github.hdcharts.line.LineChartStyle
import io.github.hdcharts.line.clamp

/**
 * Everything both line charts declare about their input. Line takes any number of aligned series,
 * negative values, Cartesian axes, and an optional fixed range; its colors match the series count.
 */
@InternalChartsApi
object LineChartSpec : ChartSpec<LineChartStyle> {
    override val policy =
        ChartPolicy(
            series =
                SeriesRule.Multiple(
                    minValues = ValidationErrors.MIN_VALUES,
                    allowNegative = true,
                    stacked = false,
                    expectedColors = { data -> data.series.size },
                ),
            hasAxis = true,
            hasFixedRange = true,
        )

    override fun validationInputs(style: LineChartStyle) =
        ChartValidationInputs(
            colorCount = style.line.colors.size,
            range = RangeInputs(min = style.range.min, max = style.range.max),
            axes = AxisInputs(xLabels = style.axes.xLabels, yLabels = style.axes.yLabels),
        )

    override fun clamp(
        style: LineChartStyle,
        density: Density,
    ) = style.clamp(density)
}

/**
 * Runs [LineChartSpec] through the shared seam. [io.github.hdcharts.line.LineChart] and
 * [io.github.hdcharts.line.LiveLineChart] share this call and differ only in what they draw below it.
 */
@Composable
internal fun LineChartEntry(
    data: ChartData,
    modifier: Modifier,
    style: LineChartStyle,
    title: String?,
    content: @Composable (data: ChartRenderData, style: LineChartStyle) -> Unit,
) {
    ChartEntry(
        spec = LineChartSpec,
        data = data,
        style = style,
        errorStyle = style.chartContainerStyle,
        content = content,
        modifier = modifier,
        title = title,
    )
}
