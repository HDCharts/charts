package io.github.dautovicharis.charts

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import io.github.dautovicharis.charts.internal.NO_SELECTION
import io.github.dautovicharis.charts.internal.barstackedchart.StackedBarChart
import io.github.dautovicharis.charts.internal.common.palette.generateColorShades
import io.github.dautovicharis.charts.internal.common.composable.Chart
import io.github.dautovicharis.charts.internal.common.composable.ChartErrors
import io.github.dautovicharis.charts.internal.common.composable.Legend
import io.github.dautovicharis.charts.internal.validateBarData
import io.github.dautovicharis.charts.model.MultiChartDataSet
import io.github.dautovicharis.charts.style.StackedBarChartDefaults
import io.github.dautovicharis.charts.style.StackedBarChartStyle
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList

/**
 * A composable function that displays a Stacked Bar Chart.
 *
 * @param dataSet The data set to be displayed in the chart.
 * @param style The style to be applied to the chart. If not provided, the default style will be used.
 * @param interactionEnabled Enables touch interactions (drag selection). Defaults to true.
 * @param animateOnStart Enables initial chart animations. Defaults to true.
 * @param selectedBarIndex Optional preselected bar index for deterministic rendering (e.g. screenshots).
 */
@Composable
fun StackedBarChart(
    dataSet: MultiChartDataSet,
    style: StackedBarChartStyle = StackedBarChartDefaults.style(),
    interactionEnabled: Boolean = true,
    animateOnStart: Boolean = true,
    selectedBarIndex: Int = NO_SELECTION,
) {
    val errors =
        remember(dataSet, style) {
            validateBarData(
                data = dataSet.data,
                style = style,
            )
        }

    if (errors.isEmpty()) {
        StackedBarChartContent(
            dataSet = dataSet,
            style = style,
            interactionEnabled = interactionEnabled,
            animateOnStart = animateOnStart,
            selectedBarIndex = selectedBarIndex,
        )
    } else {
        ChartErrors(style = style.chartViewStyle, errors = errors.toImmutableList())
    }
}

@Composable
private fun StackedBarChartContent(
    dataSet: MultiChartDataSet,
    style: StackedBarChartStyle,
    interactionEnabled: Boolean,
    animateOnStart: Boolean,
    selectedBarIndex: Int,
) {
    val dataSize = dataSet.data.items.size
    val forcedSelectedIndex =
        selectedBarIndex.takeIf { it in 0 until dataSize } ?: NO_SELECTION
    val hasForcedSelection = forcedSelectedIndex != NO_SELECTION

    var title by remember(dataSet) { mutableStateOf(dataSet.data.title) }
    var labels by remember(dataSet) {
        mutableStateOf<ImmutableList<String>>(persistentListOf())
    }

    // Apply forced selection title/labels
    LaunchedEffect(forcedSelectedIndex, dataSet) {
        if (hasForcedSelection) {
            title = dataSet.data.items[forcedSelectedIndex].label
            labels =
                if (dataSet.data.hasCategories()) {
                    dataSet.data.items[forcedSelectedIndex]
                        .item.labels
                } else {
                    persistentListOf()
                }
        }
    }

    val colors: ImmutableList<androidx.compose.ui.graphics.Color> =
        remember(
            dataSet,
            style.barColors,
            style.barColor,
            style.barAlpha,
        ) {
            if (style.barColors.isEmpty()) {
                generateColorShades(
                    baseColor = style.barColor.copy(alpha = style.barAlpha),
                    numberOfShades = dataSet.data.getFirstPointsSize(),
                )
            } else {
                style.barColors
                    .map { color -> color.copy(alpha = style.barAlpha) }
                    .toImmutableList()
            }
        }
    Chart(chartViewsStyle = style.chartViewStyle) {
        StackedBarChart(
            data = dataSet.data,
            title = title,
            style = style,
            colors = colors,
            interactionEnabled = interactionEnabled,
            animateOnStart = animateOnStart,
            selectedBarIndex = selectedBarIndex,
        ) { selectedIndex ->
            if (!hasForcedSelection) {
                title =
                    when (selectedIndex) {
                        NO_SELECTION -> dataSet.data.title
                        else -> {
                            dataSet.data.items[selectedIndex].label
                        }
                    }

                if (dataSet.data.hasCategories()) {
                    labels =
                        when (selectedIndex) {
                            NO_SELECTION -> persistentListOf()
                            else ->
                                dataSet.data.items[selectedIndex]
                                    .item.labels
                        }
                }
            }
        }

        if (dataSet.data.hasCategories()) {
            Legend(
                chartViewsStyle = style.chartViewStyle,
                colors = colors,
                legend = dataSet.data.categories,
                labels = labels,
            )
        }
    }
}
