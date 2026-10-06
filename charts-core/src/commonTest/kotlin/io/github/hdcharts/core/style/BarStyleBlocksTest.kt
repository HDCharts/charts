package io.github.hdcharts.core.style

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BarStyleBlocksTest {
    private val base = Color(0xFF4958A9)
    private val explicit = listOf(Color.Red, Color.Green, Color.Blue)

    @Test
    fun bars_emptyColors_repeatColor() {
        assertEquals(List(4) { base }, barsStyle(colors = emptyList()).resolveColors(4))
    }

    @Test
    fun bars_explicitColors_areReturned() {
        assertEquals(explicit, barsStyle(colors = explicit).resolveColors(3))
    }

    @Test
    fun bars_nonPositiveCount_returnsEmpty() {
        assertTrue(barsStyle(colors = emptyList()).resolveColors(0).isEmpty())
    }

    private fun barsStyle(colors: List<Color>): BarBarsStyle =
        BarBarsStyle(color = base, colors = colors, gradient = null, alpha = 0.4f, space = 10.dp, minBarWidth = 10.dp)
}
