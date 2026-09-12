package io.github.dautovicharis.charts

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import io.github.dautovicharis.charts.internal.NO_SELECTION
import io.github.dautovicharis.charts.internal.common.composable.Chart
import io.github.dautovicharis.charts.internal.common.composable.ChartErrors
import io.github.dautovicharis.charts.internal.common.composable.Legend
import io.github.dautovicharis.charts.internal.common.model.ChartDataItem
import io.github.dautovicharis.charts.internal.common.model.MultiChartData
import io.github.dautovicharis.charts.internal.common.palette.generateColorShades
import io.github.dautovicharis.charts.internal.stackedareachart.StackedAreaChart
import io.github.dautovicharis.charts.internal.stackedareachart.toInternal
import io.github.dautovicharis.charts.model.ChartData
import io.github.dautovicharis.charts.model.ChartSelection
import io.github.dautovicharis.charts.model.ChartValueFormatters
import io.github.dautovicharis.charts.model.rememberChartSelection
import io.github.dautovicharis.charts.style.StackedAreaChartDefaults
import io.github.dautovicharis.charts.style.StackedAreaChartStyle
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import io.github.dautovicharis.charts.internal.common.model.ChartData as InternalChartData

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
    val errors = remember(data, style) { validateStackedAreaInput(data, style) }
    val pointCount =
        data.series
            .firstOrNull()
            ?.values
            ?.size ?: 0
    val selectedIndex = selection.selectedIndex?.takeIf { it in 0 until pointCount } ?: NO_SELECTION
    val previousData = remember(selection) { mutableStateOf<ChartData?>(null) }
    LaunchedEffect(data) {
        val oldData = previousData.value
        if (oldData != null && oldData != data) selection.clear()
        previousData.value = data
    }

    if (errors.isNotEmpty()) {
        ChartErrors(style.chartContainerStyle, errors.toImmutableList(), modifier)
        return
    }

    val internalData = remember(data, title) { toInternalStackedAreaData(data, title) }
    val internalStyle = style.toInternal(showXAxisLabels = data.categories.isNotEmpty())
    val hasSingleSeries = data.series.size == 1
    val colors =
        remember(style.fill, data.series.size, hasSingleSeries) {
            val palette =
                if (hasSingleSeries) {
                    persistentListOf(style.fill.color)
                } else if (style.fill.colors.isEmpty()) {
                    generateColorShades(style.fill.color, data.series.size)
                } else {
                    style.fill.colors
                }
            palette.map { it.copy(alpha = style.fill.alpha) }.toImmutableList()
        }
    val lineColors =
        remember(style.boundary, data.series.size, hasSingleSeries) {
            if (hasSingleSeries) {
                persistentListOf(style.boundary.color)
            } else if (style.boundary.colors.isEmpty()) {
                generateColorShades(style.boundary.color, data.series.size)
            } else {
                style.boundary.colors
            }.toImmutableList()
        }
    val seriesNames = data.series.map { it.name.orEmpty() }.toImmutableList()
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
    Chart(style.chartContainerStyle, modifier) {
        Column(modifier = Modifier.wrapContentSize()) {
            StackedAreaChart(
                data = internalData,
                title = effectiveTitle,
                style = internalStyle,
                areaColors = colors,
                lineColors = lineColors,
                interactionEnabled = interactionEnabled,
                animateOnStart = animateOnStart,
                selectedPointIndex = selectedIndex,
                onValueChanged = { index ->
                    if (index == NO_SELECTION) selection.clear() else selection.select(index)
                },
            )
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

private fun validateStackedAreaInput(
    data: ChartData,
    style: StackedAreaChartStyle,
): List<String> {
    val errors = mutableListOf<String>()
    if (data.series.isEmpty()) return listOf("At least one stacked area series is required.")
    val pointCount =
        data.series
            .first()
            .values.size
    if (pointCount < 2) errors += "At least two stacked area values are required."
    if (data.categories.isNotEmpty() && data.categories.size != pointCount) {
        errors += "Category count (${data.categories.size}) must match value count ($pointCount)."
    }
    data.series.forEachIndexed { index, series ->
        if (series.values.size != pointCount) errors += "Series $index is not aligned with the first series."
        if (series.values.any { !it.isFinite() || it < 0.0 }) {
            errors += "Series $index contains a negative or non-finite contribution."
        }
    }
    if (style.fill.colors.isNotEmpty() && style.fill.colors.size != data.series.size) {
        errors += "Fill color count must match series count (${data.series.size})."
    }
    if (style.boundary.colors.isNotEmpty() && style.boundary.colors.size != data.series.size) {
        errors += "Boundary color count must match series count (${data.series.size})."
    }
    if (!style.fill.alpha.isFinite() || style.fill.alpha !in 0f..1f) {
        errors += "Fill alpha must be in 0..1."
    }
    if (style.axis.xLabels.count < 2 || style.axis.yLabels.count < 2) {
        errors += "Axis label counts must be at least two."
    }
    return errors
}

private fun toInternalStackedAreaData(
    data: ChartData,
    title: String?,
): MultiChartData {
    val categories = data.categories
    val items =
        data.series.map { series ->
            val labels =
                if (categories.isNotEmpty()) categories else emptyList()
            ChartDataItem(
                label = series.name.orEmpty(),
                item =
                    InternalChartData(
                        series.values.mapIndexed { index, value ->
                            labels.getOrNull(index).orEmpty() to value
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
