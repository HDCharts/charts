package io.github.hdcharts.core.internal.axis

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import io.github.hdcharts.core.internal.InternalChartsApi
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min

// A screen draws the labels whose tick is within X_AXIS_LABEL_EDGE_TOLERANCE_PX of a label row, and
// the row is up to 1 px wider than the viewport after rounding.
private const val SCREEN_TICK_SLACK_PX = 1f + 2f * X_AXIS_LABEL_EDGE_TOLERANCE_PX

/**
 * Layout inputs for planning X-axis labels.
 *
 * @property dataSize number of items on the X axis.
 * @property maxLabelCount most labels to show, per screen while scrolling, or null for as many as fit.
 * @property isScrollable whether the items are wider than the viewport and scroll.
 * @property unitWidthPx distance between neighboring items.
 * @property viewportWidthPx width of the visible plot.
 * @property minLabelSpacingPx smallest distance allowed between two labeled items.
 * @property isSliding whether the items are a live window that drops its oldest item as new ones
 * arrive. Labels then follow their items through the window, so the last item plays no part.
 */
@InternalChartsApi
data class AxisXPlanRequest(
    val dataSize: Int,
    val maxLabelCount: Int?,
    val isScrollable: Boolean,
    val unitWidthPx: Float,
    val viewportWidthPx: Float,
    val minLabelSpacingPx: Float,
    val isSliding: Boolean = false,
)

/**
 * Planned X-axis labels: the [labelIndices] of the items that get a label, and the [visibleRange]
 * of items in the viewport.
 */
@InternalChartsApi
data class AxisXPlanResult(
    val labelIndices: List<Int>,
    val visibleRange: IntRange,
)

/**
 * Picks the label stride: the densest grid that keeps labels at least the minimum spacing apart and
 * shows at most [AxisXPlanRequest.maxLabelCount] labels, per screen while scrolling. Without
 * scrolling or sliding, a grid that ends on the last item wins when it shows at most one label
 * fewer. The stride does not depend on the scroll position, so labels stay on the same items while
 * scrolling.
 * The search checks the divisors of the last index, so it takes about the square root of the item
 * count in steps.
 */
@InternalChartsApi
fun planAxisXLabelStride(request: AxisXPlanRequest): Int {
    val dataSize = request.dataSize
    if (dataSize <= 0 || request.viewportWidthPx <= 0f || request.unitWidthPx <= 0f) return 1

    val minStride = ceil(request.minLabelSpacingPx / request.unitWidthPx).toInt().coerceIn(1, dataSize)
    val ticksOnScreen =
        if (request.isScrollable) {
            val spanPx = request.viewportWidthPx + SCREEN_TICK_SLACK_PX
            (spanPx / request.unitWidthPx).toInt().coerceIn(0, dataSize - 1) + 1
        } else {
            dataSize
        }
    // Any ticksOnScreen items in a row hold at most ceil(ticksOnScreen / stride) grid items.
    val countStride = request.maxLabelCount?.let { maxCount -> ceilDiv(ticksOnScreen, maxCount.coerceAtLeast(1)) } ?: 1
    val densestStride = max(minStride, countStride).coerceAtMost(dataSize)
    if (request.isScrollable || request.isSliding) return densestStride

    // Grids from item 0 end on the last item when the stride divides the last index. Only strides
    // that still show at least one label fewer than the densest grid are worth it.
    val densestCount = ceilDiv(dataSize, densestStride)
    val widestStride = if (densestCount >= 3) ceilDiv(dataSize, densestCount - 2) - 1 else dataSize
    return smallestDivisorIn(value = dataSize - 1, from = densestStride, to = widestStride) ?: densestStride
}

private fun ceilDiv(
    dividend: Int,
    divisor: Int,
): Int = (dividend - 1) / divisor + 1

// Smallest divisor of value in from..to, or null. Every stride divides 0, the last index of one item.
private fun smallestDivisorIn(
    value: Int,
    from: Int,
    to: Int,
): Int? {
    if (from > to) return null
    if (value == 0) return from
    var smallest: Int? = null
    var divisor = 1
    while (divisor.toLong() * divisor <= value) {
        if (value % divisor == 0) {
            for (candidate in intArrayOf(divisor, value / divisor)) {
                if (candidate in from..to && (smallest == null || candidate < smallest)) smallest = candidate
            }
        }
        divisor++
    }
    return smallest
}

/**
 * Plans X-axis labels on one grid: every stride-th item, with the stride from
 * [planAxisXLabelStride], so labels are always evenly spaced. The grid is anchored to the item that
 * [firstItemIndex] places at index 0 of the whole series: item `i` is labeled when
 * `firstItemIndex + i` is a multiple of the stride. With the default of 0, labels stay on the same
 * items while scrolling; a live window that passes the count of items it has dropped keeps labels
 * on the same items while it slides. While scrolling, labels are planned one item past the far edge
 * of [AxisXPlanResult.visibleRange], which stays exact for drawing bars. The label layout draws only
 * the labels whose tick is inside the plot.
 */
@InternalChartsApi
fun planAxisXLabels(
    request: AxisXPlanRequest,
    scrollOffsetPx: Float,
    stride: Int = planAxisXLabelStride(request),
    firstItemIndex: Long = 0L,
): AxisXPlanResult {
    if (request.dataSize <= 0 || request.viewportWidthPx <= 0f || request.unitWidthPx <= 0f) {
        return AxisXPlanResult(labelIndices = emptyList(), visibleRange = IntRange.EMPTY)
    }

    val safeStride = stride.coerceIn(1, request.dataSize)
    val safeScrollOffsetPx = scrollOffsetPx.coerceAtLeast(0f)
    val visibleRange =
        if (request.isScrollable) {
            visibleIndexRange(
                dataSize = request.dataSize,
                viewportWidthPx = request.viewportWidthPx,
                scrollOffsetPx = safeScrollOffsetPx,
                unitWidthPx = request.unitWidthPx,
            )
        } else {
            0 until request.dataSize
        }
    // The layout draws ticks up to 1 px past the viewport. Scrolling items are at least 1 px wide, so
    // one more item covers that slack.
    val lastLabelIndex = min(visibleRange.last + 1, request.dataSize - 1)
    // Item i is on the grid when gridOffset + i is a multiple of the stride.
    val gridOffset = firstItemIndex.mod(safeStride)
    val firstGridPosition = (visibleRange.first.toLong() + gridOffset + safeStride - 1) / safeStride * safeStride
    val firstLabelIndex = (firstGridPosition - gridOffset).toInt()
    return AxisXPlanResult(
        labelIndices = (firstLabelIndex..lastLabelIndex step safeStride).toList(),
        visibleRange = visibleRange,
    )
}

/** What sits at each X-axis index, which decides where the first label's tick is. */
@InternalChartsApi
sealed interface AxisXItems {
    /** Items are points on the plot edge, such as in line and area charts. Item 0 sits at 0. */
    data object Points : AxisXItems

    /** Items are bars [widthPx] wide, so item 0 is centered half a bar in. */
    data class Bars(
        val widthPx: Float,
    ) : AxisXItems
}

/** X-axis label [ticks] to draw and the [visibleRange] of items the plan covers. */
@InternalChartsApi
data class AxisXLabelPlan(
    val ticks: List<AxisXLayoutTick>,
    val visibleRange: IntRange,
)

/**
 * Plans the X-axis labels for one chart and places them on their ticks.
 *
 * The stride is planned once per layout; the labeled items and tick positions follow
 * [scrollOffsetPx]. Item `i` sits at
 * `firstTickPx + i * unitWidthPx`, where `firstTickPx` follows from [items]. For a live window that
 * slides, [firstItemIndex] is the number of items it has dropped, which keeps each label on its
 * item; null means the items do not slide.
 */
@Composable
@InternalChartsApi
fun rememberXAxisLabelPlan(
    labels: List<String>,
    dataSize: Int,
    maxLabelCount: Int?,
    isScrollable: Boolean,
    unitWidthPx: Float,
    viewportWidthPx: Float,
    fontSizePx: Float,
    items: AxisXItems,
    scrollOffsetPx: Float,
    firstItemIndex: Long? = null,
): AxisXLabelPlan {
    // A chart with a single item reports no step; there is nothing to space, so any width works.
    val safeUnitWidthPx = if (unitWidthPx > 0f) unitWidthPx else 1f
    val request =
        AxisXPlanRequest(
            dataSize = dataSize,
            maxLabelCount = maxLabelCount,
            isScrollable = isScrollable,
            unitWidthPx = safeUnitWidthPx,
            viewportWidthPx = viewportWidthPx,
            minLabelSpacingPx = xAxisLabelMinSpacingPx(fontSizePx),
            isSliding = firstItemIndex != null,
        )
    val firstTickPx =
        when (items) {
            AxisXItems.Points -> 0f
            is AxisXItems.Bars -> items.widthPx / 2f
        }
    // The stride depends only on the layout, so scrolling reuses it.
    val stride = remember(request) { planAxisXLabelStride(request) }
    return remember(request, stride, scrollOffsetPx, labels, firstTickPx, firstItemIndex) {
        val plan =
            planAxisXLabels(
                request = request,
                scrollOffsetPx = scrollOffsetPx,
                stride = stride,
                firstItemIndex = firstItemIndex ?: 0L,
            )
        val ticks =
            buildXAxisLayoutTicks(
                labels = labels,
                labelIndices = plan.labelIndices,
                unitWidthPx = safeUnitWidthPx,
                firstTickPx = firstTickPx,
                scrollOffsetPx = scrollOffsetPx,
            )
        AxisXLabelPlan(ticks = ticks, visibleRange = plan.visibleRange)
    }
}
