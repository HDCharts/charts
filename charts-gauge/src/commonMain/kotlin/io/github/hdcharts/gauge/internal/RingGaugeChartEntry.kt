package io.github.hdcharts.gauge.internal

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Density
import io.github.hdcharts.core.internal.ChartEntry
import io.github.hdcharts.core.internal.ChartPolicy
import io.github.hdcharts.core.internal.ChartSpec
import io.github.hdcharts.core.internal.ChartValidationInputs
import io.github.hdcharts.core.internal.InternalChartsApi
import io.github.hdcharts.core.internal.NO_SELECTION
import io.github.hdcharts.core.internal.TestTags
import io.github.hdcharts.core.internal.ValidationErrors
import io.github.hdcharts.core.internal.composable.ChartPlotLayout
import io.github.hdcharts.core.internal.composable.Legend
import io.github.hdcharts.core.internal.layout.modifierTopTitle
import io.github.hdcharts.core.internal.selectedTitle
import io.github.hdcharts.core.model.ChartData
import io.github.hdcharts.core.model.ChartSelection
import io.github.hdcharts.core.model.ChartValueFormatter
import io.github.hdcharts.gauge.RingGaugeChartStyle
import io.github.hdcharts.gauge.RingGaugeRingsStyle
import io.github.hdcharts.gauge.clamp
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

/** A half circle is twice as wide as it is tall. */
private const val GAUGE_PLOT_ASPECT_RATIO = 2f

/**
 * Everything [io.github.hdcharts.gauge.RingGaugeChart] declares about its input. A ring gauge is one
 * series whose values are the rings, so it needs exactly one series and at least one value. Values
 * are placed in the style's fixed range, which may be negative, and it has no Cartesian axis.
 */
@InternalChartsApi
object RingGaugeChartSpec : ChartSpec<RingGaugeChartStyle> {
    override val policy =
        ChartPolicy(
            minValues = ValidationErrors.MIN_RING_GAUGE_VALUES,
            allowNegative = true,
            stacksValues = false,
            singleSeries = true,
            hasAxis = false,
            hasFixedRange = true,
            // A single-series chart counts its colors against its value count, so it states no rule.
            colorsMatch = null,
        )

    /** A ring gauge has no Cartesian axis, so there are no axis label styles to check. */
    override fun validationInputs(style: RingGaugeChartStyle): ChartValidationInputs =
        ChartValidationInputs(
            colorCount = style.rings.colors.size,
            rangeMin = style.range.min,
            rangeMax = style.range.max,
            xLabels = null,
            yLabels = null,
        )

    override fun clamp(
        style: RingGaugeChartStyle,
        density: Density,
    ): RingGaugeChartStyle = style.clamp(density)
}

/**
 * Runs [RingGaugeChartSpec] through the shared seam, then lays out the title, the gauge, and the
 * legend. The selected index is resolved by the caller, because it reads the public data.
 */
@Composable
internal fun RingGaugeChartEntry(
    data: ChartData,
    modifier: Modifier,
    style: RingGaugeChartStyle,
    title: String?,
    selectedValueFormatter: ChartValueFormatter,
    axisValueFormatter: ChartValueFormatter,
    selectedIndex: Int,
    selection: ChartSelection,
    interactionEnabled: Boolean,
    animateOnStart: Boolean,
) {
    ChartEntry(
        spec = RingGaugeChartSpec,
        data = data,
        style = style,
        errorStyle = style.chartContainerStyle,
        title = title,
        content = { renderData, drawStyle ->
            val values = renderData.series.single().values
            val categories = renderData.categories
            val colors =
                remember(drawStyle.rings, values.size) {
                    resolveRingColors(style = drawStyle.rings, ringCount = values.size)
                }
            // One series: the title carries the selected value (Legend and Selection, rule 5).
            val displayedTitle =
                selectedTitle(
                    data = renderData.data,
                    selectedIndex = selectedIndex,
                    title = title,
                    selectedValueFormatter = selectedValueFormatter,
                )

            ChartPlotLayout(
                plotAspectRatio = GAUGE_PLOT_ASPECT_RATIO,
                modifier = modifier,
                title = {
                    if (displayedTitle.isNotBlank()) {
                        Text(
                            modifier =
                                drawStyle.chartContainerStyle.modifierTopTitle
                                    .testTag(TestTags.CHART_TITLE),
                            text = displayedTitle,
                            style = drawStyle.chartContainerStyle.styleTitle,
                        )
                    }
                },
                legend = {
                    // The legend names the rings, like pie names its slices.
                    Legend(
                        chartContainerStyle = drawStyle.chartContainerStyle,
                        style = drawStyle.legend,
                        legend = categories,
                        colors = colors,
                    )
                },
                plot = {
                    RingGaugeChartContent(
                        values = values,
                        colors = colors,
                        style = drawStyle,
                        minLabel = axisValueFormatter.format(drawStyle.range.min),
                        maxLabel = axisValueFormatter.format(drawStyle.range.max),
                        interactionEnabled = interactionEnabled,
                        animateOnStart = animateOnStart,
                        selectedIndex = selectedIndex,
                        onRingTouched = { index ->
                            if (index == NO_SELECTION) selection.clear() else selection.select(index)
                        },
                    )
                },
            )
        },
        modifier = modifier,
    )
}

/** The colors for [ringCount] rings, with the style's alpha applied to each one. */
private fun resolveRingColors(
    style: RingGaugeRingsStyle,
    ringCount: Int,
): ImmutableList<Color> =
    style
        .resolveColors(ringCount)
        .map { color -> color.copy(alpha = style.alpha) }
        .toImmutableList()
