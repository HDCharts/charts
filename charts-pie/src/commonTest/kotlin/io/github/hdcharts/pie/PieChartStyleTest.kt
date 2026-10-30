package io.github.hdcharts.pie

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import io.github.hdcharts.core.internal.DONUT_MAX_PERCENTAGE
import io.github.hdcharts.core.internal.DONUT_MIN_PERCENTAGE
import io.github.hdcharts.core.internal.MAX_SIZE_PX
import io.github.hdcharts.core.internal.palette.generateColorShades
import io.github.hdcharts.core.style.StyleDefaults
import kotlinx.collections.immutable.persistentListOf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PieChartStyleTest {
    private val base = Color(0xFF4958A9)
    private val density = Density(2f)

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

    @Test
    fun donut_clampsHolePercentageAndFallsBackForNaN() {
        assertEquals(
            expected = StyleDefaults.pieDonutHole,
            actual = PieChartDonutStyle(holePercentage = Float.NaN).clamp().holePercentage,
        )
        assertEquals(
            expected = DONUT_MIN_PERCENTAGE,
            actual = PieChartDonutStyle(holePercentage = -10f).clamp().holePercentage,
        )
        assertEquals(
            expected = DONUT_MAX_PERCENTAGE,
            actual = PieChartDonutStyle(holePercentage = 400f).clamp().holePercentage,
        )
    }

    @Test
    fun slices_clampsAlpha() {
        val slices = PieChartSlicesStyle(alpha = 2f, baseColor = Color.Red, colors = persistentListOf())

        assertEquals(expected = 1f, actual = slices.clamp().alpha)
    }

    @Test
    fun border_clampsWidth() {
        val clamped = PieChartBorderStyle(width = Dp.Infinity, color = Color.Black).clamp(density)

        assertEquals(expected = with(density) { MAX_SIZE_PX.toDp() }, actual = clamped.width)
    }
}
