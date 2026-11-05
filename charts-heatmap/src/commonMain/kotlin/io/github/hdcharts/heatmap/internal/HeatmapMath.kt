package io.github.hdcharts.heatmap.internal

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.colorspace.ColorSpaces
import io.github.hdcharts.core.internal.NO_SELECTION
import io.github.hdcharts.core.internal.model.normalizeValue
import io.github.hdcharts.core.internal.model.resolveOptionalRange
import io.github.hdcharts.heatmap.HeatmapColorScale
import io.github.hdcharts.heatmap.HeatmapData
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

/** Returns the value at [row] and [column], or null for a missing or out-of-matrix cell. */
internal fun HeatmapData.valueAt(
    row: Int,
    column: Int,
): Double? = values.getOrNull(row)?.getOrNull(column)

/** Returns the value of the cell with selection [index], or null for a missing or out-of-matrix cell. */
internal fun HeatmapData.valueAtIndex(index: Int): Double? {
    if (columns.isEmpty() || index < 0) return null
    return valueAt(row = index / columns.size, column = index % columns.size)
}

/**
 * Value-to-color mapping with resolved bounds. Colors are evenly spaced stops on `0..1`; values
 * normalize onto that range and clamp at both ends. When the domain has no width, every value uses
 * [constantFraction].
 */
internal class HeatmapColorMapping(
    val min: Double,
    val max: Double,
    colors: List<Color>,
    val constantFraction: Float = 0.5f,
) {
    val colors: List<Color> = colors.map { it.convert(ColorSpaces.Srgb) }

    /** Stop positions of [colors], from `0` to `1`. */
    val knots: List<Float> = this.colors.indices.map { it / (this.colors.size - 1).toFloat() }

    val isConstant: Boolean get() = min == max

    /** Scale position of [value], clamped to `0..1`. */
    fun fraction(value: Double): Float = normalizeValue(value, min, max, constantFraction)

    /** Scale position of [value] before clamping, so a linear value change is a linear position change. */
    fun unclampedFraction(value: Double): Double {
        if (isConstant) return constantFraction.toDouble()
        val range = max - min
        if (range.isFinite()) return (value - min) / range
        // Finite bounds can have an infinite span. Scaling first keeps both differences finite.
        val scale = maxOf(abs(min), abs(max))
        return (value / scale - min / scale) / (max / scale - min / scale)
    }

    fun color(value: Double): Color = colorAt(fraction(value))

    /** Blends linearly in sRGB between the two stops around [fraction]. */
    fun colorAt(fraction: Float): Color {
        val position = fraction.coerceIn(0f, 1f) * (colors.size - 1)
        val lower = floor(position).toInt().coerceIn(0, colors.size - 2)
        return lerpSrgb(colors[lower], colors[lower + 1], position - lower)
    }
}

/**
 * Resolves [scale] against the present values of a validated [data]. A constant automatic domain uses
 * the middle of the scale. A single fixed bound that every value reaches or passes collapses the
 * domain onto that bound, and every value then uses that bound's end color.
 */
internal fun heatmapColorMapping(
    data: HeatmapData,
    scale: HeatmapColorScale,
): HeatmapColorMapping {
    var dataMin = Double.POSITIVE_INFINITY
    var dataMax = Double.NEGATIVE_INFINITY
    data.values.forEach { row ->
        row.forEach { value ->
            if (value != null) {
                dataMin = min(dataMin, value)
                dataMax = max(dataMax, value)
            }
        }
    }
    val (resolvedMin, resolvedMax) = resolveOptionalRange(dataMin, dataMax, scale.min, scale.max)
    val constantFraction =
        when {
            scale.min != null && scale.max == null -> 0f
            scale.max != null && scale.min == null -> 1f
            else -> 0.5f
        }
    return HeatmapColorMapping(
        min = resolvedMin,
        max = resolvedMax,
        colors = scale.colors,
        constantFraction = constantFraction,
    )
}

internal fun lerpSrgb(
    start: Color,
    end: Color,
    fraction: Float,
): Color {
    val t = fraction.coerceIn(0f, 1f)
    return Color(
        red = start.red + (end.red - start.red) * t,
        green = start.green + (end.green - start.green) * t,
        blue = start.blue + (end.blue - start.blue) * t,
        alpha = start.alpha + (end.alpha - start.alpha) * t,
    )
}

/** Linear interpolation that stays between [start] and [end], so huge finite values cannot overflow. */
private fun lerpValue(
    start: Double,
    end: Double,
    fraction: Double,
): Double = (start * (1.0 - fraction) + end * fraction).coerceIn(min(start, end), max(start, end))

/** Indices of the two cell centers around [centerPosition], clamped to the matrix edge. */
private fun neighborIndices(
    centerPosition: Double,
    count: Int,
): Pair<Int, Int> {
    val first = floor(centerPosition).toInt().coerceIn(0, count - 1)
    return first to (first + 1).coerceAtMost(count - 1)
}

/** Position of [centerPosition] between neighbors [first] and [second], or `0` when they are one cell. */
private fun neighborFraction(
    centerPosition: Double,
    first: Int,
    second: Int,
): Double = if (first == second) 0.0 else (centerPosition - first).coerceIn(0.0, 1.0)

/**
 * The interpolated field at ([x], [y]), measured in cells from the plot's top-left corner, so cell
 * `(row, column)` covers `column..column + 1` and `row..row + 1`.
 *
 * Each value sits at its cell's center, so the field equals every value there. Between four
 * neighboring centers the field is a bicubic Hermite patch whose center slopes are limited to keep
 * it shape-preserving (Fritsch-Butland): it is smooth across cell centers and edges, never leaves the
 * range of the four values around it, and adds no peak or valley the values do not have. Beyond the
 * outermost centers it extends the edge values. A point in a missing cell has no value.
 *
 * When some of the four neighbors are missing, the weights of the present neighbors are rescaled to
 * sum to one, so the field blends only real values and stays continuous up to the edge of the
 * missing cell. A point's own cell always carries at least a quarter of the weight.
 */
internal fun heatmapInterpolatedValue(
    data: HeatmapData,
    x: Double,
    y: Double,
): Double? {
    val columnCount = data.columns.size
    val rowCount = data.rows.size
    val column = floor(x).toInt().coerceIn(0, columnCount - 1)
    val row = floor(y).toInt().coerceIn(0, rowCount - 1)
    val own = data.valueAt(row, column) ?: return null
    val (column0, column1) = neighborIndices(x - 0.5, columnCount)
    val (row0, row1) = neighborIndices(y - 0.5, rowCount)
    return smoothBlend(
        data = data,
        row0 = row0,
        row1 = row1,
        rowFraction = neighborFraction(y - 0.5, row0, row1),
        column0 = column0,
        column1 = column1,
        columnFraction = neighborFraction(x - 0.5, column0, column1),
    ) ?: own
}

// Cubic Hermite basis on 0..1: the weights of the start value, end value, start slope and end slope.
private fun startValueWeight(t: Double) = (1.0 + 2.0 * t) * (1.0 - t) * (1.0 - t)

private fun endValueWeight(t: Double) = t * t * (3.0 - 2.0 * t)

private fun startSlopeWeight(t: Double) = t * (1.0 - t) * (1.0 - t)

private fun endSlopeWeight(t: Double) = -t * t * (1.0 - t)

/**
 * Slope at a center along one axis from its present neighbors, in value units per cell. Two neighbors
 * on the same side of the value give their harmonic mean (Fritsch-Butland), which keeps the curve
 * between centers monotone; a local peak or valley, or a neighbor on each side moving the other way,
 * gives zero. A single neighbor gives its secant, so straight runs stay straight.
 */
private fun monotoneSlope(
    previous: Double?,
    value: Double,
    next: Double?,
): Double {
    val before = previous?.let { value - it }
    val after = next?.let { it - value }
    return when {
        before != null && after != null ->
            if (before * after <= 0.0) 0.0 else 2.0 / (1.0 / before + 1.0 / after)
        before != null -> before
        after != null -> after
        else -> 0.0
    }
}

/**
 * Shape-preserving bicubic blend of the present corners, with their weights rescaled to sum to one,
 * clamped to the range of those corners. Falls back to [blendedBilinear] when values are so large that
 * the slopes overflow. Returns null only when every corner with weight is missing.
 */
private fun smoothBlend(
    data: HeatmapData,
    row0: Int,
    row1: Int,
    rowFraction: Double,
    column0: Int,
    column1: Int,
    columnFraction: Double,
): Double? {
    var totalWeight = 0.0
    var blended = 0.0
    var low = Double.POSITIVE_INFINITY
    var high = Double.NEGATIVE_INFINITY
    for (corner in 0 until 4) {
        val atEndColumn = corner % 2 == 1
        val atEndRow = corner >= 2
        val row = if (atEndRow) row1 else row0
        val column = if (atEndColumn) column1 else column0
        val value = data.valueAt(row, column) ?: continue
        low = min(low, value)
        high = max(high, value)
        val columnWeight = if (atEndColumn) endValueWeight(columnFraction) else startValueWeight(columnFraction)
        val rowWeight = if (atEndRow) endValueWeight(rowFraction) else startValueWeight(rowFraction)
        val columnSlopeWeight = if (atEndColumn) endSlopeWeight(columnFraction) else startSlopeWeight(columnFraction)
        val rowSlopeWeight = if (atEndRow) endSlopeWeight(rowFraction) else startSlopeWeight(rowFraction)
        val columnSlope = monotoneSlope(data.valueAt(row, column - 1), value, data.valueAt(row, column + 1))
        val rowSlope = monotoneSlope(data.valueAt(row - 1, column), value, data.valueAt(row + 1, column))
        totalWeight += columnWeight * rowWeight
        blended += columnWeight * rowWeight * value +
            columnSlopeWeight * rowWeight * columnSlope +
            columnWeight * rowSlopeWeight * rowSlope
    }
    if (totalWeight <= 0.0) return null
    val result = blended / totalWeight
    if (!result.isFinite()) {
        return blendedBilinear(data, row0, row1, rowFraction, column0, column1, columnFraction)
    }
    return result.coerceIn(low, high)
}

/** Bilinear value of four present corners, or null when any corner is missing. */
private fun bilinear(
    data: HeatmapData,
    row0: Int,
    row1: Int,
    rowFraction: Double,
    column0: Int,
    column1: Int,
    columnFraction: Double,
): Double? {
    val topLeft = data.valueAt(row0, column0) ?: return null
    val topRight = data.valueAt(row0, column1) ?: return null
    val bottomLeft = data.valueAt(row1, column0) ?: return null
    val bottomRight = data.valueAt(row1, column1) ?: return null
    val top = lerpValue(topLeft, topRight, columnFraction)
    val bottom = lerpValue(bottomLeft, bottomRight, columnFraction)
    return lerpValue(top, bottom, rowFraction)
}

/**
 * Bilinear blend of the present corners, with their weights rescaled to sum to one. Equals [bilinear]
 * when all four corners are present. Returns null only when every corner with weight is missing.
 */
private fun blendedBilinear(
    data: HeatmapData,
    row0: Int,
    row1: Int,
    rowFraction: Double,
    column0: Int,
    column1: Int,
    columnFraction: Double,
): Double? {
    bilinear(data, row0, row1, rowFraction, column0, column1, columnFraction)?.let { return it }

    val corners =
        listOf(
            data.valueAt(row0, column0) to (1.0 - rowFraction) * (1.0 - columnFraction),
            data.valueAt(row0, column1) to (1.0 - rowFraction) * columnFraction,
            data.valueAt(row1, column0) to rowFraction * (1.0 - columnFraction),
            data.valueAt(row1, column1) to rowFraction * columnFraction,
        ).filter { (value, weight) -> value != null && weight > 0.0 }
    val totalWeight = corners.sumOf { it.second }
    if (corners.isEmpty() || totalWeight <= 0.0) return null
    var low = Double.POSITIVE_INFINITY
    var high = Double.NEGATIVE_INFINITY
    var blended = 0.0
    corners.forEach { (value, weight) ->
        val present = value!!
        low = min(low, present)
        high = max(high, present)
        // Weighting each value before summing keeps huge finite values from overflowing.
        blended += present * (weight / totalWeight)
    }
    return blended.coerceIn(low, high)
}

/** A horizontal band of the interpolated field, drawn with one gradient across the plot width. */
internal class HeatmapStrip(
    val top: Float,
    val height: Float,
    /** Gradient stops as fractions of the plot width, in increasing order. */
    val stops: List<Pair<Float, Color>>,
)

// Target strip height in pixels; thinner strips hide vertical steps between rows of the field.
private const val STRIP_HEIGHT_PX = 2f

// Upper bound on strips, so a very tall plot keeps a bounded drawing cost.
private const val MAX_STRIPS = 1024

// Gradient segments per half cell. The field is a smooth cubic there, so a few straight segments
// follow it to well under one 8-bit color step.
private const val SAMPLES_PER_HALF_CELL = 4

/**
 * Splits the interpolated field of a validated [data] into horizontal strips for [layout].
 *
 * Within a strip, the field is evaluated at the strip's vertical center. Along the strip, each half
 * cell is sampled at [SAMPLES_PER_HALF_CELL] segments, with an extra stop wherever the value crosses a
 * color-scale stop, so the gradient follows both the field and the color scale. Missing cells are
 * transparent, and strip edges sit on whole pixels so strips leave no seams.
 */
internal fun heatmapInterpolatedStrips(
    data: HeatmapData,
    mapping: HeatmapColorMapping,
    layout: HeatmapPlotLayout,
): List<HeatmapStrip> {
    val rowCount = data.rows.size
    val stripsPerRow =
        ceil(layout.cellHeight / STRIP_HEIGHT_PX)
            .toInt()
            .coerceIn(1, (MAX_STRIPS / rowCount).coerceAtLeast(1))
    val strips = ArrayList<HeatmapStrip>(rowCount * stripsPerRow)
    for (row in 0 until rowCount) {
        for (part in 0 until stripsPerRow) {
            val startY = row + part / stripsPerRow.toDouble()
            val endY = row + (part + 1) / stripsPerRow.toDouble()
            val topPx = (layout.top + startY * layout.cellHeight).roundToInt().toFloat()
            val bottomPx = (layout.top + endY * layout.cellHeight).roundToInt().toFloat()
            if (bottomPx <= topPx) continue
            strips +=
                HeatmapStrip(
                    top = topPx,
                    height = bottomPx - topPx,
                    stops = heatmapStripStops(data, mapping, row, (startY + endY) / 2.0),
                )
        }
    }
    return strips
}

/** Gradient stops along the field's row at [y], which lies inside [row]. */
internal fun heatmapStripStops(
    data: HeatmapData,
    mapping: HeatmapColorMapping,
    row: Int,
    y: Double,
): List<Pair<Float, Color>> {
    val columnCount = data.columns.size
    val (row0, row1) = neighborIndices(y - 0.5, data.rows.size)
    val rowFraction = neighborFraction(y - 0.5, row0, row1)
    val stops = ArrayList<Pair<Float, Color>>(columnCount * (SAMPLES_PER_HALF_CELL + 2) * 2)

    // Each half cell has one owning cell and one set of neighbors, so one rule covers all of it.
    for (half in 0 until columnCount * 2) {
        val column = half / 2
        val startX = half * 0.5
        val endX = startX + 0.5
        val startOffset = (startX / columnCount).toFloat()
        val endOffset = (endX / columnCount).toFloat()
        val own = data.valueAt(row, column)
        if (own == null) {
            stops += startOffset to Color.Transparent
            stops += endOffset to Color.Transparent
            continue
        }
        val (column0, column1) = neighborIndices((startX + endX) / 2.0 - 0.5, columnCount)
        var previousOffset = startOffset
        var previousValue: Double = own
        for (sample in 0..SAMPLES_PER_HALF_CELL) {
            val x = startX + 0.5 * sample / SAMPLES_PER_HALF_CELL
            val offset = (x / columnCount).toFloat()
            val value =
                smoothBlend(
                    data,
                    row0,
                    row1,
                    rowFraction,
                    column0,
                    column1,
                    neighborFraction(x - 0.5, column0, column1),
                ) ?: own
            if (sample > 0) addKnotCrossings(stops, mapping, previousValue, value, previousOffset, offset)
            stops += offset to mapping.color(value)
            previousOffset = offset
            previousValue = value
        }
    }
    return stops
}

/**
 * Adds a stop wherever the value, moving linearly from [startValue] to [endValue], crosses a color
 * stop or a clamped end of the scale. Between those points the color is linear in position, which is
 * what a gradient draws.
 */
private fun addKnotCrossings(
    stops: MutableList<Pair<Float, Color>>,
    mapping: HeatmapColorMapping,
    startValue: Double,
    endValue: Double,
    startOffset: Float,
    endOffset: Float,
) {
    if (mapping.isConstant || startValue == endValue) return
    val startFraction = mapping.unclampedFraction(startValue)
    val endFraction = mapping.unclampedFraction(endValue)
    val knots = if (startFraction < endFraction) mapping.knots else mapping.knots.asReversed()
    knots.forEach { knot ->
        val t = (knot - startFraction) / (endFraction - startFraction)
        if (t > 0.0 && t < 1.0) {
            stops += (startOffset + (endOffset - startOffset) * t.toFloat()) to mapping.colorAt(knot)
        }
    }
}

/** Pixel bounds of the cell grid, shared by drawing and pointer hit testing. */
internal data class HeatmapPlotLayout(
    val left: Float,
    val top: Float,
    val width: Float,
    val height: Float,
    val columnCount: Int,
    val rowCount: Int,
) {
    val cellWidth: Float get() = width / columnCount
    val cellHeight: Float get() = height / rowCount
    val right: Float get() = left + width
    val bottom: Float get() = top + height

    /** The full slot of a cell; drawn cells are inset from it by half the cell spacing. */
    fun cellBounds(
        row: Int,
        column: Int,
    ): Rect =
        Rect(
            left = left + column * cellWidth,
            top = top + row * cellHeight,
            right = left + (column + 1) * cellWidth,
            bottom = top + (row + 1) * cellHeight,
        )

    /**
     * Returns the selection index of the cell under [position], or [NO_SELECTION] outside the grid.
     * Points on the grid's right or bottom edge belong to the last column or row, and the gaps between
     * drawn cells belong to the nearest cell.
     */
    fun cellIndexAt(position: Offset): Int {
        if (position.x < left || position.x > right || position.y < top || position.y > bottom) {
            return NO_SELECTION
        }
        val column = floor((position.x - left) / cellWidth).toInt().coerceIn(0, columnCount - 1)
        val row = floor((position.y - top) / cellHeight).toInt().coerceIn(0, rowCount - 1)
        return row * columnCount + column
    }
}

/**
 * A cell slot inset by half of [spacingPx] on each side, keeping at least one pixel of the slot so
 * large spacing on a dense matrix never hides cells.
 */
internal fun heatmapCellRect(
    slot: Rect,
    spacingPx: Float,
): Rect {
    val insetX = (spacingPx / 2f).coerceAtMost(((slot.width - 1f) / 2f).coerceAtLeast(0f))
    val insetY = (spacingPx / 2f).coerceAtMost(((slot.height - 1f) / 2f).coerceAtLeast(0f))
    return Rect(slot.left + insetX, slot.top + insetY, slot.right - insetX, slot.bottom - insetY)
}

/**
 * Places the cell grid inside a plot [width] by [height] pixels, after [leftSpace] for row labels and
 * [bottomSpace] for column labels. Edges sit on whole pixels and the grid keeps at least one pixel.
 */
internal fun heatmapPlotLayout(
    width: Float,
    height: Float,
    leftSpace: Float,
    topSpace: Float,
    rightSpace: Float,
    bottomSpace: Float,
    columnCount: Int,
    rowCount: Int,
): HeatmapPlotLayout {
    val left = ceil(leftSpace)
    val top = ceil(topSpace)
    return HeatmapPlotLayout(
        left = left,
        top = top,
        width = floor(width - left - rightSpace).coerceAtLeast(1f),
        height = floor(height - top - bottomSpace).coerceAtLeast(1f),
        columnCount = columnCount,
        rowCount = rowCount,
    )
}

/** Width-to-height ratio of the plot, following the matrix shape within [minRatio]..[maxRatio]. */
internal fun heatmapPlotAspectRatio(
    columnCount: Int,
    rowCount: Int,
    minRatio: Float,
    maxRatio: Float,
): Float = (columnCount.toFloat() / rowCount.coerceAtLeast(1)).coerceIn(minRatio, maxRatio)

/**
 * Every how-many rows a label fits: labels at least [minSpacingPx] apart, and at most [maxCount]
 * labels when it is set.
 */
internal fun heatmapRowLabelStride(
    rowCount: Int,
    cellHeightPx: Float,
    minSpacingPx: Float,
    maxCount: Int?,
): Int {
    if (rowCount <= 0) return 1
    val spacingStride = if (cellHeightPx > 0f) ceil(minSpacingPx / cellHeightPx).toInt() else rowCount
    val countStride = maxCount?.let { ceil(rowCount / it.toDouble()).toInt() } ?: 1
    return max(spacingStride, countStride).coerceIn(1, rowCount)
}

// Row labels never take more than this share of the chart width; longer labels are cut off.
private const val ROW_LABEL_MAX_WIDTH_SHARE = 0.4f

/**
 * Width of the row label column in whole pixels: the widest label plus [edgePaddingPx], but never more
 * than 40% of [availableWidthPx].
 */
internal fun heatmapRowLabelColumnWidth(
    textWidthPx: Float,
    edgePaddingPx: Float,
    availableWidthPx: Int,
): Float =
    ceil(textWidthPx + edgePaddingPx)
        .coerceIn(0f, floor(availableWidthPx.coerceAtLeast(0) * ROW_LABEL_MAX_WIDTH_SHARE))

/** A row or column label, or its 1-based number when the label is blank. */
internal fun heatmapLabel(
    labels: List<String>,
    index: Int,
): String = labels.getOrNull(index).orEmpty().ifBlank { (index + 1).toString() }
