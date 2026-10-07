package io.github.hdcharts.pie.internal

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Density
import io.github.hdcharts.core.internal.ChartEntry
import io.github.hdcharts.core.internal.ChartPolicy
import io.github.hdcharts.core.internal.ChartSpec
import io.github.hdcharts.core.internal.ChartValidationInputs
import io.github.hdcharts.core.internal.InternalChartsApi
import io.github.hdcharts.core.internal.ValidationErrors
import io.github.hdcharts.core.model.ChartData
import io.github.hdcharts.core.model.ChartSelection
import io.github.hdcharts.core.model.ChartValueFormatter
import io.github.hdcharts.pie.PieChartSlicesStyle
import io.github.hdcharts.pie.PieChartStyle
import io.github.hdcharts.pie.clamp
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

/**
 * Everything [io.github.hdcharts.pie.PieChart] declares about its input. A pie is one series whose
 * values are the slices, so it needs exactly one series, forbids negative values, and has no
 * Cartesian axis and no fixed range.
 */
@InternalChartsApi
object PieChartSpec : ChartSpec<PieChartStyle> {
    override val policy =
        ChartPolicy(
            minValues = ValidationErrors.MIN_VALUES,
            allowNegative = false,
            singleSeries = true,
            hasAxis = false,
            hasFixedRange = false,
            // A single-series chart counts its colors against its value count, so it states no rule.
            colorsMatch = null,
        )

    /** A pie has no Cartesian axis, so there are no axis label styles to check, and no range bounds. */
    override fun validationInputs(style: PieChartStyle): ChartValidationInputs =
        ChartValidationInputs(
            colorCount = style.slices.colors.size,
            rangeMin = null,
            rangeMax = null,
            xLabels = null,
            yLabels = null,
        )

    override fun clamp(
        style: PieChartStyle,
        density: Density,
    ): PieChartStyle = style.clamp(density)
}

/**
 * Runs [PieChartSpec] through the shared seam, then draws the render model with
 * [PieChartFrame]. The selection index is resolved by the caller, because it reads the public data
 * rather than the render model.
 */
@Composable
internal fun PieChartEntry(
    data: ChartData,
    modifier: Modifier,
    style: PieChartStyle,
    title: String?,
    selectedValueFormatter: ChartValueFormatter,
    selection: ChartSelection,
    interactionEnabled: Boolean,
    animateOnStart: Boolean,
    onSelectionInteraction: () -> Unit,
) {
    ChartEntry(
        spec = PieChartSpec,
        data = data,
        style = style,
        errorStyle = style.chartContainerStyle,
        title = title,
        content = { renderData, drawStyle ->
            val values = renderData.series.single().values
            val colors =
                remember(drawStyle.slices, values.size) {
                    resolveSliceColors(style = drawStyle.slices, sliceCount = values.size)
                }
            PieChartFrame(
                modifier = modifier,
                title = title,
                selectedValueFormatter = selectedValueFormatter,
                labels = renderData.categories,
                points = values,
                colors = colors,
                style = drawStyle,
                selection = selection,
                interactionEnabled = interactionEnabled,
                animateOnStart = animateOnStart,
                onSelectionInteraction = onSelectionInteraction,
            )
        },
        modifier = modifier,
    )
}

/** The colors for [sliceCount] slices, with the style's alpha applied to each one. */
private fun resolveSliceColors(
    style: PieChartSlicesStyle,
    sliceCount: Int,
): ImmutableList<Color> =
    style
        .resolveColors(sliceCount)
        .map { color -> color.copy(alpha = style.alpha) }
        .toImmutableList()
