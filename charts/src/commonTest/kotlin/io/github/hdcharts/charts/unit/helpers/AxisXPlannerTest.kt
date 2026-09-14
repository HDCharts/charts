package io.github.hdcharts.charts.unit.helpers

import io.github.hdcharts.charts.internal.common.axis.AxisXPlanRequest
import io.github.hdcharts.charts.internal.common.axis.planAxisXLabels
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AxisXPlannerTest {
    @Test
    fun planAxisXLabels_scrollAdjacentOffsets_keepStableCadence() {
        val first =
            planAxisXLabels(
                request =
                    AxisXPlanRequest(
                        dataSize = 220,
                        requestedMaxLabelCount = 3,
                        isScrollable = true,
                        unitWidthPx = 18f,
                        viewportWidthPx = 164f,
                        scrollOffsetPx = 1962f,
                        firstCenterPx = 0f,
                        labelWidthPx = 52f,
                    ),
            ).labelIndices
        val second =
            planAxisXLabels(
                request =
                    AxisXPlanRequest(
                        dataSize = 220,
                        requestedMaxLabelCount = 3,
                        isScrollable = true,
                        unitWidthPx = 18f,
                        viewportWidthPx = 164f,
                        scrollOffsetPx = 1980f,
                        firstCenterPx = 0f,
                        labelWidthPx = 52f,
                    ),
            ).labelIndices

        assertTrue(first.size >= 2)
        assertTrue(second.size >= 2)

        val firstStride = first[1] - first[0]
        val secondStride = second[1] - second[0]
        assertEquals(firstStride, secondStride)
    }

    @Test
    fun planAxisXLabels_scrollWhenSafeRangeCrossesThree_doesNotFlipStride() {
        val nearBoundary =
            planAxisXLabels(
                request =
                    AxisXPlanRequest(
                        dataSize = 180,
                        requestedMaxLabelCount = 3,
                        isScrollable = true,
                        unitWidthPx = 50f,
                        viewportWidthPx = 260f,
                        scrollOffsetPx = 0f,
                        firstCenterPx = 0f,
                        labelWidthPx = 100f,
                    ),
            )
        val afterBoundary =
            planAxisXLabels(
                request =
                    AxisXPlanRequest(
                        dataSize = 180,
                        requestedMaxLabelCount = 3,
                        isScrollable = true,
                        unitWidthPx = 50f,
                        viewportWidthPx = 260f,
                        scrollOffsetPx = 44f,
                        firstCenterPx = 0f,
                        labelWidthPx = 100f,
                    ),
            )

        assertEquals(expected = 2..4, actual = nearBoundary.safeRange)
        assertEquals(expected = 2..5, actual = afterBoundary.safeRange)
        assertEquals(expected = nearBoundary.labelIndices, actual = afterBoundary.labelIndices)
        assertTrue(nearBoundary.labelIndices.size >= 2)
        assertEquals(
            expected = nearBoundary.labelIndices[1] - nearBoundary.labelIndices[0],
            actual = afterBoundary.labelIndices[1] - afterBoundary.labelIndices[0],
        )
    }

    @Test
    fun planAxisXLabels_scrollWithShortLabels_doesNotInsertAdjacentEdgeLabels() {
        val result =
            planAxisXLabels(
                request =
                    AxisXPlanRequest(
                        dataSize = 120,
                        requestedMaxLabelCount = 6,
                        isScrollable = true,
                        unitWidthPx = 56f,
                        viewportWidthPx = 600f,
                        scrollOffsetPx = 1_000f,
                        firstCenterPx = 28f,
                        labelWidthPx = 24f,
                    ),
            )

        assertTrue(result.labelIndices.size >= 2)
        val gaps = result.labelIndices.zipWithNext { first, second -> second - first }
        assertTrue(gaps.all { gap -> gap >= 2 })
    }

    @Test
    fun planAxisXLabels_scrollShortLabels_adjacentOffsets_keepCadenceStable() {
        val first =
            planAxisXLabels(
                request =
                    AxisXPlanRequest(
                        dataSize = 240,
                        requestedMaxLabelCount = 6,
                        isScrollable = true,
                        unitWidthPx = 22f,
                        viewportWidthPx = 320f,
                        scrollOffsetPx = 1_900f,
                        firstCenterPx = 11f,
                        labelWidthPx = 24f,
                    ),
            ).labelIndices
        val second =
            planAxisXLabels(
                request =
                    AxisXPlanRequest(
                        dataSize = 240,
                        requestedMaxLabelCount = 6,
                        isScrollable = true,
                        unitWidthPx = 22f,
                        viewportWidthPx = 320f,
                        scrollOffsetPx = 1_912f,
                        firstCenterPx = 11f,
                        labelWidthPx = 24f,
                    ),
            ).labelIndices

        assertTrue(first.size >= 2)
        assertTrue(second.size >= 2)
        assertEquals(first.size, second.size)
        assertTrue(first.zipWithNext { previous, next -> next > previous }.all { it })
        assertTrue(second.zipWithNext { previous, next -> next > previous }.all { it })
        assertTrue(first.zipWithNext { start, end -> end - start }.all { gap -> gap >= 2 })
        assertTrue(second.zipWithNext { start, end -> end - start }.all { gap -> gap >= 2 })

        val firstStride = first[1] - first[0]
        val secondStride = second[1] - second[0]
        assertEquals(firstStride, secondStride)
    }

    @Test
    fun planAxisXLabels_scrollShortLabels_smallDeltaSweep_keepsStablePattern() {
        val offsets = listOf(1_900f, 1_904f, 1_908f, 1_912f, 1_916f)
        val results =
            offsets.map { offset ->
                planAxisXLabels(
                    request =
                        AxisXPlanRequest(
                            dataSize = 240,
                            requestedMaxLabelCount = 6,
                            isScrollable = true,
                            unitWidthPx = 22f,
                            viewportWidthPx = 320f,
                            scrollOffsetPx = offset,
                            firstCenterPx = 11f,
                            labelWidthPx = 24f,
                        ),
                ).labelIndices
            }

        results.forEach { indices ->
            assertTrue(indices.size >= 2)
            assertTrue(indices.zipWithNext { previous, next -> next > previous }.all { it })
            assertTrue(indices.zipWithNext { start, end -> end - start }.all { gap -> gap >= 2 })
        }

        val baselineCount = results.first().size
        val baselineStride = results.first()[1] - results.first()[0]
        results.drop(1).forEach { indices ->
            assertTrue(indices.size in (baselineCount - 1)..(baselineCount + 1))
            assertEquals(baselineStride, indices[1] - indices[0])
        }
    }

    @Test
    fun planAxisXLabels_smallDataset_showsAllAvailableLabels() {
        val two =
            planAxisXLabels(
                request =
                    AxisXPlanRequest(
                        dataSize = 2,
                        requestedMaxLabelCount = 2,
                        isScrollable = false,
                        unitWidthPx = 120f,
                        viewportWidthPx = 300f,
                        scrollOffsetPx = 0f,
                        firstCenterPx = 64f,
                        labelWidthPx = 64f,
                    ),
            ).labelIndices
        val three =
            planAxisXLabels(
                request =
                    AxisXPlanRequest(
                        dataSize = 3,
                        requestedMaxLabelCount = 2,
                        isScrollable = false,
                        unitWidthPx = 120f,
                        viewportWidthPx = 420f,
                        scrollOffsetPx = 0f,
                        firstCenterPx = 64f,
                        labelWidthPx = 64f,
                    ),
            ).labelIndices

        assertEquals(listOf(0, 1), two)
        assertEquals(listOf(0, 1, 2), three)
    }

    @Test
    fun planAxisXLabels_balancedHybrid_prefersEvenSpacingWithinSafeEdges() {
        val result =
            planAxisXLabels(
                request =
                    AxisXPlanRequest(
                        dataSize = 12,
                        requestedMaxLabelCount = 3,
                        isScrollable = false,
                        unitWidthPx = 60f,
                        viewportWidthPx = 620f,
                        scrollOffsetPx = 0f,
                        firstCenterPx = 0f,
                        labelWidthPx = 60f,
                    ),
            )

        assertEquals(expected = 1..9, actual = result.safeRange)
        assertEquals(expected = listOf(1, 5, 9), actual = result.labelIndices)
    }

    @Test
    fun planAxisXLabels_nonScroll_prefersMoreEvenCadenceWhenSpanLossIsMinimal() {
        val result =
            planAxisXLabels(
                request =
                    AxisXPlanRequest(
                        dataSize = 12,
                        requestedMaxLabelCount = 6,
                        isScrollable = false,
                        unitWidthPx = 80f,
                        viewportWidthPx = 860f,
                        scrollOffsetPx = 0f,
                        firstCenterPx = 0f,
                        labelWidthPx = 100f,
                    ),
            )

        assertEquals(expected = 1..10, actual = result.safeRange)
        assertEquals(expected = listOf(2, 4, 6, 8, 10), actual = result.labelIndices)
    }

    @Test
    fun planAxisXLabels_nonScroll_whenOneSlotIsOmitted_prefersEdgeAnchoredSpread() {
        val result =
            planAxisXLabels(
                request =
                    AxisXPlanRequest(
                        dataSize = 7,
                        requestedMaxLabelCount = 6,
                        isScrollable = false,
                        unitWidthPx = 50f,
                        viewportWidthPx = 300f,
                        scrollOffsetPx = 0f,
                        firstCenterPx = 0f,
                        labelWidthPx = 45f,
                    ),
            )

        assertEquals(expected = 1..5, actual = result.safeRange)
        assertEquals(expected = listOf(1, 2, 4, 5), actual = result.labelIndices)
    }

    @Test
    fun planAxisXLabels_respectsLeftAndRightSafetyRange() {
        val result =
            planAxisXLabels(
                request =
                    AxisXPlanRequest(
                        dataSize = 120,
                        requestedMaxLabelCount = 6,
                        isScrollable = false,
                        unitWidthPx = 10f,
                        viewportWidthPx = 1190f,
                        scrollOffsetPx = 0f,
                        firstCenterPx = 0f,
                        labelWidthPx = 80f,
                    ),
            )

        assertEquals(expected = 5..114, actual = result.safeRange)
        assertTrue(result.labelIndices.all { index -> index in result.safeRange })
    }
}
