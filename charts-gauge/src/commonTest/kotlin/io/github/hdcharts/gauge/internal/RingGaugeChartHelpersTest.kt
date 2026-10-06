package io.github.hdcharts.gauge.internal

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import io.github.hdcharts.core.internal.NO_SELECTION
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RingGaugeChartHelpersTest {
    @Test
    fun gaugeFraction_insideRange_isShareOfSpan() {
        assertEquals(expected = 0.25f, actual = gaugeFraction(value = 25.0, min = 0.0, max = 100.0))
        assertEquals(expected = 0.5f, actual = gaugeFraction(value = 0.0, min = -20.0, max = 20.0))
    }

    @Test
    fun gaugeFraction_outsideRange_stopsAtTheNearestEnd() {
        assertEquals(expected = 0f, actual = gaugeFraction(value = -5.0, min = 0.0, max = 100.0))
        assertEquals(expected = 1f, actual = gaugeFraction(value = 150.0, min = 0.0, max = 100.0))
    }

    @Test
    fun gaugeFraction_withEmptySpan_isZero() {
        assertEquals(expected = 0f, actual = gaugeFraction(value = 5.0, min = 10.0, max = 10.0))
    }

    @Test
    fun ringGaugeLayout_withRoom_drawsRingsAtMaxWidthFromTheOutside() {
        val layout =
            ringGaugeLayout(size = Size(400f, 200f), ringCount = 2, maxRingWidth = 20f, spacing = 4f, labelBand = 0f)

        assertEquals(expected = Offset(200f, 200f), actual = layout.center)
        assertEquals(expected = 200f, actual = layout.outerRadius)
        assertEquals(expected = listOf(RingGeometry(190f, 20f), RingGeometry(166f, 20f)), actual = layout.rings)
    }

    @Test
    fun ringGaugeLayout_withTooManyRings_makesThemThinnerAndKeepsTheHole() {
        val layout =
            ringGaugeLayout(size = Size(400f, 200f), ringCount = 10, maxRingWidth = 50f, spacing = 2f, labelBand = 0f)

        val innermost = layout.rings.last()
        assertTrue(layout.rings.all { it.width < 50f })
        assertEquals(
            expected = 200f * RING_GAUGE_MIN_HOLE_FRACTION,
            actual = innermost.innerRadius,
            absoluteTolerance = 0.01f,
        )
    }

    @Test
    fun ringGaugeLayout_withSpacingWiderThanTheBand_dropsSpacing() {
        val layout =
            ringGaugeLayout(size = Size(400f, 200f), ringCount = 3, maxRingWidth = 20f, spacing = 500f, labelBand = 0f)

        assertEquals(expected = 0f, actual = layout.spacing)
        assertEquals(expected = 20f, actual = layout.rings.first().width)
    }

    @Test
    fun ringGaugeLayout_keepsLabelBandUnderTheBaselineAndCentersVertically() {
        val layout =
            ringGaugeLayout(size = Size(400f, 200f), ringCount = 1, maxRingWidth = 20f, spacing = 0f, labelBand = 20f)

        assertEquals(expected = 180f, actual = layout.outerRadius)
        assertEquals(expected = 180f, actual = layout.center.y)
    }

    @Test
    fun ringIndexAt_resolvesTheRingUnderThePoint() {
        val layout =
            ringGaugeLayout(size = Size(400f, 200f), ringCount = 2, maxRingWidth = 20f, spacing = 4f, labelBand = 0f)

        assertEquals(expected = 0, actual = ringIndexAt(point = Offset(200f, 10f), layout = layout))
        assertEquals(expected = 1, actual = ringIndexAt(point = Offset(200f, 34f), layout = layout))
        // A ring is hit anywhere along its arc, including the empty part of its track.
        assertEquals(expected = 0, actual = ringIndexAt(point = Offset(390f, 199f), layout = layout))
    }

    @Test
    fun ringIndexAt_outsideTheRings_isNoSelection() {
        val layout =
            ringGaugeLayout(size = Size(400f, 200f), ringCount = 2, maxRingWidth = 20f, spacing = 4f, labelBand = 0f)

        assertEquals(expected = NO_SELECTION, actual = ringIndexAt(point = Offset(200f, 150f), layout = layout))
        assertEquals(expected = NO_SELECTION, actual = ringIndexAt(point = Offset(0f, 0f), layout = layout))
        assertEquals(expected = NO_SELECTION, actual = ringIndexAt(point = Offset(10f, 205f), layout = layout))
    }
}
