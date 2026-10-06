package io.github.hdcharts.gauge.internal

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.TweenSpec
import io.github.hdcharts.core.internal.ANIMATION_DURATION_LINE

/** Gauge animations. They live here, not in `charts-core`, because no other chart uses them. */
internal object AnimationSpec {
    /** How long a ring takes to fill to its value, on start and when the data changes. */
    fun ringGauge() =
        TweenSpec<Float>(
            durationMillis = ANIMATION_DURATION_LINE,
            delay = 0,
            easing = FastOutSlowInEasing,
        )
}
