package io.github.hdcharts.stackedbar.internal

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Density
import io.github.hdcharts.core.internal.AxisInputs
import io.github.hdcharts.core.internal.ChartEntry
import io.github.hdcharts.core.internal.ChartPolicy
import io.github.hdcharts.core.internal.ChartSpec
import io.github.hdcharts.core.internal.ChartValidationInputs
import io.github.hdcharts.core.internal.InternalChartsApi
import io.github.hdcharts.core.internal.SeriesRule
import io.github.hdcharts.core.internal.ValidationErrors
import io.github.hdcharts.core.internal.model.ChartRenderData
import io.github.hdcharts.core.internal.model.transposeForStacking
import io.github.hdcharts.core.internal.selectedLegendValues
import io.github.hdcharts.core.internal.selectedTitle
import io.github.hdcharts.core.model.ChartData
import io.github.hdcharts.core.model.ChartValueFormatter
import io.github.hdcharts.stackedbar.StackedBarChartStyle
import io.github.hdcharts.stackedbar.clamp
import kotlinx.collections.immutable.toImmutableList

/**
 * Everything [io.github.hdcharts.stackedbar.StackedBarChart] declares about its input. Stacked bar
 * stacks segments, so it forbids negative values, has Cartesian axes, and has no fixed range.
 */
@InternalChartsApi
object StackedBarChartSpec : ChartSpec<StackedBarChartStyle> {
    override val policy =
        ChartPolicy(
            series =
                SeriesRule.Multiple(
                    minValues = ValidationErrors.MIN_VALUES,
                    allowNegative = false,
                    stacked = true,
                    expectedColors = { data -> data.series.size },
                ),
            hasAxis = true,
            hasFixedRange = false,
        )

    override fun validationInputs(style: StackedBarChartStyle): ChartValidationInputs =
        ChartValidationInputs(
            colorCount = style.segments.colors.size,
            range = null,
            axes = AxisInputs(xLabels = style.axes.xLabels, yLabels = style.axes.yLabels),
        )

    override fun clamp(
        style: StackedBarChartStyle,
        density: Density,
    ): StackedBarChartStyle = style.clamp(density)

    /**
     * The one chart whose render model is not the caller's data: it draws one bar per category with
     * the segments stacked inside it, so it transposes to a per-bar model here. Every other chart
     * takes the default and passes the data through.
     */
    override fun convert(
        data: ChartData,
        title: String?,
    ): ChartRenderData = ChartRenderData(data = data, title = title.orEmpty()).transposeForStacking()
}

/**
 * Runs [StackedBarChartSpec] through the shared seam, then draws the render model with
 * [StackedBarChartImpl] inside [StackedBarChartFrame]. The selection index is resolved by the
 * caller, because it reads the public data rather than the render model.
 */
@Composable
internal fun StackedBarChartEntry(
    data: ChartData,
    modifier: Modifier,
    style: StackedBarChartStyle,
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
        spec = StackedBarChartSpec,
        data = data,
        style = style,
        errorStyle = style.chartContainerStyle,
        title = title,
        content = { renderData, drawStyle ->
            val colors =
                remember(drawStyle.segments, data.series.size) {
                    drawStyle.segments
                        .resolveColors(data.series.size)
                        .map { it.copy(alpha = 1f) }
                        .toImmutableList()
                }
            val segmentNames = data.series.map { it.name.orEmpty() }.toImmutableList()
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
            StackedBarChartFrame(
                style = drawStyle,
                colors = colors,
                segmentNames = segmentNames,
                selectedLabels = selectedLabels,
                modifier = modifier,
            ) {
                StackedBarChartImpl(
                    subtitle = subtitle,
                    data = renderData,
                    title = effectiveTitle,
                    style = drawStyle,
                    colors = colors,
                    showXAxisLabels =
                        drawStyle.axes.visible &&
                            drawStyle.axes.xLabels.visible &&
                            data.categories.any { it.isNotBlank() },
                    interactionEnabled = interactionEnabled,
                    animateOnStart = animateOnStart,
                    axisValueFormatter = axisValueFormatter,
                    selectedBarIndex = selectedIndex,
                    onValueChanged = onValueChanged,
                )
            }
        },
        modifier = modifier,
    )
}
