package io.github.dautovicharis.charts.internal.linechart

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.util.lerp
import io.github.dautovicharis.charts.internal.common.bezier.DEFAULT_BEZIER_TENSION
import io.github.dautovicharis.charts.internal.common.bezier.cubicControlPointsForSegment
import io.github.dautovicharis.charts.internal.common.model.ChartDataItem
import io.github.dautovicharis.charts.internal.common.model.MultiChartData
import io.github.dautovicharis.charts.internal.common.model.toChartData
import io.github.dautovicharis.charts.internal.common.density.aggregateLabelsByCenterValue as aggregateLabelsByCenterValueCore
import io.github.dautovicharis.charts.internal.common.density.aggregatePointsByAverage as aggregatePointsByAverageCore
import io.github.dautovicharis.charts.internal.common.density.bucketSizeForTarget as bucketSizeForTargetCore
import io.github.dautovicharis.charts.internal.common.density.buildBucketRanges as buildBucketRangesCore
import io.github.dautovicharis.charts.internal.common.density.shouldUseScrollableDensity as shouldUseScrollableDensityCore

internal const val LINE_DENSE_THRESHOLD = 50

internal fun shouldUseScrollableDensity(pointsCount: Int): Boolean =
    shouldUseScrollableDensityCore(
        pointsCount = pointsCount,
        threshold = LINE_DENSE_THRESHOLD,
    )

internal fun aggregateForCompactDensity(
    data: MultiChartData,
    targetPoints: Int = LINE_DENSE_THRESHOLD,
): MultiChartData {
    if (targetPoints <= 1) return data
    val sourcePointsCount =
        data.items
            .firstOrNull()
            ?.item
            ?.points
            ?.size ?: return data
    if (sourcePointsCount <= targetPoints) return data

    val bucketSize = bucketSizeForTargetCore(totalPoints = sourcePointsCount, targetPoints = targetPoints)
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

    return MultiChartData(
        items = aggregatedItems,
        categories = if (data.hasCategories()) aggregatedCategories else emptyList(),
        title = data.title,
    )
}

internal fun findNearestPoint(
    touchX: Float,
    scaledValues: List<Float>,
    size: Size,
    bezier: Boolean,
    verticalInset: Float = 0f,
    bezierTension: Float = DEFAULT_BEZIER_TENSION,
): Offset {
    if (scaledValues.isEmpty()) {
        return Offset(0f, 0f)
    }

    val clampedX = touchX.coerceIn(0f, size.width)
    if (scaledValues.size == 1 || size.width == 0f) {
        return Offset(
            clampedX,
            mapScaledValueToCanvasY(
                scaledValue = scaledValues.first(),
                canvasHeight = size.height,
                verticalInset = verticalInset,
            ),
        )
    }

    val lastIndex = scaledValues.size - 1
    val step = size.width / lastIndex
    val index =
        (clampedX / step)
            .toInt()
            .coerceIn(0, lastIndex)

    if (!bezier || index == lastIndex) {
        val pointBefore = scaledValues[index]
        val pointAfter =
            when (index + 1 < scaledValues.size) {
                true -> scaledValues[index + 1]
                else -> pointBefore
            }

        val ratio = ((clampedX - (index * step)) / step).coerceIn(0f, 1f)
        val interpolatedScaled = lerp(pointBefore, pointAfter, ratio)
        return Offset(
            clampedX,
            mapScaledValueToCanvasY(
                scaledValue = interpolatedScaled,
                canvasHeight = size.height,
                verticalInset = verticalInset,
            ),
        )
    }

    val points =
        List(scaledValues.size) { pointIndex ->
            Offset(
                x = pointIndex * step,
                y =
                    mapScaledValueToCanvasY(
                        scaledValue = scaledValues[pointIndex],
                        canvasHeight = size.height,
                        verticalInset = verticalInset,
                    ),
            )
        }
    val segmentStart = index.coerceIn(0, lastIndex - 1)
    val startPoint = points[segmentStart]
    val endPoint = points[segmentStart + 1]
    val controls =
        cubicControlPointsForSegment(
            points = points,
            segmentStartIndex = segmentStart,
            tension = bezierTension,
            minY = verticalInset,
            maxY = size.height - verticalInset,
        )
    val targetX = clampedX.coerceIn(startPoint.x, endPoint.x)
    val t =
        solveBezierTForX(
            targetX = targetX,
            x0 = startPoint.x,
            x1 = controls.first.x,
            x2 = controls.second.x,
            x3 = endPoint.x,
        )
    val y =
        cubicBezier(
            t = t,
            p0 = startPoint.y,
            p1 = controls.first.y,
            p2 = controls.second.y,
            p3 = endPoint.y,
        )
    return Offset(targetX, y)
}

internal fun mapScaledValueToCanvasY(
    scaledValue: Float,
    canvasHeight: Float,
    verticalInset: Float = 0f,
): Float {
    if (canvasHeight <= 0f) return 0f
    val safeInset = verticalInset.coerceIn(0f, canvasHeight / 2f)
    val drawableHeight = (canvasHeight - (safeInset * 2f)).coerceAtLeast(0f)
    val normalized = (scaledValue / canvasHeight).coerceIn(0f, 1f)
    return safeInset + ((1f - normalized) * drawableHeight)
}

private fun solveBezierTForX(
    targetX: Float,
    x0: Float,
    x1: Float,
    x2: Float,
    x3: Float,
): Float {
    var low = 0f
    var high = 1f
    repeat(20) {
        val mid = (low + high) / 2f
        val x = cubicBezier(mid, x0, x1, x2, x3)
        if (x < targetX) {
            low = mid
        } else {
            high = mid
        }
    }
    return (low + high) / 2f
}

private fun cubicBezier(
    t: Float,
    p0: Float,
    p1: Float,
    p2: Float,
    p3: Float,
): Float {
    val oneMinusT = 1f - t
    val oneMinusT2 = oneMinusT * oneMinusT
    val t2 = t * t
    return (oneMinusT2 * oneMinusT * p0) +
        (3f * oneMinusT2 * t * p1) +
        (3f * oneMinusT * t2 * p2) +
        (t2 * t * p3)
}

internal fun scaleValues(
    values: List<Double>,
    size: Size,
    minValue: Double = values.min(),
    maxValue: Double = values.max(),
): List<Float> {
    val valueRange = maxValue - minValue
    val scale = if (valueRange != 0.0) size.height / valueRange else 1.0
    return values.map { value ->
        ((value - minValue) * scale).toFloat()
    }
}
