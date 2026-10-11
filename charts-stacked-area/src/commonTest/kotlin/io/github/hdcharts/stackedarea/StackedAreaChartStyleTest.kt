package io.github.hdcharts.stackedarea

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.v2.runComposeUiTest
import io.github.hdcharts.core.internal.palette.generateColorShades
import io.github.hdcharts.core.style.StyleDefaults
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNull

@OptIn(ExperimentalTestApi::class)
class StackedAreaChartStyleTest {
    private val base = Color(0xFF4958A9)
    private val explicit = listOf(Color.Red, Color.Green, Color.Blue)

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

    private fun fillStyle(colors: List<Color> = emptyList()): StackedAreaFillStyle =
        StackedAreaFillStyle(
            color = base,
            colors = colors,
            alpha = 1f,
            bezier = false,
            gradient = null,
        )
}
