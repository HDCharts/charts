package io.github.hdcharts.bar.internal

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.TweenSpec
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import io.github.hdcharts.core.internal.ANIMATION_TARGET
import io.github.hdcharts.core.internal.AnimationSpec
import io.github.hdcharts.core.internal.animation.ChartMorphState
import io.github.hdcharts.core.internal.animation.ChartPointProgress

private const val CASCADE_MAX_POINTS = 200

/**
 * Splits one morph progress into a delayed, eased share per bar, so up to [CASCADE_MAX_POINTS] bars
 * still cascade as with [AnimationSpec.barChartCascaded].
 */
internal class BarChartCascade(
    barCount: Int,
) : ChartPointProgress {
    private val barSpec: TweenSpec<Float> =
        if (barCount <= CASCADE_MAX_POINTS) AnimationSpec.barChartCascaded(0) else AnimationSpec.barChartSmooth()

    private val delaysMillis: IntArray =
        if (barCount <= CASCADE_MAX_POINTS) {
            IntArray(barCount) { index -> AnimationSpec.barChartCascaded(index).delay }
        } else {
            IntArray(0)
        }

    /** The spec of the morph progress: linear, and long enough for the last bar to finish. */
    val morphSpec =
        TweenSpec<Float>(
            durationMillis = barSpec.durationMillis + (delaysMillis.maxOrNull() ?: 0),
            easing = LinearEasing,
        )

    override fun progressAt(
        pointIndex: Int,
        progress: Float,
    ): Float {
        val elapsedMillis = progress * morphSpec.durationMillis - delaysMillis.getOrElse(pointIndex) { 0 }
        return barSpec.easing.transform((elapsedMillis / barSpec.durationMillis).coerceIn(0f, ANIMATION_TARGET))
    }
}

@Composable
internal fun rememberBarChartMorph(
    targetNormalized: List<Float>,
    isPreview: Boolean,
    animateOnStart: Boolean,
): ChartMorphState {
    val dataSize = targetNormalized.size
    val cascade = remember(dataSize) { BarChartCascade(dataSize) }
    val hasInitialized = remember { mutableStateOf(false) }
    // Only the first bars grow from zero; a new bar count, such as on expand, draws straight away.
    val morph =
        remember(dataSize) {
            val growFromZero = animateOnStart && !isPreview && !hasInitialized.value
            ChartMorphState(
                initialValues = listOf(if (growFromZero) List(dataSize) { 0f } else targetNormalized),
                pointProgress = cascade,
            )
        }

    LaunchedEffect(targetNormalized) {
        if (targetNormalized.isEmpty()) return@LaunchedEffect
        val targets = listOf(targetNormalized)
        hasInitialized.value = true
        if (isPreview) {
            morph.snapTo(targets)
        } else if (morph.to != targets || morph.progress.value != ANIMATION_TARGET) {
            morph.animateTo(targets = targets, animationSpec = cascade.morphSpec)
        }
    }

    return morph
}
