package io.github.hdcharts.charts.internal

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.TweenSpec

object AnimationSpec {
    private const val BAR_CASCADE_MAX_INDEX = 18
    private const val BAR_CASCADE_STEP_DELAY_MS = 22

    private fun duration(
        index: Int,
        duration: Int = ANIMATION_DURATION,
        offset: Int = ANIMATION_OFFSET,
    ): Int = duration + offset * index

    fun lineChart() =
        TweenSpec<Float>(
            durationMillis = ANIMATION_DURATION_LINE_CHART,
            delay = 0,
            easing = LinearEasing,
        )

    fun radarChart() =
        TweenSpec<Float>(
            durationMillis = ANIMATION_DURATION_LINE + 200,
            delay = 0,
            easing = FastOutSlowInEasing,
        )

    fun barChartSmooth() =
        TweenSpec<Float>(
            durationMillis = ANIMATION_DURATION_BAR,
            delay = 0,
            easing = FastOutSlowInEasing,
        )

    fun barChartCascaded(index: Int) =
        TweenSpec<Float>(
            durationMillis = ANIMATION_DURATION_BAR,
            delay = index.coerceIn(0, BAR_CASCADE_MAX_INDEX) * BAR_CASCADE_STEP_DELAY_MS,
            easing = FastOutSlowInEasing,
        )

    fun stackedBar(index: Int) =
        TweenSpec<Float>(
            durationMillis =
                duration(
                    index = index,
                    duration = ANIMATION_DURATION_BAR,
                ),
            delay = 0,
            easing = LinearEasing,
        )

    fun pieChart(index: Int) =
        TweenSpec<Float>(
            durationMillis = duration(index = index),
            delay = index * ANIMATION_OFFSET,
            easing = LinearEasing,
        )

    fun pieChartValue() =
        TweenSpec<Float>(
            durationMillis = ANIMATION_DURATION_LINE,
            delay = 0,
            easing = FastOutSlowInEasing,
        )

    fun pieChartDonut() =
        TweenSpec<Float>(
            durationMillis = 900,
            delay = 0,
        )
}
