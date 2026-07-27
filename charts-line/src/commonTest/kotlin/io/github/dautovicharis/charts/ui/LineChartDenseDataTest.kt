package io.github.dautovicharis.charts.ui

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.click
import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.test.v2.runComposeUiTest
import io.github.dautovicharis.charts.LineChart
import io.github.dautovicharis.charts.internal.TestTags
import io.github.dautovicharis.charts.model.ChartDataSet
import io.github.dautovicharis.charts.model.toChartDataSet
import io.github.dautovicharis.charts.style.LineChartDefaults
import kotlin.test.Test
import kotlin.test.assertNotEquals

@OptIn(ExperimentalTestApi::class)
class LineChartDenseDataTest {
    private companion object {
        const val PLOT_START_PADDING_PX = 12f
    }

    @Test
    fun lineChart_withLargeDataset_showsCompactToggleByDefault() =
        runComposeUiTest {
            setContent {
                LineChart(dataSet = largeDataSet())
            }

            onNodeWithTag(TestTags.LINE_CHART).isDisplayed()
            onNodeWithTag(TestTags.LINE_CHART_DENSE_EXPAND).isDisplayed()
            onAllNodesWithTag(TestTags.LINE_CHART_ZOOM_OUT).assertCountEquals(0)
            onAllNodesWithTag(TestTags.LINE_CHART_ZOOM_IN).assertCountEquals(0)
        }

    @Test
    fun lineChart_smallDataset_doesNotShowZoomControls() =
        runComposeUiTest {
            setContent {
                LineChart(dataSet = smallDataSet())
            }

            onNodeWithTag(TestTags.LINE_CHART).isDisplayed()
            onAllNodesWithTag(TestTags.LINE_CHART_ZOOM_OUT).assertCountEquals(0)
            onAllNodesWithTag(TestTags.LINE_CHART_ZOOM_IN).assertCountEquals(0)
        }

    @Test
    fun lineChart_withZoomControlsHidden_doesNotRenderZoomButtons() =
        runComposeUiTest {
            setContent {
                LineChart(
                    dataSet = largeDataSet(),
                    style = LineChartDefaults.style(zoomControlsVisible = false),
                )
            }

            onAllNodesWithTag(TestTags.LINE_CHART_ZOOM_OUT).assertCountEquals(0)
            onAllNodesWithTag(TestTags.LINE_CHART_ZOOM_IN).assertCountEquals(0)
        }

    @Test
    fun lineChart_scrollThenTap_changesSelectedLabelAtSameViewportX() =
        runComposeUiTest {
            val dataSet = largeDataSet(title = "Dense Line Chart")
            val currentDataSet = mutableStateOf(dataSet)
            setContent {
                LineChart(dataSet = currentDataSet.value)
            }

            onNodeWithTag(TestTags.LINE_CHART_DENSE_EXPAND).performTouchInput { click() }
            onNodeWithTag(TestTags.LINE_CHART_DENSE_COLLAPSE).isDisplayed()
            onNodeWithTag(TestTags.LINE_CHART_ZOOM_OUT).isDisplayed()
            onNodeWithTag(TestTags.LINE_CHART_ZOOM_IN).isDisplayed()

            tapChartAt(x = 24f)
            waitUntil(timeoutMillis = 3_000L) {
                currentTitle() != dataSet.data.label
            }
            val beforeScrollTitle = currentTitle()

            onNodeWithTag(TestTags.LINE_CHART).performTouchInput {
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
        val chartNode = onNodeWithTag(TestTags.LINE_CHART).fetchSemanticsNode()
        val size = chartNode.size
        val chartLeft = chartNode.boundsInRoot.left
        val yAxisRight =
            runCatching {
                onNodeWithTag(TestTags.LINE_CHART_Y_AXIS_LABELS).fetchSemanticsNode().boundsInRoot.right
            }.getOrDefault(chartLeft)
        val plotStartX = (yAxisRight - chartLeft + PLOT_START_PADDING_PX).coerceAtLeast(0f)
        val safeX = (plotStartX + x).coerceIn(1f, (size.width - 1).coerceAtLeast(1).toFloat())
        val safeY = (size.height / 2f).coerceIn(1f, (size.height - 1).coerceAtLeast(1).toFloat())
        onNodeWithTag(TestTags.LINE_CHART).performTouchInput {
            click(Offset(x = safeX, y = safeY))
        }
    }

    private fun smallDataSet(points: Int = 12): ChartDataSet {
        val labels = dateLabels(points)
        val values = values(points)
        return values.toChartDataSet(
            title = "Small Line Chart",
            labels = labels,
        )
    }

    private fun largeDataSet(
        points: Int = 120,
        title: String = "Large Line Chart",
    ): ChartDataSet {
        val labels = dateLabels(points)
        val values = values(points)
        return values.toChartDataSet(
            title = title,
            labels = labels,
        )
    }

    private fun values(points: Int): List<Float> =
        List(points) { index ->
            ((index % 30) - 10).toFloat()
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
