package io.github.hdcharts.gauge.internal

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import io.github.hdcharts.core.internal.NO_SELECTION
import io.github.hdcharts.gauge.SegmentedRingGaugeDirection
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SegmentedRingGaugeMathTest {
    @Test
    fun compensatedSum_correctsAccumulatedRoundingAtCapacity() {
        val values = List(300) { 0.1 }

        assertEquals(expected = 30.0, actual = values.compensatedSum())
        assertFalse(totalExceedsCapacity(total = values.compensatedSum(), capacity = 30.0))
    }

    @Test
    fun compensatedSum_keepsMixedMagnitudeContributions() {
        val values = listOf(1_000_000.0) + List(300) { 0.1 }

        assertEquals(expected = 1_000_030.0, actual = values.compensatedSum())
        assertFalse(totalExceedsCapacity(total = values.compensatedSum(), capacity = 1_000_030.0))
    }

    @Test
    fun totalExceedsCapacity_rejectsMeaningfulOverCapacityValues() {
        assertTrue(totalExceedsCapacity(total = List(300) { 0.1 }.compensatedSum() + 0.001, capacity = 30.0))
        assertTrue(totalExceedsCapacity(total = Double.MIN_VALUE * 2.0, capacity = Double.MIN_VALUE))
        assertTrue(totalExceedsCapacity(total = 1.00000000000001e308, capacity = 1e308))
    }

    @Test
    fun totalExceedsCapacity_acceptsVerySmallAndVeryLargeExactCapacities() {
        assertFalse(totalExceedsCapacity(total = Double.MIN_VALUE, capacity = Double.MIN_VALUE))
        assertFalse(totalExceedsCapacity(total = Double.MAX_VALUE, capacity = Double.MAX_VALUE))
    }

    @Test
    fun segmentedRingGaugeCornerRadius_zeroUsesSquareEndedGeometryForShortAndThickRings() {
        assertEquals(
            expected = 0f,
            actual =
                segmentedRingGaugeCornerRadius(
                    cornerRadius = 0f,
                    width = 20f,
                    innerRadius = 100f,
                    sweepRadians = 1f,
                ),
        )
        assertEquals(
            expected = 0f,
            actual =
                segmentedRingGaugeCornerRadius(
                    cornerRadius = 0f,
                    width = 2f,
                    innerRadius = 1f,
                    sweepRadians = 0.01f,
                ),
        )
    }

    @Test
    fun segmentedRingGaugeLayout_withRoom_keepsTheRequestedThickness() {
        val layout = segmentedRingGaugeLayout(size = Size(400f, 200f), width = 20f)

        assertEquals(expected = Offset(200f, 100f), actual = layout.center)
        assertEquals(expected = 90f, actual = layout.radius)
        assertEquals(expected = 20f, actual = layout.width)
    }

    @Test
    fun segmentedRingGaugeLayout_inShortViewport_shrinksThicknessAndPreservesTheHole() {
        val layout = segmentedRingGaugeLayout(size = Size(600f, 80f), width = 72f)

        assertEquals(expected = Offset(300f, 40f), actual = layout.center)
        assertEquals(expected = 40f, actual = layout.radius + layout.width / 2f)
        assertEquals(
            expected = 40f * GAUGE_MIN_HOLE_FRACTION,
            actual = layout.radius - layout.width / 2f,
            absoluteTolerance = 0.0001f,
        )
    }

    @Test
    fun segmentedRingGaugeLayout_withSelectionOverflow_reservesSpaceForTheScaledSegment() {
        val layout = segmentedRingGaugeLayout(size = Size(400f, 400f), width = 20f, overflowInset = 10f)

        assertEquals(expected = 190f, actual = layout.radius + layout.width / 2f)
    }

    @Test
    fun segmentGeometries_placeValuesConsecutivelyAndLeaveConfiguredGaps() {
        val geometries =
            segmentGeometries(
                values = listOf(100.0, 200.0),
                capacity = 1_000.0,
                gapAngle = 4f,
                startAngle = -90f,
                direction = SegmentedRingGaugeDirection.Clockwise,
            )

        assertEquals(expected = -88f, actual = geometries[0].startAngle)
        assertEquals(expected = 32f, actual = geometries[0].sweepAngle)
        assertEquals(expected = -52f, actual = geometries[1].startAngle)
        assertEquals(expected = 68f, actual = geometries[1].sweepAngle)
    }

    @Test
    fun segmentGeometries_singleFullSegment_usesTheWholeCircle() {
        val geometries =
            segmentGeometries(
                values = listOf(100.0),
                capacity = 100.0,
                gapAngle = 4f,
                startAngle = -90f,
                direction = SegmentedRingGaugeDirection.Clockwise,
            )

        assertEquals(expected = -90f, actual = geometries.single().startAngle)
        assertEquals(expected = 360f, actual = geometries.single().sweepAngle)
    }

    @Test
    fun segmentGeometries_singleSegmentWithinCapacityRounding_usesTheWholeCircle() {
        val geometries =
            segmentGeometries(
                values = listOf(0.3),
                capacity = 0.1 + 0.2,
                gapAngle = 4f,
                startAngle = -90f,
                direction = SegmentedRingGaugeDirection.Clockwise,
            )

        assertEquals(expected = 360f, actual = geometries.single().sweepAngle)
    }

    @Test
    fun segmentIndexAt_selectsOnlyTheVisibleSegmentInsteadOfTheTrackOrHole() {
        val layout = segmentedRingGaugeLayout(size = Size(200f, 200f), width = 20f)
        val geometries =
            segmentGeometries(
                values = listOf(25.0, 25.0),
                capacity = 100.0,
                gapAngle = 0f,
                startAngle = -90f,
                direction = SegmentedRingGaugeDirection.Clockwise,
            )

        assertEquals(
            expected = 0,
            actual = segmentIndexAt(point = Offset(157f, 43f), layout = layout, geometries = geometries),
        )
        assertEquals(
            expected = 1,
            actual = segmentIndexAt(point = Offset(157f, 157f), layout = layout, geometries = geometries),
        )
        assertEquals(
            expected = NO_SELECTION,
            actual = segmentIndexAt(point = Offset(20f, 100f), layout = layout, geometries = geometries),
        )
        assertEquals(
            expected = NO_SELECTION,
            actual = segmentIndexAt(point = Offset(100f, 100f), layout = layout, geometries = geometries),
        )
    }

    @Test
    fun segmentGeometries_counterClockwise_placesTheNextSegmentBeforeTheFirst() {
        val geometries =
            segmentGeometries(
                values = listOf(100.0, 200.0),
                capacity = 1_000.0,
                gapAngle = 4f,
                startAngle = 220f,
                direction = SegmentedRingGaugeDirection.CounterClockwise,
            )

        assertEquals(expected = 186f, actual = geometries[0].startAngle)
        assertEquals(expected = 114f, actual = geometries[1].startAngle)
    }

    @Test
    fun remainingSegmentGeometry_counterClockwise_onlyCoversUnusedCapacity() {
        val geometry =
            remainingSegmentGeometry(
                values = listOf(240.0, 180.0, 320.0),
                capacity = 2_000.0,
                gapAngle = 4f,
                startAngle = 220f,
                direction = SegmentedRingGaugeDirection.CounterClockwise,
            )

        assertEquals(expected = -138f, actual = geometry?.startAngle)
        assertEquals(expected = 222.8f, actual = geometry?.sweepAngle)
    }

    @Test
    fun remainingSegmentGeometry_emptyGauge_hasNoGap() {
        val geometry =
            remainingSegmentGeometry(
                values = listOf(0.0, 0.0),
                capacity = 100.0,
                gapAngle = 4f,
                startAngle = -90f,
                direction = SegmentedRingGaugeDirection.Clockwise,
            )

        assertEquals(expected = -90f, actual = geometry?.startAngle)
        assertEquals(expected = 360f, actual = geometry?.sweepAngle)
    }

    @Test
    fun animatedSegmentGeometry_counterClockwise_growsFromTheConfiguredBoundary() {
        val geometry =
            segmentGeometries(
                values = listOf(100.0),
                capacity = 1_000.0,
                gapAngle = 4f,
                startAngle = 220f,
                direction = SegmentedRingGaugeDirection.CounterClockwise,
            ).single()

        val halfway =
            animatedSegmentGeometry(
                geometry = geometry,
                progress = 0.5f,
                direction = SegmentedRingGaugeDirection.CounterClockwise,
            )

        assertEquals(expected = 202f, actual = halfway.startAngle)
        assertEquals(expected = 16f, actual = halfway.sweepAngle)
    }

    @Test
    fun animatedSegmentGeometry_growsFromZeroWithoutHoldingTheFirstFrames() {
        val geometry = SegmentGeometry(startAngle = 0f, sweepAngle = 90f)
        for (direction in SegmentedRingGaugeDirection.entries) {
            for (progress in listOf(0f, 0.001f, 0.01f, 0.02f, 1f)) {
                val frame = animatedSegmentGeometry(geometry, progress, direction)

                assertEquals(expected = 90f * progress, actual = frame.sweepAngle)
                if (direction == SegmentedRingGaugeDirection.Clockwise) {
                    assertEquals(expected = 0f, actual = frame.startAngle)
                } else {
                    assertEquals(expected = 90f, actual = frame.startAngle + frame.sweepAngle)
                }
            }
        }
    }

    @Test
    fun sequentialSegmentAnimationRanges_startEachSegmentAfterThePreviousOne() {
        val ranges =
            sequentialSegmentAnimationRanges(
                geometries =
                    listOf(
                        SegmentGeometry(startAngle = 0f, sweepAngle = 30f),
                        SegmentGeometry(startAngle = 34f, sweepAngle = 60f),
                        SegmentGeometry(startAngle = 98f, sweepAngle = 90f),
                    ),
            )

        assertEquals(expected = SegmentAnimationRange(startProgress = 0f, endProgress = 1f / 6f), actual = ranges[0])
        assertEquals(
            expected = SegmentAnimationRange(startProgress = 1f / 6f, endProgress = 1f / 2f),
            actual = ranges[1],
        )
        assertEquals(expected = SegmentAnimationRange(startProgress = 1f / 2f, endProgress = 1f), actual = ranges[2])
    }

    @Test
    fun segmentAnimationRange_keepsEarlierSegmentsCompleteAndLaterSegmentsHidden() {
        val ranges =
            sequentialSegmentAnimationRanges(
                geometries =
                    listOf(
                        SegmentGeometry(startAngle = 0f, sweepAngle = 30f),
                        SegmentGeometry(startAngle = 34f, sweepAngle = 60f),
                        SegmentGeometry(startAngle = 98f, sweepAngle = 90f),
                    ),
            )

        assertEquals(expected = 1f, actual = ranges[0].progressAt(0.4f))
        assertEquals(
            expected = 0.7f,
            actual = ranges[1].progressAt(0.4f),
            absoluteTolerance = 0.0001f,
        )
        assertEquals(expected = 0f, actual = ranges[2].progressAt(0.4f))
        assertEquals(expected = 1f, actual = ranges[0].progressAt(ranges[1].startProgress))
        assertEquals(expected = 0f, actual = ranges[1].progressAt(ranges[1].startProgress))
        assertEquals(expected = 1f, actual = ranges.last().progressAt(1f))
    }

    @Test
    fun sequentialSegmentAnimationRanges_zeroSegmentsStayHiddenWithoutDelayingTheNextSegment() {
        val ranges =
            sequentialSegmentAnimationRanges(
                listOf(
                    SegmentGeometry(startAngle = 0f, sweepAngle = 0f),
                    SegmentGeometry(startAngle = 0f, sweepAngle = 40f),
                ),
            )

        assertEquals(expected = 0f, actual = ranges[0].progressAt(1f))
        assertEquals(expected = SegmentAnimationRange(0f, 1f), actual = ranges[1])
        assertEquals(expected = 0.5f, actual = ranges[1].progressAt(0.5f))
    }

    @Test
    fun sequentialSegmentAnimationRanges_emptyGaugeStaysHidden() {
        val ranges = sequentialSegmentAnimationRanges(List(3) { SegmentGeometry(0f, 0f) })

        assertEquals(expected = List(3) { 0f }, actual = ranges.map { it.progressAt(1f) })
        assertTrue(sequentialSegmentAnimationRanges(emptyList()).isEmpty())
    }

    @Test
    fun gapAngleFor_convertsThePhysicalGapAtTheCurrentRingRadius() {
        assertEquals(expected = 0f, actual = gapAngleFor(gap = 10f, radius = 0f))
        assertEquals(expected = 5.729578f, actual = gapAngleFor(gap = 10f, radius = 100f), absoluteTolerance = 0.0001f)
    }
}
