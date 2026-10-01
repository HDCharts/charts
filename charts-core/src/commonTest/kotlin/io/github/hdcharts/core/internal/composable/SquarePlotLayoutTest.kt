package io.github.hdcharts.core.internal.composable

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.unit.dp
import io.github.hdcharts.core.internal.InternalChartsApi
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

private const val PLOT_TAG = "plot"
private const val TITLE_TAG = "title"
private const val LEGEND_TAG = "legend"

@OptIn(InternalChartsApi::class, ExperimentalTestApi::class)
class SquarePlotLayoutTest {
    /**
     * A chart wider than its square plot must center the plot, not stick it to the start edge.
     * This is the rule pie and radar both depend on.
     */
    @Test
    fun squarePlotLayout_wideBox_centersThePlot() =
        runComposeUiTest {
            setContent {
                ChartSquarePlotLayout(modifier = Modifier.size(width = 600.dp, height = 200.dp)) {
                    Plot()
                }
            }

            val plotBounds = onNodeWithTag(PLOT_TAG).fetchSemanticsNode().boundsInRoot

            assertEquals(expected = 300f, actual = plotBounds.center.x, absoluteTolerance = 1.5f)
            assertEquals(expected = 200f, actual = plotBounds.width, absoluteTolerance = 1.5f)
            assertEquals(expected = 200f, actual = plotBounds.height, absoluteTolerance = 1.5f)
        }

    /** The title and legend keep their own space, so a bounded height never squashes the plot. */
    @Test
    fun squarePlotLayout_boundedHeight_keepsRoomForTitleAndLegend() =
        runComposeUiTest {
            setContent {
                ChartSquarePlotLayout(
                    modifier = Modifier.size(width = 400.dp, height = 400.dp),
                    title = { Text(modifier = Modifier.testTag(TITLE_TAG), text = "Title") },
                    legend = { Text(modifier = Modifier.testTag(LEGEND_TAG), text = "Legend") },
                    plot = { Plot() },
                )
            }

            val titleBounds = onNodeWithTag(TITLE_TAG).fetchSemanticsNode().boundsInRoot
            val plotBounds = onNodeWithTag(PLOT_TAG).fetchSemanticsNode().boundsInRoot
            val legendBounds = onNodeWithTag(LEGEND_TAG).fetchSemanticsNode().boundsInRoot

            assertTrue { titleBounds.bottom <= plotBounds.top }
            assertTrue { plotBounds.bottom <= legendBounds.top }
            assertTrue { plotBounds.width < 400f }
        }

    /** The title and legend slots are optional, so a chart can render its plot alone. */
    @Test
    fun squarePlotLayout_withoutTitleOrLegend_fillsTheBox() =
        runComposeUiTest {
            setContent {
                ChartSquarePlotLayout(modifier = Modifier.size(width = 300.dp, height = 300.dp)) {
                    Plot()
                }
            }

            val plotBounds = onNodeWithTag(PLOT_TAG).fetchSemanticsNode().boundsInRoot

            assertEquals(expected = 300f, actual = plotBounds.width, absoluteTolerance = 1.5f)
        }

    /** Fills the plot box, so the tag's bounds are the square the layout gave the plot. */
    @Composable
    private fun Plot() {
        Box(modifier = Modifier.fillMaxSize().testTag(PLOT_TAG))
    }
}
