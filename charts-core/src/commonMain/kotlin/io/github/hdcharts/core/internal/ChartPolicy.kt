package io.github.hdcharts.core.internal

import androidx.compose.ui.unit.Density
import io.github.hdcharts.core.internal.axis.validateAxisLabels
import io.github.hdcharts.core.model.ChartData
import io.github.hdcharts.core.style.AxisLabelStyle

/** The series-specific validation a chart runs before it draws. */
@InternalChartsApi
sealed interface SeriesRule {
    val minValues: Int
    val allowNegative: Boolean

    /** A chart whose data must contain exactly one series. Colors match its values. */
    data class Single(
        override val minValues: Int,
        override val allowNegative: Boolean,
    ) : SeriesRule

    /** A chart whose data contains aligned series, optionally stacked at each value index. */
    data class Multiple(
        override val minValues: Int,
        override val allowNegative: Boolean,
        val stacked: Boolean,
        /** Returns the expected color count, or null to disable color matching for this data. */
        val expectedColors: (ChartData) -> Int?,
    ) : SeriesRule
}

/**
 * Which input checks a chart runs before it draws, declared once by the chart and composed into one
 * error list by [ChartEntry]. A chart declares every field, so the declaration reads as the chart's
 * row in the pipeline table.
 *
 * The [series] rule selects the data-shape algorithm and carries only the options that algorithm
 * understands. Single-series charts therefore cannot accidentally declare stacking or a
 * multi-series color rule.
 *
 * @property hasAxis Whether the chart draws X and Y axes, and therefore validates its axis labels.
 * @property hasFixedRange Whether the chart's style carries optional range bounds, and therefore
 *   validates them.
 */
@InternalChartsApi
data class ChartPolicy(
    val series: SeriesRule,
    val hasAxis: Boolean,
    val hasFixedRange: Boolean,
)

/**
 * The style-derived values validation reads. A chart maps its own style onto these once, so
 * [ChartPolicy] stays a declaration of *which* checks run.
 *
 * @property colorCount Colors the style sets, checked against the series rule. Zero skips the
 *   check, so state it even when the style sets no colors.
 * @property range Range bounds the style sets, or null when it has no range. A non-null range with
 *   null bounds represents an explicitly configured automatic bound and is distinct from no range.
 * @property axes The two axis label styles, or null when the style has no Cartesian axes. A present
 *   value always supplies both styles.
 *
 * No field has a default. A skipped check looks exactly like a forgotten one, so every mapper states
 * every value and the omission is a compile error.
 */
@InternalChartsApi
data class ChartValidationInputs(
    val colorCount: Int,
    val range: RangeInputs?,
    val axes: AxisInputs?,
)

/** Explicit range inputs; null bounds are automatic values, not absent validation inputs. */
@InternalChartsApi
data class RangeInputs(
    val min: Double?,
    val max: Double?,
)

/** Both axis label inputs are required whenever a chart declares Cartesian axes. */
@InternalChartsApi
data class AxisInputs(
    val xLabels: AxisLabelStyle,
    val yLabels: AxisLabelStyle,
)

/** The errors of every check this policy enables, in the order the checks read. */
@InternalChartsApi
fun ChartPolicy.errorsFor(
    data: ChartData,
    inputs: ChartValidationInputs,
    density: Density,
): List<String> {
    val errors =
        when (val rule = series) {
            is SeriesRule.Single ->
                validateSingleSeries(
                    data = data,
                    minValues = rule.minValues,
                    allowNegative = rule.allowNegative,
                    colorCount = inputs.colorCount,
                )
            is SeriesRule.Multiple ->
                validateSeries(
                    data = data,
                    minValues = rule.minValues,
                    allowNegative = rule.allowNegative,
                    stacksValues = rule.stacked,
                    colorCount = inputs.colorCount,
                    expectedColors = rule.expectedColors(data),
                )
        }.toMutableList()
    if (hasFixedRange) {
        val range = inputs.range
        errors +=
            if (range == null) {
                listOf(ValidationErrors.missingRange())
            } else {
                validateRange(min = range.min, max = range.max)
            }
    }
    if (hasAxis) {
        val axes = inputs.axes
        errors +=
            if (axes == null) {
                listOf(ValidationErrors.missingAxisLabels())
            } else {
                validateAxisLabels(xLabels = axes.xLabels, yLabels = axes.yLabels, density = density)
            }
    }
    return errors
}
