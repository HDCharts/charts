package io.github.dautovicharis.charts.internal.piechart

import androidx.compose.ui.layout.LayoutModifier
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.MeasureResult
import androidx.compose.ui.layout.MeasureScope
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import kotlin.math.min

/**
 * Sizes the pie drawing area so it adapts to its constraints.
 *
 * - The width fills the available width, while the height is capped at
 *   `min(available width, available height)`. A non-square plot area therefore yields a
 *   round pie (the renderer inscribes a centered circle) instead of a stretched ellipse,
 *   and a wrap-content container keeps wrapping instead of growing to fill the screen.
 * - When a dimension is unbounded (e.g. inside a scrollable column), the missing
 *   dimension falls back to the other one, keeping the pie a square so it does not
 *   collapse.
 */
internal object AdaptivePieSizeModifier : LayoutModifier {
    override fun MeasureScope.measure(
        measurable: Measurable,
        constraints: Constraints,
    ): MeasureResult {
        val boundedWidth = constraints.hasBoundedWidth
        val boundedHeight = constraints.hasBoundedHeight
        val fallbackSide = with(density) { 200.dp.roundToPx() }
        val width =
            when {
                boundedWidth -> constraints.maxWidth
                boundedHeight -> constraints.maxHeight
                else -> fallbackSide
            }
        val height =
            if (boundedHeight) {
                min(width, constraints.maxHeight)
            } else {
                width
            }
        val placeable = measurable.measure(Constraints.fixed(width, height))
        return layout(width, height) {
            placeable.place(0, 0)
        }
    }
}
