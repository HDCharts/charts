package io.github.dautovicharis.charts.unit.helpers

import androidx.compose.ui.graphics.Color
import io.github.dautovicharis.charts.internal.common.palette.generateColorShades
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ColorShadesTest {
    @Test
    fun generateColorShades_withNonPositiveCount_returnsEmpty() {
        assertTrue(generateColorShades(Color.Red, 0).isEmpty())
        assertTrue(generateColorShades(Color.Red, -4).isEmpty())
    }

    @Test
    fun generateColorShades_withOneShade_returnsBaseColor() {
        val base = Color(0xFF4958A9)
        val shades = generateColorShades(base, 1)

        assertEquals(1, shades.size)
        assertEquals(base, shades.first())
    }

    @Test
    fun generateColorShades_withChromaticBase_generatesDistinctPalette() {
        val base = Color(0xFF4958A9)
        val shades = generateColorShades(base, 8)

        assertEquals(8, shades.size)
        assertEquals(base, shades.first())

        val minDistanceSquared = shades.minPairwiseRgbDistanceSquared()
        assertTrue(minDistanceSquared > 0.008f)
    }

    @Test
    fun generateColorShades_withManySeries_preservesReasonableSeparation() {
        val base = Color(0xFF4958A9)
        val shades = generateColorShades(base, 20)

        assertEquals(20, shades.size)
        assertEquals(base, shades.first())

        val minDistanceSquared = shades.minPairwiseRgbDistanceSquared()
        assertTrue(minDistanceSquared > 0.003f)
    }

    @Test
    fun generateColorShades_withNeutralBase_respectsProvidedLightnessBounds_forGeneratedShades() {
        val base = Color(0xFF7A7A7A)
        val minLightness = 0.40f
        val maxLightness = 0.55f

        val shades =
            generateColorShades(
                baseColor = base,
                numberOfShades = 10,
                minSeriesLightness = minLightness,
                maxSeriesLightness = maxLightness,
            )

        assertEquals(10, shades.size)
        assertEquals(base, shades.first())

        assertTrue(
            shades.drop(1).all { color ->
                val lightness = color.toLightness()
                lightness in minLightness..maxLightness
            },
        )
    }
}

private fun List<Color>.minPairwiseRgbDistanceSquared(): Float {
    var minDistance = Float.MAX_VALUE
    for (i in indices) {
        for (j in (i + 1) until size) {
            val a = this[i]
            val b = this[j]
            val distance =
                ((a.red - b.red) * (a.red - b.red)) +
                    ((a.green - b.green) * (a.green - b.green)) +
                    ((a.blue - b.blue) * (a.blue - b.blue))
            if (distance < minDistance) {
                minDistance = distance
            }
        }
    }
    return minDistance
}

private fun Color.toLightness(): Float = (maxOf(red, green, blue) + minOf(red, green, blue)) / 2f
