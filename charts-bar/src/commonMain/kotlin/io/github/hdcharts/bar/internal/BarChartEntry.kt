package io.github.hdcharts.bar.internal

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Density
import io.github.hdcharts.core.internal.ChartEntry
import io.github.hdcharts.core.internal.ChartPolicy
import io.github.hdcharts.core.internal.ChartSpec
import io.github.hdcharts.core.internal.ChartValidationInputs
import io.github.hdcharts.core.internal.InternalChartsApi
import io.github.hdcharts.core.internal.ValidationErrors
import io.github.hdcharts.core.model.ChartData
import io.github.hdcharts.core.model.ChartSelection
import io.github.hdcharts.core.model.ChartValueFormatter
import io.github.hdcharts.core.style.BarChartStyle
import io.github.hdcharts.core.style.clamp
import io.github.hdcharts.core.internal.model.ChartData as InternalChartData

/**
 * Everything [io.github.hdcharts.bar.BarChart] declares about its input. Bar draws one series as
 * bars, so it needs exactly one series, allows negative values, has Cartesian axes and an optional
 * fixed range, and its colors match its value count.
 *
 * Histogram draws the same plot from the same render model, so it shares [toBarRenderData] and
 * declares its own spec next to its own composable.
 */
@InternalChartsApi
object BarChartSpec : ChartSpec<BarChartStyle, InternalChartData> {
    override val policy =
        ChartPolicy(
            minValues = ValidationErrors.MIN_VALUES,
            allowNegative = true,
            singleSeries = true,
            hasAxis = true,
            hasFixedRange = true,
            colorsMatch = { data ->
                data.series
                    .firstOrNull()
                    ?.values
                    ?.size
            },
        )

    override fun validationInputs(style: BarChartStyle): ChartValidationInputs =
        ChartValidationInputs(
            colorCount = style.bars.colors.size,
            rangeMin = style.range.min,
            rangeMax = style.range.max,
            xLabels = style.axis.xLabels,
            yLabels = style.axis.yLabels,
        )

    override fun clamp(
        style: BarChartStyle,
        density: Density,
    ): BarChartStyle = style.clamp(density)

    override fun convert(
        data: ChartData,
        title: String?,
    ): InternalChartData = toBarRenderData(data = data)
}

/**
 * The render model bar and histogram share: one series of bars, each carrying its category label.
 * Validation has already run, so the single series and the label count are known good.
 */
@InternalChartsApi
fun toBarRenderData(data: ChartData) =
    InternalChartData(
        labels = data.categories,
        points = data.series.single().values,
    )

/**
 * Runs [BarChartSpec] through the shared seam, then hands the render model and the clamped style to
 * [BarChartInternalPlot]. The selection index is resolved by the caller, because it reads the public
 * data rather than the render model.
 */
@Composable
internal fun BarChartEntry(
    data: ChartData,
    modifier: Modifier,
    style: BarChartStyle,
    title: String?,
    selection: ChartSelection,
    selectedIndex: Int,
    interactionEnabled: Boolean,
    animateOnStart: Boolean,
    valueFormatter: ChartValueFormatter,
    axisValueFormatter: ChartValueFormatter,
) {
    ChartEntry(
        spec = BarChartSpec,
        data = data,
        style = style,
        errorStyle = style.chartContainerStyle,
        title = title,
        content = { renderData, drawStyle ->
            BarChartInternalPlot(
                data = renderData,
                title = title,
                style = drawStyle,
                selection = selection,
                selectedIndex = selectedIndex,
                interactionEnabled = interactionEnabled,
                animateOnStart = animateOnStart,
                aggregate = true,
                valueFormatter = valueFormatter,
                axisValueFormatter = axisValueFormatter,
                modifier = modifier,
            )
        },
        modifier = modifier,
    )
}
