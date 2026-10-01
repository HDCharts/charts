package io.github.hdcharts.radar

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import io.github.hdcharts.core.internal.InternalChartsApi
import io.github.hdcharts.core.internal.NO_SELECTION
import io.github.hdcharts.core.internal.TestTags
import io.github.hdcharts.core.internal.ValidationErrors
import io.github.hdcharts.core.internal.composable.ChartErrors
import io.github.hdcharts.core.internal.composable.Legend
import io.github.hdcharts.core.internal.layout.modifierTopTitle
import io.github.hdcharts.core.internal.model.ChartDataItem
import io.github.hdcharts.core.internal.model.MultiChartData
import io.github.hdcharts.core.internal.validateSeries
import io.github.hdcharts.core.model.ChartData
import io.github.hdcharts.core.model.ChartSelection
import io.github.hdcharts.core.model.ChartValueFormatters
import io.github.hdcharts.core.model.rememberChartSelection
import io.github.hdcharts.core.model.rememberSelectionLifecycle
import io.github.hdcharts.radar.internal.RadarChart
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import io.github.hdcharts.core.internal.model.ChartData as InternalChartData

/**
 * Displays one or more radar polygons sharing common axes.
 *
 * [selection] holds the axis selected by dragging, and a tap clears it. [seriesSelection] holds the
 * series focused by tapping its outline.
 */
@OptIn(InternalChartsApi::class)
@Composable
fun RadarChart(
    data: ChartData,
    modifier: Modifier = Modifier,
    style: RadarChartStyle = RadarChartDefaults.style(),
    title: String? = null,
    selection: ChartSelection = rememberChartSelection(),
    seriesSelection: ChartSelection = rememberChartSelection(),
    interactionEnabled: Boolean = true,
    animateOnStart: Boolean = true,
) {
    val axisCount =
        data.series
            .firstOrNull()
            ?.values
            ?.size ?: 0
    val selectedIndex = selection.selectedIndex?.takeIf { it in 0 until axisCount } ?: NO_SELECTION
    rememberSelectionLifecycle(
        selection = selection,
        data = data,
        itemCount = axisCount,
    )
    val hasMultipleSeries = data.series.size > 1
    val focusedSeriesIndex =
        seriesSelection.selectedIndex?.takeIf { hasMultipleSeries && it in data.series.indices } ?: NO_SELECTION
    rememberSelectionLifecycle(
        selection = seriesSelection,
        data = data,
        itemCount = if (hasMultipleSeries) data.series.size else 0,
    )

    val internalData = remember(data, title) { toInternalRadarData(data, title) }
    val hasSingleSeries = data.series.size == 1
    val density = LocalDensity.current
    val errors =
        remember(data, style, density) {
            validateSeries(
                data = data,
                minValues = ValidationErrors.MIN_RADAR_VALUES,
                colorCount = if (hasSingleSeries) 0 else style.polygon.lineColors.size,
            )
        }
    val drawStyle = remember(style, density) { style.clamped(density) }

    if (errors.isNotEmpty()) {
        ChartErrors(style.chartContainerStyle, errors.toImmutableList(), modifier)
        return
    }

    val lineColors =
        remember(style.polygon, data.series.size) {
            style.polygon.resolveLineColors(data.series.size)
        }
    val categories: ImmutableList<String> = data.categories.toImmutableList()
    val seriesNames = data.series.map { it.name.orEmpty() }.toImmutableList()
    // A single series has no legend to carry its value, so the title takes it, like a bar or a line.
    // Several series put their values in the legend, so the title only names the axis.
    val singleSeries = data.series.singleOrNull()
    val selectedTitle =
        when {
            selectedIndex == NO_SELECTION -> null
            singleSeries != null ->
                resolveSelectedAxisTitle(
                    category = data.categories.getOrNull(selectedIndex),
                    value = singleSeries.values.getOrNull(selectedIndex),
                )
            else -> data.categories.getOrNull(selectedIndex)
        }
    val effectiveTitle = selectedTitle ?: title.orEmpty()
    val selectedLabels =
        when {
            selectedIndex == NO_SELECTION || data.series.isEmpty() -> persistentListOf()
            else ->
                data.series
                    .map { series -> ChartValueFormatters.Default.format(series.values[selectedIndex]) }
                    .toImmutableList()
        }
    BoxWithConstraints(modifier = modifier) {
        val boundedHeight = maxHeight != Dp.Infinity
        // Match the width the caller asks for, so the plot centers in a wide chart.
        Column(modifier = Modifier.widthIn(min = minWidth)) {
            if (effectiveTitle.isNotBlank()) {
                Text(
                    modifier =
                        style.chartContainerStyle.modifierTopTitle
                            .testTag(TestTags.CHART_TITLE),
                    text = effectiveTitle,
                    style = style.chartContainerStyle.styleTitle,
                )
            }
            // The web is a circle, so the plot box stays square and is centered over the
            // shorter side. This mirrors [PieChart]: it centers the chart and keeps the title
            // sitting above it instead of pinning the title to the start edge of a full-width box.
            val plotModifier =
                if (boundedHeight) Modifier.weight(1f, fill = false) else Modifier
            Box(
                modifier =
                    plotModifier
                        .aspectRatio(1f)
                        .align(Alignment.CenterHorizontally),
            ) {
                RadarChart(
                    data = internalData,
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
            }
            // The legend names the series and shows each value while an axis is selected. Categories
            // are named by the axis labels, and a single series needs no legend.
            val legendSeries = if (hasMultipleSeries) seriesNames else persistentListOf()
            if (legendSeries.isNotEmpty()) {
                Legend(
                    chartContainerStyle = style.chartContainerStyle,
                    legend = legendSeries,
                    colors = lineColors,
                    labels = selectedLabels,
                )
            }
        }
    }
}

/** The title for a selected axis: `Category: value`, or just the value when the category is blank. */
private fun resolveSelectedAxisTitle(
    category: String?,
    value: Double?,
): String? {
    if (value == null) return category
    val formatted = ChartValueFormatters.Default.format(value)
    val label = category.orEmpty()
    return if (label.isBlank()) formatted else "$label: $formatted"
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
