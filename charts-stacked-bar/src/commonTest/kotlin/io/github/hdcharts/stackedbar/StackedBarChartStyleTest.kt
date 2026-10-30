package io.github.hdcharts.stackedbar

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
import io.github.hdcharts.core.style.AxesStyle
import io.github.hdcharts.core.style.ChartGradients
import io.github.hdcharts.core.style.StyleDefaults
import kotlinx.collections.immutable.persistentListOf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class StackedBarChartStyleTest {
    private val base = Color(0xFF4958A9)
    private val density = Density(2f)

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
    fun stackedBar_segments_keepSharedGradient() {
        val gradient = ChartGradients.fade(endAlpha = 0.4f)
        val segments = StackedBarSegmentStyle(color = base, colors = emptyList(), alpha = 1f, gradient = gradient)

        assertEquals(gradient, segments.gradient)
    }

    @Test
    fun segments_clampsAlphaAndGradient() {
        val clamped =
            StackedBarSegmentStyle(
                color = Color.Blue,
                colors = persistentListOf(),
                alpha = -1f,
                gradient = ChartGradients.fade(endAlpha = 2f),
            ).clamp()

        assertEquals(expected = 0f, actual = clamped.alpha)
        assertEquals(expected = ChartGradients.fade(endAlpha = 1f), actual = clamped.gradient)
    }

    @Test
    fun layout_clampsSpaceAndMinBarWidth() {
        val clamped =
            StackedBarLayoutStyle(
                space = Dp.Infinity,
                minBarWidth = Float.NaN.dp,
            ).clamp(density)

        assertEquals(expected = with(density) { MAX_SIZE_PX.toDp() }, actual = clamped.space)
        assertEquals(expected = StyleDefaults.minBarWidth, actual = clamped.minBarWidth)
    }

    @Test
    fun selection_clampsWidthAndUnselectedAlpha() {
        val clamped =
            StackedBarSelectionStyle(
                visible = true,
                color = Color.Blue,
                width = (-6f).dp,
                unselectedAlpha = 3f,
            ).clamp(density)

        assertEquals(expected = 0.dp, actual = clamped.width)
        assertEquals(expected = 1f, actual = clamped.unselectedAlpha)
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

    @Test
    fun axisDefault_drawsLinesInAxisColor() =
        runComposeUiTest {
            lateinit var axis: AxesStyle
            var axisColor = Color.Unspecified

            setContent {
                MaterialTheme {
                    val styleAxis = StackedBarChartDefaults.style().axes
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
}
