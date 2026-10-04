package io.github.hdcharts.line.internal

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.util.lerp
import io.github.hdcharts.core.internal.NO_SELECTION
import io.github.hdcharts.core.internal.bezier.DEFAULT_BEZIER_TENSION
import io.github.hdcharts.core.internal.bezier.cubicControlPoints
import io.github.hdcharts.core.internal.density.aggregateLabelsByCenterValue
import io.github.hdcharts.core.internal.density.aggregatePointsByAverage
import io.github.hdcharts.core.internal.density.bucketCenterIndex
import io.github.hdcharts.core.internal.density.bucketSizeForTarget
import io.github.hdcharts.core.internal.density.buildBucketRanges
import io.github.hdcharts.core.internal.density.shouldUseScrollableDensity
import io.github.hdcharts.core.internal.model.ChartRenderData
import io.github.hdcharts.core.internal.model.minMax
import io.github.hdcharts.core.internal.model.resolveOptionalRange
import io.github.hdcharts.core.model.ChartData
import io.github.hdcharts.core.model.ChartSeries

internal const val LINE_DENSE_THRESHOLD = 50

internal fun shouldUseScrollableDensity(pointsCount: Int): Boolean =
    shouldUseScrollableDensity(
        pointsCount = pointsCount,
        threshold = LINE_DENSE_THRESHOLD,
    )

internal fun compactDensityRanges(
    sourcePointsCount: Int,
    targetPoints: Int = LINE_DENSE_THRESHOLD,
): List<IntRange> {
    if (sourcePointsCount <= 0) return emptyList()
    val bucketSize = bucketSizeForTarget(sourcePointsCount, targetPoints.coerceAtLeast(1))
    return buildBucketRanges(sourcePointsCount, bucketSize)
}

internal fun renderIndexForSourceIndex(
    sourceIndex: Int,
    sourceRanges: List<IntRange>,
): Int = sourceRanges.indexOfFirst { sourceIndex in it }

internal fun sourceIndexForRenderIndex(
    renderIndex: Int,
    sourceRanges: List<IntRange>,
): Int =
    sourceRanges
        .getOrNull(renderIndex)
        ?.let(::bucketCenterIndex)
        ?: NO_SELECTION

internal fun aggregateForCompactDensity(
    data: ChartRenderData,
    targetPoints: Int = LINE_DENSE_THRESHOLD,
): ChartRenderData {
    if (targetPoints <= 1) return data
    val sourcePointsCount = data.valueCount()
    if (sourcePointsCount <= targetPoints) return data

    val bucketRanges = compactDensityRanges(sourcePointsCount, targetPoints)
    val aggregatedCategories = aggregateLabelsByCenterValue(data.categories, bucketRanges)
    val aggregatedSeries =
        data.series.map { series ->
            ChartSeries(
                name = series.name,
                values = aggregatePointsByAverage(series.values, bucketRanges),
            )
        }

    return ChartRenderData(
        data =
            ChartData(
                categories = if (data.hasCategories()) aggregatedCategories else emptyList(),
                series = aggregatedSeries,
            ),
        title = data.title,
    )
}

/** X-axis labels of a line chart: the shared categories, unless every one of them is blank. */
internal fun resolveLineXAxisLabels(data: ChartRenderData): List<String> =
    data.categories
        .takeUnless { it.all(String::isBlank) }
        .orEmpty()

/**
 * Resolves the Y-axis domain, applying [minValue] and [maxValue] independently over the
 * data-derived range. If either override is null, that bound falls back to the data range. If
 * both bounds are explicit overrides and conflict, both fall back to the data range. If only one
 * bound is overridden and the data-derived opposite bound crosses it, the override still wins and
 * the opposite bound is clamped to it. See [resolveOptionalRange].
 */
internal fun ChartRenderData.resolveLineRange(
    minValue: Double?,
    maxValue: Double?,
): Pair<Double, Double> {
    val (dataMin, dataMax) = minMax()
    return resolveOptionalRange(dataMin, dataMax, minValue, maxValue)
}

/** Reports whether both data sets have the same number of series and the same number of points. */
internal fun hasSameSeriesStructure(
    previous: List<List<Double>>,
    current: List<List<Double>>,
): Boolean {
    if (previous.size != current.size) return false
    return previous.indices.all { index -> previous[index].size == current[index].size }
}

/**
 * Returns the point nearest to [touchX] on a line drawn through [scaledValues].
 *
 * Reads [scaledValuesCount] values from [scaledValues] instead of taking a list, so a chart that
 * redraws the drag marker on every touch frame reuses one buffer instead of allocating a list per
 * series per frame.
 */
internal fun findNearestPoint(
    touchX: Float,
    scaledValues: FloatArray,
    scaledValuesCount: Int,
    size: Size,
    bezier: Boolean,
    verticalInset: Float = 0f,
    bezierTension: Float = DEFAULT_BEZIER_TENSION,
): Offset {
    if (scaledValuesCount <= 0) {
        return Offset(0f, 0f)
    }

    val clampedX = touchX.coerceIn(0f, size.width)
    if (scaledValuesCount == 1 || size.width == 0f) {
        return Offset(
            clampedX,
            mapScaledValueToCanvasY(
                scaledValue = scaledValues[0],
                canvasHeight = size.height,
                verticalInset = verticalInset,
            ),
        )
    }

    val lastIndex = scaledValuesCount - 1
    val step = size.width / lastIndex
    val index =
        (clampedX / step)
            .toInt()
            .coerceIn(0, lastIndex)

    if (!bezier || index == lastIndex) {
        val pointBefore = scaledValues[index]
        val pointAfter =
            when (index + 1 < scaledValuesCount) {
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

    val yAt: (Int) -> Float =
        { pointIndex ->
            mapScaledValueToCanvasY(
                scaledValue = scaledValues[pointIndex],
                canvasHeight = size.height,
                verticalInset = verticalInset,
            )
        }
    val segmentStart = index.coerceIn(0, lastIndex - 1)
    val startPoint = Offset(segmentStart * step, yAt(segmentStart))
    val endPoint = Offset((segmentStart + 1) * step, yAt(segmentStart + 1))
    val controls =
        cubicControlPoints(
            p0 = Offset((segmentStart - 1).coerceAtLeast(0) * step, yAt((segmentStart - 1).coerceAtLeast(0))),
            p1 = startPoint,
            p2 = endPoint,
            p3 =
                Offset(
                    x = (segmentStart + 2).coerceAtMost(lastIndex) * step,
                    y = yAt((segmentStart + 2).coerceAtMost(lastIndex)),
                ),
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
