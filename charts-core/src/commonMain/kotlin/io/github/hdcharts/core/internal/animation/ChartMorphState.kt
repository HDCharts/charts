package io.github.hdcharts.core.internal.animation

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.TweenSpec
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import io.github.hdcharts.core.internal.ANIMATION_TARGET
import io.github.hdcharts.core.internal.InternalChartsApi

/**
 * The morph between two data sets, driven by one animated scalar.
 *
 * [from] and [to] hold the normalized values the morph started from and is heading to, and the
 * drawn value of a point is the linear blend of the two at [progress]. One animated value keeps the
 * animation's cost independent of how many points the chart draws, and [blendInto] writes a frame
 * into a reused buffer so it allocates nothing per point.
 */
@InternalChartsApi
class ChartMorphState(
    initialValues: List<List<Float>>,
) {
    var from: List<List<Float>> by mutableStateOf(initialValues)
        private set

    var to: List<List<Float>> by mutableStateOf(initialValues)
        private set

    val progress = Animatable(ANIMATION_TARGET)

    /** Starts a morph to [targets] from the values that are drawn right now. */
    suspend fun animateTo(
        targets: List<List<Float>>,
        animationSpec: TweenSpec<Float>,
    ) {
        from = blendSeries(from = from, to = to, progress = progress.value)
        to = targets
        progress.snapTo(0f)
        progress.animateTo(targetValue = ANIMATION_TARGET, animationSpec = animationSpec)
    }

    /** Draws [targets] right away, with no animation. */
    fun snapTo(targets: List<List<Float>>) {
        from = targets
        to = targets
    }

    /** The normalized value drawn for one point, or 0 when the point is not drawn. */
    fun drawnValueAt(
        seriesIndex: Int,
        pointIndex: Int,
        progress: Float,
    ): Float {
        val target = to.getOrNull(seriesIndex)?.getOrNull(pointIndex) ?: return 0f
        val start = from.getOrNull(seriesIndex)?.getOrNull(pointIndex) ?: target
        return blendPoint(start = start, target = target, progress = progress)
    }
}

/** The one lerp rule every morph path uses, so the path and the selection marker cannot disagree. */
private fun blendPoint(
    start: Float,
    target: Float,
    progress: Float,
): Float {
    val safeProgress = progress.coerceIn(0f, ANIMATION_TARGET)
    return start + ((target - start) * safeProgress)
}

/** Freezes the values a morph is part-way through, so the next morph resumes from what is on screen. */
private fun blendSeries(
    from: List<List<Float>>,
    to: List<List<Float>>,
    progress: Float,
): List<List<Float>> =
    to.mapIndexed { seriesIndex, targets ->
        val start = from.getOrNull(seriesIndex).orEmpty()
        targets.mapIndexed { pointIndex, target ->
            blendPoint(
                start = start.getOrElse(pointIndex) { target },
                target = target,
                progress = progress,
            )
        }
    }

/**
 * Blends [from] towards [to] at [progress] into [into], scaled by [scaleBy], and reports how many
 * values it wrote. Points past the end of [into] are dropped.
 */
@InternalChartsApi
fun blendInto(
    into: FloatArray,
    from: List<Float>,
    to: List<Float>,
    progress: Float,
    scaleBy: Float = 1f,
): Int {
    val count = minOf(into.size, to.size)
    for (pointIndex in 0 until count) {
        val target = to[pointIndex]
        val start = if (pointIndex < from.size) from[pointIndex] else target
        into[pointIndex] = blendPoint(start = start, target = target, progress = progress) * scaleBy
    }
    return count
}
