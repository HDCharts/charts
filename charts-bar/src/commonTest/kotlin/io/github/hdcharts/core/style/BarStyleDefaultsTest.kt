package io.github.hdcharts.core.style

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import io.github.hdcharts.core.internal.MAX_GRID_STEPS
import io.github.hdcharts.core.internal.MAX_SIZE_PX
import io.github.hdcharts.core.internal.axis.validateAxisLabels
import io.github.hdcharts.core.internal.validateRange
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class BarStyleDefaultsTest {
    @Test
    fun barsConstructor_copiesMutablePalette() {
        val source = mutableListOf(Color.Red, Color.Blue)
        val bars =
            BarBarsStyle(
                color = Color.Black,
                colors = source,
                gradient = null,
                alpha = 1f,
                space = 10.dp,
                minBarWidth = 10.dp,
            )

        source[0] = Color.Green
        source.clear()

        val palette: ImmutableList<Color> = bars.colors
        assertEquals(expected = listOf(Color.Red, Color.Blue), actual = palette)
    }

    @Test
    fun barsCopy_acceptsImmutablePaletteWithoutChangingOriginal() {
        val original =
            BarBarsStyle(
                color = Color.Black,
                colors = persistentListOf(Color.Red),
                gradient = null,
                alpha = 0.5f,
                space = 10.dp,
                minBarWidth = 10.dp,
            )
        val replacement = persistentListOf(Color.Blue, Color.Green)

        val copied = original.copy(colors = replacement)

        assertEquals(expected = listOf(Color.Red), actual = original.colors)
        assertEquals(expected = replacement, actual = copied.colors)
        assertEquals(expected = original.color, actual = copied.color)
        assertEquals(expected = original.alpha, actual = copied.alpha)
        assertEquals(expected = original.space, actual = copied.space)
        assertEquals(expected = original.minBarWidth, actual = copied.minBarWidth)
    }

    @Test
    fun barsFactories_copyMutablePalettes() =
        runComposeUiTest {
            val source = mutableListOf(Color.Red, Color.Blue)
            lateinit var barBars: BarBarsStyle
            lateinit var histogramBars: BarBarsStyle

            setContent {
                MaterialTheme {
                    val bars = BarChartDefaults.bars(colors = source)
                    val bins = HistogramChartDefaults.bars(colors = source)
                    SideEffect {
                        barBars = bars
                        histogramBars = bins
                    }
                }
            }

            runOnIdle {
                source[0] = Color.Green
                source.clear()
                assertEquals(expected = listOf(Color.Red, Color.Blue), actual = barBars.colors)
                assertEquals(expected = listOf(Color.Red, Color.Blue), actual = histogramBars.colors)
                assertEquals(expected = 0.dp, actual = histogramBars.space)
                assertEquals(expected = 10.dp, actual = histogramBars.minBarWidth)
            }
        }

    @Test
    fun defaults_useStyleDefaultsAndShareBlocksWithHistogram() =
        runComposeUiTest {
            lateinit var barBars: BarBarsStyle
            lateinit var barStyle: BarChartStyle
            lateinit var histogramBars: BarBarsStyle
            lateinit var histogramStyle: HistogramChartStyle
            var selectionColor = Color.Unspecified

            setContent {
                MaterialTheme {
                    val bars = BarChartDefaults.bars()
                    val bar = BarChartDefaults.style()
                    val bins = HistogramChartDefaults.bars()
                    val style = HistogramChartDefaults.style()
                    val themeSelectionColor = StyleDefaults.selectionColor
                    SideEffect {
                        barBars = bars
                        barStyle = bar
                        histogramBars = bins
                        histogramStyle = style
                        selectionColor = themeSelectionColor
                    }
                }
            }

            runOnIdle {
                assertEquals(expected = StyleDefaults.barSpacing, actual = barBars.space)
                assertEquals(expected = StyleDefaults.minBarWidth, actual = barBars.minBarWidth)
                assertEquals(expected = StyleDefaults.histogramBarSpacing, actual = histogramBars.space)
                assertEquals(expected = StyleDefaults.minBarWidth, actual = histogramBars.minBarWidth)
                assertEquals(expected = StyleDefaults.seriesAlpha, actual = histogramBars.alpha)
                assertEquals(expected = histogramBars, actual = histogramStyle.bars)
                assertEquals(expected = StyleDefaults.histogramRangeMin, actual = histogramStyle.range.min)
                assertNull(histogramStyle.range.max)
                assertEquals(expected = barStyle.grid, actual = histogramStyle.grid)
                assertEquals(expected = barStyle.axis, actual = histogramStyle.axis)
                assertEquals(expected = barStyle.selection, actual = histogramStyle.selection)
                assertEquals(expected = selectionColor, actual = barStyle.selection.color)
                assertEquals(expected = StyleDefaults.unselectedAlpha, actual = barStyle.selection.unselectedAlpha)
                assertNotEquals(illegal = barBars.color, actual = barStyle.selection.color)
            }
        }

    @Test
    fun histogramColorCustomization_preservesAdjacentBins() =
        runComposeUiTest {
            lateinit var histogramStyle: HistogramChartStyle

            setContent {
                MaterialTheme {
                    val style =
                        HistogramChartDefaults.style(
                            bars = HistogramChartDefaults.bars(color = Color.Magenta),
                        )
                    SideEffect { histogramStyle = style }
                }
            }

            runOnIdle {
                assertEquals(expected = Color.Magenta, actual = histogramStyle.bars.color)
                assertEquals(expected = 0.dp, actual = histogramStyle.bars.space)
                assertEquals(expected = 10.dp, actual = histogramStyle.bars.minBarWidth)
            }
        }

    @Test
    fun range_preservesDoublePrecisionWithoutComposition() {
        val min = 16_777_216.25
        val max = 16_777_216.75
        val range = BarChartDefaults.range(min = min, max = max)

        assertEquals(expected = min, actual = range.min)
        assertEquals(expected = max, actual = range.max)
        assertEquals(expected = BarRangeStyle(min = min, max = max), actual = range)
        assertNull(BarChartDefaults.range().min)
        assertNull(BarChartDefaults.range().max)
    }

    @Test
    fun strokesAndRange_keepDpWidthsAndDoublePrecision() =
        runComposeUiTest {
            lateinit var defaultStyle: BarChartStyle
            lateinit var customStyle: BarChartStyle
            val min = 16_777_216.25
            val max = 16_777_216.75

            setContent {
                CompositionLocalProvider(LocalDensity provides Density(density = 2f, fontScale = 1.5f)) {
                    MaterialTheme {
                        val defaults = BarChartDefaults.style()
                        val custom =
                            BarChartDefaults.style(
                                range = BarChartDefaults.range(min = min, max = max),
                                grid = BarChartDefaults.grid(lineWidth = 2.dp),
                                axis = BarChartDefaults.axis(lineWidth = 2.dp),
                                selection = BarChartDefaults.selection(width = 2.dp),
                            )
                        SideEffect {
                            defaultStyle = defaults
                            customStyle = custom
                        }
                    }
                }
            }

            runOnIdle {
                assertEquals(expected = 1.dp, actual = defaultStyle.grid.lineWidth)
                assertEquals(expected = 1.dp, actual = defaultStyle.axis.lineWidth)
                assertEquals(expected = 1.dp, actual = defaultStyle.selection.width)
                assertEquals(expected = 2.dp, actual = customStyle.grid.lineWidth)
                assertEquals(expected = 2.dp, actual = customStyle.axis.lineWidth)
                assertEquals(expected = 2.dp, actual = customStyle.selection.width)
                assertEquals(expected = min, actual = customStyle.range.min)
                assertEquals(expected = max, actual = customStyle.range.max)
            }
        }

    @Test
    fun barsFactories_doNotClampExplicitAlpha() =
        runComposeUiTest {
            lateinit var barBars: BarBarsStyle
            lateinit var histogramBars: BarBarsStyle

            setContent {
                MaterialTheme {
                    val bars = BarChartDefaults.bars(alpha = 1.5f)
                    val bins = HistogramChartDefaults.bars(alpha = -0.5f)
                    SideEffect {
                        barBars = bars
                        histogramBars = bins
                    }
                }
            }

            runOnIdle {
                assertEquals(expected = 1.5f, actual = barBars.alpha)
                assertEquals(expected = -0.5f, actual = histogramBars.alpha)
            }
        }

    @Test
    fun formatters_preserveReadoutsAndTrimOnlyTerminalPointZeroFromTicks() {
        assertSame(expected = StyleDefaults.selectedValueFormatter, actual = BarChartDefaults.selectedValueFormatter)
        assertEquals(expected = "3.0", actual = BarChartDefaults.selectedValueFormatter.format(3.0))
        assertEquals(expected = "3", actual = BarChartDefaults.axisValueFormatter.format(3.0))
        assertEquals(expected = "0", actual = BarChartDefaults.axisValueFormatter.format(0.0))
        assertEquals(expected = "-3", actual = BarChartDefaults.axisValueFormatter.format(-3.0))
        assertEquals(expected = "10.01", actual = BarChartDefaults.axisValueFormatter.format(10.01))
        assertEquals(expected = "41.7", actual = BarChartDefaults.axisValueFormatter.format(41.7))
    }

    @Test
    fun invalidRangeAndAxisLabels_areRejected() =
        runComposeUiTest {
            var errors: List<List<String>> = emptyList()
            setContent {
                val styles =
                    listOf(
                        BarChartDefaults.style(range = BarChartDefaults.range(max = Double.POSITIVE_INFINITY)),
                        BarChartDefaults.style(
                            axis =
                                BarChartDefaults.axis(
                                    yLabels = BarChartDefaults.yLabels(size = TextUnit.Unspecified),
                                ),
                        ),
                        BarChartDefaults.style(
                            axis = BarChartDefaults.axis(xLabels = BarChartDefaults.xLabels(maxCount = 0)),
                        ),
                    )
                val validation =
                    styles.map {
                        validateRange(min = it.range.min, max = it.range.max) +
                            validateAxisLabels(it.axis.xLabels, it.axis.yLabels, Density(2f))
                    }
                SideEffect { errors = validation }
            }
            runOnIdle {
                assertEquals(3, errors.size)
                assertTrue(errors.all { it.isNotEmpty() })
            }
        }

    @Test
    fun invalidNumericStyleValues_areClampedInsteadOfRejected() =
        runComposeUiTest {
            lateinit var style: BarChartStyle
            setContent {
                val invalidStyle =
                    BarChartDefaults.style(
                        bars = BarChartDefaults.bars(space = Dp.Infinity, minBarWidth = (-1).dp, alpha = Float.NaN),
                        grid = BarChartDefaults.grid(lineWidth = Dp.Unspecified, steps = Int.MAX_VALUE),
                        selection = BarChartDefaults.selection(unselectedAlpha = 1.5f),
                    )
                SideEffect { style = invalidStyle }
            }
            runOnIdle {
                val density = Density(2f)
                val clamped = style.clamp(density)

                assertEquals(expected = with(density) { MAX_SIZE_PX.toDp() }, actual = clamped.bars.space)
                assertEquals(expected = 0.dp, actual = clamped.bars.minBarWidth)
                assertEquals(expected = 1f, actual = clamped.bars.alpha)
                assertEquals(expected = StyleDefaults.lineWidth, actual = clamped.grid.lineWidth)
                assertEquals(expected = MAX_GRID_STEPS, actual = clamped.grid.steps)
                assertEquals(expected = 1f, actual = clamped.selection.unselectedAlpha)
            }
        }
}
