package io.github.dautovicharis.charts.ui

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.click
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.test.v2.runComposeUiTest
import io.github.dautovicharis.charts.StackedBarChart
import io.github.dautovicharis.charts.internal.TestTags
import io.github.dautovicharis.charts.model.ChartSelection
import io.github.dautovicharis.charts.model.ChartSeries
import io.github.dautovicharis.charts.model.chartDataOf
import kotlin.test.Test
import kotlin.test.assertNotEquals

@OptIn(ExperimentalTestApi::class)
class StackedBarChartDenseDataTest {
    private companion object {
        const val PLOT_START_PADDING_PX = 12f
    }

    @Test
    fun stackedBarChart_scrollThenTap_changesSelectedLabelAtSameViewportX() =
        runComposeUiTest {
            val dataSet = denseStackedBarDataSet()
            val selection = ChartSelection()
            setContent {
                StackedBarChart(data = dataSet, title = "Dense Stacked Bar", selection = selection)
            }

            onNodeWithTag(TestTags.STACKED_BAR_CHART_DENSE_EXPAND).performTouchInput { click() }
            onNodeWithTag(TestTags.STACKED_BAR_CHART_DENSE_COLLAPSE).assertIsDisplayed()
            onNodeWithTag(TestTags.STACKED_BAR_CHART_ZOOM_OUT).assertIsDisplayed()
            onNodeWithTag(TestTags.STACKED_BAR_CHART_ZOOM_IN).assertIsDisplayed()

            tapChartAt(x = 24f)
            waitUntil(timeoutMillis = 3_000L) { selection.selectedIndex != null }
            val beforeScrollIndex = selection.selectedIndex

            onNodeWithTag(TestTags.STACKED_BAR_CHART).performTouchInput {
                swipeLeft()
                swipeLeft()
            }

            selection.clear()
            tapChartAt(x = 24f)
            waitUntil(timeoutMillis = 3_000L) { selection.selectedIndex != null }
            val afterScrollIndex = selection.selectedIndex

            assertNotEquals(beforeScrollIndex, afterScrollIndex)
        }

    @Test
    fun stackedBarChart_withSmallDataset_doesNotShowDenseControls() =
        runComposeUiTest {
            setContent {
                StackedBarChart(data = smallStackedBarDataSet())
            }

            onNodeWithTag(TestTags.STACKED_BAR_CHART).assertIsDisplayed()
            onAllNodesWithTag(TestTags.STACKED_BAR_CHART_DENSE_EXPAND).assertCountEquals(0)
            onAllNodesWithTag(TestTags.STACKED_BAR_CHART_ZOOM_OUT).assertCountEquals(0)
            onAllNodesWithTag(TestTags.STACKED_BAR_CHART_ZOOM_IN).assertCountEquals(0)
        }

    private fun ComposeUiTest.tapChartAt(x: Float) {
        val chartNode = onNodeWithTag(TestTags.STACKED_BAR_CHART).fetchSemanticsNode()
        val size = chartNode.size
        val chartLeft = chartNode.boundsInRoot.left
        val yAxisRight =
            runCatching {
                onNodeWithTag(TestTags.STACKED_BAR_CHART_Y_AXIS_LABELS).fetchSemanticsNode().boundsInRoot.right
            }.getOrDefault(chartLeft)
        val plotStartX = (yAxisRight - chartLeft + PLOT_START_PADDING_PX).coerceAtLeast(0f)
        val safeX = (plotStartX + x).coerceIn(1f, (size.width - 1).coerceAtLeast(1).toFloat())
        val safeY = (size.height / 2f).coerceIn(1f, (size.height - 1).coerceAtLeast(1).toFloat())
        onNodeWithTag(TestTags.STACKED_BAR_CHART).performTouchInput {
            click(Offset(x = safeX, y = safeY))
        }
    }

    private fun denseStackedBarDataSet(bars: Int = 120) =
        List(bars) { index ->
            "Bar ${index + 1}" to
                listOf(
                    50f + (index % 9),
                    30f + (index % 7),
                    20f + (index % 5),
                    10f + (index % 3),
                )
        }.let { rows -> transpose(rows, listOf("S1", "S2", "S3", "S4")) }

    private fun smallStackedBarDataSet(bars: Int = 8) =
        List(bars) { index ->
            "Bar ${index + 1}" to
                listOf(
                    20f + index,
                    10f + (index % 4),
                    8f + (index % 3),
                )
        }.let { rows -> transpose(rows, listOf("A", "B", "C")) }

    private fun transpose(
        rows: List<Pair<String, List<Float>>>,
        segmentNames: List<String>,
    ) = chartDataOf(
        categories = rows.map { (barLabel, _) -> barLabel },
        *segmentNames
            .mapIndexed { segmentIndex, segmentName ->
                ChartSeries(
                    name = segmentName,
                    values = rows.map { (_, values) -> values[segmentIndex].toDouble() },
                )
            }.toTypedArray(),
    )
}
