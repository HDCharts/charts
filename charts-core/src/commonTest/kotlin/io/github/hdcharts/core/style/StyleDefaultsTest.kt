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
}
