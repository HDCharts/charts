package io.github.hdcharts.pie.internal

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import io.github.hdcharts.core.internal.InternalChartsApi
import io.github.hdcharts.core.internal.NO_SELECTION
import io.github.hdcharts.core.internal.composable.ChartHeading
import io.github.hdcharts.core.internal.composable.ChartSquarePlotLayout
import io.github.hdcharts.core.internal.composable.Legend
import io.github.hdcharts.core.internal.layout.modifierTopTitle
import io.github.hdcharts.core.internal.selectedCategoryTitle
import io.github.hdcharts.core.internal.selectedValueTitle
import io.github.hdcharts.core.model.ChartSelection
import io.github.hdcharts.core.model.ChartValueFormatter
import io.github.hdcharts.pie.PieChartStyle
import kotlinx.collections.immutable.ImmutableList

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
    selectedValueFormatter: ChartValueFormatter,
    labels: ImmutableList<String>,
    points: ImmutableList<Double>,
    colors: ImmutableList<Color>,
    style: PieChartStyle,
    selection: ChartSelection,
    interactionEnabled: Boolean,
    animateOnStart: Boolean,
    onSelectionInteraction: () -> Unit,
    subtitle: String?,
) {
    val shares =
        remember(points) {
            calculateShares(points)
        }
    val forcedSelectedIndex =
        selection.selectedIndex?.takeIf { it in points.indices } ?: NO_SELECTION
    val hasSelection = forcedSelectedIndex != NO_SELECTION

    ChartSquarePlotLayout(
        modifier = modifier,
        title = {
            // A selected slice is named by its category, falling back to the chart title.
            val categoryTitle =
                selectedCategoryTitle(
                    categories = labels,
                    selectedIndex = forcedSelectedIndex,
                    title = title,
                )
            val displayedTitle =
                if (hasSelection) {
                    selectedValueTitle(
                        category = categoryTitle,
                        value = selectedValueFormatter.format(shares[forcedSelectedIndex]),
                    )
                } else {
                    categoryTitle
                }
            ChartHeading(
                title = displayedTitle,
                titleTextStyle = style.chartContainerStyle.styleTitle,
                subtitle = subtitle,
                subtitleTextStyle = style.chartContainerStyle.styleSubtitle,
                modifier = style.chartContainerStyle.modifierTopTitle,
            )
        },
        legend = {
            // The legend names the slices.
            Legend(
                chartContainerStyle = style.chartContainerStyle,
                style = style.legend,
                legend = labels,
                colors = colors,
            )
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
