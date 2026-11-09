package io.github.hdcharts.waterfall

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.hdcharts.core.model.ChartSelection
import io.github.hdcharts.core.model.ChartValueFormatter
import io.github.hdcharts.core.model.rememberChartSelection
import io.github.hdcharts.waterfall.internal.WaterfallChartEntry

/**
 * Draws floating change bars joined at their running balances, and total bars from zero.
 *
 * [selection] uses source entry indices. The heading displays the selected category, change amount
 * for a [WaterfallEntry.Change], and resulting balance. [WaterfallData.runningTotalAt] provides that
 * balance to callers. Programmatic selection also works with [interactionEnabled] set to false.
 *
 * All bars fit the available width; labels use the shared axis planner to reduce crowding.
 * [valueFormatter] formats selected amounts and balances; [axisValueFormatter] formats the Y axis.
 */
@Composable
fun WaterfallChart(
    data: WaterfallData,
    modifier: Modifier = Modifier,
    style: WaterfallChartStyle = WaterfallChartDefaults.style(),
    title: String? = null,
    selection: ChartSelection = rememberChartSelection(),
    interactionEnabled: Boolean = true,
    animateOnStart: Boolean = true,
    valueFormatter: ChartValueFormatter = WaterfallChartDefaults.valueFormatter,
    axisValueFormatter: ChartValueFormatter = WaterfallChartDefaults.axisValueFormatter,
) {
    WaterfallChartEntry(
        data,
        modifier,
        style,
        title,
        selection,
        interactionEnabled,
        animateOnStart,
        valueFormatter,
        axisValueFormatter,
    )
}
