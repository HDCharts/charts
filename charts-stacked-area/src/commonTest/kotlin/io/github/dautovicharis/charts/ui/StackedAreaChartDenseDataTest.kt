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
import io.github.dautovicharis.charts.model.MultiChartDataSet
import io.github.dautovicharis.charts.model.toMultiChartDataSet
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
            val dataSet = denseStackedAreaDataSet()
            setContent {
                StackedAreaChart(dataSet = dataSet)
            }

            onNodeWithTag(TestTags.STACKED_AREA_CHART_DENSE_EXPAND).performTouchInput { click() }
            onNodeWithTag(TestTags.STACKED_AREA_CHART_DENSE_COLLAPSE).isDisplayed()
            onNodeWithTag(TestTags.STACKED_AREA_CHART_ZOOM_OUT).isDisplayed()
            onNodeWithTag(TestTags.STACKED_AREA_CHART_ZOOM_IN).isDisplayed()

            tapChartAt(x = 24f)
            waitUntil(timeoutMillis = 3_000L) {
                currentTitle() != dataSet.data.title
            }
            val beforeScrollTitle = currentTitle()

            onNodeWithTag(TestTags.STACKED_AREA_CHART).performTouchInput {
                swipeLeft()
                swipeLeft()
            }

            tapChartAt(x = 24f)
            waitUntil(timeoutMillis = 3_000L) {
                val title = currentTitle()
                title != beforeScrollTitle && title != dataSet.data.title
            }
            val afterScrollTitle = currentTitle()

            assertNotEquals(beforeScrollTitle, afterScrollTitle)
            assertNotEquals(dataSet.data.title, afterScrollTitle)
        }

    @Test
    fun stackedAreaChart_withSmallDataset_doesNotShowDenseControls() =
        runComposeUiTest {
            setContent {
                StackedAreaChart(dataSet = smallStackedAreaDataSet())
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

    private fun denseStackedAreaDataSet(points: Int = 120): MultiChartDataSet =
        listOf(
            "Series A" to List(points) { index -> 40f + (index % 8) },
            "Series B" to List(points) { index -> 25f + (index % 6) },
            "Series C" to List(points) { index -> 15f + (index % 5) },
        ).toMultiChartDataSet(
            title = "Dense Stacked Area",
            categories = List(points) { index -> "P${index + 1}" },
        )

    private fun smallStackedAreaDataSet(points: Int = 8): MultiChartDataSet =
        listOf(
            "Series A" to List(points) { index -> 20f + index },
            "Series B" to List(points) { index -> 14f + (index % 4) },
            "Series C" to List(points) { index -> 8f + (index % 3) },
        ).toMultiChartDataSet(
            title = "Small Stacked Area",
            categories = List(points) { index -> "P${index + 1}" },
        )
}
