package io.github.hdcharts.bar.internal

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import io.github.hdcharts.core.internal.drawing.drawSelectionLine
import io.github.hdcharts.core.style.BarChartStyle
import kotlin.math.abs

/**
 * Draws the bar chart bars with selection visualization.
 *
 * When [style.selection.visible] is true and [selectedIndex] falls within [visibleRange]:
 * - The selected bar draws at full opacity.
 * - Other visible bars draw at [style.selection.unselectedAlpha] opacity so the selection stands out.
 * - A vertical selection line draws at [selectedCenterX], skipping the selected bar's vertical span
 *   so the bar itself shows through.
 *
 * Selection has no visual effect when [selectedIndex] is outside [visibleRange] (e.g., scrolled out of view).
 */
internal fun DrawScope.drawBars(
    style: BarChartStyle,
    animatedValues: List<Animatable<Float, AnimationVector1D>>,
    visibleRange: IntRange,
    selectedIndex: Int,
    barColors: List<Color>,
    defaultBarColor: Color,
    maxValue: Double,
    minValue: Double,
    barWidthPx: Float,
    spacingPx: Float,
    selectedCenterX: Float,
) {
    if (animatedValues.isEmpty() || visibleRange.isEmpty()) return

    val clampedBaselineY =
        baselineYForRange(
            minValue = minValue,
            maxValue = maxValue,
            heightPx = size.height,
        )

    if (style.grid.visible && style.grid.steps > 0) {
        val safeSteps = style.grid.steps.coerceAtLeast(1)
        repeat(safeSteps + 1) { step ->
            val progress = step / safeSteps.toFloat()
            val y = size.height * progress
            drawLine(
                color = style.grid.color,
                start = Offset(0f, y),
                end = Offset(size.width, y),
                strokeWidth = style.grid.lineWidth.toPx(),
            )
        }
    }

    if (style.axis.visible) {
        drawLine(
            color = style.axis.color,
            start = Offset(0f, 0f),
            end = Offset(0f, size.height),
            strokeWidth = style.axis.lineWidth.toPx(),
        )
        drawLine(
            color = style.axis.color,
            start = Offset(0f, clampedBaselineY),
            end = Offset(size.width, clampedBaselineY),
            strokeWidth = style.axis.lineWidth.toPx(),
        )
    }

    val firstVisible = visibleRange.first.coerceIn(0, animatedValues.lastIndex)
    val lastVisible = visibleRange.last.coerceIn(firstVisible, animatedValues.lastIndex)
    val showSelection = style.selection.visible && selectedIndex in firstVisible..lastVisible
    var selectedMark: ClosedFloatingPointRange<Float>? = null
    for (index in firstVisible..lastVisible) {
        val animatedValue = animatedValues[index]
        val value = animatedValue.value
        val barHeight = abs(value) * size.height
        val top = if (value >= 0f) clampedBaselineY - barHeight else clampedBaselineY
        val left = barLeftPx(index = index, barWidthPx = barWidthPx, spacingPx = spacingPx)
        val right = barRightPx(index = index, barWidthPx = barWidthPx, spacingPx = spacingPx)
        val resolvedBarColor = barColors.getOrNull(index) ?: defaultBarColor
        val isSelected = showSelection && index == selectedIndex
        val barColor =
            if (showSelection && !isSelected) {
                resolvedBarColor.copy(alpha = resolvedBarColor.alpha * style.selection.unselectedAlpha)
            } else {
                resolvedBarColor
            }
        if (isSelected) selectedMark = top..(top + barHeight)

        drawRect(
            color = barColor,
            topLeft = Offset(x = left, y = top),
            size =
                androidx.compose.ui.geometry.Size(
                    width = right - left,
                    height = barHeight,
                ),
        )
    }

    if (showSelection && selectedCenterX.isFinite()) {
        // The selected bar shows the selection; the line stays over the background for contrast.
        drawSelectionLine(
            x = selectedCenterX,
            color = style.selection.color,
            strokeWidth = style.selection.width.toPx(),
            mark = selectedMark,
        )
    }
}
