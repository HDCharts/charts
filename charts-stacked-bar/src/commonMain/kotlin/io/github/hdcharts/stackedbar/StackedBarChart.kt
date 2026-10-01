package io.github.hdcharts.stackedbar

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import io.github.hdcharts.core.internal.NO_SELECTION
import io.github.hdcharts.core.internal.ValidationErrors
import io.github.hdcharts.core.internal.axis.validateAxisLabels
import io.github.hdcharts.core.internal.composable.ChartErrors
import io.github.hdcharts.core.internal.composable.Legend
import io.github.hdcharts.core.internal.model.ChartDataItem
import io.github.hdcharts.core.internal.model.MultiChartData
import io.github.hdcharts.core.internal.validateSeries
import io.github.hdcharts.core.model.ChartData
import io.github.hdcharts.core.model.ChartSelection
import io.github.hdcharts.core.model.ChartValueFormatters
import io.github.hdcharts.core.model.rememberChartSelection
import io.github.hdcharts.core.model.rememberSelectionLifecycle
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import io.github.hdcharts.core.internal.model.ChartData as InternalChartData
import io.github.hdcharts.stackedbar.internal.StackedBarChart as StackedBarChartInternal

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
    val errors =
        remember(data, style, density) {
            validateSeries(
                data = data,
                minValues = ValidationErrors.MIN_VALUES,
                allowNegative = false,
                colorCount = style.segments.colors.size,
            ) + validateAxisLabels(style.axis.xLabels, style.axis.yLabels, density)
        }
    val drawStyle = remember(style, density) { style.clamped(density) }
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
    val colors =
        remember(drawStyle.segments, data.series.size) {
            drawStyle.segments
                .resolveColors(data.series.size)
                .map { it.copy(alpha = drawStyle.segments.alpha) }
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
                    style = drawStyle,
                    colors = colors,
                    showXAxisLabels = style.axis.xLabels.visible && data.categories.any { it.isNotBlank() },
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
