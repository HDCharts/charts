package io.github.hdcharts.gauge.internal

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Density
import io.github.hdcharts.core.internal.ChartEntry
import io.github.hdcharts.core.internal.ChartPolicy
import io.github.hdcharts.core.internal.ChartSpec
import io.github.hdcharts.core.internal.InternalChartsApi
import io.github.hdcharts.core.internal.NO_SELECTION
import io.github.hdcharts.core.internal.SeriesRule
import io.github.hdcharts.core.internal.ValidationErrors
import io.github.hdcharts.core.internal.composable.ChartHeading
import io.github.hdcharts.core.internal.composable.ChartPlotLayout
import io.github.hdcharts.core.internal.composable.Legend
import io.github.hdcharts.core.internal.layout.modifierTopTitle
import io.github.hdcharts.core.internal.selectedTitle
import io.github.hdcharts.core.model.ChartData
import io.github.hdcharts.core.model.ChartSelection
import io.github.hdcharts.core.model.ChartValueFormatter
import io.github.hdcharts.gauge.SegmentedRingGaugeChartStyle
import io.github.hdcharts.gauge.SegmentedRingGaugeSegmentsStyle
import io.github.hdcharts.gauge.clamp
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

@InternalChartsApi
object SegmentedRingGaugeChartSpec : ChartSpec<SegmentedRingGaugeChartStyle> {
    override val policy =
        ChartPolicy<SegmentedRingGaugeChartStyle>(
            series =
                SeriesRule.Single(
                    minValues = ValidationErrors.MIN_RING_GAUGE_VALUES,
                    allowNegative = false,
                ),
            colorCount = { it.segments.colors.size },
            range = null,
            axes = null,
        )

    override fun clamp(
        style: SegmentedRingGaugeChartStyle,
        density: Density,
    ) = style.clamp(density)
}

@Composable
internal fun SegmentedRingGaugeChartEntry(
    data: ChartData,
    capacity: Double,
    modifier: Modifier,
    style: SegmentedRingGaugeChartStyle,
    title: String?,
    centerLabel: String?,
    centerSupportingLabel: String?,
    centerValueFormatter: ChartValueFormatter,
    selectedValueFormatter: ChartValueFormatter,
    selectedIndex: Int,
    selection: ChartSelection,
    interactionEnabled: Boolean,
    animateOnStart: Boolean,
    subtitle: String?,
    onSelectionInteraction: () -> Unit,
) {
    val capacityErrors = remember(data, capacity) { segmentedGaugeCapacityErrors(data = data, capacity = capacity) }
    ChartEntry(
        spec = SegmentedRingGaugeChartSpec,
        data = data,
        style = style,
        errorStyle = style.chartContainerStyle,
        modifier = modifier,
        title = title,
        additionalErrors = capacityErrors,
        content = { renderData, drawStyle ->
            val values = renderData.series.single().values
            val colors =
                remember(drawStyle.segments, values.size) { resolveSegmentColors(drawStyle.segments, values.size) }
            val total = values.sum()
            val displayedTitle =
                selectedTitle(
                    data = renderData.data,
                    selectedIndex = selectedIndex,
                    title = title,
                    selectedValueFormatter = selectedValueFormatter,
                )
            ChartPlotLayout(
                plotAspectRatio = 1f,
                modifier = modifier,
                title = {
                    ChartHeading(
                        title = displayedTitle,
                        titleTextStyle = drawStyle.chartContainerStyle.styleTitle,
                        subtitle = subtitle,
                        subtitleTextStyle = drawStyle.chartContainerStyle.styleSubtitle,
                        modifier = drawStyle.chartContainerStyle.modifierTopTitle,
                    )
                },
                legend = {
                    Legend(
                        chartContainerStyle = drawStyle.chartContainerStyle,
                        style = drawStyle.legend,
                        legend = renderData.categories,
                        colors = colors,
                    )
                },
                plot = {
                    SegmentedRingGaugeChartContent(
                        values = values,
                        capacity = capacity,
                        colors = colors,
                        style = drawStyle,
                        centerLabel = centerLabel ?: centerValueFormatter.format(total),
                        centerSupportingLabel = centerSupportingLabel,
                        interactionEnabled = interactionEnabled,
                        animateOnStart = animateOnStart,
                        selectedIndex = selectedIndex,
                        onSegmentTouched = { index ->
                            if (index ==
                                NO_SELECTION
                            ) {
                                selection.clear()
                            } else {
                                selection.select(index)
                                onSelectionInteraction()
                            }
                        },
                    )
                },
            )
        },
    )
}

private fun segmentedGaugeCapacityErrors(
    data: ChartData,
    capacity: Double,
): List<String> {
    if (!capacity.isFinite()) return listOf(ValidationErrors.nonFiniteRange())
    if (capacity <= 0.0) return listOf("Capacity must be greater than zero.")
    val values = data.series.singleOrNull()?.values ?: return emptyList()
    if (values.any { !it.isFinite() }) return emptyList()
    val total = values.sum()
    return when {
        !total.isFinite() -> listOf("Segment total must be finite.")
        totalExceedsCapacity(total = total, capacity = capacity) ->
            listOf("Segment total ($total) must not exceed capacity ($capacity).")
        else -> emptyList()
    }
}

private fun resolveSegmentColors(
    style: SegmentedRingGaugeSegmentsStyle,
    segmentCount: Int,
): ImmutableList<Color> =
    style.resolveColors(segmentCount).map { it.copy(alpha = it.alpha * style.alpha) }.toImmutableList()
