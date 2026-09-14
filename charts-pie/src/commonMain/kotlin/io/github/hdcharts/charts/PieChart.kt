package io.github.hdcharts.charts

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import io.github.hdcharts.charts.internal.NO_SELECTION
import io.github.hdcharts.charts.internal.TestTags
import io.github.hdcharts.charts.internal.common.composable.Chart
import io.github.hdcharts.charts.internal.common.composable.ChartErrors
import io.github.hdcharts.charts.internal.common.composable.Legend
import io.github.hdcharts.charts.internal.common.palette.generateColorShades
import io.github.hdcharts.charts.internal.piechart.PieChart
import io.github.hdcharts.charts.internal.piechart.calculatePercentages
import io.github.hdcharts.charts.internal.validatePieData
import io.github.hdcharts.charts.model.ChartSelection
import io.github.hdcharts.charts.model.PieSlice
import io.github.hdcharts.charts.model.SelectionLifetime
import io.github.hdcharts.charts.model.rememberChartSelection
import io.github.hdcharts.charts.model.rememberSelectionLifecycle
import io.github.hdcharts.charts.style.PieChartDefaults
import io.github.hdcharts.charts.style.PieChartStyle
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList

private const val SELECTED_TITLE_PERCENTAGE_SIZE_FACTOR = 0.72f
internal const val PIE_SELECTION_AUTO_DESELECT_TIMEOUT_MS = 3000L

/**
 * A composable function that displays a Pie Chart.
 *
 * [interactionEnabled] disables all user controls (tap-to-select and the auto-deselect
 * timeout), but programmatic selection still renders. [animateOnStart] controls the
 * initial reveal, not subsequent update animations.
 *
 * @param data The chart data to display. Each [PieSlice] renders as a slice with its own
 * label, value, and optional color. Slices without a color fall back to shades generated
 * from the style's base color.
 * @param modifier The modifier to be applied to the chart. Also forwarded to the error
 * branch when the data fails validation.
 * @param style The style to be applied to the chart. If not provided, the default style will be used.
 * @param title Optional chart title displayed when no slice is selected.
 * @param selection The hoisted selection state. Use [rememberChartSelection] for interactive
 *   charts or [io.github.hdcharts.charts.model.staticChartSelection] for deterministic
 *   preset selections.
 * @param interactionEnabled When `false`, disables tap-to-select and the auto-deselect timeout.
 * @param animateOnStart When `false`, renders the chart in its final state without the
 *   initial reveal animation.
 */
@Composable
fun PieChart(
    data: List<PieSlice>,
    modifier: Modifier = Modifier,
    style: PieChartStyle = PieChartDefaults.style(),
    title: String? = null,
    selection: ChartSelection = rememberChartSelection(),
    interactionEnabled: Boolean = true,
    animateOnStart: Boolean = true,
) {
    var interactionNonce by remember(data, selection) { mutableStateOf<Long?>(null) }
    rememberSelectionLifecycle(
        selection = selection,
        data = data,
        itemCount = data.size,
        lifetime =
            if (interactionEnabled) {
                SelectionLifetime.AutoDeselect(PIE_SELECTION_AUTO_DESELECT_TIMEOUT_MS)
            } else {
                SelectionLifetime.Persistent
            },
        autoDeselectTrigger = interactionNonce,
    )

    val validationErrors =
        remember(data) {
            validatePieData(data)
        }

    if (validationErrors.isNotEmpty()) {
        ChartErrors(
            style = style.chartContainerStyle,
            errors = validationErrors.toImmutableList(),
            modifier = modifier,
        )
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
    val points = remember(data) { data.map { it.value }.toImmutableList() }

    PieChartContent(
        modifier = modifier,
        title = title,
        labels = labels,
        points = points,
        colors = colors,
        style = style,
        selection = selection,
        interactionEnabled = interactionEnabled,
        animateOnStart = animateOnStart,
        onSelectionInteraction = {
            interactionNonce = (interactionNonce ?: 0L) + 1L
        },
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
    selection: ChartSelection,
    interactionEnabled: Boolean,
    animateOnStart: Boolean,
    onSelectionInteraction: () -> Unit,
) {
    val piePercentages =
        remember(points) {
            calculatePercentages(points)
        }
    val forcedSelectedIndex =
        selection.selectedIndex?.takeIf { it in points.indices } ?: NO_SELECTION
    val hasSelection = forcedSelectedIndex != NO_SELECTION

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
            interactionEnabled = interactionEnabled,
            animateOnStart = animateOnStart,
            selectedSliceIndex = forcedSelectedIndex,
        ) { index ->
            if (index != NO_SELECTION) {
                selection.select(index)
                onSelectionInteraction()
            } else {
                selection.clear()
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
): io.github.hdcharts.charts.internal.common.model.ChartData =
    io.github.hdcharts.charts.internal.common.model.ChartData(
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
