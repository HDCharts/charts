package io.github.hdcharts.charts.internal.stackedareachart

import io.github.hdcharts.charts.internal.NO_SELECTION
import io.github.hdcharts.charts.internal.common.model.ChartDataItem
import io.github.hdcharts.charts.internal.common.model.MultiChartData
import io.github.hdcharts.charts.internal.common.model.toChartData
import io.github.hdcharts.charts.internal.common.density.aggregateLabelsByCenterValue as aggregateLabelsByCenterValueCore
import io.github.hdcharts.charts.internal.common.density.aggregatePointsByAverage as aggregatePointsByAverageCore
import io.github.hdcharts.charts.internal.common.density.bucketSizeForTarget as bucketSizeForTargetCore
import io.github.hdcharts.charts.internal.common.density.buildBucketRanges as buildBucketRangesCore
import io.github.hdcharts.charts.internal.common.density.shouldUseScrollableDensity as shouldUseScrollableDensityCore

internal const val STACKED_AREA_DENSE_THRESHOLD = 50

internal data class StackedAreaRenderData(
    val data: MultiChartData,
    val sourcePointsCount: Int,
    val sourceIndexByRenderIndex: List<Int>,
    val bucketRanges: List<IntRange>,
) {
    fun resolveSourceIndex(renderIndex: Int): Int = sourceIndexByRenderIndex.getOrNull(renderIndex) ?: NO_SELECTION

    fun resolveRenderIndex(sourceIndex: Int): Int {
        if (sourceIndex !in 0 until sourcePointsCount) return NO_SELECTION
        if (bucketRanges.isEmpty()) return sourceIndex
        val renderIndex = bucketRanges.indexOfFirst { range -> sourceIndex in range }
        return renderIndex.takeIf { it >= 0 } ?: NO_SELECTION
    }
}

internal fun shouldUseScrollableDensity(pointsCount: Int): Boolean =
    shouldUseScrollableDensityCore(
        pointsCount = pointsCount,
        threshold = STACKED_AREA_DENSE_THRESHOLD,
    )

internal fun resolveStackedAreaTotalsRange(data: MultiChartData): Pair<Double, Double> {
    val pointsCount =
        data.items
            .firstOrNull()
            ?.item
            ?.points
            ?.size ?: 0
    if (pointsCount <= 0) return 0.0 to 1.0

    val totalsByPoint =
        (0 until pointsCount).map { pointIndex ->
            data.items.sumOf { item -> item.item.points[pointIndex] }
        }
    val minTotal = totalsByPoint.minOrNull() ?: 0.0
    val maxTotal = totalsByPoint.maxOrNull() ?: 0.0
    val resolvedMin = minOf(0.0, minTotal)
    val resolvedMax = maxOf(0.0, maxTotal)
    return if (resolvedMax <= resolvedMin) {
        resolvedMin to (resolvedMin + 1.0)
    } else {
        resolvedMin to resolvedMax
    }
}

internal fun aggregateForCompactDensity(
    data: MultiChartData,
    targetPoints: Int = STACKED_AREA_DENSE_THRESHOLD,
): StackedAreaRenderData {
    val sourcePointsCount =
        data.items
            .firstOrNull()
            ?.item
            ?.points
            ?.size ?: 0
    val safeTargetPoints = targetPoints.coerceAtLeast(1)
    if (sourcePointsCount <= safeTargetPoints) {
        return identityRenderData(data)
    }

    val bucketSize = bucketSizeForTargetCore(totalPoints = sourcePointsCount, targetPoints = safeTargetPoints)
    val bucketRanges = buildBucketRangesCore(totalPoints = sourcePointsCount, bucketSize = bucketSize)
    val aggregatedCategories = aggregateLabelsByCenterValueCore(data.categories, bucketRanges)
    val aggregatedItems =
        data.items.map { item ->
            val aggregatedPoints = aggregatePointsByAverageCore(item.item.points, bucketRanges)
            val aggregatedLabels = aggregateLabelsByCenterValueCore(item.item.labels, bucketRanges)
            ChartDataItem(
                label = item.label,
                item = aggregatedPoints.toChartData(labels = aggregatedLabels),
            )
        }

    return StackedAreaRenderData(
        data =
            MultiChartData(
                items = aggregatedItems,
                categories = if (data.hasCategories()) aggregatedCategories else emptyList(),
                title = data.title,
            ),
        sourcePointsCount = sourcePointsCount,
        sourceIndexByRenderIndex = bucketRanges.map { range -> range.first + ((range.last - range.first) / 2) },
        bucketRanges = bucketRanges,
    )
}

internal fun identityRenderData(data: MultiChartData): StackedAreaRenderData {
    val sourcePointsCount =
        data.items
            .firstOrNull()
            ?.item
            ?.points
            ?.size ?: 0
    return StackedAreaRenderData(
        data = data,
        sourcePointsCount = sourcePointsCount,
        sourceIndexByRenderIndex = List(sourcePointsCount) { index -> index },
        bucketRanges = emptyList(),
    )
}
