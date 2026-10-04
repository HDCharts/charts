package io.github.hdcharts.stackedbar.internal

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Density
import io.github.hdcharts.core.internal.ChartEntry
import io.github.hdcharts.core.internal.ChartPolicy
import io.github.hdcharts.core.internal.ChartSpec
import io.github.hdcharts.core.internal.ChartValidationInputs
import io.github.hdcharts.core.internal.InternalChartsApi
import io.github.hdcharts.core.internal.NO_SELECTION
import io.github.hdcharts.core.internal.ValidationErrors
import io.github.hdcharts.core.internal.model.ChartRenderData
import io.github.hdcharts.core.internal.model.transposeForStacking
import io.github.hdcharts.core.model.ChartData
import io.github.hdcharts.core.model.ChartValueFormatters
import io.github.hdcharts.stackedbar.StackedBarChartStyle
import io.github.hdcharts.stackedbar.clamp
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList

/**
 * Everything [io.github.hdcharts.stackedbar.StackedBarChart] declares about its input. Stacked bar
 * stacks segments, so it forbids negative values, has Cartesian axes, and has no fixed range.
 */
@InternalChartsApi
object StackedBarChartSpec : ChartSpec<StackedBarChartStyle> {
    override val policy =
        ChartPolicy(
            minValues = ValidationErrors.MIN_VALUES,
            allowNegative = false,
            singleSeries = false,
            hasAxis = true,
            hasFixedRange = false,
            colorsMatch = { data -> data.series.size },
        )

    override fun validationInputs(style: StackedBarChartStyle): ChartValidationInputs =
        ChartValidationInputs(
            colorCount = style.segments.colors.size,
            rangeMin = null,
            rangeMax = null,
            xLabels = style.axis.xLabels,
            yLabels = style.axis.yLabels,
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
    onValueChanged: (Int) -> Unit,
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
                        .map { it.copy(alpha = drawStyle.segments.alpha) }
                        .toImmutableList()
                }
            val segmentNames = data.series.map { it.name.orEmpty() }.toImmutableList()
            // A selected category names the chart while it is selected; otherwise the caller's title does.
            val effectiveTitle = data.categories.getOrNull(selectedIndex) ?: title.orEmpty()
            val selectedLabels =
                if (selectedIndex == NO_SELECTION) {
                    persistentListOf()
                } else {
                    data.series
                        .map { ChartValueFormatters.Default.format(it.values[selectedIndex]) }
                        .toImmutableList()
                }
            StackedBarChartFrame(
                style = drawStyle,
                colors = colors,
                segmentNames = segmentNames,
                selectedLabels = selectedLabels,
                showLegend = data.categories.isNotEmpty() && segmentNames.any { it.isNotBlank() },
                modifier = modifier,
            ) {
                StackedBarChartImpl(
                    data = renderData,
                    title = effectiveTitle,
                    style = drawStyle,
                    colors = colors,
                    showXAxisLabels = drawStyle.axis.xLabels.visible && data.categories.any { it.isNotBlank() },
                    interactionEnabled = interactionEnabled,
                    animateOnStart = animateOnStart,
                    selectedBarIndex = selectedIndex,
                    onValueChanged = onValueChanged,
                )
            }
        },
        modifier = modifier,
    )
}
