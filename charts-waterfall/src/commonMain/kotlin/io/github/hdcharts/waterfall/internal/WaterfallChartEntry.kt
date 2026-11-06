package io.github.hdcharts.waterfall.internal

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import io.github.hdcharts.core.internal.AxisInputs
import io.github.hdcharts.core.internal.axis.validateAxisLabels
import io.github.hdcharts.core.internal.composable.ChartErrors
import io.github.hdcharts.core.internal.validateRange
import io.github.hdcharts.core.internal.validationValues
import io.github.hdcharts.core.model.ChartSelection
import io.github.hdcharts.core.model.ChartValueFormatter
import io.github.hdcharts.core.model.rememberSelectionLifecycle
import io.github.hdcharts.core.style.BarRangeStyle
import io.github.hdcharts.waterfall.WaterfallChartStyle
import io.github.hdcharts.waterfall.WaterfallData
import io.github.hdcharts.waterfall.WaterfallEntry
import io.github.hdcharts.waterfall.clamp
import kotlinx.collections.immutable.toImmutableList

@Composable
internal fun WaterfallChartEntry(
    data: WaterfallData,
    modifier: Modifier,
    style: WaterfallChartStyle,
    title: String?,
    selection: ChartSelection,
    interactionEnabled: Boolean,
    animateOnStart: Boolean,
    valueFormatter: ChartValueFormatter,
    axisValueFormatter: ChartValueFormatter,
) {
    rememberSelectionLifecycle(selection = selection, data = data, itemCount = data.entries.size)
    val density = LocalDensity.current
    val inputs = WaterfallChartPolicy.snapshot(style)
    val errors = remember(data, inputs, density) { WaterfallChartPolicy.errorsFor(data, inputs, density) }
    if (errors.isNotEmpty()) {
        ChartErrors(style = style.chartContainerStyle, errors = errors.toImmutableList(), modifier = modifier)
        return
    }
    val bars = remember(data) { waterfallBars(data) }
    val drawStyle = remember(style, density) { style.clamp(density) }
    WaterfallChartContent(
        data,
        bars,
        modifier,
        drawStyle,
        title,
        selection,
        interactionEnabled,
        animateOnStart,
        valueFormatter,
        axisValueFormatter,
    )
}

internal data class WaterfallValidationInputs(
    val axes: AxisInputs,
    val range: BarRangeStyle,
)

/** Waterfall's own data contract, following the custom-model entry used by Scatter and Heatmap. */
internal object WaterfallChartPolicy {
    fun snapshot(style: WaterfallChartStyle) =
        WaterfallValidationInputs(
            axes = AxisInputs(style.axes.xLabels.validationValues(), style.axes.yLabels.validationValues()),
            range = style.range,
        )

    fun errorsFor(
        data: WaterfallData,
        inputs: WaterfallValidationInputs,
        density: Density,
    ): List<String> =
        buildList {
            if (data.entries.isEmpty()) add("At least one waterfall entry is required.")
            var balance = 0.0
            for (entry in data.entries) {
                if (entry is WaterfallEntry.Change) {
                    if (!entry.amount.isFinite()) {
                        add("Change amounts must be finite.")
                        break
                    }
                    balance += entry.amount
                    if (!balance.isFinite()) {
                        add("Every running total must be finite.")
                        break
                    }
                }
            }
            addAll(validateRange(inputs.range.min, inputs.range.max))
            addAll(validateAxisLabels(inputs.axes.xLabels, inputs.axes.yLabels, density))
        }
}
