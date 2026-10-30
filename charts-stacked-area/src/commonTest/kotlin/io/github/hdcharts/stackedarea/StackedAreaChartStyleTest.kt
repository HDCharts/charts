package io.github.hdcharts.stackedarea

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import io.github.hdcharts.core.internal.palette.generateColorShades
import io.github.hdcharts.core.style.AxesStyle
import io.github.hdcharts.core.style.ChartGradients
import io.github.hdcharts.core.style.StyleDefaults
import kotlinx.collections.immutable.persistentListOf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class StackedAreaChartStyleTest {
    private val base = Color(0xFF4958A9)
    private val explicit = listOf(Color.Red, Color.Green, Color.Blue)
    private val density = Density(2f)
    private val fill =
        StackedAreaFillStyle(
            color = Color.Blue,
            colors = persistentListOf(),
            alpha = 1f,
            bezier = false,
            gradient = null,
        )

    @Test
    fun stackedArea_fill_followsSeriesRules() {
        val fill = fillStyle()
        val explicitFill = fillStyle(colors = explicit)

        assertEquals(generateColorShades(base, 3), fill.resolveColors(3))
        assertEquals(listOf(base), fill.resolveColors(1))
        assertEquals(explicit, explicitFill.resolveColors(3))
        assertEquals(listOf(base), explicitFill.resolveColors(1))
    }

    @Test
    fun stackedArea_fillEquality_includesBezier() {
        val straight = fillStyle(colors = explicit)

        assertEquals(straight, fillStyle(colors = explicit))
        assertNotEquals(straight, straight.copy(bezier = true))
        assertNotEquals(straight.hashCode(), straight.copy(bezier = true).hashCode())
    }

    @Test
    fun fill_clampsAlphaAndSharedGradient() {
        val clamped =
            fill
                .copy(
                    alpha = Float.NaN,
                    bezier = true,
                    gradient = ChartGradients.fade(endAlpha = 2f),
                ).clamp()

        assertEquals(expected = 1f, actual = clamped.alpha)
        assertEquals(expected = ChartGradients.fade(endAlpha = 1f), actual = clamped.gradient)
    }

    @Test
    fun fill_invalidSharedGradientUsesSolidFallback() {
        val clamped = fill.copy(gradient = ChartGradients.vertical(colors = emptyList())).clamp()

        assertNull(clamped.gradient)
    }

    @Test
    fun selection_clampsWidthAndUnselectedAlpha() {
        val clamped =
            StackedAreaSelectionStyle(
                visible = true,
                color = Color.Blue,
                width = (-8f).dp,
                unselectedAlpha = 0.5f,
            ).clamp(density)

        assertEquals(expected = 0.dp, actual = clamped.width)
        assertEquals(expected = 0.5f, actual = clamped.unselectedAlpha)
    }

    @Test
    fun selectionDefault_usesStyleDefaults() =
        runComposeUiTest {
            lateinit var selection: StackedAreaSelectionStyle
            lateinit var fill: StackedAreaFillStyle
            var selectionColor = Color.Unspecified

            setContent {
                MaterialTheme {
                    val selectionStyle = StackedAreaChartDefaults.selection()
                    val fillStyle = StackedAreaChartDefaults.fill()
                    val themeSelectionColor = StyleDefaults.selectionColor
                    SideEffect {
                        selection = selectionStyle
                        fill = fillStyle
                        selectionColor = themeSelectionColor
                    }
                }
            }

            runOnIdle {
                assertEquals(expected = selectionColor, actual = selection.color)
                assertEquals(expected = StyleDefaults.lineWidth, actual = selection.width)
                assertEquals(expected = StyleDefaults.unselectedAlpha, actual = selection.unselectedAlpha)
                assertNotEquals(illegal = fill.color, actual = selection.color)
                assertNull(fill.gradient)
            }
        }

    @Test
    fun axisDefault_drawsLinesInAxisColor() =
        runComposeUiTest {
            lateinit var axis: AxesStyle
            var axisColor = Color.Unspecified

            setContent {
                MaterialTheme {
                    val styleAxis = StackedAreaChartDefaults.style().axes
                    val themeAxisColor = StyleDefaults.axisColor
                    SideEffect {
                        axis = styleAxis
                        axisColor = themeAxisColor
                    }
                }
            }

            runOnIdle {
                assertTrue(axis.visible)
                assertEquals(expected = axisColor, actual = axis.lines.color)
                assertEquals(expected = StyleDefaults.lineWidth, actual = axis.lines.lineWidth)
            }
        }

    private fun fillStyle(colors: List<Color> = emptyList()): StackedAreaFillStyle =
        StackedAreaFillStyle(
            color = base,
            colors = colors,
            alpha = 1f,
            bezier = false,
            gradient = null,
        )
}
