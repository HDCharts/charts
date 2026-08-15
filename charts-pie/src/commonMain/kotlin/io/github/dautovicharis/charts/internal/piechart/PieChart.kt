package io.github.dautovicharis.charts.internal.piechart

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.testTag
import io.github.dautovicharis.charts.internal.ANIMATION_DURATION
import io.github.dautovicharis.charts.internal.AnimationSpec
import io.github.dautovicharis.charts.internal.DEFAULT_SCALE
import io.github.dautovicharis.charts.internal.MAX_SCALE
import io.github.dautovicharis.charts.internal.NO_SELECTION
import io.github.dautovicharis.charts.internal.TestTags
import io.github.dautovicharis.charts.internal.common.composable.rememberShowState
import io.github.dautovicharis.charts.internal.common.model.ChartData
import io.github.dautovicharis.charts.style.PieChartStyle
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

internal data class SliceGeometry(
    val startDeg: Float,
    val endDeg: Float,
    val sweepAngle: Float,
    val value: Double,
    val normalizedValue: Double,
)

@Composable
internal fun PieChart(
    chartData: ChartData,
    colors: ImmutableList<Color>,
    style: PieChartStyle,
    interactionEnabled: Boolean,
    animateOnStart: Boolean,
    selectedSliceIndex: Int = NO_SELECTION,
    onSliceTouched: (Int) -> Unit = {},
) {
    val isPreview = LocalInspectionMode.current
    var show by rememberShowState(isPreviewMode = isPreview || !animateOnStart)
    val values = chartData.points
    val animatables =
        remember(values.size, isPreview, animateOnStart) {
            List(values.size) { index ->
                val initialValue =
                    if (isPreview || !animateOnStart) {
                        values[index].toFloat()
                    } else {
                        0f
                    }
                Animatable(initialValue)
            }
        }
    val hasInitialized = remember { mutableStateOf(false) }
    LaunchedEffect(values, isPreview, animateOnStart) {
        if (values.isEmpty()) return@LaunchedEffect
        val shouldAnimate = !isPreview && (animateOnStart || hasInitialized.value)
        coroutineScope {
            values.forEachIndexed { index, value ->
                launch {
                    val target = value.toFloat()
                    if (!shouldAnimate) {
                        animatables[index].snapTo(target)
                    } else {
                        animatables[index].animateTo(
                            targetValue = target,
                            animationSpec = AnimationSpec.pieChartValue(),
                        )
                    }
                }
            }
        }
        hasInitialized.value = true
    }

    val interactionSlices = remember(values) { createPieSlices(values) }
    var selectedIndex by remember { mutableIntStateOf(NO_SELECTION) }
    LaunchedEffect(selectedSliceIndex) {
        selectedIndex = selectedSliceIndex
    }
    val effectiveSelectedIndex =
        when (selectedSliceIndex) {
            NO_SELECTION -> selectedIndex
            else -> selectedSliceIndex
        }

    val selectedSliceAnimation =
        animateFloatAsState(
            targetValue = if (effectiveSelectedIndex == NO_SELECTION) DEFAULT_SCALE else MAX_SCALE,
            animationSpec = tween(durationMillis = ANIMATION_DURATION),
            label = "sliceAnimation",
        )

    val slicesAnimations =
        List(values.size) { index ->
            animateFloatAsState(
                targetValue = if (show) DEFAULT_SCALE else 0f,
                animationSpec = AnimationSpec.pieChart(index),
                label = "scaleAnimation",
            )
        }

    val donutHoleAnimation by animateFloatAsState(
        targetValue = if (show) style.donut.holePercentage else 0f,
        animationSpec = AnimationSpec.pieChartDonut(),
        label = "donutHoleAnimation",
    )

    val interactionModifier =
        if (interactionEnabled) {
            Modifier
                .pointerInput(interactionSlices) {
                    detectTapGestures { offset ->
                        selectedIndex =
                            getSelectedIndex(
                                pointX = offset.x,
                                pointY = offset.y,
                                size = size,
                                slices = interactionSlices,
                            )
                        onSliceTouched(selectedIndex)
                    }
                }
        } else {
            Modifier
        }

    Box(
        modifier =
            style.modifier
                .testTag(TestTags.PIE_CHART)
                .onGloballyPositioned { show = true }
                .then(interactionModifier)
                .drawWithCache {
                    val overflowInset = size.minDimension * (MAX_SCALE - DEFAULT_SCALE) / 2f
                    val pieDiameter = size.minDimension - overflowInset * 2f
                    val pieBounds =
                        Rect(
                            left = (size.width - pieDiameter) / 2f,
                            top = (size.height - pieDiameter) / 2f,
                            right = (size.width + pieDiameter) / 2f,
                            bottom = (size.height + pieDiameter) / 2f,
                        )
                    val pieCenter = pieBounds.center
                    val pieRadius = pieBounds.width / 2f
                    val layerBounds = Rect(0f, 0f, size.width, size.height)
                    val borderStroke = Stroke(width = style.border.width)

                    onDrawBehind {
                        val animatedSlices = createPieSlices(animatables.map { it.value.toDouble() })
                        val shouldDrawDonutHole = donutHoleAnimation > 0f
                        if (shouldDrawDonutHole) {
                            drawContext.canvas.saveLayer(layerBounds, Paint())
                        }

                        animatedSlices.forEachIndexed { i, slice ->
                            val scale =
                                when (effectiveSelectedIndex) {
                                    NO_SELECTION -> slicesAnimations[i].value
                                    i -> selectedSliceAnimation.value
                                    else -> DEFAULT_SCALE
                                }

                            scale(scale) {
                                drawArc(
                                    color = colors[i],
                                    startAngle = slice.startDeg,
                                    sweepAngle = slice.sweepAngle,
                                    useCenter = true,
                                    style = Fill,
                                    topLeft = pieBounds.topLeft,
                                    size = pieBounds.size,
                                )
                                drawArc(
                                    color = style.border.color,
                                    startAngle = slice.startDeg,
                                    sweepAngle = slice.sweepAngle,
                                    useCenter = true,
                                    style = borderStroke,
                                    topLeft = pieBounds.topLeft,
                                    size = pieBounds.size,
                                )
                            }
                        }

                        if (shouldDrawDonutHole) {
                            val innerRadius = pieRadius * (donutHoleAnimation / 100f)
                            drawCircle(
                                color = Color.Transparent,
                                radius = innerRadius,
                                center = pieCenter,
                                blendMode = BlendMode.Clear,
                            )
                            drawCircle(
                                color = style.border.color,
                                radius = innerRadius,
                                center = pieCenter,
                                style = borderStroke,
                            )
                            drawContext.canvas.restore()
                        }
                    }
                },
    )
}
