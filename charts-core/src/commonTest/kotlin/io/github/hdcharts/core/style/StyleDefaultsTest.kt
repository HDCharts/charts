package io.github.hdcharts.core.style

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.hdcharts.core.internal.InternalChartsApi
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class, InternalChartsApi::class)
class StyleDefaultsTest {
    @Test
    fun defaults_matchCurrentValues() =
        runComposeUiTest {
            lateinit var scheme: ColorScheme
            lateinit var colors: List<Color>

            setContent {
                MaterialTheme {
                    val themeScheme = MaterialTheme.colorScheme
                    val defaultColors =
                        listOf(
                            StyleDefaults.seriesColor,
                            StyleDefaults.pointColor,
                            StyleDefaults.gridColor,
                            StyleDefaults.axisColor,
                            StyleDefaults.axisLabelColor,
                            StyleDefaults.selectionColor,
                            StyleDefaults.titleColor,
                            StyleDefaults.pieBorderColor,
                            StyleDefaults.gaugeTrackColor,
                        )
                    SideEffect {
                        scheme = themeScheme
                        colors = defaultColors
                    }
                }
            }

            runOnIdle {
                assertEquals(
                    expected =
                        listOf(
                            scheme.primary,
                            scheme.tertiary,
                            scheme.outlineVariant,
                            scheme.outline,
                            scheme.onSurfaceVariant,
                            scheme.onSurface,
                            scheme.onSurface,
                            scheme.surface,
                            scheme.surfaceVariant,
                        ),
                    actual = colors,
                )
                assertEquals(expected = 1.dp, actual = StyleDefaults.lineWidth)
                assertEquals(expected = 2.dp, actual = StyleDefaults.seriesLineWidth)
                assertEquals(expected = 4.dp, actual = StyleDefaults.pointSize)
                assertEquals(expected = false, actual = StyleDefaults.pointsVisible)
                assertEquals(expected = 5.dp, actual = StyleDefaults.selectedPointSize)
                assertEquals(expected = 10.dp, actual = StyleDefaults.barSpacing)
                assertEquals(expected = 10.dp, actual = StyleDefaults.minBarWidth)
                assertEquals(expected = 11.sp, actual = StyleDefaults.axisLabelSize)
                assertEquals(expected = 10.dp, actual = StyleDefaults.axisLabelPadding)
                assertEquals(expected = 1f, actual = StyleDefaults.seriesAlpha)
                assertEquals(expected = 0.7f, actual = StyleDefaults.unselectedAlpha)
                assertEquals(expected = 4, actual = StyleDefaults.gridSteps)
                assertEquals(expected = 15.dp, actual = StyleDefaults.containerPadding)
                assertEquals(expected = 20.sp, actual = StyleDefaults.titleSize)
                assertEquals(expected = FontWeight.ExtraBold, actual = StyleDefaults.titleWeight)
                assertEquals(expected = 0.dp, actual = StyleDefaults.histogramBarSpacing)
                assertEquals(expected = 0.0, actual = StyleDefaults.histogramRangeMin)
                assertEquals(expected = 3.dp, actual = StyleDefaults.lineSelectionMarkerSize)
                assertEquals(expected = 0f, actual = StyleDefaults.pieDonutHole)
                assertEquals(expected = 0.25f, actual = StyleDefaults.radarFillAlpha)
                assertEquals(expected = 0.35f, actual = StyleDefaults.radarUnfocusedSeriesAlpha)
                assertEquals(expected = true, actual = StyleDefaults.radarAxisLabelsVisible)
                assertEquals(expected = 6.dp, actual = StyleDefaults.radarLabelEdgePadding)
                assertEquals(expected = 0.0, actual = StyleDefaults.gaugeRangeMin)
                assertEquals(expected = 100.0, actual = StyleDefaults.gaugeRangeMax)
                assertEquals(expected = 24.dp, actual = StyleDefaults.ringGaugeWidth)
                assertEquals(expected = 4.dp, actual = StyleDefaults.ringGaugeSpacing)
            }
        }

    @Test
    fun selectedValueFormatter_roundsToTwoDecimals() {
        // Act
        val formatter = StyleDefaults.selectedValueFormatter

        // Assert
        assertEquals(expected = "41.7", actual = formatter.format(41.7))
        assertEquals(expected = "-41.7", actual = formatter.format(-41.7))
        assertEquals(expected = "1.5", actual = formatter.format(1.5))
        assertEquals(expected = "3.0", actual = formatter.format(3.0))
        assertEquals(expected = "3.0", actual = formatter.format(2.999))
        assertEquals(expected = "1.23", actual = formatter.format(1.234))
        assertEquals(expected = "-1.24", actual = formatter.format(-1.236))
        assertEquals(expected = "10.0", actual = formatter.format(9.999))
    }

    @Test
    fun selectedValueFormatter_normalizesNearZeroToZero() {
        // Act
        val formatter = StyleDefaults.selectedValueFormatter

        // Assert
        for (value in listOf(0.0, -0.0, 0.004, -0.004, Double.MIN_VALUE, -Double.MIN_VALUE)) {
            assertEquals(expected = "0.0", actual = formatter.format(value), message = "value=$value")
        }
        assertEquals(expected = "0.01", actual = formatter.format(0.01))
        assertEquals(expected = "0.01", actual = formatter.format(0.005))
        assertEquals(expected = "-0.01", actual = formatter.format(-0.005))
    }

    @Test
    fun selectedValueFormatter_roundsDecimalTiesAwayFromZero() {
        assertEquals(expected = "0.13", actual = StyleDefaults.selectedValueFormatter.format(0.125))
        assertEquals(expected = "-0.13", actual = StyleDefaults.selectedValueFormatter.format(-0.125))
        assertEquals(expected = "1.01", actual = StyleDefaults.selectedValueFormatter.format(1.005))
        assertEquals(expected = "-1.01", actual = StyleDefaults.selectedValueFormatter.format(-1.005))
    }

    @Test
    fun selectedValueFormatter_doesNotSaturateLargeValues() {
        assertEquals(expected = "21474836.48", actual = StyleDefaults.selectedValueFormatter.format(21_474_836.48))
        assertEquals(expected = "-21474836.48", actual = StyleDefaults.selectedValueFormatter.format(-21_474_836.48))
        assertEquals(
            expected = "100000000000000000000.0",
            actual = StyleDefaults.selectedValueFormatter.format(1.0e20),
        )
    }

    @Test
    fun axisValueFormatter_roundsToTwoDecimalsWithoutTrailingZero() {
        assertEquals(expected = "5", actual = StyleDefaults.axisValueFormatter.format(5.0))
        assertEquals(expected = "-10", actual = StyleDefaults.axisValueFormatter.format(-10.0))
        assertEquals(expected = "2.5", actual = StyleDefaults.axisValueFormatter.format(2.5))
        assertEquals(expected = "1.23", actual = StyleDefaults.axisValueFormatter.format(1.234))
        assertEquals(expected = "0", actual = StyleDefaults.axisValueFormatter.format(-0.001))
        assertEquals(expected = "NaN", actual = StyleDefaults.axisValueFormatter.format(Double.NaN))
        assertEquals(expected = "Infinity", actual = StyleDefaults.axisValueFormatter.format(Double.POSITIVE_INFINITY))
    }

    @Test
    fun axisValueFormatter_largeValues_printPlainDigits() {
        // Double.toString switches to scientific notation from 10^7 on the JVM.
        assertEquals(expected = "9000000", actual = StyleDefaults.axisValueFormatter.format(9_000_000.0))
        assertEquals(expected = "10000000", actual = StyleDefaults.axisValueFormatter.format(1e7))
        assertEquals(expected = "12500000", actual = StyleDefaults.axisValueFormatter.format(12_500_000.0))
        assertEquals(expected = "-22500000.5", actual = StyleDefaults.axisValueFormatter.format(-22_500_000.5))
    }
}
