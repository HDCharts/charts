package io.github.dautovicharis.charts

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import io.github.dautovicharis.charts.internal.NO_SELECTION
import io.github.dautovicharis.charts.internal.common.composable.ChartErrors
import io.github.dautovicharis.charts.internal.common.model.ChartDataItem
import io.github.dautovicharis.charts.internal.common.model.MultiChartData
import io.github.dautovicharis.charts.internal.linechart.LineChartImpl
import io.github.dautovicharis.charts.internal.linechart.toInternal
import io.github.dautovicharis.charts.model.ChartData
import io.github.dautovicharis.charts.model.ChartSelection
import io.github.dautovicharis.charts.model.ChartValueFormatter
import io.github.dautovicharis.charts.model.rememberChartSelection
import io.github.dautovicharis.charts.style.LineChartDefaults
import io.github.dautovicharis.charts.style.LineChartStyle
import kotlinx.collections.immutable.toImmutableList
import io.github.dautovicharis.charts.internal.common.model.ChartData as InternalChartData

/**
 * Displays one or more aligned indexed series. A single public entry point handles both
 * single- and multi-line data. Selection always refers to a source X index shared by all series.
 */
@Composable
fun LineChart(
    data: ChartData,
    modifier: Modifier = Modifier,
    style: LineChartStyle = LineChartDefaults.style(),
    title: String? = null,
    selection: ChartSelection = rememberChartSelection(),
    interactionEnabled: Boolean = true,
    animateOnStart: Boolean = true,
    renderMode: LineChartRenderMode = LineChartRenderMode.Morph,
    animationDurationMillis: Int = 420,
    valueFormatter: ChartValueFormatter = LineChartDefaults.valueFormatter,
    axisValueFormatter: ChartValueFormatter = LineChartDefaults.axisValueFormatter,
) {
    val errors = remember(data, style) { validateLineInput(data, style) }
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
    val internalData =
        remember(data, title, valueFormatter) {
            toInternalLineData(data, title, valueFormatter)
        }
    LineChartImpl(
        data = internalData,
        style = style.toInternal(modifier),
        interactionEnabled = interactionEnabled,
        animateOnStart = animateOnStart,
        renderMode = renderMode,
        animationDurationMillis = animationDurationMillis,
        selectedPointIndex = selectedIndex,
        onValueChanged = { index ->
            if (index == NO_SELECTION) selection.clear() else selection.select(index)
        },
        valueFormatter = valueFormatter,
        axisValueFormatter = axisValueFormatter,
        selectedTitle =
            if (selectedIndex == NO_SELECTION) {
                null
            } else if (data.series.size == 1) {
                val value = valueFormatter.format(data.series.single().values[selectedIndex])
                data.categories.getOrNull(selectedIndex)?.let { "$it: $value" } ?: value
            } else {
                data.categories.getOrNull(selectedIndex)
            },
    )
}

private fun validateLineInput(
    data: ChartData,
    style: LineChartStyle,
): List<String> {
    val errors = mutableListOf<String>()
    if (data.series.isEmpty()) return listOf("At least one line series is required.")
    val pointCount =
        data.series
            .first()
            .values.size
    if (pointCount < 2) errors += "At least two line values are required."
    if (data.categories.isNotEmpty() && data.categories.size != pointCount) {
        errors += "Category count (${data.categories.size}) must match value count ($pointCount)."
    }
    if (style.line.colors.isNotEmpty() && style.line.colors.size != data.series.size) {
        errors += "Line color count must match series count (${data.series.size})."
    }
    data.series.forEachIndexed { index, series ->
        if (series.values.size != pointCount) errors += "Series $index is not aligned with the first series."
        if (data.categories.isNotEmpty() &&
            series.values.size != data.categories.size &&
            errors.none { it.startsWith("Category count") }
        ) {
            errors += "Category count (${data.categories.size}) must match every series value count."
        }
        if (series.values.any { !it.isFinite() }) errors += "Series $index contains a non-finite value."
    }
    if (!style.line.alpha.isFinite() || style.line.alpha !in 0f..1f) errors += "Line alpha must be in 0..1."
    if (!style.line.strokeWidth.value
            .isFinite() ||
        style.line.strokeWidth.value < 0f
    ) {
        errors += "Line stroke width must be finite and nonnegative."
    }
    if (style.axis.xLabels.count < 2 ||
        style.axis.yLabels.count < 2
    ) {
        errors += "Axis label counts must be at least two."
    }
    return errors
}

private fun toInternalLineData(
    data: ChartData,
    title: String?,
    formatter: ChartValueFormatter,
): MultiChartData {
    val categories = data.categories
    val items =
        data.series.map { series ->
            val labels =
                when {
                    data.series.size == 1 && categories.isNotEmpty() -> categories
                    data.series.size == 1 -> emptyList()
                    else -> series.values.map(formatter::format)
                }
            ChartDataItem(
                label = series.name.orEmpty(),
                item =
                    InternalChartData(
                        series.values.mapIndexed { index, value ->
                            labels.getOrNull(index).orEmpty() to
                                value
                        },
                    ),
            )
        }
    return MultiChartData(
        items = items,
        categories = categories.takeIf { data.series.size > 1 }.orEmpty(),
        title =
            title ?: data.series
                .firstOrNull()
                ?.name
                .orEmpty(),
    )
}
