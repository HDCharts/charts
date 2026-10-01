package io.github.hdcharts.radar.internal

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.IntSize
import io.github.hdcharts.core.internal.NO_SELECTION
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

    @Test
    fun seriesCandidatesAt_nearOutline_returnsOnlySeriesWithinTouchRadius() {
        val candidates = seriesCandidatesAt(tap = Offset(61f, 50f), polygons = nestedSquares, touchRadius = 5f)

        assertEquals(expected = listOf(0), actual = candidates)
    }

    @Test
    fun seriesCandidatesAt_nearSeveralOutlines_ordersByDistance() {
        val polygons =
            listOf(
                square(left = 52f, top = 0f, size = 40f),
                square(left = 10f, top = 0f, size = 40f),
            )

        val candidates = seriesCandidatesAt(tap = Offset(50.5f, 20f), polygons = polygons, touchRadius = 5f)

        assertEquals(expected = listOf(1, 0), actual = candidates)
    }

    @Test
    fun seriesCandidatesAt_insideAwayFromOutlines_returnsContainingSeriesSmallestFirst() {
        val candidates = seriesCandidatesAt(tap = Offset(50f, 50f), polygons = nestedSquares, touchRadius = 5f)

        assertEquals(expected = listOf(0, 1), actual = candidates)
    }

    @Test
    fun seriesCandidatesAt_outsideEverySeries_returnsEmpty() {
        val candidates = seriesCandidatesAt(tap = Offset(150f, 150f), polygons = nestedSquares, touchRadius = 5f)

        assertTrue(candidates.isEmpty())
    }

    @Test
    fun nextFocusedSeries_cyclesThroughCandidatesAndStartsFromTheFirst() {
        val candidates = listOf(2, 0)

        assertEquals(expected = NO_SELECTION, actual = nextFocusedSeries(candidates = emptyList(), focusedIndex = 2))
        assertEquals(expected = 2, actual = nextFocusedSeries(candidates = candidates, focusedIndex = NO_SELECTION))
        assertEquals(expected = 0, actual = nextFocusedSeries(candidates = candidates, focusedIndex = 2))
        assertEquals(expected = 2, actual = nextFocusedSeries(candidates = candidates, focusedIndex = 0))
        assertEquals(expected = 2, actual = nextFocusedSeries(candidates = candidates, focusedIndex = 1))
    }

    @Test
    fun radarPlotRadius_withoutLabels_fillsTheSmallerSide() {
        val radius =
            radarPlotRadius(
                axisCount = 4,
                widthPx = 300f,
                heightPx = 200f,
                labelWidthPx = 0f,
                labelHeightPx = 0f,
                labelPaddingPx = 0f,
            )

        assertEquals(expected = 100f, actual = radius)
    }

    @Test
    fun radarPlotRadius_withLabels_leavesRoomForTheWidestAndTallestLabel() {
        // The left and right labels leave 150 - 60 - 10 = 80, the top and bottom ones 150 - 20 - 10 = 120.
        val radius =
            radarPlotRadius(
                axisCount = 4,
                widthPx = 300f,
                heightPx = 300f,
                labelWidthPx = 60f,
                labelHeightPx = 20f,
                labelPaddingPx = 10f,
            )

        assertEquals(expected = 80f, actual = radius)
    }

    @Test
    fun radarPlotRadius_withDiagonalLabels_keepsTheWebTallerThanTheLabelWidth() {
        // A six axis web has axes at -90°, -30°, 30°, 90°, 150°, 210°. The ±30° diagonal axes
        // constrain the radius more than the vertical ones because their cos/sin components
        // require more horizontal clearance for the wide label.
        val radius =
            radarPlotRadius(
                axisCount = 6,
                widthPx = 300f,
                heightPx = 300f,
                labelWidthPx = 60f,
                labelHeightPx = 20f,
                labelPaddingPx = 10f,
            )

        // The ±30° axes limit radius to ~97.6, not the vertical axis's 120.
        assertEquals(expected = 97.58f, actual = radius, absoluteTolerance = 0.02f)
    }

    @Test
    fun radarPlotRadius_withHugeLabels_keepsAQuarterOfTheSmallerSide() {
        val radius =
            radarPlotRadius(
                axisCount = 4,
                widthPx = 200f,
                heightPx = 300f,
                labelWidthPx = 500f,
                labelHeightPx = 20f,
                labelPaddingPx = 10f,
            )

        assertEquals(expected = 50f, actual = radius)
    }

    @Test
    fun axisLabelTopLeft_placesEachLabelOutsideItsAnchor() {
        val center = Offset(100f, 100f)

        assertOffsetEquals(
            expected = Offset(160f, 90f),
            actual = axisLabelTopLeft(anchor = Offset(160f, 100f), center = center, widthPx = 40, heightPx = 20),
        )
        assertOffsetEquals(
            expected = Offset(80f, 20f),
            actual = axisLabelTopLeft(anchor = Offset(100f, 40f), center = center, widthPx = 40, heightPx = 20),
        )
        assertOffsetEquals(
            expected = Offset(0f, 90f),
            actual = axisLabelTopLeft(anchor = Offset(40f, 100f), center = center, widthPx = 40, heightPx = 20),
        )
    }

    @Test
    fun axisLabelTopLeft_onAVerticalAxis_keepsAWideLabelOneHeightAboveTheAnchor() {
        val topLeft =
            axisLabelTopLeft(
                anchor = Offset(100f, 40f),
                center = Offset(100f, 100f),
                widthPx = 60,
                heightPx = 20,
            )

        // The bottom edge sits on the anchor, so a wide label does not float half its width above it.
        assertEquals(expected = 40f, actual = topLeft.y + 20f, absoluteTolerance = 0.5f)
        assertEquals(expected = 70f, actual = topLeft.x, absoluteTolerance = 0.5f)
    }

    @Test
    fun axisLabelTopLeft_onADiagonalAxis_putsTheAnchorOnTheInnerEdge() {
        val center = Offset(100f, 100f)
        val anchor = Offset(160f, 40f)

        val topLeft = axisLabelTopLeft(anchor = anchor, center = center, widthPx = 60, heightPx = 20)

        val unitX = 0.70710678f
        val unitY = -0.70710678f
        val labelCenterX = topLeft.x + 30f
        val labelCenterY = topLeft.y + 10f
        val innerEdge =
            labelCenterX * unitX +
                labelCenterY * unitY -
                (abs(unitX) * 60f + abs(unitY) * 20f) / 2f

        assertEquals(expected = anchor.x * unitX + anchor.y * unitY, actual = innerEdge, absoluteTolerance = 0.5f)
    }

    private val nestedSquares =
        listOf(
            square(left = 40f, top = 40f, size = 20f),
            square(left = 0f, top = 0f, size = 100f),
        )

    private fun square(
        left: Float,
        top: Float,
        size: Float,
    ): List<Offset> =
        listOf(
            Offset(left, top),
            Offset(left + size, top),
            Offset(left + size, top + size),
            Offset(left, top + size),
        )
}
