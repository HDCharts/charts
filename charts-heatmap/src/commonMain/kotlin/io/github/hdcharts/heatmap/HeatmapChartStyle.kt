package io.github.hdcharts.heatmap

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import io.github.hdcharts.core.internal.clampSize
import io.github.hdcharts.core.model.ChartValueFormatter
import io.github.hdcharts.core.style.AxisLabelDefaults
import io.github.hdcharts.core.style.AxisLabelStyle
import io.github.hdcharts.core.style.ChartContainerDefaults
import io.github.hdcharts.core.style.ChartContainerStyle
import io.github.hdcharts.core.style.LegendDefaults
import io.github.hdcharts.core.style.LegendStyle
import io.github.hdcharts.core.style.StyleDefaults
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

/** How a [HeatmapChart] paints its values. */
enum class HeatmapRenderMode {
    /** One flat rectangle per cell, colored by that cell's value. */
    Cells,

    /**
     * A smooth color field computed from the values. Each cell value sits at the cell's center, and
     * colors between centers follow a smooth, shape-preserving bicubic interpolation of the four
     * neighboring values: it never leaves their range or adds a peak or valley of its own. Next to a
     * missing cell, only the present neighbors are blended, with their weights rescaled, so no value
     * is blended from data that does not exist and the field stays continuous. Missing cells draw empty.
     */
    Interpolated,
}

/**
 * Maps values to colors, shared by both render modes and the legend.
 *
 * [colors] are evenly spaced from the lowest to the highest value, so two colors give a sequential
 * scale and three colors around a zero midpoint give a diverging one. Colors blend linearly in sRGB
 * between neighboring stops. Values outside the bounds draw with the nearest end color.
 *
 * @property colors Ordered colors from the lowest value to the highest; at least two.
 * @property min Fixed lowest value, or `null` to use the smallest cell value.
 * @property max Fixed highest value, or `null` to use the largest cell value. When both bounds are
 * `null` and every cell has the same value, every cell uses the middle of the scale.
 */
@Immutable
data class HeatmapColorScale(
    val colors: ImmutableList<Color>,
    val min: Double? = null,
    val max: Double? = null,
) {
    /** Copies the supplied colors into an immutable list. */
    constructor(
        colors: List<Color>,
        min: Double? = null,
        max: Double? = null,
    ) : this(colors.toImmutableList(), min, max)
}

/**
 * Presentation for heatmap charts.
 *
 * @property renderMode Discrete cells or an interpolated color field.
 * @property colorScale Value-to-color mapping shared by both modes and the legend.
 * @property cellSpacing Gap between cells in [HeatmapRenderMode.Cells]; the interpolated field has none.
 * @property xLabels Column label configuration.
 * @property yLabels Row label configuration.
 * @property legend Visibility of the color-scale legend.
 */
@Immutable
class HeatmapChartStyle(
    val chartContainerStyle: ChartContainerStyle,
    val renderMode: HeatmapRenderMode,
    val colorScale: HeatmapColorScale,
    val cellSpacing: Dp,
    val xLabels: AxisLabelStyle,
    val yLabels: AxisLabelStyle,
    val legend: LegendStyle,
)

/** Defaults for heatmap charts. */
object HeatmapChartDefaults {
    /** Formats legend and selected values with the shared chart defaults. */
    val valueFormatter: ChartValueFormatter = StyleDefaults.axisValueFormatter

    @Composable
    fun style(
        chartContainerStyle: ChartContainerStyle = ChartContainerDefaults.style(),
        renderMode: HeatmapRenderMode = HeatmapRenderMode.Cells,
        colorScale: HeatmapColorScale = colorScale(),
        cellSpacing: Dp = StyleDefaults.heatmapCellSpacing,
        xLabels: AxisLabelStyle = AxisLabelDefaults.style(),
        yLabels: AxisLabelStyle = AxisLabelDefaults.style(),
        legend: LegendStyle = LegendDefaults.style(),
    ) = HeatmapChartStyle(chartContainerStyle, renderMode, colorScale, cellSpacing, xLabels, yLabels, legend)

    /** Returns a theme-aware sequential scale from a light tint to the primary color. */
    @Composable
    fun colorScale(
        colors: List<Color> = listOf(StyleDefaults.heatmapLowColor, StyleDefaults.heatmapHighColor),
        min: Double? = null,
        max: Double? = null,
    ) = HeatmapColorScale(colors, min, max)
}

internal fun HeatmapChartStyle.clamp(density: Density) =
    HeatmapChartStyle(
        chartContainerStyle = chartContainerStyle,
        renderMode = renderMode,
        colorScale = colorScale,
        cellSpacing = cellSpacing.clampSize(StyleDefaults.heatmapCellSpacing, density),
        xLabels = xLabels,
        yLabels = yLabels,
        legend = legend,
    )
