package io.github.hdcharts.stackedarea

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.github.hdcharts.core.internal.palette.generateColorShades
import kotlin.test.Test
import kotlin.test.assertEquals

class StackedAreaChartStyleTest {
    private val base = Color(0xFF4958A9)
    private val explicit = listOf(Color.Red, Color.Green, Color.Blue)

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
}
