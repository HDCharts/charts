package io.github.hdcharts.core.internal.axis

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import io.github.hdcharts.core.internal.AXIS_LABEL_CHART_GAP
import io.github.hdcharts.core.internal.InternalChartsApi
import io.github.hdcharts.core.model.ChartValueFormatter
import io.github.hdcharts.core.style.AxisLabelStyle
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.roundToInt

// Space between the Y-axis labels and the right edge of their column, next to the plot.
private val Y_AXIS_LABEL_EDGE_PADDING = 4.dp

// Y-axis labels never take more than this share of the chart width.
private const val Y_AXIS_MAX_WIDTH_SHARE = 0.4f

// The space kept past each plot edge for the X-axis edge labels never takes more than this share of
// the chart width. Longer labels hang past the chart padding instead.
private const val X_AXIS_EDGE_INSET_MAX_WIDTH_SHARE = 0.2f

// The plot keeps at least this much width when the Y-axis labels are wide.
private const val MIN_PLOT_WIDTH_PX = 1f

/** An X-axis [label] and the [centerX] of its tick, in pixels from the left edge of the label row. */
@InternalChartsApi
data class AxisXLayoutTick(
    val label: String,
    val centerX: Float,
)

/** X-axis ticks for [labelIndices]. Item `index` sits at `firstTickPx + index * unitWidthPx`, minus the scroll. */
@InternalChartsApi
fun buildXAxisLayoutTicks(
    labels: List<String>,
    labelIndices: List<Int>,
    unitWidthPx: Float,
    firstTickPx: Float,
    scrollOffsetPx: Float,
): List<AxisXLayoutTick> =
    labelIndices.map { index ->
        AxisXLayoutTick(
            label = resolveAxisLabel(labels = labels, index = index),
            centerX = firstTickPx + index * unitWidthPx - scrollOffsetPx,
        )
    }

/** A Y-axis [label] and the [centerY] of its tick, in pixels from the top of the plot. */
@InternalChartsApi
data class AxisYLayoutTick(
    val label: String,
    val centerY: Float,
)

/** Y-axis [ticks], the [widthPx] the labels need and the [gapPx] between them and the plot. */
@InternalChartsApi
class AxisYLayout(
    val ticks: List<AxisYLayoutTick>,
    val widthPx: Float,
    val gapPx: Float,
)

/**
 * Width of the Y-axis label column in whole pixels: the widest of [labels], but never more than 40%
 * of [availableWidthPx]. Wider labels are cut off.
 */
@InternalChartsApi
fun yAxisLabelColumnWidthPx(
    labels: List<String>,
    fontSizePx: Float,
    availableWidthPx: Int,
): Float =
    estimateYAxisLabelWidthPx(labels = labels, fontSizePx = fontSizePx)
        .coerceIn(0f, availableWidthPx.coerceAtLeast(0) * Y_AXIS_MAX_WIDTH_SHARE)
        .roundToInt()
        .toFloat()

/**
 * Numeric Y-axis ticks, label column width and plot gap for a chart [availableWidthPx] wide. Ticks
 * run from [maxValue] at the top to [minValue] at the bottom of a plot [chartHeightPx] tall, kept
 * [verticalInsetPx] from its edges. The tick count follows [yAxisTickCount], so a shorter plot shows
 * fewer ticks. Hidden labels take no ticks and no width.
 */
@Composable
@InternalChartsApi
fun rememberNumericYAxisLayout(
    labels: AxisLabelStyle,
    minValue: Double,
    maxValue: Double,
    chartHeightPx: Float,
    verticalInsetPx: Float,
    formatter: ChartValueFormatter,
    availableWidthPx: Int,
): AxisYLayout {
    val density = LocalDensity.current
    val fontSizePx = with(density) { labels.size.toPx() }
    val tickCount =
        yAxisTickCount(
            maxCount = labels.maxCount,
            spanPx = chartHeightPx - 2f * verticalInsetPx.coerceIn(0f, chartHeightPx.coerceAtLeast(0f) / 2f),
            fontSizePx = fontSizePx,
        )
    val ticks =
        remember(labels.visible, minValue, maxValue, chartHeightPx, tickCount, verticalInsetPx, formatter) {
            if (labels.visible) {
                buildNumericYAxisTicks(
                    minValue = minValue,
                    maxValue = maxValue,
                    labelCount = tickCount,
                    plotHeightPx = chartHeightPx,
                    verticalInsetPx = verticalInsetPx,
                    formatter = formatter,
                )
            } else {
                emptyList()
            }
        }
    val chartGapPx = with(density) { AXIS_LABEL_CHART_GAP.roundToPx().toFloat() }
    // Scrolling charts recompose on every scrolled pixel, so the width is only measured again when the
    // ticks or the space change.
    return remember(ticks, labels.visible, fontSizePx, availableWidthPx, chartGapPx) {
        if (!labels.visible) {
            AxisYLayout(ticks = ticks, widthPx = 0f, gapPx = 0f)
        } else {
            val widthPx =
                yAxisLabelColumnWidthPx(
                    labels = ticks.map { tick -> tick.label },
                    fontSizePx = fontSizePx,
                    availableWidthPx = availableWidthPx,
                )
            val gapPx = chartGapPx.coerceAtMost((availableWidthPx - widthPx - MIN_PLOT_WIDTH_PX).coerceAtLeast(0f))
            AxisYLayout(ticks = ticks, widthPx = widthPx, gapPx = gapPx)
        }
    }
}

/**
 * Height of the X-axis label row: the estimated label height of [extent] plus the gap above the
 * labels, which [AxisXLabelsLayout] leaves before centering them.
 */
@InternalChartsApi
fun Density.xAxisLabelRowHeightPx(extent: AxisXLabelExtent): Float = extent.heightPx + AXIS_LABEL_CHART_GAP.toPx()

/**
 * Whole pixels a plot keeps free past an edge where an item sits, so the longest label of [extent],
 * centered on that item, fits in the [edgeSlackPx] already beyond the plot, such as the chart padding.
 * It never takes more than 20% of [availableWidthPx], so long labels cannot squeeze the plot.
 */
@InternalChartsApi
fun xAxisLabelEdgeInsetPx(
    extent: AxisXLabelExtent,
    edgeSlackPx: Float,
    availableWidthPx: Int,
): Float =
    ceil((extent.halfWidthPx - edgeSlackPx).coerceAtLeast(0f))
        .coerceAtMost(floor(availableWidthPx.coerceAtLeast(0) * X_AXIS_EDGE_INSET_MAX_WIDTH_SHARE))

/**
 * Draws tilted X-axis labels, each centered on its tick, with all label centers on one line.
 * [tickOffsetPx] moves every tick, such as while a live window slides. It is read only when the
 * labels are placed, so a change moves them without recomposing.
 */
@Composable
@InternalChartsApi
fun AxisXLabelsLayout(
    ticks: List<AxisXLayoutTick>,
    color: Color,
    fontSize: TextUnit,
    modifier: Modifier = Modifier,
    tickOffsetPx: () -> Float = { 0f },
) {
    Layout(
        modifier = modifier,
        content = {
            ticks.forEach { tick ->
                Text(
                    text = tick.label,
                    style = TextStyle(color = color, fontSize = fontSize),
                    maxLines = 1,
                    modifier =
                        Modifier.graphicsLayer {
                            rotationZ = -X_AXIS_LABEL_TILT_DEGREES
                            transformOrigin = TransformOrigin.Center
                        },
                )
            }
        },
    ) { measurables, constraints ->
        val placeables =
            measurables.map { measurable ->
                measurable.measure(
                    Constraints(
                        minWidth = 0,
                        minHeight = 0,
                        maxWidth = constraints.maxWidth,
                        maxHeight = constraints.maxHeight,
                    ),
                )
            }
        // Labels share one center line: the middle of the row below the plot gap.
        val topPx = AXIS_LABEL_CHART_GAP.toPx()
        val centerY = topPx + (constraints.maxHeight - topPx) / 2f

        layout(constraints.maxWidth, constraints.maxHeight) {
            val offsetPx = tickOffsetPx()
            placeables.forEachIndexed { index, placeable ->
                val tick = ticks.getOrNull(index) ?: return@forEachIndexed
                val position =
                    placeXAxisLabel(
                        tickX = tick.centerX + offsetPx,
                        labelWidthPx = placeable.width,
                        labelHeightPx = placeable.height,
                        rowWidthPx = constraints.maxWidth,
                        centerY = centerY,
                    ) ?: return@forEachIndexed
                placeable.place(position)
            }
        }
    }
}

/**
 * Draws Y-axis labels right-aligned, each vertically centered on its tick and kept inside the
 * plot height. Ticks outside the plot height, beyond 1 px of rounding slack, are skipped.
 */
@Composable
@InternalChartsApi
fun AxisYLabelsLayout(
    ticks: List<AxisYLayoutTick>,
    color: Color,
    fontSize: TextUnit,
    modifier: Modifier = Modifier,
) {
    Layout(
        modifier = modifier,
        content = {
            ticks.forEach { tick ->
                Text(
                    text = tick.label,
                    style = TextStyle(color = color, fontSize = fontSize),
                    maxLines = 1,
                )
            }
        },
    ) { measurables, constraints ->
        val placeables =
            measurables.map { measurable ->
                measurable.measure(
                    Constraints(
                        minWidth = 0,
                        minHeight = 0,
                        maxWidth = constraints.maxWidth,
                        maxHeight = constraints.maxHeight,
                    ),
                )
            }
        val edgePadding = Y_AXIS_LABEL_EDGE_PADDING.roundToPx()

        layout(constraints.maxWidth, constraints.maxHeight) {
            placeables.forEachIndexed { index, placeable ->
                val tick = ticks.getOrNull(index) ?: return@forEachIndexed
                val position =
                    placeYAxisLabel(
                        tickY = tick.centerY,
                        labelWidthPx = placeable.width,
                        labelHeightPx = placeable.height,
                        columnWidthPx = constraints.maxWidth,
                        columnHeightPx = constraints.maxHeight,
                        edgePaddingPx = edgePadding,
                    ) ?: return@forEachIndexed
                placeable.place(position)
            }
        }
    }
}
