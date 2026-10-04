package io.github.hdcharts.pie.internal

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import io.github.hdcharts.core.internal.InternalChartsApi
import io.github.hdcharts.core.internal.NO_SELECTION
import io.github.hdcharts.core.internal.TestTags
import io.github.hdcharts.core.internal.composable.ChartSquarePlotLayout
import io.github.hdcharts.core.internal.composable.Legend
import io.github.hdcharts.core.internal.layout.modifierTopTitle
import io.github.hdcharts.core.internal.selectedCategoryTitle
import io.github.hdcharts.core.model.ChartSelection
import io.github.hdcharts.pie.PieChartStyle
import kotlinx.collections.immutable.ImmutableList

internal const val SELECTED_TITLE_PERCENTAGE_SIZE_FACTOR = 0.72f

/**
 * The layout shell around a pie: a title that becomes the selected slice's name and share, the
 * legend, and the plot. Reads the clamped style and the validated values, and holds nothing the
 * caller did not pass.
 */
@OptIn(InternalChartsApi::class)
@Composable
internal fun PieChartFrame(
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

    ChartSquarePlotLayout(
        modifier = modifier,
        title = {
            // A selected slice is named by its category, falling back to the chart title.
            val displayedTitle =
                selectedCategoryTitle(
                    categories = labels,
                    selectedIndex = forcedSelectedIndex,
                    title = title,
                )
            if (displayedTitle.isNotBlank()) {
                if (hasSelection) {
                    Row(
                        modifier =
                            style.chartContainerStyle.modifierTopTitle
                                .padding(end = style.chartContainerStyle.contentPadding),
                        horizontalArrangement =
                            Arrangement.spacedBy(style.chartContainerStyle.contentPadding),
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
        },
        legend = {
            // The legend names the slices, so it has nothing to list without categories. This is the
            // same rule the stacked charts use: no categories, no legend.
            if (style.legend.visible && labels.isNotEmpty()) {
                Legend(
                    chartContainerStyle = style.chartContainerStyle,
                    legend = labels,
                    colors = colors,
                )
            }
        },
        plot = {
            PieChartContent(
                values = points,
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
        },
    )
}

private fun selectedPercentageStyle(base: TextStyle): TextStyle =
    base.copy(
        fontSize = base.fontSize * SELECTED_TITLE_PERCENTAGE_SIZE_FACTOR,
        fontWeight = FontWeight.SemiBold,
    )
