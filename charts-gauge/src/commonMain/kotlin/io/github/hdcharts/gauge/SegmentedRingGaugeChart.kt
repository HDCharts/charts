package io.github.hdcharts.gauge

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import io.github.hdcharts.core.internal.InternalChartsApi
import io.github.hdcharts.core.internal.NO_SELECTION
import io.github.hdcharts.core.model.ChartData
import io.github.hdcharts.core.model.ChartSelection
import io.github.hdcharts.core.model.ChartValueFormatter
import io.github.hdcharts.core.model.SelectionLifetime
import io.github.hdcharts.core.model.rememberChartSelection
import io.github.hdcharts.core.model.rememberSelectionLifecycle
import io.github.hdcharts.gauge.internal.SegmentedRingGaugeChartEntry

internal const val SEGMENTED_RING_GAUGE_SELECTION_AUTO_DESELECT_TIMEOUT_MS = 3000L

/**
 * Draws one circular capacity ring whose filled amount is divided into colored segments.
 *
 * Each value in the single [data] series is a non-negative contribution to [capacity]. Their sum
 * must not exceed that capacity. Categories optionally name segments in the legend and selected
 * title. The unfilled part of the ring remains visible as the track, while [centerLabel] defaults
 * to the formatted segment total. Tapping a segment selects it for three seconds; tapping again
 * renews that timeout. Programmatic selections persist when [interactionEnabled] is false.
 *
 * @param data One series of at least one non-negative finite value. Categories are optional and,
 * when set, must contain one name for every value.
 * @param capacity The finite positive total represented by the full ring.
 * @param centerLabel The primary text inside the ring. When null, the sum of segments is formatted.
 * @param centerSupportingLabel Optional text below [centerLabel], such as `of 2000 kcal`.
 * @param interactionEnabled When `false`, disables tap-to-select and the auto-deselect timeout.
 */
@OptIn(InternalChartsApi::class)
@Composable
fun SegmentedRingGaugeChart(
    data: ChartData,
    capacity: Double,
    modifier: Modifier = Modifier,
    style: SegmentedRingGaugeChartStyle = SegmentedRingGaugeChartDefaults.style(),
    title: String? = null,
    centerLabel: String? = null,
    centerSupportingLabel: String? = null,
    centerValueFormatter: ChartValueFormatter = SegmentedRingGaugeChartDefaults.centerValueFormatter,
    selectedValueFormatter: ChartValueFormatter = SegmentedRingGaugeChartDefaults.selectedValueFormatter,
    selection: ChartSelection = rememberChartSelection(),
    interactionEnabled: Boolean = true,
    animateOnStart: Boolean = true,
    subtitle: String? = null,
) {
    var interactionNonce by remember(data, selection) { mutableStateOf<Long?>(null) }
    val segmentCount =
        data.series
            .singleOrNull()
            ?.values
            ?.size ?: 0
    rememberSelectionLifecycle(
        selection = selection,
        data = data,
        itemCount = segmentCount,
        lifetime =
            if (interactionEnabled) {
                SelectionLifetime.AutoDeselect(SEGMENTED_RING_GAUGE_SELECTION_AUTO_DESELECT_TIMEOUT_MS)
            } else {
                SelectionLifetime.Persistent
            },
        autoDeselectTrigger = interactionNonce,
    )
    val selectedIndex = selection.selectedIndex?.takeIf { it in 0 until segmentCount } ?: NO_SELECTION

    SegmentedRingGaugeChartEntry(
        data = data,
        capacity = capacity,
        modifier = modifier,
        style = style,
        title = title,
        centerLabel = centerLabel,
        centerSupportingLabel = centerSupportingLabel,
        centerValueFormatter = centerValueFormatter,
        selectedValueFormatter = selectedValueFormatter,
        selectedIndex = selectedIndex,
        selection = selection,
        interactionEnabled = interactionEnabled,
        animateOnStart = animateOnStart,
        subtitle = subtitle,
        onSelectionInteraction = { interactionNonce = (interactionNonce ?: 0L) + 1L },
    )
}
