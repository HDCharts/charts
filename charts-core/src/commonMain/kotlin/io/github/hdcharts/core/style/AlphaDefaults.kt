package io.github.hdcharts.core.style

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable

@Composable
fun defaultChartAlpha(
    light: Float = 0.7f,
    dark: Float = 0.6f,
): Float = if (isSystemInDarkTheme()) dark else light
