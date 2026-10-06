package io.github.hdcharts.core.internal.drawing

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import io.github.hdcharts.core.internal.InternalChartsApi
import io.github.hdcharts.core.style.ChartGradient
import io.github.hdcharts.core.style.GradientPoint
import io.github.hdcharts.core.style.GradientSpan
import io.github.hdcharts.core.style.GradientStop

/**
 * Returns the brush that paints a shape with [gradient], reading [color] for its series stops.
 *
 * [shapeBounds] and [plotBounds] are in draw coordinates; [gradient]'s span picks one. With
 * [mirrored] set, a [GradientSpan.Shape] gradient is flipped vertically, as for a bar below the
 * baseline. A gradient over bounds with no area paints its first stop.
 */
@InternalChartsApi
fun gradientBrush(
    color: Color,
    gradient: ChartGradient,
    shapeBounds: Rect,
    plotBounds: Rect,
    mirrored: Boolean,
): Brush {
    val colorStops = gradient.stops.map { stop -> stop.offset to stop.resolve(seriesColor = color) }
    val bounds = if (gradient.span == GradientSpan.Shape) shapeBounds else plotBounds
    if (bounds.width <= 0f || bounds.height <= 0f) return SolidColor(colorStops.first().second)
    val flip = mirrored && gradient.span == GradientSpan.Shape
    return when (gradient) {
        is ChartGradient.Linear ->
            Brush.linearGradient(
                colorStops = colorStops.toTypedArray(),
                start = gradient.start.toOffset(bounds = bounds, flip = flip),
                end = gradient.end.toOffset(bounds = bounds, flip = flip),
                tileMode = gradient.tileMode,
            )
    }
}

/** Returns the stop's color, reading [seriesColor] for a [GradientStop.Series] stop. */
private fun GradientStop.resolve(seriesColor: Color): Color =
    when (this) {
        is GradientStop.Fixed -> color
        is GradientStop.Series -> seriesColor.copy(alpha = alpha)
    }

private fun GradientPoint.toOffset(
    bounds: Rect,
    flip: Boolean,
): Offset =
    Offset(
        x = bounds.left + x * bounds.width,
        y = bounds.top + (if (flip) 1f - y else y) * bounds.height,
    )
