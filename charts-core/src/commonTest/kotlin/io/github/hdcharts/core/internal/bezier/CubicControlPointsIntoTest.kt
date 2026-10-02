package io.github.hdcharts.core.internal.bezier

import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * The bezier control point formula, checked against values worked out by hand.
 *
 * The expected numbers here are deliberately not produced by `cubicControlPoints`, which now
 * delegates to the function under test and would only compare it with itself.
 */
class CubicControlPointsIntoTest {
    @Test
    fun middleSegment_writesTheExpectedControlPoints() {
        // factor = 0.95 / 6 = 0.1583333
        // c1x = 10 + (20 - 0) * factor = 13.166667
        // c1y = 20 + (30 - 10) * factor = 23.166666
        // c2x = 20 - (30 - 10) * factor = 16.833334
        // c2y = 30 - (40 - 20) * factor = 26.833334
        val out = FloatArray(CUBIC_CONTROL_POINT_COUNT)

        cubicControlPointsInto(
            out = out,
            p0x = 0f,
            p0y = 10f,
            p1x = 10f,
            p1y = 20f,
            p2x = 20f,
            p2y = 30f,
            p3x = 30f,
            p3y = 40f,
        )

        assertControlPoints(
            expected = floatArrayOf(13.166667f, 23.166666f, 16.833334f, 26.833334f),
            actual = out,
        )
    }

    @Test
    fun firstSegment_repeatsTheStartPoint_writesTheExpectedControlPoints() {
        // With no point before it, p0 is p1, so the first control point sits a third of a factor
        // along the segment instead of a full one.
        // c1x = 10 + (20 - 10) * factor = 11.583333
        // c1y = 20 + (30 - 20) * factor = 21.583332
        // c2x = 20 - (30 - 10) * factor = 16.833334
        // c2y = 30 - (40 - 20) * factor = 26.833334
        val out = FloatArray(CUBIC_CONTROL_POINT_COUNT)

        cubicControlPointsInto(
            out = out,
            p0x = 10f,
            p0y = 20f,
            p1x = 10f,
            p1y = 20f,
            p2x = 20f,
            p2y = 30f,
            p3x = 30f,
            p3y = 40f,
        )

        assertControlPoints(
            expected = floatArrayOf(11.583333f, 21.583332f, 16.833334f, 26.833334f),
            actual = out,
        )
    }

    @Test
    fun lastSegment_repeatsTheEndPoint_writesTheExpectedControlPoints() {
        // With no point after it, p3 is p2, so the second control point sits a third of a factor
        // back from the end instead of a full one.
        // c1x = 10 + (20 - 0) * factor = 13.166667
        // c1y = 20 + (30 - 10) * factor = 23.166666
        // c2x = 20 - (20 - 10) * factor = 18.416666
        // c2y = 30 - (30 - 20) * factor = 28.416666
        val out = FloatArray(CUBIC_CONTROL_POINT_COUNT)

        cubicControlPointsInto(
            out = out,
            p0x = 0f,
            p0y = 10f,
            p1x = 10f,
            p1y = 20f,
            p2x = 20f,
            p2y = 30f,
            p3x = 20f,
            p3y = 30f,
        )

        assertControlPoints(
            expected = floatArrayOf(13.166667f, 23.166666f, 18.416666f, 28.416666f),
            actual = out,
        )
    }

    @Test
    fun yBoundsProvided_clampsTheControlPointsIntoThem() {
        // Unclamped c1y would be 20 + (30 - (-200)) * factor = 56.416666, inside the bounds, and
        // c2y would be 30 - (400 - 20) * factor = -30.166666, below minY, so it clamps to 0.
        // c1x = 10 + (20 - 0) * factor = 13.166667
        // c2x = 20 - (30 - 10) * factor = 16.833334
        val out = FloatArray(CUBIC_CONTROL_POINT_COUNT)

        cubicControlPointsInto(
            out = out,
            p0x = 0f,
            p0y = -200f,
            p1x = 10f,
            p1y = 20f,
            p2x = 20f,
            p2y = 30f,
            p3x = 30f,
            p3y = 400f,
            minY = 0f,
            maxY = 100f,
        )

        assertControlPoints(
            expected = floatArrayOf(13.166667f, 56.416666f, 16.833334f, 0f),
            actual = out,
        )
    }

    @Test
    fun tensionProvided_movesTheControlPointsAlongTheSegment() {
        // factor = 0.4 / 6 = 0.0666667
        // c1x = 10 + (20 - 0) * factor = 11.333334
        // c1y = 20 + (30 - 10) * factor = 21.333332
        // c2x = 20 - (30 - 10) * factor = 18.666666
        // c2y = 30 - (40 - 20) * factor = 28.666666
        val out = FloatArray(CUBIC_CONTROL_POINT_COUNT)

        cubicControlPointsInto(
            out = out,
            p0x = 0f,
            p0y = 10f,
            p1x = 10f,
            p1y = 20f,
            p2x = 20f,
            p2y = 30f,
            p3x = 30f,
            p3y = 40f,
            tension = 0.4f,
        )

        assertControlPoints(
            expected = floatArrayOf(11.333334f, 21.333332f, 18.666666f, 28.666666f),
            actual = out,
        )
    }

    @Test
    fun reversedYBounds_clampsBetweenThemRatherThanSwapping() {
        // Reversed bounds still describe one interval, so minY 100 and maxY 0 clamp to 100 and 0.
        // Unclamped c1y = 20 + (30 - 0) * factor = 24.75 and c2y = 30 - (40 - 20) * factor =
        // 26.833334, both inside the reversed interval once ordered, so both survive.
        // c1x = 10 + (20 - 0) * factor = 13.166667
        // c2x = 20 - (30 - 10) * factor = 16.833334
        val out = FloatArray(CUBIC_CONTROL_POINT_COUNT)

        cubicControlPointsInto(
            out = out,
            p0x = 0f,
            p0y = 0f,
            p1x = 10f,
            p1y = 20f,
            p2x = 20f,
            p2y = 30f,
            p3x = 30f,
            p3y = 40f,
            minY = 100f,
            maxY = 0f,
        )

        assertControlPoints(
            expected = floatArrayOf(13.166667f, 24.75f, 16.833334f, 26.833334f),
            actual = out,
        )
    }

    @Test
    fun defaultTension_writesTheSameValuesAsTheDefaultConstant() {
        val out = FloatArray(CUBIC_CONTROL_POINT_COUNT)

        cubicControlPointsInto(
            out = out,
            p0x = 0f,
            p0y = 10f,
            p1x = 10f,
            p1y = 20f,
            p2x = 20f,
            p2y = 30f,
            p3x = 30f,
            p3y = 40f,
            tension = DEFAULT_BEZIER_TENSION,
        )

        assertControlPoints(
            expected = floatArrayOf(13.166667f, 23.166666f, 16.833334f, 26.833334f),
            actual = out,
        )
    }

    private fun assertControlPoints(
        expected: FloatArray,
        actual: FloatArray,
    ) {
        for (index in expected.indices) {
            assertEquals(
                expected = expected[index],
                actual = actual[index],
                absoluteTolerance = 0.0001f,
                message = "Control point ${index / 2} (${if (index % 2 == 0) "x" else "y"})",
            )
        }
    }
}
