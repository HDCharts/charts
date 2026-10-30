package io.github.hdcharts.gauge

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import io.github.hdcharts.core.internal.InternalChartsApi
import io.github.hdcharts.core.internal.clampAlpha
import io.github.hdcharts.core.internal.clampSize
import io.github.hdcharts.core.internal.layout.fillMaxSizeChartModifier
import io.github.hdcharts.core.internal.palette.resolvePaletteColors
import io.github.hdcharts.core.style.ChartContainerDefaults
import io.github.hdcharts.core.style.ChartContainerStyle
import io.github.hdcharts.core.style.LegendDefaults
import io.github.hdcharts.core.style.LegendStyle
import io.github.hdcharts.core.style.StyleDefaults
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

/** The presentation of a [SegmentedRingGaugeChart], grouped into cohesive sub-styles. */
@Stable
class SegmentedRingGaugeChartStyle(
    internal val modifier: Modifier,
    val chartContainerStyle: ChartContainerStyle,
    val segments: SegmentedRingGaugeSegmentsStyle,
    val track: SegmentedRingGaugeTrackStyle,
    val centerLabels: SegmentedRingGaugeCenterLabelsStyle,
    val legend: LegendStyle,
)

/** The direction in which segments expand from [SegmentedRingGaugeSegmentsStyle.startPosition]. */
enum class SegmentedRingGaugeDirection {
    Clockwise,
    CounterClockwise,
}

/** A named boundary before the first segment of a segmented ring gauge. */
enum class SegmentedRingGaugeStartPosition {
    Top,
    TopRight,
    Right,
    BottomRight,
    Bottom,
    BottomLeft,
    Left,
    TopLeft,
    ;

    internal val angle: Float
        get() =
            when (this) {
                Top -> -90f
                TopRight -> -45f
                Right -> 0f
                BottomRight -> 45f
                Bottom -> 90f
                BottomLeft -> 135f
                Left -> 180f
                TopLeft -> 225f
            }
}

/**
 * The colored portions of a segmented ring gauge.
 *
 * @property width The maximum thickness shared by the segments and their track. It shrinks when
 * needed to preserve the center hole.
 * @property spacing The physical space reserved between neighbouring segments.
 * @property cornerRadius The radius applied to the four corners of every annular segment.
 * @property startPosition The boundary before the first segment.
 * @property direction The direction in which segments are placed from [startPosition].
 */
@Immutable
data class SegmentedRingGaugeSegmentsStyle(
    val alpha: Float,
    val baseColor: Color,
    val colors: ImmutableList<Color>,
    val width: Dp,
    val spacing: Dp,
    val cornerRadius: Dp,
    val startPosition: SegmentedRingGaugeStartPosition,
    val direction: SegmentedRingGaugeDirection,
) {
    /** Resolves one color per input value before [alpha] is applied. */
    fun resolveColors(segmentCount: Int): ImmutableList<Color> =
        resolvePaletteColors(
            baseColor = baseColor,
            colors = colors,
            count = segmentCount,
            singleItemUsesBase = true,
        )

    internal fun clamp(density: Density) =
        SegmentedRingGaugeSegmentsStyle(
            alpha = alpha.clampAlpha(),
            baseColor = baseColor,
            colors = colors,
            width = width.clampSize(fallback = StyleDefaults.segmentedRingGaugeWidth, density = density),
            spacing = spacing.clampSize(fallback = StyleDefaults.segmentedRingGaugeSpacing, density = density),
            cornerRadius =
                cornerRadius.clampSize(fallback = StyleDefaults.segmentedRingGaugeCornerRadius, density = density),
            startPosition = startPosition,
            direction = direction,
        )
}

/** The unfilled capacity behind the colored segments. */
@Immutable
data class SegmentedRingGaugeTrackStyle(
    val color: Color,
)

/** Text styles for the value and optional supporting text inside the ring. */
@Immutable
data class SegmentedRingGaugeCenterLabelsStyle(
    val value: TextStyle,
    val supporting: TextStyle,
    val spacing: Dp,
) {
    internal fun clamp(density: Density) =
        SegmentedRingGaugeCenterLabelsStyle(
            value = value,
            supporting = supporting,
            spacing = spacing.clampSize(fallback = StyleDefaults.subtitleSpacing, density = density),
        )
}

/** Factories for [SegmentedRingGaugeChartStyle] and its grouped style blocks. */
object SegmentedRingGaugeChartDefaults {
    /** Formats the sum of all segments when [SegmentedRingGaugeChart.centerLabel] is omitted. */
    val centerValueFormatter = StyleDefaults.axisValueFormatter

    /** Formats a selected segment in the chart title. */
    val selectedValueFormatter = StyleDefaults.selectedValueFormatter

    @Composable
    @OptIn(InternalChartsApi::class)
    fun style(
        chartContainerStyle: ChartContainerStyle = ChartContainerDefaults.style(),
        segments: SegmentedRingGaugeSegmentsStyle = segments(),
        track: SegmentedRingGaugeTrackStyle = track(),
        centerLabels: SegmentedRingGaugeCenterLabelsStyle = centerLabels(),
        legend: LegendStyle = legend(),
    ): SegmentedRingGaugeChartStyle =
        SegmentedRingGaugeChartStyle(
            modifier = fillMaxSizeChartModifier(chartContainerStyle),
            chartContainerStyle = chartContainerStyle,
            segments = segments,
            track = track,
            centerLabels = centerLabels,
            legend = legend,
        )

    /**
     * Returns the colored-segment configuration for a capacity ring.
     *
     * @param width The maximum thickness shared by the segments and the remaining-capacity track.
     * It shrinks when needed to preserve the center hole.
     * @param spacing The physical space between neighbouring segments.
     * @param cornerRadius The corner radius of every annular segment.
     * @param startPosition The boundary before the first segment.
     * @param direction The direction in which the segments are placed from [startPosition].
     */
    @Composable
    fun segments(
        baseColor: Color = StyleDefaults.seriesColor,
        alpha: Float = StyleDefaults.seriesAlpha,
        colors: List<Color> = emptyList(),
        width: Dp = StyleDefaults.segmentedRingGaugeWidth,
        spacing: Dp = StyleDefaults.segmentedRingGaugeSpacing,
        cornerRadius: Dp = StyleDefaults.segmentedRingGaugeCornerRadius,
        startPosition: SegmentedRingGaugeStartPosition = SegmentedRingGaugeStartPosition.TopLeft,
        direction: SegmentedRingGaugeDirection = SegmentedRingGaugeDirection.CounterClockwise,
    ) = SegmentedRingGaugeSegmentsStyle(
        alpha = alpha,
        baseColor = baseColor,
        colors = colors.toImmutableList(),
        width = width,
        spacing = spacing,
        cornerRadius = cornerRadius,
        startPosition = startPosition,
        direction = direction,
    )

    @Composable
    fun track(color: Color = StyleDefaults.gaugeTrackColor) = SegmentedRingGaugeTrackStyle(color = color)

    @Composable
    fun centerLabels(
        value: TextStyle =
            TextStyle(
                color = StyleDefaults.titleColor,
                fontSize = StyleDefaults.segmentedRingGaugeCenterValueSize,
                fontWeight = StyleDefaults.titleWeight,
            ),
        supporting: TextStyle =
            TextStyle(
                color = StyleDefaults.subtitleColor,
                fontSize = StyleDefaults.segmentedRingGaugeCenterSupportingSize,
                fontWeight = FontWeight.Normal,
            ),
        spacing: Dp = StyleDefaults.subtitleSpacing,
    ) = SegmentedRingGaugeCenterLabelsStyle(value = value, supporting = supporting, spacing = spacing)

    @Composable
    fun legend(visible: Boolean = StyleDefaults.legendVisible): LegendStyle = LegendDefaults.style(visible = visible)
}

internal fun SegmentedRingGaugeChartStyle.clamp(density: Density) =
    SegmentedRingGaugeChartStyle(
        modifier = modifier,
        chartContainerStyle = chartContainerStyle,
        segments = segments.clamp(density),
        track = track,
        centerLabels = centerLabels.clamp(density),
        legend = legend,
    )
