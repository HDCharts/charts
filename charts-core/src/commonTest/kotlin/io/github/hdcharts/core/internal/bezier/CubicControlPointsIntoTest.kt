package io.github.hdcharts.core.internal.bezier

import androidx.compose.ui.geometry.Offset
import kotlin.test.Test
import kotlin.test.assertContentEquals

class CubicControlPointsIntoTest {
    @Test
    fun middleSegment_writesTheSameValuesAsTheObjectForm() {
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

        assertSameAsObjectForm(
            out = out,
            p0 = Offset(0f, 10f),
            p1 = Offset(10f, 20f),
            p2 = Offset(20f, 30f),
            p3 = Offset(30f, 40f),
        )
    }

    @Test
    fun firstSegment_repeatsTheStartPoint_writesTheSameValuesAsTheObjectForm() {
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

        assertSameAsObjectForm(
            out = out,
            p0 = Offset(10f, 20f),
            p1 = Offset(10f, 20f),
            p2 = Offset(20f, 30f),
            p3 = Offset(30f, 40f),
        )
    }

    @Test
    fun lastSegment_repeatsTheEndPoint_writesTheSameValuesAsTheObjectForm() {
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

        assertSameAsObjectForm(
            out = out,
            p0 = Offset(0f, 10f),
            p1 = Offset(10f, 20f),
            p2 = Offset(20f, 30f),
            p3 = Offset(20f, 30f),
        )
    }

    @Test
    fun yBoundsProvided_clampsTheSameWayAsTheObjectForm() {
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

        assertSameAsObjectForm(
            out = out,
            p0 = Offset(0f, -200f),
            p1 = Offset(10f, 20f),
            p2 = Offset(20f, 30f),
            p3 = Offset(30f, 400f),
            minY = 0f,
            maxY = 100f,
        )
    }

    @Test
    fun tensionProvided_writesTheSameValuesAsTheObjectForm() {
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

        assertSameAsObjectForm(
            out = out,
            p0 = Offset(0f, 10f),
            p1 = Offset(10f, 20f),
            p2 = Offset(20f, 30f),
            p3 = Offset(30f, 40f),
            tension = 0.4f,
        )
    }

    @Test
    fun reversedYBounds_theSameBoundsAreUsedAsTheObjectForm() {
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

        assertSameAsObjectForm(
            out = out,
            p0 = Offset(0f, 0f),
            p1 = Offset(10f, 20f),
            p2 = Offset(20f, 30f),
            p3 = Offset(30f, 40f),
            minY = 100f,
            maxY = 0f,
        )
    }

    private fun assertSameAsObjectForm(
        out: FloatArray,
        p0: Offset,
        p1: Offset,
        p2: Offset,
        p3: Offset,
        tension: Float = DEFAULT_BEZIER_TENSION,
        minY: Float = Float.NEGATIVE_INFINITY,
        maxY: Float = Float.POSITIVE_INFINITY,
    ) {
        val controls =
            cubicControlPoints(p0 = p0, p1 = p1, p2 = p2, p3 = p3, tension = tension, minY = minY, maxY = maxY)

        assertContentEquals(
            expected = floatArrayOf(controls.first.x, controls.first.y, controls.second.x, controls.second.y),
            actual = out,
        )
    }
}
