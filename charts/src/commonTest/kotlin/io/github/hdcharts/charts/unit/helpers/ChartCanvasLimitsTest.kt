package io.github.hdcharts.charts.unit.helpers

import io.github.hdcharts.core.internal.layout.chartCanvasFits
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ChartCanvasLimitsTest {
    @Test
    fun chartCanvasFits_checksWidthAndHeightTogether() {
        assertTrue(chartCanvasFits(200_000f, 240f))
        assertTrue(chartCanvasFits(262_142f, 240f))
        assertFalse(chartCanvasFits(262_143f, 240f))
        assertFalse(chartCanvasFits(17 * 16_384f, 240f))
        assertFalse(chartCanvasFits(200_000f, 16_384f))
    }

    @Test
    fun chartCanvasFits_rejectsNonFiniteAndNegativeSizes() {
        assertFalse(chartCanvasFits(Float.POSITIVE_INFINITY, 240f))
        assertFalse(chartCanvasFits(Float.NaN, 240f))
        assertFalse(chartCanvasFits(240f, Float.NaN))
        assertFalse(chartCanvasFits(-1f, 240f))
    }
}
