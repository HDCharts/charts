package io.github.dautovicharis.charts

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import io.github.dautovicharis.charts.internal.NO_SELECTION
import io.github.dautovicharis.charts.internal.TestTags
import io.github.dautovicharis.charts.internal.common.composable.Chart
import io.github.dautovicharis.charts.internal.common.composable.ChartErrors
import io.github.dautovicharis.charts.internal.common.model.ChartDataItem
import io.github.dautovicharis.charts.internal.common.model.MultiChartData
import io.github.dautovicharis.charts.internal.common.palette.generateColorShades
import io.github.dautovicharis.charts.internal.radarchart.RadarChart
import io.github.dautovicharis.charts.internal.radarchart.RadarLegend
import io.github.dautovicharis.charts.internal.radarchart.categoryColors
import io.github.dautovicharis.charts.internal.radarchart.toInternal
import io.github.dautovicharis.charts.internal.validateRadarData
import io.github.dautovicharis.charts.model.ChartData
import io.github.dautovicharis.charts.model.ChartSelection
import io.github.dautovicharis.charts.model.ChartValueFormatters
import io.github.dautovicharis.charts.model.rememberChartSelection
import io.github.dautovicharis.charts.style.RadarChartDefaults
import io.github.dautovicharis.charts.style.RadarChartStyle
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import io.github.dautovicharis.charts.internal.common.model.ChartData as InternalChartData

/** Displays one or more radar polygons sharing common axes. */
@Composable
fun RadarChart(
    data: ChartData,
    modifier: Modifier = Modifier,
    style: RadarChartStyle = RadarChartDefaults.style(),
    title: String? = null,
    selection: ChartSelection = rememberChartSelection(),
    interactionEnabled: Boolean = true,
    animateOnStart: Boolean = true,
) {
    val axisCount =
        data.series
            .firstOrNull()
            ?.values
            ?.size ?: 0
    val selectedIndex = selection.selectedIndex?.takeIf { it in 0 until axisCount } ?: NO_SELECTION
    val previousData = remember(selection) { mutableStateOf<ChartData?>(null) }
    LaunchedEffect(data) {
        val oldData = previousData.value
        if (oldData != null && oldData != data) selection.clear()
        previousData.value = data
    }

    val internalData = remember(data, title) { toInternalRadarData(data, title) }
    val internalStyle = style.toInternal()
    val hasSingleSeries = data.series.size == 1
    val errors =
        remember(data, style) {
            validateRadarData(
                data = data,
                paletteSize = if (hasSingleSeries) 0 else style.polygon.lineColors.size,
                categoryPaletteSize = style.categories.colors.size,
                categoryCount = if (data.categories.isEmpty()) 0 else data.categories.size,
            )
        }

    if (errors.isNotEmpty()) {
        ChartErrors(style.chartContainerStyle, errors.toImmutableList(), modifier)
        return
    }

    val lineColors =
        remember(style.polygon, data.series.size, hasSingleSeries) {
            if (hasSingleSeries) {
                persistentListOf(style.polygon.lineColor)
            } else if (style.polygon.lineColors.isEmpty()) {
                generateColorShades(style.polygon.lineColor, data.series.size)
            } else {
                style.polygon.lineColors
            }.toImmutableList()
        }
    val categories: ImmutableList<String> = data.categories.toImmutableList()
    val categoryColorsList =
        remember(internalStyle, categories.size) {
            categoryColors(internalStyle, categories.size)
        }
    val seriesNames = data.series.map { it.name.orEmpty() }.toImmutableList()
    val hasMultipleSeries = data.series.size > 1
    val selectedTitle = data.categories.getOrNull(selectedIndex)
    val effectiveTitle = selectedTitle ?: title.orEmpty()
    val selectedLabels =
        if (selectedIndex == NO_SELECTION || data.series.isEmpty()) {
            persistentListOf()
        } else {
            data.series
                .map { ChartValueFormatters.Default.format(it.values[selectedIndex]) }
                .toImmutableList()
        }
    Chart(chartContainerStyle = style.chartContainerStyle, modifier = modifier) {
        if (effectiveTitle.isNotBlank()) {
            Text(
                modifier =
                    style.chartContainerStyle.modifierTopTitle
                        .testTag(TestTags.CHART_TITLE),
                text = effectiveTitle,
                style = style.chartContainerStyle.styleTitle,
            )
        }
        RadarChart(
            data = internalData,
            style = internalStyle,
            colors = lineColors,
            categoryColors = categoryColorsList,
            axisLabels = categories,
            interactionEnabled = interactionEnabled,
            animateOnStart = animateOnStart,
            selectedAxisIndex = selectedIndex,
            onValueChanged = { index ->
                if (index == NO_SELECTION) selection.clear() else selection.select(index)
            },
        )
        if (
            data.categories.isNotEmpty() &&
            (style.categories.legendVisible || selectedIndex != NO_SELECTION)
        ) {
            RadarLegend(
                chartContainerStyle = style.chartContainerStyle,
                series = if (hasMultipleSeries) seriesNames else persistentListOf(),
                seriesColors = lineColors,
                seriesLabels = if (hasMultipleSeries) selectedLabels else persistentListOf(),
                categories = categories,
                categoryColors = categoryColorsList,
            )
        }
    }
}

private fun toInternalRadarData(
    data: ChartData,
    title: String?,
): MultiChartData {
    val categories = data.categories
    val items =
        data.series.map { series ->
            ChartDataItem(
                label = series.name.orEmpty(),
                item =
                    InternalChartData(
                        if (categories.isEmpty()) {
                            series.values.mapIndexed { index, value -> "" to value }
                        } else {
                            categories.zip(series.values) { c, v -> c to v }
                        },
                    ),
            )
        }
    return MultiChartData(
        items = items,
        categories = categories,
        title = title.orEmpty(),
    )
}
