package io.github.hdcharts.charts.style

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp

@Composable
fun defaultChartBackgroundColor(
    light: Float = 0.14f,
    dark: Float = 0.20f,
): Color {
    val colorScheme = MaterialTheme.colorScheme
    val fraction = if (isSystemInDarkTheme()) dark else light
    return lerp(colorScheme.surface, colorScheme.primaryContainer, fraction)
}
