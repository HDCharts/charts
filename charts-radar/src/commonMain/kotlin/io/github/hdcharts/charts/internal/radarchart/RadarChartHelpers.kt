package io.github.hdcharts.charts.internal.radarchart

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.IntSize
import io.github.hdcharts.charts.internal.NO_SELECTION
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

internal fun seriesAnimationProgress(
    index: Int,
    total: Int,
    animationProgress: Float,
): Float {
    if (total <= 1) return animationProgress.coerceIn(0f, 1f)
    val staggerWindow = 0.45f
    val stagger = staggerWindow / (total - 1)
    val delay = index * stagger
    val available = (1f - delay).coerceAtLeast(0.01f)
    return ((animationProgress - delay) / available).coerceIn(0f, 1f)
}

internal fun axisIndexForOffset(
    offset: Offset,
    size: IntSize,
    axisCount: Int,
): Int {
    if (axisCount <= 0) return NO_SELECTION
    val center = Offset(size.width / 2f, size.height / 2f)
    val dx = offset.x - center.x
    val dy = offset.y - center.y
    if (dx == 0f && dy == 0f) return NO_SELECTION

    val angle = atan2(dy, dx)
    val startAngle = (-PI / 2f).toFloat()
    val twoPi = (2f * PI).toFloat()
    var normalized = angle - startAngle
    while (normalized < 0f) normalized += twoPi
    while (normalized >= twoPi) normalized -= twoPi
    val step = twoPi / axisCount
    return (normalized / step).roundToInt() % axisCount
}

internal fun buildAxisLabelPositions(
    axisCount: Int,
    center: Offset,
    radius: Float,
): List<Offset> {
    if (axisCount == 0) return emptyList()
    val startAngle = (-PI / 2f).toFloat()
    val angleStep = (2f * PI / axisCount).toFloat()
    return List(axisCount) { index ->
        val angle = startAngle + angleStep * index
        Offset(
            x = center.x + cos(angle) * radius,
            y = center.y + sin(angle) * radius,
        )
    }
}
