package io.github.dautovicharis.charts.internal.common.palette

import androidx.compose.ui.graphics.Color
import io.github.dautovicharis.charts.internal.InternalChartsApi
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

private const val GOLDEN_ANGLE_DEGREES = 137.508f
private const val HUE_MAX_DEGREES = 360f

private object PaletteBounds {
    const val NEAR_GRAY_SATURATION = 0.04f
    const val MIN_SERIES_SATURATION = 0.33f
    const val MAX_SERIES_SATURATION = 0.85f
    const val MIN_SERIES_LIGHTNESS = 0.35f
    const val MAX_SERIES_LIGHTNESS = 0.65f
}

private object SeriesVariation {
    const val LIGHTNESS_AMPLITUDE = 0.11f
    const val SATURATION_AMPLITUDE = 0.09f
    const val LIGHTNESS_FREQUENCY = 1.37
    const val SATURATION_FREQUENCY = 1.71
    const val DECAY = 0.07f
    const val MIN_SCALE = 0.35f
}

private object MonochromeVariation {
    const val TINT_SATURATION = 0.02f
    const val AMPLITUDE_STEP = 0.1f
    const val MAX_AMPLITUDE = 0.28f
}

private data class Hsl(
    val hue: Float,
    val saturation: Float,
    val lightness: Float,
)

@InternalChartsApi
fun generateColorShades(
    baseColor: Color,
    numberOfShades: Int,
    minSeriesLightness: Float = PaletteBounds.MIN_SERIES_LIGHTNESS,
    maxSeriesLightness: Float = PaletteBounds.MAX_SERIES_LIGHTNESS,
): ImmutableList<Color> {
    if (numberOfShades <= 0) return persistentListOf()
    if (numberOfShades == 1) return persistentListOf(baseColor)

    val clampedMinSeriesLightness = minSeriesLightness.coerceIn(0f, 1f)
    val clampedMaxSeriesLightness =
        maxSeriesLightness
            .coerceIn(clampedMinSeriesLightness, 1f)

    val baseHsl = baseColor.toHsl()

    if (baseHsl.saturation <= PaletteBounds.NEAR_GRAY_SATURATION) {
        return generateMonochromeShades(
            baseColor = baseColor,
            baseHsl = baseHsl,
            numberOfShades = numberOfShades,
            minSeriesLightness = clampedMinSeriesLightness,
            maxSeriesLightness = clampedMaxSeriesLightness,
        )
    }

    val baseSaturation =
        baseHsl.saturation.coerceIn(
            PaletteBounds.MIN_SERIES_SATURATION,
            PaletteBounds.MAX_SERIES_SATURATION,
        )
    val baseLightness =
        baseHsl.lightness.coerceIn(clampedMinSeriesLightness, clampedMaxSeriesLightness)

    return List(numberOfShades) { index ->
        if (index == 0) {
            // Keep the input color unchanged to preserve brand/base palette intent.
            baseColor
        } else {
            val hue = (baseHsl.hue + (GOLDEN_ANGLE_DEGREES * index)).normalizeHue()
            val variationScale = getVariationScale(index)
            val phase = index.toDouble()
            val saturationOffset =
                (
                    SeriesVariation.SATURATION_AMPLITUDE *
                        variationScale *
                        sin(phase * SeriesVariation.SATURATION_FREQUENCY)
                ).toFloat()
            val lightnessOffset =
                (
                    SeriesVariation.LIGHTNESS_AMPLITUDE *
                        variationScale *
                        cos(phase * SeriesVariation.LIGHTNESS_FREQUENCY)
                ).toFloat()
            val saturation =
                (baseSaturation + saturationOffset)
                    .coerceIn(
                        PaletteBounds.MIN_SERIES_SATURATION,
                        PaletteBounds.MAX_SERIES_SATURATION,
                    )
            val lightness =
                (baseLightness + lightnessOffset)
                    .coerceIn(clampedMinSeriesLightness, clampedMaxSeriesLightness)
            hslToColor(hue, saturation, lightness, baseColor.alpha)
        }
    }.toImmutableList()
}

private fun generateMonochromeShades(
    baseColor: Color,
    baseHsl: Hsl,
    numberOfShades: Int,
    minSeriesLightness: Float,
    maxSeriesLightness: Float,
): ImmutableList<Color> {
    val baseLightness = baseHsl.lightness.coerceIn(minSeriesLightness, maxSeriesLightness)
    val neutralSaturation =
        if (baseHsl.saturation == 0f) 0f else MonochromeVariation.TINT_SATURATION

    return List(numberOfShades) { index ->
        if (index == 0) {
            baseColor
        } else {
            val amplitudeScale = getVariationScale(index)
            val amplitudeUncapped =
                MonochromeVariation.AMPLITUDE_STEP * ((index + 1) / 2f) * amplitudeScale
            val amplitude = amplitudeUncapped.coerceAtMost(MonochromeVariation.MAX_AMPLITUDE)
            val direction = if (index % 2 == 0) -1f else 1f
            val lightness =
                (baseLightness + (direction * amplitude))
                    .coerceIn(minSeriesLightness, maxSeriesLightness)
            hslToColor(
                hue = baseHsl.hue,
                saturation = neutralSaturation,
                lightness = lightness,
                alpha = baseColor.alpha,
            )
        }
    }.toImmutableList()
}

private fun getVariationScale(index: Int): Float {
    if (index <= 1) return 1f
    return (1f / (1f + ((index - 1) * SeriesVariation.DECAY)))
        .coerceAtLeast(SeriesVariation.MIN_SCALE)
}

private fun Color.toHsl(): Hsl {
    val max = maxOf(red, green, blue)
    val min = minOf(red, green, blue)
    val delta = max - min

    val hue =
        when {
            delta == 0f -> 0f
            max == red -> {
                val segment = (green - blue) / delta
                val wrappedSegment = if (segment < 0f) segment + 6f else segment
                60f * wrappedSegment
            }
            max == green -> 60f * (((blue - red) / delta) + 2f)
            else -> 60f * (((red - green) / delta) + 4f)
        }

    val lightness = (max + min) / 2f
    val saturation =
        if (delta == 0f) {
            0f
        } else {
            delta / (1f - abs(2f * lightness - 1f))
        }

    return Hsl(
        hue = hue.coerceIn(0f, HUE_MAX_DEGREES),
        saturation = saturation.coerceIn(0f, 1f),
        lightness = lightness.coerceIn(0f, 1f),
    )
}

private fun hslToColor(
    hue: Float,
    saturation: Float,
    lightness: Float,
    alpha: Float,
): Color {
    val c = (1f - abs(2f * lightness - 1f)) * saturation
    val normalizedHue = hue.normalizeHue()
    val hPrime = normalizedHue / 60f
    val x = c * (1f - abs((hPrime % 2f) - 1f))

    val (rPrime, gPrime, bPrime) =
        when {
            hPrime < 1f -> Triple(c, x, 0f)
            hPrime < 2f -> Triple(x, c, 0f)
            hPrime < 3f -> Triple(0f, c, x)
            hPrime < 4f -> Triple(0f, x, c)
            hPrime < 5f -> Triple(x, 0f, c)
            else -> Triple(c, 0f, x)
        }

    val m = lightness - (c / 2f)
    return Color(
        red = (rPrime + m).coerceIn(0f, 1f),
        green = (gPrime + m).coerceIn(0f, 1f),
        blue = (bPrime + m).coerceIn(0f, 1f),
        alpha = alpha.coerceIn(0f, 1f),
    )
}

private fun Float.normalizeHue(): Float {
    val normalized = this % HUE_MAX_DEGREES
    return if (normalized < 0f) normalized + HUE_MAX_DEGREES else normalized
}
