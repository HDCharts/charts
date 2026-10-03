package io.github.hdcharts.core.internal.density

import io.github.hdcharts.core.internal.composable.zoomInScale
import io.github.hdcharts.core.internal.composable.zoomOutScale
import kotlin.test.Test
import kotlin.test.assertEquals

class DensityHelpersTest {
    @Test
    fun denseStepForViewport_usesFitStepButAtLeastMinimumStepTimesZoom() {
        assertEquals(
            expected = 100f,
            actual = denseStepForViewport(viewportWidth = 400f, pointsCount = 5, zoomScale = 1f),
        )
        assertEquals(
            expected = DENSE_POINT_MIN_STEP_PX,
            actual = denseStepForViewport(viewportWidth = 400f, pointsCount = 1_000, zoomScale = 1f),
        )
        assertEquals(
            expected = DENSE_POINT_MIN_STEP_PX * 4f,
            actual = denseStepForViewport(viewportWidth = 400f, pointsCount = 1_000, zoomScale = 4f),
        )
        assertEquals(
            expected = DENSE_POINT_MIN_STEP_PX,
            actual = denseStepForViewport(viewportWidth = 400f, pointsCount = 1_000, zoomScale = 0.5f),
        )
    }

    @Test
    fun denseStepForViewport_withoutTwoPointsOrWidth_returnsZero() {
        assertEquals(
            expected = 0f,
            actual = denseStepForViewport(viewportWidth = 400f, pointsCount = 1, zoomScale = 1f),
        )
        assertEquals(expected = 0f, actual = denseStepForViewport(viewportWidth = 0f, pointsCount = 10, zoomScale = 1f))
    }

    @Test
    fun shouldUseScrollableDensity_respectsThreshold() {
        assertEquals(expected = false, actual = shouldUseScrollableDensity(pointsCount = 49, threshold = 50))
        assertEquals(expected = true, actual = shouldUseScrollableDensity(pointsCount = 50, threshold = 50))
    }

    @Test
    fun bucketSizeForTarget_roundsUpToFitTargetCount() {
        val bucketSize = bucketSizeForTarget(totalPoints = 120, targetPoints = 50)
        assertEquals(expected = 3, actual = bucketSize)
    }

    @Test
    fun bucketCenterIndex_usesLowerMiddleElement() {
        assertEquals(expected = 1, actual = bucketCenterIndex(0..2))
        assertEquals(expected = 0, actual = bucketCenterIndex(0..1))
    }

    @Test
    fun buildBucketRanges_andAggregatePoints_matchCompactBehavior() {
        val ranges = buildBucketRanges(totalPoints = 6, bucketSize = 2)
        val aggregatedPoints =
            aggregatePointsByAverage(
                sourcePoints = listOf(1.0, 3.0, 5.0, 7.0, 9.0, 11.0),
                bucketRanges = ranges,
            )
        assertEquals(expected = listOf(0 until 2, 2 until 4, 4 until 6), actual = ranges)
        assertEquals(expected = listOf(2.0, 6.0, 10.0), actual = aggregatedPoints)
    }

    @Test
    fun aggregateLabelsByCenterValue_usesMiddleElementPerBucket() {
        val ranges = buildBucketRanges(totalPoints = 6, bucketSize = 2)
        val labels =
            aggregateLabelsByCenterValue(
                sourceLabels = listOf("P1", "P2", "P3", "P4", "P5", "P6"),
                bucketRanges = ranges,
            )

        assertEquals(expected = listOf("P1", "P3", "P5"), actual = labels)
    }

    @Test
    fun zoomOutScale_clampsToMinZoom() {
        val updated = zoomOutScale(zoomScale = 1f, zoomStep = 1.25f, minZoom = 1f, maxZoom = 4f)
        assertEquals(expected = 1f, actual = updated)
    }

    @Test
    fun zoomInScale_clampsToMaxZoom() {
        val updated = zoomInScale(zoomScale = 3.5f, zoomStep = 1.25f, minZoom = 1f, maxZoom = 4f)
        assertEquals(expected = 4f, actual = updated)
    }
}
