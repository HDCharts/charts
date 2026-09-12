package io.github.dautovicharis.charts.ui

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
import io.github.dautovicharis.charts.StackedAreaChart
import io.github.dautovicharis.charts.internal.TestTags
import io.github.dautovicharis.charts.model.ChartData
import io.github.dautovicharis.charts.model.ChartSeries
import io.github.dautovicharis.charts.model.chartDataOf
import kotlin.test.Test
import kotlin.test.assertNotEquals

@OptIn(ExperimentalTestApi::class)
class StackedAreaChartDenseDataTest {
    private companion object {
        const val PLOT_START_PADDING_PX = 12f
    }

    @Test
    fun stackedAreaChart_scrollThenTap_changesSelectedLabelAtSameViewportX() =
        runComposeUiTest {
            val data = denseStackedAreaData()
            setContent {
                StackedAreaChart(data = data, title = "Dense Stacked Area")
            }

            onNodeWithTag(TestTags.STACKED_AREA_CHART_DENSE_EXPAND).performTouchInput { click() }
            onNodeWithTag(TestTags.STACKED_AREA_CHART_DENSE_COLLAPSE).isDisplayed()
            onNodeWithTag(TestTags.STACKED_AREA_CHART_ZOOM_OUT).isDisplayed()
            onNodeWithTag(TestTags.STACKED_AREA_CHART_ZOOM_IN).isDisplayed()

            tapChartAt(x = 24f)
            waitUntil(timeoutMillis = 3_000L) {
                currentTitle() != "Dense Stacked Area"
            }
            val beforeScrollTitle = currentTitle()

            onNodeWithTag(TestTags.STACKED_AREA_CHART).performTouchInput {
                swipeLeft()
                swipeLeft()
            }

            tapChartAt(x = 24f)
            waitUntil(timeoutMillis = 3_000L) {
                val title = currentTitle()
                title != beforeScrollTitle && title != "Dense Stacked Area"
            }
            val afterScrollTitle = currentTitle()

            assertNotEquals(beforeScrollTitle, afterScrollTitle)
            assertNotEquals("Dense Stacked Area", afterScrollTitle)
        }

    @Test
    fun stackedAreaChart_withSmallDataset_doesNotShowDenseControls() =
        runComposeUiTest {
            setContent {
                StackedAreaChart(data = smallStackedAreaData(), title = "Small Stacked Area")
            }

            onNodeWithTag(TestTags.STACKED_AREA_CHART).isDisplayed()
            onAllNodesWithTag(TestTags.STACKED_AREA_CHART_DENSE_EXPAND).assertCountEquals(0)
            onAllNodesWithTag(TestTags.STACKED_AREA_CHART_ZOOM_OUT).assertCountEquals(0)
            onAllNodesWithTag(TestTags.STACKED_AREA_CHART_ZOOM_IN).assertCountEquals(0)
        }

    private fun ComposeUiTest.currentTitle(): String {
        val semanticsNode = onNodeWithTag(TestTags.CHART_TITLE).fetchSemanticsNode()
        return semanticsNode.config[SemanticsProperties.Text]
            .joinToString(separator = "") { item -> item.text }
    }

    private fun ComposeUiTest.tapChartAt(x: Float) {
        val chartNode = onNodeWithTag(TestTags.STACKED_AREA_CHART).fetchSemanticsNode()
        val size = chartNode.size
        val chartLeft = chartNode.boundsInRoot.left
        val yAxisRight =
            runCatching {
                onNodeWithTag(TestTags.STACKED_AREA_CHART_Y_AXIS_LABELS).fetchSemanticsNode().boundsInRoot.right
            }.getOrDefault(chartLeft)
        val plotStartX = (yAxisRight - chartLeft + PLOT_START_PADDING_PX).coerceAtLeast(0f)
        val safeX = (plotStartX + x).coerceIn(1f, (size.width - 1).coerceAtLeast(1).toFloat())
        val safeY = (size.height / 2f).coerceIn(1f, (size.height - 1).coerceAtLeast(1).toFloat())
        onNodeWithTag(TestTags.STACKED_AREA_CHART).performTouchInput {
            click(Offset(x = safeX, y = safeY))
        }
    }

    private fun denseStackedAreaData(points: Int = 120): ChartData =
        chartDataOf(
            categories = List(points) { index -> "P${index + 1}" },
            ChartSeries(name = "Series A", values = List(points) { index -> 40.0 + (index % 8) }),
            ChartSeries(name = "Series B", values = List(points) { index -> 25.0 + (index % 6) }),
            ChartSeries(name = "Series C", values = List(points) { index -> 15.0 + (index % 5) }),
        )

    private fun smallStackedAreaData(points: Int = 8): ChartData =
        chartDataOf(
            categories = List(points) { index -> "P${index + 1}" },
            ChartSeries(name = "Series A", values = List(points) { index -> 20.0 + index }),
            ChartSeries(name = "Series B", values = List(points) { index -> 14.0 + (index % 4) }),
            ChartSeries(name = "Series C", values = List(points) { index -> 8.0 + (index % 3) }),
        )
}
