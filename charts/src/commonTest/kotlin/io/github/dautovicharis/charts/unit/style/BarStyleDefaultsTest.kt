package io.github.dautovicharis.charts.unit.style

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
import io.github.dautovicharis.charts.internal.barchart.BarChartInternalStyle
import io.github.dautovicharis.charts.internal.barchart.toInternal
import io.github.dautovicharis.charts.internal.validateBarStyle
import io.github.dautovicharis.charts.model.ChartValueFormatters
import io.github.dautovicharis.charts.style.BarBarsStyle
import io.github.dautovicharis.charts.style.BarChartDefaults
import io.github.dautovicharis.charts.style.BarChartStyle
import io.github.dautovicharis.charts.style.BarRangeStyle
import io.github.dautovicharis.charts.style.HistogramChartDefaults
import io.github.dautovicharis.charts.style.HistogramChartStyle
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlin.test.Test
import kotlin.test.assertEquals
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
    fun histogramDefaults_useAdjacentBinsWithTenDpMinimumWidth() =
        runComposeUiTest {
            lateinit var barBars: BarBarsStyle
            lateinit var histogramBars: BarBarsStyle
            lateinit var histogramStyle: HistogramChartStyle

            setContent {
                MaterialTheme {
                    val bars = BarChartDefaults.bars()
                    val bins = HistogramChartDefaults.bars()
                    val style = HistogramChartDefaults.style()
                    SideEffect {
                        barBars = bars
                        histogramBars = bins
                        histogramStyle = style
                    }
                }
            }

            runOnIdle {
                assertEquals(expected = 10.dp, actual = barBars.space)
                assertEquals(expected = 10.dp, actual = barBars.minBarWidth)
                assertEquals(expected = 0.dp, actual = histogramBars.space)
                assertEquals(expected = 10.dp, actual = histogramBars.minBarWidth)
                assertEquals(expected = histogramBars, actual = histogramStyle.bars)
                assertEquals(expected = 0.0, actual = histogramStyle.range.min)
                assertNull(histogramStyle.range.max)
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
    fun strokes_preserveDefaultPixelsAndResolveExplicitDpAtControlledDensity() =
        runComposeUiTest {
            lateinit var defaultStyle: BarChartStyle
            lateinit var defaultInternalStyle: BarChartInternalStyle
            lateinit var customInternalStyle: BarChartInternalStyle
            val min = 16_777_216.25
            val max = 16_777_216.75

            setContent {
                CompositionLocalProvider(LocalDensity provides Density(density = 2f, fontScale = 1.5f)) {
                    MaterialTheme {
                        val defaults = BarChartDefaults.style()
                        val defaultInternal = defaults.toInternal()
                        val customInternal =
                            BarChartDefaults
                                .style(
                                    range = BarChartDefaults.range(min = min, max = max),
                                    grid = BarChartDefaults.grid(lineWidth = 2.dp),
                                    axis = BarChartDefaults.axis(lineWidth = 2.dp),
                                    selectionLine = BarChartDefaults.selectionLine(width = 2.dp),
                                ).toInternal()
                        SideEffect {
                            defaultStyle = defaults
                            defaultInternalStyle = defaultInternal
                            customInternalStyle = customInternal
                        }
                    }
                }
            }

            runOnIdle {
                assertEquals(expected = 0.5.dp, actual = defaultStyle.grid.lineWidth)
                assertEquals(expected = 0.5.dp, actual = defaultStyle.axis.lineWidth)
                assertEquals(expected = 0.5.dp, actual = defaultStyle.selectionLine.width)
                assertEquals(expected = 1f, actual = defaultInternalStyle.gridLineWidth)
                assertEquals(expected = 1f, actual = defaultInternalStyle.axisLineWidth)
                assertEquals(expected = 1f, actual = defaultInternalStyle.selectionLineWidth)
                assertEquals(expected = 4f, actual = customInternalStyle.gridLineWidth)
                assertEquals(expected = 4f, actual = customInternalStyle.axisLineWidth)
                assertEquals(expected = 4f, actual = customInternalStyle.selectionLineWidth)
                assertEquals(expected = min, actual = customInternalStyle.minValue)
                assertEquals(expected = max, actual = customInternalStyle.maxValue)
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
        assertSame(expected = ChartValueFormatters.Default, actual = BarChartDefaults.valueFormatter)
        assertEquals(expected = "3.0", actual = BarChartDefaults.valueFormatter.format(3.0))
        assertEquals(expected = "3", actual = BarChartDefaults.axisValueFormatter.format(3.0))
        assertEquals(expected = "0", actual = BarChartDefaults.axisValueFormatter.format(0.0))
        assertEquals(expected = "-3", actual = BarChartDefaults.axisValueFormatter.format(-3.0))
        assertEquals(expected = "10.01", actual = BarChartDefaults.axisValueFormatter.format(10.01))
        assertEquals(expected = "41.7", actual = BarChartDefaults.axisValueFormatter.format(41.7))
    }

    @Test
    fun invalidStyleValues_areRejectedBeforePixelConversionOrDrawing() =
        runComposeUiTest {
            var errors: List<List<String>> = emptyList()
            setContent {
                val styles =
                    listOf(
                        BarChartDefaults.style(bars = BarChartDefaults.bars(space = Dp.Infinity)),
                        BarChartDefaults.style(bars = BarChartDefaults.bars(space = Float.MAX_VALUE.dp)),
                        BarChartDefaults.style(bars = BarChartDefaults.bars(minBarWidth = (-1).dp)),
                        BarChartDefaults.style(bars = BarChartDefaults.bars(alpha = Float.NaN)),
                        BarChartDefaults.style(range = BarChartDefaults.range(max = Double.POSITIVE_INFINITY)),
                        BarChartDefaults.style(grid = BarChartDefaults.grid(lineWidth = Dp.Unspecified)),
                        BarChartDefaults.style(grid = BarChartDefaults.grid(steps = Int.MAX_VALUE)),
                        BarChartDefaults.style(
                            axis =
                                BarChartDefaults.axis(
                                    yLabels = BarChartDefaults.yLabels(size = TextUnit.Unspecified),
                                ),
                        ),
                        BarChartDefaults.style(
                            axis = BarChartDefaults.axis(xLabels = BarChartDefaults.xLabels(count = 0)),
                        ),
                    )
                val validation = styles.map { validateBarStyle(it, Density(2f)) }
                SideEffect { errors = validation }
            }
            runOnIdle {
                assertEquals(9, errors.size)
                assertTrue(errors.all { it.isNotEmpty() })
            }
        }
}
