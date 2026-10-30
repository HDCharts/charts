package io.github.hdcharts.gauge

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import io.github.hdcharts.core.internal.palette.generateColorShades
import io.github.hdcharts.core.style.StyleDefaults
import kotlinx.collections.immutable.persistentListOf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class SegmentedRingGaugeChartStyleTest {
    private val base = Color(0xFF4958A9)
    private val explicit = persistentListOf(Color.Red, Color.Green, Color.Blue)

    @Test
    fun segments_emptyColors_generatesShadesOfColor() {
        val segments =
            SegmentedRingGaugeSegmentsStyle(
                alpha = 0.5f,
                baseColor = base,
                colors = persistentListOf(),
                width = 20.dp,
                spacing = 4.dp,
                cornerRadius = 4.dp,
                startPosition = SegmentedRingGaugeStartPosition.Top,
                direction = SegmentedRingGaugeDirection.Clockwise,
            )

        assertEquals(expected = generateColorShades(base, 3), actual = segments.resolveColors(3))
    }

    @Test
    fun segments_explicitColors_areReturned() {
        val segments =
            SegmentedRingGaugeSegmentsStyle(
                alpha = 0.5f,
                baseColor = base,
                colors = explicit,
                width = 20.dp,
                spacing = 4.dp,
                cornerRadius = 4.dp,
                startPosition = SegmentedRingGaugeStartPosition.Top,
                direction = SegmentedRingGaugeDirection.Clockwise,
            )

        assertEquals(expected = explicit, actual = segments.resolveColors(3))
    }

    @Test
    fun segments_singleItem_usesBaseColor() {
        val segments =
            SegmentedRingGaugeSegmentsStyle(
                alpha = 0.5f,
                baseColor = base,
                colors = explicit,
                width = 20.dp,
                spacing = 4.dp,
                cornerRadius = 4.dp,
                startPosition = SegmentedRingGaugeStartPosition.Top,
                direction = SegmentedRingGaugeDirection.Clockwise,
            )

        assertEquals(expected = listOf(base), actual = segments.resolveColors(1))
    }

    @Test
    fun segments_nonPositiveCount_returnsEmpty() {
        val segments =
            SegmentedRingGaugeSegmentsStyle(
                alpha = 0.5f,
                baseColor = base,
                colors = persistentListOf(),
                width = 20.dp,
                spacing = 4.dp,
                cornerRadius = 4.dp,
                startPosition = SegmentedRingGaugeStartPosition.Top,
                direction = SegmentedRingGaugeDirection.Clockwise,
            )

        assertTrue(segments.resolveColors(0).isEmpty())
    }

    @Test
    fun segments_keepsAnExplicitZeroCornerRadius() {
        val segments =
            SegmentedRingGaugeSegmentsStyle(
                alpha = 1f,
                baseColor = base,
                colors = persistentListOf(),
                width = 20.dp,
                spacing = 4.dp,
                cornerRadius = 0.dp,
                startPosition = SegmentedRingGaugeStartPosition.Top,
                direction = SegmentedRingGaugeDirection.Clockwise,
            )

        assertEquals(expected = 0.dp, actual = segments.clamp(Density(2f)).cornerRadius)
    }

    @Test
    fun defaults_useStyleDefaults() =
        runComposeUiTest {
            lateinit var segments: SegmentedRingGaugeSegmentsStyle
            lateinit var track: SegmentedRingGaugeTrackStyle
            lateinit var labels: SegmentedRingGaugeCenterLabelsStyle
            var seriesColor = Color.Unspecified
            var trackColor = Color.Unspecified

            setContent {
                MaterialTheme {
                    val segmentsStyle = SegmentedRingGaugeChartDefaults.segments()
                    val trackStyle = SegmentedRingGaugeChartDefaults.track()
                    val labelsStyle = SegmentedRingGaugeChartDefaults.centerLabels()
                    val themeSeriesColor = StyleDefaults.seriesColor
                    val themeTrackColor = StyleDefaults.gaugeTrackColor
                    SideEffect {
                        segments = segmentsStyle
                        track = trackStyle
                        labels = labelsStyle
                        seriesColor = themeSeriesColor
                        trackColor = themeTrackColor
                    }
                }
            }

            runOnIdle {
                assertEquals(expected = seriesColor, actual = segments.baseColor)
                assertEquals(expected = StyleDefaults.seriesAlpha, actual = segments.alpha)
                assertEquals(expected = StyleDefaults.segmentedRingGaugeWidth, actual = segments.width)
                assertEquals(expected = StyleDefaults.segmentedRingGaugeSpacing, actual = segments.spacing)
                assertEquals(expected = StyleDefaults.segmentedRingGaugeCornerRadius, actual = segments.cornerRadius)
                assertEquals(expected = trackColor, actual = track.color)
                assertEquals(expected = StyleDefaults.segmentedRingGaugeCenterValueSize, actual = labels.value.fontSize)
                assertEquals(
                    expected = StyleDefaults.segmentedRingGaugeCenterSupportingSize,
                    actual = labels.supporting.fontSize,
                )
                assertEquals(expected = StyleDefaults.subtitleSpacing, actual = labels.spacing)
            }
        }
}
