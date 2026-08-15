package io.github.dautovicharis.charts

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
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
import io.github.dautovicharis.charts.internal.common.composable.Chart
import io.github.dautovicharis.charts.internal.common.composable.ChartErrors
import io.github.dautovicharis.charts.internal.common.composable.Legend
import io.github.dautovicharis.charts.internal.common.palette.generateColorShades
import io.github.dautovicharis.charts.internal.piechart.PieChart
import io.github.dautovicharis.charts.internal.piechart.calculatePercentages
import io.github.dautovicharis.charts.internal.validatePieData
import io.github.dautovicharis.charts.model.PieSlice
import io.github.dautovicharis.charts.style.PieChartDefaults
import io.github.dautovicharis.charts.style.PieChartStyle
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.delay

private const val SELECTED_TITLE_PERCENTAGE_SIZE_FACTOR = 0.72f
internal const val PIE_SELECTION_AUTO_DESELECT_TIMEOUT_MS = 3000L

/**
 * A composable function that displays a Pie Chart.
 *
 * @param data The chart data to display. Each [PieSlice] renders as a slice with its own
 * label, value, and optional color. Slices without a color fall back to shades generated
 * from the style's base color.
 * @param modifier The modifier to be applied to the chart.
 * @param style The style to be applied to the chart. If not provided, the default style will be used.
 * @param title Optional chart title displayed when no slice is selected.
 */
@Composable
fun PieChart(
    data: List<PieSlice>,
    modifier: Modifier = Modifier,
    style: PieChartStyle = PieChartDefaults.style(),
    title: String? = null,
) {
    val validationErrors =
        remember(data) {
            validatePieData(data)
        }

    if (validationErrors.isNotEmpty()) {
        ChartErrors(style = style.chartContainerStyle, errors = validationErrors.toImmutableList())
        return
    }

    val colors =
        remember(data, style.slices.alpha, style.slices.baseColor) {
            resolveSliceColors(
                slices = data,
                baseColor = style.slices.baseColor,
                alpha = style.slices.alpha,
            )
        }
    val labels = remember(data) { data.map { it.label }.toImmutableList() }
    val points = remember(data) { data.map { it.value.toDouble() }.toImmutableList() }

    PieChartContent(
        modifier = modifier,
        title = title,
        labels = labels,
        points = points,
        colors = colors,
        style = style,
    )
}

@Composable
private fun PieChartContent(
    modifier: Modifier,
    title: String?,
    labels: ImmutableList<String>,
    points: ImmutableList<Double>,
    colors: ImmutableList<Color>,
    style: PieChartStyle,
) {
    val selection = style.selection
    val piePercentages =
        remember(points) {
            calculatePercentages(points)
        }
    val forcedSelectedIndex =
        selection.selectedIndex?.takeIf { it in points.indices } ?: NO_SELECTION
    val hasSelection = forcedSelectedIndex != NO_SELECTION
    var interactionSelection by remember(points, selection) { mutableStateOf<Int?>(null) }
    var interactionNonce by remember(points, selection) { mutableStateOf(0L) }

    LaunchedEffect(interactionNonce) {
        if (interactionSelection == null) return@LaunchedEffect
        delay(PIE_SELECTION_AUTO_DESELECT_TIMEOUT_MS)
        if (selection.selectedIndex == interactionSelection) {
            selection.clear()
        }
        interactionSelection = null
    }

    Chart(
        chartContainerStyle = style.chartContainerStyle,
        modifier = modifier,
    ) {
        val displayedTitle = if (hasSelection) labels[forcedSelectedIndex] else title.orEmpty()
        if (displayedTitle.isNotBlank()) {
            if (hasSelection) {
                Row(
                    modifier =
                        style.chartContainerStyle.modifierTopTitle
                            .padding(end = style.chartContainerStyle.innerPadding),
                    horizontalArrangement =
                        Arrangement.spacedBy(style.chartContainerStyle.innerPadding),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        modifier = Modifier.testTag(TestTags.CHART_TITLE),
                        text = displayedTitle,
                        style = style.chartContainerStyle.styleTitle,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = "${piePercentages[forcedSelectedIndex]}%",
                        style = selectedPercentageStyle(style.chartContainerStyle.styleTitle),
                        maxLines = 1,
                    )
                }
            } else {
                Text(
                    modifier =
                        style.chartContainerStyle.modifierTopTitle
                            .testTag(TestTags.CHART_TITLE),
                    text = displayedTitle,
                    style = style.chartContainerStyle.styleTitle,
                )
            }
        }

        val chartData =
            remember(labels, points) {
                toInternalChartData(labels = labels, points = points)
            }
        PieChart(
            chartData = chartData,
            colors = colors,
            style = style,
            interactionEnabled = true,
            animateOnStart = true,
            selectedSliceIndex = forcedSelectedIndex,
        ) { index ->
            if (index != NO_SELECTION) {
                selection.select(index)
                interactionSelection = index
                interactionNonce++
            } else {
                selection.clear()
                interactionSelection = null
            }
        }

        if (style.legend.visible) {
            Legend(
                chartContainerStyle = style.chartContainerStyle,
                legend = labels,
                colors = colors,
            )
        }
    }
}

private fun resolveSliceColors(
    slices: List<PieSlice>,
    baseColor: Color,
    alpha: Float,
): ImmutableList<Color> {
    if (slices.isEmpty()) return persistentListOf()
    val defaultPalette =
        generateColorShades(
            baseColor = baseColor,
            numberOfShades = slices.size,
        )
    return slices
        .mapIndexed { index, slice ->
            (slice.color ?: defaultPalette[index % defaultPalette.size]).copy(alpha = alpha)
        }.toImmutableList()
}

private fun toInternalChartData(
    labels: ImmutableList<String>,
    points: ImmutableList<Double>,
): io.github.dautovicharis.charts.internal.common.model.ChartData =
    io.github.dautovicharis.charts.internal.common.model.ChartData(
        data =
            points.mapIndexed { index, value ->
                labels.getOrElse(index) { index.toString() } to value
            },
    )

private fun selectedPercentageStyle(base: TextStyle): TextStyle =
    base.copy(
        fontSize = base.fontSize * SELECTED_TITLE_PERCENTAGE_SIZE_FACTOR,
        fontWeight = FontWeight.SemiBold,
    )
