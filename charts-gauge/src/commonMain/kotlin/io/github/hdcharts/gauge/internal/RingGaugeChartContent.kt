package io.github.hdcharts.gauge.internal

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import io.github.hdcharts.core.internal.NO_SELECTION
import io.github.hdcharts.core.style.StyleDefaults
import io.github.hdcharts.gauge.RingGaugeChartStyle
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

/** Test tag on the plot; the docs GIF scenario targets it by this string. */
internal const val RING_GAUGE_CHART_TAG = "RingGaugeChart"

/**
 * Draws the rings of a ring gauge over their tracks, with the range labels under the ends of the
 * arc. Reads the clamped style and prepared values; a tap reports the ring under it, or
 * [NO_SELECTION] outside every ring.
 */
@Composable
internal fun RingGaugeChartContent(
    values: ImmutableList<Double>,
    colors: ImmutableList<Color>,
    style: RingGaugeChartStyle,
    minLabel: String,
    maxLabel: String,
    interactionEnabled: Boolean,
    animateOnStart: Boolean,
    selectedIndex: Int,
    onRingTouched: (Int) -> Unit,
) {
    val isPreview = LocalInspectionMode.current
    val density = LocalDensity.current
    val targets =
        remember(values, style.range) {
            values.map { gaugeFraction(value = it, min = style.range.min, max = style.range.max) }
        }
    // Keyed per ring, so adding or removing a ring keeps the other rings' fill.
    val animatables =
        targets.indices.map { index ->
            key(index) {
                remember(isPreview, animateOnStart) {
                    Animatable(if (isPreview || !animateOnStart) targets[index] else 0f)
                }
            }
        }
    LaunchedEffect(targets, animatables) {
        coroutineScope {
            targets.forEachIndexed { index, target ->
                launch {
                    if (isPreview) {
                        animatables[index].snapTo(target)
                    } else {
                        animatables[index].animateTo(
                            targetValue = target,
                            animationSpec = AnimationSpec.ringGauge(),
                        )
                    }
                }
            }
        }
    }

    val textMeasurer = rememberTextMeasurer()
    val labelTextStyle = remember(style.labels) { TextStyle(color = style.labels.color, fontSize = style.labels.size) }
    val minLabelLayout =
        remember(minLabel, labelTextStyle, textMeasurer) {
            textMeasurer.measure(text = minLabel, style = labelTextStyle)
        }
    val maxLabelLayout =
        remember(maxLabel, labelTextStyle, textMeasurer) {
            textMeasurer.measure(text = maxLabel, style = labelTextStyle)
        }
    // The range labels mark the arc's scale, so they sit as far from it as axis labels do.
    val labelPaddingPx = with(density) { StyleDefaults.axisLabelPadding.toPx() }
    val labelBand =
        if (style.labels.visible) {
            maxOf(minLabelLayout.size.height, maxLabelLayout.size.height) + labelPaddingPx
        } else {
            0f
        }
    val maxRingWidthPx = with(density) { style.rings.width.toPx() }
    val spacingPx = with(density) { style.rings.spacing.toPx() }

    val currentOnRingTouched by rememberUpdatedState(onRingTouched)
    val interactionModifier =
        if (interactionEnabled) {
            Modifier.pointerInput(values.size, maxRingWidthPx, spacingPx, labelBand) {
                detectTapGestures { offset ->
                    val layout =
                        ringGaugeLayout(
                            size = Size(size.width.toFloat(), size.height.toFloat()),
                            ringCount = values.size,
                            maxRingWidth = maxRingWidthPx,
                            spacing = spacingPx,
                            labelBand = labelBand,
                        )
                    currentOnRingTouched(ringIndexAt(point = offset, layout = layout))
                }
            }
        } else {
            Modifier
        }

    Box(
        modifier =
            style.modifier
                .testTag(RING_GAUGE_CHART_TAG)
                .then(interactionModifier)
                .drawWithCache {
                    val layout =
                        ringGaugeLayout(
                            size = size,
                            ringCount = values.size,
                            maxRingWidth = maxRingWidthPx,
                            spacing = spacingPx,
                            labelBand = labelBand,
                        )
                    onDrawBehind {
                        layout.rings.forEachIndexed { index, ring ->
                            if (style.track.visible) {
                                drawRing(
                                    center = layout.center,
                                    ring = ring,
                                    color = style.track.color,
                                    sweepAngle = GAUGE_SWEEP_ANGLE,
                                )
                            }
                            val dimmed = selectedIndex != NO_SELECTION && selectedIndex != index
                            val alpha = if (dimmed) StyleDefaults.unselectedAlpha else 1f
                            val color = colors[index]
                            drawRing(
                                center = layout.center,
                                ring = ring,
                                color = color.copy(alpha = color.alpha * alpha),
                                sweepAngle = GAUGE_SWEEP_ANGLE * animatables[index].value,
                            )
                        }
                        val innerRadius = layout.rings.lastOrNull()?.innerRadius ?: layout.outerRadius
                        if (style.labels.visible && layout.outerRadius > 0f) {
                            val labelTop = layout.center.y + labelPaddingPx
                            val bandMiddle = (layout.outerRadius + innerRadius) / 2f
                            drawText(
                                textLayoutResult = minLabelLayout,
                                topLeft =
                                    Offset(
                                        x =
                                            labelLeft(
                                                centerX = layout.center.x - bandMiddle,
                                                labelWidth = minLabelLayout.size.width,
                                                canvasWidth = size.width,
                                            ),
                                        y = labelTop,
                                    ),
                            )
                            drawText(
                                textLayoutResult = maxLabelLayout,
                                topLeft =
                                    Offset(
                                        x =
                                            labelLeft(
                                                centerX = layout.center.x + bandMiddle,
                                                labelWidth = maxLabelLayout.size.width,
                                                canvasWidth = size.width,
                                            ),
                                        y = labelTop,
                                    ),
                            )
                        }
                    }
                },
    )
}

private fun DrawScope.drawRing(
    center: Offset,
    ring: RingGeometry,
    color: Color,
    sweepAngle: Float,
) {
    if (ring.width <= 0f || sweepAngle <= 0f) return
    drawArc(
        color = color,
        startAngle = GAUGE_START_ANGLE,
        sweepAngle = sweepAngle,
        useCenter = false,
        topLeft = Offset(center.x - ring.centerRadius, center.y - ring.centerRadius),
        size = Size(ring.centerRadius * 2f, ring.centerRadius * 2f),
        style = Stroke(width = ring.width, cap = StrokeCap.Butt),
    )
}

/** Centers a label of [labelWidth] on [centerX], kept inside a canvas [canvasWidth] wide. */
private fun labelLeft(
    centerX: Float,
    labelWidth: Int,
    canvasWidth: Float,
): Float = (centerX - labelWidth / 2f).coerceIn(0f, (canvasWidth - labelWidth).coerceAtLeast(0f))
