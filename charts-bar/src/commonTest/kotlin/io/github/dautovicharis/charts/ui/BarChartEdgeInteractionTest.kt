package io.github.dautovicharis.charts.ui

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.click
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.test.v2.runComposeUiTest
import io.github.dautovicharis.charts.BarChart
import io.github.dautovicharis.charts.internal.TestTags
import io.github.dautovicharis.charts.model.ChartDataSet
import io.github.dautovicharis.charts.model.toChartDataSet
import kotlin.test.Test
import kotlin.test.assertNotEquals

@OptIn(ExperimentalTestApi::class)
class BarChartEdgeInteractionTest {
    private companion object {
        const val PLOT_START_PADDING_PX = 12f
    }

    @Test
    fun barChart_secondTapOnSameBar_togglesSelectionOff() =
        runComposeUiTest {
            val dataSet = largeDataSet()
            setContent {
                BarChart(dataSet = dataSet)
            }

            tapChartAt(x = 24f)
            waitUntil(timeoutMillis = 3_000L) {
                currentTitle() != dataSet.data.label
            }

            tapChartAt(x = 24f)
            waitUntil(timeoutMillis = 3_000L) {
                currentTitle() == dataSet.data.label
            }
            onNodeWithTag(TestTags.CHART_TITLE).assertTextEquals(dataSet.data.label)
        }

    @Test
    fun barChart_datasetReload_resetsTitleToNewDatasetTitleAfterSelection() =
        runComposeUiTest {
            val initialDataSet = largeDataSet(title = "Initial Bar Chart")
            val reloadedDataSet = largeDataSet(title = "Reloaded Bar Chart", valueShift = 7)
            val currentDataSet = mutableStateOf(initialDataSet)

            setContent {
                BarChart(dataSet = currentDataSet.value)
            }

            tapChartAt(x = 24f)
            waitUntil(timeoutMillis = 3_000L) {
                currentTitle() != initialDataSet.data.label
            }
            assertNotEquals(initialDataSet.data.label, currentTitle())

            runOnIdle {
                currentDataSet.value = reloadedDataSet
            }
            waitUntil(timeoutMillis = 3_000L) {
                runCatching {
                    onNodeWithTag(TestTags.CHART_TITLE).assertTextEquals(reloadedDataSet.data.label)
                }.isSuccess
            }
            onNodeWithTag(TestTags.CHART_TITLE).assertTextEquals(reloadedDataSet.data.label)
        }

    @Test
    fun barChart_scrollThenTap_changesSelectedLabelAtSameViewportX() =
        runComposeUiTest {
            val dataSet = largeDataSet(title = "Scrollable Bar Chart")
            setContent {
                BarChart(dataSet = dataSet)
            }

            onNodeWithTag(TestTags.BAR_CHART_DENSE_EXPAND).performTouchInput { click() }

            tapChartAt(x = 24f)
            waitUntil(timeoutMillis = 3_000L) {
                currentTitle() != dataSet.data.label
            }
            val beforeScrollTitle = currentTitle()

            onNodeWithTag(TestTags.BAR_CHART).performTouchInput {
                swipeLeft()
                swipeLeft()
            }

            tapChartAt(x = 24f)
            waitUntil(timeoutMillis = 3_000L) {
                val title = currentTitle()
                title != beforeScrollTitle && title != dataSet.data.label
            }
            val afterScrollTitle = currentTitle()

            assertNotEquals(beforeScrollTitle, afterScrollTitle)
            assertNotEquals(dataSet.data.label, afterScrollTitle)
        }

    private fun ComposeUiTest.currentTitle(): String {
        val semanticsNode = onNodeWithTag(TestTags.CHART_TITLE).fetchSemanticsNode()
        return semanticsNode.config[SemanticsProperties.Text]
            .joinToString(separator = "") { item -> item.text }
    }

    private fun ComposeUiTest.tapChartAt(x: Float) {
        val chartNode = onNodeWithTag(TestTags.BAR_CHART).fetchSemanticsNode()
        val size = chartNode.size
        val chartLeft = chartNode.boundsInRoot.left
        val yAxisRight =
            runCatching {
                onNodeWithTag(TestTags.BAR_CHART_Y_AXIS_LABELS).fetchSemanticsNode().boundsInRoot.right
            }.getOrDefault(chartLeft)
        val plotStartX = (yAxisRight - chartLeft + PLOT_START_PADDING_PX).coerceAtLeast(0f)
        val safeX = (plotStartX + x).coerceIn(1f, (size.width - 1).coerceAtLeast(1).toFloat())
        val safeY = (size.height / 2f).coerceIn(1f, (size.height - 1).coerceAtLeast(1).toFloat())
        onNodeWithTag(TestTags.BAR_CHART).performTouchInput {
            click(Offset(x = safeX, y = safeY))
        }
    }

    private fun largeDataSet(
        points: Int = 120,
        title: String = "Large Bar Chart",
        valueShift: Int = 0,
    ): ChartDataSet {
        val labels = dateLabels(points)
        val values =
            List(points) { index ->
                (((index + valueShift) % 30) - 15).toFloat()
            }
        return values.toChartDataSet(
            title = title,
            labels = labels,
        )
    }

    private fun dateLabels(points: Int): List<String> {
        val monthNames = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun")
        val monthLengths = listOf(31, 28, 31, 30, 31, 30)

        var month = 0
        var day = 1
        return List(points) {
            val label = "${monthNames[month]} ${day.toString().padStart(2, '0')}"
            day++
            if (day > monthLengths[month]) {
                day = 1
                month = (month + 1) % monthNames.size
            }
            label
        }
    }
}
