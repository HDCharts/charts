package io.github.hdcharts.stackedarea.internal

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Density
import io.github.hdcharts.core.internal.AxisInputs
import io.github.hdcharts.core.internal.ChartEntry
import io.github.hdcharts.core.internal.ChartPolicy
import io.github.hdcharts.core.internal.ChartSpec
import io.github.hdcharts.core.internal.InternalChartsApi
import io.github.hdcharts.core.internal.SeriesRule
import io.github.hdcharts.core.internal.ValidationErrors
import io.github.hdcharts.core.internal.selectedLegendValues
import io.github.hdcharts.core.internal.selectedTitle
import io.github.hdcharts.core.internal.validationValues
import io.github.hdcharts.core.model.ChartData
import io.github.hdcharts.core.model.ChartValueFormatter
import io.github.hdcharts.stackedarea.StackedAreaChartStyle
import io.github.hdcharts.stackedarea.clamp
import kotlinx.collections.immutable.toImmutableList

/**
 * Everything [io.github.hdcharts.stackedarea.StackedAreaChart] declares about its input. Stacked
 * area stacks series, so it forbids negative values, has Cartesian axes, and has no fixed range.
 */
@InternalChartsApi
object StackedAreaChartSpec : ChartSpec<StackedAreaChartStyle> {
    override val policy =
        ChartPolicy<StackedAreaChartStyle>(
            series =
                SeriesRule.Multiple(
                    minValues = ValidationErrors.MIN_VALUES,
                    allowNegative = false,
                    stacked = true,
                    expectedColors = { data -> data.series.size },
                ),
            colorCount = { it.fill.colors.size },
            range = null,
            axes = {
                AxisInputs(
                    xLabels = it.axes.xLabels.validationValues(),
                    yLabels = it.axes.yLabels.validationValues(),
                )
            },
        )

    override fun clamp(
        style: StackedAreaChartStyle,
        density: Density,
    ): StackedAreaChartStyle = style.clamp(density)
}

/**
 * Runs [StackedAreaChartSpec] through the shared seam, then draws the render model with
 * [StackedAreaChartImpl] inside [StackedAreaChartFrame]. The selection index is resolved by the
 * caller, because it reads the public data rather than the render model.
 */
@Composable
internal fun StackedAreaChartEntry(
    data: ChartData,
    modifier: Modifier,
    style: StackedAreaChartStyle,
    title: String?,
    selectedIndex: Int,
    interactionEnabled: Boolean,
    animateOnStart: Boolean,
    selectedValueFormatter: ChartValueFormatter,
    axisValueFormatter: ChartValueFormatter,
    onValueChanged: (Int) -> Unit,
    subtitle: String?,
) {
    ChartEntry(
        spec = StackedAreaChartSpec,
        data = data,
        style = style,
        errorStyle = style.chartContainerStyle,
        title = title,
        content = { renderData, drawStyle ->
            val colors =
                remember(drawStyle.fill, data.series.size) {
                    drawStyle.fill
                        .resolveColors(data.series.size)
                        .map { it.copy(alpha = drawStyle.fill.alpha) }
                        .toImmutableList()
                }
            val seriesNames = data.series.map { it.name.orEmpty() }.toImmutableList()
            val effectiveTitle =
                selectedTitle(
                    data = data,
                    selectedIndex = selectedIndex,
                    title = title,
                    selectedValueFormatter = selectedValueFormatter,
                )
            val selectedLabels =
                selectedLegendValues(
                    data = data,
                    selectedIndex = selectedIndex,
                    selectedValueFormatter = selectedValueFormatter,
                )
            StackedAreaChartFrame(
                style = drawStyle,
                colors = colors,
                seriesNames = seriesNames,
                selectedLabels = selectedLabels,
                modifier = modifier,
            ) {
                StackedAreaChartImpl(
                    subtitle = subtitle,
                    data = renderData,
                    title = effectiveTitle,
                    style = drawStyle,
                    areaColors = colors,
                    interactionEnabled = interactionEnabled,
                    animateOnStart = animateOnStart,
                    axisValueFormatter = axisValueFormatter,
                    selectedPointIndex = selectedIndex,
                    onValueChanged = onValueChanged,
                )
            }
        },
        modifier = modifier,
    )
}
