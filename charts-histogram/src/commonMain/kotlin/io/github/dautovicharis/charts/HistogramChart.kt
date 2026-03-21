package io.github.dautovicharis.charts

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import io.github.dautovicharis.charts.internal.NO_SELECTION
import io.github.dautovicharis.charts.internal.TestTags
import io.github.dautovicharis.charts.internal.common.composable.ChartErrors
import io.github.dautovicharis.charts.internal.validateHistogramData
import io.github.dautovicharis.charts.model.ChartDataSet
import io.github.dautovicharis.charts.style.HistogramChartDefaults
import io.github.dautovicharis.charts.style.HistogramChartStyle
import kotlinx.collections.immutable.toImmutableList

/**
 * A composable function that displays a Histogram Chart.
 *
 * Expects pre-binned data where each point is a bin count.
 */
@Composable
fun HistogramChart(
    dataSet: ChartDataSet,
    style: HistogramChartStyle = HistogramChartDefaults.style(),
    interactionEnabled: Boolean = true,
    animateOnStart: Boolean = true,
    selectedBarIndex: Int = NO_SELECTION,
) {
    val errors =
        remember(dataSet, style.barColors) {
            validateHistogramData(
                data = dataSet.data.item,
                colorsSize = style.barColors.size,
            )
        }

    if (errors.isEmpty()) {
        Box(modifier = Modifier.testTag(TestTags.HISTOGRAM_CHART)) {
            BarChart(
                dataSet = dataSet,
                style = style,
                interactionEnabled = interactionEnabled,
                animateOnStart = animateOnStart,
                selectedBarIndex = selectedBarIndex,
            )
        }
    } else {
        ChartErrors(style = style.chartViewStyle, errors = errors.toImmutableList())
    }
}
