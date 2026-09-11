package io.github.dautovicharis.charts

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import io.github.dautovicharis.charts.internal.InternalChartsApi
import io.github.dautovicharis.charts.internal.TestTags
import io.github.dautovicharis.charts.internal.common.composable.ChartErrors
import io.github.dautovicharis.charts.internal.validateHistogramData
import io.github.dautovicharis.charts.model.ChartData
import io.github.dautovicharis.charts.model.ChartSelection
import io.github.dautovicharis.charts.model.rememberChartSelection
import io.github.dautovicharis.charts.style.HistogramChartDefaults
import io.github.dautovicharis.charts.style.HistogramChartStyle
import kotlinx.collections.immutable.toImmutableList
import io.github.dautovicharis.charts.internal.common.model.ChartData as InternalChartData

/**
 * Displays a histogram chart from a single aligned [ChartData] series of pre-binned values.
 *
 * Histograms render adjacent equal-width bars at the Y-baseline (defaults to zero). Numeric
 * bins must be non-finite-free and non-negative. Categories are optional bin labels; arbitrary
 * text and open-ended intervals such as `300ms+` are permitted.
 *
 * @param data The chart data. Exactly one series of precomputed bin heights.
 * @param modifier Optional [Modifier] for the chart container. Applied on valid and validation-error paths.
 * @param style The grouped [HistogramChartStyle] controlling bars, range, grid, axis, selection indicator, and container.
 * @param title Optional chart title. Selected-bin readouts replace this title when a bin is selected.
 * @param selection Top-level hoisted [ChartSelection]. Selected index always refers to a source bin.
 * @param interactionEnabled Enables touch interactions. Defaults to true.
 * @param animateOnStart Enables initial reveal animation. Defaults to true.
 */
@Composable
fun HistogramChart(
    data: ChartData,
    modifier: Modifier = Modifier,
    style: HistogramChartStyle = HistogramChartDefaults.style(),
    title: String? = null,
    selection: ChartSelection = rememberChartSelection(),
    interactionEnabled: Boolean = true,
    animateOnStart: Boolean = true,
) {
    val internalChartData =
        remember(data, style.bars.colors) {
            adaptHistogramToInternalChartData(data, title)
        }
    val errors =
        remember(internalChartData, style.bars.colors.size) {
            validateHistogramData(
                data = internalChartData,
                colorsSize = style.bars.colors.size,
            )
        }

    if (errors.isEmpty()) {
        Box(modifier = modifier.testTag(TestTags.HISTOGRAM_CHART)) {
            BarChartInternalPlot(
                chartData = data,
                title = title.orEmpty(),
                style = style.asBarChartStyle(),
                interactionEnabled = interactionEnabled,
                animateOnStart = animateOnStart,
                selection = selection,
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
fun adaptHistogramToInternalChartData(
    data: ChartData,
    title: String?,
): InternalChartData {
    require(data.series.size == 1) {
        "HistogramChart requires exactly one series; got ${data.series.size}."
    }
    val values = data.series.single().values
    require(values.size >= MIN_HISTOGRAM_BINS) {
        "HistogramChart requires at least $MIN_HISTOGRAM_BINS bins; got ${values.size}."
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
            "HistogramChart value at index $index is not finite: $value."
        }
        require(value >= 0.0) {
            "HistogramChart value at index $index is negative: $value."
        }
    }
    return InternalChartData(labels.zip(values) { label, value -> label to value })
}

private const val MIN_HISTOGRAM_BINS = 2

/**
 * Reuses the bar renderer for histograms while keeping histogram defaults (adjacent bins,
 * zero baseline) on the histogram style surface. Conversion happens at the boundary so the
 * public histogram style is preserved as a distinct type.
 */
@InternalChartsApi
fun HistogramChartStyle.asBarChartStyle(): io.github.dautovicharis.charts.style.BarChartStyle =
    io.github.dautovicharis.charts.style.BarChartStyle(
        chartContainerStyle = chartContainerStyle,
        bars = bars,
        range = range,
        grid = grid,
        axis = axis,
        selectionLine = selectionLine,
        zoomControlsVisible = zoomControlsVisible,
    )
