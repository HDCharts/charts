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

@OptIn(ExperimentalTestApi::class)
class StackedAreaChartStyleTest {
    private val base = Color(0xFF4958A9)
    private val explicit = listOf(Color.Red, Color.Green, Color.Blue)

    @Test
    fun stackedArea_fill_followsSeriesRules() {
        val fill = StackedAreaFillStyle(color = base, colors = emptyList(), alpha = 0.4f, bezier = false)
        val explicitFill = StackedAreaFillStyle(color = base, colors = explicit, alpha = 0.4f, bezier = false)

        assertEquals(generateColorShades(base, 3), fill.resolveColors(3))
        assertEquals(listOf(base), fill.resolveColors(1))
        assertEquals(explicit, explicitFill.resolveColors(3))
        assertEquals(listOf(base), explicitFill.resolveColors(1))
    }

    @Test
    fun stackedArea_fillEquality_includesBezier() {
        val straight = StackedAreaFillStyle(color = base, colors = explicit, alpha = 1f, bezier = false)

        assertEquals(straight, StackedAreaFillStyle(color = base, colors = explicit, alpha = 1f, bezier = false))
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
            }
        }
}
