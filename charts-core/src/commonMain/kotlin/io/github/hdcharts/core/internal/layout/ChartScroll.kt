package io.github.hdcharts.core.internal.layout

import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import io.github.hdcharts.core.internal.InternalChartsApi
import kotlin.math.roundToInt

/**
 * Scroll offset at which a `horizontalScroll` viewport [viewportWidthPx] wide places [contentWidth] of
 * content: [scrollValue] clamped to the scroll range. `horizontalScroll` clamps its state only when it
 * measures, after composition has read the old value, so a chart whose content just shrank would lay
 * out labels and cull items for an offset the canvas is no longer placed at. Charts read the scroll
 * through this to draw each frame at the offset it is placed at.
 */
@InternalChartsApi
fun Density.placedHorizontalScrollPx(
    scrollValue: Int,
    contentWidth: Dp,
    viewportWidthPx: Float,
): Int = scrollValue.coerceIn(0, (contentWidth.roundToPx() - viewportWidthPx.roundToInt()).coerceAtLeast(0))
