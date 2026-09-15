@file:OptIn(InternalChartsApi::class)

package io.github.hdcharts.charts.internal.common.layout

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.LayoutModifier
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.MeasureResult
import androidx.compose.ui.layout.MeasureScope
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.constrainHeight
import androidx.compose.ui.unit.constrainWidth
import androidx.compose.ui.unit.dp
import io.github.hdcharts.charts.internal.InternalChartsApi
import io.github.hdcharts.charts.style.ChartContainerStyle

private val DefaultChartPlotSize = 200.dp

@InternalChartsApi
val ChartContainerStyle.modifierTopTitle: Modifier
    get() = Modifier.padding(top = contentPadding, start = contentPadding)

@InternalChartsApi
val ChartContainerStyle.modifierLegend: Modifier
    get() =
        Modifier
            .wrapContentSize()
            .padding(start = contentPadding, end = contentPadding, bottom = contentPadding)

@InternalChartsApi
fun wrapContentChartModifier(
    style: ChartContainerStyle,
    contentPadding: Dp = style.contentPadding,
): Modifier =
    Modifier
        .wrapContentSize()
        .padding(contentPadding)
        .then(AdaptiveChartSizeModifier)

@InternalChartsApi
fun fillMaxSizeChartModifier(
    style: ChartContainerStyle,
    contentPadding: Dp = style.contentPadding,
): Modifier =
    Modifier
        .padding(contentPadding)
        .then(AdaptiveChartSizeModifier)

/**
 * Resolves the chart plot's width and height from caller constraints.
 *
 * The chart composable's `modifier` is the only sizing control callers need. This modifier turns
 * that modifier into a finite plot rectangle so renderers always see a bounded size and never
 * produce an unbounded-height layout (the failure mode the old `Modifier.aspectRatio(1f)` masked).
 *
 * Sizing policy:
 *
 * - When the caller bounds both width and height, use that rectangle as-is.
 * - When the caller bounds only one axis (typical inside `verticalScroll` or `Row`s), derive a
 *   square from the bounded axis so the plot stays visible without consuming the unbounded axis.
 * - When neither axis is bounded (no modifier or fully wrapping parents), fall back to
 *   [DefaultChartPlotSize] so the chart still renders at a useful size.
 *
 * The final dimensions are clamped to the incoming `Constraints` so this modifier respects both
 * the minimum and maximum dimensions required by the caller.
 */
@InternalChartsApi
internal object AdaptiveChartSizeModifier : LayoutModifier {
    override fun MeasureScope.measure(
        measurable: Measurable,
        constraints: Constraints,
    ): MeasureResult {
        val fallbackSize = DefaultChartPlotSize.roundToPx()

        // Prefer the bounded axis when only one is bounded; otherwise use the documented fallback.
        val width =
            when {
                constraints.hasBoundedWidth -> constraints.maxWidth
                constraints.hasBoundedHeight -> constraints.maxHeight
                else -> fallbackSize
            }
        val height =
            when {
                constraints.hasBoundedHeight -> constraints.maxHeight
                constraints.hasBoundedWidth -> constraints.maxWidth
                else -> fallbackSize
            }

        val measuredWidth = constraints.constrainWidth(width)
        val measuredHeight = constraints.constrainHeight(height)

        val placeable = measurable.measure(Constraints.fixed(measuredWidth, measuredHeight))
        return layout(measuredWidth, measuredHeight) {
            placeable.place(0, 0)
        }
    }
}
