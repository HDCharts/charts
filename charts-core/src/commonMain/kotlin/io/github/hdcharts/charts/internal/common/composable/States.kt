package io.github.hdcharts.charts.internal.common.composable

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalInspectionMode

/**
 * Remembers a state that indicates whether the current mode is preview mode.
 *
 * @return A [MutableState] of [Boolean] indicating if the current mode is preview mode.
 */
@Composable
fun rememberShowState(isPreviewMode: Boolean = LocalInspectionMode.current): MutableState<Boolean> =
    remember { mutableStateOf(isPreviewMode) }

/**
 * Remembers an animation state that is initialized based on the current mode.
 *
 * @return An [Animatable] of [Float] with [AnimationVector1D] indicating the animation state.
 */
@Composable
fun rememberAnimationState(
    isPreviewMode: Boolean = LocalInspectionMode.current,
): Animatable<Float, AnimationVector1D> = remember { Animatable(if (isPreviewMode) 1.0f else 0f) }
