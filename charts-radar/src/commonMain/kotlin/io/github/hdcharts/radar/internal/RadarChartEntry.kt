package io.github.hdcharts.radar.internal

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Density
import io.github.hdcharts.core.internal.ChartEntry
import io.github.hdcharts.core.internal.ChartPolicy
import io.github.hdcharts.core.internal.ChartSpec
import io.github.hdcharts.core.internal.InternalChartsApi
import io.github.hdcharts.core.internal.NO_SELECTION
import io.github.hdcharts.core.internal.SeriesRule
import io.github.hdcharts.core.internal.ValidationErrors
import io.github.hdcharts.core.internal.composable.ChartHeading
import io.github.hdcharts.core.internal.composable.ChartSquarePlotLayout
import io.github.hdcharts.core.internal.composable.Legend
import io.github.hdcharts.core.internal.layout.modifierTopTitle
import io.github.hdcharts.core.internal.selectedLegendValues
import io.github.hdcharts.core.internal.selectedTitle
import io.github.hdcharts.core.model.ChartData
import io.github.hdcharts.core.model.ChartSelection
import io.github.hdcharts.core.model.ChartValueFormatter
import io.github.hdcharts.radar.RadarChartStyle
import io.github.hdcharts.radar.clamp
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

/**
 * Everything [io.github.hdcharts.radar.RadarChart] declares about its input. A radar axis holds one
 * value per category, so it needs at least three; it has no Cartesian axis and no fixed range, and it
 * allows negative values.
 */
@InternalChartsApi
object RadarChartSpec : ChartSpec<RadarChartStyle> {
    override val policy =
        ChartPolicy<RadarChartStyle>(
            series =
                SeriesRule.Multiple(
                    minValues = ValidationErrors.MIN_RADAR_VALUES,
                    allowNegative = true,
                    stacked = false,
                    // One series cannot be mis-coloured, so the count is only checked when there are several.
                    expectedColors = { data -> data.series.size.takeIf { count -> count > 1 } },
                ),
            colorCount = { it.polygon.lineColors.size },
            range = null,
            axes = null,
        )

    override fun clamp(
        style: RadarChartStyle,
        density: Density,
    ): RadarChartStyle = style.clamp(density)
}

/**
 * Runs [RadarChartSpec] through the shared seam, then draws the render model with
 * [RadarChartContent]. Both selection indexes are resolved by the caller, because they read the
 * public data rather than the render model.
 */
@Composable
internal fun RadarChartEntry(
    data: ChartData,
    modifier: Modifier,
    style: RadarChartStyle,
    title: String?,
    selectedIndex: Int,
    focusedSeriesIndex: Int,
    selection: ChartSelection,
    seriesSelection: ChartSelection,
    interactionEnabled: Boolean,
    animateOnStart: Boolean,
    selectedValueFormatter: ChartValueFormatter,
    subtitle: String?,
) {
    ChartEntry(
        spec = RadarChartSpec,
        data = data,
        style = style,
        errorStyle = style.chartContainerStyle,
        title = title,
        content = { renderData, drawStyle ->
            val lineColors =
                remember(drawStyle.polygon, data.series.size) {
                    drawStyle.polygon.resolveLineColors(data.series.size)
                }
            val categories: ImmutableList<String> = data.categories.toImmutableList()
            val seriesNames = data.series.map { it.name.orEmpty() }.toImmutableList()
            val effectiveTitle =
                selectedTitle(
                    data = data,
                    selectedIndex = selectedIndex,
                    title = title,
                    selectedValueFormatter = selectedValueFormatter,
                )

            ChartSquarePlotLayout(
                modifier = modifier,
                title = {
                    ChartHeading(
                        title = effectiveTitle,
                        titleTextStyle = drawStyle.chartContainerStyle.styleTitle,
                        subtitle = subtitle,
                        subtitleTextStyle = drawStyle.chartContainerStyle.styleSubtitle,
                        modifier = drawStyle.chartContainerStyle.modifierTopTitle,
                    )
                },
                legend = {
                    // The legend names the series and shows each value while an axis is selected.
                    Legend(
                        chartContainerStyle = drawStyle.chartContainerStyle,
                        style = drawStyle.legend,
                        legend = seriesNames,
                        colors = lineColors,
                        labels =
                            selectedLegendValues(
                                data = data,
                                selectedIndex = selectedIndex,
                                selectedValueFormatter = selectedValueFormatter,
                            ),
                    )
                },
                plot = {
                    RadarChartContent(
                        data = renderData,
                        style = drawStyle,
                        colors = lineColors,
                        axisLabels = categories,
                        interactionEnabled = interactionEnabled,
                        animateOnStart = animateOnStart,
                        selectedAxisIndex = selectedIndex,
                        focusedSeriesIndex = focusedSeriesIndex,
                        onValueChanged = { index ->
                            if (index == NO_SELECTION) selection.clear() else selection.select(index)
                        },
                        onFocusedSeriesChanged = { index ->
                            if (index == NO_SELECTION) seriesSelection.clear() else seriesSelection.select(index)
                        },
                    )
                },
            )
        },
        modifier = modifier,
    )
}
