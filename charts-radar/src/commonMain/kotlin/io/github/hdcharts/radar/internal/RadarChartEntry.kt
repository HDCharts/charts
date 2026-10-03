package io.github.hdcharts.radar.internal

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Density
import io.github.hdcharts.core.internal.ChartEntry
import io.github.hdcharts.core.internal.ChartPolicy
import io.github.hdcharts.core.internal.ChartSpec
import io.github.hdcharts.core.internal.ChartValidationInputs
import io.github.hdcharts.core.internal.InternalChartsApi
import io.github.hdcharts.core.internal.NO_SELECTION
import io.github.hdcharts.core.internal.TestTags
import io.github.hdcharts.core.internal.ValidationErrors
import io.github.hdcharts.core.internal.composable.ChartSquarePlotLayout
import io.github.hdcharts.core.internal.composable.Legend
import io.github.hdcharts.core.internal.layout.modifierTopTitle
import io.github.hdcharts.core.model.ChartData
import io.github.hdcharts.core.model.ChartSelection
import io.github.hdcharts.core.model.ChartValueFormatters
import io.github.hdcharts.radar.RadarChartStyle
import io.github.hdcharts.radar.clamp
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList

/**
 * Everything [io.github.hdcharts.radar.RadarChart] declares about its input. A radar axis holds one
 * value per category, so it needs at least three; it has no Cartesian axis and no fixed range, and it
 * allows negative values.
 */
@InternalChartsApi
object RadarChartSpec : ChartSpec<RadarChartStyle> {
    override val policy =
        ChartPolicy(
            minValues = ValidationErrors.MIN_RADAR_VALUES,
            allowNegative = true,
            singleSeries = false,
            hasAxis = false,
            hasFixedRange = false,
            // One series cannot be mis-coloured, so the count is only checked when there are several.
            colorsMatch = { data -> data.series.size.takeIf { count -> count > 1 } },
        )

    /** A radar chart has no Cartesian axis, so there are no axis label styles to check. */
    override fun validationInputs(style: RadarChartStyle): ChartValidationInputs =
        ChartValidationInputs(
            colorCount = style.polygon.lineColors.size,
            rangeMin = null,
            rangeMax = null,
            xLabels = null,
            yLabels = null,
        )

    override fun clamp(
        style: RadarChartStyle,
        density: Density,
    ): RadarChartStyle = style.clamp(density)
}

/**
 * Runs [RadarChartSpec] through the shared seam, then draws the render model with
 * [RadarChartContent]. Both selection indexes are resolved by the caller, because they read the
 * public data rather than the render model.
 */
@Composable
internal fun RadarChartEntry(
    data: ChartData,
    modifier: Modifier,
    style: RadarChartStyle,
    title: String?,
    selectedIndex: Int,
    focusedSeriesIndex: Int,
    selection: ChartSelection,
    seriesSelection: ChartSelection,
    interactionEnabled: Boolean,
    animateOnStart: Boolean,
) {
    ChartEntry(
        spec = RadarChartSpec,
        data = data,
        style = style,
        errorStyle = style.chartContainerStyle,
        title = title,
        content = { renderData, drawStyle ->
            val lineColors =
                remember(drawStyle.polygon, data.series.size) {
                    drawStyle.polygon.resolveLineColors(data.series.size)
                }
            val categories: ImmutableList<String> = data.categories.toImmutableList()
            val seriesNames = data.series.map { it.name.orEmpty() }.toImmutableList()
            val singleSeries = data.series.singleOrNull()
            // A single series has no legend to carry its value, so the title takes it, like a bar or a
            // line. Several series put their values in the legend, so the title only names the axis.
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
            // The legend names the series and shows each value while an axis is selected. Categories
            // are named by the axis labels, and a single series needs no legend.
            val legendSeries = if (data.series.size > 1) seriesNames else persistentListOf()

            ChartSquarePlotLayout(
                modifier = modifier,
                title = {
                    if (effectiveTitle.isNotBlank()) {
                        Text(
                            modifier =
                                drawStyle.chartContainerStyle.modifierTopTitle
                                    .testTag(TestTags.CHART_TITLE),
                            text = effectiveTitle,
                            style = drawStyle.chartContainerStyle.styleTitle,
                        )
                    }
                },
                legend = {
                    if (legendSeries.isNotEmpty()) {
                        Legend(
                            chartContainerStyle = drawStyle.chartContainerStyle,
                            legend = legendSeries,
                            colors = lineColors,
                            labels = selectedLabels,
                        )
                    }
                },
                plot = {
                    RadarChartContent(
                        data = renderData,
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
                },
            )
        },
        modifier = modifier,
    )
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
