package io.github.hdcharts.gauge

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import io.github.hdcharts.core.internal.MAX_SIZE_PX
import io.github.hdcharts.core.internal.palette.generateColorShades
import io.github.hdcharts.core.style.StyleDefaults
import kotlinx.collections.immutable.persistentListOf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class RingGaugeChartStyleTest {
    private val base = Color(0xFF4958A9)
    private val explicit = persistentListOf(Color.Red, Color.Green, Color.Blue)
    private val density = Density(2f)

    @Test
    fun range_keepsAnAscendingRange() {
        assertEquals(
            expected = GaugeRangeStyle(min = -20.0, max = 40.0),
            actual = GaugeRangeStyle(min = -20.0, max = 40.0).clamp(),
        )
    }

    @Test
    fun range_fallsBackToTheDefaultRangeWhenEqualOrReversed() {
        val default = GaugeRangeStyle(min = StyleDefaults.gaugeRangeMin, max = StyleDefaults.gaugeRangeMax)

        assertEquals(expected = default, actual = GaugeRangeStyle(min = 50.0, max = 50.0).clamp())
        assertEquals(expected = default, actual = GaugeRangeStyle(min = 80.0, max = 20.0).clamp())
    }

    @Test
    fun rings_emptyColors_generatesShadesOfColor() {
        val rings =
            RingGaugeRingsStyle(
                alpha = 0.5f,
                baseColor = base,
                colors = persistentListOf(),
                width = 24.dp,
                spacing = 4.dp,
            )

        assertEquals(expected = generateColorShades(base, 3), actual = rings.resolveColors(3))
    }

    @Test
    fun rings_explicitColors_areReturned() {
        val rings =
            RingGaugeRingsStyle(alpha = 0.5f, baseColor = base, colors = explicit, width = 24.dp, spacing = 4.dp)

        assertEquals(expected = explicit, actual = rings.resolveColors(3))
    }

    @Test
    fun rings_nonPositiveCount_returnsEmpty() {
        val rings =
            RingGaugeRingsStyle(
                alpha = 0.5f,
                baseColor = base,
                colors = persistentListOf(),
                width = 24.dp,
                spacing = 4.dp,
            )

        assertTrue(rings.resolveColors(0).isEmpty())
    }

    @Test
    fun rings_clampsAlphaAndSizes() {
        val clamped =
            RingGaugeRingsStyle(
                alpha = 2f,
                baseColor = Color.Red,
                colors = persistentListOf(),
                width = Dp.Infinity,
                spacing = (-4).dp,
            ).clamp(density)

        assertEquals(expected = 1f, actual = clamped.alpha)
        assertEquals(expected = with(density) { MAX_SIZE_PX.toDp() }, actual = clamped.width)
        assertEquals(expected = 0.dp, actual = clamped.spacing)
    }

    @Test
    fun labels_fallsBackForAnUndrawableTextSize() {
        val clamped = GaugeLabelsStyle(visible = true, color = Color.Black, size = TextUnit.Unspecified).clamp(density)

        assertEquals(expected = StyleDefaults.axisLabelSize, actual = clamped.size)
    }

    @Test
    fun defaults_useStyleDefaults() =
        runComposeUiTest {
            lateinit var rings: RingGaugeRingsStyle
            lateinit var track: GaugeTrackStyle
            lateinit var labels: GaugeLabelsStyle
            var seriesColor = Color.Unspecified
            var trackColor = Color.Unspecified

            setContent {
                MaterialTheme {
                    val ringsStyle = RingGaugeChartDefaults.rings()
                    val trackStyle = RingGaugeChartDefaults.track()
                    val labelsStyle = RingGaugeChartDefaults.labels()
                    val themeSeriesColor = StyleDefaults.seriesColor
                    val themeTrackColor = StyleDefaults.gaugeTrackColor
                    SideEffect {
                        rings = ringsStyle
                        track = trackStyle
                        labels = labelsStyle
                        seriesColor = themeSeriesColor
                        trackColor = themeTrackColor
                    }
                }
            }

            runOnIdle {
                assertEquals(expected = seriesColor, actual = rings.baseColor)
                assertEquals(expected = StyleDefaults.seriesAlpha, actual = rings.alpha)
                assertEquals(expected = StyleDefaults.ringGaugeWidth, actual = rings.width)
                assertEquals(expected = StyleDefaults.ringGaugeSpacing, actual = rings.spacing)
                assertTrue(track.visible)
                assertEquals(expected = trackColor, actual = track.color)
                assertTrue(labels.visible)
                assertEquals(expected = StyleDefaults.axisLabelSize, actual = labels.size)
            }
        }
}
