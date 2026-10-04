package io.github.hdcharts.stackedbar.internal

import io.github.hdcharts.core.internal.NO_SELECTION
import io.github.hdcharts.core.internal.model.ChartRenderData
import io.github.hdcharts.core.model.ChartData
import io.github.hdcharts.core.model.ChartSeries
import kotlin.math.max
import io.github.hdcharts.core.internal.density.aggregatePointsByAverage as aggregatePointsByAverageCore
import io.github.hdcharts.core.internal.density.bucketSizeForTarget as bucketSizeForTargetCore
import io.github.hdcharts.core.internal.density.buildBucketRanges as buildBucketRangesCore

internal data class StackedBarRenderData(
    val data: ChartRenderData,
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

internal fun resolveStackedTotalsRange(data: ChartRenderData): Pair<Double, Double> {
    val totals = data.seriesTotals
    if (totals.isEmpty()) return 0.0 to 1.0
    val resolvedMin = minOf(0.0, totals.min())
    val resolvedMax = maxOf(0.0, totals.max())
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
    data: ChartRenderData,
    targetBars: Int,
): StackedBarRenderData {
    val sourceSize = data.series.size
    val safeTargetBars = targetBars.coerceAtLeast(1)
    if (sourceSize <= safeTargetBars) {
        return identityRenderData(data)
    }

    val bucketSize = bucketSizeForTargetCore(totalPoints = sourceSize, targetPoints = safeTargetBars)
    val bucketRanges = buildBucketRangesCore(totalPoints = sourceSize, bucketSize = bucketSize)
    val segmentCount = data.valueCount()

    val aggregatedBars =
        bucketRanges.mapIndexed { bucketIndex, range ->
            val centerIndex = range.first + ((range.last - range.first) / 2)
            val centerBar = data.series[centerIndex]
            val pointsBySegment =
                (0 until segmentCount).map { segmentIndex ->
                    val segmentValues = range.map { sourceIndex -> data.series[sourceIndex].values[segmentIndex] }
                    aggregatePointsByAverageCore(
                        sourcePoints = segmentValues,
                        bucketRanges = listOf(0 until segmentValues.size),
                    ).firstOrNull() ?: 0.0
                }
            ChartSeries(
                name = centerBar.name.orEmpty().ifBlank { "Bucket ${bucketIndex + 1}" },
                values = pointsBySegment,
            )
        }

    return StackedBarRenderData(
        data =
            ChartRenderData(
                data =
                    ChartData(
                        categories = data.categories,
                        series = aggregatedBars,
                    ),
                title = data.title,
            ),
        sourceSize = sourceSize,
        sourceIndexByRenderIndex = bucketRanges.map { range -> range.first + ((range.last - range.first) / 2) },
        bucketRanges = bucketRanges,
    )
}

internal fun identityRenderData(data: ChartRenderData): StackedBarRenderData {
    val sourceSize = data.series.size
    return StackedBarRenderData(
        data = data,
        sourceSize = sourceSize,
        sourceIndexByRenderIndex = List(sourceSize) { index -> index },
        bucketRanges = emptyList(),
    )
}
