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
import io.github.dautovicharis.charts.internal.ValidationErrors.RULE_COLORS_SIZE_MISMATCH
import io.github.dautovicharis.charts.internal.ValidationErrors.RULE_DATA_POINTS_LESS_THAN_MIN
import io.github.dautovicharis.charts.internal.common.model.ChartDataType
import io.github.dautovicharis.charts.internal.format
import io.github.dautovicharis.charts.mock.MockTest.TITLE
import io.github.dautovicharis.charts.mock.MockTest.colors
import io.github.dautovicharis.charts.mock.MockTest.dataSet
import io.github.dautovicharis.charts.mock.MockTest.mockPieChartStyle
import io.github.dautovicharis.charts.model.ChartDataSet
import io.github.dautovicharis.charts.style.PieChartDefaults
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.round
import kotlin.math.sin
import kotlin.test.Test

class PieChartTest {
    @OptIn(ExperimentalTestApi::class)
    @Test
    fun pieChart_withValidData_displaysChart() =
        runComposeUiTest {
            // Arrange
            val expectedTitle = dataSet.data.label

            // Act
            setContent {
                PieChart(dataSet, PieChartDefaults.style())
            }

            // Assert
            onNodeWithTag(TestTags.PIE_CHART).isDisplayed()
            onNodeWithTag(TestTags.CHART_TITLE).assertTextEquals(expectedTitle)
        }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun pieChart_withValidData_displayAndInteractWithChart() =
        runComposeUiTest {
            // Arrange
            val slices = createPieSlices(dataSet.data.item.points)
            val percentages = calculatePercentages(dataSet.data.item.points)
            val expectedTitle = dataSet.data.label

            // Act
            setContent {
                PieChart(dataSet, PieChartDefaults.style())
            }

            // Assert
            onNodeWithTag(TestTags.PIE_CHART).isDisplayed()
            onNodeWithTag(TestTags.CHART_TITLE).assertTextEquals(expectedTitle)
            val size = onNodeWithTag(TestTags.PIE_CHART).fetchSemanticsNode().size

            dataSet.data.item.labels.forEachIndexed { index, value ->
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
            // Arrange
            val dataSet =
                ChartDataSet(
                    items = ChartDataType.FloatData(listOf(1f)),
                    title = TITLE,
                )
            val expectedError = RULE_DATA_POINTS_LESS_THAN_MIN.format(MIN_REQUIRED_PIE)

            // Act
            setContent {
                PieChart(dataSet, PieChartDefaults.style())
            }

            // Assert
            onNodeWithTag(TestTags.PIE_CHART).assertDoesNotExist()
            onNodeWithTag(TestTags.CHART_ERROR).isDisplayed()
            onNodeWithText("${expectedError}\n").isDisplayed()
        }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun pieChart_withInvalidColors_displaysError() =
        runComposeUiTest {
            // Arrange
            val colors = colors.drop(2)
            val pieChartStyle = mockPieChartStyle(colors)
            val expectedColorsSize = dataSet.data.item.points.size
            val colorsSize = colors.size
            val expectedError = RULE_COLORS_SIZE_MISMATCH.format(colorsSize, expectedColorsSize)

            // Act
            setContent {
                PieChart(
                    dataSet,
                    pieChartStyle,
                )
            }

            // Assert
            onNodeWithTag(TestTags.PIE_CHART).assertDoesNotExist()
            onNodeWithTag(TestTags.CHART_ERROR).isDisplayed()
            onNodeWithText("${expectedError}\n").isDisplayed()
        }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun pieChart_withDonutStyle_displaysCorrectly() =
        runComposeUiTest {
            // Arrange
            val slices = createPieSlices(dataSet.data.item.points)
            val percentages = calculatePercentages(dataSet.data.item.points)

            // Act
            setContent {
                PieChart(dataSet, PieChartDefaults.style(donutPercentage = 0.5f))
            }

            // Assert
            onNodeWithTag(TestTags.PIE_CHART).isDisplayed()
            onNodeWithTag(TestTags.CHART_TITLE).assertTextEquals(dataSet.data.label)
            val size = onNodeWithTag(TestTags.PIE_CHART).fetchSemanticsNode().size

            dataSet.data.item.labels.forEachIndexed { index, value ->
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
            // Arrange
            val selectedSliceIndex = 1
            val expectedTitle = dataSet.data.item.labels[selectedSliceIndex]
            val expectedPercentage =
                "${calculatePercentages(dataSet.data.item.points)[selectedSliceIndex]}%"

            // Act
            setContent {
                PieChart(
                    dataSet = dataSet,
                    style = PieChartDefaults.style(),
                    interactionEnabled = false,
                    animateOnStart = false,
                    selectedSliceIndex = selectedSliceIndex,
                )
            }

            // Assert
            onNodeWithTag(TestTags.PIE_CHART).isDisplayed()
            onNodeWithTag(TestTags.CHART_TITLE).assertTextEquals(expectedTitle)
            onNodeWithText(expectedPercentage).isDisplayed()
        }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun pieChart_withTapSelection_autoDeselectsAfterTimeout() =
        runComposeUiTest {
            // Arrange
            val slices = createPieSlices(dataSet.data.item.points)

            // Act
            setContent {
                PieChart(dataSet, PieChartDefaults.style())
            }

            // Assert
            val size = onNodeWithTag(TestTags.PIE_CHART).fetchSemanticsNode().size
            val sliceMiddlePosition =
                getCoordinatesForSlice(index = 0, size = size, slices = slices)
            val selectedLabel = dataSet.data.item.labels[0]
            onNodeWithTag(TestTags.PIE_CHART).performTouchInput {
                down(sliceMiddlePosition)
                up()
            }
            onNodeWithTag(TestTags.CHART_TITLE).assertTextEquals(selectedLabel)

            waitUntil(timeoutMillis = PIE_SELECTION_AUTO_DESELECT_TIMEOUT_MS + 2_000L) {
                runCatching {
                    onNodeWithTag(TestTags.CHART_TITLE).assertTextEquals(dataSet.data.label)
                }.isSuccess
            }
            onNodeWithTag(TestTags.CHART_TITLE).assertTextEquals(dataSet.data.label)
        }

    private data class PieSlice(
        val startDeg: Float,
        val sweepAngle: Float,
    )

    private fun createPieSlices(values: List<Double>): List<PieSlice> {
        val total = values.sum()
        var lastEndDeg = 0.0
        return values.map { slice ->
            val normalized = if (total == 0.0) 0.0 else slice / total
            val startDeg = lastEndDeg
            val endDeg = lastEndDeg + (normalized * 360)
            lastEndDeg = endDeg
            PieSlice(
                startDeg = startDeg.toFloat(),
                sweepAngle = (endDeg - startDeg).toFloat(),
            )
        }
    }

    private fun getCoordinatesForSlice(
        index: Int,
        size: IntSize,
        slices: List<PieSlice>,
    ): androidx.compose.ui.geometry.Offset {
        val slice = slices[index]
        val radius = size.width / 2
        val midAngle = slice.startDeg + (slice.sweepAngle / 2f)
        val radian = midAngle * (PI / 180)
        val middleRadius = radius / 2f
        val x = radius + middleRadius * cos(radian).toFloat()
        val y = radius + middleRadius * sin(radian).toFloat()
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
