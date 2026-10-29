package io.github.hdcharts.histogram.internal

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Density
import io.github.hdcharts.bar.internal.BarChartInternalPlot
import io.github.hdcharts.core.internal.AxisInputs
import io.github.hdcharts.core.internal.ChartEntry
import io.github.hdcharts.core.internal.ChartPolicy
import io.github.hdcharts.core.internal.ChartSpec
import io.github.hdcharts.core.internal.ChartValidationInputs
import io.github.hdcharts.core.internal.InternalChartsApi
import io.github.hdcharts.core.internal.RangeInputs
import io.github.hdcharts.core.internal.SeriesRule
import io.github.hdcharts.core.internal.TestTags
import io.github.hdcharts.core.internal.ValidationErrors
import io.github.hdcharts.core.model.ChartData
import io.github.hdcharts.core.model.ChartSelection
import io.github.hdcharts.core.model.ChartValueFormatter
import io.github.hdcharts.core.style.BarChartStyle
import io.github.hdcharts.core.style.clamp

/**
 * Everything [io.github.hdcharts.histogram.HistogramChart] declares about its input. It draws the
 * same plot as bar from the same render model, including compact mode, and differs only in that a
 * bin height cannot be negative.
 */
@InternalChartsApi
object HistogramChartSpec : ChartSpec<BarChartStyle> {
    override val policy =
        ChartPolicy(
            series =
                SeriesRule.Single(
                    minValues = ValidationErrors.MIN_VALUES,
                    allowNegative = false,
                ),
            hasAxis = true,
            hasFixedRange = true,
        )

    override fun validationInputs(style: BarChartStyle): ChartValidationInputs =
        ChartValidationInputs(
            colorCount = style.bars.colors.size,
            range = RangeInputs(min = style.range.min, max = style.range.max),
            axes = AxisInputs(xLabels = style.axes.xLabels, yLabels = style.axes.yLabels),
        )

    override fun clamp(
        style: BarChartStyle,
        density: Density,
    ): BarChartStyle = style.clamp(density)
}

/**
 * Runs [HistogramChartSpec] through the shared seam, then hands the render model to bar's shared
 * plot. The selection index is resolved by the caller, because it reads the public data rather than
 * the render model.
 */
@Composable
internal fun HistogramChartEntry(
    data: ChartData,
    modifier: Modifier,
    style: BarChartStyle,
    title: String?,
    selection: ChartSelection,
    selectedIndex: Int,
    interactionEnabled: Boolean,
    animateOnStart: Boolean,
    selectedValueFormatter: ChartValueFormatter,
    axisValueFormatter: ChartValueFormatter,
    subtitle: String?,
) {
    ChartEntry(
        spec = HistogramChartSpec,
        data = data,
        style = style,
        errorStyle = style.chartContainerStyle,
        title = title,
        content = { renderData, drawStyle ->
            BarChartInternalPlot(
                subtitle = subtitle,
                data = renderData,
                style = drawStyle,
                selection = selection,
                selectedIndex = selectedIndex,
                interactionEnabled = interactionEnabled,
                animateOnStart = animateOnStart,
                selectedValueFormatter = selectedValueFormatter,
                axisValueFormatter = axisValueFormatter,
                modifier = modifier,
                chartTag = TestTags.HISTOGRAM_CHART,
            )
        },
        modifier = modifier,
    )
}
