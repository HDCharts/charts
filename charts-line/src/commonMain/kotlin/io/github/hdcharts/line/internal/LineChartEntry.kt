package io.github.hdcharts.line.internal

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import io.github.hdcharts.core.internal.ValidationErrors
import io.github.hdcharts.core.internal.axis.validateAxisLabels
import io.github.hdcharts.core.internal.composable.ChartErrors
import io.github.hdcharts.core.internal.model.ChartDataItem
import io.github.hdcharts.core.internal.model.MultiChartData
import io.github.hdcharts.core.internal.validateRange
import io.github.hdcharts.core.internal.validateSeries
import io.github.hdcharts.core.model.ChartData
import io.github.hdcharts.line.LineChartStyle
import io.github.hdcharts.line.clamped
import kotlinx.collections.immutable.toImmutableList
import io.github.hdcharts.core.internal.model.ChartData as InternalChartData

/**
 * Validates public line data, renders the errors or converts the data, and hands it to [content]
 * with [style] clamped for drawing.
 * Shared by [io.github.hdcharts.line.LineChart] and [io.github.hdcharts.line.LiveLineChart].
 */
@Composable
internal fun LineChartEntry(
    data: ChartData,
    modifier: Modifier,
    style: LineChartStyle,
    title: String?,
    content: @Composable (data: MultiChartData, style: LineChartStyle) -> Unit,
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
    val drawStyle = remember(style, density) { style.clamped(density) }
    content(internalData, drawStyle)
}

internal fun validateLineInput(
    data: ChartData,
    style: LineChartStyle,
    density: Density,
): List<String> =
    validateSeries(data = data, minValues = ValidationErrors.MIN_VALUES, colorCount = style.line.colors.size) +
        validateRange(min = style.range.min, max = style.range.max) +
        validateAxisLabels(style.axis.xLabels, style.axis.yLabels, density)

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
