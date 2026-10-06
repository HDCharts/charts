package io.github.hdcharts.core.style

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TileMode
import io.github.hdcharts.core.internal.InternalChartsApi
import io.github.hdcharts.core.internal.clampAlpha
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

/**
 * A gradient that paints a chart shape, such as a bar.
 *
 * Positions are fractions of the gradient's bounds, which [span] picks, so a gradient reads the same
 * on every bar size and screen. A chart clamps a gradient it cannot draw as given, as [stops]
 * describes. One with non-finite positions, or the same start and end, draws the shape without it.
 */
@Immutable
sealed interface ChartGradient {
    /**
     * The colors along the gradient, with offsets in `0..1`. Offsets outside that range are clamped,
     * stops out of order are sorted, and a stop with a non-finite offset is dropped. A single stop
     * paints its color solid, and no stops draw the shape without a gradient.
     */
    val stops: ImmutableList<GradientStop>

    /** The bounds the gradient's positions are fractions of. */
    val span: GradientSpan

    /**
     * A gradient along the line from [start] to [end].
     *
     * @property tileMode How the gradient continues beyond [start] and [end].
     */
    @Immutable
    data class Linear(
        override val stops: ImmutableList<GradientStop>,
        val start: GradientPoint,
        val end: GradientPoint,
        override val span: GradientSpan,
        val tileMode: TileMode,
    ) : ChartGradient {
        constructor(
            stops: List<GradientStop>,
            start: GradientPoint,
            end: GradientPoint,
            span: GradientSpan,
            tileMode: TileMode,
        ) : this(
            stops = stops.toImmutableList(),
            start = start,
            end = end,
            span = span,
            tileMode = tileMode,
        )
    }
}

/** A color at [offset] along a gradient. */
@Immutable
sealed interface GradientStop {
    /** Where the color sits, from `0` at the gradient's start to `1` at its end. */
    val offset: Float

    /** A fixed [color], drawn as given. */
    @Immutable
    data class Fixed(
        override val offset: Float,
        val color: Color,
    ) : GradientStop

    /**
     * The color the chart resolves for the shape, such as the bar's color, drawn at [alpha].
     * One gradient can then follow every color of a palette.
     *
     * @property alpha The alpha in `0..1`. Replaces the resolved color's alpha.
     */
    @Immutable
    data class Series(
        override val offset: Float,
        val alpha: Float,
    ) : GradientStop
}

/**
 * A position as fractions of a gradient's bounds: `(0, 0)` is the top-left corner and `(1, 1)` the
 * bottom-right. Values outside `0..1` sit outside the bounds.
 */
@Immutable
data class GradientPoint(
    val x: Float,
    val y: Float,
)

/** The bounds a [ChartGradient]'s positions are fractions of. */
enum class GradientSpan {
    /**
     * Each shape's own bounds, so every bar shows the whole gradient. A bar below the baseline
     * mirrors it vertically, so the gradient still runs from the bar's value edge to the baseline.
     */
    Shape,

    /** The whole plot, so every shape shows its part of one gradient. */
    Plot,
}

/** Ready-made [ChartGradient]s with evenly spaced colors. */
object ChartGradients {
    /**
     * Returns a top-to-bottom gradient through [colors]. With [GradientSpan.Shape], that is from each
     * bar's value edge to the baseline.
     */
    fun vertical(
        colors: List<Color>,
        span: GradientSpan = GradientSpan.Shape,
    ): ChartGradient.Linear = linear(angleDegrees = 90f, colors = colors, span = span)

    /** Returns a left-to-right gradient through [colors]. */
    fun horizontal(
        colors: List<Color>,
        span: GradientSpan = GradientSpan.Shape,
    ): ChartGradient.Linear = linear(angleDegrees = 0f, colors = colors, span = span)

    /**
     * Returns a gradient through [colors] at [angleDegrees], clockwise from left-to-right.
     *
     * The angle is measured in the bounds' fractions, so `45` runs from the top-left corner to the
     * bottom-right corner whatever the bounds' shape. At multiples of 45 degrees the gradient reaches
     * both corners it runs between; at other angles on non-square bounds, it holds its end colors
     * near those corners.
     */
    fun linear(
        angleDegrees: Float,
        colors: List<Color>,
        span: GradientSpan = GradientSpan.Shape,
    ): ChartGradient.Linear {
        val radians = angleDegrees * PI / 180.0
        val dx = cos(radians).toFloat()
        val dy = sin(radians).toFloat()
        val halfLength = (abs(dx) + abs(dy)) / 2f
        return ChartGradient.Linear(
            stops = evenStops(colors = colors),
            start = GradientPoint(x = 0.5f - dx * halfLength, y = 0.5f - dy * halfLength),
            end = GradientPoint(x = 0.5f + dx * halfLength, y = 0.5f + dy * halfLength),
            span = span,
            tileMode = TileMode.Clamp,
        )
    }

    /**
     * Returns a top-to-bottom fade from each shape's own color to that color at [endAlpha]. On
     * bars with [GradientSpan.Shape], that is from the value edge to the baseline.
     */
    fun fade(
        endAlpha: Float = 0f,
        span: GradientSpan = GradientSpan.Shape,
    ): ChartGradient.Linear =
        ChartGradient.Linear(
            stops =
                listOf(
                    GradientStop.Series(offset = 0f, alpha = 1f),
                    GradientStop.Series(offset = 1f, alpha = endAlpha),
                ),
            start = GradientPoint(x = 0.5f, y = 0f),
            end = GradientPoint(x = 0.5f, y = 1f),
            span = span,
            tileMode = TileMode.Clamp,
        )

    private fun evenStops(colors: List<Color>): List<GradientStop> =
        colors.mapIndexed { index, color ->
            val offset = if (colors.size > 1) index / (colors.size - 1).toFloat() else 0f
            GradientStop.Fixed(offset = offset, color = color)
        }
}

/**
 * Returns [this] made drawable, or null when it cannot be drawn, so the shape is solid.
 *
 * Drops stops with non-finite offsets, coerces the rest into `0..1`, sorts them, and clamps each
 * [GradientStop.Series] alpha. A single stop paints its color solid. No stops, non-finite positions,
 * or a start equal to the end return null.
 */
@InternalChartsApi
fun ChartGradient.clamp(): ChartGradient? {
    val clampedStops =
        stops
            .filter { stop -> stop.offset.isFinite() }
            .map { stop -> stop.at(offset = stop.offset.coerceIn(0f, 1f)) }
            .sortedBy { stop -> stop.offset }
    val drawableStops =
        when (clampedStops.size) {
            0 -> return null
            1 -> listOf(clampedStops.single().at(offset = 0f), clampedStops.single().at(offset = 1f))
            else -> clampedStops
        }
    return when (this) {
        is ChartGradient.Linear -> {
            val positions = listOf(start.x, start.y, end.x, end.y)
            val zeroLength = start.x == end.x && start.y == end.y
            if (positions.any { !it.isFinite() } || zeroLength) return null
            ChartGradient.Linear(stops = drawableStops, start = start, end = end, span = span, tileMode = tileMode)
        }
    }
}

/** Returns this stop at [offset], with a [GradientStop.Series] alpha clamped to `0..1`. */
private fun GradientStop.at(offset: Float): GradientStop =
    when (this) {
        is GradientStop.Fixed -> GradientStop.Fixed(offset = offset, color = color)
        is GradientStop.Series -> GradientStop.Series(offset = offset, alpha = alpha.clampAlpha())
    }
