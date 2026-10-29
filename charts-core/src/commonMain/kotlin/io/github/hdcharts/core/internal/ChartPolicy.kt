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
 * Which input checks a chart runs before it draws, and where each check gets its style values.
 * [ChartEntry] composes the results into one error list.
 *
 * The [series] rule selects the data-shape algorithm and carries only the options that algorithm
 * understands. Single-series charts therefore cannot accidentally declare stacking or a
 * multi-series color rule.
 *
 * A non-null mapper declares and supplies the input for its check. A null [range] or [axes] mapper
 * means that capability does not apply. A range mapper that returns [RangeInputs] with null bounds
 * declares automatic range bounds; it is different from having no range mapper.
 *
 * @param S The chart's style type.
 */
@InternalChartsApi
data class ChartPolicy<S : Any>(
    val series: SeriesRule,
    val colorCount: (S) -> Int,
    val range: ((S) -> RangeInputs)?,
    val axes: ((S) -> AxisInputs)?,
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

/** The value-based style inputs that validation reads for one policy evaluation. */
@InternalChartsApi
data class ValidationSnapshot(
    val colorCount: Int,
    val range: RangeInputs?,
    val axes: AxisInputs?,
)

/** Extracts the style values that can affect validation. */
@InternalChartsApi
fun <S : Any> ChartPolicy<S>.snapshot(style: S): ValidationSnapshot =
    ValidationSnapshot(
        colorCount = colorCount(style),
        range = range?.invoke(style),
        axes = axes?.invoke(style),
    )

/** The errors of every check this policy enables, in the order the checks read. */
@InternalChartsApi
fun <S : Any> ChartPolicy<S>.errorsFor(
    data: ChartData,
    inputs: ValidationSnapshot,
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
    inputs.range?.let { range ->
        errors += validateRange(min = range.min, max = range.max)
    }
    inputs.axes?.let { axes ->
        errors += validateAxisLabels(xLabels = axes.xLabels, yLabels = axes.yLabels, density = density)
    }
    return errors
}

/** Evaluates validation directly from a style for non-composable callers. */
@InternalChartsApi
fun <S : Any> ChartPolicy<S>.errorsFor(
    data: ChartData,
    style: S,
    density: Density,
): List<String> = errorsFor(data = data, inputs = snapshot(style), density = density)
