package io.github.dautovicharis.charts

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import io.github.dautovicharis.charts.internal.InternalChartsApi
import io.github.dautovicharis.charts.internal.NO_SELECTION
import io.github.dautovicharis.charts.internal.barchart.toInternal
import io.github.dautovicharis.charts.internal.common.composable.Chart
import io.github.dautovicharis.charts.internal.common.composable.ChartErrors
import io.github.dautovicharis.charts.internal.validateBarData
import io.github.dautovicharis.charts.model.ChartData
import io.github.dautovicharis.charts.model.ChartSelection
import io.github.dautovicharis.charts.model.rememberChartSelection
import io.github.dautovicharis.charts.style.BarChartDefaults
import io.github.dautovicharis.charts.style.BarChartStyle
import kotlinx.collections.immutable.toImmutableList
import io.github.dautovicharis.charts.internal.barchart.BarChart as BarChartInternal
import io.github.dautovicharis.charts.internal.common.model.ChartData as InternalChartData

/**
 * Displays a vertical bar chart from a single aligned [ChartData] series.
 *
 * @param data The chart data. Exactly one series is required; category index identifies a source bar.
 * @param modifier Optional [Modifier] for the chart container. Applied on valid and validation-error paths.
 * @param style The grouped [BarChartStyle] controlling bars, range, grid, axis, selection indicator, and container.
 * @param title Optional chart title. Selected-bar readouts replace this title when a source bar is selected.
 * @param selection Top-level hoisted [ChartSelection] for programmatic and gesture-driven selection.
 * Defaults to [rememberChartSelection]. Disabled interaction still allows programmatic selection.
 * @param interactionEnabled Enables touch interactions (tap selection, scrolling, zoom). Defaults to true.
 * @param animateOnStart Enables initial reveal animation. Subsequent updates still animate regardless.
 * Defaults to true.
 */
@Composable
fun BarChart(
    data: ChartData,
    modifier: Modifier = Modifier,
    style: BarChartStyle = BarChartDefaults.style(),
    title: String? = null,
    selection: ChartSelection = rememberChartSelection(),
    interactionEnabled: Boolean = true,
    animateOnStart: Boolean = true,
) {
    val internalChartData =
        remember(data, style.bars.colors) {
            adaptToInternalChartData(data, title)
        }
    val errors =
        remember(internalChartData, style.bars.colors.size) {
            validateBarData(
                data = internalChartData,
                colorsSize = style.bars.colors.size,
            )
        }

    if (errors.isEmpty()) {
        Chart(
            chartContainerStyle = style.chartContainerStyle,
            modifier = modifier,
        ) {
            BarChartInternal(
                chartData = internalChartData,
                title = title.orEmpty(),
                style = style.toInternal(),
                interactionEnabled = interactionEnabled,
                animateOnStart = animateOnStart,
                selectedBarIndex = selection.selectedIndex ?: NO_SELECTION,
                onValueChanged = { index -> selection.select(index) },
            )
        }
    } else {
        ChartErrors(
            style = style.chartContainerStyle,
            errors = errors.toImmutableList(),
        )
    }
}

/**
 * Internal entry point used by [io.github.dautovicharis.charts.HistogramChart] to delegate to
 * the shared bar renderer with already-binned data. Not part of the public v3 API.
 */
@InternalChartsApi
@Composable
fun BarChartInternalPlot(
    chartData: ChartData,
    title: String,
    style: BarChartStyle,
    interactionEnabled: Boolean,
    animateOnStart: Boolean,
    selection: ChartSelection,
    modifier: Modifier = Modifier,
) {
    val internalChartData =
        remember(chartData, style.bars.colors) {
            adaptToInternalChartData(chartData, title.takeIf { it.isNotBlank() })
        }
    val errors =
        remember(internalChartData, style.bars.colors.size) {
            validateBarData(
                data = internalChartData,
                colorsSize = style.bars.colors.size,
            )
        }

    if (errors.isEmpty()) {
        Chart(
            chartContainerStyle = style.chartContainerStyle,
            modifier = modifier,
        ) {
            BarChartInternal(
                chartData = internalChartData,
                title = title,
                style = style.toInternal(),
                interactionEnabled = interactionEnabled,
                animateOnStart = animateOnStart,
                selectedBarIndex = selection.selectedIndex ?: NO_SELECTION,
                onValueChanged = { index -> selection.select(index) },
            )
        }
    } else {
        ChartErrors(
            style = style.chartContainerStyle,
            errors = errors.toImmutableList(),
        )
    }
}

@InternalChartsApi
fun adaptToInternalChartData(
    data: ChartData,
    title: String?,
): InternalChartData {
    require(data.series.size == 1) {
        "BarChart requires exactly one series; got ${data.series.size}."
    }
    val values = data.series.single().values
    require(values.size >= MIN_BAR_POINTS) {
        "BarChart requires at least $MIN_BAR_POINTS values; got ${values.size}."
    }
    val labels =
        if (data.categories.isNotEmpty()) {
            require(data.categories.size == values.size) {
                "categories count (${data.categories.size}) must match values count (${values.size})."
            }
            data.categories
        } else {
            val titlePrefix = title.orEmpty()
            List(values.size) { index -> "$titlePrefix${index + 1}" }
        }
    values.forEachIndexed { index, value ->
        require(value.isFinite()) {
            "BarChart value at index $index is not finite: $value."
        }
    }
    return InternalChartData(labels.zip(values) { label, value -> label to value })
}

private const val MIN_BAR_POINTS = 2
