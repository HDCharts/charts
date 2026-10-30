package io.github.hdcharts.gauge.internal

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.testTag
import io.github.hdcharts.core.internal.ANIMATION_DURATION
import io.github.hdcharts.core.internal.DEFAULT_SCALE
import io.github.hdcharts.core.internal.MAX_SCALE
import io.github.hdcharts.core.internal.NO_SELECTION
import io.github.hdcharts.gauge.SegmentedRingGaugeChartStyle
import kotlinx.collections.immutable.ImmutableList
import kotlin.math.cos
import kotlin.math.sin

internal const val SEGMENTED_RING_GAUGE_CHART_TAG = "SegmentedRingGaugeChart"

@Composable
internal fun SegmentedRingGaugeChartContent(
    values: List<Double>,
    capacity: Double,
    colors: ImmutableList<Color>,
    style: SegmentedRingGaugeChartStyle,
    centerLabel: String,
    centerSupportingLabel: String?,
    interactionEnabled: Boolean,
    animateOnStart: Boolean,
    selectedIndex: Int,
    onSegmentTouched: (Int) -> Unit,
) {
    val isPreview = LocalInspectionMode.current
    val density = LocalDensity.current
    val widthPx = with(density) { style.segments.width.toPx() }
    val spacingPx = with(density) { style.segments.spacing.toPx() }
    val cornerRadiusPx = with(density) { style.segments.cornerRadius.toPx() }
    val animation = remember(isPreview, animateOnStart) { Animatable(if (isPreview || !animateOnStart) 1f else 0f) }
    val selectedSegmentScale by
        animateFloatAsState(
            targetValue = if (selectedIndex == NO_SELECTION) DEFAULT_SCALE else MAX_SCALE,
            animationSpec = tween(durationMillis = ANIMATION_DURATION),
            label = "segmentScaleAnimation",
        )
    LaunchedEffect(values, capacity, isPreview, animateOnStart) {
        if (isPreview || !animateOnStart) {
            animation.snapTo(1f)
        } else {
            animation.animateTo(1f, animationSpec = AnimationSpec.ringGauge())
        }
    }
    val currentOnSegmentTouched by rememberUpdatedState(onSegmentTouched)
    val interactionModifier =
        if (interactionEnabled) {
            Modifier.pointerInput(
                values,
                capacity,
                widthPx,
                spacingPx,
                style.segments.startPosition,
                style.segments.direction,
            ) {
                detectTapGestures { offset ->
                    val overflowInset = minOf(size.width, size.height).toFloat() * (MAX_SCALE - DEFAULT_SCALE) / 2f
                    val layout =
                        segmentedRingGaugeLayout(
                            size = Size(size.width.toFloat(), size.height.toFloat()),
                            width = widthPx,
                            overflowInset = overflowInset,
                        )
                    val geometries =
                        segmentGeometries(
                            values = values,
                            capacity = capacity,
                            gapAngle = gapAngleFor(gap = spacingPx, radius = layout.radius),
                            startAngle = style.segments.startPosition.angle,
                            direction = style.segments.direction,
                        )
                    currentOnSegmentTouched(segmentIndexAt(point = offset, layout = layout, geometries = geometries))
                }
            }
        } else {
            Modifier
        }

    Box(
        modifier =
            style.modifier
                .testTag(SEGMENTED_RING_GAUGE_CHART_TAG)
                .then(interactionModifier)
                .drawWithCache {
                    val overflowInset = size.minDimension * (MAX_SCALE - DEFAULT_SCALE) / 2f
                    val layout = segmentedRingGaugeLayout(size = size, width = widthPx, overflowInset = overflowInset)
                    val gapAngle = gapAngleFor(gap = spacingPx, radius = layout.radius)
                    val geometries =
                        segmentGeometries(
                            values = values,
                            capacity = capacity,
                            gapAngle = gapAngle,
                            startAngle = style.segments.startPosition.angle,
                            direction = style.segments.direction,
                        )
                    val remainingGeometry =
                        remainingSegmentGeometry(
                            values = values,
                            capacity = capacity,
                            gapAngle = gapAngle,
                            startAngle = style.segments.startPosition.angle,
                            direction = style.segments.direction,
                        )
                    onDrawBehind {
                        remainingGeometry?.let { geometry ->
                            drawRoundedSegmentedRing(
                                center = layout.center,
                                radius = layout.radius,
                                width = layout.width,
                                cornerRadius = cornerRadiusPx,
                                color = style.track.color,
                                startAngle = geometry.startAngle,
                                sweepAngle = geometry.sweepAngle,
                            )
                        }
                        geometries.forEachIndexed { index, geometry ->
                            val segmentScale = if (selectedIndex == index) selectedSegmentScale else DEFAULT_SCALE
                            val animatedGeometry =
                                animatedSegmentGeometry(
                                    geometry = geometry,
                                    progress = animation.value,
                                    direction = style.segments.direction,
                                )
                            scale(scale = segmentScale, pivot = layout.center) {
                                drawRoundedSegmentedRing(
                                    center = layout.center,
                                    radius = layout.radius,
                                    width = layout.width,
                                    cornerRadius = cornerRadiusPx,
                                    color = colors[index],
                                    startAngle = animatedGeometry.startAngle,
                                    sweepAngle = animatedGeometry.sweepAngle,
                                )
                            }
                        }
                    }
                },
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.wrapContentSize()) {
            Text(text = centerLabel, style = style.centerLabels.value)
            if (!centerSupportingLabel.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(style.centerLabels.spacing))
                Text(text = centerSupportingLabel, style = style.centerLabels.supporting)
            }
        }
    }
}

private fun DrawScope.drawSegmentedRing(
    center: Offset,
    radius: Float,
    width: Float,
    color: Color,
    startAngle: Float,
    sweepAngle: Float,
) {
    if (radius <= 0f || width <= 0f || sweepAngle <= 0f) return
    drawArc(
        color = color,
        startAngle = startAngle,
        sweepAngle = sweepAngle,
        useCenter = false,
        topLeft = Offset(center.x - radius, center.y - radius),
        size = Size(radius * 2f, radius * 2f),
        style = Stroke(width = width, cap = StrokeCap.Butt),
    )
}

private fun DrawScope.drawRoundedSegmentedRing(
    center: Offset,
    radius: Float,
    width: Float,
    cornerRadius: Float,
    color: Color,
    startAngle: Float,
    sweepAngle: Float,
) {
    val outerRadius = radius + width / 2f
    val innerRadius = radius - width / 2f
    if (innerRadius <= 0f || sweepAngle <= 0f) return
    if (sweepAngle >= 360f) {
        drawSegmentedRing(
            center = center,
            radius = radius,
            width = width,
            color = color,
            startAngle = startAngle,
            sweepAngle = 360f,
        )
        return
    }

    val sweepRadians = sweepAngle.toRadians()
    val actualCornerRadius =
        segmentedRingGaugeCornerRadius(
            cornerRadius = cornerRadius,
            width = width,
            innerRadius = innerRadius,
            sweepRadians = sweepRadians,
        )
    if (actualCornerRadius <= 0f) {
        drawSegmentedRing(
            center = center,
            radius = radius,
            width = width,
            color = color,
            startAngle = startAngle,
            sweepAngle = sweepAngle,
        )
        return
    }

    val outerCornerAngle = (actualCornerRadius / outerRadius).toDegrees()
    val innerCornerAngle = (actualCornerRadius / innerRadius).toDegrees()
    val endAngle = startAngle + sweepAngle
    val outerBounds = center.ringBounds(radius = outerRadius)
    val innerBounds = center.ringBounds(radius = innerRadius)
    val path =
        Path().apply {
            moveTo(
                x = center.polarOffset(radius = outerRadius, angle = startAngle + outerCornerAngle).x,
                y = center.polarOffset(radius = outerRadius, angle = startAngle + outerCornerAngle).y,
            )
            arcTo(
                rect = outerBounds,
                startAngleDegrees = startAngle + outerCornerAngle,
                sweepAngleDegrees = sweepAngle - outerCornerAngle * 2f,
                forceMoveTo = false,
            )
            quadraticTo(
                x1 = center.polarOffset(radius = outerRadius, angle = endAngle).x,
                y1 = center.polarOffset(radius = outerRadius, angle = endAngle).y,
                x2 = center.polarOffset(radius = outerRadius - actualCornerRadius, angle = endAngle).x,
                y2 = center.polarOffset(radius = outerRadius - actualCornerRadius, angle = endAngle).y,
            )
            lineTo(
                x = center.polarOffset(radius = innerRadius + actualCornerRadius, angle = endAngle).x,
                y = center.polarOffset(radius = innerRadius + actualCornerRadius, angle = endAngle).y,
            )
            quadraticTo(
                x1 = center.polarOffset(radius = innerRadius, angle = endAngle).x,
                y1 = center.polarOffset(radius = innerRadius, angle = endAngle).y,
                x2 = center.polarOffset(radius = innerRadius, angle = endAngle - innerCornerAngle).x,
                y2 = center.polarOffset(radius = innerRadius, angle = endAngle - innerCornerAngle).y,
            )
            arcTo(
                rect = innerBounds,
                startAngleDegrees = endAngle - innerCornerAngle,
                sweepAngleDegrees = -sweepAngle + innerCornerAngle * 2f,
                forceMoveTo = false,
            )
            quadraticTo(
                x1 = center.polarOffset(radius = innerRadius, angle = startAngle).x,
                y1 = center.polarOffset(radius = innerRadius, angle = startAngle).y,
                x2 = center.polarOffset(radius = innerRadius + actualCornerRadius, angle = startAngle).x,
                y2 = center.polarOffset(radius = innerRadius + actualCornerRadius, angle = startAngle).y,
            )
            lineTo(
                x = center.polarOffset(radius = outerRadius - actualCornerRadius, angle = startAngle).x,
                y = center.polarOffset(radius = outerRadius - actualCornerRadius, angle = startAngle).y,
            )
            quadraticTo(
                x1 = center.polarOffset(radius = outerRadius, angle = startAngle).x,
                y1 = center.polarOffset(radius = outerRadius, angle = startAngle).y,
                x2 = center.polarOffset(radius = outerRadius, angle = startAngle + outerCornerAngle).x,
                y2 = center.polarOffset(radius = outerRadius, angle = startAngle + outerCornerAngle).y,
            )
            close()
        }
    drawPath(path = path, color = color)
}

private fun Offset.ringBounds(radius: Float): Rect =
    Rect(
        left = x - radius,
        top = y - radius,
        right = x + radius,
        bottom = y + radius,
    )

private fun Offset.polarOffset(
    radius: Float,
    angle: Float,
): Offset {
    val radians = angle.toRadians()
    return Offset(x = x + radius * cos(radians), y = y + radius * sin(radians))
}
