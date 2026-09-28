package io.github.hdcharts.charts.unit.validation

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import io.github.hdcharts.charts.internal.common.axis.validateAxisLabels
import io.github.hdcharts.charts.style.AxisLabelStyle
import kotlin.test.Test
import kotlin.test.assertEquals

class AxisLabelValidationTest {
    private val density = Density(density = 2.625f, fontScale = 1f)

    @Test
    fun nullAndTwoToThousandMaxCounts_areValid() {
        listOf(null, 2, 6, 1000).forEach { maxCount ->
            assertEquals(
                expected = emptyList(),
                actual = validateAxisLabels(labels(maxCount = maxCount), labels(maxCount = maxCount), density),
                message = "maxCount = $maxCount",
            )
        }
    }

    @Test
    fun maxCountsOutsideTwoToThousand_reportTheirAxis() {
        listOf(Int.MIN_VALUE, -1, 0, 1, 1001, Int.MAX_VALUE).forEach { maxCount ->
            assertEquals(
                expected = listOf("X-axis label max count must be in 2..1000."),
                actual =
                    validateAxisLabels(
                        xLabels = labels(maxCount = maxCount),
                        yLabels = labels(),
                        density = density,
                    ),
                message = "x maxCount = $maxCount",
            )
            assertEquals(
                expected = listOf("Y-axis label max count must be in 2..1000."),
                actual =
                    validateAxisLabels(
                        xLabels = labels(),
                        yLabels = labels(maxCount = maxCount),
                        density = density,
                    ),
                message = "y maxCount = $maxCount",
            )
        }
    }

    @Test
    fun bothAxesBelowTwo_reportsEachAxisOnce() {
        assertEquals(
            expected =
                listOf(
                    "X-axis label max count must be in 2..1000.",
                    "Y-axis label max count must be in 2..1000.",
                ),
            actual =
                validateAxisLabels(
                    xLabels = labels(maxCount = 1),
                    yLabels = labels(maxCount = 0),
                    density = density,
                ),
        )
    }

    @Test
    fun sizeThatIsNotFinitePositiveSp_reportsItsAxis() {
        listOf(1.em, TextUnit.Unspecified, 0.sp, (-4).sp, Float.NaN.sp, Float.POSITIVE_INFINITY.sp).forEach { size ->
            assertEquals(
                expected = listOf("X-axis label size must be a finite, positive sp value."),
                actual = validateAxisLabels(xLabels = labels(size = size), yLabels = labels(), density = density),
                message = "x size = $size",
            )
            assertEquals(
                expected = listOf("Y-axis label size must be a finite, positive sp value."),
                actual = validateAxisLabels(xLabels = labels(), yLabels = labels(size = size), density = density),
                message = "y size = $size",
            )
        }
    }

    @Test
    fun sizeAboveTheLargestStyleSize_reportsItsAxis() {
        assertEquals(
            expected = listOf("X-axis label size must resolve to at most 16384 pixels."),
            actual = validateAxisLabels(xLabels = labels(size = 7_000.sp), yLabels = labels(), density = density),
        )
    }

    @Test
    fun sizeAndCountErrors_areReportedPerAxis() {
        assertEquals(
            expected =
                listOf(
                    "X-axis label size must be a finite, positive sp value.",
                    "X-axis label max count must be in 2..1000.",
                    "Y-axis label max count must be in 2..1000.",
                ),
            actual =
                validateAxisLabels(
                    xLabels = labels(size = 1.em, maxCount = 1),
                    yLabels = labels(maxCount = 0),
                    density = density,
                ),
        )
    }

    private fun labels(
        size: TextUnit = 11.sp,
        maxCount: Int? = null,
    ) = AxisLabelStyle(visible = true, color = Color.Black, size = size, maxCount = maxCount)
}
