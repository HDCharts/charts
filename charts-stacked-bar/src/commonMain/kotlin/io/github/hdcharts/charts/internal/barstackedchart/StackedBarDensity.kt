package io.github.hdcharts.charts.internal.barstackedchart

import io.github.hdcharts.charts.internal.NO_SELECTION
import io.github.hdcharts.charts.internal.common.model.ChartDataItem
import io.github.hdcharts.charts.internal.common.model.MultiChartData
import io.github.hdcharts.charts.internal.common.model.toChartData
import kotlin.math.max
import io.github.hdcharts.charts.internal.common.density.aggregatePointsByAverage as aggregatePointsByAverageCore
import io.github.hdcharts.charts.internal.common.density.bucketSizeForTarget as bucketSizeForTargetCore
import io.github.hdcharts.charts.internal.common.density.buildBucketRanges as buildBucketRangesCore

internal data class StackedBarRenderData(
    val data: MultiChartData,
    val sourceSize: Int,
    val sourceIndexByRenderIndex: List<Int>,
    val bucketRanges: List<IntRange>,
) {
    fun resolveSourceIndex(renderIndex: Int): Int = sourceIndexByRenderIndex.getOrNull(renderIndex) ?: NO_SELECTION

    fun resolveRenderIndex(sourceIndex: Int): Int {
        if (sourceIndex !in 0 until sourceSize) return NO_SELECTION
        if (bucketRanges.isEmpty()) return sourceIndex
        val renderIndex = bucketRanges.indexOfFirst { range -> sourceIndex in range }
        return renderIndex.takeIf { it >= 0 } ?: NO_SELECTION
    }
}

internal fun resolveStackedTotalsRange(data: MultiChartData): Pair<Double, Double> {
    if (data.items.isEmpty()) return 0.0 to 1.0
    val totals = data.items.map { item -> item.item.points.sum() }
    val minTotal = totals.minOrNull() ?: 0.0
    val maxTotal = totals.maxOrNull() ?: 0.0
    val resolvedMin = minOf(0.0, minTotal)
    val resolvedMax = maxOf(0.0, maxTotal)
    return if (resolvedMax <= resolvedMin) {
        resolvedMin to (resolvedMin + 1.0)
    } else {
        resolvedMin to resolvedMax
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
): Float = max(1f, barWidthPx + spacingPx)

internal fun contentWidth(
    dataSize: Int,
    unitWidthPx: Float,
    spacingPx: Float,
): Float {
    if (dataSize <= 0) return 0f
    return max(1f, dataSize * unitWidthPx - spacingPx)
}

internal fun aggregateForCompactDensity(
    data: MultiChartData,
    targetBars: Int,
): StackedBarRenderData {
    val sourceSize = data.items.size
    val safeTargetBars = targetBars.coerceAtLeast(1)
    if (sourceSize <= safeTargetBars) {
        return identityRenderData(data)
    }

    val bucketSize = bucketSizeForTargetCore(totalPoints = sourceSize, targetPoints = safeTargetBars)
    val bucketRanges = buildBucketRangesCore(totalPoints = sourceSize, bucketSize = bucketSize)
    val segmentCount =
        data.items
            .firstOrNull()
            ?.item
            ?.points
            ?.size ?: 0

    val aggregatedItems =
        bucketRanges.mapIndexed { bucketIndex, range ->
            val centerIndex = range.first + ((range.last - range.first) / 2)
            val centerItem = data.items.getOrNull(centerIndex) ?: data.items[range.first]
            val pointsBySegment =
                (0 until segmentCount).map { segmentIndex ->
                    val segmentValues = range.map { sourceIndex -> data.items[sourceIndex].item.points[segmentIndex] }
                    aggregatePointsByAverageCore(
                        sourcePoints = segmentValues,
                        bucketRanges = listOf(0 until segmentValues.size),
                    ).firstOrNull() ?: 0.0
                }
            val fallbackLabel = "Bucket ${bucketIndex + 1}"
            val label = centerItem.label.ifBlank { fallbackLabel }
            ChartDataItem(
                label = label,
                item = pointsBySegment.toChartData(labels = centerItem.item.labels),
            )
        }

    return StackedBarRenderData(
        data =
            MultiChartData(
                items = aggregatedItems,
                categories = data.categories,
                title = data.title,
            ),
        sourceSize = sourceSize,
        sourceIndexByRenderIndex = bucketRanges.map { range -> range.first + ((range.last - range.first) / 2) },
        bucketRanges = bucketRanges,
    )
}

internal fun identityRenderData(data: MultiChartData): StackedBarRenderData {
    val sourceSize = data.items.size
    return StackedBarRenderData(
        data = data,
        sourceSize = sourceSize,
        sourceIndexByRenderIndex = List(sourceSize) { index -> index },
        bucketRanges = emptyList(),
    )
}
