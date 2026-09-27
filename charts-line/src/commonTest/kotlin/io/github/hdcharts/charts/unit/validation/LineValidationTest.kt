package io.github.hdcharts.charts.unit.validation

import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.unit.Density
import io.github.hdcharts.charts.internal.linechart.validateLineInput
import io.github.hdcharts.charts.mock.MockTest.colors
import io.github.hdcharts.charts.mock.MockTest.multiDataSet
import io.github.hdcharts.charts.model.ChartData
import io.github.hdcharts.charts.model.ChartSeries
import io.github.hdcharts.charts.model.toChartData
import io.github.hdcharts.charts.style.LineChartDefaults
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class LineValidationTest {
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
                expected = listOf("Line color count must match series count (4)."),
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
    fun categoriesMatchFirstSeriesOnly_reportsCategoryErrorOnce() =
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
                        "Category count (3) must match every series value count.",
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

            assertEquals(expected = listOf("At least two line values are required."), actual = validate(data = data))
        }

    @Test
    fun explicitlyParsedInvalidValue_reportsNonFiniteError() =
        runComposeUiTest {
            val data = listOf("2.0", "invalid").map { it.toDoubleOrNull() ?: Double.NaN }.toChartData()

            assertEquals(expected = listOf("Series 0 contains a non-finite value."), actual = validate(data = data))
        }

    private fun ComposeUiTest.validate(
        data: ChartData,
        lineColors: List<Color> = emptyList(),
    ): List<String> {
        var errors: List<String>? = null
        setContent {
            val style = LineChartDefaults.style(line = LineChartDefaults.line(colors = lineColors))
            SideEffect { errors = validateLineInput(data = data, style = style, density = Density(1f)) }
        }
        return runOnIdle { checkNotNull(errors) }
    }
}
