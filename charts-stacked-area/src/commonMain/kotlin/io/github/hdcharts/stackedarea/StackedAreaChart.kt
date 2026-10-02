package io.github.hdcharts.stackedarea

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
import io.github.hdcharts.stackedarea.internal.StackedAreaChartImpl
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import io.github.hdcharts.core.internal.model.ChartData as InternalChartData

/** Displays absolute stacked areas from one or more aligned series sharing common X categories. */
@Composable
fun StackedAreaChart(
    data: ChartData,
    modifier: Modifier = Modifier,
    style: StackedAreaChartStyle = StackedAreaChartDefaults.style(),
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
                colorCount = style.fill.colors.size,
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

    val internalData = remember(data, title) { toInternalStackedAreaData(data, title) }
    val colors =
        remember(drawStyle.fill, data.series.size) {
            drawStyle.fill
                .resolveColors(data.series.size)
                .map { it.copy(alpha = drawStyle.fill.alpha) }
                .toImmutableList()
        }
    val seriesNames = data.series.map { it.name.orEmpty() }.toImmutableList()
    val selectedTitle = data.categories.getOrNull(selectedIndex)?.takeIf(String::isNotBlank)
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
                StackedAreaChartImpl(
                    data = internalData,
                    title = effectiveTitle,
                    style = drawStyle,
                    areaColors = colors,
                    interactionEnabled = interactionEnabled,
                    animateOnStart = animateOnStart,
                    selectedPointIndex = selectedIndex,
                    onValueChanged = { index ->
                        if (index == NO_SELECTION) selection.clear() else selection.select(index)
                    },
                )
            }
            if (data.categories.isNotEmpty() && seriesNames.any { it.isNotBlank() }) {
                Legend(
                    chartContainerStyle = style.chartContainerStyle,
                    colors = colors,
                    legend = seriesNames,
                    labels = selectedLabels,
                )
            }
        }
    }
}

private fun toInternalStackedAreaData(
    data: ChartData,
    title: String?,
): MultiChartData {
    val categories = data.categories
    val items =
        data.series.map { series ->
            ChartDataItem(
                label = series.name.orEmpty(),
                item =
                    InternalChartData(
                        series.values.mapIndexed { index, value ->
                            categories.getOrNull(index).orEmpty() to value
                        },
                    ),
            )
        }
    return MultiChartData(
        items = items,
        categories = categories,
        title = title.orEmpty(),
    )
}
