package io.github.hdcharts.core.internal.axis

import kotlin.math.ceil
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AxisXPlannerTest {
    @Test
    fun planAxisXLabels_fit_picksDensestGridWithinMaxCount() {
        assertEquals(expected = listOf(0, 2, 4, 6, 8, 10), actual = fitLabels(dataSize = 12))
    }

    @Test
    fun planAxisXLabels_fit_whenStepDividesAxis_labelsBothEnds() {
        assertEquals(expected = listOf(0, 4, 8, 12, 16, 20), actual = fitLabels(dataSize = 21))
    }

    @Test
    fun planAxisXLabels_fit_whenLastItemCostsOneLabel_endsOnLastItem() {
        // Every 2nd item gives five labels but skips item 9; every 3rd gives four and ends on it.
        assertEquals(expected = listOf(0, 3, 6, 9), actual = fitLabels(dataSize = 10))
    }

    @Test
    fun planAxisXLabels_fit_whenLastItemCostsTwoLabels_keepsDensestGrid() {
        // Only every item or the two end items reach item 29, and the two end items are four labels fewer.
        assertEquals(expected = listOf(0, 5, 10, 15, 20, 25), actual = fitLabels(dataSize = 30))
    }

    @Test
    fun planAxisXLabels_fit_neverShowsMoreThanMaxCount() {
        // Every item would be 8 labels; every 2nd item is the densest grid of at most 6.
        assertEquals(expected = listOf(0, 2, 4, 6), actual = fitLabels(dataSize = 8))
        for (dataSize in 1..120) {
            for (maxLabelCount in 2..12) {
                val labels = fitLabels(dataSize = dataSize, maxLabelCount = maxLabelCount)

                assertTrue(labels.size <= maxLabelCount, "$dataSize items, at most $maxLabelCount: $labels")
            }
        }
    }

    @Test
    fun planAxisXLabels_fit_whenLabelsWouldCrowd_widensStep() {
        // 50 px per item and 120 px minimum spacing: labels must be at least three items apart.
        val labels = fitLabels(dataSize = 12, unitWidthPx = 50f, minLabelSpacingPx = 120f)

        assertEquals(expected = listOf(0, 3, 6, 9), actual = labels)
    }

    @Test
    fun planAxisXLabels_fit_automatic_labelsEveryItemThatFits() {
        assertEquals(expected = (0..11).toList(), actual = fitLabels(dataSize = 12, maxLabelCount = null))
    }

    @Test
    fun planAxisXLabels_fit_automatic_widensStepOnlyForSpacing() {
        // 50 px per item and 120 px minimum spacing: every 3rd item is as dense as labels fit.
        val labels = fitLabels(dataSize = 12, maxLabelCount = null, unitWidthPx = 50f, minLabelSpacingPx = 120f)

        assertEquals(expected = listOf(0, 3, 6, 9), actual = labels)
    }

    @Test
    fun planAxisXLabels_fit_alwaysEvenlySpacedFromFirstItem() {
        for (dataSize in 1..60) {
            for (maxLabelCount in listOf(null) + (2..10)) {
                for (minLabelSpacingPx in listOf(0f, 30f, 75f, 160f)) {
                    val labels =
                        fitLabels(
                            dataSize = dataSize,
                            maxLabelCount = maxLabelCount,
                            unitWidthPx = 40f,
                            minLabelSpacingPx = minLabelSpacingPx,
                        )
                    val gaps = labels.zipWithNext { left, right -> right - left }.distinct()
                    val case = "$dataSize items, at most $maxLabelCount, spacing $minLabelSpacingPx: $labels"

                    assertEquals(expected = 0, actual = labels.first(), message = case)
                    assertTrue(gaps.size <= 1, case)
                    assertTrue(gaps.all { gap -> gap * 40f >= minLabelSpacingPx }, case)
                }
            }
        }
    }

    @Test
    fun planAxisXLabels_fit_singleItem_labelsIt() {
        assertEquals(expected = listOf(0), actual = fitLabels(dataSize = 1))
    }

    @Test
    fun planAxisXLabels_scroll_singleItem_labelsIt() {
        val request = SCROLL_REQUEST.copy(dataSize = 1)
        val result = planAxisXLabels(request = request, scrollOffsetPx = 0f)

        assertEquals(expected = listOf(0), actual = result.labelIndices)
    }

    @Test
    fun planAxisXLabels_fit_maxCountOfTwo_labelsFirstAndLastItem() {
        assertEquals(expected = listOf(0, 9), actual = fitLabels(dataSize = 10, maxLabelCount = 2))
    }

    @Test
    fun planAxisXLabelStride_maxCountAboveDensestGrid_matchesAutomaticCount() {
        for (isScrollable in listOf(false, true)) {
            for (dataSize in 1..60) {
                for (minLabelSpacingPx in listOf(0f, 30f, 75f, 160f)) {
                    val request =
                        AxisXPlanRequest(
                            dataSize = dataSize,
                            maxLabelCount = null,
                            isScrollable = isScrollable,
                            unitWidthPx = 40f,
                            viewportWidthPx = if (isScrollable) 400f else 40f * dataSize,
                            minLabelSpacingPx = minLabelSpacingPx,
                        )
                    val case = "$dataSize items, scrollable $isScrollable, spacing $minLabelSpacingPx"

                    assertEquals(
                        expected = planAxisXLabelStride(request),
                        actual = planAxisXLabelStride(request.copy(maxLabelCount = 1000)),
                        message = case,
                    )
                }
            }
        }
    }

    @Test
    fun planAxisXLabelStride_scroll_maxCountAboveItemsInView_usesDensestStride() {
        // Up to 21 ticks per screen is under the cap, so only the 3-item minimum spacing applies.
        assertEquals(expected = 3, actual = planAxisXLabelStride(SCROLL_REQUEST.copy(maxLabelCount = 100)))
    }

    @Test
    fun planAxisXLabelStride_scroll_maxCountBelowDensestGrid_widensStride() {
        // A 360 px screen of 18 px items shows up to 21 ticks, so at most two labels need every 11th item.
        assertEquals(expected = 11, actual = planAxisXLabelStride(SCROLL_REQUEST.copy(maxLabelCount = 2)))
    }

    @Test
    fun planAxisXLabels_scroll_neverDrawsMoreThanMaxCountPerScreen() {
        for (maxLabelCount in 2..8) {
            for (unitWidthPx in listOf(1f, 7.5f, 18f, 40f)) {
                for (viewportWidthPx in listOf(90f, 359.5f, 360f, 361f)) {
                    val request =
                        SCROLL_REQUEST.copy(
                            dataSize = 60,
                            maxLabelCount = maxLabelCount,
                            unitWidthPx = unitWidthPx,
                            viewportWidthPx = viewportWidthPx,
                            minLabelSpacingPx = 0f,
                        )
                    val stride = planAxisXLabelStride(request)
                    // Rounding can lay the label row out up to 1 px wider than the viewport.
                    val rowWidthPx = ceil(viewportWidthPx).toInt()
                    // Point ticks start at 0 and bar ticks at half a bar; each offset also puts a tick on the
                    // near edge of the 1 px slack.
                    val offsets =
                        (0 until request.dataSize).flatMap { index ->
                            listOf(0f, 1f).map {
                                index * unitWidthPx +
                                    it
                            }
                        }
                    for (firstTickPx in listOf(0f, unitWidthPx / 2f)) {
                        for (scrollOffsetPx in offsets) {
                            val drawn =
                                (0 until request.dataSize step stride).count { index ->
                                    val tickX = firstTickPx + index * unitWidthPx - scrollOffsetPx
                                    placeXAxisLabel(
                                        tickX,
                                        labelWidthPx = 10,
                                        labelHeightPx = 10,
                                        rowWidthPx,
                                        centerY = 0f,
                                    ) !=
                                        null
                                }

                            assertTrue(drawn <= maxLabelCount, "$request, offset $scrollOffsetPx: $drawn labels")
                        }
                    }
                }
            }
        }
    }

    @Test
    fun planAxisXLabelStride_matchesSearchOfEveryStride() {
        // The planner checks divisors of the last index; walking every stride must agree.
        val modes = listOf(false to false, true to false, false to true)
        for ((isScrollable, isSliding) in modes) {
            for (dataSize in 1..24) {
                for (minStride in 1..dataSize) {
                    for (maxLabelCount in listOf(null, 1000) + (1..26)) {
                        for (itemsInView in listOf(1, 2, 5, 13, 24, 90)) {
                            val request =
                                AxisXPlanRequest(
                                    dataSize = dataSize,
                                    maxLabelCount = maxLabelCount,
                                    isScrollable = isScrollable,
                                    unitWidthPx = 8f,
                                    viewportWidthPx = 8f * itemsInView,
                                    minLabelSpacingPx = 8f * minStride,
                                    isSliding = isSliding,
                                )

                            assertEquals(
                                expected = strideFromEveryStride(request, minStride),
                                actual = planAxisXLabelStride(request),
                                message = "$request",
                            )
                        }
                    }
                }
            }
        }
    }

    @Test
    fun planAxisXLabelStride_tinyUnitWidth_labelsOnlyFirstItem() {
        val request =
            AxisXPlanRequest(
                dataSize = 50,
                maxLabelCount = null,
                isScrollable = false,
                unitWidthPx = Float.MIN_VALUE,
                viewportWidthPx = 1f,
                minLabelSpacingPx = 40f,
            )

        assertEquals(expected = 50, actual = planAxisXLabelStride(request))
        assertEquals(
            expected = listOf(0),
            actual = planAxisXLabels(request = request, scrollOffsetPx = 0f).labelIndices,
        )
    }

    @Test
    fun planAxisXLabels_fit_hugeUnitWidth_labelsEveryItem() {
        assertEquals(
            expected = (0..11).toList(),
            actual = fitLabels(dataSize = 12, maxLabelCount = null, unitWidthPx = 1_000_000f),
        )
    }

    @Test
    fun planAxisXLabels_fit_spacingWiderThanPlot_labelsOnlyFirstItem() {
        // Five 20 px items cannot fit two labels 500 px apart.
        assertEquals(
            expected = listOf(0),
            actual = fitLabels(dataSize = 5, unitWidthPx = 20f, minLabelSpacingPx = 500f),
        )
    }

    @Test
    fun planAxisXLabels_scroll_spacingWiderThanViewport_showsAtMostOneLabel() {
        val request = SCROLL_REQUEST.copy(minLabelSpacingPx = 500f)
        val stride = planAxisXLabelStride(request)

        // 500 px at 18 px per item needs 28 items between labels, more than the 20 in view.
        assertEquals(expected = 28, actual = stride)
        for (scrollOffsetPx in 0..3600 step 7) {
            val labels = planAxisXLabels(request = request, scrollOffsetPx = scrollOffsetPx.toFloat())

            assertTrue(labels.labelIndices.size <= 1, "offset $scrollOffsetPx: ${labels.labelIndices}")
            assertTrue(labels.labelIndices.all { index -> index % stride == 0 }, "offset $scrollOffsetPx")
        }
    }

    @Test
    fun planAxisXLabels_scroll_labelsVisibleItemsOnSharedGrid() {
        val result = scrollPlan(scrollOffsetPx = 900f)

        // 20 items in view and at least 3 items between labels.
        assertEquals(expected = 50..70, actual = result.visibleRange)
        assertEquals(expected = listOf(51, 54, 57, 60, 63, 66, 69), actual = result.labelIndices)
    }

    @Test
    fun planAxisXLabels_scroll_keepsLabelsOnSameItemsWhileScrolling() {
        for (scrollOffsetPx in 0..3600 step 7) {
            val labels = scrollPlan(scrollOffsetPx = scrollOffsetPx.toFloat()).labelIndices

            assertTrue(labels.isNotEmpty(), "offset $scrollOffsetPx")
            assertTrue(labels.all { index -> index % 3 == 0 }, "offset $scrollOffsetPx: $labels")
            assertTrue(labels.zipWithNext { left, right -> right - left }.all { gap -> gap == 3 })
        }
    }

    @Test
    fun planAxisXLabels_scroll_plansEveryDrawnLabelAndAtMostOneGridItemPastViewport() {
        for (scrollOffsetPx in 0..3600 step 7) {
            val result = scrollPlan(scrollOffsetPx = scrollOffsetPx.toFloat())

            assertPlansDrawnLabels(SCROLL_REQUEST, stride = 3, scrollOffsetPx.toFloat(), result)
        }
    }

    @Test
    fun planAxisXLabels_scroll_atEnd_labelsLastPointPastUnroundedViewport() {
        // A 340.86 px plot is laid out as a 341 px label row. Scrolled to the end at 847 px, point 99
        // sits at 341 px: past the unrounded viewport, but inside the row that draws the labels.
        val request =
            AxisXPlanRequest(
                dataSize = 100,
                maxLabelCount = null,
                isScrollable = true,
                unitWidthPx = 12f,
                viewportWidthPx = 340.86f,
                minLabelSpacingPx = 35.4f,
            )
        val result = planAxisXLabels(request = request, scrollOffsetPx = 847f)

        assertEquals(expected = 70..98, actual = result.visibleRange)
        assertEquals(expected = (72..99 step 3).toList(), actual = result.labelIndices)
    }

    @Test
    fun planAxisXLabels_scroll_fractionalGeometry_plansEveryDrawnLabel() {
        // Fractional item and viewport widths put ticks just inside and just past the far edge.
        for (dataSize in 1..30) {
            for (unitWidthPx in listOf(1.3f, 3.3f, 17.25f)) {
                for (viewportWidthPx in listOf(10.5f, 60.4f, 340.86f)) {
                    // Spacing of 0, 3 and 5 items gives strides of 1, 3 and 5, or one more after rounding.
                    for (spacingItems in listOf(0, 3, 5)) {
                        val request =
                            AxisXPlanRequest(
                                dataSize = dataSize,
                                maxLabelCount = null,
                                isScrollable = true,
                                unitWidthPx = unitWidthPx,
                                viewportWidthPx = viewportWidthPx,
                                minLabelSpacingPx = spacingItems * unitWidthPx,
                            )
                        val stride = planAxisXLabelStride(request)
                        for (quarterItem in 0..4 * dataSize) {
                            val scrollOffsetPx = quarterItem * unitWidthPx / 4f
                            val result = planAxisXLabels(request = request, scrollOffsetPx = scrollOffsetPx)

                            assertPlansDrawnLabels(request, stride, scrollOffsetPx, result)
                        }
                    }
                }
            }
        }
    }

    @Test
    fun planAxisXLabels_invalidInput_returnsNoLabels() {
        val invalidRequests =
            listOf(
                SCROLL_REQUEST.copy(dataSize = 0),
                SCROLL_REQUEST.copy(dataSize = -1),
                SCROLL_REQUEST.copy(viewportWidthPx = 0f),
                SCROLL_REQUEST.copy(viewportWidthPx = -1f),
                SCROLL_REQUEST.copy(unitWidthPx = 0f),
                SCROLL_REQUEST.copy(unitWidthPx = -1f),
            )

        invalidRequests.forEach { request ->
            val result = planAxisXLabels(request = request, scrollOffsetPx = 0f)

            assertEquals(expected = 1, actual = planAxisXLabelStride(request))
            assertTrue(result.labelIndices.isEmpty())
            assertEquals(expected = IntRange.EMPTY, actual = result.visibleRange)
        }
    }

    @Test
    fun planAxisXLabels_scroll_viewportStartsBetweenGridItems_startsAtNextGridItem() {
        // Item 51 starts the viewport, between grid items 50 and 55.
        val result = planAxisXLabels(request = STRIDE_5_REQUEST, scrollOffsetPx = 51 * 18f)

        assertEquals(expected = 51..71, actual = result.visibleRange)
        assertEquals(expected = listOf(55, 60, 65, 70), actual = result.labelIndices)
    }

    @Test
    fun planAxisXLabels_scroll_gridItemOnFarEdge_isLabeled() {
        // Item 75 starts exactly at the far edge of the viewport.
        val result = planAxisXLabels(request = STRIDE_5_REQUEST, scrollOffsetPx = 55 * 18f)

        assertEquals(expected = 55..75, actual = result.visibleRange)
        assertEquals(expected = listOf(55, 60, 65, 70, 75), actual = result.labelIndices)
    }

    @Test
    fun planAxisXLabels_scroll_atEnd_stopsAtLastItem() {
        // 220 items of 18 px in a 360 px viewport scroll at most 3600 px.
        val result = planAxisXLabels(request = STRIDE_5_REQUEST, scrollOffsetPx = 3600f)

        assertEquals(expected = 200..219, actual = result.visibleRange)
        assertEquals(expected = listOf(200, 205, 210, 215), actual = result.labelIndices)
    }

    @Test
    fun planAxisXLabels_scroll_negativeOffset_matchesZeroOffset() {
        assertEquals(
            expected = planAxisXLabels(request = SCROLL_REQUEST, scrollOffsetPx = 0f),
            actual = planAxisXLabels(request = SCROLL_REQUEST, scrollOffsetPx = -100f),
        )
    }

    @Test
    fun planAxisXLabelStride_sliding_keepsDensestGrid() {
        // 100 px per item and 200 px minimum spacing: at least every 2nd item.
        val request = FIT_REQUEST.copy(dataSize = 10, minLabelSpacingPx = 200f)

        // Every 3rd item ends on item 9; a sliding window has no fixed last item to end on.
        assertEquals(expected = 3, actual = planAxisXLabelStride(request))
        assertEquals(expected = 2, actual = planAxisXLabelStride(request.copy(isSliding = true)))
    }

    @Test
    fun planAxisXLabels_sliding_keepsLabelsOnTheirItems() {
        // 100 px per item and 250 px minimum spacing: every 3rd item of the whole series.
        val request = FIT_REQUEST.copy(dataSize = 12, minLabelSpacingPx = 250f, isSliding = true)
        val labels = { firstItemIndex: Long ->
            planAxisXLabels(request = request, scrollOffsetPx = 0f, firstItemIndex = firstItemIndex).labelIndices
        }

        assertEquals(expected = listOf(0, 3, 6, 9), actual = labels(0L))
        assertEquals(expected = listOf(2, 5, 8, 11), actual = labels(1L))
        assertEquals(expected = listOf(1, 4, 7, 10), actual = labels(2L))
        assertEquals(expected = listOf(0, 3, 6, 9), actual = labels(3L))
    }

    @Test
    fun planAxisXLabels_sliding_labelsEveryGridItemOfTheWholeSeries() {
        for (dataSize in 1..30) {
            for (stride in 1..dataSize) {
                val request = FIT_REQUEST.copy(dataSize = dataSize, isSliding = true)
                for (firstItemIndex in (0L..40L) + listOf(Long.MAX_VALUE - 1, Long.MAX_VALUE)) {
                    val labels =
                        planAxisXLabels(
                            request = request,
                            scrollOffsetPx = 0f,
                            stride = stride,
                            firstItemIndex = firstItemIndex,
                        ).labelIndices
                    // Series index firstItemIndex + index, taken modulo the stride first so it cannot overflow.
                    val expected =
                        (0 until dataSize).filter { index ->
                            (firstItemIndex.mod(stride) + index) % stride ==
                                0
                        }

                    assertEquals(
                        expected = expected,
                        actual = labels,
                        message = "$dataSize items, stride $stride, first $firstItemIndex",
                    )
                }
            }
        }
    }

    @Test
    fun planAxisXLabels_fit_ignoresScrollOffset() {
        val request = SCROLL_REQUEST.copy(dataSize = 12, isScrollable = false, viewportWidthPx = 12 * 18f)

        assertEquals(
            expected = planAxisXLabels(request = request, scrollOffsetPx = 0f),
            actual = planAxisXLabels(request = request, scrollOffsetPx = 500f),
        )
    }

    private fun fitLabels(
        dataSize: Int,
        maxLabelCount: Int? = 6,
        unitWidthPx: Float = 100f,
        minLabelSpacingPx: Float = 40f,
    ): List<Int> {
        val request =
            AxisXPlanRequest(
                dataSize = dataSize,
                maxLabelCount = maxLabelCount,
                isScrollable = false,
                unitWidthPx = unitWidthPx,
                viewportWidthPx = unitWidthPx * dataSize,
                minLabelSpacingPx = minLabelSpacingPx,
            )
        return planAxisXLabels(request = request, scrollOffsetPx = 0f).labelIndices
    }

    // Walks every stride from minStride: the first with at most maxLabelCount labels on a screen is the
    // densest grid.
    // Without scrolling or sliding, the first stride after it that ends on the last item wins if it shows
    // at most one label fewer.
    private fun strideFromEveryStride(
        request: AxisXPlanRequest,
        minStride: Int,
    ): Int {
        // While scrolling, a screen shows every tick in a span 3 px wider than the viewport: 1 px of
        // rounding and 1 px of slack on each side.
        val itemsInView =
            if (request.isScrollable) {
                (0 until request.dataSize).count { index ->
                    index * request.unitWidthPx <= request.viewportWidthPx + 3f
                }
            } else {
                request.dataSize
            }
        // Items 0, stride, 2 * stride, ... among the first itemsInView items.
        val labelCount = { stride: Int -> (0 until itemsInView).count { index -> index % stride == 0 } }
        val strides = minStride.coerceAtMost(request.dataSize)..request.dataSize
        val maxLabelCount = request.maxLabelCount ?: Int.MAX_VALUE
        val densest = strides.firstOrNull { stride -> labelCount(stride) <= maxLabelCount } ?: request.dataSize
        if (request.isScrollable || request.isSliding) return densest
        return (densest..request.dataSize).firstOrNull { stride ->
            (request.dataSize - 1) % stride == 0 && labelCount(stride) >= labelCount(densest) - 1
        } ?: densest
    }

    // The label layout draws a point's label when its tick is inside the viewport, with 1 px of slack
    // at each edge. The plan must hold every such grid item, plus at most the grid item right after
    // the visible range.
    private fun assertPlansDrawnLabels(
        request: AxisXPlanRequest,
        stride: Int,
        scrollOffsetPx: Float,
        result: AxisXPlanResult,
    ) {
        val drawn =
            (0 until request.dataSize).filter { index ->
                val tickPx = index * request.unitWidthPx - scrollOffsetPx
                index % stride == 0 && tickPx >= -1f && tickPx <= request.viewportWidthPx + 1f
            }
        val case = "$request, stride $stride, offset $scrollOffsetPx: ${result.labelIndices}"

        assertTrue(result.labelIndices.containsAll(drawn), case)
        assertTrue(result.labelIndices.all { index -> index % stride == 0 }, case)
        assertTrue(result.labelIndices.all { index -> index <= result.visibleRange.last + 1 }, case)
    }

    private fun scrollPlan(scrollOffsetPx: Float) =
        planAxisXLabels(request = SCROLL_REQUEST, scrollOffsetPx = scrollOffsetPx)

    private companion object {
        val SCROLL_REQUEST =
            AxisXPlanRequest(
                dataSize = 220,
                maxLabelCount = null,
                isScrollable = true,
                unitWidthPx = 18f,
                viewportWidthPx = 360f,
                minLabelSpacingPx = 40f,
            )

        // A screen shows up to 21 ticks: every 5th item gives at most five labels.
        val STRIDE_5_REQUEST = SCROLL_REQUEST.copy(maxLabelCount = 5)

        // Every item is 100 px wide and fits in the plot.
        val FIT_REQUEST =
            AxisXPlanRequest(
                dataSize = 12,
                maxLabelCount = null,
                isScrollable = false,
                unitWidthPx = 100f,
                viewportWidthPx = 1200f,
                minLabelSpacingPx = 0f,
            )
    }
}
