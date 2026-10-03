package io.github.hdcharts.core.internal

import androidx.compose.ui.unit.Density
import io.github.hdcharts.core.internal.axis.validateAxisLabels
import io.github.hdcharts.core.model.ChartData
import io.github.hdcharts.core.style.AxisLabelStyle

/**
 * Which input checks a chart runs before it draws, declared once by the chart and composed into one
 * error list by [ChartEntry]. A chart declares every field, so the declaration reads as the chart's
 * row in the pipeline table.
 *
 * @property minValues Fewest values a point needs. Passed to whichever series check runs.
 * @property allowNegative Whether a negative value is an error.
 * @property singleSeries Whether the chart needs exactly one series. Checked by
 *   [validateSingleSeries], which also matches the color count against the value count, so this
 *   policy's [colorsMatch] is not read.
 * @property hasAxis Whether the chart draws X and Y axes, and therefore validates its axis labels.
 * @property hasFixedRange Whether the chart's style carries optional range bounds, and therefore
 *   validates them.
 * @property colorsMatch How many colors the style must set, read from the data, or null to skip the
 *   check. Most charts match the series count. Radar matches it only when the data holds more than
 *   one series, and says so here rather than by reporting that it has no colors. Read once before
 *   the data-shape checks, so it must tolerate empty and misaligned data.
 */
@InternalChartsApi
data class ChartPolicy(
    val minValues: Int,
    val allowNegative: Boolean,
    val singleSeries: Boolean,
    val hasAxis: Boolean,
    val hasFixedRange: Boolean,
    val colorsMatch: (ChartData) -> Int?,
)

/**
 * The style-derived values validation reads. A chart maps its own style onto these once, so
 * [ChartPolicy] stays a declaration of *which* checks run.
 *
 * @property colorCount Colors the style sets, checked against the count
 *   [ChartPolicy.colorsMatch] returns, or against the value count for a [ChartPolicy.singleSeries]
 *   chart. Zero skips the check, so state it even when the style sets no colors.
 * @property rangeMin Range minimum the style sets, or null when it sets none. Validated only when
 *   [ChartPolicy.hasFixedRange] is set.
 * @property rangeMax Range maximum the style sets, or null when it sets none. Validated only when
 *   [ChartPolicy.hasFixedRange] is set.
 * @property xLabels X-axis label style. Required when [ChartPolicy.hasAxis] is set; a missing pair is
 *   reported as an error.
 * @property yLabels Y-axis label style. Required when [ChartPolicy.hasAxis] is set.
 *
 * No field has a default. A skipped check looks exactly like a forgotten one, so every mapper states
 * every value and the omission is a compile error.
 */
@InternalChartsApi
data class ChartValidationInputs(
    val colorCount: Int,
    val rangeMin: Double?,
    val rangeMax: Double?,
    val xLabels: AxisLabelStyle?,
    val yLabels: AxisLabelStyle?,
)

/** The errors of every check this policy enables, in the order the checks read. */
@InternalChartsApi
fun ChartPolicy.errorsFor(
    data: ChartData,
    inputs: ChartValidationInputs,
    density: Density,
): List<String> {
    val errors =
        if (singleSeries) {
            validateSingleSeries(
                data = data,
                minValues = minValues,
                allowNegative = allowNegative,
                colorCount = inputs.colorCount,
            )
        } else {
            validateSeries(
                data = data,
                minValues = minValues,
                allowNegative = allowNegative,
                colorCount = inputs.colorCount,
                expectedColors = colorsMatch(data),
            )
        }.toMutableList()
    if (hasFixedRange) {
        errors += validateRange(min = inputs.rangeMin, max = inputs.rangeMax)
    }
    val xLabels = inputs.xLabels
    val yLabels = inputs.yLabels
    if (hasAxis) {
        errors +=
            if (xLabels == null || yLabels == null) {
                listOf(ValidationErrors.missingAxisLabels())
            } else {
                validateAxisLabels(xLabels = xLabels, yLabels = yLabels, density = density)
            }
    }
    return errors
}
