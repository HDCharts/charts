package io.github.hdcharts.charts

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import io.github.hdcharts.charts.internal.NO_SELECTION
import io.github.hdcharts.charts.internal.common.composable.ChartErrors
import io.github.hdcharts.charts.internal.common.model.ChartDataItem
import io.github.hdcharts.charts.internal.common.model.MultiChartData
import io.github.hdcharts.charts.internal.linechart.LineChartImpl
import io.github.hdcharts.charts.internal.linechart.toInternal
import io.github.hdcharts.charts.internal.validateSizes
import io.github.hdcharts.charts.model.ChartData
import io.github.hdcharts.charts.model.ChartSelection
import io.github.hdcharts.charts.model.ChartValueFormatter
import io.github.hdcharts.charts.model.rememberChartSelection
import io.github.hdcharts.charts.model.rememberSelectionLifecycle
import io.github.hdcharts.charts.style.LineChartDefaults
import io.github.hdcharts.charts.style.LineChartStyle
import kotlinx.collections.immutable.toImmutableList
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import io.github.hdcharts.charts.internal.common.model.ChartData as InternalChartData

/**
 * Displays one or more aligned indexed series. A single public entry point handles both
 * single- and multi-line data. Selection always refers to a source X index shared by all series.
 * In compact mode, user interaction selects the middle source index represented by a bucket;
 * programmatic source selection highlights the bucket containing that index.
 *
 * - [interactionEnabled] gates gesture-based selection. It has no effect in [LineChartRenderMode.Timeline],
 *   which is a non-interactive, continuously updating presentation mode.
 * - [renderMode] controls only the update transition. In [LineChartRenderMode.Timeline] the strip shifts
 *   horizontally as new points arrive; gestures are not supported regardless of [interactionEnabled].
 * - [animationDuration] is typed as [Duration] (for example `420.milliseconds`); the value is
 *   carried as a typed duration through the rendering pipeline and converted to milliseconds
 *   only at the animation spec boundary.
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
    animationDuration: Duration = 420.milliseconds,
    valueFormatter: ChartValueFormatter = LineChartDefaults.valueFormatter,
    axisValueFormatter: ChartValueFormatter = LineChartDefaults.axisValueFormatter,
) {
    val density = LocalDensity.current
    val errors = remember(data, style, density) { validateLineInput(data, style, density) }
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
    val internalData =
        remember(data, title, valueFormatter) {
            toInternalLineData(data, title, valueFormatter)
        }
    LineChartImpl(
        data = internalData,
        modifier = modifier,
        style = style.toInternal(),
        interactionEnabled = interactionEnabled,
        animateOnStart = animateOnStart,
        renderMode = renderMode,
        animationDuration = animationDuration,
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
    density: Density,
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
    if (style.range.min?.isFinite() == false || style.range.max?.isFinite() == false) {
        errors += "Range bounds must be finite."
    }
    errors +=
        validateSizes(
            density,
            "Line stroke width" to style.line.strokeWidth,
            "Point size" to style.points.size,
            "Selection size" to style.selection.size,
            "Active selection size" to style.selection.activeSize,
            "Axis line width" to style.axis.lineWidth,
        )
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
        title = title.orEmpty(),
    )
}
