package io.github.hdcharts.line

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.unit.dp
import io.github.hdcharts.core.internal.palette.generateColorShades
import io.github.hdcharts.core.style.StyleDefaults
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class LineChartStyleTest {
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
    fun line_nonPositiveCount_returnsEmpty() {
        assertTrue(lineStyle(colors = emptyList()).resolveColors(0).isEmpty())
    }

    @Test
    fun defaults_useStyleDefaults() =
        runComposeUiTest {
            lateinit var selection: LineSelectionStyle
            lateinit var points: LinePointStyle
            var selectionColor = Color.Unspecified
            var pointColor = Color.Unspecified

            setContent {
                MaterialTheme {
                    val selectionStyle = LineChartDefaults.selection()
                    val pointStyle = LineChartDefaults.points()
                    val themeSelectionColor = StyleDefaults.selectionColor
                    val themePointColor = StyleDefaults.pointColor
                    SideEffect {
                        selection = selectionStyle
                        points = pointStyle
                        selectionColor = themeSelectionColor
                        pointColor = themePointColor
                    }
                }
            }

            runOnIdle {
                assertEquals(expected = selectionColor, actual = selection.color)
                assertEquals(expected = StyleDefaults.lineWidth, actual = selection.width)
                assertEquals(expected = pointColor, actual = selection.markerColor)
                assertEquals(expected = StyleDefaults.pointsVisible, actual = points.visible)
            }
        }

    private fun lineStyle(colors: List<Color>): LineVisualStyle =
        LineVisualStyle(color = base, alpha = 0.4f, colors = colors, strokeWidth = 5.dp, bezier = true)
}
