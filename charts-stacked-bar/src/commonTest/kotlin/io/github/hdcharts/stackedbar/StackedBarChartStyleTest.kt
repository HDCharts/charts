package io.github.hdcharts.stackedbar

import androidx.compose.ui.graphics.Color
import io.github.hdcharts.core.internal.palette.generateColorShades
import kotlin.test.Test
import kotlin.test.assertEquals

class StackedBarChartStyleTest {
    private val base = Color(0xFF4958A9)

    @Test
    fun stackedBar_singleSeries_usesExplicitColor() {
        val segments = StackedBarSegmentStyle(color = base, colors = listOf(Color.Red), alpha = 0.4f)

        assertEquals(listOf(Color.Red), segments.resolveColors(1))
        assertEquals(
            generateColorShades(base, 4),
            StackedBarSegmentStyle(color = base, colors = emptyList(), alpha = 0.4f).resolveColors(4),
        )
    }
}
