package io.github.dautovicharis.charts.internal.barchart

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import io.github.dautovicharis.charts.internal.NO_SELECTION
import io.github.dautovicharis.charts.style.BarChartStyle
import kotlin.math.abs

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

    if (style.gridVisible && style.gridSteps > 0) {
        val safeSteps = style.gridSteps.coerceAtLeast(1)
        repeat(safeSteps + 1) { step ->
            val progress = step / safeSteps.toFloat()
            val y = size.height * progress
            drawLine(
                color = style.gridColor,
                start = Offset(0f, y),
                end = Offset(size.width, y),
                strokeWidth = style.gridLineWidth,
            )
        }
    }

    if (style.axisVisible) {
        drawLine(
            color = style.axisColor,
            start = Offset(0f, 0f),
            end = Offset(0f, size.height),
            strokeWidth = style.axisLineWidth,
        )
        drawLine(
            color = style.axisColor,
            start = Offset(0f, clampedBaselineY),
            end = Offset(size.width, clampedBaselineY),
            strokeWidth = style.axisLineWidth,
        )
    }

    val unitWidth = unitWidth(barWidthPx, spacingPx)
    val firstVisible = visibleRange.first.coerceIn(0, animatedValues.lastIndex)
    val lastVisible = visibleRange.last.coerceIn(firstVisible, animatedValues.lastIndex)
    for (index in firstVisible..lastVisible) {
        val animatedValue = animatedValues[index]
        val value = animatedValue.value
        val barHeight = abs(value) * size.height
        val top = if (value >= 0f) clampedBaselineY - barHeight else clampedBaselineY
        val left = unitWidth * index
        val resolvedBarColor = barColors.getOrNull(index) ?: defaultBarColor

        drawRect(
            color = resolvedBarColor,
            topLeft = Offset(x = left, y = top),
            size =
                androidx.compose.ui.geometry.Size(
                    width = barWidthPx,
                    height = barHeight,
                ),
        )
    }

    if (style.selectionLineVisible && selectedIndex != NO_SELECTION && selectedCenterX.isFinite()) {
        drawLine(
            color = style.selectionLineColor,
            start = Offset(selectedCenterX, 0f),
            end = Offset(selectedCenterX, size.height),
            strokeWidth = style.selectionLineWidth,
        )
        drawCircle(
            color = style.selectionLineColor,
            radius = 3.dp.toPx(),
            center = Offset(selectedCenterX, clampedBaselineY),
            style = Stroke(width = style.selectionLineWidth),
        )
    }
}
