package io.github.dautovicharis.charts.internal.radarchart

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.testTag
import io.github.dautovicharis.charts.internal.NO_SELECTION
import io.github.dautovicharis.charts.internal.TestTags
import io.github.dautovicharis.charts.internal.common.palette.generateColorShades
import io.github.dautovicharis.charts.internal.common.composable.Chart
import io.github.dautovicharis.charts.internal.common.composable.ChartErrors
import io.github.dautovicharis.charts.internal.common.model.MultiChartData
import io.github.dautovicharis.charts.internal.validateRadarData
import io.github.dautovicharis.charts.style.RadarChartDefaults
import io.github.dautovicharis.charts.style.RadarChartStyle
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList

@Composable
internal fun RadarChartImpl(
    data: MultiChartData,
    style: RadarChartStyle = RadarChartDefaults.style(),
    interactionEnabled: Boolean = true,
    animateOnStart: Boolean = true,
    selectedAxisIndex: Int = NO_SELECTION,
) {
    val legendAnimationDuration = 300
    val errors =
        remember(data, style) {
            validateRadarData(
                data = data,
                style = style,
            )
        }

    if (errors.isEmpty()) {
        var title by remember(data) { mutableStateOf(data.title) }
        var selectedIndex by remember(data) { mutableIntStateOf(NO_SELECTION) }
        var seriesLabels by remember(data) {
            mutableStateOf<ImmutableList<String>>(persistentListOf())
        }

        val axisCount = data.getFirstPointsSize()
        val forcedSelectedIndex =
            selectedAxisIndex.takeIf { it in 0 until axisCount } ?: NO_SELECTION
        val hasForcedSelection = forcedSelectedIndex != NO_SELECTION

        // Apply forced selection title/labels
        LaunchedEffect(forcedSelectedIndex, data) {
            if (hasForcedSelection) {
                title = data.getLabel(forcedSelectedIndex)
                seriesLabels =
                    data.items
                        .map {
                            it.item.labels
                                .getOrNull(forcedSelectedIndex)
                                .orEmpty()
                        }.toImmutableList()
            }
        }

        val lineColors =
            remember(data, style.lineColors, style.lineColor) {
                if (data.hasSingleItem()) {
                    persistentListOf(style.lineColor)
                } else if (style.lineColors.isEmpty()) {
                    generateColorShades(style.lineColor, data.items.size)
                } else {
                    style.lineColors.toImmutableList()
                }
            }
        Chart(chartViewsStyle = style.chartViewStyle) {
            if (title.isNotBlank()) {
                Text(
                    modifier =
                        style.chartViewStyle.modifierTopTitle
                            .testTag(TestTags.CHART_TITLE),
                    text = title,
                    style = style.chartViewStyle.styleTitle,
                )
            }

            val categories = if (data.hasCategories()) data.categories else persistentListOf()
            val legendVisible = style.categoryLegendVisible || selectedIndex != NO_SELECTION
            val legendCategories =
                when (legendVisible) {
                    true -> categories
                    else -> persistentListOf()
                }
            val categoryColorsList =
                remember(categories, style.categoryColors) {
                    categoryColors(style, categories.size)
                }

            RadarChart(
                data = data,
                style = style,
                colors = lineColors,
                categoryColors = categoryColorsList,
                axisLabels = categories,
                interactionEnabled = interactionEnabled,
                animateOnStart = animateOnStart,
                selectedAxisIndex = selectedAxisIndex,
                onValueChanged = { index ->
                    if (!hasForcedSelection) {
                        selectedIndex = index
                        title = data.getLabel(index)
                        seriesLabels =
                            when (index) {
                                NO_SELECTION -> persistentListOf()
                                else ->
                                    data.items
                                        .map {
                                            it.item.labels
                                                .getOrNull(index)
                                                .orEmpty()
                                        }.toImmutableList()
                            }
                    }
                },
            )

            val series =
                if (!legendVisible || data.hasSingleItem()) {
                    persistentListOf()
                } else {
                    data.items.map { it.label }.toImmutableList()
                }

            val hasLegendContent = series.isNotEmpty() || legendCategories.isNotEmpty()

            AnimatedVisibility(
                visible = legendVisible && hasLegendContent,
                enter =
                    fadeIn(
                        animationSpec =
                            tween(
                                durationMillis = legendAnimationDuration,
                                easing = LinearOutSlowInEasing,
                            ),
                    ) +
                        expandVertically(
                            animationSpec =
                                tween(
                                    durationMillis = legendAnimationDuration,
                                    easing = LinearOutSlowInEasing,
                                ),
                            expandFrom = Alignment.Top,
                        ),
                exit =
                    fadeOut(
                        animationSpec =
                            tween(
                                durationMillis = legendAnimationDuration,
                                easing = LinearOutSlowInEasing,
                            ),
                    ) +
                        shrinkVertically(
                            animationSpec =
                                tween(
                                    durationMillis = legendAnimationDuration,
                                    easing = LinearOutSlowInEasing,
                                ),
                            shrinkTowards = Alignment.Top,
                        ),
            ) {
                RadarLegend(
                    chartViewsStyle = style.chartViewStyle,
                    series = series,
                    seriesColors = lineColors,
                    seriesLabels = seriesLabels,
                    categories = legendCategories,
                    categoryColors = categoryColorsList,
                )
            }
        }
    } else {
        ChartErrors(style = style.chartViewStyle, errors = errors.toImmutableList())
    }
}
