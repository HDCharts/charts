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
import io.github.dautovicharis.charts.model.ChartData
import io.github.dautovicharis.charts.model.toChartData
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
            val title = "Large Bar Chart"
            val data = largeDataSet()
            setContent {
                BarChart(
                    data = data,
                    title = title,
                )
            }

            tapChartAt(x = 24f)
            waitUntil(timeoutMillis = 3_000L) {
                currentChartTitle() != title
            }

            tapChartAt(x = 24f)
            waitUntil(timeoutMillis = 3_000L) {
                currentChartTitle() == title
            }
            onNodeWithTag(TestTags.CHART_TITLE).assertTextEquals(title)
        }

    @Test
    fun barChart_datasetReload_resetsTitleToNewDatasetTitleAfterSelection() =
        runComposeUiTest {
            val initialTitle = "Initial Bar Chart"
            val reloadedTitle = "Reloaded Bar Chart"
            val initialData = largeDataSet()
            val reloadedData = largeDataSet(valueShift = 7)
            val currentData = mutableStateOf(initialData)
            val currentTitle = mutableStateOf(initialTitle)

            setContent {
                BarChart(
                    data = currentData.value,
                    title = currentTitle.value,
                )
            }

            tapChartAt(x = 24f)
            waitUntil(timeoutMillis = 3_000L) {
                currentChartTitle() != initialTitle
            }
            assertNotEquals(initialTitle, currentChartTitle())

            runOnIdle {
                currentData.value = reloadedData
                currentTitle.value = reloadedTitle
            }
            waitUntil(timeoutMillis = 3_000L) {
                runCatching {
                    onNodeWithTag(TestTags.CHART_TITLE).assertTextEquals(reloadedTitle)
                }.isSuccess
            }
            onNodeWithTag(TestTags.CHART_TITLE).assertTextEquals(reloadedTitle)
        }

    @Test
    fun barChart_scrollThenTap_changesSelectedLabelAtSameViewportX() =
        runComposeUiTest {
            val title = "Scrollable Bar Chart"
            val data = largeDataSet()
            setContent {
                BarChart(
                    data = data,
                    title = title,
                )
            }

            onNodeWithTag(TestTags.BAR_CHART_DENSE_EXPAND).performTouchInput { click() }

            tapChartAt(x = 24f)
            waitUntil(timeoutMillis = 3_000L) {
                currentChartTitle() != title
            }
            val beforeScrollTitle = currentChartTitle()

            onNodeWithTag(TestTags.BAR_CHART).performTouchInput {
                swipeLeft()
                swipeLeft()
            }

            tapChartAt(x = 24f)
            waitUntil(timeoutMillis = 3_000L) {
                val t = currentChartTitle()
                t != beforeScrollTitle && t != title
            }
            val afterScrollTitle = currentChartTitle()

            assertNotEquals(beforeScrollTitle, afterScrollTitle)
            assertNotEquals(title, afterScrollTitle)
        }

    private fun ComposeUiTest.currentChartTitle(): String {
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
        valueShift: Int = 0,
    ): ChartData {
        val labels = dateLabels(points)
        val values =
            List(points) { index ->
                (((index + valueShift) % 30) - 15).toDouble()
            }
        return values.toChartData(categories = labels)
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
