package io.github.dautovicharis.charts

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import io.github.dautovicharis.charts.internal.NO_SELECTION
import io.github.dautovicharis.charts.internal.TestTags
import io.github.dautovicharis.charts.internal.common.palette.generateColorShades
import io.github.dautovicharis.charts.internal.common.composable.Chart
import io.github.dautovicharis.charts.internal.common.composable.ChartErrors
import io.github.dautovicharis.charts.internal.common.composable.Legend
import io.github.dautovicharis.charts.internal.piechart.PieChart
import io.github.dautovicharis.charts.internal.piechart.calculatePercentages
import io.github.dautovicharis.charts.internal.validatePieData
import io.github.dautovicharis.charts.model.ChartDataSet
import io.github.dautovicharis.charts.style.PieChartDefaults
import io.github.dautovicharis.charts.style.PieChartStyle
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.delay

private const val SELECTED_TITLE_PERCENTAGE_SIZE_FACTOR = 0.72f
internal const val PIE_SELECTION_AUTO_DESELECT_TIMEOUT_MS = 3000L

/**
 * A composable function that displays a Pie Chart.
 *
 * @param dataSet The data set to be displayed in the chart.
 * @param style The style to be applied to the chart. If not provided, the default style will be used.
 * @param interactionEnabled Enables touch interactions (tap selection). Defaults to true.
 * @param animateOnStart Enables initial chart animations. Defaults to true.
 * @param selectedSliceIndex Optional preselected slice index for deterministic rendering (e.g. screenshots).
 */
@Composable
fun PieChart(
    dataSet: ChartDataSet,
    style: PieChartStyle = PieChartDefaults.style(),
    interactionEnabled: Boolean = true,
    animateOnStart: Boolean = true,
    selectedSliceIndex: Int = NO_SELECTION,
) {
    val pieChartColors =
        remember(
            style.pieColors,
            style.pieColor,
            style.pieAlpha,
            dataSet.data.item.points.size,
        ) {
            if (style.pieColors.isEmpty()) {
                generateColorShades(
                    baseColor = style.pieColor.copy(alpha = style.pieAlpha),
                    numberOfShades = dataSet.data.item.points.size,
                )
            } else {
                style.pieColors
                    .map { color -> color.copy(alpha = style.pieAlpha) }
                    .toImmutableList()
            }
        }

    val errors =
        remember(dataSet, style) {
            validatePieData(dataSet = dataSet, style = style)
        }

    if (errors.isNotEmpty()) {
        ChartErrors(style = style.chartViewStyle, errors = errors.toImmutableList())
    } else {
        PieChartContent(
            dataSet = dataSet,
            style = style,
            pieChartColors = pieChartColors,
            interactionEnabled = interactionEnabled,
            animateOnStart = animateOnStart,
            selectedSliceIndex = selectedSliceIndex,
        )
    }
}

@Composable
private fun PieChartContent(
    dataSet: ChartDataSet,
    style: PieChartStyle,
    pieChartColors: ImmutableList<Color>,
    interactionEnabled: Boolean,
    animateOnStart: Boolean,
    selectedSliceIndex: Int,
) {
    val piePercentages =
        remember(dataSet.data.item.points) {
            calculatePercentages(dataSet.data.item.points)
        }
    val forcedSelectedIndex =
        selectedSliceIndex.takeIf { it in dataSet.data.item.points.indices } ?: NO_SELECTION
    var selectedIndex by remember(dataSet) { mutableIntStateOf(NO_SELECTION) }
    var selectionInteractionId by remember(dataSet) { mutableIntStateOf(0) }
    val effectiveSelectedIndex =
        when (forcedSelectedIndex) {
            NO_SELECTION -> selectedIndex
            else -> forcedSelectedIndex
        }
    val hasSelection = effectiveSelectedIndex != NO_SELECTION
    val selectedTitle =
        if (hasSelection) {
            dataSet.data.item.labels[effectiveSelectedIndex]
        } else {
            dataSet.data.label
        }

    LaunchedEffect(forcedSelectedIndex, selectedIndex, selectionInteractionId) {
        if (forcedSelectedIndex != NO_SELECTION || selectedIndex == NO_SELECTION) return@LaunchedEffect
        delay(PIE_SELECTION_AUTO_DESELECT_TIMEOUT_MS)
        selectedIndex = NO_SELECTION
    }

    Chart(chartViewsStyle = style.chartViewStyle) {
        if (selectedTitle.isNotBlank()) {
            if (hasSelection) {
                Row(
                    modifier =
                        style.chartViewStyle.modifierTopTitle
                            .padding(end = style.chartViewStyle.innerPadding),
                    horizontalArrangement = Arrangement.spacedBy(style.chartViewStyle.innerPadding),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        modifier = Modifier.testTag(TestTags.CHART_TITLE),
                        text = selectedTitle,
                        style = style.chartViewStyle.styleTitle,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = "${piePercentages[effectiveSelectedIndex]}%",
                        style = selectedPercentageStyle(style.chartViewStyle.styleTitle),
                        maxLines = 1,
                    )
                }
            } else {
                Text(
                    modifier = style.chartViewStyle.modifierTopTitle.testTag(TestTags.CHART_TITLE),
                    text = selectedTitle,
                    style = style.chartViewStyle.styleTitle,
                )
            }
        }
        PieChart(
            chartData = dataSet.data.item,
            colors = pieChartColors,
            style = style,
            interactionEnabled = interactionEnabled,
            animateOnStart = animateOnStart,
            selectedSliceIndex = effectiveSelectedIndex,
        ) { index ->
            if (forcedSelectedIndex == NO_SELECTION) {
                selectedIndex = index
                if (index != NO_SELECTION) {
                    selectionInteractionId += 1
                }
            }
        }

        if (style.legendVisible) {
            Legend(
                chartViewsStyle = style.chartViewStyle,
                legend = dataSet.data.item.labels,
                colors = pieChartColors,
            )
        }
    }
}

private fun selectedPercentageStyle(base: TextStyle): TextStyle =
    base.copy(
        fontSize = base.fontSize * SELECTED_TITLE_PERCENTAGE_SIZE_FACTOR,
        fontWeight = FontWeight.SemiBold,
    )
