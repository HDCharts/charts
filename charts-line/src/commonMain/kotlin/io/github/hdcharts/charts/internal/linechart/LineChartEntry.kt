package io.github.hdcharts.charts.internal.linechart

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import io.github.hdcharts.charts.internal.common.composable.ChartErrors
import io.github.hdcharts.charts.internal.common.model.ChartDataItem
import io.github.hdcharts.charts.internal.common.model.MultiChartData
import io.github.hdcharts.charts.internal.validateSizes
import io.github.hdcharts.charts.model.ChartData
import io.github.hdcharts.charts.style.LineChartStyle
import kotlinx.collections.immutable.toImmutableList
import io.github.hdcharts.charts.internal.common.model.ChartData as InternalChartData

/**
 * Validates public line data, renders the errors or converts the data, and hands it to [content].
 * Shared by [io.github.hdcharts.charts.LineChart] and [io.github.hdcharts.charts.LiveLineChart].
 */
@Composable
internal fun LineChartEntry(
    data: ChartData,
    modifier: Modifier,
    style: LineChartStyle,
    title: String?,
    content: @Composable (data: MultiChartData) -> Unit,
) {
    val density = LocalDensity.current
    val errors = remember(data, style, density) { validateLineInput(data = data, style = style, density = density) }
    if (errors.isNotEmpty()) {
        ChartErrors(
            style = style.chartContainerStyle,
            errors = errors.toImmutableList(),
            modifier = modifier,
        )
        return
    }
    val internalData =
        remember(data, title) {
            toInternalLineData(data = data, title = title)
        }
    content(internalData)
}

internal fun validateLineInput(
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
    } else if (data.categories.isNotEmpty() && data.series.any { it.values.size != pointCount }) {
        errors += "Category count (${data.categories.size}) must match every series value count."
    }
    if (style.line.colors.isNotEmpty() && style.line.colors.size != data.series.size) {
        errors += "Line color count must match series count (${data.series.size})."
    }
    data.series.forEachIndexed { index, series ->
        if (series.values.size != pointCount) errors += "Series $index is not aligned with the first series."
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
): MultiChartData {
    val categories = data.categories
    // A single series carries its categories as point labels; multi-series data keeps them on MultiChartData.
    val labels = categories.takeIf { data.series.size == 1 }.orEmpty()
    val items =
        data.series.map { series ->
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
