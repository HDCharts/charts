package io.github.hdcharts.line.internal

import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.unit.Density
import io.github.hdcharts.core.model.ChartData
import io.github.hdcharts.core.model.ChartSeries
import io.github.hdcharts.core.model.toChartData
import io.github.hdcharts.line.LineChartDefaults
import io.github.hdcharts.line.LineTestFixtures.colors
import io.github.hdcharts.line.LineTestFixtures.multiDataSet
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class LineChartEntryTest {
    @Test
    fun validMultiSeries_noErrors() =
        runComposeUiTest {
            assertEquals(expected = emptyList(), actual = validate(data = multiDataSet, lineColors = colors))
        }

    @Test
    fun categoryCountMismatch_reportsCategoryError() =
        runComposeUiTest {
            val data = ChartData(categories = multiDataSet.categories.drop(1), series = multiDataSet.series)

            assertEquals(
                expected = listOf("Category count (3) must match value count (4)."),
                actual = validate(data = data),
            )
        }

    @Test
    fun lineColorCountMismatch_reportsColorError() =
        runComposeUiTest {
            assertEquals(
                expected = listOf("Color count (2) must match series count (4)."),
                actual = validate(data = multiDataSet, lineColors = colors.drop(2)),
            )
        }

    @Test
    fun seriesColorsMatchSeriesCount_noErrors() =
        runComposeUiTest {
            val data =
                ChartData(
                    categories = listOf("A", "B", "C", "D", "E"),
                    series =
                        listOf(
                            ChartSeries(name = "First", values = listOf(1.0, 2.0, 3.0, 4.0, 5.0)),
                            ChartSeries(name = "Second", values = listOf(2.0, 3.0, 4.0, 5.0, 6.0)),
                            ChartSeries(name = "Third", values = listOf(3.0, 4.0, 5.0, 6.0, 7.0)),
                        ),
                )

            assertEquals(expected = emptyList(), actual = validate(data = data, lineColors = colors.take(3)))
        }

    @Test
    fun misalignedSeries_reportsAlignmentError() =
        runComposeUiTest {
            val data =
                ChartData(
                    series =
                        listOf(
                            ChartSeries(name = "First", values = listOf(1.0, 2.0, 3.0)),
                            ChartSeries(name = "Second", values = listOf(1.0)),
                        ),
                )

            assertEquals(
                expected = listOf("Series 1 is not aligned with the first series."),
                actual = validate(data = data),
            )
        }

    @Test
    fun categoriesMatchFirstSeriesOnly_reportsOnlyAlignmentErrors() =
        runComposeUiTest {
            val data =
                ChartData(
                    categories = listOf("A", "B", "C"),
                    series =
                        listOf(
                            ChartSeries(name = "First", values = listOf(1.0, 2.0, 3.0)),
                            ChartSeries(name = "Second", values = listOf(1.0)),
                            ChartSeries(name = "Third", values = listOf(1.0, 2.0)),
                        ),
                )

            assertEquals(
                expected =
                    listOf(
                        "Series 1 is not aligned with the first series.",
                        "Series 2 is not aligned with the first series.",
                    ),
                actual = validate(data = data),
            )
        }

    @Test
    fun emptySeries_reportsTooFewValues() =
        runComposeUiTest {
            val data =
                ChartData(
                    series =
                        listOf(
                            ChartSeries(name = "First", values = emptyList()),
                            ChartSeries(name = "Second", values = emptyList()),
                        ),
                )

            assertEquals(expected = listOf("At least 2 values are required."), actual = validate(data = data))
        }

    @Test
    fun explicitlyParsedInvalidValue_reportsNonFiniteError() =
        runComposeUiTest {
            val data = listOf("2.0", "invalid").map { it.toDoubleOrNull() ?: Double.NaN }.toChartData()

            assertEquals(expected = listOf("Series 0 contains a non-finite value."), actual = validate(data = data))
        }

    @Test
    fun axisLabelMaxCountsBelowTwo_reportEachAxis() =
        runComposeUiTest {
            assertEquals(
                expected =
                    listOf(
                        "X-axis label max count must be in 2..1000.",
                        "Y-axis label max count must be in 2..1000.",
                    ),
                actual = validate(data = multiDataSet, lineColors = colors, xLabelMaxCount = 1, yLabelMaxCount = 0),
            )
        }

    @Test
    fun axisLabelMaxCountsAtLimit_noErrors() =
        runComposeUiTest {
            assertEquals(
                expected = emptyList(),
                actual = validate(data = multiDataSet, lineColors = colors, xLabelMaxCount = 2, yLabelMaxCount = 2),
            )
        }

    private fun ComposeUiTest.validate(
        data: ChartData,
        lineColors: List<Color> = emptyList(),
        xLabelMaxCount: Int? = null,
        yLabelMaxCount: Int? = null,
    ): List<String> {
        var errors: List<String>? = null
        setContent {
            val style =
                LineChartDefaults.style(
                    line = LineChartDefaults.line(colors = lineColors),
                    axis =
                        LineChartDefaults.axis(
                            xLabels = LineChartDefaults.xLabels(maxCount = xLabelMaxCount),
                            yLabels = LineChartDefaults.yLabels(maxCount = yLabelMaxCount),
                        ),
                )
            SideEffect { errors = validateLineInput(data = data, style = style, density = Density(1f)) }
        }
        return runOnIdle { checkNotNull(errors) }
    }
}
