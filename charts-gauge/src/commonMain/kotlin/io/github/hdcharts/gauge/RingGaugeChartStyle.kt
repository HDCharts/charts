package io.github.hdcharts.gauge

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import io.github.hdcharts.core.internal.InternalChartsApi
import io.github.hdcharts.core.internal.clampAlpha
import io.github.hdcharts.core.internal.clampSize
import io.github.hdcharts.core.internal.clampTextSize
import io.github.hdcharts.core.internal.layout.fillMaxSizeChartModifier
import io.github.hdcharts.core.internal.palette.resolvePaletteColors
import io.github.hdcharts.core.model.ChartValueFormatter
import io.github.hdcharts.core.style.ChartContainerDefaults
import io.github.hdcharts.core.style.ChartContainerStyle
import io.github.hdcharts.core.style.LegendDefaults
import io.github.hdcharts.core.style.LegendStyle
import io.github.hdcharts.core.style.StyleDefaults
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

/**
 * The style for a Ring Gauge Chart, grouped into cohesive sub-styles.
 *
 * @property chartContainerStyle The shared container/layout presentation.
 * @property range The values at the start and the end of the arc.
 * @property rings The ring configuration of the chart.
 * @property track The track drawn behind each ring.
 * @property labels The labels under the ends of the arc.
 * @property legend The legend configuration of the chart.
 */
@Stable
class RingGaugeChartStyle(
    internal val modifier: Modifier,
    val chartContainerStyle: ChartContainerStyle,
    val range: GaugeRangeStyle,
    val rings: RingGaugeRingsStyle,
    val track: GaugeTrackStyle,
    val labels: GaugeLabelsStyle,
    val legend: LegendStyle,
)

/**
 * The values a gauge maps onto its arc: [min] at the start and [max] at the end. Values outside the
 * range stop at the nearest end.
 *
 * @property min The value at the start of the arc. Must be finite.
 * @property max The value at the end of the arc. Must be finite. When it is not greater than [min],
 * the chart draws the default `0..100` range.
 */
@Immutable
data class GaugeRangeStyle(
    val min: Double,
    val max: Double,
) {
    /**
     * Returns this block with an ascending range. Names every field instead of using `copy`, so a
     * field added to the constructor fails to compile here until it is dealt with.
     */
    internal fun clamp() =
        if (max > min) {
            GaugeRangeStyle(min = min, max = max)
        } else {
            GaugeRangeStyle(min = StyleDefaults.gaugeRangeMin, max = StyleDefaults.gaugeRangeMax)
        }
}

/**
 * Ring configuration for a [RingGaugeChartStyle].
 *
 * @property alpha The alpha value applied to rendered rings.
 * @property baseColor The base color used to generate shades when [colors] is empty.
 * @property colors The colors the chart draws for rings, in value order. When empty, a shade is
 * generated for every ring from [baseColor]. When set, its count must match the value count.
 * @property width The widest a ring is drawn. Rings get thinner when this many would not fit.
 * @property spacing The gap between neighbouring rings.
 */
@Immutable
data class RingGaugeRingsStyle(
    val alpha: Float,
    val baseColor: Color,
    val colors: ImmutableList<Color>,
    val width: Dp,
    val spacing: Dp,
) {
    /**
     * Returns the default colors for [ringCount] rings, before `alpha` is applied:
     * [colors] when set, or generated shades of [baseColor] when it is empty.
     */
    fun resolveColors(ringCount: Int): ImmutableList<Color> =
        resolvePaletteColors(
            baseColor = baseColor,
            colors = colors,
            count = ringCount,
            singleItemUsesBase = false,
        )

    /**
     * Returns this block with [alpha] and sizes clamped. Names every field instead of using `copy`,
     * so a field added to the constructor fails to compile here until it is dealt with.
     */
    internal fun clamp(density: Density) =
        RingGaugeRingsStyle(
            alpha = alpha.clampAlpha(),
            baseColor = baseColor,
            colors = colors,
            width = width.clampSize(fallback = StyleDefaults.ringGaugeWidth, density = density),
            spacing = spacing.clampSize(fallback = StyleDefaults.ringGaugeSpacing, density = density),
        )
}

/**
 * Track configuration for a gauge: the full arc drawn behind each value.
 *
 * @property visible Whether the track is drawn.
 * @property color The track color.
 */
@Immutable
data class GaugeTrackStyle(
    val visible: Boolean,
    val color: Color,
)

/**
 * Configuration for the range labels under the start and the end of the arc.
 *
 * @property visible Whether the labels are drawn.
 * @property color The label color.
 * @property size The label text size.
 */
@Immutable
data class GaugeLabelsStyle(
    val visible: Boolean,
    val color: Color,
    val size: TextUnit,
) {
    /**
     * Returns this block with [size] clamped. Names every field instead of using `copy`, so a field
     * added to the constructor fails to compile here until it is dealt with.
     */
    internal fun clamp(density: Density) =
        GaugeLabelsStyle(
            visible = visible,
            color = color,
            size = size.clampTextSize(fallback = StyleDefaults.axisLabelSize, density = density),
        )
}

/**
 * An object that provides default styles for a Ring Gauge Chart.
 */
object RingGaugeChartDefaults {
    /** Default formatter for the selected value in the title. */
    val selectedValueFormatter: ChartValueFormatter = StyleDefaults.selectedValueFormatter

    /** Default formatter for the range labels, omitting only the terminal `.0` on whole values. */
    val axisValueFormatter: ChartValueFormatter = StyleDefaults.axisValueFormatter

    /**
     * Returns a [RingGaugeChartStyle] with the provided parameters or their default values.
     *
     * @param chartContainerStyle The style to be applied to the chart view. Defaults to the default style of ChartContainerDefaults.
     * @param range The range configuration. Defaults to `0..100`.
     * @param rings The ring configuration. Defaults to theme-derived ring colors.
     * @param track The track configuration. Defaults to a visible track in the surface variant color.
     * @param labels The range label configuration. Defaults to visible labels.
     * @param legend The legend configuration. Defaults to a visible legend.
     */
    @Composable
    @OptIn(InternalChartsApi::class)
    fun style(
        chartContainerStyle: ChartContainerStyle = ChartContainerDefaults.style(),
        range: GaugeRangeStyle = range(),
        rings: RingGaugeRingsStyle = rings(),
        track: GaugeTrackStyle = track(),
        labels: GaugeLabelsStyle = labels(),
        legend: LegendStyle = legend(),
    ): RingGaugeChartStyle {
        val modifier: Modifier = fillMaxSizeChartModifier(chartContainerStyle)
        return RingGaugeChartStyle(
            modifier = modifier,
            chartContainerStyle = chartContainerStyle,
            range = range,
            rings = rings,
            track = track,
            labels = labels,
            legend = legend,
        )
    }

    /**
     * Returns a [GaugeRangeStyle] with the provided parameters or their default values.
     *
     * @param min The value at the start of the arc. Defaults to 0.
     * @param max The value at the end of the arc. Defaults to 100.
     */
    @Composable
    fun range(
        min: Double = StyleDefaults.gaugeRangeMin,
        max: Double = StyleDefaults.gaugeRangeMax,
    ) = GaugeRangeStyle(min = min, max = max)

    /**
     * Returns a [RingGaugeRingsStyle] with the provided parameters or their default values.
     *
     * @param baseColor The base color used to generate shades when [colors] is empty. Defaults to
     * the primary color of the MaterialTheme.
     * @param alpha The alpha value applied to rendered rings. Defaults to 1f.
     * @param colors The colors to draw rings in, in value order. Empty by default, which draws a
     * generated shade per ring. When set, the count must match the value count.
     * @param width The widest a ring is drawn. Defaults to 24.dp.
     * @param spacing The gap between neighbouring rings. Defaults to 4.dp.
     */
    @Composable
    fun rings(
        baseColor: Color = StyleDefaults.seriesColor,
        alpha: Float = StyleDefaults.seriesAlpha,
        colors: List<Color> = emptyList(),
        width: Dp = StyleDefaults.ringGaugeWidth,
        spacing: Dp = StyleDefaults.ringGaugeSpacing,
    ) = RingGaugeRingsStyle(
        alpha = alpha,
        baseColor = baseColor,
        colors = colors.toImmutableList(),
        width = width,
        spacing = spacing,
    )

    /**
     * Returns a [GaugeTrackStyle] with the provided parameters or their default values.
     *
     * @param visible Whether the track is drawn. Defaults to true.
     * @param color The track color. Defaults to the surface variant color of the MaterialTheme.
     */
    @Composable
    fun track(
        visible: Boolean = true,
        color: Color = StyleDefaults.gaugeTrackColor,
    ) = GaugeTrackStyle(visible = visible, color = color)

    /**
     * Returns a [GaugeLabelsStyle] with the provided parameters or their default values.
     *
     * @param visible Whether the range labels are drawn. Defaults to true.
     * @param color The label color. Defaults to the axis label color.
     * @param size The label text size. Defaults to the axis label size.
     */
    @Composable
    fun labels(
        visible: Boolean = true,
        color: Color = StyleDefaults.axisLabelColor,
        size: TextUnit = StyleDefaults.axisLabelSize,
    ) = GaugeLabelsStyle(visible = visible, color = color, size = size)

    /**
     * Returns a [LegendStyle] with the provided parameters or their default values.
     *
     * @param visible Whether the legend is visible. Defaults to true.
     */
    @Composable
    fun legend(visible: Boolean = StyleDefaults.legendVisible): LegendStyle = LegendDefaults.style(visible = visible)
}

/** Returns [this] with the range, alpha, and sizes clamped to drawable values. */
internal fun RingGaugeChartStyle.clamp(density: Density) =
    RingGaugeChartStyle(
        modifier = modifier,
        chartContainerStyle = chartContainerStyle,
        range = range.clamp(),
        rings = rings.clamp(density),
        track = track,
        labels = labels.clamp(density),
        legend = legend,
    )
