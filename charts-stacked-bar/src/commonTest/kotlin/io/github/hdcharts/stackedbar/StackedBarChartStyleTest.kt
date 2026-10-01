package io.github.hdcharts.stackedbar

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
class StackedBarChartStyleTest {
    private val base = Color(0xFF4958A9)

    @Test
    fun stackedBar_singleSeries_usesExplicitColor() {
        val segments = StackedBarSegmentStyle(color = base, colors = listOf(Color.Red), alpha = 0.4f)

        assertEquals(listOf(Color.Red), segments.resolveColors(1))
        assertEquals(
            generateColorShades(base, 4),
            StackedBarSegmentStyle(color = base, colors = emptyList(), alpha = 0.4f).resolveColors(4),
        )
    }

    @Test
    fun selectionDefault_usesStyleDefaults() =
        runComposeUiTest {
            lateinit var selection: StackedBarSelectionStyle
            lateinit var segments: StackedBarSegmentStyle
            var selectionColor = Color.Unspecified

            setContent {
                MaterialTheme {
                    val selectionStyle = StackedBarChartDefaults.selection()
                    val segmentStyle = StackedBarChartDefaults.segments()
                    val themeSelectionColor = StyleDefaults.selectionColor
                    SideEffect {
                        selection = selectionStyle
                        segments = segmentStyle
                        selectionColor = themeSelectionColor
                    }
                }
            }

            runOnIdle {
                assertEquals(expected = selectionColor, actual = selection.color)
                assertEquals(expected = StyleDefaults.lineWidth, actual = selection.width)
                assertEquals(expected = StyleDefaults.unselectedAlpha, actual = selection.unselectedAlpha)
                assertNotEquals(illegal = segments.color, actual = selection.color)
            }
        }
}
