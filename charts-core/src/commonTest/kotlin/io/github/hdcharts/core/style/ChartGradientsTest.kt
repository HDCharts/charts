package io.github.hdcharts.core.style

import androidx.compose.ui.graphics.Color
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ChartGradientsTest {
    private val colors = listOf(Color.Red, Color.Green, Color.Blue)

    @Test
    fun vertical_runsTopToBottom() {
        val gradient = ChartGradients.vertical(colors = colors)

        assertPoint(expected = GradientPoint(x = 0.5f, y = 0f), actual = gradient.start)
        assertPoint(expected = GradientPoint(x = 0.5f, y = 1f), actual = gradient.end)
    }

    @Test
    fun horizontal_runsLeftToRight() {
        val gradient = ChartGradients.horizontal(colors = colors)

        assertPoint(expected = GradientPoint(x = 0f, y = 0.5f), actual = gradient.start)
        assertPoint(expected = GradientPoint(x = 1f, y = 0.5f), actual = gradient.end)
    }

    @Test
    fun linear45_runsCornerToCorner() {
        val gradient = ChartGradients.linear(angleDegrees = 45f, colors = colors)

        assertPoint(expected = GradientPoint(x = 0f, y = 0f), actual = gradient.start)
        assertPoint(expected = GradientPoint(x = 1f, y = 1f), actual = gradient.end)
    }

    @Test
    fun colors_areEvenlySpacedFixedStops() {
        assertEquals<List<GradientStop>?>(
            expected =
                listOf(
                    GradientStop.Fixed(offset = 0f, color = Color.Red),
                    GradientStop.Fixed(offset = 0.5f, color = Color.Green),
                    GradientStop.Fixed(offset = 1f, color = Color.Blue),
                ),
            actual = ChartGradients.horizontal(colors = colors).stops,
        )
    }

    @Test
    fun span_defaultsToShapeAndIsKept() {
        assertEquals(expected = GradientSpan.Shape, actual = ChartGradients.horizontal(colors = colors).span)
        assertEquals(
            expected = GradientSpan.Plot,
            actual = ChartGradients.vertical(colors = colors, span = GradientSpan.Plot).span,
        )
    }

    @Test
    fun fade_runsFromShapeColorToEndAlpha() {
        assertEquals<List<GradientStop>?>(
            expected =
                listOf(
                    GradientStop.Series(offset = 0f, alpha = 1f),
                    GradientStop.Series(offset = 1f, alpha = 0.3f),
                ),
            actual = ChartGradients.fade(endAlpha = 0.3f).stops,
        )
    }

    @Test
    fun clamp_clampsSeriesAlphasAndKeepsFixedColors() {
        val gradient =
            ChartGradients.vertical(colors = listOf(Color.Red)).copy(
                stops =
                    persistentListOf(
                        GradientStop.Series(offset = 0f, alpha = Float.NaN),
                        GradientStop.Series(offset = 0.5f, alpha = 2f),
                        GradientStop.Fixed(offset = 1f, color = Color.Blue),
                    ),
            )

        assertEquals<List<GradientStop>?>(
            expected =
                listOf(
                    GradientStop.Series(offset = 0f, alpha = 1f),
                    GradientStop.Series(offset = 0.5f, alpha = 1f),
                    GradientStop.Fixed(offset = 1f, color = Color.Blue),
                ),
            actual = gradient.clamp()?.stops,
        )
    }

    @Test
    fun clamp_coercesOffsetsIntoRangeAndSortsThem() {
        val gradient =
            gradient(
                GradientStop.Fixed(offset = 1.5f, color = Color.Blue),
                GradientStop.Fixed(offset = -0.2f, color = Color.Red),
                GradientStop.Fixed(offset = 0.5f, color = Color.Green),
            )

        assertEquals<List<GradientStop>?>(
            expected =
                listOf(
                    GradientStop.Fixed(offset = 0f, color = Color.Red),
                    GradientStop.Fixed(offset = 0.5f, color = Color.Green),
                    GradientStop.Fixed(offset = 1f, color = Color.Blue),
                ),
            actual = gradient.clamp()?.stops,
        )
    }

    @Test
    fun clamp_dropsNonFiniteOffsets() {
        val gradient =
            gradient(
                GradientStop.Fixed(offset = 0f, color = Color.Red),
                GradientStop.Fixed(offset = Float.NaN, color = Color.Green),
                GradientStop.Fixed(offset = 1f, color = Color.Blue),
            )

        assertEquals<List<GradientStop>?>(
            expected =
                listOf(
                    GradientStop.Fixed(offset = 0f, color = Color.Red),
                    GradientStop.Fixed(offset = 1f, color = Color.Blue),
                ),
            actual = gradient.clamp()?.stops,
        )
    }

    @Test
    fun clamp_withOneStop_paintsItsColorSolid() {
        assertEquals<List<GradientStop>?>(
            expected =
                listOf(
                    GradientStop.Fixed(offset = 0f, color = Color.Red),
                    GradientStop.Fixed(offset = 1f, color = Color.Red),
                ),
            actual = ChartGradients.vertical(colors = listOf(Color.Red)).clamp()?.stops,
        )
    }

    @Test
    fun clamp_withNoStops_returnsNull() {
        assertNull(ChartGradients.vertical(colors = emptyList()).clamp())
        assertNull(gradient(GradientStop.Fixed(offset = Float.NaN, color = Color.Red)).clamp())
    }

    @Test
    fun clamp_withZeroLengthOrNonFinitePositions_returnsNull() {
        val gradient = ChartGradients.vertical(colors = colors)

        listOf(
            GradientPoint(x = 0.5f, y = 0f),
            GradientPoint(x = 0.5f, y = -0f),
            GradientPoint(x = 0.5f, y = Float.POSITIVE_INFINITY),
            GradientPoint(x = Float.NaN, y = 1f),
        ).forEach { end -> assertNull(gradient.copy(end = end).clamp(), "end = $end") }
    }

    private fun gradient(vararg stops: GradientStop) =
        ChartGradients.vertical(colors = colors).copy(stops = stops.toList().toImmutableList())

    private fun assertPoint(
        expected: GradientPoint,
        actual: GradientPoint,
    ) {
        assertEquals(expected = expected.x, actual = actual.x, absoluteTolerance = 1e-6f)
        assertEquals(expected = expected.y, actual = actual.y, absoluteTolerance = 1e-6f)
    }
}
