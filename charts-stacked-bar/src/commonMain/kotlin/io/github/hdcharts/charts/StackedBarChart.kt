package io.github.hdcharts.charts

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import io.github.hdcharts.charts.internal.NO_SELECTION
import io.github.hdcharts.charts.internal.barstackedchart.toInternal
import io.github.hdcharts.charts.internal.common.composable.ChartErrors
import io.github.hdcharts.charts.internal.common.composable.Legend
import io.github.hdcharts.charts.internal.common.model.ChartDataItem
import io.github.hdcharts.charts.internal.common.model.MultiChartData
import io.github.hdcharts.charts.internal.validateSizes
import io.github.hdcharts.charts.model.ChartData
import io.github.hdcharts.charts.model.ChartSelection
import io.github.hdcharts.charts.model.ChartValueFormatters
import io.github.hdcharts.charts.model.rememberChartSelection
import io.github.hdcharts.charts.model.rememberSelectionLifecycle
import io.github.hdcharts.charts.style.StackedBarChartDefaults
import io.github.hdcharts.charts.style.StackedBarChartStyle
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import io.github.hdcharts.charts.internal.barstackedchart.StackedBarChart as StackedBarChartInternal
import io.github.hdcharts.charts.internal.common.model.ChartData as InternalChartData

/** Displays nonnegative absolute stacks from one aligned series per segment. */
@Composable
fun StackedBarChart(
    data: ChartData,
    modifier: Modifier = Modifier,
    style: StackedBarChartStyle = StackedBarChartDefaults.style(),
    title: String? = null,
    selection: ChartSelection = rememberChartSelection(),
    interactionEnabled: Boolean = true,
    animateOnStart: Boolean = true,
) {
    val density = LocalDensity.current
    val errors = remember(data, style, density) { validateStackedBarInput(data, style, density) }
    val pointCount =
        data.series
            .firstOrNull()
            ?.values
            ?.size ?: 0
    val selectedIndex = selection.selectedIndex?.takeIf { it in 0 until pointCount } ?: NO_SELECTION
    rememberSelectionLifecycle(
        selection = selection,
        data = data,
        itemCount = pointCount,
    )

    if (errors.isNotEmpty()) {
        ChartErrors(style.chartContainerStyle, errors.toImmutableList(), modifier)
        return
    }

    val internalData = remember(data, title) { toInternalStackedData(data, title) }
    val internalStyle = style.toInternal(showXAxisLabels = data.categories.isNotEmpty())
    val colors =
        remember(style.segments, data.series.size) {
            style.segments
                .resolveColors(data.series.size)
                .map { it.copy(alpha = style.segments.alpha) }
                .toImmutableList()
        }
    val segmentNames = data.series.map { it.name.orEmpty() }.toImmutableList()
    val selectedTitle = data.categories.getOrNull(selectedIndex)
    val effectiveTitle = selectedTitle ?: title.orEmpty()
    val selectedLabels =
        if (selectedIndex == NO_SELECTION) {
            persistentListOf()
        } else {
            data.series
                .map { ChartValueFormatters.Default.format(it.values[selectedIndex]) }
                .toImmutableList()
        }
    BoxWithConstraints(modifier = modifier) {
        val boundedHeight = maxHeight != Dp.Infinity
        Column {
            val plotModifier = if (boundedHeight) Modifier.weight(1f) else Modifier
            Box(modifier = plotModifier) {
                StackedBarChartInternal(
                    data = internalData,
                    title = effectiveTitle,
                    style = internalStyle,
                    colors = colors,
                    interactionEnabled = interactionEnabled,
                    animateOnStart = animateOnStart,
                    selectedBarIndex = selectedIndex,
                    onValueChanged = { index ->
                        if (index == NO_SELECTION) selection.clear() else selection.select(index)
                    },
                )
            }
            if (data.categories.isNotEmpty() && segmentNames.any { it.isNotBlank() }) {
                Legend(
                    chartContainerStyle = style.chartContainerStyle,
                    colors = colors,
                    legend = segmentNames,
                    labels = selectedLabels,
                )
            }
        }
    }
}

private fun validateStackedBarInput(
    data: ChartData,
    style: StackedBarChartStyle,
    density: Density,
): List<String> {
    val errors = mutableListOf<String>()
    if (data.series.isEmpty()) return listOf("At least one stacked segment is required.")
    val barCount =
        data.series
            .first()
            .values.size
    if (barCount < 2) errors += "At least two stacked bars are required."
    if (data.categories.isNotEmpty() && data.categories.size != barCount) {
        errors += "Category count (${data.categories.size}) must match bar count ($barCount)."
    }
    data.series.forEachIndexed { index, series ->
        if (series.values.size != barCount) errors += "Segment $index is not aligned with the first segment."
        if (series.values.any { !it.isFinite() || it < 0.0 }) {
            errors += "Segment $index contains a negative or non-finite contribution."
        }
    }
    if (style.segments.colors.isNotEmpty() && style.segments.colors.size != data.series.size) {
        errors += "Segment color count must match segment count (${data.series.size})."
    }
    if (!style.segments.alpha.isFinite() || style.segments.alpha !in 0f..1f) {
        errors += "Segment alpha must be in 0..1."
    }
    if (style.axis.xLabels.count < 2 || style.axis.yLabels.count < 2) {
        errors += "Axis label counts must be at least two."
    }
    errors +=
        validateSizes(
            density,
            "Bar spacing" to style.layout.space,
            "Minimum bar width" to style.layout.minBarWidth,
            "Selection line width" to style.selection.width,
        )
    return errors
}

private fun toInternalStackedData(
    data: ChartData,
    title: String?,
): MultiChartData {
    val barCount =
        data.series
            .first()
            .values.size
    val segmentLabels = data.series.map { it.name.orEmpty() }
    val items =
        List(barCount) { barIndex ->
            val barLabel = data.categories.getOrNull(barIndex).orEmpty()
            ChartDataItem(
                label = barLabel,
                item =
                    InternalChartData(
                        data.series.mapIndexed { segmentIndex, series ->
                            segmentLabels[segmentIndex] to series.values[barIndex]
                        },
                    ),
            )
        }
    return MultiChartData(
        items = items,
        categories = segmentLabels,
        title = title.orEmpty(),
    )
}
