package io.github.hdcharts.pie

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import io.github.hdcharts.core.internal.TestTags
import io.github.hdcharts.core.internal.ValidationErrors
import io.github.hdcharts.core.model.ChartSelection
import io.github.hdcharts.core.model.ChartSeries
import io.github.hdcharts.core.model.ChartValueFormatter
import io.github.hdcharts.core.model.chartDataOf
import io.github.hdcharts.core.model.staticChartSelection
import io.github.hdcharts.core.model.toChartData
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.round
import kotlin.math.sin
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class PieChartTest {
    private val points: List<Double> = listOf(10.0, 20.0, 30.0, 40.0)
    private val pieData = points.toChartData(categories = LABELS)
    private val labels: List<String> = LABELS

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun pieChart_withValidData_displaysChart() =
        runComposeUiTest {
            setContent {
                PieChart(pieData, title = TITLE)
            }

            onNodeWithTag(TestTags.PIE_CHART).assertIsDisplayed()
            onNodeWithTag(TestTags.CHART_TITLE).assertTextEquals(TITLE)
        }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun pieChart_withBoundedSize_keepsLegendInsideContainer() =
        runComposeUiTest {
            setContent {
                PieChart(
                    data = pieData,
                    modifier = Modifier.size(width = 280.dp, height = 240.dp).testTag("pie-container"),
                    title = TITLE,
                    animateOnStart = false,
                )
            }

            val containerBounds = onNodeWithTag("pie-container").fetchSemanticsNode().boundsInRoot
            labels.forEach { label ->
                val legend = onNodeWithText(label).assertIsDisplayed().fetchSemanticsNode().boundsInRoot
                assertTrue(legend.bottom <= containerBounds.bottom)
            }
        }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun pieChart_insideVerticalScroll_displaysChart() =
        runComposeUiTest {
            setContent {
                Column(
                    modifier =
                        Modifier
                            .width(240.dp)
                            .verticalScroll(rememberScrollState()),
                ) {
                    PieChart(
                        data = pieData,
                        title = TITLE,
                        animateOnStart = false,
                    )
                }
            }

            val chartBounds = onNodeWithTag(TestTags.PIE_CHART).assertIsDisplayed().fetchSemanticsNode().boundsInRoot
            val legendBounds = onNodeWithText(labels.first()).fetchSemanticsNode().boundsInRoot
            assertTrue(
                chartBounds.bottom <= legendBounds.top,
                "Chart bounds $chartBounds overlap legend bounds $legendBounds",
            )
        }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun pieChart_withValidData_displayAndInteractWithChart() =
        runComposeUiTest {
            val slices = createPieSlices(points)
            val percentages = calculatePercentages(points)

            setContent {
                PieChart(pieData, title = TITLE)
            }

            onNodeWithTag(TestTags.PIE_CHART).assertIsDisplayed()
            onNodeWithTag(TestTags.CHART_TITLE).assertTextEquals(TITLE)
            val size = onNodeWithTag(TestTags.PIE_CHART).fetchSemanticsNode().size

            labels.forEachIndexed { index, value ->
                val sliceMiddlePosition =
                    getCoordinatesForSlice(index = index, size = size, slices = slices)
                onNodeWithTag(TestTags.PIE_CHART).performTouchInput {
                    down(sliceMiddlePosition)
                    up()
                }
                onNodeWithTag(TestTags.CHART_TITLE).assertTextEquals(value).assertIsDisplayed()
                onNodeWithText("${percentages[index]}%").assertIsDisplayed()
            }
        }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun pieChart_withInvalidData_displaysError() =
        runComposeUiTest {
            val invalidData = listOf(1.0).toChartData(categories = listOf("A"))
            val expectedError = ValidationErrors.tooFewValues(min = ValidationErrors.MIN_VALUES)

            setContent {
                PieChart(invalidData)
            }

            onNodeWithTag(TestTags.PIE_CHART).assertDoesNotExist()
            onNodeWithTag(TestTags.CHART_ERROR).assertIsDisplayed()
            onNodeWithText("${expectedError}\n").assertIsDisplayed()
        }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun pieChart_withSliceColors_rendersChart() =
        runComposeUiTest {
            setContent {
                PieChart(
                    data = pieData,
                    style = PieChartDefaults.style(slices = PieChartDefaults.slices(colors = colors)),
                )
            }

            onNodeWithTag(TestTags.PIE_CHART).assertIsDisplayed()
            onNodeWithTag(TestTags.CHART_ERROR).assertDoesNotExist()
        }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun pieChart_withColorCountMismatch_displaysError() =
        runComposeUiTest {
            val expectedError =
                ValidationErrors.colorCountMismatch(colors = 3, expected = 4, target = "value")

            setContent {
                PieChart(
                    data = pieData,
                    style =
                        PieChartDefaults.style(
                            slices = PieChartDefaults.slices(colors = colors.take(3)),
                        ),
                )
            }

            onNodeWithTag(TestTags.PIE_CHART).assertDoesNotExist()
            onNodeWithTag(TestTags.CHART_ERROR).assertIsDisplayed()
            onNodeWithText("${expectedError}\n").assertIsDisplayed()
        }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun pieChart_withTwoSeries_displaysError() =
        runComposeUiTest {
            val expectedError = ValidationErrors.exactlyOneSeries(count = 2)

            setContent {
                PieChart(
                    data =
                        chartDataOf(
                            categories = labels,
                            series =
                                arrayOf(
                                    ChartSeries(name = "One", values = points),
                                    ChartSeries(name = "Two", values = points),
                                ),
                        ),
                )
            }

            onNodeWithTag(TestTags.PIE_CHART).assertDoesNotExist()
            onNodeWithTag(TestTags.CHART_ERROR).assertIsDisplayed()
            onNodeWithText("${expectedError}\n").assertIsDisplayed()
        }

    /**
     * A pie with no categories has nothing to name its slices with, so it draws the slices and drops
     * the legend, the way a line chart drops its X-axis labels. It is not an error.
     */
    @OptIn(ExperimentalTestApi::class)
    @Test
    fun pieChart_withoutCategories_drawsWithoutLegend() =
        runComposeUiTest {
            setContent {
                PieChart(data = points.toChartData(), title = TITLE)
            }

            onNodeWithTag(TestTags.PIE_CHART).assertIsDisplayed()
            onNodeWithTag(TestTags.CHART_ERROR).assertDoesNotExist()
            onNodeWithText(labels.first()).assertDoesNotExist()
        }

    /** A selected slice with no category falls back to the chart title rather than an empty header. */
    @OptIn(ExperimentalTestApi::class)
    @Test
    fun pieChart_withBlankCategory_fallsBackToTheChartTitle() =
        runComposeUiTest {
            setContent {
                PieChart(
                    data = points.toChartData(categories = listOf("", "B", "C", "D")),
                    title = TITLE,
                    selection = staticChartSelection(0),
                )
            }

            onNodeWithTag(TestTags.PIE_CHART).assertIsDisplayed()
            onNodeWithTag(TestTags.CHART_TITLE).assertTextEquals(TITLE)
        }

    /** A selected slice with no category and no title still shows its share. */
    @OptIn(ExperimentalTestApi::class)
    @Test
    fun pieChart_withBlankCategoryAndNoTitle_showsTheShareAlone() =
        runComposeUiTest {
            setContent {
                PieChart(
                    data = points.toChartData(categories = listOf("", "B", "C", "D")),
                    selection = staticChartSelection(0),
                )
            }

            onNodeWithTag(TestTags.CHART_TITLE).assertDoesNotExist()
            onNodeWithText("${calculatePercentages(points)[0]}%").assertIsDisplayed()
        }

    /** A selected slice with no categories and no title still shows its share. */
    @OptIn(ExperimentalTestApi::class)
    @Test
    fun pieChart_withNoCategoriesAndNoTitle_showsTheShareAlone() =
        runComposeUiTest {
            setContent {
                PieChart(
                    data = points.toChartData(),
                    selection = staticChartSelection(0),
                )
            }

            onNodeWithTag(TestTags.CHART_TITLE).assertDoesNotExist()
            onNodeWithText("${calculatePercentages(points)[0]}%").assertIsDisplayed()
        }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun pieChart_withDonutStyle_displaysCorrectly() =
        runComposeUiTest {
            val slices = createPieSlices(points)
            val percentages = calculatePercentages(points)

            setContent {
                PieChart(
                    pieData,
                    style = PieChartDefaults.style(donut = PieChartDefaults.donut(holePercentage = 0.5f)),
                    title = TITLE,
                )
            }

            onNodeWithTag(TestTags.PIE_CHART).assertIsDisplayed()
            onNodeWithTag(TestTags.CHART_TITLE).assertTextEquals(TITLE)
            val size = onNodeWithTag(TestTags.PIE_CHART).fetchSemanticsNode().size

            labels.forEachIndexed { index, value ->
                val sliceMiddlePosition =
                    getCoordinatesForSlice(index = index, size = size, slices = slices)
                onNodeWithTag(TestTags.PIE_CHART).performTouchInput {
                    down(sliceMiddlePosition)
                    up()
                }
                onNodeWithTag(TestTags.CHART_TITLE).assertTextEquals(value).assertIsDisplayed()
                onNodeWithText("${percentages[index]}%").assertIsDisplayed()
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
                    pieData,
                    title = TITLE,
                    selection = staticChartSelection(selectedSliceIndex),
                )
            }

            onNodeWithTag(TestTags.PIE_CHART).assertIsDisplayed()
            onNodeWithTag(TestTags.CHART_TITLE).assertTextEquals(expectedTitle)
            onNodeWithText(expectedPercentage).assertIsDisplayed()
        }

    /** The selected share goes through the caller's formatter, in percent from 0 to 100. */
    @OptIn(ExperimentalTestApi::class)
    @Test
    fun pieChart_withSelectedValueFormatter_formatsTheSelectedShare() =
        runComposeUiTest {
            setContent {
                PieChart(
                    data = pieData,
                    title = TITLE,
                    selectedValueFormatter = ChartValueFormatter { value -> "share=$value" },
                    selection = staticChartSelection(1),
                    interactionEnabled = false,
                )
            }

            onNodeWithTag(TestTags.CHART_TITLE).assertTextEquals(labels[1])
            onNodeWithText("share=20.0").assertIsDisplayed()
        }

    /** The default keeps the share at two decimals with a `%` suffix. */
    @Test
    fun pieChartDefaults_selectedValueFormatter_formatsTheShareAsAPercentage() {
        assertEquals(expected = "33.33%", actual = PieChartDefaults.selectedValueFormatter.format(100.0 / 3))
        assertEquals(expected = "50.0%", actual = PieChartDefaults.selectedValueFormatter.format(50.0))
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun pieChart_withTapSelection_autoDeselectsAfterTimeout() =
        runComposeUiTest {
            val slices = createPieSlices(points)

            setContent {
                PieChart(pieData, title = TITLE)
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

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun pieChart_donutTapInsideHole_doesNotSelect() =
        runComposeUiTest {
            // 50% donut — tapping the dead center must not select any slice.
            setContent {
                PieChart(
                    pieData,
                    style = PieChartDefaults.style(donut = PieChartDefaults.donut(holePercentage = 50f)),
                    title = TITLE,
                )
            }

            val size = onNodeWithTag(TestTags.PIE_CHART).fetchSemanticsNode().size
            onNodeWithTag(TestTags.PIE_CHART).performTouchInput {
                down(
                    androidx.compose.ui.geometry
                        .Offset(size.width / 2f, size.height / 2f),
                )
                up()
            }
            onNodeWithTag(TestTags.CHART_TITLE).assertTextEquals(TITLE)
        }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun pieChart_allZeroValues_rendersBlankWithoutNaN() =
        runComposeUiTest {
            val zeroData = listOf(0.0, 0.0).toChartData(categories = listOf("Empty", "Nothing"))
            setContent {
                PieChart(data = zeroData, title = TITLE)
            }

            onNodeWithTag(TestTags.PIE_CHART).assertIsDisplayed()
            onNodeWithText("NaN").assertDoesNotExist()
        }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun pieChart_modifierForwardedToErrorBranch() =
        runComposeUiTest {
            // Forwarding the public modifier to the error branch is part of the hardening:
            // the error composable must respect caller-supplied layout (asserted here via testTag).
            setContent {
                PieChart(
                    data = listOf(1.0).toChartData(categories = listOf("A")),
                    modifier = Modifier.testTag(TestTags.CHART_ERROR),
                )
            }

            onAllNodesWithTag(TestTags.CHART_ERROR).assertCountEquals(2)
            onNodeWithTag(TestTags.PIE_CHART).assertDoesNotExist()
        }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun pieChart_externalSelectionAndClear_updateDetails() =
        runComposeUiTest {
            val selection = ChartSelection()
            setContent {
                PieChart(pieData, title = TITLE, selection = selection)
            }

            runOnIdle { selection.select(1) }
            onNodeWithTag(TestTags.CHART_TITLE).assertTextEquals(labels[1])
            onNodeWithText("20.0%").assertIsDisplayed()

            runOnIdle { selection.clear() }
            onNodeWithTag(TestTags.CHART_TITLE).assertTextEquals(TITLE)
            onNodeWithText("20.0%").assertDoesNotExist()
        }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun pieChart_externalSelection_doesNotAutoDeselect() =
        runComposeUiTest {
            val selection = ChartSelection()
            setContent {
                PieChart(pieData, title = TITLE, selection = selection)
            }

            mainClock.autoAdvance = false
            runOnIdle { selection.select(1) }
            mainClock.advanceTimeBy(
                PIE_SELECTION_AUTO_DESELECT_TIMEOUT_MS + 500L,
                ignoreFrameDuration = true,
            )
            runOnIdle {
                assertEquals(expected = 1, actual = selection.selectedIndex)
            }
            onNodeWithTag(TestTags.CHART_TITLE).assertTextEquals(labels[1])
        }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun pieChart_invalidReplacementData_clearsSelection() =
        runComposeUiTest {
            val selection = ChartSelection(initialIndex = 2)
            var data by mutableStateOf(pieData)
            setContent {
                PieChart(data, title = TITLE, selection = selection)
            }

            onNodeWithTag(TestTags.CHART_TITLE).assertTextEquals(labels[2])
            runOnIdle {
                data = listOf(1.0).toChartData(categories = listOf("Only"))
            }
            onNodeWithTag(TestTags.CHART_ERROR).assertIsDisplayed()
            runOnIdle {
                assertNull(actual = selection.selectedIndex)
            }
        }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun pieChart_repeatedTap_renewsTimeoutWithoutDuplicateNotification() =
        runComposeUiTest {
            val notifications = mutableListOf<Int?>()
            val selection = ChartSelection { notifications.add(it) }
            setContent {
                PieChart(pieData, title = TITLE, selection = selection)
            }

            waitForIdle()
            mainClock.autoAdvance = false

            fun tapFirstSlice() {
                val node = onNodeWithTag(TestTags.PIE_CHART)
                val position = getCoordinatesForSlice(0, node.fetchSemanticsNode().size, createPieSlices(points))
                node.performTouchInput {
                    down(position)
                    up()
                }
                repeat(2) { mainClock.advanceTimeByFrame() }
                waitForIdle()
            }

            fun assertSelectedOnce() {
                runOnIdle {
                    assertEquals(expected = 0, actual = selection.selectedIndex)
                    assertEquals(expected = listOf<Int?>(0), actual = notifications)
                }
                onNodeWithTag(TestTags.CHART_TITLE).assertTextEquals(labels[0])
            }

            tapFirstSlice()
            mainClock.advanceTimeBy(2_000L, ignoreFrameDuration = true)
            assertSelectedOnce()
            tapFirstSlice()

            // Past the first tap's deadline, but before the renewed deadline.
            mainClock.advanceTimeBy(1_500L, ignoreFrameDuration = true)
            assertSelectedOnce()
            mainClock.advanceTimeBy(1_000L, ignoreFrameDuration = true)
            assertSelectedOnce()

            mainClock.advanceTimeBy(600L, ignoreFrameDuration = true)
            repeat(2) { mainClock.advanceTimeByFrame() }
            waitForIdle()
            runOnIdle {
                assertNull(selection.selectedIndex)
                assertEquals(expected = listOf(0, null), actual = notifications)
            }
            onNodeWithTag(TestTags.CHART_TITLE).assertTextEquals(TITLE)
        }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun pieChart_replacedSelectionHolder_receivesGesturesAndOwnsTimeout() =
        runComposeUiTest {
            val oldSelection = ChartSelection()
            val newSelection = ChartSelection()
            var selection by mutableStateOf(oldSelection)
            setContent {
                PieChart(pieData, title = TITLE, selection = selection)
            }

            var size = onNodeWithTag(TestTags.PIE_CHART).fetchSemanticsNode().size
            val firstPosition = getCoordinatesForSlice(0, size, createPieSlices(points))
            onNodeWithTag(TestTags.PIE_CHART).performTouchInput {
                down(firstPosition)
                up()
            }
            onNodeWithTag(TestTags.CHART_TITLE).assertTextEquals(labels[0])

            runOnIdle { selection = newSelection }
            onNodeWithTag(TestTags.CHART_TITLE).assertTextEquals(TITLE)
            size = onNodeWithTag(TestTags.PIE_CHART).fetchSemanticsNode().size
            val secondPosition = getCoordinatesForSlice(1, size, createPieSlices(points))
            onNodeWithTag(TestTags.PIE_CHART).performTouchInput {
                down(secondPosition)
                up()
            }
            onNodeWithTag(TestTags.CHART_TITLE).assertTextEquals(labels[1])
            runOnIdle {
                assertEquals(expected = 0, actual = oldSelection.selectedIndex)
                assertEquals(expected = 1, actual = newSelection.selectedIndex)
            }

            waitUntil(timeoutMillis = PIE_SELECTION_AUTO_DESELECT_TIMEOUT_MS + 2_000L) {
                newSelection.selectedIndex == null
            }
            onNodeWithTag(TestTags.CHART_TITLE).assertTextEquals(TITLE)
            runOnIdle {
                assertEquals(expected = 0, actual = oldSelection.selectedIndex)
                assertNull(newSelection.selectedIndex)
            }
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
        if (total == 0.0 || !total.isFinite()) return List(values.size) { "0" }
        return values.map { value ->
            val percentage = (value / total) * 100
            val rounded = round(percentage * 100) / 100
            "$rounded"
        }
    }

    private companion object {
        const val TITLE = "Title"

        val LABELS = listOf("A", "B", "C", "D")

        val colors = listOf(Color.Red, Color.Green, Color.Cyan, Color.Black)
    }
}
