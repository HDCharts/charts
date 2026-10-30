package io.github.hdcharts.line

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.hdcharts.core.internal.MAX_SIZE_PX
import io.github.hdcharts.core.internal.palette.generateColorShades
import io.github.hdcharts.core.style.ChartGradients
import io.github.hdcharts.core.style.StyleDefaults
import kotlinx.collections.immutable.persistentListOf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class LineChartStyleTest {
    private val base = Color(0xFF4958A9)
    private val explicit = listOf(Color.Red, Color.Green, Color.Blue)
    private val density = Density(2f)

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
    fun line_keepsSharedGradient() {
        val gradient = ChartGradients.fade(endAlpha = 0.4f)
        val line =
            LineVisualStyle(
                color = base,
                alpha = 1f,
                colors = emptyList(),
                strokeWidth = 5.dp,
                bezier = true,
                gradient = gradient,
            )

        assertEquals(gradient, line.gradient)
    }

    @Test
    fun line_nonPositiveCount_returnsEmpty() {
        assertTrue(lineStyle(colors = emptyList()).resolveColors(0).isEmpty())
    }

    @Test
    fun line_clampsAlphaStrokeWidthAndGradient() {
        val clamped =
            LineVisualStyle(
                color = Color.Blue,
                alpha = 1.4f,
                colors = persistentListOf(),
                strokeWidth = Dp.Infinity,
                bezier = true,
                gradient = ChartGradients.fade(endAlpha = 2f),
            ).clamp(density)

        assertEquals(expected = 1f, actual = clamped.alpha)
        assertEquals(expected = with(density) { MAX_SIZE_PX.toDp() }, actual = clamped.strokeWidth)
        assertEquals(expected = ChartGradients.fade(endAlpha = 1f), actual = clamped.gradient)
    }

    @Test
    fun points_clampsSize() {
        val clamped = LinePointStyle(color = Color.Blue, size = (-3f).dp, visible = true).clamp(density)

        assertEquals(expected = 0.dp, actual = clamped.size)
    }

    @Test
    fun selection_clampsWidthAndBothMarkerSizes() {
        val clamped =
            LineSelectionStyle(
                visible = true,
                color = Color.Blue,
                width = Float.NaN.dp,
                markerColor = Color.White,
                markerSize = Dp.Unspecified,
                pointSize = Dp.Infinity,
            ).clamp(density)

        assertEquals(expected = StyleDefaults.lineWidth, actual = clamped.width)
        assertEquals(expected = StyleDefaults.lineSelectionMarkerSize, actual = clamped.markerSize)
        assertEquals(expected = with(density) { MAX_SIZE_PX.toDp() }, actual = clamped.pointSize)
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
