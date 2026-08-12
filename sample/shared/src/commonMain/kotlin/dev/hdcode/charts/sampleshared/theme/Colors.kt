package dev.hdcode.charts.sampleshared.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

/**
 * Minimal UI color tokens used to build MaterialTheme color schemes.
 */
@Immutable
data class UiColors(
    val primary: Color,
    val onPrimary: Color,
    val primaryContainer: Color,
    val secondary: Color,
    val onSecondary: Color,
    val tertiary: Color,
    val background: Color,
    val onBackground: Color,
    val surface: Color,
    val onSurface: Color,
    val surfaceVariant: Color,
    val error: Color,
    val onError: Color,
)

private object UiNeutrals {
    // Light neutrals (bright, slightly violet-tinted)
    val lightBackground = Color(0xFFFCFCFD)
    val lightOnBackground = Color(0xFF121218)
    val lightSurface = Color(0xFFF8F8FB)
    val lightOnSurface = lightOnBackground
    val lightSurfaceVariant = Color(0xFFEDEDF2)

    // Dark neutrals (inky noir)
    val darkBackground = Color(0xFF09090F)
    val darkOnBackground = Color(0xFFF2F2FA)
    val darkSurface = Color(0xFF0D0D14)
    val darkOnSurface = darkOnBackground
    val darkSurfaceVariant = Color(0xFF191922)

    // Error colors (Material-ish, consistent across themes)
    val lightError = Color(0xFFBA1A1A)
    val lightOnError = Color(0xFFFFFFFF)
    val darkError = Color(0xFFFFB4AB)
    val darkOnError = Color(0xFF690005)
}

val deepOceanBlue =
    Theme(
        name = "Deep Ocean Blue",
        light =
            UiColors(
                primary = Color(0xFF006D9C),
                onPrimary = Color(0xFFFFFFFF),
                primaryContainer = Color(0xFFB9F7FF),
                secondary = Color(0xFF1F5BFF),
                onSecondary = Color(0xFFFFFFFF),
                tertiary = Color(0xFF9D00FF),
                background = UiNeutrals.lightBackground,
                onBackground = UiNeutrals.lightOnBackground,
                surface = UiNeutrals.lightSurface,
                onSurface = UiNeutrals.lightOnSurface,
                surfaceVariant = UiNeutrals.lightSurfaceVariant,
                error = UiNeutrals.lightError,
                onError = UiNeutrals.lightOnError,
            ),
        dark =
            UiColors(
                primary = Color(0xFF00E5FF),
                onPrimary = Color(0xFF001318),
                primaryContainer = Color(0xFF004A5A),
                secondary = Color(0xFFA7C0FF),
                onSecondary = Color(0xFF00153D),
                tertiary = Color(0xFFE0A3FF),
                background = UiNeutrals.darkBackground,
                onBackground = UiNeutrals.darkOnBackground,
                surface = UiNeutrals.darkSurface,
                onSurface = UiNeutrals.darkOnSurface,
                surfaceVariant = UiNeutrals.darkSurfaceVariant,
                error = UiNeutrals.darkError,
                onError = UiNeutrals.darkOnError,
            ),
    )

val blueViolet =
    Theme(
        name = "Blue Violet",
        light =
            UiColors(
                primary = Color(0xFF6C2CFF),
                onPrimary = Color(0xFFFFFFFF),
                primaryContainer = Color(0xFFE3D9FF),
                secondary = Color(0xFF0077B6),
                onSecondary = Color(0xFFFFFFFF),
                tertiary = Color(0xFFB0006F),
                background = UiNeutrals.lightBackground,
                onBackground = UiNeutrals.lightOnBackground,
                surface = UiNeutrals.lightSurface,
                onSurface = UiNeutrals.lightOnSurface,
                surfaceVariant = UiNeutrals.lightSurfaceVariant,
                error = UiNeutrals.lightError,
                onError = UiNeutrals.lightOnError,
            ),
        dark =
            UiColors(
                primary = Color(0xFFBBA4FF),
                onPrimary = Color(0xFF21005A),
                primaryContainer = Color(0xFF3D1A86),
                secondary = Color(0xFF00E5FF),
                onSecondary = Color(0xFF001318),
                tertiary = Color(0xFFFF4DB1),
                background = UiNeutrals.darkBackground,
                onBackground = UiNeutrals.darkOnBackground,
                surface = UiNeutrals.darkSurface,
                onSurface = UiNeutrals.darkOnSurface,
                surfaceVariant = UiNeutrals.darkSurfaceVariant,
                error = UiNeutrals.darkError,
                onError = UiNeutrals.darkOnError,
            ),
    )

val deepRed =
    Theme(
        name = "Deep Red",
        light =
            UiColors(
                primary = Color(0xFFC0006F),
                onPrimary = Color(0xFFFFFFFF),
                primaryContainer = Color(0xFFFFD6EA),
                secondary = Color(0xFF006A8E),
                onSecondary = Color(0xFFFFFFFF),
                tertiary = Color(0xFF3F7A00),
                background = UiNeutrals.lightBackground,
                onBackground = UiNeutrals.lightOnBackground,
                surface = UiNeutrals.lightSurface,
                onSurface = UiNeutrals.lightOnSurface,
                surfaceVariant = UiNeutrals.lightSurfaceVariant,
                error = UiNeutrals.lightError,
                onError = UiNeutrals.lightOnError,
            ),
        dark =
            UiColors(
                primary = Color(0xFFFF4DB1),
                onPrimary = Color(0xFF2A0018),
                primaryContainer = Color(0xFF5A0034),
                secondary = Color(0xFF00E5FF),
                onSecondary = Color(0xFF001318),
                tertiary = Color(0xFFC6FF00),
                background = UiNeutrals.darkBackground,
                onBackground = UiNeutrals.darkOnBackground,
                surface = UiNeutrals.darkSurface,
                onSurface = UiNeutrals.darkOnSurface,
                surfaceVariant = UiNeutrals.darkSurfaceVariant,
                error = UiNeutrals.darkError,
                onError = UiNeutrals.darkOnError,
            ),
    )

val citrusGrove =
    Theme(
        name = "Citrus Grove",
        light =
            UiColors(
                primary = Color(0xFF2B7D00),
                onPrimary = Color(0xFFFFFFFF),
                primaryContainer = Color(0xFFE6FFB3),
                secondary = Color(0xFF9A4F00),
                onSecondary = Color(0xFFFFFFFF),
                tertiary = Color(0xFF006D9C),
                background = UiNeutrals.lightBackground,
                onBackground = UiNeutrals.lightOnBackground,
                surface = UiNeutrals.lightSurface,
                onSurface = UiNeutrals.lightOnSurface,
                surfaceVariant = UiNeutrals.lightSurfaceVariant,
                error = UiNeutrals.lightError,
                onError = UiNeutrals.lightOnError,
            ),
        dark =
            UiColors(
                primary = Color(0xFFC6FF00),
                onPrimary = Color(0xFF1B2200),
                primaryContainer = Color(0xFF334000),
                secondary = Color(0xFFFFB84D),
                onSecondary = Color(0xFF2A1700),
                tertiary = Color(0xFF00E5FF),
                background = UiNeutrals.darkBackground,
                onBackground = UiNeutrals.darkOnBackground,
                surface = UiNeutrals.darkSurface,
                onSurface = UiNeutrals.darkOnSurface,
                surfaceVariant = UiNeutrals.darkSurfaceVariant,
                error = UiNeutrals.darkError,
                onError = UiNeutrals.darkOnError,
            ),
    )

val docsSlate =
    Theme(
        name = "Docs Slate",
        light =
            UiColors(
                primary = Color(0xFF0E6BA8),
                onPrimary = Color(0xFFFFFFFF),
                primaryContainer = Color(0xFFD2E9FF),
                secondary = Color(0xFF0E7490),
                onSecondary = Color(0xFFFFFFFF),
                tertiary = Color(0xFFB45309),
                background = UiNeutrals.lightBackground,
                onBackground = UiNeutrals.lightOnBackground,
                surface = UiNeutrals.lightSurface,
                onSurface = UiNeutrals.lightOnSurface,
                surfaceVariant = UiNeutrals.lightSurfaceVariant,
                error = UiNeutrals.lightError,
                onError = UiNeutrals.lightOnError,
            ),
        dark =
            UiColors(
                primary = Color(0xFF7CC4FA),
                onPrimary = Color(0xFF002B45),
                primaryContainer = Color(0xFF004C76),
                secondary = Color(0xFF67E8F9),
                onSecondary = Color(0xFF002A33),
                tertiary = Color(0xFFFDBA74),
                background = UiNeutrals.darkBackground,
                onBackground = UiNeutrals.darkOnBackground,
                surface = UiNeutrals.darkSurface,
                onSurface = UiNeutrals.darkOnSurface,
                surfaceVariant = UiNeutrals.darkSurfaceVariant,
                error = UiNeutrals.darkError,
                onError = UiNeutrals.darkOnError,
            ),
    )
