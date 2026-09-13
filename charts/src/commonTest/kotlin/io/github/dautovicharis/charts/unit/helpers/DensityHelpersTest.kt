package io.github.dautovicharis.charts.unit.helpers

import io.github.dautovicharis.charts.internal.common.composable.zoomInScale
import io.github.dautovicharis.charts.internal.common.composable.zoomOutScale
import io.github.dautovicharis.charts.internal.common.density.aggregateLabelsByCenterValue
import io.github.dautovicharis.charts.internal.common.density.aggregateLabelsByLastValue
import io.github.dautovicharis.charts.internal.common.density.aggregatePointsByAverage
import io.github.dautovicharis.charts.internal.common.density.bucketCenterIndex
import io.github.dautovicharis.charts.internal.common.density.bucketSizeForTarget
import io.github.dautovicharis.charts.internal.common.density.buildBucketRanges
import io.github.dautovicharis.charts.internal.common.density.shouldUseScrollableDensity
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DensityHelpersTest {
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
    fun buildBucketRanges_andAggregateHelpers_matchCompactBehavior() {
        val ranges = buildBucketRanges(totalPoints = 6, bucketSize = 2)
        val aggregatedPoints =
            aggregatePointsByAverage(
                sourcePoints = listOf(1.0, 3.0, 5.0, 7.0, 9.0, 11.0),
                bucketRanges = ranges,
            )
        val aggregatedLabels =
            aggregateLabelsByLastValue(
                sourceLabels = listOf("P1", "P2", "P3", "P4", "P5", "P6"),
                bucketRanges = ranges,
            )

        assertEquals(expected = listOf(0 until 2, 2 until 4, 4 until 6), actual = ranges)
        assertEquals(expected = listOf(2.0, 6.0, 10.0), actual = aggregatedPoints)
        assertEquals(expected = listOf("P2", "P4", "P6"), actual = aggregatedLabels)
    }

    @Test
    fun aggregateLabelsByLastValue_usesBucketFallbackWhenLabelMissing() {
        val ranges = buildBucketRanges(totalPoints = 4, bucketSize = 2)
        val labels = aggregateLabelsByLastValue(sourceLabels = listOf("Only"), bucketRanges = ranges)
        assertTrue(labels[1].startsWith("Bucket "))
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
