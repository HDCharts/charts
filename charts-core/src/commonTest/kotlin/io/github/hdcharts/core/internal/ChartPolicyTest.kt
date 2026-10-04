package io.github.hdcharts.core.internal

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.sp
import io.github.hdcharts.core.model.ChartSeries
import io.github.hdcharts.core.model.chartDataOf
import io.github.hdcharts.core.style.AxisLabelStyle
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * The seam composes a policy into one error list. These tests pin which check each field turns on,
 * because a chart declares its policy once and never composes the checks itself.
 */
class ChartPolicyTest {
    @Test
    fun alignedSeriesPolicy_withValidData_returnsNoErrors() {
        val errors =
            ALIGNED_SERIES.errorsFor(
                data = chartData(ChartSeries(name = "One", values = listOf(1.0, -2.0))),
                inputs = chartInputs(),
                density = Density(1f),
            )

        assertEquals(expected = emptyList(), actual = errors)
    }

    @Test
    fun singleSeriesPolicy_withTwoSeries_reportsTheSeriesCount() {
        val data =
            chartData(
                ChartSeries(name = "One", values = listOf(1.0, 2.0)),
                ChartSeries(name = "Two", values = listOf(3.0, 4.0)),
            )

        val errors =
            SINGLE_SERIES.errorsFor(
                data = data,
                inputs = chartInputs(colorCount = 2),
                density = Density(1f),
            )

        assertEquals(expected = listOf("Exactly one series is required; got 2."), actual = errors)
    }

    @Test
    fun singleSeriesPolicy_checksColorsAgainstValueCount() {
        val data = chartData(ChartSeries(name = "One", values = listOf(1.0, 2.0)))

        val errors =
            SINGLE_SERIES.errorsFor(
                data = data,
                inputs = chartInputs(colorCount = 3),
                density = Density(1f),
            )

        assertEquals(expected = listOf("Color count (3) must match value count (2)."), actual = errors)
    }

    @Test
    fun alignedSeriesPolicy_checksColorsAgainstSeriesCount() {
        val data =
            chartData(
                ChartSeries(name = "One", values = listOf(1.0, 2.0)),
                ChartSeries(name = "Two", values = listOf(3.0, 4.0)),
            )

        val errors =
            ALIGNED_SERIES.errorsFor(
                data = data,
                inputs = chartInputs(colorCount = 3),
                density = Density(1f),
            )

        assertEquals(expected = listOf("Color count (3) must match series count (2)."), actual = errors)
    }

    @Test
    fun noColors_skipsTheColorCheck() {
        val data =
            chartData(
                ChartSeries(name = "One", values = listOf(1.0, 2.0)),
                ChartSeries(name = "Two", values = listOf(3.0, 4.0)),
            )

        val errors =
            ALIGNED_SERIES.errorsFor(
                data = data,
                inputs = chartInputs(colorCount = 0),
                density = Density(1f),
            )

        assertEquals(expected = emptyList(), actual = errors)
    }

    @Test
    fun minValues_isTheChartThreshold() {
        val data = chartData(ChartSeries(name = "One", values = listOf(1.0, 2.0)))

        val errors =
            RADAR
                .errorsFor(
                    data = data,
                    inputs = NO_STYLE_VALUES,
                    density = Density(1f),
                )

        assertEquals(expected = listOf("At least 3 values are required."), actual = errors)
    }

    @Test
    fun negativeValuesAllowedByDefault() {
        val data = chartData(ChartSeries(name = "One", values = listOf(-1.0, -2.0)))

        val errors =
            ALIGNED_SERIES.errorsFor(data = data, inputs = chartInputs(), density = Density(1f))

        assertEquals(expected = emptyList(), actual = errors)
    }

    @Test
    fun allowNegativeFalse_reportsNegativeValues() {
        val data = chartData(ChartSeries(name = "One", values = listOf(-1.0, 2.0)))

        val errors =
            STACKED
                .errorsFor(data = data, inputs = chartInputs(), density = Density(1f))

        assertEquals(expected = listOf("Series 0 contains a negative value."), actual = errors)
    }

    @Test
    fun noFixedRange_ignoresRangeBounds() {
        val data = chartData(ChartSeries(name = "One", values = listOf(1.0, 2.0)))

        val errors =
            STACKED.errorsFor(
                data = data,
                inputs = chartInputs(rangeMax = Double.POSITIVE_INFINITY),
                density = Density(1f),
            )

        assertEquals(expected = emptyList(), actual = errors)
    }

    @Test
    fun fixedRange_reportsNonFiniteBounds() {
        val data = chartData(ChartSeries(name = "One", values = listOf(1.0, 2.0)))

        val errors =
            ALIGNED_SERIES.errorsFor(
                data = data,
                inputs = chartInputs(rangeMax = Double.POSITIVE_INFINITY),
                density = Density(1f),
            )

        assertEquals(expected = listOf("Range bounds must be finite."), actual = errors)
    }

    @Test
    fun noAxis_ignoresAxisLabels() {
        val data = chartData(ChartSeries(name = "One", values = listOf(1.0, 2.0)))

        val errors =
            STACKED
                .copy(hasAxis = false)
                .errorsFor(
                    data = data,
                    inputs = chartInputs(xLabels = UNDRAWABLE_LABELS, yLabels = UNDRAWABLE_LABELS),
                    density = Density(1f),
                )

        assertEquals(expected = emptyList(), actual = errors)
    }

    @Test
    fun axis_reportsBothAxes() {
        val data = chartData(ChartSeries(name = "One", values = listOf(1.0, 2.0)))

        val errors =
            ALIGNED_SERIES.errorsFor(
                data = data,
                inputs = chartInputs(xLabels = UNDRAWABLE_LABELS, yLabels = UNDRAWABLE_LABELS),
                density = Density(1f),
            )

        assertEquals(
            expected =
                listOf(
                    "X-axis label size must be a finite, positive sp value.",
                    "Y-axis label size must be a finite, positive sp value.",
                ),
            actual = errors,
        )
    }

    @Test
    fun minValues_isTheChartThresholdOnASingleSeriesPolicyToo() {
        val data = chartData(ChartSeries(name = "One", values = listOf(1.0, 2.0)))

        val errors =
            SINGLE_SERIES
                .copy(minValues = 3)
                .errorsFor(
                    data = data,
                    inputs = chartInputs(),
                    density = Density(1f),
                )

        assertEquals(expected = listOf("At least 3 values are required."), actual = errors)
    }

    /** A declared axis with no label styles would validate nothing, so the seam reports it as an error. */
    @Test
    fun hasAxis_withoutLabelStyles_reportsTheMissingPair() {
        val data = chartData(ChartSeries(name = "One", values = listOf(1.0, 2.0)))

        val errors =
            ALIGNED_SERIES.errorsFor(
                data = data,
                inputs = NO_STYLE_VALUES,
                density = Density(1f),
            )

        assertEquals(expected = listOf("Axis label styles are missing."), actual = errors)
    }

    @Test
    fun hasAxis_withoutOneLabelStyle_reportsTheMissingPair() {
        val data = chartData(ChartSeries(name = "One", values = listOf(1.0, 2.0)))

        val errors =
            ALIGNED_SERIES.errorsFor(
                data = data,
                inputs = chartInputs(yLabels = null),
                density = Density(1f),
            )

        assertEquals(expected = listOf("Axis label styles are missing."), actual = errors)
    }

    /**
     * A chart whose color count depends on the data — radar matches the series count only when there
     * is more than one series — declares the rule rather than reporting that it has no colors.
     */
    @Test
    fun colorsMatch_returningNull_skipsTheColorCheck() {
        val data =
            chartData(
                ChartSeries(name = "One", values = listOf(1.0, 2.0)),
                ChartSeries(name = "Two", values = listOf(3.0, 4.0)),
            )

        val errors =
            ALIGNED_SERIES
                .copy(colorsMatch = { null })
                .errorsFor(
                    data = data,
                    inputs = chartInputs(colorCount = 5),
                    density = Density(1f),
                )

        assertEquals(expected = emptyList(), actual = errors)
    }

    @Test
    fun colorsMatch_dependingOnTheSeriesCount_isTheRadarRule() {
        val radar =
            ALIGNED_SERIES.copy(
                colorsMatch = { data -> data.series.size.takeIf { count -> count > 1 } },
            )
        val oneSeries = chartData(ChartSeries(name = "One", values = listOf(1.0, 2.0)))
        val twoSeries =
            chartData(
                ChartSeries(name = "One", values = listOf(1.0, 2.0)),
                ChartSeries(name = "Two", values = listOf(3.0, 4.0)),
            )

        assertEquals(
            expected = emptyList(),
            actual = radar.errorsFor(data = oneSeries, inputs = chartInputs(colorCount = 5), density = Density(1f)),
        )
        assertEquals(
            expected = listOf("Color count (5) must match series count (2)."),
            actual = radar.errorsFor(data = twoSeries, inputs = chartInputs(colorCount = 5), density = Density(1f)),
        )
    }

    /** A single-series chart matches its value count, so its policy's color rule is never read. */
    @Test
    fun singleSeriesPolicy_doesNotReadColorsMatch() {
        val data = chartData(ChartSeries(name = "One", values = listOf(1.0, 2.0)))

        val errors =
            SINGLE_SERIES
                .copy(colorsMatch = { error("colorsMatch must not be read when singleSeries is set") })
                .errorsFor(
                    data = data,
                    inputs = chartInputs(colorCount = 2),
                    density = Density(1f),
                )

        assertEquals(expected = emptyList(), actual = errors)
    }

    private companion object {
        fun chartData(vararg series: ChartSeries) = chartDataOf(series = series)

        /** A chart that takes any number of aligned series, with axes and an optional fixed range. */
        val ALIGNED_SERIES =
            ChartPolicy(
                minValues = ValidationErrors.MIN_VALUES,
                allowNegative = true,
                singleSeries = false,
                hasAxis = true,
                hasFixedRange = true,
                colorsMatch = { data -> data.series.size },
            )

        /** A chart that needs exactly one series, with axes and an optional fixed range. */
        val SINGLE_SERIES =
            ChartPolicy(
                minValues = ValidationErrors.MIN_VALUES,
                allowNegative = true,
                singleSeries = true,
                hasAxis = true,
                hasFixedRange = true,
                colorsMatch = { data -> data.series.size },
            )

        /** A chart that stacks nonnegative series, with axes and no fixed range. */
        val STACKED =
            ChartPolicy(
                minValues = ValidationErrors.MIN_VALUES,
                allowNegative = false,
                singleSeries = false,
                hasAxis = true,
                hasFixedRange = false,
                colorsMatch = { data -> data.series.size },
            )

        /** A chart that draws polygons around a shared axis, with no Cartesian axis and no fixed range. */
        val RADAR =
            ChartPolicy(
                minValues = ValidationErrors.MIN_RADAR_VALUES,
                allowNegative = true,
                singleSeries = false,
                hasAxis = false,
                hasFixedRange = false,
                colorsMatch = { data -> data.series.size },
            )

        val DRAWABLE_LABELS = AxisLabelStyle(visible = true, color = Color.Unspecified, size = 12.sp, maxCount = null)
        val UNDRAWABLE_LABELS = DRAWABLE_LABELS.copy(size = 0.sp)

        fun chartInputs(
            colorCount: Int = 0,
            rangeMin: Double? = null,
            rangeMax: Double? = null,
            xLabels: AxisLabelStyle? = DRAWABLE_LABELS,
            yLabels: AxisLabelStyle? = DRAWABLE_LABELS,
        ) = ChartValidationInputs(
            colorCount = colorCount,
            rangeMin = rangeMin,
            rangeMax = rangeMax,
            xLabels = xLabels,
            yLabels = yLabels,
        )

        /** A style that sets no colors, no range and no labels, stated in full. */
        val NO_STYLE_VALUES =
            ChartValidationInputs(
                colorCount = 0,
                rangeMin = null,
                rangeMax = null,
                xLabels = null,
                yLabels = null,
            )
    }
}
