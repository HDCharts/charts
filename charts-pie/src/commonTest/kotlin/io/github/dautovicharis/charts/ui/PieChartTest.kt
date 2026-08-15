package io.github.dautovicharis.charts.ui

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.unit.IntSize
import io.github.dautovicharis.charts.PIE_SELECTION_AUTO_DESELECT_TIMEOUT_MS
import io.github.dautovicharis.charts.PieChart
import io.github.dautovicharis.charts.internal.TestTags
import io.github.dautovicharis.charts.internal.ValidationErrors.MIN_REQUIRED_PIE
import io.github.dautovicharis.charts.internal.ValidationErrors.RULE_DATA_POINTS_LESS_THAN_MIN
import io.github.dautovicharis.charts.internal.piechart.calculatePercentages
import io.github.dautovicharis.charts.mock.MockTest.TITLE
import io.github.dautovicharis.charts.mock.MockTest.colors
import io.github.dautovicharis.charts.model.PieSlice
import io.github.dautovicharis.charts.model.staticChartSelection
import io.github.dautovicharis.charts.style.PieChartDefaults
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.round
import kotlin.math.sin
import kotlin.test.Test

class PieChartTest {
    private val pieSlices =
        listOf(
            PieSlice(label = "A", value = 10f),
            PieSlice(label = "B", value = 20f),
            PieSlice(label = "C", value = 30f),
            PieSlice(label = "D", value = 40f),
        )
    private val points: List<Double> = pieSlices.map { it.value.toDouble() }
    private val labels: List<String> = pieSlices.map { it.label }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun pieChart_withValidData_displaysChart() =
        runComposeUiTest {
            setContent {
                PieChart(pieSlices, title = TITLE)
            }

            onNodeWithTag(TestTags.PIE_CHART).isDisplayed()
            onNodeWithTag(TestTags.CHART_TITLE).assertTextEquals(TITLE)
        }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun pieChart_withValidData_displayAndInteractWithChart() =
        runComposeUiTest {
            val slices = createPieSlices(points)
            val percentages = calculatePercentages(points)

            setContent {
                PieChart(pieSlices, title = TITLE)
            }

            onNodeWithTag(TestTags.PIE_CHART).isDisplayed()
            onNodeWithTag(TestTags.CHART_TITLE).assertTextEquals(TITLE)
            val size = onNodeWithTag(TestTags.PIE_CHART).fetchSemanticsNode().size

            labels.forEachIndexed { index, value ->
                val sliceMiddlePosition =
                    getCoordinatesForSlice(index = index, size = size, slices = slices)
                onNodeWithTag(TestTags.PIE_CHART).performTouchInput {
                    down(sliceMiddlePosition)
                    up()
                }
                onNodeWithTag(TestTags.CHART_TITLE).assertTextEquals(value).isDisplayed()
                onNodeWithText("${percentages[index]}%").isDisplayed()
            }
        }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun pieChart_withInvalidData_displaysError() =
        runComposeUiTest {
            val invalidSlices = listOf(PieSlice(label = "A", value = 1f))
            val expectedError = RULE_DATA_POINTS_LESS_THAN_MIN.format(MIN_REQUIRED_PIE)

            setContent {
                PieChart(invalidSlices)
            }

            onNodeWithTag(TestTags.PIE_CHART).assertDoesNotExist()
            onNodeWithTag(TestTags.CHART_ERROR).isDisplayed()
            onNodeWithText("${expectedError}\n").isDisplayed()
        }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun pieChart_withSliceColors_rendersChart() =
        runComposeUiTest {
            val slices =
                pieSlices.mapIndexed { index, slice ->
                    slice.copy(color = colors[index % colors.size])
                }

            setContent {
                PieChart(data = slices)
            }

            onNodeWithTag(TestTags.PIE_CHART).isDisplayed()
            onNodeWithTag(TestTags.CHART_ERROR).assertDoesNotExist()
        }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun pieChart_withDonutStyle_displaysCorrectly() =
        runComposeUiTest {
            val slices = createPieSlices(points)
            val percentages = calculatePercentages(points)

            setContent {
                PieChart(
                    pieSlices,
                    style = PieChartDefaults.style(donut = PieChartDefaults.donut(holePercentage = 0.5f)),
                    title = TITLE,
                )
            }

            onNodeWithTag(TestTags.PIE_CHART).isDisplayed()
            onNodeWithTag(TestTags.CHART_TITLE).assertTextEquals(TITLE)
            val size = onNodeWithTag(TestTags.PIE_CHART).fetchSemanticsNode().size

            labels.forEachIndexed { index, value ->
                val sliceMiddlePosition =
                    getCoordinatesForSlice(index = index, size = size, slices = slices)
                onNodeWithTag(TestTags.PIE_CHART).performTouchInput {
                    down(sliceMiddlePosition)
                    up()
                }
                onNodeWithTag(TestTags.CHART_TITLE).assertTextEquals(value).isDisplayed()
                onNodeWithText("${percentages[index]}%").isDisplayed()
            }
        }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun pieChart_withSelectedSliceIndex_displaysSelectedSliceDetails() =
        runComposeUiTest {
            val selectedSliceIndex = 1
            val expectedTitle = labels[selectedSliceIndex]
            val expectedPercentage =
                "${calculatePercentages(points)[selectedSliceIndex]}%"

            setContent {
                PieChart(
                    pieSlices,
                    style = PieChartDefaults.style(selection = staticChartSelection(selectedSliceIndex)),
                    title = TITLE,
                )
            }

            onNodeWithTag(TestTags.PIE_CHART).isDisplayed()
            onNodeWithTag(TestTags.CHART_TITLE).assertTextEquals(expectedTitle)
            onNodeWithText(expectedPercentage).isDisplayed()
        }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun pieChart_withTapSelection_autoDeselectsAfterTimeout() =
        runComposeUiTest {
            val slices = createPieSlices(points)

            setContent {
                PieChart(pieSlices, title = TITLE)
            }

            val selectedLabel = labels[0]
            val size = onNodeWithTag(TestTags.PIE_CHART).fetchSemanticsNode().size
            val sliceMiddlePosition =
                getCoordinatesForSlice(index = 0, size = size, slices = slices)
            onNodeWithTag(TestTags.PIE_CHART).performTouchInput {
                down(sliceMiddlePosition)
                up()
            }
            onNodeWithTag(TestTags.CHART_TITLE).assertTextEquals(selectedLabel)

            waitUntil(timeoutMillis = PIE_SELECTION_AUTO_DESELECT_TIMEOUT_MS + 2_000L) {
                runCatching {
                    onNodeWithTag(TestTags.CHART_TITLE).assertTextEquals(TITLE)
                }.isSuccess
            }
            onNodeWithTag(TestTags.CHART_TITLE).assertTextEquals(TITLE)
        }

    private data class SliceGeometry(
        val startDeg: Float,
        val sweepAngle: Float,
    )

    private fun createPieSlices(values: List<Double>): List<SliceGeometry> {
        val total = values.sum()
        var lastEndDeg = 0.0
        return values.map { slice ->
            val normalized = if (total == 0.0) 0.0 else slice / total
            val startDeg = lastEndDeg
            val endDeg = lastEndDeg + (normalized * 360)
            lastEndDeg = endDeg
            SliceGeometry(
                startDeg = startDeg.toFloat(),
                sweepAngle = (endDeg - startDeg).toFloat(),
            )
        }
    }

    private fun getCoordinatesForSlice(
        index: Int,
        size: IntSize,
        slices: List<SliceGeometry>,
    ): androidx.compose.ui.geometry.Offset {
        val slice = slices[index]
        val centerX = size.width / 2f
        val centerY = size.height / 2f
        val radius = minOf(size.width, size.height) / 2f
        val midAngle = slice.startDeg + (slice.sweepAngle / 2f)
        val radian = midAngle * (PI / 180)
        val middleRadius = radius / 2f
        val x = centerX + middleRadius * cos(radian).toFloat()
        val y = centerY + middleRadius * sin(radian).toFloat()
        return androidx.compose.ui.geometry
            .Offset(x, y)
    }

    private fun calculatePercentages(values: List<Double>): List<String> {
        val total = values.sum()
        return values.map { value ->
            val percentage = (if (total == 0.0) Double.NaN else value / total) * 100
            val rounded = round(percentage * 100) / 100
            "$rounded"
        }
    }
}
