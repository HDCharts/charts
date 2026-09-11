package io.github.dautovicharis.charts.internal.common.axis

import io.github.dautovicharis.charts.internal.InternalChartsApi
import kotlin.math.abs
import kotlin.math.ceil

private const val AXIS_X_EDGE_PADDING_PX = 4f
private const val AXIS_X_MIN_SPACING_FACTOR = 1.15f
private const val AXIS_X_MIN_PREFERRED_RANGE_COUNT = 2
private const val AXIS_X_SMALL_DATASET_LABEL_THRESHOLD = 3

@InternalChartsApi
data class AxisXPlanRequest(
    val dataSize: Int,
    val requestedMaxLabelCount: Int,
    val isScrollable: Boolean,
    val unitWidthPx: Float,
    val viewportWidthPx: Float,
    val scrollOffsetPx: Float,
    val firstCenterPx: Float = 0f,
    val labelWidthPx: Float = 0f,
)

@InternalChartsApi
data class AxisXPlanResult(
    val labelIndices: List<Int>,
    val visibleRange: IntRange,
    val safeRange: IntRange,
)

@InternalChartsApi
fun planAxisXLabels(request: AxisXPlanRequest): AxisXPlanResult {
    if (request.dataSize <= 0 || request.viewportWidthPx <= 0f || request.unitWidthPx <= 0f) {
        return AxisXPlanResult(
            labelIndices = emptyList(),
            visibleRange = IntRange.EMPTY,
            safeRange = IntRange.EMPTY,
        )
    }

    val clampedScrollOffset = if (request.isScrollable) request.scrollOffsetPx.coerceAtLeast(0f) else 0f
    val safeUnitWidth = request.unitWidthPx.coerceAtLeast(Float.MIN_VALUE)
    val safeLabelWidth = request.labelWidthPx.coerceAtLeast(1f)
    val visibleRange =
        if (request.isScrollable) {
            visibleIndexRange(
                dataSize = request.dataSize,
                viewportWidthPx = request.viewportWidthPx,
                scrollOffsetPx = clampedScrollOffset,
                unitWidthPx = safeUnitWidth,
            )
        } else {
            0..(request.dataSize - 1)
        }
    val safeRange =
        centeredLabelIndexRange(
            dataSize = request.dataSize,
            unitWidthPx = safeUnitWidth,
            viewportWidthPx = request.viewportWidthPx,
            scrollOffsetPx = clampedScrollOffset,
            firstCenterPx = request.firstCenterPx,
            labelWidthPx = safeLabelWidth,
            edgePaddingPx = AXIS_X_EDGE_PADDING_PX,
        )
    val selectedRange =
        resolveLabelRangeWithFallback(
            dataSize = request.dataSize,
            preferredRange = safeRange,
            fallbackRange = visibleRange,
        )
    if (selectedRange.isEmpty()) {
        return AxisXPlanResult(
            labelIndices = emptyList(),
            visibleRange = visibleRange,
            safeRange = safeRange,
        )
    }

    val selectedRangeCount = rangeCount(selectedRange)
    val stableVisibleCount =
        if (request.isScrollable) {
            resolveStableVisibleLabelCount(
                dataSize = request.dataSize,
                unitWidthPx = safeUnitWidth,
                viewportWidthPx = request.viewportWidthPx,
                labelWidthPx = safeLabelWidth,
                edgePaddingPx = AXIS_X_EDGE_PADDING_PX,
            )
        } else {
            0
        }
    val shouldShowAllVisibleLabels =
        when {
            selectedRangeCount <= 1 -> true
            request.isScrollable -> {
                stableVisibleCount in 1..AXIS_X_SMALL_DATASET_LABEL_THRESHOLD &&
                    selectedRangeCount <= AXIS_X_SMALL_DATASET_LABEL_THRESHOLD
            }
            else -> selectedRangeCount <= AXIS_X_SMALL_DATASET_LABEL_THRESHOLD
        }
    val maxVisibleLabels =
        when {
            shouldShowAllVisibleLabels -> selectedRangeCount
            request.isScrollable -> {
                val stableRange =
                    stableRangeForCount(
                        dataSize = request.dataSize,
                        targetCount = stableVisibleCount,
                    )
                resolveMaxXAxisLabelCount(
                    requestedMaxCount = request.requestedMaxLabelCount,
                    visibleRange =
                        when {
                            stableRange.isEmpty() -> selectedRange
                            else -> stableRange
                        },
                    unitWidthPx = safeUnitWidth,
                    labelWidthPx = safeLabelWidth,
                )
            }

            else ->
                resolveMaxXAxisLabelCount(
                    requestedMaxCount = request.requestedMaxLabelCount,
                    visibleRange = selectedRange,
                    unitWidthPx = safeUnitWidth,
                    labelWidthPx = safeLabelWidth,
                )
        }

    if (maxVisibleLabels <= 0) {
        return AxisXPlanResult(
            labelIndices = emptyList(),
            visibleRange = visibleRange,
            safeRange = safeRange,
        )
    }

    val cadenceCandidate =
        when {
            shouldShowAllVisibleLabels -> selectedRange.toList()
            request.isScrollable -> {
                scrollableLabelIndices(
                    dataSize = request.dataSize,
                    maxCount = maxVisibleLabels.coerceAtLeast(2),
                    visibleRange = selectedRange,
                    stableVisibleCount = stableVisibleCount,
                )
            }

            else ->
                chooseNonScrollCadenceCandidate(
                    dataSize = request.dataSize,
                    maxVisibleLabels = maxVisibleLabels.coerceAtLeast(2),
                    range = selectedRange,
                )
        }

    val balancedIndices =
        if (request.isScrollable) {
            cadenceCandidate.distinct().sorted()
        } else {
            expandEdgeLabelsIfSpacingAllows(
                indices = cadenceCandidate,
                range = selectedRange,
                unitWidthPx = safeUnitWidth,
                labelWidthPx = safeLabelWidth,
            )
        }

    return AxisXPlanResult(
        labelIndices = balancedIndices,
        visibleRange = visibleRange,
        safeRange = safeRange,
    )
}

private fun resolveLabelRangeWithFallback(
    dataSize: Int,
    preferredRange: IntRange,
    fallbackRange: IntRange,
): IntRange {
    if (dataSize <= 0) return IntRange.EMPTY
    val clampedPreferred = preferredRange.clampToDataSize(dataSize)
    val clampedFallback = fallbackRange.clampToDataSize(dataSize)
    return when {
        rangeCount(clampedPreferred) >= AXIS_X_MIN_PREFERRED_RANGE_COUNT -> clampedPreferred
        rangeCount(clampedFallback) >= AXIS_X_MIN_PREFERRED_RANGE_COUNT -> clampedFallback
        rangeCount(clampedPreferred) > 0 -> clampedPreferred
        else -> clampedFallback
    }
}

private fun resolveMaxXAxisLabelCount(
    requestedMaxCount: Int,
    visibleRange: IntRange,
    unitWidthPx: Float,
    labelWidthPx: Float,
): Int {
    val labelsInRange = rangeCount(visibleRange)
    if (labelsInRange <= 0) return 0
    if (labelsInRange <= AXIS_X_SMALL_DATASET_LABEL_THRESHOLD) return labelsInRange

    val requested = requestedMaxCount.coerceAtLeast(2).coerceAtMost(labelsInRange)
    val safeUnitWidth = unitWidthPx.coerceAtLeast(Float.MIN_VALUE)
    val safeLabelWidth = labelWidthPx.coerceAtLeast(1f)
    val requiredSpacingPx = (safeLabelWidth * AXIS_X_MIN_SPACING_FACTOR).coerceAtLeast(1f)
    val spanPx = (labelsInRange - 1) * safeUnitWidth
    val fitCount = ((spanPx / requiredSpacingPx).toInt() + 1).coerceIn(2, labelsInRange)
    return fitCount.coerceIn(2, requested)
}

private fun resolveStableVisibleLabelCount(
    dataSize: Int,
    unitWidthPx: Float,
    viewportWidthPx: Float,
    labelWidthPx: Float,
    edgePaddingPx: Float,
): Int {
    if (dataSize <= 0 || unitWidthPx <= 0f || viewportWidthPx <= 0f) return 0

    val safeLabelHalfWidth = labelWidthPx.coerceAtLeast(0f) / 2f
    val safeEdgePadding = edgePaddingPx.coerceAtLeast(0f)
    val minCenterX = safeLabelHalfWidth + safeEdgePadding
    val maxCenterX = viewportWidthPx - safeLabelHalfWidth - safeEdgePadding
    if (maxCenterX < minCenterX) return 0

    val spanPx = (maxCenterX - minCenterX).coerceAtLeast(0f)
    val safeUnitWidth = unitWidthPx.coerceAtLeast(Float.MIN_VALUE)
    val count = (spanPx / safeUnitWidth).toInt() + 1
    return count.coerceIn(1, dataSize)
}

private fun stableRangeForCount(
    dataSize: Int,
    targetCount: Int,
): IntRange {
    if (dataSize <= 0 || targetCount <= 0) return IntRange.EMPTY
    val count = targetCount.coerceIn(1, dataSize)
    return 0..(count - 1)
}

private fun chooseNonScrollCadenceCandidate(
    dataSize: Int,
    maxVisibleLabels: Int,
    range: IntRange,
): List<Int> {
    if (range.isEmpty()) return emptyList()
    val edgeAnchoredCandidate =
        sampledLabelIndices(
            dataSize = dataSize,
            maxCount = maxVisibleLabels,
            visibleRange = range,
        )
    val centeredCandidate =
        centeredCadenceLabelIndices(
            range = range,
            targetCount = maxVisibleLabels,
        )

    val bestAtRequestedCount =
        chooseEvenerCadenceCandidate(
            primary = edgeAnchoredCandidate,
            secondary = centeredCandidate,
        )
    val requestedCandidate =
        preferEdgeAnchoredWhenSingleSlotOmitted(
            range = range,
            targetCount = maxVisibleLabels,
            edgeAnchored = edgeAnchoredCandidate,
            chosen = bestAtRequestedCount,
        )
    // When only one slot is omitted from the safe range, keep requested cadence density.
    // Compacting to one fewer label in this specific case causes visibly sparse x-axis labeling.
    if (rangeCount(range) == maxVisibleLabels + 1) {
        return requestedCandidate
    }
    val compactTargetCount = maxVisibleLabels - 1
    if (compactTargetCount < 3 || requestedCandidate.size < 4) {
        return requestedCandidate
    }

    val compactEdgeAnchoredCandidate =
        sampledLabelIndices(
            dataSize = dataSize,
            maxCount = compactTargetCount,
            visibleRange = range,
        )
    val compactCenteredCandidate =
        centeredCadenceLabelIndices(
            range = range,
            targetCount = compactTargetCount,
        )
    val bestCompactCandidate =
        chooseEvenerCadenceCandidate(
            primary = compactEdgeAnchoredCandidate,
            secondary = compactCenteredCandidate,
        )

    return when {
        shouldPreferCompactCadenceForEvenness(
            primary = requestedCandidate,
            compact = bestCompactCandidate,
        ) -> bestCompactCandidate
        else -> requestedCandidate
    }
}

private fun preferEdgeAnchoredWhenSingleSlotOmitted(
    range: IntRange,
    targetCount: Int,
    edgeAnchored: List<Int>,
    chosen: List<Int>,
): List<Int> {
    if (range.isEmpty() || targetCount <= 1) return chosen
    val countInRange = rangeCount(range)
    if (countInRange != targetCount + 1) return chosen

    val edge = edgeAnchored.distinct().sorted()
    val selected = chosen.distinct().sorted()
    if (edge.size != targetCount || selected.size != targetCount) return chosen

    val edgeAnchorsBothEdges = edge.firstOrNull() == range.first && edge.lastOrNull() == range.last
    if (!edgeAnchorsBothEdges) return chosen
    val selectedAnchorsBothEdges = selected.firstOrNull() == range.first && selected.lastOrNull() == range.last
    if (selectedAnchorsBothEdges) return chosen

    return edge
}

private fun centeredCadenceLabelIndices(
    range: IntRange,
    targetCount: Int,
): List<Int> {
    if (range.isEmpty()) return emptyList()
    val countInRange = rangeCount(range)
    val safeTarget = targetCount.coerceIn(1, countInRange)
    if (countInRange <= safeTarget) return range.toList()
    if (safeTarget == 1) return listOf((range.first + range.last) / 2)

    val span = countInRange - 1
    val stride = (span / (safeTarget - 1)).coerceAtLeast(1)
    val coveredSpan = stride * (safeTarget - 1)
    val slack = (span - coveredSpan).coerceAtLeast(0)
    val startOffset = (slack + 1) / 2
    val start = range.first + startOffset

    val centered =
        (0 until safeTarget)
            .map { index -> start + index * stride }
            .filter { index -> index in range }
    if (centered.size == safeTarget) {
        return centered
    }

    val fallbackStart = (range.last - stride * (safeTarget - 1)).coerceAtLeast(range.first)
    return (0 until safeTarget)
        .map { index -> fallbackStart + index * stride }
        .filter { index -> index in range }
}

private fun chooseEvenerCadenceCandidate(
    primary: List<Int>,
    secondary: List<Int>,
): List<Int> {
    if (primary.isEmpty()) return secondary
    if (secondary.isEmpty()) return primary

    val primaryOrdered = primary.distinct().sorted()
    val secondaryOrdered = secondary.distinct().sorted()
    if (secondaryOrdered.size != primaryOrdered.size) return primaryOrdered

    val primarySpan = labelSpan(primaryOrdered)
    val secondarySpan = labelSpan(secondaryOrdered)
    if (secondarySpan < primarySpan - 1) return primaryOrdered

    val primaryVariance = spacingVariance(primaryOrdered)
    val secondaryVariance = spacingVariance(secondaryOrdered)
    return if (secondaryVariance < primaryVariance) secondaryOrdered else primaryOrdered
}

private fun spacingVariance(indices: List<Int>): Int {
    if (indices.size < 3) return 0
    val gaps = indices.zipWithNext { first, second -> second - first }
    val maxGap = gaps.maxOrNull() ?: return 0
    val minGap = gaps.minOrNull() ?: return 0
    return maxGap - minGap
}

private fun labelSpan(indices: List<Int>): Int =
    when (indices.size) {
        0, 1 -> 0
        else -> indices.last() - indices.first()
    }

private fun shouldPreferCompactCadenceForEvenness(
    primary: List<Int>,
    compact: List<Int>,
): Boolean {
    if (primary.isEmpty() || compact.isEmpty()) return false
    if (compact.size != primary.size - 1) return false

    val primaryVariance = spacingVariance(primary)
    val compactVariance = spacingVariance(compact)
    if (compactVariance >= primaryVariance) return false

    val primarySpan = labelSpan(primary)
    val compactSpan = labelSpan(compact)
    if (compactSpan < primarySpan - 1) return false

    return true
}

private fun expandEdgeLabelsIfSpacingAllows(
    indices: List<Int>,
    range: IntRange,
    unitWidthPx: Float,
    labelWidthPx: Float,
): List<Int> {
    if (indices.isEmpty() || range.isEmpty()) return indices

    val requiredIndexSpacing =
        ceil(
            ((labelWidthPx.coerceAtLeast(1f) * AXIS_X_MIN_SPACING_FACTOR) / unitWidthPx.coerceAtLeast(Float.MIN_VALUE))
                .coerceAtLeast(1f),
        ).toInt().coerceAtLeast(1)
    val expanded = indices.distinct().sorted().toMutableList()
    val edgeCandidates = listOf(range.first, range.last).distinct()
    edgeCandidates.forEach { edgeIndex ->
        val canInsert =
            expanded.none { existingIndex ->
                abs(existingIndex - edgeIndex) < requiredIndexSpacing
            }
        if (canInsert) {
            expanded.add(edgeIndex)
        }
    }

    return expanded.distinct().sorted()
}

private fun IntRange.clampToDataSize(dataSize: Int): IntRange {
    if (isEmpty() || dataSize <= 0) return IntRange.EMPTY
    val start = first.coerceIn(0, dataSize - 1)
    val end = last.coerceIn(start, dataSize - 1)
    return start..end
}

private fun rangeCount(range: IntRange): Int = if (range.isEmpty()) 0 else range.last - range.first + 1
