package io.github.dautovicharis.charts

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import io.github.dautovicharis.charts.internal.NO_SELECTION
import io.github.dautovicharis.charts.internal.common.palette.generateColorShades
import io.github.dautovicharis.charts.internal.common.composable.Chart
import io.github.dautovicharis.charts.internal.common.composable.ChartErrors
import io.github.dautovicharis.charts.internal.common.composable.Legend
import io.github.dautovicharis.charts.internal.stackedareachart.StackedAreaChart
import io.github.dautovicharis.charts.internal.validateStackedAreaData
import io.github.dautovicharis.charts.model.MultiChartDataSet
import io.github.dautovicharis.charts.style.StackedAreaChartDefaults
import io.github.dautovicharis.charts.style.StackedAreaChartStyle
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList

/**
 * A composable function that displays a Stacked Area Chart.
 *
 * @param dataSet The data set to be displayed in the chart.
 * @param style The style to be applied to the chart. If not provided, the default style will be used.
 * @param interactionEnabled Enables touch interactions (drag selection). Defaults to true.
 * @param animateOnStart Enables initial chart animations. Defaults to true.
 * @param selectedPointIndex Optional preselected point index for deterministic rendering (e.g. screenshots).
 */
@Composable
fun StackedAreaChart(
    dataSet: MultiChartDataSet,
    style: StackedAreaChartStyle = StackedAreaChartDefaults.style(),
    interactionEnabled: Boolean = true,
    animateOnStart: Boolean = true,
    selectedPointIndex: Int = NO_SELECTION,
) {
    val errors =
        remember(dataSet, style) {
            validateStackedAreaData(
                data = dataSet.data,
                style = style,
            )
        }

    if (errors.isEmpty()) {
        StackedAreaChartContent(
            dataSet = dataSet,
            style = style,
            interactionEnabled = interactionEnabled,
            animateOnStart = animateOnStart,
            selectedPointIndex = selectedPointIndex,
        )
    } else {
        ChartErrors(style = style.chartViewStyle, errors = errors.toImmutableList())
    }
}

@Composable
private fun StackedAreaChartContent(
    dataSet: MultiChartDataSet,
    style: StackedAreaChartStyle,
    interactionEnabled: Boolean,
    animateOnStart: Boolean,
    selectedPointIndex: Int,
) {
    val pointsSize = dataSet.data.getFirstPointsSize()
    val forcedSelectedIndex =
        selectedPointIndex.takeIf { it in 0 until pointsSize } ?: NO_SELECTION
    val hasForcedSelection = forcedSelectedIndex != NO_SELECTION

    var title by remember(dataSet) { mutableStateOf(dataSet.data.title) }
    var labels by remember(dataSet) {
        mutableStateOf<ImmutableList<String>>(persistentListOf())
    }

    // Apply forced selection title/labels
    LaunchedEffect(forcedSelectedIndex, dataSet) {
        if (hasForcedSelection) {
            title = dataSet.data.getLabel(forcedSelectedIndex)
            labels =
                if (dataSet.data.hasCategories()) {
                    dataSet.data.items
                        .map { it.item.labels[forcedSelectedIndex] }
                        .toImmutableList()
                } else {
                    persistentListOf()
                }
        }
    }

    val areaColors =
        remember(dataSet, style.areaColors, style.areaColor) {
            if (dataSet.data.hasSingleItem()) {
                persistentListOf(style.areaColor)
            } else if (style.areaColors.isEmpty()) {
                generateColorShades(style.areaColor, dataSet.data.items.size)
            } else {
                style.areaColors.toImmutableList()
            }
        }
    val lineColors =
        remember(dataSet, style.lineColors, style.lineColor) {
            if (dataSet.data.hasSingleItem()) {
                persistentListOf(style.lineColor)
            } else if (style.lineColors.isEmpty()) {
                generateColorShades(style.lineColor, dataSet.data.items.size)
            } else {
                style.lineColors.toImmutableList()
            }
        }
    Chart(chartViewsStyle = style.chartViewStyle) {
        StackedAreaChart(
            data = dataSet.data,
            title = title,
            style = style,
            areaColors = areaColors,
            lineColors = lineColors,
            interactionEnabled = interactionEnabled,
            animateOnStart = animateOnStart,
            selectedPointIndex = selectedPointIndex,
        ) { selectedIndex ->
            if (!hasForcedSelection) {
                title = dataSet.data.getLabel(selectedIndex)

                if (dataSet.data.hasCategories()) {
                    labels =
                        when (selectedIndex) {
                            NO_SELECTION -> persistentListOf()
                            else ->
                                dataSet.data.items
                                    .map { it.item.labels[selectedIndex] }
                                    .toImmutableList()
                        }
                }
            }
        }

        if (dataSet.data.hasCategories()) {
            Legend(
                chartViewsStyle = style.chartViewStyle,
                legend =
                    dataSet.data.items
                        .map { it.label }
                        .toImmutableList(),
                colors = areaColors,
                labels = labels,
            )
        }
    }
}
