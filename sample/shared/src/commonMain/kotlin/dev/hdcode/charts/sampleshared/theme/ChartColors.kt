package dev.hdcode.charts.sampleshared.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList

/**
 * Chart and data palette used by charts, separate from MaterialTheme UI colors.
 */
@Immutable
data class ChartColors(
    val series: ImmutableList<Color>,
    val seriesMuted: ImmutableList<Color>,
    val gridLine: Color,
    val axisLine: Color,
    val axisLabel: Color,
    val valueLabel: Color,
    val selection: Color,
    val highlight: Color,
)

val LocalChartColors =
    staticCompositionLocalOf<ChartColors> {
        error("ChartColors not provided")
    }

private val DarkSeries =
    persistentListOf(
        Color(0xFF4FB6FF),
        Color(0xFF7C6BFF),
        Color(0xFFC86BFF),
        Color(0xFFFF79A9),
        Color(0xFFFFA14D),
        Color(0xFFF2D15A),
        Color(0xFF48D1B5),
        Color(0xFF8BD96C),
    )

private val LightSeries =
    persistentListOf(
        Color(0xFF1E89E6),
        Color(0xFF5C4EE5),
        Color(0xFF9B4EDD),
        Color(0xFFE54F89),
        Color(0xFFEA7A2E),
        Color(0xFFCFB13A),
        Color(0xFF22B59A),
        Color(0xFF5EBA44),
    )

val DarkChartColors =
    ChartColors(
        series = DarkSeries,
        seriesMuted = mutedSeries(DarkSeries),
        gridLine = Color(0xFF242430),
        axisLine = Color(0xFF3A3A4A),
        axisLabel = Color(0xFFC2C2D2),
        valueLabel = Color(0xFFF2F2FA),
        selection = DarkSeries[0],
        highlight = DarkSeries[5],
    )

val LightChartColors =
    ChartColors(
        series = LightSeries,
        seriesMuted = mutedSeries(LightSeries),
        gridLine = Color(0xFFE3E3EE),
        axisLine = Color(0xFFC6C6D6),
        axisLabel = Color(0xFF4A4A5A),
        valueLabel = Color(0xFF121218),
        selection = LightSeries[0],
        highlight = LightSeries[5],
    )

/**
 * Returns a stable color for a series index from the current palette.
 */
fun ChartColors.seriesColor(index: Int): Color {
    if (series.isEmpty()) return Color.Unspecified
    val safeIndex = ((index % series.size) + series.size) % series.size
    return series[safeIndex]
}

/**
 * Returns a stable color for a series key from the current palette.
 */
fun ChartColors.seriesColorForKey(key: String): Color {
    if (series.isEmpty()) return Color.Unspecified
    val index = (key.hashCode().toUInt() % series.size.toUInt()).toInt()
    return series[index]
}

/**
 * Generates a list of series colors for a count using the palette.
 */
fun ChartColors.seriesColors(count: Int): List<Color> {
    if (count <= 0) return emptyList()
    return List(count) { seriesColor(it) }
}

/**
 * Generates a list of series colors for keys using the palette.
 */
fun ChartColors.seriesColors(keys: List<String>): List<Color> {
    if (keys.isEmpty()) return emptyList()
    return keys.map { seriesColorForKey(it) }
}

private fun mutedSeries(
    series: ImmutableList<Color>,
    alpha: Float = 0.45f,
): ImmutableList<Color> = series.map { it.copy(alpha = alpha) }.toImmutableList()
