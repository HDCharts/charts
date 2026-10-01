package io.github.hdcharts.core.internal

import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp

/** Clamps a style alpha to `0..1`, drawing `NaN` as `1f`; styles keep what users pass, charts draw this. */
@InternalChartsApi
fun Float.clampAlpha(): Float = if (isNaN()) 1f else coerceIn(0f, 1f)

/** Clamps a style size to `0..`[MAX_SIZE_PX] pixels at [density], using [fallback] for `NaN`. */
@InternalChartsApi
fun Dp.clampSize(
    fallback: Dp,
    density: Density,
): Dp = if (value.isNaN()) fallback else coerceIn(0.dp, with(density) { MAX_SIZE_PX.toDp() })

/**
 * Clamps a style text size to a finite, positive `sp` value that resolves to at most
 * [MAX_SIZE_PX] pixels at [density], using [fallback] for anything that cannot be drawn.
 */
@InternalChartsApi
fun TextUnit.clampTextSize(
    fallback: TextUnit,
    density: Density,
): TextUnit {
    if (type != TextUnitType.Sp || !value.isFinite() || value <= 0f) return fallback
    val sizePx = with(density) { toPx() }
    if (!sizePx.isFinite() || sizePx > MAX_SIZE_PX) {
        return with(density) { MAX_SIZE_PX.toSp() }
    }
    return this
}

/** Clamps a grid step count to `0..`[MAX_GRID_STEPS]. */
@InternalChartsApi
fun Int.clampGridSteps(): Int = coerceIn(0, MAX_GRID_STEPS)
