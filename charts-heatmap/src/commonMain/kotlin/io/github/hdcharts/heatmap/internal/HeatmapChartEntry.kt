package io.github.hdcharts.heatmap.internal

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import io.github.hdcharts.heatmap.HeatmapChartStyle
import io.github.hdcharts.heatmap.HeatmapColorScale
import io.github.hdcharts.heatmap.HeatmapData
import io.github.hdcharts.heatmap.clamp
import kotlinx.collections.immutable.toImmutableList

/** Validates heatmap inputs before clamping its style and drawing the matrix. */
@Composable
internal fun HeatmapChartEntry(
    data: HeatmapData,
    modifier: Modifier,
    style: HeatmapChartStyle,
    title: String?,
    selection: ChartSelection,
    interactionEnabled: Boolean,
    animateOnStart: Boolean,
    valueFormatter: ChartValueFormatter,
) {
    rememberSelectionLifecycle(selection = selection, data = data, itemCount = data.rows.size * data.columns.size)
    // A missing cell holds no observation, so a programmatic selection of one is cleared.
    val selectedIndex = selection.selectedIndex
    LaunchedEffect(selection, data, selectedIndex) {
        val current = selection.selectedIndex
        if (current != null && data.valueAtIndex(current) == null) selection.clear()
    }

    val density = LocalDensity.current
    val inputs = HeatmapChartPolicy.snapshot(style)
    val errors = remember(data, inputs, density) { HeatmapChartPolicy.errorsFor(data, inputs, density) }
    if (errors.isNotEmpty()) {
        ChartErrors(
            style = style.chartContainerStyle,
            errors = errors.toImmutableList(),
            modifier = modifier,
        )
        return
    }

    val drawStyle = remember(style, density) { style.clamp(density) }
    HeatmapChartContent(
        data = data,
        modifier = modifier,
        style = drawStyle,
        title = title,
        selection = selection,
        interactionEnabled = interactionEnabled,
        animateOnStart = animateOnStart,
        valueFormatter = valueFormatter,
    )
}

private data class HeatmapValidationInputs(
    val colorScale: HeatmapColorScale,
    val axes: AxisInputs,
)

/**
 * The heatmap's own validation contract. It checks the matrix shape against the labels, finite
 * present values, at least one present value, the color scale and the axis label settings.
 */
private object HeatmapChartPolicy {
    fun snapshot(style: HeatmapChartStyle) =
        HeatmapValidationInputs(
            colorScale = style.colorScale,
            axes =
                AxisInputs(
                    xLabels = style.xLabels.validationValues(),
                    yLabels = style.yLabels.validationValues(),
                ),
        )

    fun errorsFor(
        data: HeatmapData,
        inputs: HeatmapValidationInputs,
        density: Density,
    ): List<String> =
        buildList {
            if (data.columns.isEmpty()) add("At least one column is required.")
            if (data.rows.isEmpty()) add("At least one row is required.")
            if (data.values.size != data.rows.size) {
                add("The value matrix must have one value row per row label.")
            }
            if (data.values.any { it.size != data.columns.size }) {
                add("Every value row must have one value per column label.")
            }
            val present = data.values.flatMap { row -> row.filterNotNull() }
            if (present.any { !it.isFinite() }) add("Cell values must be finite.")
            if (isEmpty() && present.isEmpty()) add("At least one cell must have a value.")

            val scale = inputs.colorScale
            if (scale.colors.size < 2) add("The color scale requires at least two colors.")
            if (listOfNotNull(scale.min, scale.max).any { !it.isFinite() }) {
                add("Color scale bounds must be finite.")
            }
            if (scale.min != null && scale.max != null && scale.min >= scale.max) {
                add("The color scale minimum must be less than its maximum.")
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
