package io.github.hdcharts.core.internal.composable

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
