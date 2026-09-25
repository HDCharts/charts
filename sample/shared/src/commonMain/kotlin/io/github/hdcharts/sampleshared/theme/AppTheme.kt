package io.github.hdcharts.sampleshared.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf

val LocalHasDynamicColorFeature = compositionLocalOf { false }

@Composable
fun AppTheme(
    theme: Theme,
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    useDynamicColors: Boolean = false,
    content: @Composable () -> Unit,
) {
    val dynamicColors = dynamicColors()
    val baseScheme =
        when {
            useDynamicColors && dynamicColors != null -> dynamicColors
            darkTheme -> darkColorScheme()
            else -> lightColorScheme()
        }
    val uiColors = if (darkTheme) theme.dark else theme.light
    val colorScheme =
        if (useDynamicColors && dynamicColors != null) {
            baseScheme
        } else {
            baseScheme.copy(
                primary = uiColors.primary,
                onPrimary = uiColors.onPrimary,
                primaryContainer = uiColors.primaryContainer,
                secondary = uiColors.secondary,
                onSecondary = uiColors.onSecondary,
                tertiary = uiColors.tertiary,
                background = uiColors.background,
                onBackground = uiColors.onBackground,
                surface = uiColors.surface,
                onSurface = uiColors.onSurface,
                surfaceVariant = uiColors.surfaceVariant,
                error = uiColors.error,
                onError = uiColors.onError,
                onPrimaryContainer = uiColors.onPrimaryContainer ?: baseScheme.onPrimaryContainer,
                secondaryContainer = uiColors.secondaryContainer ?: baseScheme.secondaryContainer,
                onSecondaryContainer = uiColors.onSecondaryContainer ?: baseScheme.onSecondaryContainer,
                onSurfaceVariant = uiColors.onSurfaceVariant ?: baseScheme.onSurfaceVariant,
                errorContainer = uiColors.errorContainer ?: baseScheme.errorContainer,
                outline = uiColors.outline ?: baseScheme.outline,
                outlineVariant = uiColors.outlineVariant ?: baseScheme.outlineVariant,
                surfaceTint = uiColors.surfaceTint ?: baseScheme.surfaceTint,
                surfaceContainerLow = uiColors.surfaceContainerLow ?: baseScheme.surfaceContainerLow,
                surfaceContainer = uiColors.surfaceContainer ?: baseScheme.surfaceContainer,
                surfaceContainerHigh = uiColors.surfaceContainerHigh ?: baseScheme.surfaceContainerHigh,
                surfaceContainerHighest = uiColors.surfaceContainerHighest ?: baseScheme.surfaceContainerHighest,
            )
        }
    val chartColors = if (darkTheme) DarkChartColors else LightChartColors

    CompositionLocalProvider(
        LocalHasDynamicColorFeature provides (dynamicColors != null),
        LocalChartColors provides chartColors,
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content,
        )
    }
}

@Composable
expect fun dynamicColors(): ColorScheme?
