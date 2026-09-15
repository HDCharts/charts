package io.github.hdcharts.charts.unit.radarchart

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.IntSize
import io.github.hdcharts.charts.internal.NO_SELECTION
import io.github.hdcharts.charts.internal.radarchart.axisIndexForOffset
import io.github.hdcharts.charts.internal.radarchart.buildAxisLabelPositions
import io.github.hdcharts.charts.internal.radarchart.seriesAnimationProgress
import kotlin.math.abs
import kotlin.math.hypot
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RadarChartHelpersTest {
    private val eps = 0.0001f
    private val pixelEps = 0.01f

    private fun centerOf(size: IntSize) = Offset(size.width / 2f, size.height / 2f)

    private fun topOf(size: IntSize) = Offset(size.width / 2f, 0f + pixelEps)

    private fun rightOf(size: IntSize) = Offset(size.width - pixelEps, size.height / 2f)

    private fun bottomOf(size: IntSize) = Offset(size.width / 2f, size.height - pixelEps)

    private fun leftOf(size: IntSize) = Offset(0f + pixelEps, size.height / 2f)

    private fun assertOffsetEquals(
        expected: Offset,
        actual: Offset,
        tolerance: Float = eps,
    ) {
        assertEquals(expected.x, actual.x, tolerance)
        assertEquals(expected.y, actual.y, tolerance)
    }

    private fun distance(
        a: Offset,
        b: Offset,
    ): Float = hypot((a.x - b.x).toDouble(), (a.y - b.y).toDouble()).toFloat()

    @Test
    fun seriesAnimationProgress_singleSeries_returnsInputProgress() {
        val actual = seriesAnimationProgress(index = 0, total = 1, animationProgress = 0.3f)
        assertEquals(0.3f, actual, eps)
    }

    @Test
    fun seriesAnimationProgress_staggersSeries_soLaterSeriesStayAtZeroInitially() {
        val actual = seriesAnimationProgress(index = 1, total = 3, animationProgress = 0.225f)
        assertEquals(0f, actual, eps)
    }

    @Test
    fun axisIndexForOffset_center_returnsNoSelection() {
        val size = IntSize(100, 100)
        val actual = axisIndexForOffset(offset = centerOf(size), size = size, axisCount = 6)
        assertEquals(NO_SELECTION, actual)
    }

    @Test
    fun axisIndexForOffset_cardinalDirections_matchExpectedAxes() {
        val size = IntSize(100, 100)
        val axisCount = 4

        assertEquals(0, axisIndexForOffset(offset = topOf(size), size = size, axisCount = axisCount))
        assertEquals(1, axisIndexForOffset(offset = rightOf(size), size = size, axisCount = axisCount))
        assertEquals(2, axisIndexForOffset(offset = bottomOf(size), size = size, axisCount = axisCount))
        assertEquals(3, axisIndexForOffset(offset = leftOf(size), size = size, axisCount = axisCount))
    }

    @Test
    fun buildAxisLabelPositions_returnsPointsOnCircle_inCardinalOrderFor4Axes() {
        val axisCount = 4
        val center = Offset(0f, 0f)
        val radius = 10f

        val positions = buildAxisLabelPositions(axisCount = axisCount, center = center, radius = radius)

        assertEquals(axisCount, positions.size)

        // Semantic invariant: every label is on the radius circle
        positions.forEachIndexed { i, p ->
            val d = distance(center, p)
            assertTrue(abs(d - radius) <= eps, "Axis $i expected distance=$radius but was $d")
        }

        // Clear intent: 4-axis radar usually maps to top/right/bottom/left
        val expected =
            listOf(
                // top
                Offset(0f, -radius),
                // right
                Offset(radius, 0f),
                // bottom
                Offset(0f, radius),
                // left
                Offset(-radius, 0f),
            )

        expected.zip(positions).forEachIndexed { i, (e, a) ->
            assertOffsetEquals(e, a, eps)
        }
    }
}
