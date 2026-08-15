package io.github.dautovicharis.charts.style

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import io.github.dautovicharis.charts.internal.DONUT_MAX_PERCENTAGE
import io.github.dautovicharis.charts.internal.DONUT_MIN_PERCENTAGE
import io.github.dautovicharis.charts.internal.piechart.AdaptivePieSizeModifier
import io.github.dautovicharis.charts.model.ChartSelection
import io.github.dautovicharis.charts.model.rememberChartSelection

/**
 * The style for a Pie Chart, grouped into cohesive sub-styles.
 *
 * @property chartContainerStyle The shared container/layout presentation.
 * @property donut The donut configuration of the chart.
 * @property slices The slice configuration of the chart.
 * @property border The border configuration of the chart.
 * @property legend The legend configuration of the chart.
 * @property selection The selection state of the chart. Use [io.github.dautovicharis.charts.model.rememberChartSelection]
 * for interactive charts or [io.github.dautovicharis.charts.model.staticChartSelection] for
 * deterministic preset selections.
 */
@Stable
class PieChartStyle(
    internal val modifier: Modifier,
    val chartContainerStyle: ChartContainerStyle,
    val donut: PieChartDonutStyle,
    val slices: PieChartSlicesStyle,
    val border: PieChartBorderStyle,
    val legend: LegendStyle,
    val selection: ChartSelection,
) : Style {
    /**
     * Returns a flat list of the style properties.
     *
     * The keys mirror the legacy flat names so codegen snapshots keep working
     * across the migration.
     */
    override fun getProperties(): List<Pair<String, Any>> =
        listOf(
            "donutPercentage" to donut.holePercentage,
            "pieColor" to slices.baseColor,
            "pieAlpha" to slices.alpha,
            "borderColor" to border.color,
            "borderWidth" to border.width,
            "legendVisible" to legend.visible,
        )
}

/**
 * Donut configuration for a [PieChartStyle].
 *
 * @property holePercentage The percentage of the chart that is a donut hole.
 * Must be between [io.github.dautovicharis.charts.internal.DONUT_MIN_PERCENTAGE]
 * and [io.github.dautovicharis.charts.internal.DONUT_MAX_PERCENTAGE].
 */
@Immutable
data class PieChartDonutStyle(
    val holePercentage: Float,
)

/**
 * Slice configuration for a [PieChartStyle].
 *
 * @property alpha The alpha value applied to rendered pie slices.
 * @property baseColor The base color used to generate shades for slices that do not
 * specify their own color via [io.github.dautovicharis.charts.model.PieSlice.color].
 */
@Immutable
data class PieChartSlicesStyle(
    val alpha: Float,
    val baseColor: Color,
)

/**
 * Border configuration for a [PieChartStyle].
 *
 * @property width The width of the border around the pie chart.
 * @property color The color of the border around the pie chart.
 */
@Immutable
data class PieChartBorderStyle(
    val width: Float,
    val color: Color,
)

/**
 * An object that provides default styles for a Pie Chart.
 */
object PieChartDefaults {
    /**
     * Returns a [PieChartStyle] with the provided parameters or their default values.
     *
     * @param chartContainerStyle The style to be applied to the chart view. Defaults to the default style of ChartContainerDefaults.
     * @param donut The donut configuration. Defaults to a chart without a donut hole.
     * @param slices The slice configuration. Defaults to theme-derived slice colors and alpha.
     * @param border The border configuration. Defaults to a 3f border using the surface color.
     * @param legend The legend configuration. Defaults to a visible legend.
     * @param selection The selection state of the chart. Defaults to an empty remembered selection.
     */
    @Composable
    fun style(
        chartContainerStyle: ChartContainerStyle = ChartContainerDefaults.style(),
        donut: PieChartDonutStyle = donut(),
        slices: PieChartSlicesStyle = slices(),
        border: PieChartBorderStyle = border(),
        legend: LegendStyle = legend(),
        selection: ChartSelection = rememberChartSelection(),
    ): PieChartStyle {
        val modifier: Modifier =
            AdaptivePieSizeModifier
                .padding(chartContainerStyle.innerPadding)
                .fillMaxSize()
        return PieChartStyle(
            modifier = modifier,
            chartContainerStyle = chartContainerStyle,
            donut = donut,
            slices = slices,
            border = border,
            legend = legend,
            selection = selection,
        )
    }

    /**
     * Returns a [PieChartDonutStyle] with the provided parameters or their default values.
     *
     * @param holePercentage The percentage of the chart that is a donut hole. Defaults to 0f.
     */
    @Composable
    fun donut(holePercentage: Float = 0f): PieChartDonutStyle =
        PieChartDonutStyle(
            holePercentage =
                holePercentage.coerceIn(
                    DONUT_MIN_PERCENTAGE,
                    DONUT_MAX_PERCENTAGE,
                ),
        )

    /**
     * Returns a [PieChartSlicesStyle] with the provided parameters or their default values.
     *
     * @param baseColor The base color used to generate shades for slices that do not specify
     * their own color via [io.github.dautovicharis.charts.model.PieSlice.color]. Defaults to
     * the primary color of the MaterialTheme.
     * @param alpha The alpha value applied to rendered pie slices. Defaults to 0.4f in light
     * theme and 0.6f in dark theme.
     */
    @Composable
    fun slices(
        baseColor: Color = MaterialTheme.colorScheme.primary,
        alpha: Float = defaultChartAlpha(),
    ): PieChartSlicesStyle =
        PieChartSlicesStyle(
            alpha = alpha.coerceIn(0f, 1f),
            baseColor = baseColor,
        )

    /**
     * Returns a [PieChartBorderStyle] with the provided parameters or their default values.
     *
     * @param color The color of the border around the pie chart. Defaults to the surface color of the MaterialTheme.
     * @param width The width of the border around the pie chart. Defaults to 3f.
     */
    @Composable
    fun border(
        color: Color = MaterialTheme.colorScheme.surface,
        width: Float = 3f,
    ): PieChartBorderStyle =
        PieChartBorderStyle(
            width = width,
            color = color,
        )

    /**
     * Returns a [LegendStyle] with the provided parameters or their default values.
     *
     * @param visible Whether the legend is visible. Defaults to true.
     */
    @Composable
    fun legend(visible: Boolean = true): LegendStyle = LegendDefaults.style(visible = visible)
}
