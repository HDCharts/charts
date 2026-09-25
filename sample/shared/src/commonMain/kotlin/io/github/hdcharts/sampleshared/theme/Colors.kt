package io.github.hdcharts.sampleshared.theme

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
    // Optional roles; when null, the Material baseline value is kept.
    val onPrimaryContainer: Color? = null,
    val secondaryContainer: Color? = null,
    val onSecondaryContainer: Color? = null,
    val onSurfaceVariant: Color? = null,
    val errorContainer: Color? = null,
    val outline: Color? = null,
    val outlineVariant: Color? = null,
    val surfaceTint: Color? = null,
    val surfaceContainerLow: Color? = null,
    val surfaceContainer: Color? = null,
    val surfaceContainerHigh: Color? = null,
    val surfaceContainerHighest: Color? = null,
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

/**
 * Theme mirroring the charts-docs site tokens (docs-app/app/globals.css).
 * The docs site is light-only; the dark variant is derived from the same teal and sage-gray scale.
 */
val docsTheme =
    Theme(
        name = "Docs",
        light =
            UiColors(
                primary = Color(0xFF39777A),
                onPrimary = Color(0xFFFFFFFF),
                primaryContainer = Color(0xFFDCE9E8),
                onPrimaryContainer = Color(0xFF23585C),
                secondary = Color(0xFF786FAE),
                onSecondary = Color(0xFFFFFFFF),
                secondaryContainer = Color(0xFFE7EFEF),
                onSecondaryContainer = Color(0xFF23585C),
                tertiary = Color(0xFF8A601C),
                background = Color(0xFFF7F9F8),
                onBackground = Color(0xFF1F2933),
                surface = Color(0xFFFFFFFF),
                onSurface = Color(0xFF1F2933),
                surfaceVariant = Color(0xFFEEF3F1),
                onSurfaceVariant = Color(0xFF5F6D73),
                surfaceTint = Color(0xFF39777A),
                error = Color(0xFFB65C57),
                onError = Color(0xFFFFFFFF),
                errorContainer = Color(0xFFF6E3E1),
                outline = Color(0xFF94A39D),
                outlineVariant = Color(0xFFDDE5E2),
                surfaceContainerLow = Color(0xFFF8FAF9),
                surfaceContainer = Color(0xFFF1F5F3),
                surfaceContainerHigh = Color(0xFFE7EEEB),
                surfaceContainerHighest = Color(0xFFDDE5E2),
            ),
        dark =
            UiColors(
                primary = Color(0xFF7FC3C5),
                onPrimary = Color(0xFF00373A),
                primaryContainer = Color(0xFF1F5457),
                onPrimaryContainer = Color(0xFFBFE6E6),
                secondary = Color(0xFFBDB6E6),
                onSecondary = Color(0xFF2E275C),
                secondaryContainer = Color(0xFF2A3D3A),
                onSecondaryContainer = Color(0xFFBFE6E6),
                tertiary = Color(0xFFE3B872),
                background = Color(0xFF121A18),
                onBackground = Color(0xFFE7EEEB),
                surface = Color(0xFF172120),
                onSurface = Color(0xFFE7EEEB),
                surfaceVariant = Color(0xFF2B3A36),
                onSurfaceVariant = Color(0xFFB6C4BF),
                surfaceTint = Color(0xFF7FC3C5),
                error = Color(0xFFE8A09B),
                onError = Color(0xFF5C1814),
                errorContainer = Color(0xFF7A2F2B),
                outline = Color(0xFF74827D),
                outlineVariant = Color(0xFF40514B),
                surfaceContainerLow = Color(0xFF172120),
                surfaceContainer = Color(0xFF1F2B28),
                surfaceContainerHigh = Color(0xFF25332F),
                surfaceContainerHighest = Color(0xFF2F3E3A),
            ),
    )
