package io.github.hdcharts.pie

import androidx.compose.ui.graphics.Color
import io.github.hdcharts.core.internal.palette.generateColorShades
import kotlinx.collections.immutable.persistentListOf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PieChartStyleTest {
    private val base = Color(0xFF4958A9)

    @Test
    fun pie_generatesShadesOfBaseColor() {
        val slices = PieChartSlicesStyle(alpha = 0.5f, baseColor = base, colors = persistentListOf())

        assertEquals(generateColorShades(base, 5), slices.resolveColors(5))
    }

    @Test
    fun pie_nonPositiveCount_returnsEmpty() {
        val slices = PieChartSlicesStyle(alpha = 0.5f, baseColor = base, colors = persistentListOf())

        assertTrue(slices.resolveColors(-1).isEmpty())
    }

    @Test
    fun pie_withExplicitColors_usesThemInSliceOrder() {
        val palette = persistentListOf(Color.Red, Color.Green, Color.Blue)
        val slices = PieChartSlicesStyle(alpha = 0.5f, baseColor = base, colors = palette)

        assertEquals(palette, slices.resolveColors(3))
    }
}
