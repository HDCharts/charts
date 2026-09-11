package io.github.dautovicharis.charts.internal.barchart

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.IntSize
import io.github.dautovicharis.charts.internal.common.axis.AxisLabelFootprintPx
import io.github.dautovicharis.charts.internal.common.axis.AxisXPlanRequest
import io.github.dautovicharis.charts.internal.common.axis.AxisXPlanResult
import io.github.dautovicharis.charts.internal.common.model.ChartData
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.roundToInt
import io.github.dautovicharis.charts.internal.common.axis.centeredLabelIndexRange as centeredLabelIndexRangeCore
import io.github.dautovicharis.charts.internal.common.axis.estimateXAxisLabelFootprintPx as estimateXAxisLabelFootprintPxCore
import io.github.dautovicharis.charts.internal.common.axis.estimateYAxisLabelWidthPx as estimateYAxisLabelWidthPxCore
import io.github.dautovicharis.charts.internal.common.axis.planAxisXLabels as planAxisXLabelsCore
import io.github.dautovicharis.charts.internal.common.axis.resolveAxisLabel as resolveAxisLabelCore
import io.github.dautovicharis.charts.internal.common.axis.sampledLabelIndices as sampledLabelIndicesCore
import io.github.dautovicharis.charts.internal.common.axis.scrollableLabelIndices as scrollableLabelIndicesCore
import io.github.dautovicharis.charts.internal.common.axis.visibleIndexRange as visibleIndexRangeCore
import io.github.dautovicharis.charts.internal.common.density.aggregateLabelsByCenterValue as aggregateLabelsByCenterValueCore
import io.github.dautovicharis.charts.internal.common.density.bucketSizeForTarget as bucketSizeForTargetCore
import io.github.dautovicharis.charts.internal.common.density.buildBucketRanges as buildBucketRangesCore
import io.github.dautovicharis.charts.internal.common.density.shouldUseScrollableDensity as shouldUseScrollableDensityCore

internal const val BAR_DENSE_THRESHOLD = 50

internal fun resolveAxisLabel(
    labels: List<String>,
    index: Int,
): String = resolveAxisLabelCore(labels = labels, index = index)

internal fun estimateXAxisLabelFootprintPx(
    labels: List<String>,
    dataSize: Int,
    fontSizePx: Float,
    tiltDegrees: Float,
): AxisLabelFootprintPx =
    estimateXAxisLabelFootprintPxCore(
        labels = labels,
        dataSize = dataSize,
        fontSizePx = fontSizePx,
        tiltDegrees = tiltDegrees,
    )

internal fun estimateYAxisLabelWidthPx(
    ticks: List<YAxisTick>,
    fontSizePx: Float,
): Float =
    estimateYAxisLabelWidthPxCore(
        labels = ticks.map { it.label },
        fontSizePx = fontSizePx,
    )

internal fun barYAxisWidthPx(
    ticks: List<YAxisTick>,
    fontSizePx: Float,
    availableWidthPx: Int,
): Float =
    estimateYAxisLabelWidthPx(ticks, fontSizePx)
        .coerceIn(0f, availableWidthPx.coerceAtLeast(0) * 0.4f)
        .roundToInt()
        .toFloat()

internal fun getSelectedIndex(
    position: Offset,
    dataSize: Int,
    canvasSize: IntSize,
    spacingPx: Float,
): Int =
    getSelectedIndexForContentX(
        contentX = position.x,
        dataSize = dataSize,
        unitWidthPx = (canvasSize.width + spacingPx) / dataSize.coerceAtLeast(1),
    )

internal fun getSelectedIndexForContentX(
    contentX: Float,
    dataSize: Int,
    unitWidthPx: Float,
): Int = if (dataSize <= 0 || unitWidthPx <= 0f) 0 else (contentX / unitWidthPx).toInt().coerceIn(0, dataSize - 1)

internal fun shouldUseScrollableDensity(pointsCount: Int): Boolean =
    shouldUseScrollableDensityCore(
        pointsCount = pointsCount,
        threshold = BAR_DENSE_THRESHOLD,
    )

internal fun aggregateForCompactDensity(
    data: ChartData,
    targetPoints: Int = BAR_DENSE_THRESHOLD,
): ChartData {
    val sourcePointsCount = data.points.size
    if (sourcePointsCount <= targetPoints.coerceAtLeast(1)) return data

    val bucketRanges = compactDensityRanges(sourcePointsCount, targetPoints)
    val aggregatedPoints =
        bucketRanges.map { range ->
            val sum = range.sumOf { data.points[it] }
            if (sum.isFinite()) {
                sum / range.count()
            } else {
                val scale = range.maxOf { abs(data.points[it]) }
                (range.sumOf { data.points[it] / scale } / range.count()).coerceIn(-1.0, 1.0) * scale
            }
        }
    val aggregatedLabels = aggregateLabelsByCenterValueCore(data.labels, bucketRanges)
    return ChartData(aggregatedLabels.zip(aggregatedPoints))
}

internal fun compactDensityRanges(
    sourcePointsCount: Int,
    targetPoints: Int,
): List<IntRange> {
    if (sourcePointsCount <= 0) return emptyList()
    val bucketSize = bucketSizeForTargetCore(sourcePointsCount, targetPoints.coerceAtLeast(1))
    return buildBucketRangesCore(sourcePointsCount, bucketSize)
}

internal fun compactDensityCenterIndices(
    sourcePointsCount: Int,
    targetPoints: Int = BAR_DENSE_THRESHOLD,
): List<Int> {
    val bucketRanges = compactDensityRanges(sourcePointsCount, targetPoints)
    return bucketRanges.map { range ->
        range.first + ((range.last - range.first) / 2)
    }
}

internal fun maxBarsThatFit(
    viewportWidthPx: Float,
    spacingPx: Float,
    minBarWidthPx: Float,
): Int {
    val safeViewportWidthPx = max(1f, viewportWidthPx)
    val safeSpacingPx = spacingPx.coerceAtLeast(0f)
    val safeMinBarWidthPx = minBarWidthPx.coerceAtLeast(1f)
    val unitWidthPx = safeMinBarWidthPx + safeSpacingPx
    return ((safeViewportWidthPx + safeSpacingPx) / unitWidthPx).toInt().coerceAtLeast(1)
}

internal fun unitWidth(
    barWidthPx: Float,
    spacingPx: Float,
): Float = max(Float.MIN_VALUE, barWidthPx + spacingPx)

internal fun contentWidth(
    dataSize: Int,
    unitWidthPx: Float,
    spacingPx: Float,
): Float {
    if (dataSize <= 0) return 0f
    return max(1f, dataSize * unitWidthPx - spacingPx)
}

internal fun barCanvasFits(
    widthPx: Float,
    heightPx: Float,
): Boolean {
    if (!widthPx.isFinite() || !heightPx.isFinite() || widthPx < 0f || heightPx < 0f) return false
    // Compose shares a packed bit budget between width and height, so check the pair.
    return runCatching { Constraints.fixed(widthPx.roundToInt(), heightPx.roundToInt()) }.isSuccess
}

internal fun baselineYForRange(
    minValue: Double,
    maxValue: Double,
    heightPx: Float,
): Float = barValueYFraction(0.0, minValue, maxValue).toFloat() * heightPx.coerceAtLeast(0f)

/** One zero-inclusive source domain for bars, ticks and both density modes. */
internal fun ChartData.resolveBarRange(
    minValue: Double?,
    maxValue: Double?,
): Pair<Double, Double> {
    val autoMin = minOf(0.0, points.min())
    val autoMax = maxOf(0.0, points.max())
    val fallback = if (autoMin == autoMax) 0.0 to 1.0 else autoMin to autoMax
    val min = minValue ?: fallback.first
    val max = maxValue ?: fallback.second
    return if (max <= min) fallback else min to max
}

/** Overflow-safe mapping; Float conversion happens only at the drawing boundary. */
internal fun barValueYFraction(
    value: Double,
    min: Double,
    max: Double,
): Double {
    if (min == max) return if (max < 0.0) 0.0 else 1.0
    val clamped = value.coerceIn(min, max)
    val span = max - min
    return if (span.isFinite()) {
        (max - clamped) / span
    } else {
        val scale = maxOf(abs(min), abs(max))
        (max / scale - clamped / scale) / (max / scale - min / scale)
    }.coerceIn(0.0, 1.0)
}

internal fun visibleIndexRange(
    dataSize: Int,
    viewportWidthPx: Float,
    scrollOffsetPx: Float,
    unitWidthPx: Float,
): IntRange =
    visibleIndexRangeCore(
        dataSize = dataSize,
        viewportWidthPx = viewportWidthPx,
        scrollOffsetPx = scrollOffsetPx,
        unitWidthPx = unitWidthPx,
    )

internal fun sampledLabelIndices(
    dataSize: Int,
    maxCount: Int,
    visibleRange: IntRange? = null,
): List<Int> =
    sampledLabelIndicesCore(
        dataSize = dataSize,
        maxCount = maxCount,
        visibleRange = visibleRange,
    )

internal fun scrollableLabelIndices(
    dataSize: Int,
    maxCount: Int,
    visibleRange: IntRange,
): List<Int> =
    scrollableLabelIndicesCore(
        dataSize = dataSize,
        maxCount = maxCount,
        visibleRange = visibleRange,
    )

internal fun centeredLabelIndexRange(
    dataSize: Int,
    unitWidthPx: Float,
    viewportWidthPx: Float,
    scrollOffsetPx: Float,
    firstCenterPx: Float = 0f,
    labelWidthPx: Float = 0f,
    edgePaddingPx: Float = 0f,
): IntRange =
    centeredLabelIndexRangeCore(
        dataSize = dataSize,
        unitWidthPx = unitWidthPx,
        viewportWidthPx = viewportWidthPx,
        scrollOffsetPx = scrollOffsetPx,
        firstCenterPx = firstCenterPx,
        labelWidthPx = labelWidthPx,
        edgePaddingPx = edgePaddingPx,
    )

internal fun planAxisXLabels(request: AxisXPlanRequest): AxisXPlanResult = planAxisXLabelsCore(request)
