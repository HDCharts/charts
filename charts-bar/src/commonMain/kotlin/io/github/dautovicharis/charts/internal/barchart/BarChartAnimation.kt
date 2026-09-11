package io.github.dautovicharis.charts.internal.barchart

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import io.github.dautovicharis.charts.internal.AnimationSpec
import io.github.dautovicharis.charts.internal.common.model.ChartData
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlin.math.abs

private const val VALUE_DELTA_EPSILON = 0.0001f
private const val CASCADE_MAX_POINTS = 200

@Composable
internal fun rememberBarChartAnimatedValues(
    chartData: ChartData,
    targetNormalized: List<Float>,
    isPreview: Boolean,
    animateOnStart: Boolean,
): List<Animatable<Float, AnimationVector1D>> {
    val dataSize = chartData.points.size
    val valueAnimationSpec = remember { AnimationSpec.barChartSmooth() }
    val initialValues =
        remember(chartData.points.size, isPreview, animateOnStart) {
            if (isPreview || !animateOnStart) targetNormalized else null
        }
    val animatedValues =
        remember(dataSize, isPreview, animateOnStart) {
            chartData.points.mapIndexed { index, _ ->
                Animatable(initialValues?.getOrNull(index) ?: 0f)
            }
        }
    val hasInitialized = remember { mutableStateOf(false) }

    LaunchedEffect(targetNormalized, animatedValues, isPreview, animateOnStart) {
        if (chartData.points.isEmpty()) return@LaunchedEffect
        val shouldAnimate = !isPreview && (animateOnStart || hasInitialized.value)
        val useCascadeAnimation = shouldAnimate && dataSize <= CASCADE_MAX_POINTS
        coroutineScope {
            animatedValues.forEachIndexed { index, animatable ->
                val target = targetNormalized.getOrNull(index) ?: 0f
                if (abs(animatable.value - target) <= VALUE_DELTA_EPSILON) return@forEachIndexed
                launch {
                    if (!shouldAnimate) {
                        animatable.snapTo(target)
                    } else {
                        animatable.animateTo(
                            targetValue = target,
                            animationSpec =
                                if (useCascadeAnimation) {
                                    AnimationSpec.barChartCascaded(index)
                                } else {
                                    valueAnimationSpec
                                },
                        )
                    }
                }
            }
        }
        hasInitialized.value = true
    }

    return animatedValues
}
