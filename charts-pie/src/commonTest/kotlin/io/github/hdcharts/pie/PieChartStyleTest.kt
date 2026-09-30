package io.github.hdcharts.pie

import androidx.compose.ui.graphics.Color
import io.github.hdcharts.core.internal.palette.generateColorShades
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PieChartStyleTest {
    private val base = Color(0xFF4958A9)

    @Test
    fun pie_generatesShadesOfBaseColor() {
        val slices = PieChartSlicesStyle(alpha = 0.5f, baseColor = base)

        assertEquals(generateColorShades(base, 5), slices.resolveColors(5))
    }

    @Test
    fun pie_nonPositiveCount_returnsEmpty() {
        assertTrue(PieChartSlicesStyle(alpha = 0.5f, baseColor = base).resolveColors(-1).isEmpty())
    }
}
