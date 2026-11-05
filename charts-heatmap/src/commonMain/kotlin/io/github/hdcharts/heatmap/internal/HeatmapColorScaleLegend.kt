package io.github.hdcharts.heatmap.internal

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import io.github.hdcharts.core.model.ChartValueFormatter
import io.github.hdcharts.core.style.AxisLabelStyle
import io.github.hdcharts.core.style.StyleDefaults
import kotlin.math.roundToInt

internal const val HEATMAP_CHART_LEGEND_TAG = "HeatmapChartLegend"

// The selected-value marker reaches this far above and below the legend bar.
private val MARKER_OVERHANG = 3.dp

// Smallest gap between two legend labels; a label that would come closer is left out.
private val LABEL_GAP = 8.dp

/** A legend label at [fraction] of the color scale. */
internal data class HeatmapLegendLabel(
    val fraction: Float,
    val text: String,
)

/**
 * Labels for the color-scale legend: the value at every color stop, so the ends show the lowest and
 * highest values. A constant domain has one value, shown once where its color sits on the scale.
 */
internal fun heatmapLegendLabels(
    mapping: HeatmapColorMapping,
    formatter: ChartValueFormatter,
): List<HeatmapLegendLabel> {
    if (mapping.isConstant) {
        return listOf(HeatmapLegendLabel(mapping.constantFraction, formatter.format(mapping.min)))
    }
    return mapping.knots.map { knot ->
        val value = (mapping.min * (1.0 - knot) + mapping.max * knot).coerceIn(mapping.min, mapping.max)
        HeatmapLegendLabel(knot, formatter.format(value))
    }
}

/**
 * A gradient bar with the scale's colors, value labels at its color stops, and a marker at the
 * selected cell's value.
 */
@Composable
internal fun HeatmapColorScaleLegend(
    mapping: HeatmapColorMapping,
    valueFormatter: ChartValueFormatter,
    labelStyle: AxisLabelStyle,
    selectedValue: Double?,
    markerColor: Color,
    modifier: Modifier = Modifier,
) {
    val labels = remember(mapping, valueFormatter) { heatmapLegendLabels(mapping, valueFormatter) }
    val brush =
        remember(mapping) {
            Brush.horizontalGradient(
                colorStops = mapping.knots.zip(mapping.colors).toTypedArray(),
            )
        }
    val markerFraction = selectedValue?.let(mapping::fraction)
    Column(modifier = modifier.widthIn(max = StyleDefaults.heatmapLegendBarWidth).testTag(HEATMAP_CHART_LEGEND_TAG)) {
        Canvas(
            Modifier
                .fillMaxWidth()
                .height(StyleDefaults.heatmapLegendBarHeight + MARKER_OVERHANG * 2),
        ) {
            val overhang = MARKER_OVERHANG.toPx()
            val barHeight = size.height - overhang * 2f
            drawRoundRect(
                brush = brush,
                topLeft = Offset(0f, overhang),
                size = Size(size.width, barHeight),
                cornerRadius = CornerRadius(barHeight / 4f),
            )
            val markerWidth = StyleDefaults.heatmapSelectionWidth.toPx()
            if (markerFraction != null && size.width >= markerWidth) {
                val x = (markerFraction * size.width).coerceIn(markerWidth / 2f, size.width - markerWidth / 2f)
                drawLine(markerColor, Offset(x, 0f), Offset(x, size.height), strokeWidth = markerWidth)
            }
        }
        HeatmapLegendLabels(
            labels = labels,
            textStyle = TextStyle(color = labelStyle.color, fontSize = labelStyle.size),
            modifier = Modifier.fillMaxWidth().padding(top = 2.dp),
        )
    }
}

/**
 * Centers each label on its scale position, kept inside the bar's width. The end labels are placed
 * first; an inner label that would overlap a placed one is left out.
 */
@Composable
private fun HeatmapLegendLabels(
    labels: List<HeatmapLegendLabel>,
    textStyle: TextStyle,
    modifier: Modifier = Modifier,
) {
    Layout(
        modifier = modifier,
        content = { labels.forEach { label -> Text(text = label.text, style = textStyle, maxLines = 1) } },
    ) { measurables, constraints ->
        val width = constraints.maxWidth
        val placeables = measurables.map { it.measure(Constraints(maxWidth = width)) }
        val gap = LABEL_GAP.roundToPx()
        val order =
            if (labels.size <= 2) labels.indices.toList() else listOf(0, labels.lastIndex) + (1 until labels.lastIndex)
        val placed = mutableListOf<Pair<Int, IntRange>>()
        order.forEach { index ->
            val placeable = placeables[index]
            val x =
                (labels[index].fraction * width - placeable.width / 2f)
                    .roundToInt()
                    .coerceIn(0, (width - placeable.width).coerceAtLeast(0))
            val span = (x - gap)..(x + placeable.width + gap)
            if (placed.none { (_, other) -> span.first < other.last && other.first < span.last }) {
                placed += index to (x..(x + placeable.width))
            }
        }
        val height = placeables.maxOfOrNull { it.height } ?: 0
        layout(width, height) {
            placed.forEach { (index, range) -> placeables[index].place(range.first, 0) }
        }
    }
}
