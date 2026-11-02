package io.github.hdcharts.scatter.internal

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import io.github.hdcharts.core.internal.NO_SELECTION
import io.github.hdcharts.scatter.ScatterPoint
import io.github.hdcharts.scatter.ScatterRangeStyle
import kotlin.test.Test
import kotlin.test.assertEquals

class ScatterMathTest {
    @Test
    fun normalizedPositions_preserveSourceOrderAndInvertTheYAxis() {
        val points = listOf(ScatterPoint(0.0, 10.0), ScatterPoint(-10.0, 0.0), ScatterPoint(10.0, 20.0))
        val domain = scatterDomain(points, ScatterRangeStyle())

        assertEquals(expected = ScatterDomain(-10.0, 10.0, 0.0, 20.0), actual = domain)
        assertEquals(
            expected = listOf(Offset(0.5f, 0.5f), Offset(0f, 1f), Offset(1f, 0f)),
            actual = normalizedScatterPositions(points, domain),
        )
    }

    @Test
    fun scatterDomain_resolvesEachAutomaticBoundIndependently() {
        val points = listOf(ScatterPoint(0.0, 10.0), ScatterPoint(10.0, 20.0))
        val domain = scatterDomain(points, ScatterRangeStyle(xMin = -10.0, yMax = 30.0))

        assertEquals(expected = ScatterDomain(-10.0, 10.0, 10.0, 30.0), actual = domain)
        assertEquals(
            expected = listOf(Offset(0.5f, 1f), Offset(1f, 0.5f)),
            actual = normalizedScatterPositions(points, domain),
        )
    }

    @Test
    fun normalizedPositions_constantCoordinatesUseTheExistingUnitSpan() {
        val points = listOf(ScatterPoint(5.0, 10.0))
        val domain = scatterDomain(points, ScatterRangeStyle())

        assertEquals(expected = 1.0, actual = domain.xRange)
        assertEquals(expected = 1.0, actual = domain.yRange)
        assertEquals(expected = listOf(Offset(0f, 1f)), actual = normalizedScatterPositions(points, domain))
    }

    @Test
    fun pointRadii_interpolateBubbleAreaAndKeepUnsizedPointsAtTheirConfiguredRadius() {
        val radii =
            scatterPointRadii(
                points =
                    listOf(
                        ScatterPoint(0.0, 0.0),
                        ScatterPoint(1.0, 1.0, size = 0.0),
                        ScatterPoint(2.0, 2.0, size = 5.0),
                        ScatterPoint(3.0, 3.0, size = 10.0),
                    ),
                pointRadius = 3f,
                minBubbleRadius = 4f,
                maxBubbleRadius = 10f,
            )

        assertEquals(expected = 3f, actual = radii[0])
        assertEquals(expected = 4f, actual = radii[1])
        assertEquals(expected = 58f, actual = radii[2] * radii[2], absoluteTolerance = 0.0001f)
        assertEquals(expected = 10f, actual = radii[3])
    }

    @Test
    fun pointRadii_equalBubbleSizesUseTheMiddleArea() {
        val radius =
            scatterPointRadii(
                points = listOf(ScatterPoint(1.0, 2.0, size = 5.0)),
                pointRadius = 3f,
                minBubbleRadius = 4f,
                maxBubbleRadius = 10f,
            ).single()

        assertEquals(expected = 58f, actual = radius * radius, absoluteTolerance = 0.0001f)
    }

    @Test
    fun nearestPoint_usesTheDrawnPixelPositionsInAWidePlot() {
        val layout = scatterPlotLayout(size = Size(1056f, 156f), padding = 28f)
        val positions = listOf(Offset(0f, 0.6f), Offset(0.2f, 0f))

        assertEquals(expected = Offset(28f, 88f), actual = layout.position(positions[0]))
        assertEquals(expected = Offset(228f, 28f), actual = layout.position(positions[1]))
        assertEquals(
            expected = 0,
            actual = nearestScatterPointIndex(point = Offset(28f, 28f), positions = positions, layout = layout),
        )
    }

    @Test
    fun nearestPoint_tiesKeepTheFirstSourceIndexAndEmptyPointsHaveNoSelection() {
        val layout = scatterPlotLayout(size = Size(256f, 156f), padding = 28f)
        val point = Offset(128f, 78f)

        assertEquals(
            expected = 0,
            actual = nearestScatterPointIndex(point, listOf(Offset(0f, 0.5f), Offset(1f, 0.5f)), layout),
        )
        assertEquals(expected = NO_SELECTION, actual = nearestScatterPointIndex(point, emptyList(), layout))
    }

    @Test
    fun plotLayout_keepsAPositiveSpanInATinyViewport() {
        val layout = scatterPlotLayout(size = Size(20f, 10f), padding = 28f)

        assertEquals(expected = 1f, actual = layout.width)
        assertEquals(expected = 1f, actual = layout.height)
        assertEquals(expected = Offset(29f, 29f), actual = layout.position(Offset(1f, 1f)))
    }

    @Test
    fun plotLayout_reservesAxisSpaceForDrawingAndHitTesting() {
        val layout =
            scatterPlotLayout(Size(300f, 200f), padding = 28f, yAxisSpace = 60f, xAxisSpace = 50f, xAxisEdgeSpace = 40f)
        val positions = listOf(Offset(0f, 1f), Offset(1f, 0f))

        assertEquals(expected = Offset(60f, 150f), actual = layout.position(positions[0]))
        assertEquals(expected = Offset(260f, 28f), actual = layout.position(positions[1]))
        assertEquals(expected = 1, actual = nearestScatterPointIndex(Offset(260f, 28f), positions, layout))
    }

    @Test
    fun scatterDomain_preservesASingleFixedBoundWhenItCrossesTheData() {
        val points = listOf(ScatterPoint(1.0, 2.0), ScatterPoint(3.0, 4.0))
        val domain = scatterDomain(points, ScatterRangeStyle(xMin = 10.0, yMax = 0.0))

        assertEquals(expected = ScatterDomain(10.0, 10.0, 0.0, 0.0), actual = domain)
    }

    @Test
    fun xAxisValues_includeBothBoundsWithoutExposingSubtractionDrift() {
        val domain = ScatterDomain(1.8, 4.2, 0.0, 1.0)
        val values = scatterXAxisValues(domain, count = 5)

        assertEquals(expected = 1.8, actual = values.first())
        assertEquals(expected = 4.2, actual = values.last())
        assertEquals(expected = 3.0, actual = values[2])
        val largeDomain = ScatterDomain(-Double.MAX_VALUE, Double.MAX_VALUE, 0.0, 1.0)
        assertEquals(
            expected = listOf(-Double.MAX_VALUE, 0.0, Double.MAX_VALUE),
            actual = scatterXAxisValues(largeDomain, count = 3),
        )
    }
}
