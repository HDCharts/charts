package io.github.hdcharts.line

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.hdcharts.core.model.ChartData
import io.github.hdcharts.core.model.ChartValueFormatter
import io.github.hdcharts.line.internal.LineChartEntry
import io.github.hdcharts.line.internal.LiveLineChartImpl
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

/**
 * Displays a live window of one or more aligned indexed series. When [data] drops its oldest point
 * and appends a new one, the line slides one step to the left, X-axis labels stay on their points
 * and slide with them, and the Y axis rescales to the current window. Other changes that keep the
 * number of points and series morph the line over [shiftDuration]; changes to either count redraw
 * immediately.
 *
 * The chart is display-only: it has no selection and ignores gestures.
 *
 * - [shiftDuration] is how long each new point takes to slide into place. Match it to how often
 *   [data] changes so each shift finishes as the next point arrives.
 */
@Composable
fun LiveLineChart(
    data: ChartData,
    modifier: Modifier = Modifier,
    style: LineChartStyle = LineChartDefaults.style(),
    title: String? = null,
    shiftDuration: Duration = 420.milliseconds,
    animateOnStart: Boolean = true,
    axisValueFormatter: ChartValueFormatter = LineChartDefaults.axisValueFormatter,
) {
    LineChartEntry(
        data = data,
        modifier = modifier,
        style = style,
        title = title,
    ) { internalData, drawStyle ->
        LiveLineChartImpl(
            data = internalData,
            modifier = modifier,
            style = drawStyle,
            shiftDuration = shiftDuration,
            animateOnStart = animateOnStart,
            axisValueFormatter = axisValueFormatter,
        )
    }
}
