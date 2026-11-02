package io.github.hdcharts.scatter.internal

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import io.github.hdcharts.core.internal.AxisInputs
import io.github.hdcharts.core.internal.axis.validateAxisLabels
import io.github.hdcharts.core.internal.composable.ChartErrors
import io.github.hdcharts.core.internal.validationValues
import io.github.hdcharts.core.model.ChartSelection
import io.github.hdcharts.core.model.ChartValueFormatter
import io.github.hdcharts.core.model.rememberSelectionLifecycle
import io.github.hdcharts.scatter.ScatterChartStyle
import io.github.hdcharts.scatter.ScatterData
import io.github.hdcharts.scatter.ScatterRangeStyle
import io.github.hdcharts.scatter.clamp
import kotlinx.collections.immutable.toImmutableList

/** Validates scatter inputs before clamping its style and drawing the points. */
@Composable
internal fun ScatterChartEntry(
    data: ScatterData,
    modifier: Modifier,
    style: ScatterChartStyle,
    title: String?,
    selection: ChartSelection,
    interactionEnabled: Boolean,
    xValueFormatter: ChartValueFormatter,
    yValueFormatter: ChartValueFormatter,
    sizeValueFormatter: ChartValueFormatter,
) {
    val pointCount = data.series.sumOf { it.points.size }
    rememberSelectionLifecycle(selection = selection, data = data, itemCount = pointCount)

    val density = LocalDensity.current
    val inputs = ScatterChartPolicy.snapshot(style)
    val errors = remember(data, inputs, density) { ScatterChartPolicy.errorsFor(data, inputs, density) }
    if (errors.isNotEmpty()) {
        ChartErrors(
            style = style.chartContainerStyle,
            errors = errors.toImmutableList(),
            modifier = modifier,
        )
        return
    }

    val drawStyle = remember(style, density) { style.clamp(density) }
    ScatterChartContent(
        data = data,
        modifier = modifier,
        style = drawStyle,
        title = title,
        selection = selection,
        interactionEnabled = interactionEnabled,
        xValueFormatter = xValueFormatter,
        yValueFormatter = yValueFormatter,
        sizeValueFormatter = sizeValueFormatter,
    )
}

private data class ScatterValidationInputs(
    val colorCount: Int,
    val range: ScatterRangeStyle,
    val axes: AxisInputs,
)

private object ScatterChartPolicy {
    fun snapshot(style: ScatterChartStyle) =
        ScatterValidationInputs(
            colorCount = style.points.colors.size,
            range = style.range,
            axes =
                AxisInputs(
                    xLabels = style.axes.xLabels.validationValues(),
                    yLabels = style.axes.yLabels.validationValues(),
                ),
        )

    fun errorsFor(
        data: ScatterData,
        inputs: ScatterValidationInputs,
        density: Density,
    ): List<String> {
        val points = data.series.flatMap { it.points }
        return buildList {
            if (data.series.isEmpty()) add("At least one series is required.")
            if (data.series.any { it.points.isEmpty() }) add("Every series must contain at least one point.")
            if (data.series.size > 1 && data.series.any { it.name.isNullOrBlank() }) {
                add("Every series must have a name when multiple series are used.")
            }
            val multipleSeries = data.series.size > 1
            val paletteMismatch = inputs.colorCount > 0 && inputs.colorCount != data.series.size
            if (multipleSeries && paletteMismatch) {
                add("The color count must match the series count.")
            }
            if (points.any {
                    !it.x.isFinite() ||
                        !it.y.isFinite() ||
                        it.size?.let { value -> !value.isFinite() || value < 0.0 } == true
                }
            ) {
                add("Point coordinates must be finite and sizes must be finite and non-negative.")
            }
            val bounds = listOf(inputs.range.xMin, inputs.range.xMax, inputs.range.yMin, inputs.range.yMax)
            if (bounds.any { it != null && !it.isFinite() }) add("Fixed axis bounds must be finite.")
            if (inputs.range.xMin != null && inputs.range.xMax != null && inputs.range.xMin >= inputs.range.xMax) {
                add("The X minimum must be less than the X maximum.")
            }
            if (inputs.range.yMin != null && inputs.range.yMax != null && inputs.range.yMin >= inputs.range.yMax) {
                add("The Y minimum must be less than the Y maximum.")
            }
            addAll(
                validateAxisLabels(
                    xLabels = inputs.axes.xLabels,
                    yLabels = inputs.axes.yLabels,
                    density = density,
                ),
            )
        }
    }
}
