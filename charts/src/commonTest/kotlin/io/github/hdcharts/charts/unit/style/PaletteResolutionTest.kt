package io.github.hdcharts.charts.unit.style

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.github.hdcharts.charts.internal.common.palette.generateColorShades
import io.github.hdcharts.charts.style.BarBarsStyle
import io.github.hdcharts.charts.style.LineVisualStyle
import io.github.hdcharts.charts.style.PieChartSlicesStyle
import io.github.hdcharts.charts.style.RadarPolygonStyle
import io.github.hdcharts.charts.style.StackedAreaBoundaryStyle
import io.github.hdcharts.charts.style.StackedAreaFillStyle
import io.github.hdcharts.charts.style.StackedBarSegmentStyle
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PaletteResolutionTest {
    private val base = Color(0xFF4958A9)
    private val explicit = listOf(Color.Red, Color.Green, Color.Blue)

    @Test
    fun line_emptyColors_generatesShadesOfColor() {
        val line = lineStyle(colors = emptyList())

        assertEquals(generateColorShades(base, 3), line.resolveColors(3))
    }

    @Test
    fun line_explicitColors_areReturned() {
        assertEquals(explicit, lineStyle(colors = explicit).resolveColors(3))
    }

    @Test
    fun line_singleSeries_usesColorEvenWithExplicitColors() {
        assertEquals(listOf(base), lineStyle(colors = explicit).resolveColors(1))
    }

    @Test
    fun line_colorsExcludeAlpha() {
        val resolved = lineStyle(colors = emptyList()).resolveColors(3)

        assertTrue(resolved.all { it.alpha == 1f })
    }

    @Test
    fun nonPositiveCount_returnsEmpty() {
        assertTrue(lineStyle(colors = emptyList()).resolveColors(0).isEmpty())
        assertTrue(PieChartSlicesStyle(alpha = 0.5f, baseColor = base).resolveColors(-1).isEmpty())
        assertTrue(barsStyle(colors = emptyList()).resolveColors(0).isEmpty())
    }

    @Test
    fun pie_generatesShadesOfBaseColor() {
        val slices = PieChartSlicesStyle(alpha = 0.5f, baseColor = base)

        assertEquals(generateColorShades(base, 5), slices.resolveColors(5))
    }

    @Test
    fun bars_emptyColors_repeatColor() {
        assertEquals(List(4) { base }, barsStyle(colors = emptyList()).resolveColors(4))
    }

    @Test
    fun bars_explicitColors_areReturned() {
        assertEquals(explicit, barsStyle(colors = explicit).resolveColors(3))
    }

    @Test
    fun stackedArea_fillAndBoundary_followSeriesRules() {
        val fill = StackedAreaFillStyle(color = base, colors = emptyList(), alpha = 0.4f)
        val boundary =
            StackedAreaBoundaryStyle(visible = true, color = base, colors = explicit, width = 2.dp, bezier = false)

        assertEquals(generateColorShades(base, 3), fill.resolveColors(3))
        assertEquals(listOf(base), fill.resolveColors(1))
        assertEquals(explicit, boundary.resolveColors(3))
        assertEquals(listOf(base), boundary.resolveColors(1))
    }

    @Test
    fun stackedBar_singleSeries_usesExplicitColor() {
        val segments = StackedBarSegmentStyle(color = base, colors = listOf(Color.Red), alpha = 0.4f)

        assertEquals(listOf(Color.Red), segments.resolveColors(1))
        assertEquals(
            generateColorShades(base, 4),
            StackedBarSegmentStyle(color = base, colors = emptyList(), alpha = 0.4f).resolveColors(4),
        )
    }

    @Test
    fun radar_followsSeriesRules() {
        val polygon =
            RadarPolygonStyle(
                fillVisible = true,
                fillAlpha = 0.25f,
                lineColor = base,
                lineColors = emptyList(),
                lineWidth = 3f,
            )

        assertEquals(generateColorShades(base, 3), polygon.resolveLineColors(3))
        assertEquals(listOf(base), polygon.resolveLineColors(1))
    }

    private fun lineStyle(colors: List<Color>): LineVisualStyle =
        LineVisualStyle(color = base, alpha = 0.4f, colors = colors, strokeWidth = 5.dp, bezier = true)

    private fun barsStyle(colors: List<Color>): BarBarsStyle =
        BarBarsStyle(color = base, colors = colors, alpha = 0.4f, space = 10.dp, minBarWidth = 10.dp)
}
